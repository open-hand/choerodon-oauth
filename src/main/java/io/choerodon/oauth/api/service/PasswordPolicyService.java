package io.choerodon.oauth.api.service;


import io.choerodon.oauth.infra.dataobject.PasswordPolicyDO;

/**
 * @author Eugen
 */
public interface PasswordPolicyService {
    PasswordPolicyDO queryByOrgId(Long orgId);
}
