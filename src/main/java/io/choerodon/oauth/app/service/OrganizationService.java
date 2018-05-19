package io.choerodon.oauth.app.service;

import java.util.List;

import io.choerodon.oauth.api.dto.OrganizationDTO;


/**
 * @author wuguokai
 */
public interface OrganizationService {

    OrganizationDTO getCurrentOrganization();

    OrganizationDTO updateOrganization(Long organizationId, OrganizationDTO organizationDTO);

    OrganizationDTO queryOrganizationById(Long organizationId);

    List<OrganizationDTO> queryAllOrganization();

    OrganizationDTO createOrganization(OrganizationDTO organization);
}
