package io.choerodon.oauth.domain.iam.converter;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.oauth.api.dto.OrganizationDTO;
import io.choerodon.oauth.domain.iam.entity.OrganizationE;
import io.choerodon.oauth.domain.iam.factory.OrganizationEFactory;
import io.choerodon.oauth.infra.dataobject.OrganizationDO;

/**
 * @author wuguokai
 */
@Component
public class OrganizationConverter implements ConvertorI<OrganizationE, OrganizationDO, OrganizationDTO> {
    @Override
    public OrganizationE dtoToEntity(OrganizationDTO dto) {
        return OrganizationEFactory.createOrganizationE(
                dto.getId(), dto.getName(), dto.getObjectVersionNumber());
    }

    @Override
    public OrganizationDTO entityToDto(OrganizationE entity) {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        organizationDTO.setName(entity.getName());
        organizationDTO.setId(entity.getId());
        organizationDTO.setObjectVersionNumber(entity.getObjectVersionNumber());
        return organizationDTO;
    }

    @Override
    public OrganizationE doToEntity(OrganizationDO dataObject) {
        return OrganizationEFactory.createOrganizationE(
                dataObject.getId(), dataObject.getName(), dataObject.getObjectVersionNumber());
    }

    @Override
    public OrganizationDO entityToDo(OrganizationE entity) {
        OrganizationDO organizationDO = new OrganizationDO();
        organizationDO.setId(entity.getId());
        organizationDO.setName(entity.getName());
        organizationDO.setObjectVersionNumber(entity.getObjectVersionNumber());
        return organizationDO;
    }

    @Override
    public OrganizationDTO doToDto(OrganizationDO dataObject) {
        OrganizationDTO organizationDTO = new OrganizationDTO();
        organizationDTO.setId(dataObject.getId());
        organizationDTO.setName(dataObject.getName());
        organizationDTO.setObjectVersionNumber(dataObject.getObjectVersionNumber());
        return organizationDTO;
    }

    @Override
    public OrganizationDO dtoToDo(OrganizationDTO dto) {
        OrganizationDO organizationDO = new OrganizationDO();
        organizationDO.setId(dto.getId());
        organizationDO.setName(dto.getName());
        organizationDO.setObjectVersionNumber(dto.getObjectVersionNumber());
        return organizationDO;
    }
}
