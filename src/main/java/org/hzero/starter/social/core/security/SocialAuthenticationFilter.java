/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.hzero.starter.social.core.security;

import static org.hzero.starter.social.core.common.constant.SocialConstant.*;
import static org.hzero.starter.social.core.exception.SocialErrorCode.OPEN_ID_ALREADY_BIND_OTHER_USER;
import static org.hzero.starter.social.core.exception.SocialErrorCode.USER_ALREADY_BIND;

import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.TokenUtils;
import org.hzero.starter.social.core.common.configurer.SocialConnectionFactoryBuilder;
import org.hzero.starter.social.core.common.connect.SocialUserData;
import org.hzero.starter.social.core.common.constant.ChannelEnum;
import org.hzero.starter.social.core.common.constant.SocialConstant;
import org.hzero.starter.social.core.configuration.CustomSocialConfiguration;
import org.hzero.starter.social.core.exception.RejectAuthorizationException;
import org.hzero.starter.social.core.exception.UserBindException;
import org.hzero.starter.social.core.exception.UserUnbindException;
import org.hzero.starter.social.core.provider.Provider;
import org.hzero.starter.social.core.provider.SocialProviderRepository;
import org.hzero.starter.social.core.provider.SocialUserProviderRepository;
import org.hzero.starter.social.core.security.holder.SocialSessionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.*;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.support.OAuth1ConnectionFactory;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.connect.web.HttpSessionSessionStrategy;
import org.springframework.social.connect.web.SessionStrategy;
import org.springframework.social.security.*;
import org.springframework.social.security.provider.OAuth1AuthenticationService;
import org.springframework.social.security.provider.OAuth2AuthenticationService;
import org.springframework.social.security.provider.SocialAuthenticationService;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * 跳转认证地址：/open/qq
 * 回调地址：/open/qq/callback，
 * 通过 Provider 返回三方应用信息
 * c7n覆盖私有方法{@link #getAuthentication} {@link #attemptAuthentication}
 *
 * @author bojiangzhou 2019/08/30
 */
public class SocialAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocialAuthenticationFilter.class);

    private static final String DEFAULT_FAILURE_URL = "/login";
    private static final String DEFAULT_FILTER_PROCESSES_URL = "/open/**";
    private static final String DEFAULT_BIND_URL = "/bind";
    private static final String REDIS_KEY_OPEN_APP_FORMAT = "open-app:%s:%s";


    private String bindUrl = DEFAULT_BIND_URL;
    private boolean attemptBind = false;
    private boolean updateBind = true;
    private boolean enableHttps = false;
    private String filterProcessesUrl = DEFAULT_FILTER_PROCESSES_URL;
    private String filterProcessesUrlPrefix;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private TokenStore tokenStore;
    private SocialAuthenticationServiceLocator authServiceLocator;
    private SimpleUrlAuthenticationFailureHandler delegateAuthenticationFailureHandler;
    private SocialUserProviderRepository userProviderRepository;
    private SocialProviderRepository socialProviderRepository;
    private ApplicationContext applicationContext;
    private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();

    public SocialAuthenticationFilter(AuthenticationManager authManager,
                                      TokenStore tokenStore,
                                      SocialUserProviderRepository userProviderRepository,
                                      SocialAuthenticationServiceLocator authServiceLocator,

                                      SocialProviderRepository socialProviderRepository,
                                      ApplicationContext applicationContext) {
        super(DEFAULT_FILTER_PROCESSES_URL);
        filterProcessesUrlPrefix = DEFAULT_FILTER_PROCESSES_URL.replace("/**", "");
        setAuthenticationManager(authManager);
        this.tokenStore = tokenStore;
        this.authServiceLocator = authServiceLocator;
        this.socialProviderRepository = socialProviderRepository;
        this.userProviderRepository = userProviderRepository;
        this.applicationContext = applicationContext;
        this.delegateAuthenticationFailureHandler = new SimpleUrlAuthenticationFailureHandler(DEFAULT_FAILURE_URL);
        super.setAuthenticationFailureHandler(new SocialAuthenticationFailureHandler(delegateAuthenticationFailureHandler));
    }

    /**
     * bind user page
     */
    public void setBindUrl(String bindUrl) {
        this.bindUrl = bindUrl;
    }

    /**
     * if redirect to bind page when user not bind.
     */
    public void setAttemptBind(boolean attemptBind) {
        this.attemptBind = attemptBind;
    }

    /**
     * if update bind providerUserId
     */
    public void setUpdateBind(boolean updateBind) {
        this.updateBind = updateBind;
    }

    /**
     * if enable https
     */
    public void setEnableHttps(boolean enableHttps) {
        this.enableHttps = enableHttps;
    }

    /**
     * The URL to redirect to if authentication fails or if authorization is denied by the user.
     *
     * @param defaultFailureUrl The failure URL. Defaults to "/signin" (relative to the servlet context).
     */
    public void setDefaultFailureUrl(String defaultFailureUrl) {
        delegateAuthenticationFailureHandler.setDefaultFailureUrl(defaultFailureUrl);
    }

    public void setPostLoginUrl(String postLoginUrl) {
        AuthenticationSuccessHandler successHandler = getSuccessHandler();
        if (successHandler instanceof AbstractAuthenticationTargetUrlRequestHandler) {
            AbstractAuthenticationTargetUrlRequestHandler h = (AbstractAuthenticationTargetUrlRequestHandler) successHandler;
            h.setDefaultTargetUrl(postLoginUrl);
        } else {
            throw new IllegalStateException("can't set postLoginUrl on unknown successHandler, type is " + successHandler.getClass().getName());
        }
    }

    public void setAlwaysUsePostLoginUrl(boolean alwaysUsePostLoginUrl) {
        AuthenticationSuccessHandler successHandler = getSuccessHandler();
        if (successHandler instanceof AbstractAuthenticationTargetUrlRequestHandler) {
            AbstractAuthenticationTargetUrlRequestHandler h = (AbstractAuthenticationTargetUrlRequestHandler) successHandler;
            h.setAlwaysUseDefaultTargetUrl(alwaysUsePostLoginUrl);
        } else {
            throw new IllegalStateException("can't set alwaysUsePostLoginUrl on unknown successHandler, type is " + successHandler.getClass().getName());
        }
    }

    public void setPostFailureUrl(String postFailureUrl) {
        AuthenticationFailureHandler failureHandler = getFailureHandler();
        if (failureHandler instanceof SimpleUrlAuthenticationFailureHandler) {
            SimpleUrlAuthenticationFailureHandler h = (SimpleUrlAuthenticationFailureHandler) failureHandler;
            h.setDefaultFailureUrl(postFailureUrl);
        } else {
            throw new IllegalStateException("can't set postFailureUrl on unknown failureHandler, type is " + failureHandler.getClass().getName());
        }
    }

    /**
     * Sets a strategy to use when persisting information that is to survive past the boundaries of a
     * request. The default strategy is to set the data as attributes in the HTTP Session.
     *
     * @param sessionStrategy the session strategy.
     */
    public void setSessionStrategy(SessionStrategy sessionStrategy) {
        this.sessionStrategy = sessionStrategy;
    }

    public SocialProviderRepository getSocialProviderRepository() {
        return socialProviderRepository;
    }

    public SocialAuthenticationServiceLocator getAuthServiceLocator() {
        return authServiceLocator;
    }

    /**
     * 判断是否拦截请求 - 请求认证地址：/open/qq?channel=pc - 回调地址：/open/qq/callback?xxx
     */
    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        if (!super.requiresAuthentication(request, response)) {
            return false;
        }
        String providerId = getRequestedProviderId(request);
        if (providerId == null) {
            return false;
        }
        String[] arr = providerId.split("@");
        String openAppCode = arr[0];
        String tenantId = arr[2];
        String redisKey = String.format(REDIS_KEY_OPEN_APP_FORMAT, openAppCode, tenantId);
        String openAppStr = stringRedisTemplate.opsForValue().get(redisKey);
        if (openAppStr == null) {
            List<Provider> providerList = Optional.ofNullable(socialProviderRepository.getProvider(Provider.realProviderId(providerId))).orElse(new ArrayList<>());
            Provider nowProvider = providerList.stream()
                    .filter(provider -> providerId.equals(Provider.uniqueProviderId(provider.getProviderId(), provider.getChannel(), provider.getOrganizationId())))
                    .findFirst()
                    .orElse(null);

            if (nowProvider == null) {
                //删除的三方方式不可认证
                return false;
            }
            SocialAuthenticationServiceLocator locator = getAuthServiceLocator();
            //刷新 Provider 使得Provider参数在界面修改后可以立即生效
            refreshAuthProviders(nowProvider, (SocialAuthenticationServiceRegistry) locator, providerId);
            stringRedisTemplate.opsForValue().set(redisKey, "1", 5, TimeUnit.MINUTES);
        }
        Set<String> authProviders = getAuthServiceLocator().registeredAuthenticationProviderIds();

        return !authProviders.isEmpty() && authProviders.contains(providerId);
    }

    private void refreshAuthProviders(Provider provider, SocialAuthenticationServiceRegistry locator, String uniqueProviderId) {
        if (!ObjectUtils.isEmpty(provider)) {
            Provider newProvider = new Provider(uniqueProviderId, provider.getChannel(),
                    provider.getAppId(), provider.getAppKey(), provider.getSubAppId(), provider.getOrganizationId());

            CustomSocialConfiguration customSocialConfiguration = getDependency(applicationContext, CustomSocialConfiguration.class);
            SocialConnectionFactoryBuilder socialConnectionFactoryBuilder = customSocialConfiguration.getSocialConnectionFactoryByProviderId(Provider.realProviderId(uniqueProviderId));
            locator.addAuthenticationService(wrapAsSocialAuthenticationService(socialConnectionFactoryBuilder.buildConnectionFactory(newProvider)));
        }
    }

    private <A> SocialAuthenticationService<A> wrapAsSocialAuthenticationService(ConnectionFactory<A> cf) {
        if (cf instanceof OAuth1ConnectionFactory) {
            return new OAuth1AuthenticationService<A>((OAuth1ConnectionFactory<A>) cf);
        } else if (cf instanceof OAuth2ConnectionFactory) {
            final OAuth2AuthenticationService<A> authService = new OAuth2AuthenticationService<A>((OAuth2ConnectionFactory<A>) cf);
            authService.setDefaultScope(((OAuth2ConnectionFactory<A>) cf).getScope());
            return authService;
        }
        throw new IllegalArgumentException("The connection factory must be one of OAuth1ConnectionFactory or OAuth2ConnectionFactory");
    }

    private <T> T getDependency(ApplicationContext applicationContext, Class<T> dependencyType) {
        try {
            T dependency = applicationContext.getBean(dependencyType);
            return dependency;
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalStateException("SpringSocialConfigurer depends on " + dependencyType.getName()
                    + ". No single bean of that type found in application context.", e);
        }
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (detectRejection(request)) {
            if (logger.isDebugEnabled()) {
                logger.debug("A rejection was detected. Failing authentication.");
            }
            throw new RejectAuthorizationException("Authentication failed because user rejected authorization.");
        }
        Authentication auth;
        // 添加catch覆盖 三方登录失败 跳转到正常页面登录 todo
        try {
            String providerId = getRequestedProviderId(request);

            SocialAuthenticationService<?> authService = getAuthServiceLocator().getAuthenticationService(providerId);
            auth = attemptAuthService(authService, request, response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AuthenticationServiceException(e.getMessage());
        }
        if (auth == null) {
            throw new AuthenticationServiceException("authentication failed");
        }
        return auth;
    }

    @Override
    public void setFilterProcessesUrl(String filterProcessesUrl) {
        super.setFilterProcessesUrl(filterProcessesUrl);
        this.filterProcessesUrl = filterProcessesUrl;
        this.filterProcessesUrlPrefix = filterProcessesUrl.replace("/**", "");
    }

    /**
     * 判断用户是否拒绝授权，只有回调时才有这种情况
     */
    protected boolean detectRejection(HttpServletRequest request) {
        String uri = request.getRequestURI();
        Set<?> parameterKeys = request.getParameterMap().keySet();
        // 回调接口
        if (uri.contains(DEFAULT_CALLBACK_SUFFIX)) {
            return parameterKeys.size() > 0
                    && !parameterKeys.contains("oauth_token")
                    && !parameterKeys.contains("code")
                    && !parameterKeys.contains("scope");
        }
        return false;
    }

    protected Authentication attemptAuthService(final SocialAuthenticationService<?> authService, final HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!callbackRequest(request)) {
            // state 参数
            String state = UUID.randomUUID().toString();
            request.setAttribute(PARAM_STATE, state);
            // 缓存用户 access_token
            String accessToken = TokenUtils.getToken(request);
            if (StringUtils.isNotBlank(accessToken)) {
                SocialSessionHolder.add(request, PREFIX_ACCESS_TOKEN, state, accessToken);
            }
            String bindRedirectUrl = request.getParameter(PARAM_BIND_REDIRECT_URI);
            if (StringUtils.isNotBlank(bindRedirectUrl)) {
                SocialSessionHolder.add(request, PREFIX_REDIRECT_URL, state, bindRedirectUrl);
            }
        }

        request.setAttribute("enableHttps", enableHttps);
        final SocialAuthenticationToken token = authService.getAuthToken(request, response);
        if (token == null) {
            return null;
        }

        Assert.notNull(token.getConnection(), "token connection is null.");

        Authentication auth = getAuthentication(request);
        if (auth == null || !auth.isAuthenticated()) {
            // 用户未登录，校验三方账号绑定的本地账号并进行登录
            return doAuthentication(authService, request, token);
        } else {
            // 用户已登录，绑定当前登录账号
            addConnection(authService, request, token, auth);
            return null;
        }
    }

    protected Authentication doAuthentication(SocialAuthenticationService<?> authService, HttpServletRequest request, SocialAuthenticationToken token) {
        try {
            if (!authService.getConnectionCardinality().isAuthenticatePossible()) {
                return null;
            }
            token.setDetails(authenticationDetailsSource.buildDetails(request));
            Authentication success = getAuthenticationManager().authenticate(token);
            Assert.isInstanceOf(UserDetails.class, success.getPrincipal(), "unexpected principle type");
            return success;
        } catch (UserUnbindException e) {
            // 没有跳转绑定逻辑
           /* if (attemptBind && bindUrl != null) {
                // tokenStore ConnectionData in session and redirect to bind page
                ProviderBindHelper.setConnection(request, token.getConnection());
                //sessionStrategy.setAttribute(new ServletWebRequest(request), ProviderBindHelper.SESSION_ATTRIBUTE, new ProviderBindHelper(token.getConnection()));
                throw new SocialAuthenticationRedirectException(buildBindUrl(request));
            }*/
            throw e;
        }
    }

    protected Authentication getAuthentication(HttpServletRequest request) {
//        if (callbackRequest(request)) {
//            String state = request.getParameter(PARAM_STATE);
//            request.setAttribute(PARAM_STATE, state);
//            String accessToken = SocialSessionHolder.get(request, PREFIX_ACCESS_TOKEN, state);
//            if (StringUtils.isNotBlank(accessToken)) {
//                Authentication authentication = tokenStore.readAuthentication(accessToken);
//                if (authentication != null) {
//                    authentication.setAuthenticated(true);
//                    return authentication;
//                }
//            }
//        }
        // todo 覆盖逻辑 上下文获取的认证不准
        return null;
    }

    protected void addConnection(final SocialAuthenticationService<?> authService, HttpServletRequest request, SocialAuthenticationToken token, Authentication auth) {
        Object o = auth.getPrincipal();
        String userId = null;
        if (o instanceof UserDetails) {
            UserDetails self = (UserDetails) o;
            userId = self.getUsername();
        }
        ConnectionData connectionData = token.getConnection().createData();
        if (userId == null || connectionData == null) {
            return;
        }

        Connection<?> connection = addConnection(authService, userId, connectionData);
        String redirectUrl = SocialSessionHolder.remove(request, PREFIX_REDIRECT_URL, request.getParameter(PARAM_STATE));
        if (redirectUrl == null) {
            // use default instead
            redirectUrl = authService.getConnectionAddedRedirectUrl(request, connection);
        }
        throw new SocialAuthenticationRedirectException(redirectUrl);
    }

    protected Connection<?> addConnection(SocialAuthenticationService<?> authService, String userId, ConnectionData data) {
        String username = userProviderRepository.findUsernameByProviderId(data.getProviderId(), data.getProviderUserId());
        if (username != null && StringUtils.equals(username, userId)) {
            // already bind
            throw new UserBindException(USER_ALREADY_BIND);
        }
        if (username != null && !StringUtils.equals(username, userId)) {
            // already bind other user
            throw new UserBindException(OPEN_ID_ALREADY_BIND_OTHER_USER);
        }

        List<SocialUserData> providerUsers = userProviderRepository.findProviderUser(data.getProviderId(), userId);
        if (CollectionUtils.isNotEmpty(providerUsers)) {
            // 如果 provider 下没有 unionId 或 只要有一个 unionId 相同就认为已绑定
            if (providerUsers.stream().allMatch(p -> StringUtils.isBlank(p.getProviderUnionId()))
                    || providerUsers.stream().anyMatch(p -> StringUtils.equals(p.getProviderUnionId(), data.getProviderUnionId()))) {
                // already bind
                throw new UserBindException(USER_ALREADY_BIND);
            }
        }

        // bind
        SocialUserData socialUserData = new SocialUserData(data);
        userProviderRepository.createUserBind(userId, data.getProviderId(), data.getProviderUserId(), socialUserData);

        Connection<?> connection = authService.getConnectionFactory().createConnection(data);
        connection.sync();
        return connection;
    }

    protected String getRequestedProviderId(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // uri must start with context path
        uri = uri.substring(request.getContextPath().length());
        // remaining uri must start with filterProcessesUrl
        if (!uri.startsWith(filterProcessesUrlPrefix)) {
            return null;
        }
        uri = uri.substring(filterProcessesUrlPrefix.length());

        // /filterProcessesUrl/providerId/callback
        String providerId;
        if (uri.startsWith("/")) {
            providerId = StringUtils.split(uri, "/")[0];
        } else {
            return null;
        }
        String channel = request.getParameter(SocialConstant.PARAM_CHANNEL);
        String organizationId = request.getParameter(SocialConstant.PARAM_ORGANIZATION_ID);
        if (StringUtils.isBlank(channel)) {
            channel = ChannelEnum.pc.name();
            logger.warn("Param channel is blank, use default pc channel.");
        }
        if (StringUtils.isBlank(organizationId) || !StringUtils.isNumeric(organizationId)) {
            organizationId = ((Long) Optional.ofNullable(request.getSession().getAttribute("openAppOrganizationId")).orElse(BaseConstants.DEFAULT_TENANT_ID)).toString();
        }
        return Provider.uniqueProviderId(providerId, channel, Long.parseLong(organizationId));
    }

    protected boolean callbackRequest(HttpServletRequest request) {
        return request.getRequestURI().endsWith("/" + DEFAULT_CALLBACK_SUFFIX) && request.getParameter(PARAM_STATE) != null;
    }

    protected String buildBindUrl(HttpServletRequest request) {
        if (bindUrl.startsWith("http://") || bindUrl.startsWith("https://")) {
            return bindUrl;
        }
        String returnUrl;
        if (!bindUrl.startsWith("/")) {
            returnUrl = ServletUriComponentsBuilder.fromContextPath(request).path("/" + bindUrl).build().toUriString();
        } else {
            returnUrl = ServletUriComponentsBuilder.fromContextPath(request).path(bindUrl).build().toUriString();
        }

        if (enableHttps && returnUrl.startsWith("http://")) {
            returnUrl = returnUrl.replace("http://", "https://");
        }
        return returnUrl;
    }

}
