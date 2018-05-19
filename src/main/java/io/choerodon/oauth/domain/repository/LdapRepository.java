package io.choerodon.oauth.domain.repository;

import io.choerodon.oauth.domain.oauth.entity.LdapE;

/**
 * @author wuguokai
 */
public interface LdapRepository {
    LdapE queryByOrgId(Long orgId);
}
