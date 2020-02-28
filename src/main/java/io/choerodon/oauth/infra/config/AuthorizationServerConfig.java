package io.choerodon.oauth.infra.config;

import java.util.Arrays;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsByNameServiceWrapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

import javax.sql.DataSource;

import io.choerodon.oauth.domain.service.CustomClientDetailsService;
import io.choerodon.oauth.infra.common.util.*;


/**
 * @author wuguokai
 */
@Configuration
@EnableAuthorizationServer //提供/oauth/authorize,/oauth/token,/oauth/check_token,/oauth/confirm_access,/oauth/error
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    private AuthenticationManager authenticationManager;
    private CustomClientDetailsService clientDetailsService;
    private CustomUserDetailsServiceImpl userDetailsService;
    private CustomClientInterceptor customClientInterceptor;
    private DataSource dataSource;
    private CustomTokenStore tokenStore;
    private CustomTokenService customTokenService;

    public AuthorizationServerConfig(
            AuthenticationManager authenticationManager,
            CustomClientDetailsService clientDetailsService,
            CustomUserDetailsServiceImpl userDetailsService,
            CustomClientInterceptor customClientInterceptor,
            DataSource dataSource,
            CustomTokenService customTokenService,
            CustomTokenStore tokenStore) {
        this.authenticationManager = authenticationManager;
        this.clientDetailsService = clientDetailsService;
        this.userDetailsService = userDetailsService;
        this.customClientInterceptor = customClientInterceptor;
        this.dataSource = dataSource;
        this.customTokenService = customTokenService;
        this.tokenStore = tokenStore;
    }

    /**
     * 用来配置授权（authorization）以及令牌（token）的访问端点和令牌服务(token services)。
     * <p>
     * authenticationManager: 注入一个AuthenticationManager后，password grant将打开
     * userDetailsService: 如果注入了一个UserDetailsService,refresh token grant将对用户状态进行校验，以保证用户处于激活状态
     * authorizationCodeServices: 这个属性是用来设置授权码服务的（即 AuthorizationCodeServices 的实例对象），主要用于 "authorization_code" 授权码类型模式。
     * CustomTokenStore extends JdbcTokenStore: 令牌会被保存进关系型数据库
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .addInterceptor(customClientInterceptor)
                .authorizationCodeServices(new JdbcAuthorizationCodeServices(dataSource))
                .tokenStore(tokenStore)
                .tokenServices(setTokenService(endpoints))
                .userDetailsService(userDetailsService)
                .authenticationManager(authenticationManager)
                .redirectResolver(new ChoerodonRedirectResolver());
    }

    public CustomTokenService setTokenService(AuthorizationServerEndpointsConfigurer endpoints) {
        customTokenService.setTokenStore(tokenStore);
        customTokenService.setSupportRefreshToken(true);
        customTokenService.setReuseRefreshToken(true);
        customTokenService.setClientDetailsService(endpoints.getClientDetailsService());
        customTokenService.setTokenEnhancer(endpoints.getTokenEnhancer());
        addUserDetailsService(customTokenService, userDetailsService);
        return customTokenService;
    }

    private void addUserDetailsService(CustomTokenService tokenServices, UserDetailsService userDetailsService) {
        if (userDetailsService != null) {
            PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
            provider.setPreAuthenticatedUserDetailsService(new UserDetailsByNameServiceWrapper<>(
                    userDetailsService));
            tokenServices.setAuthenticationManager(new ProviderManager(Arrays.asList(provider)));
        }
    }

    /**
     * 配置客户端详情服务，客户端详情信息在这里进行初始化
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetailsService);
    }

    /**
     * 用来配置令牌端点(Token Endpoint)的安全约束
     * allowFormAuthenticationForClients:为了注册 clientCredentialsTokenEndpointFilter
     * ( clientCredentialsTokenEndpointFilter:
     * 解析request中的client_id和client_secret;构造成UsernamePasswordAuthenticationToken,
     * 然后通过UserDetailsService查询作简单的认证,一般是针对password模式和client_credentials
     * )
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer)
            throws Exception {
        oauthServer
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("permitAll()")
                .allowFormAuthenticationForClients();
    }

}
