package io.choerodon.oauth.api.service;

import java.security.Principal;

/**
 * @author Eugen
 */
public interface PrincipalService {
    Principal setClientDetailUserDetails(Principal principal);
}
