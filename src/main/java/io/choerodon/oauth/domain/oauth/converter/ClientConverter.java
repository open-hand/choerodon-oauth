package io.choerodon.oauth.domain.oauth.converter;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.oauth.api.dto.ClientDTO;
import io.choerodon.oauth.domain.oauth.entity.ClientE;
import io.choerodon.oauth.infra.dataobject.ClientDO;

/**
 * @author wuguokai
 */
@Component
public class ClientConverter implements ConvertorI<ClientE, ClientDO, ClientDTO> {
    @Override
    public ClientE dtoToEntity(ClientDTO dto) {
        ClientE clientE = new ClientE();
        clientE.setId(dto.getId());
        clientE.setName(dto.getName());
        clientE.setAccessTokenValidity(dto.getAccessTokenValidity());
        clientE.setAdditionalInformation(dto.getAdditionalInformation());
        clientE.setAuthorizedGrantTypes(dto.getAuthorizedGrantTypes());
        clientE.setAutoApprove(dto.getAutoApprove());
        clientE.setOrganizationId(dto.getOrganizationId());
        clientE.setRefreshTokenValidity(dto.getRefreshTokenValidity());
        clientE.setResourceIds(dto.getResourceIds());
        clientE.setScope(dto.getScope());
        clientE.setSecret(dto.getSecret());
        clientE.setWebServerRedirectUri(dto.getWebServerRedirectUri());
        clientE.setObjectVersionNumber(dto.getObjectVersionNumber());
        return clientE;
    }

    @Override
    public ClientDTO entityToDto(ClientE entity) {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(entity.getId());
        clientDTO.setName(entity.getName());
        clientDTO.setAccessTokenValidity(entity.getAccessTokenValidity());
        clientDTO.setAdditionalInformation(entity.getAdditionalInformation());
        clientDTO.setAuthorizedGrantTypes(entity.getAuthorizedGrantTypes());
        clientDTO.setAutoApprove(entity.getAutoApprove());
        clientDTO.setOrganizationId(entity.getOrganizationId());
        clientDTO.setRefreshTokenValidity(entity.getRefreshTokenValidity());
        clientDTO.setResourceIds(entity.getResourceIds());
        clientDTO.setScope(entity.getScope());
        clientDTO.setSecret(entity.getSecret());
        clientDTO.setWebServerRedirectUri(entity.getWebServerRedirectUri());
        clientDTO.setObjectVersionNumber(entity.getObjectVersionNumber());
        return clientDTO;
    }

    @Override
    public ClientE doToEntity(ClientDO dataObject) {
        ClientE clientE = new ClientE();
        clientE.setId(dataObject.getId());
        clientE.setName(dataObject.getName());
        clientE.setAccessTokenValidity(dataObject.getAccessTokenValidity());
        clientE.setAdditionalInformation(dataObject.getAdditionalInformation());
        clientE.setAuthorizedGrantTypes(dataObject.getAuthorizedGrantTypes());
        clientE.setAutoApprove(dataObject.getAutoApprove());
        clientE.setOrganizationId(dataObject.getOrganizationId());
        clientE.setRefreshTokenValidity(dataObject.getRefreshTokenValidity());
        clientE.setResourceIds(dataObject.getResourceIds());
        clientE.setScope(dataObject.getScope());
        clientE.setSecret(dataObject.getSecret());
        clientE.setWebServerRedirectUri(dataObject.getWebServerRedirectUri());
        clientE.setObjectVersionNumber(dataObject.getObjectVersionNumber());
        return clientE;
    }

    @Override
    public ClientDO entityToDo(ClientE entity) {
        ClientDO clientDO = new ClientDO();
        clientDO.setId(entity.getId());
        clientDO.setName(entity.getName());
        clientDO.setAccessTokenValidity(entity.getAccessTokenValidity());
        clientDO.setAdditionalInformation(entity.getAdditionalInformation());
        clientDO.setAuthorizedGrantTypes(entity.getAuthorizedGrantTypes());
        clientDO.setAutoApprove(entity.getAutoApprove());
        clientDO.setOrganizationId(entity.getOrganizationId());
        clientDO.setRefreshTokenValidity(entity.getRefreshTokenValidity());
        clientDO.setResourceIds(entity.getResourceIds());
        clientDO.setScope(entity.getScope());
        clientDO.setSecret(entity.getSecret());
        clientDO.setWebServerRedirectUri(entity.getWebServerRedirectUri());
        clientDO.setObjectVersionNumber(entity.getObjectVersionNumber());
        return clientDO;
    }

    @Override
    public ClientDTO doToDto(ClientDO dataObject) {
        ClientDTO clientDTO = new ClientDTO();
        clientDTO.setId(dataObject.getId());
        clientDTO.setName(dataObject.getName());
        clientDTO.setAccessTokenValidity(dataObject.getAccessTokenValidity());
        clientDTO.setAdditionalInformation(dataObject.getAdditionalInformation());
        clientDTO.setAuthorizedGrantTypes(dataObject.getAuthorizedGrantTypes());
        clientDTO.setAutoApprove(dataObject.getAutoApprove());
        clientDTO.setOrganizationId(dataObject.getOrganizationId());
        clientDTO.setRefreshTokenValidity(dataObject.getRefreshTokenValidity());
        clientDTO.setResourceIds(dataObject.getResourceIds());
        clientDTO.setScope(dataObject.getScope());
        clientDTO.setSecret(dataObject.getSecret());
        clientDTO.setWebServerRedirectUri(dataObject.getWebServerRedirectUri());
        clientDTO.setObjectVersionNumber(dataObject.getObjectVersionNumber());
        return clientDTO;
    }

    @Override
    public ClientDO dtoToDo(ClientDTO dto) {
        ClientDO clientDO = new ClientDO();
        clientDO.setId(dto.getId());
        clientDO.setName(dto.getName());
        clientDO.setAccessTokenValidity(dto.getAccessTokenValidity());
        clientDO.setAdditionalInformation(dto.getAdditionalInformation());
        clientDO.setAuthorizedGrantTypes(dto.getAuthorizedGrantTypes());
        clientDO.setAutoApprove(dto.getAutoApprove());
        clientDO.setOrganizationId(dto.getOrganizationId());
        clientDO.setRefreshTokenValidity(dto.getRefreshTokenValidity());
        clientDO.setResourceIds(dto.getResourceIds());
        clientDO.setScope(dto.getScope());
        clientDO.setSecret(dto.getSecret());
        clientDO.setWebServerRedirectUri(dto.getWebServerRedirectUri());
        clientDO.setObjectVersionNumber(dto.getObjectVersionNumber());
        return clientDO;
    }
}
