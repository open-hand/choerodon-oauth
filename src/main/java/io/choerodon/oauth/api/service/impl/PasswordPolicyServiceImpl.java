package io.choerodon.oauth.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.oauth.api.dto.PasswordPolicyDTO;
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

    /**
     *
     * @param orgId
     * @return 目标组织的密码策略
     */
    @Override
    public PasswordPolicyDTO queryByOrgId(Long orgId) {
        PasswordPolicyDO passwordPolicy = new PasswordPolicyDO();
        passwordPolicy.setOrganizationId(orgId);
        PasswordPolicyDO passwordPolicyDO = passwordPolicyMapper.selectOne(passwordPolicy);
        return ConvertHelper.convert(passwordPolicyDO, PasswordPolicyDTO.class);
    }
}
