package io.choerodon.oauth.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.oauth.api.service.PasswordPolicyService;
import io.choerodon.oauth.infra.dataobject.PasswordPolicyDO;
import io.choerodon.oauth.infra.mapper.PasswordPolicyMapper;

/**
 * @author Eugen
 */
@Component
public class PasswordPolicyServiceImpl implements PasswordPolicyService {
    @Autowired
    private PasswordPolicyMapper passwordPolicyMapper;

    public void setPasswordPolicyMapper(PasswordPolicyMapper passwordPolicyMapper) {
        this.passwordPolicyMapper = passwordPolicyMapper;
    }

    /**
     * @param orgId
     * @return 目标组织的密码策略
     */
    @Override
    public PasswordPolicyDO queryByOrgId(Long orgId) {
        PasswordPolicyDO passwordPolicy = new PasswordPolicyDO();
        passwordPolicy.setOrganizationId(orgId);
        return passwordPolicyMapper.selectOne(passwordPolicy);
    }
}
