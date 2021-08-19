package io.choerodon.oauth.security.config;

import org.hzero.boot.oauth.domain.repository.BaseClientRepository;
import org.hzero.boot.oauth.domain.repository.BasePasswordPolicyRepository;
import org.hzero.boot.oauth.domain.service.BaseUserService;
import org.hzero.boot.oauth.policy.PasswordPolicyManager;
import org.hzero.oauth.domain.repository.UserRepository;
import org.hzero.oauth.domain.service.UserLoginService;
import org.hzero.oauth.domain.service.impl.UserLoginServiceImpl;
import org.hzero.oauth.security.config.SecurityProperties;
import org.hzero.oauth.security.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import io.choerodon.oauth.app.service.impl.C7nUserAccountService;
import io.choerodon.oauth.app.service.impl.UserC7NLoginServiceImpl;
import io.choerodon.oauth.infra.mapper.MemberRoleMapper;
import io.choerodon.oauth.infra.mapper.TenantMapper;

/**
 * @author scp
 * @date 2020/9/17
 * @description 替换TokenStore 为c7n自定义的 C7nCustomRedisTokenStore
 */
@Configuration
public class C7nSecurityConfig {
    @Autowired
    private CookieConfiguration cookieConfiguration;


    @Autowired
    private MemberRoleMapper memberRoleMapper;
    @Autowired
    private TenantMapper tenantMapper;

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setSameSite(cookieConfiguration.getSameSite());
        cookieSerializer.setUseSecureCookie(cookieConfiguration.isSecure());
        return cookieSerializer;
    }

    @Bean
    public UserAccountService userAccountService(UserRepository userRepository,
                                                 BaseUserService baseUserService,
                                                 PasswordPolicyManager passwordPolicyManager,
                                                 BasePasswordPolicyRepository basePasswordPolicyRepository,
                                                 BaseClientRepository baseClientRepository,
                                                 SecurityProperties securityProperties) {
        return new C7nUserAccountService(userRepository, baseUserService, passwordPolicyManager, basePasswordPolicyRepository, baseClientRepository, securityProperties, memberRoleMapper, tenantMapper);
    }

    @Bean
    public UserLoginService userLoginService() {
        return new UserC7NLoginServiceImpl();
    }

}
