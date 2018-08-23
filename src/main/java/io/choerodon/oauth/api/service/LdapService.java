package io.choerodon.oauth.api.service;

import io.choerodon.oauth.domain.entity.LdapE;

/**
 * @author wuguokai
 */
public interface LdapService {
    LdapE queryByOrgId(Long orgId);
}
