package io.choerodon.oauth.security.config;

import org.hzero.oauth.infra.constant.Constants;
import org.hzero.oauth.security.config.SecurityProperties;
import org.hzero.oauth.security.custom.CustomAuthenticationKeyGenerator;
import org.hzero.oauth.security.util.LoginUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.session.SessionRepository;

import io.choerodon.oauth.security.custom.C7nCustomRedisTokenStore;

/**
 * @author scp
 * @date 2020/9/17
 * @description
 * 替换TokenStore 为c7n自定义的 C7nCustomRedisTokenStore
 */
@Configuration
public class C7nSecurityConfiguration {
    @Autowired
    private SecurityProperties securityProperties;
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    @Autowired
    private LoginUtil loginUtil;
    @Autowired
    private SessionRepository<?> sessionRepository;

    @Bean
    @Primary
    public TokenStore tokenStore() {
        C7nCustomRedisTokenStore redisTokenStore = new C7nCustomRedisTokenStore(redisConnectionFactory, loginUtil, sessionRepository,
                securityProperties.isAccessTokenAutoRenewal());
        redisTokenStore.setAuthenticationKeyGenerator(authenticationKeyGenerator());
        redisTokenStore.setPrefix(Constants.CacheKey.ACCESS_TOKEN);
        return redisTokenStore;
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationKeyGenerator.class)
    public AuthenticationKeyGenerator authenticationKeyGenerator () {
        return new CustomAuthenticationKeyGenerator(loginUtil);
    }
}
