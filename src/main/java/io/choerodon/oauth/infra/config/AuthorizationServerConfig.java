package io.choerodon.oauth.infra.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;

import io.choerodon.oauth.domain.service.CustomClientDetailsService;
import io.choerodon.oauth.infra.common.util.CustomTokenStore;
import io.choerodon.oauth.infra.common.util.CustomUserDetailsServiceImpl;


/**
 * @author wuguokai
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
    private AuthenticationManager authenticationManager;
    private CustomClientDetailsService clientDetailsService;
    private CustomUserDetailsServiceImpl userDetailsService;
    private DataSource dataSource;
    private CustomTokenStore tokenStore;

    public AuthorizationServerConfig(
            AuthenticationManager authenticationManager,
            CustomClientDetailsService clientDetailsService,
            CustomUserDetailsServiceImpl userDetailsService,
            DataSource dataSource,
            CustomTokenStore tokenStore) {
        this.authenticationManager = authenticationManager;
        this.clientDetailsService = clientDetailsService;
        this.userDetailsService = userDetailsService;
        this.dataSource = dataSource;
        this.tokenStore = tokenStore;
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .authorizationCodeServices(new JdbcAuthorizationCodeServices(dataSource))
                .tokenStore(tokenStore)
                .userDetailsService(userDetailsService)
                .authenticationManager(authenticationManager);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetailsService);
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer)
            throws Exception {
        oauthServer
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("permitAll()")
                .allowFormAuthenticationForClients();
    }

}
