package io.choerodon.oauth.app.service;


import io.choerodon.oauth.infra.dto.PasswordPolicyDTO;

/**
 * @author Eugen
 */
public interface PasswordPolicyService {
    PasswordPolicyDTO queryByOrgId(Long orgId);
}
