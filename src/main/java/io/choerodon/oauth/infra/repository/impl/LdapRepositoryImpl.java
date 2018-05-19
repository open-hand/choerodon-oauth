package io.choerodon.oauth.infra.repository.impl;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.oauth.domain.oauth.entity.LdapE;
import io.choerodon.oauth.domain.repository.LdapRepository;
import io.choerodon.oauth.infra.dataobject.LdapDO;
import io.choerodon.oauth.infra.mapper.LdapMapper;

/**
 * @author wuguokai
 */
@Component
public class LdapRepositoryImpl implements LdapRepository {
    private LdapMapper ldapMapper;

    public LdapRepositoryImpl(LdapMapper ldapMapper) {
        this.ldapMapper = ldapMapper;
    }

    @Override
    public LdapE queryByOrgId(Long orgId) {
        LdapDO ldapDO = new LdapDO();
        ldapDO.setOrganizationId(orgId);
        ldapDO = ldapMapper.selectOne(ldapDO);
        return ConvertHelper.convert(ldapDO, LdapE.class);
    }
}
