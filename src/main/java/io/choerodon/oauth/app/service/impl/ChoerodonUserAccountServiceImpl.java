package io.choerodon.oauth.app.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.hzero.boot.oauth.domain.entity.BasePasswordPolicy;
import org.hzero.boot.oauth.domain.repository.BaseClientRepository;
import org.hzero.boot.oauth.domain.repository.BasePasswordPolicyRepository;
import org.hzero.boot.oauth.domain.service.BaseUserService;
import org.hzero.boot.oauth.policy.PasswordPolicyManager;
import org.hzero.oauth.domain.entity.User;
import org.hzero.oauth.domain.repository.UserRepository;
import org.hzero.oauth.infra.encrypt.EncryptClient;
import org.hzero.oauth.security.config.SecurityProperties;
import org.hzero.oauth.security.service.impl.DefaultUserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.choerodon.oauth.api.vo.SysSettingVO;
import io.choerodon.oauth.app.service.SystemSettingService;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/8/17 15:22
 */
@Service
public class ChoerodonUserAccountServiceImpl extends DefaultUserAccountService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SystemSettingService systemSettingService;
    @Autowired
    private EncryptClient encryptClient;

    public ChoerodonUserAccountServiceImpl(UserRepository userRepository, BaseUserService baseUserService, PasswordPolicyManager passwordPolicyManager, BasePasswordPolicyRepository basePasswordPolicyRepository, BaseClientRepository baseClientRepository, SecurityProperties securityProperties) {
        super(userRepository, baseUserService, passwordPolicyManager, basePasswordPolicyRepository, baseClientRepository, securityProperties);
    }

    @Override
    public boolean isNeedForceModifyPassword(BasePasswordPolicy passwordPolicy, User user) {
        // 1.校验平台层密码策略
        SysSettingVO setting = systemSettingService.getSetting();
        if (Boolean.TRUE.equals(setting.getEnableUpdateDefaultPwd())) {
            // 1.校验组织是否开启密码策略和强制修改默认密码
            // 为 0 的时候校验密码策略是否开启了强制修改初始密码的配置
            if (Boolean.TRUE.equals(passwordPolicy.getEnablePassword())
                    && Boolean.TRUE.equals(passwordPolicy.getForceModifyPassword())
                    && StringUtils.isNotBlank(passwordPolicy.getOriginalPassword())) {
                return this.passwordEncoder.matches(setting.getDefaultPassword(), user.getPassword()) && this.passwordEncoder.matches(passwordPolicy.getOriginalPassword(), user.getPassword());
            } else {
                return this.passwordEncoder.matches(setting.getDefaultPassword(), user.getPassword());
            }
        } else {
            // 1.校验组织是否开启密码策略和强制修改默认密码
            // 为 0 的时候校验密码策略是否开启了强制修改初始密码的配置
            if (Boolean.TRUE.equals(passwordPolicy.getEnablePassword())
                    && Boolean.TRUE.equals(passwordPolicy.getForceModifyPassword())
                    && StringUtils.isNotBlank(passwordPolicy.getOriginalPassword())) {
                return this.passwordEncoder.matches(passwordPolicy.getOriginalPassword(), user.getPassword());
            }
        }
        return false;
    }
}
