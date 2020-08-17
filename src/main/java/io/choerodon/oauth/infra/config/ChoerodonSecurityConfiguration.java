package io.choerodon.oauth.infra.config;

import org.hzero.boot.oauth.domain.repository.BaseClientRepository;
import org.hzero.boot.oauth.domain.repository.BasePasswordPolicyRepository;
import org.hzero.boot.oauth.domain.service.BaseUserService;
import org.hzero.boot.oauth.policy.PasswordPolicyManager;
import org.hzero.oauth.domain.repository.UserRepository;
import org.hzero.oauth.security.config.SecurityProperties;
import org.hzero.oauth.security.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.choerodon.oauth.app.service.impl.ChoerodonUserAccountServiceImpl;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/8/17 15:25
 */
@Configuration
@EnableConfigurationProperties({SecurityProperties.class})
public class ChoerodonSecurityConfiguration {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BaseUserService baseUserService;
    @Autowired
    private PasswordPolicyManager passwordPolicyManager;
    @Autowired
    private BasePasswordPolicyRepository basePasswordPolicyRepository;
    @Autowired
    private BaseClientRepository baseClientRepository;
    @Autowired
    private SecurityProperties securityProperties;

    @Bean
    @Primary
    public UserAccountService userAccountService() {
        return new ChoerodonUserAccountServiceImpl(this.userRepository, this.baseUserService, this.passwordPolicyManager, this.basePasswordPolicyRepository, this.baseClientRepository, this.securityProperties);
    }
}
