package io.choerodon.oauth.infra.config;

import io.choerodon.oauth.infra.common.util.ChoerodonAuthenticationProvider;
import io.choerodon.oauth.infra.common.util.ChoerodonBcryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * @author wuguokai
 */
@Configuration
public class GlobalAuthenticationConfig extends GlobalAuthenticationConfigurerAdapter {

    @Autowired
    private ChoerodonAuthenticationProvider choerodonAuthenticationProvider;

    @Override
    public void init(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(choerodonAuthenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new ChoerodonBcryptPasswordEncoder();
    }

}
