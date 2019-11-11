package io.choerodon.oauth.api.service;

import io.choerodon.core.oauth.CustomUserDetails;

import java.security.Principal;

/**
 * @author Eugen
 */
public interface PrincipalService {
    Principal setClientDetailUserDetails(Principal principal);

    void addRouteRuleCode(CustomUserDetails customUserDetails);
}
