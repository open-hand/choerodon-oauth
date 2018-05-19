package io.choerodon.oauth.app.service.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.oauth.api.dto.OrganizationDTO;
import io.choerodon.oauth.app.service.OrganizationService;
import io.choerodon.oauth.domain.iam.entity.OrganizationE;
import io.choerodon.oauth.domain.repository.OrganizationRepository;
import io.choerodon.oauth.infra.dataobject.OrganizationDO;

/**
 * @author wuguokai
 */
@Component
public class OrganizationServiceImpl implements OrganizationService {

    private OrganizationRepository organizationRepository;

    public OrganizationServiceImpl(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    public OrganizationDTO getCurrentOrganization() {
        CustomUserDetails userDetails = DetailsHelper.getUserDetails();
        if (userDetails == null) {
            throw new CommonException("error.iam.not.login");
        }
        OrganizationDO organizationDO = organizationRepository.selectByPrimaryKey(userDetails.getOrganizationId());
        return ConvertHelper.convert(organizationDO, OrganizationDTO.class);
    }

    @Override
    public OrganizationDTO updateOrganization(Long organizationId, OrganizationDTO organizationDTO) {
        organizationDTO.setId(organizationId);
        OrganizationE organizationE = ConvertHelper.convert(organizationDTO, OrganizationE.class);
        organizationE = organizationRepository.update(organizationE);
        return ConvertHelper.convert(organizationE, OrganizationDTO.class);
    }

    @Override
    public OrganizationDTO queryOrganizationById(Long organizationId) {
        OrganizationDO organizationDO = organizationRepository.selectByPrimaryKey(organizationId);
        return ConvertHelper.convert(organizationDO, OrganizationDTO.class);
    }

    @Override
    public List<OrganizationDTO> queryAllOrganization() {
        List<OrganizationE> organizationEList = organizationRepository.queryAll();
        return ConvertHelper.convertList(organizationEList, OrganizationDTO.class);
    }

    @Override
    public OrganizationDTO createOrganization(OrganizationDTO organization) {
        OrganizationE organizationE = ConvertHelper.convert(organization, OrganizationE.class);
        organizationE = organizationRepository.create(organizationE);
        return ConvertHelper.convert(organizationE, OrganizationDTO.class);
    }
}
