package io.choerodon.oauth.infra.config;

import javax.sql.DataSource;

import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;

import io.choerodon.oauth.infra.common.util.CustomClientDetailsService;
import io.choerodon.oauth.infra.common.util.CustomTokenServices;
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
    private OauthProperties choerodonOauthProperties;

    public AuthorizationServerConfig(AuthenticationManager authenticationManager,
                                     CustomClientDetailsService clientDetailsService,
                                     CustomUserDetailsServiceImpl userDetailsService,
                                     DataSource dataSource,
                                     CustomTokenStore tokenStore,
                                     OauthProperties choerodonOauthProperties) {
        this.authenticationManager = authenticationManager;
        this.clientDetailsService = clientDetailsService;
        this.userDetailsService = userDetailsService;
        this.dataSource = dataSource;
        this.tokenStore = tokenStore;
        this.choerodonOauthProperties = choerodonOauthProperties;
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


    @Bean
    @Primary
    public DefaultTokenServices tokenServices(ConfigurableEmbeddedServletContainer container) {
        container.setSessionTimeout(choerodonOauthProperties.getAccessTokenValiditySeconds());
        CustomTokenServices customTokenServices = new CustomTokenServices();
        customTokenServices.setTokenStore(tokenStore);
        customTokenServices.setSupportRefreshToken(true);
        return customTokenServices;
    }

}
