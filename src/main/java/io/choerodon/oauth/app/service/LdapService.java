package io.choerodon.oauth.app.service;

import io.choerodon.oauth.api.dto.LdapDTO;
import io.choerodon.oauth.infra.dataobject.LdapDO;

/**
 * @author wuguokai
 */
public interface LdapService {
    LdapDO queryByLoginName(String loginName);

    LdapDTO queryByOrgId(Long orgId);

}
