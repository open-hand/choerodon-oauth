package io.choerodon.oauth.app.service.impl;

import io.choerodon.oauth.app.service.PasswordPolicyService;
import io.choerodon.oauth.infra.dto.PasswordPolicyDTO;
import io.choerodon.oauth.infra.mapper.PasswordPolicyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Eugen
 */
@Component
public class PasswordPolicyServiceImpl implements PasswordPolicyService {
    @Autowired
    private PasswordPolicyMapper passwordPolicyMapper;

    /**
     * @param orgId
     * @return 目标组织的密码策略
     */
    @Override
    public PasswordPolicyDTO queryByOrgId(Long orgId) {
        PasswordPolicyDTO passwordPolicy = new PasswordPolicyDTO();
        passwordPolicy.setOrganizationId(orgId);
        return passwordPolicyMapper.selectOne(passwordPolicy);
    }
}
