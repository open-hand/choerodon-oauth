package io.choerodon.oauth.api.service;


import io.choerodon.oauth.api.dto.PasswordPolicyDTO;

/**
 * @author Eugen
 */
public interface PasswordPolicyService {
    PasswordPolicyDTO queryByOrgId(Long orgId);
}
