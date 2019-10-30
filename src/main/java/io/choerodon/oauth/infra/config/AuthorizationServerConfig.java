package io.choerodon.oauth.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;

import javax.sql.DataSource;

import io.choerodon.oauth.domain.service.CustomClientDetailsService;
import io.choerodon.oauth.infra.common.util.ChoerodonRedirectResolver;
import io.choerodon.oauth.infra.common.util.CustomClientInterceptor;
import io.choerodon.oauth.infra.common.util.CustomTokenStore;
import io.choerodon.oauth.infra.common.util.CustomUserDetailsServiceImpl;


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

    public AuthorizationServerConfig(
            AuthenticationManager authenticationManager,
            CustomClientDetailsService clientDetailsService,
            CustomUserDetailsServiceImpl userDetailsService,
            CustomClientInterceptor customClientInterceptor,
            DataSource dataSource,
            CustomTokenStore tokenStore) {
        this.authenticationManager = authenticationManager;
        this.clientDetailsService = clientDetailsService;
        this.userDetailsService = userDetailsService;
        this.customClientInterceptor = customClientInterceptor;
        this.dataSource = dataSource;
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
                .userDetailsService(userDetailsService)
                .authenticationManager(authenticationManager)
                .redirectResolver(new ChoerodonRedirectResolver());
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
