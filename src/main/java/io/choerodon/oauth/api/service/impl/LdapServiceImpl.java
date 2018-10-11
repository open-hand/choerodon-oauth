package io.choerodon.oauth.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.choerodon.core.exception.CommonException;
import io.choerodon.oauth.api.service.LdapService;
import io.choerodon.oauth.domain.entity.LdapE;
import io.choerodon.oauth.infra.mapper.LdapMapper;

/**
 * @author wuguokai
 */
@Component
public class LdapServiceImpl implements LdapService {
    @Autowired
    private LdapMapper ldapMapper;

    public void setLdapMapper(LdapMapper ldapMapper) {
        this.ldapMapper = ldapMapper;
    }

    @Override
    public LdapE queryByOrgId(Long orgId) {
        LdapE ldapE = new LdapE();
        ldapE.setOrganizationId(orgId);
        ldapE = ldapMapper.selectOne(ldapE);
        if (ldapE == null) {
            throw new CommonException("error.ldap.not.exist");
        }
        return ldapE;
    }
}
