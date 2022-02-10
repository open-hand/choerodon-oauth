package org.hzero.oauth.security.custom;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import org.apache.commons.lang3.StringUtils;
import org.hzero.boot.oauth.domain.entity.BaseClient;
import org.hzero.boot.oauth.domain.entity.BaseLdap;
import org.hzero.boot.oauth.domain.entity.BasePasswordPolicy;
import org.hzero.boot.oauth.domain.repository.BaseLdapRepository;
import org.hzero.boot.oauth.util.CustomBCryptPasswordEncoder;
import org.hzero.common.HZeroService;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.user.UserType;
import org.hzero.core.util.AssertUtils;
import org.hzero.core.util.EncryptionUtils;
import org.hzero.oauth.domain.entity.User;
import org.hzero.oauth.domain.repository.UserRepository;
import org.hzero.oauth.infra.encrypt.EncryptClient;
import org.hzero.oauth.security.config.SecurityProperties;
import org.hzero.oauth.security.constant.*;
import org.hzero.oauth.security.custom.tenant.vo.TenantVO;
import org.hzero.oauth.security.custom.user.UserLocateContext;
import org.hzero.oauth.security.custom.user.UserLocatorChain;
import org.hzero.oauth.security.custom.vo.ForceModifyInitPasswordVO;
import org.hzero.oauth.security.exception.CustomAuthenticationException;
import org.hzero.oauth.security.exception.ErrorWithTimesException;
import org.hzero.oauth.security.exception.LoginExceptions;
import org.hzero.oauth.security.service.LoginRecordService;
import org.hzero.oauth.security.service.UserAccountService;
import org.hzero.oauth.security.util.LoginUtil;
import org.hzero.oauth.security.util.MultiUserHelper;
import org.hzero.starter.captcha.domain.image.valid.ImageValidResult;
import org.hzero.starter.captcha.infra.builder.CaptchaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.LdapOperationsCallback;
import org.springframework.ldap.core.support.SingleContextSource;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.HardcodedFilter;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import io.choerodon.core.ldap.DirectoryType;

