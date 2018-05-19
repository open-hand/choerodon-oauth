package io.choerodon.oauth.app.service.impl;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.oauth.api.dto.LdapDTO;
import io.choerodon.oauth.app.service.LdapService;
import io.choerodon.oauth.domain.oauth.entity.LdapE;
import io.choerodon.oauth.domain.repository.LdapRepository;
import io.choerodon.oauth.domain.repository.OrganizationRepository;
import io.choerodon.oauth.domain.repository.UserRepository;
import io.choerodon.oauth.infra.dataobject.LdapDO;
import io.choerodon.oauth.infra.dataobject.UserDO;

/**
 * @author wuguokai
 */
@Component
public class LdapServiceImpl implements LdapService {
    private LdapRepository ldapRepository;
    private OrganizationRepository organizationRepository;
    private UserRepository userRepository;

    public LdapServiceImpl(LdapRepository ldapRepository, OrganizationRepository organizationRepository,
                           UserRepository userRepository) {
        this.ldapRepository = ldapRepository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public LdapDO queryByLoginName(String loginName) {
        UserDO userDO = ConvertHelper.convert(userRepository.selectByLoginName(loginName), UserDO.class);
        return ConvertHelper.convert(queryByOrgId(userDO.getOrganizationId()), LdapDO.class);
    }

    @Override
    public LdapDTO queryByOrgId(Long orgId) {
        if (organizationRepository.selectByPrimaryKey(orgId) == null) {
            throw new CommonException("error.organization.not.exist");
        }
        LdapE ldapE = ldapRepository.queryByOrgId(orgId);
        if (ldapE == null) {
            throw new CommonException("error.ldap.not.exist");
        }
        return ConvertHelper.convert(ldapE, LdapDTO.class);
    }
}
