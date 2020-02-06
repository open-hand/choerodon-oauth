package io.choerodon.oauth.infra.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import io.choerodon.oauth.infra.common.util.*;

/**
 * @author wuguokai
 */
@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER - 2)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Value("${choerodon.oauth.login.path:/login}")
    private String loginPath;
    @Autowired
    private CustomAuthenticationDetailSource detailSource;
    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    private SingleLoginSessionInformationExpiredStrategy sessionInformationExpiredStrategy;
    @Autowired
    private OauthProperties properties;
    @Autowired
    private CustomLogoutSuccessHandler customLogoutSuccessHandler;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/login", "/is_login","/public/**", "/password/**", "/static/**", "/token",
                        "/forgetPassword/**", "/wechat/**", "/choerodon/config", "/actuator/**", "/v1/token_manager/*")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin()
                .loginPage(loginPath)
                .loginProcessingUrl("/login")
                .authenticationDetailsSource(detailSource)
                .failureHandler(customAuthenticationFailureHandler)
                .successHandler(customAuthenticationSuccessHandler)
                .and()
                .logout().deleteCookies("access_token").invalidateHttpSession(true)//deleteCookies:指定退出登录后需要删除的cookie名称，多个cookie之间以逗号分隔 ; invalidateHttpSession 默认为true,用户在退出后Http session失效
                .logoutSuccessHandler(customLogoutSuccessHandler)// 用来自定义退出成功后的操作
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .and()
                .csrf()
                .disable();
        if (properties.isEnabledSingleLogin()) {
            http.sessionManagement().maximumSessions(1).expiredSessionStrategy(sessionInformationExpiredStrategy);
        }
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