public class CustomAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationProvider.class);

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private BaseLdapRepository baseLdapRepository;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private LoginRecordService loginRecordService;
    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private EncryptClient encryptClient;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserLocatorChain userLocatorChain;
    @Autowired
    private PasswordEncoder passwordEncoder = new CustomBCryptPasswordEncoder();


    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        if (passwordEncoder != null) {
            this.passwordEncoder = passwordEncoder;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return super.supports(authentication) &&
                UsernamePasswordAuthenticationToken.class.getTypeName().equals(authentication.getTypeName());
    }

    @Override
    @SuppressWarnings("rawtypes")
    public UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) {
        // 解密账户信息
        String account = getDecryptAccount(username);
        // 获取当前登录用户信息
        Long userId = null;
        TenantVO tenantVO = TenantVO.EMPTY;
        Object details = authentication.getDetails();
        if (details instanceof CustomWebAuthenticationDetails) {
            CustomWebAuthenticationDetails customDetails = (CustomWebAuthenticationDetails) details;
            userId = customDetails.getChooseUserId();
            tenantVO = customDetails.getTenantVO();
        } else if (details instanceof Map) {
            Map mapDetails = (Map) details;
            userId = Optional.ofNullable(mapDetails.get("userId")).map(String::valueOf).map(Long::valueOf).orElse(null);
            tenantVO = Optional.ofNullable(mapDetails.get("tenantCode")).map(String::valueOf)
                    .map(tenantCode -> Optional.ofNullable(this.userRepository.selectTenantIdByNum(tenantCode))
                            .map(tenantId -> TenantVO.of(tenantId, tenantCode))
                            .orElse(TenantVO.of(tenantCode))
                    ).orElse(TenantVO.EMPTY);
        }

        // 获取用户信息
        TenantVO finalTenantVO = tenantVO;
        return MultiUserHelper.findChooseUser(userId)
                // 上面从session中获取选择的用户
                .map(user -> {
                    // 清理登录失败记录
                    String notExistsAccount = SecurityAttributes.PREFIX_ACCOUNT_NOT_EXISTS +
                            Optional.ofNullable(finalTenantVO.getTenantId()).orElse(-1L) + ":" + account;
                    this.loginRecordService.loginSuccess(notExistsAccount);

                    return Optional.of(user);
                }).orElseGet(() -> {
                    // 从session中未获取到选择的用户，在这里走用户筛选流程
                    String loginField = null;
                    String userType = null;

                    if (details instanceof CustomWebAuthenticationDetails) {
                        CustomWebAuthenticationDetails customDetails = (CustomWebAuthenticationDetails) details;
                        loginField = customDetails.getLoginField();
                        userType = customDetails.getUserType();
                    } else if (details instanceof Map) {
                        Map mapDetails = (Map) details;
                        loginField = (String) mapDetails.get(LoginUtil.FIELD_LOGIN_FIELD);
                        userType = (String) mapDetails.get(UserType.PARAM_NAME);
                    }

                    String rawPassword = getDecryptPassword(authentication);

                    // 获取用户对象
                    return this.userLocatorChain.locate(UserLocateContext.Builder.of()
                            // 登录字段不为空，就获取登录字段类型
                            .setLoginField(Optional.ofNullable(loginField).map(String::toUpperCase).map(LoginField::valueOf).orElse(null))
                            // 登录字段为空，就设置登录类型为ACCOUNT
                            .setLoginType(Optional.ofNullable(loginField).map(field -> (LoginType) null).orElse(LoginType.ACCOUNT))
                            // 处理用户类型
                            .setUserType(UserType.ofDefault(userType))
                            // 设置租户ID
                            .setTenantId(finalTenantVO.getTenantId())
                            .setPassword(rawPassword)
                            // 构建用户定位参数对象
                            .built(account));
                }).map(user -> {
                    // 获取到用户信息

                    // 缓存用户
                    this.loginRecordService.saveLocalLoginUser(user);
                    // 获取用户详情
                    return getUserDetailsService().loadUserByUsername(username);
                }).orElseGet(() -> {
                    // 账户不存在登录错误
                    this.accountNotExistsLoginError(finalTenantVO, account, authentication);
                    // 用户不存在，上一步必定抛出异常，这里返回空
                    return null;
                });
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) {
        // 检查验证码
        checkCaptcha(userDetails, authentication);
        BaseClient client = userAccountService.findCurrentClient();
        if (client == null) {
            throw new AuthenticationServiceException(LoginExceptions.CLIENT_NOT_FOUND.value());
        }
        // 检查密码重放
        checkReplayPassword(authentication, client);
        // 检查密码
        checkPassword(userDetails, authentication);
        // 校验用户账户有效性
        userAccountService.checkLoginUser(loginRecordService.getLocalLoginUser());
        // 检查是否可访问客户端
        checkAccessClient(userDetails, client);
    }

    /**
     * 检查密码重放
     */
    protected void checkReplayPassword(UsernamePasswordAuthenticationToken authentication, BaseClient client) {
        if (!BaseConstants.Flag.YES.equals(client.getPwdReplayFlag())) {
            return;
        }

        String md5Pass = EncryptionUtils.MD5.encrypt((String) authentication.getCredentials());
        boolean absent = loginRecordService.savePassIfAbsent(client.getName(), md5Pass,
                securityProperties.getLogin().getPassReplayExpire(), TimeUnit.DAYS);

        if (absent) {
            throw new AuthenticationServiceException(LoginExceptions.DUPLICATE_PASSWORD.value());
        }
    }

    /**
     * 检查角色能否访问客户端
     */
    protected void checkAccessClient(UserDetails userDetails, BaseClient client) {
        // 可访问角色ID
        String accessRoles = client.getAccessRoles();
        if (StringUtils.isEmpty(accessRoles)) {
            return;
        }
        User loginUser = loginRecordService.getLocalLoginUser();
        // 用户角色ID
        List<Long> roleIds = userRepository.selectUserRole(loginUser.getId());
        String[] roleIdArr = StringUtils.split(accessRoles, BaseConstants.Symbol.COMMA);
        for (String roleId : roleIdArr) {
            if (roleIds.contains(Long.parseLong(roleId))) {
                return;
            }
        }
        throw new CustomAuthenticationException(LoginExceptions.USER_NOT_ACCESS_CLIENT.value());
    }

    /**
     * 检查验证码
     *
     * @param userDetails    用户详情
     * @param authentication 认证对象
     */
    protected void checkCaptcha(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) {
        this.checkCaptcha(authentication, this::checkCaptcha);
    }

    /**
     * 检查验证码
     *
     * @param loginName      用户名
     * @param authentication 认证对象
     */
    protected void checkCaptcha(String loginName, UsernamePasswordAuthenticationToken authentication) {
        this.checkCaptcha(authentication, (captchaKey, captcha) -> {
            if (userAccountService.isNeedCaptcha(loginName)) {
                if (StringUtils.isBlank(captcha)) {
                    throw new AuthenticationServiceException(LoginExceptions.CAPTCHA_NULL.value());
                }
                ImageValidResult imageValidResult = CaptchaBuilder.Valid.Image.of(captchaKey, captcha)
                        .setPrefix(HZeroService.Oauth.CODE)
                        .execute();
                if (imageValidResult.isFailure()) {
                    throw new AuthenticationServiceException(LoginExceptions.CAPTCHA_ERROR.value());
                }
            }
        });
    }

    /**
     * 检查验证码
     *
     * @param authentication 认证对象
     * @param consumer       执行验证码校验的逻辑 两个参数分别是：captcha captchaCode
     */
    protected void checkCaptcha(UsernamePasswordAuthenticationToken authentication, BiConsumer<String, String> consumer) {
        String captchaKey = null;
        String captcha = null;

        // Web 请求
        if (authentication.getDetails() instanceof CustomWebAuthenticationDetails) {
            CustomWebAuthenticationDetails webDetails = (CustomWebAuthenticationDetails) authentication.getDetails();

            captchaKey = webDetails.getCaptchaKey();
            captcha = webDetails.getInputCaptcha();
        }
        // 接口调用
        else if (authentication.getDetails() instanceof Map) {
            Map parameters = (Map) authentication.getDetails();
            String sourceType = getParameterFromMap(parameters, LoginUtil.FIELD_SOURCE_TYPE);

            // 如果未传入 source_type 则不校验验证码，认为是标准OAuth2.0 授权方式
            if (StringUtils.isBlank(sourceType) ||
                    (LoginSource.APP.value().equalsIgnoreCase(sourceType) && !securityProperties.isEnableAppCaptcha())) {
                return;
            }

            captchaKey = getParameterFromMap(parameters, LoginUtil.FIELD_CAPTCHA_KEY);
            captcha = getParameterFromMap(parameters, LoginUtil.FIELD_CAPTCHA);
        }

        consumer.accept(captchaKey, captcha);
    }

    private String getParameterFromMap(Map parameters, String key) {
        return Optional.ofNullable(parameters.get(key)).map(Object::toString).orElse(null);
    }

    private void checkCaptcha(String captchaKey, String captcha) {
        User user = loginRecordService.getLocalLoginUser();
        if (userAccountService.isNeedCaptcha(user)) {
            if (StringUtils.isBlank(captcha)) {
                throw new AuthenticationServiceException(LoginExceptions.CAPTCHA_NULL.value());
            }
            ImageValidResult imageValidResult = CaptchaBuilder.Valid.Image.of(captchaKey, captcha)
                    .setPrefix(HZeroService.Oauth.CODE)
                    .execute();
            if (imageValidResult.isFailure()) {
                throw new AuthenticationServiceException(LoginExceptions.CAPTCHA_ERROR.value());
            }
        }
    }

    /**
     * 检查密码
     */
    protected void checkPassword(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) {
        String rawPassword = getDecryptPassword(authentication);

        boolean passed;
        User user = loginRecordService.getLocalLoginUser();
        if (user.getLdap()) {
            passed = ldapAuthentication(user.getOrganizationId(), userDetails.getUsername(), rawPassword);
        } else {
            passed = passwordEncoder.matches(rawPassword, userDetails.getPassword());
            // 检查是否强制修改密码
            if (passed) {
                checkPasswordModified(user);
            }
        }
        if (passed) {
            processPasswordLoginSuccess();
            return;
        }

        processPasswordLoginError();
    }

    /**
     * 解密账号
     *
     * @param content 加密的内容
     * @return 解密后的内容
     */
    protected String getDecryptAccount(String content) {
        // RSA 非对称加密
        try {
            // 获取当前用户的客户端配置信息
            BaseClient currentClient = this.userAccountService.findCurrentClient();

            // 是否需要对密码进行加密
            boolean isAccountEncrypt;
            if (Objects.isNull(currentClient)) {
                isAccountEncrypt = this.securityProperties.getLogin().isAccountEncrypt();
            } else {
                isAccountEncrypt = Boolean.TRUE.equals(currentClient.getAccountEncryptFlag());
            }

            if (isAccountEncrypt) {
                return encryptClient.decrypt(content);
            } else {
                return content;
            }
        } catch (Exception e) {
            LOGGER.error("decode account error. ex={}", e.getMessage());
            throw new AuthenticationServiceException(LoginExceptions.DECODE_PASSWORD_ERROR.value());
        }
    }

    /**
     * 获取客户端解密后的密码
     */
    protected String getDecryptPassword(UsernamePasswordAuthenticationToken authentication) {
        String credentials = null;
        try {
            // 获取当前用户的客户端配置信息
            BaseClient currentClient = this.userAccountService.findCurrentClient();

            // 是否需要对密码进行加密
            boolean isPasswordEncrypt;
            if (Objects.isNull(currentClient)) {
                isPasswordEncrypt = this.securityProperties.getLogin().isPasswordEncrypt();
            } else {
                isPasswordEncrypt = Boolean.TRUE.equals(currentClient.getPasswordEncryptFlag());
            }

            if (isPasswordEncrypt) {
                // RSA 非对称加密
                credentials = encryptClient.decrypt((String) authentication.getCredentials());
            } else {
                // 密码明文
                credentials = (String) authentication.getCredentials();
            }
        } catch (Exception e) {
            LOGGER.error("decode password error. ex={}", e.getMessage());
            throw new AuthenticationServiceException(LoginExceptions.DECODE_PASSWORD_ERROR.value());
        }
        return credentials;
    }

    /**
     * 密码认证成功处理
     */
    protected void processPasswordLoginSuccess() {
        User user = loginRecordService.getLocalLoginUser();
        loginRecordService.loginSuccess(user);
    }

    /**
     * 密码认证失败处理
     */
    protected void processPasswordLoginError() {
        // password error
        User loginUser = loginRecordService.getLocalLoginUser();
        long residualTimes = loginRecordService.loginError(loginUser);

        if (loginUser.getLocked()) {
            // 密码错误 已锁定
            throw new CustomAuthenticationException(LoginExceptions.LOGIN_ERROR_MORE_THEN_MAX_TIME.value(), loginUser.getLoginName());
        } else {
            // 密码错误 返回剩余次数
            if (residualTimes == -1) {
                throw new CustomAuthenticationException(LoginExceptions.USERNAME_OR_PASSWORD_ERROR.value());
            } else {
                ErrorWithTimesException ex = new ErrorWithTimesException(LoginExceptions.PASSWORD_ERROR.value(), residualTimes);
                ex.setErrorTimes(loginRecordService.getErrorTimes(loginUser));
                ex.setSurplusTimes(residualTimes);
                throw ex;
            }
        }
    }

    /**
     * 账户不存在登录错误
     *
     * @param tenantVO       租户信息
     * @param loginName      用户名
     * @param authentication 认证对象
     */
    protected void accountNotExistsLoginError(TenantVO tenantVO, String loginName, UsernamePasswordAuthenticationToken authentication) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        AssertUtils.notNull(requestAttributes, "Request Attributes Required");

        Long tenantId = Optional.ofNullable(tenantVO.getTenantId()).orElse(-1L);
        requestAttributes.setAttribute(SecurityAttributes.SECURITY_ACCOUNT_NOT_EXISTS, true, RequestAttributes.SCOPE_SESSION);
        requestAttributes.setAttribute(SecurityAttributes.SECURITY_TENANT_ID, tenantId, RequestAttributes.SCOPE_SESSION);

        // 账户名转换，主要用于防止当不存在的用户锁定时，创建了该用户，导致用户已锁定不可用
        // 对不存在的用户增加前缀用作区分
        String notExistsAccount = SecurityAttributes.PREFIX_ACCOUNT_NOT_EXISTS + tenantId + ":" + loginName;
        // 校验验证码
        this.checkCaptcha(notExistsAccount, authentication);

        // 还可以登录几次
        final long residualTimes = loginRecordService.loginError(notExistsAccount);


        // 判断类型
        String moreThanMaxTime = LoginExceptions.LOGIN_ERROR_MORE_THEN_MAX_TIME.value();
        String inputError = LoginExceptions.USERNAME_OR_PASSWORD_ERROR.value();
        String inputErrorLess = LoginExceptions.PASSWORD_ERROR.value();
        if (MultiUserHelper.isNeedInputTenant(requestAttributes)) {
            moreThanMaxTime = LoginExceptions.LOGIN_ERROR_MORE_THAN_MAX_TIME_WITH_TENANT.value();
            inputError = LoginExceptions.TENANT_OR_USERNAME_OR_PASSWORD_ERROR.value();
            inputErrorLess = LoginExceptions.TENANT_USERNAME_PASSWORD_ERROR.value();
        }

        if (0 == residualTimes) {
            // 密码错误 已锁定
            throw new CustomAuthenticationException(moreThanMaxTime, loginName, tenantVO.getTenantCode());
        } else {
            // 密码错误 返回剩余次数
            if (residualTimes == -1) {
                throw new CustomAuthenticationException(inputError);
            } else {
                ErrorWithTimesException ex = new ErrorWithTimesException(inputErrorLess, residualTimes);
                ex.setErrorTimes(loginRecordService.getErrorTimes(notExistsAccount));
                ex.setSurplusTimes(residualTimes);
                throw ex;
            }
        }
    }

    /**
     * 账户不存在登录错误
     *
     * @param loginName      用户名
     * @param authentication 认证对象
     * @see CustomAuthenticationProvider#accountNotExistsLoginError(TenantVO, String, UsernamePasswordAuthenticationToken)
     * @deprecated 调整用户登录名为租户唯一
     */
    @Deprecated
    protected void accountNotExistsLoginError(String loginName, UsernamePasswordAuthenticationToken authentication) {
        this.accountNotExistsLoginError(TenantVO.EMPTY, loginName, authentication);
    }

    /**
     * 检查是否强制修改密码
     *
     * @param user 用户
     */
    protected void checkPasswordModified(User user) {
        BasePasswordPolicy basePasswordPolicy = userAccountService.getPasswordPolicyByUser(user);
        // 校验密码是否过期
        if (basePasswordPolicy == null) {
            return;
        }
        if (userAccountService.isPasswordExpired(basePasswordPolicy, user)) {
            throw new CustomAuthenticationException(LoginExceptions.PASSWORD_EXPIRED.value());
        }

        // 校验是否开启了强制修改初始密码且当前用户未修改过初始密码
        if (userAccountService.isNeedForceModifyPassword(basePasswordPolicy, user)) {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            AssertUtils.notNull(requestAttributes, "Request Attributes Required");
            // 初始化强制修改初始密码值对象
            ForceModifyInitPasswordVO forceModifyInitPassword = new ForceModifyInitPasswordVO(user.getId(),
                    basePasswordPolicy.getForceCodeVerify(), user.getPhone(), user.getEmail());

            // 将值放入session
            requestAttributes.setAttribute(ForceModifyInitPasswordVO.SESSION_KEY, forceModifyInitPassword,
                    RequestAttributes.SCOPE_SESSION);
            throw new CustomAuthenticationException(LoginExceptions.PASSWORD_FORCE_MODIFY.value());
        }
    }

    private boolean ldapAuthentication(Long organizationId, String loginName, String credentials) {
        BaseLdap ldap = baseLdapRepository.selectLdap(organizationId);
        if (ldap != null && ldap.getEnabled()) {
            LdapContextSource contextSource = new LdapContextSource();
            String url = ldap.getServerAddress() + ":" + ldap.getPort();
            contextSource.setUrl(url);
            contextSource.setBase(ldap.getBaseDn());
            contextSource.afterPropertiesSet();
            LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
            // ad目录不设置会报错
            if (DirectoryType.MICROSOFT_ACTIVE_DIRECTORY.value().equals(ldap.getDirectoryType())) {
                ldapTemplate.setIgnorePartialResultException(true);
            }
            String userDn = null;
            boolean anonymousFetchFailed = false;
            try {
                List<String> names = ldapTemplate.search(
                        query()
                                .searchScope(SearchScope.SUBTREE)
                                .where("objectclass")
                                .is(ldap.getObjectClass())
                                .and(ldap.getLoginNameField())
                                .is(loginName),
                        new AbstractContextMapper<String>() {
                            @Override
                            protected String doMapFromContext(DirContextOperations ctx) {
                                return ctx.getNameInNamespace();
                            }
                        });
                userDn = getUserDn(names, ldap.getLoginNameField(), loginName);
            } catch (Exception e) {
                anonymousFetchFailed = true;
                LOGGER.error("ldap anonymous search objectclass = {}, {} = {} failed, exception {}",
                        ldap.getObjectClass(), ldap.getLoginNameField(), loginName, e);
            }
            if (anonymousFetchFailed) {
                userDn = accountAsUserDn2Authentication(loginName, ldap, contextSource, userDn);
            }
            return authentication(credentials, contextSource, userDn);
        } else {
            throw new AuthenticationServiceException(LoginException.LDAP_IS_DISABLE.value());
        }
    }

    private String accountAsUserDn2Authentication(String loginName, BaseLdap ldap, LdapContextSource contextSource, String userDn) {
        contextSource.setUserDn(ldap.getAccount());
        contextSource.setPassword(ldap.getLdapPassword());
        contextSource.afterPropertiesSet();
        LdapTemplate template = new LdapTemplate(contextSource);
        if (DirectoryType.MICROSOFT_ACTIVE_DIRECTORY.value().equals(ldap.getDirectoryType())) {
            template.setIgnorePartialResultException(true);
        }
        try {
            List<String> names = template.search(
                    query()
                            .searchScope(SearchScope.SUBTREE)
                            .where("objectclass")
                            .is(ldap.getObjectClass())
                            .and(ldap.getLoginNameField())
                            .is(loginName),
                    new AbstractContextMapper<String>() {
                        @Override
                        protected String doMapFromContext(DirContextOperations ctx) {
                            return ctx.getNameInNamespace();
                        }
                    });
            userDn = getUserDn(names, ldap.getLoginNameField(), loginName);
        } catch (Exception e) {
            LOGGER.error("use ldap account as userDn and password to authentication but search objectclass = {}, {} = {} failed, maybe the account or password is illegal, and check for the ldap config, exception {}",
                    ldap.getObjectClass(), ldap.getLoginNameField(), loginName, e);
            List<String> names = retryAccountAsUserDn2Authentication(loginName, ldap, template);
            userDn = getUserDn(names, ldap.getLoginNameField(), loginName);
        }
        return userDn;
    }

    /**
     * 补偿机制
     * 不能正常search的情况下 换种方式尝试获取ldap用户
     *
     * @param loginName
     * @param ldap
     * @param template
     * @return
     */
    private List<String> retryAccountAsUserDn2Authentication(String loginName, BaseLdap ldap, LdapTemplate template) {
        LOGGER.info("=====get it another way search====");
        //搜索控件
        final SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        //Filter
        AndFilter andFilter = getAndFilterByObjectClass(ldap);
        HardcodedFilter hardcodedFilter = new HardcodedFilter("(" + ldap.getLoginNameField() + "=" + loginName + ")");
        andFilter.and(hardcodedFilter);

        int batchSize = ldap.getSagaBatchSize();
        //分页PagedResultsDirContextProcessor
        final PagedResultsDirContextProcessor processor = new PagedResultsDirContextProcessor(batchSize);
        return SingleContextSource.doWithSingleContext(
                template.getContextSource(), (LdapOperationsCallback<List<String>>) operations -> {
                    ContextMapper attributesMapper = new AbstractContextMapper<String>() {
                        @Override
                        protected String doMapFromContext(DirContextOperations ctx) {
                            return ctx.getNameInNamespace();
                        }
                    };
                    return operations.search("", andFilter.toString(), searchControls, attributesMapper, processor);
                });
    }

    private AndFilter getAndFilterByObjectClass(BaseLdap ldapDO) {
        String objectClass = ldapDO.getObjectClass();
        String[] arr = objectClass.split(",");
        AndFilter andFilter = new AndFilter();
        for (String str : arr) {
            andFilter.and(new EqualsFilter("objectclass", str));
        }
        return andFilter;
    }

    private String getUserDn(List<String> names, String loginFiled, String loginName) {
        if (names.isEmpty()) {
            LOGGER.warn("user not found");
        } else if (names.size() == 1) {
            return names.get(0);
        } else {
            LOGGER.warn("user {} = {} is not unique", loginFiled, loginName);
        }
        return null;
    }

    private boolean authentication(String credentials, LdapContextSource contextSource, String userDn) {
        if (userDn == null) {
            return false;
        } else {
            DirContext ctx = null;
            try {
                ctx = contextSource.getContext(userDn, credentials);
                return true;
            } catch (Exception e) {
                logger.error("Login failed, userDn or credentials may be wrong, exception {}", e);
                return false;
            } finally {
                // It is imperative that the created DirContext instance is always closed
                LdapUtils.closeContext(ctx);
            }

        }
    }

    //
    // getter
    // ------------------------------------------------------------------------------


    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public BaseLdapRepository getBaseLdapRepository() {
        return baseLdapRepository;
    }

    public UserAccountService getUserAccountService() {
        return userAccountService;
    }

    public LoginRecordService getLoginRecordService() {
        return loginRecordService;
    }
}
