package io.choerodon.oauth.domain.iam.converter;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.oauth.api.dto.UserDTO;
import io.choerodon.oauth.domain.iam.entity.UserE;
import io.choerodon.oauth.infra.dataobject.UserDO;

/**
 * @author superlee
 */
@Component
public class UserConverter implements ConvertorI<UserE, UserDO, UserDTO> {

    @Override
    public UserE dtoToEntity(UserDTO dto) {
        return new UserE(dto.getId(), dto.getLoginName(), dto.getEmail(), dto.getOrganizationId(), dto.getPassword(),
                dto.getRealName(), dto.getPhone(), null, null, dto.getLanguage(), null,
                null, null, dto.getEnabled(), dto.getLocked(), dto.getLdap(), null,
                null, dto.getObjectVersionNumber());
    }

    @Override
    public UserDTO entityToDto(UserE entity) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(entity.getId());
        userDTO.setOrganizationId(entity.getOrganizationId());
        userDTO.setEmail(entity.getEmail());
        userDTO.setLanguage(entity.getLanguage());
        userDTO.setEnabled(entity.getEnabled());
        userDTO.setLocked(entity.getLocked());
        userDTO.setPhone(entity.getPhone());
        userDTO.setRealName(entity.getRealName());
        userDTO.setLoginName(entity.getLoginName());
        userDTO.setObjectVersionNumber(entity.getObjectVersionNumber());
        userDTO.setLdap(entity.getLdap());
        return userDTO;
    }

    @Override
    public UserE doToEntity(UserDO dataObject) {
        return new UserE(dataObject.getId(), dataObject.getLoginName(), dataObject.getEmail(),
                dataObject.getOrganizationId(), dataObject.getPassword(), dataObject.getRealName(),
                dataObject.getPhone(), dataObject.getImageUrl(), dataObject.getProfilePhoto(),
                dataObject.getLanguage(), dataObject.getTimeZone(), dataObject.getLastPasswordUpdatedAt(),
                dataObject.getLastLoginAt(), dataObject.getEnabled(), dataObject.getLocked(),
                dataObject.getLdap(), dataObject.getLockedUntilAt(),
                dataObject.getPasswordAttempt(), dataObject.getObjectVersionNumber());
    }

    @Override
    public UserDO entityToDo(UserE entity) {
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(entity, userDO);
        return userDO;
    }

    @Override
    public UserDTO doToDto(UserDO dataObject) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(dataObject.getId());
        userDTO.setEmail(dataObject.getEmail());
        userDTO.setOrganizationId(dataObject.getOrganizationId());
        userDTO.setLanguage(dataObject.getLanguage());
        userDTO.setEnabled(dataObject.getEnabled());
        userDTO.setLocked(dataObject.getLocked());
        userDTO.setPhone(dataObject.getPhone());
        userDTO.setRealName(dataObject.getRealName());
        userDTO.setLoginName(dataObject.getLoginName());
        userDTO.setObjectVersionNumber(dataObject.getObjectVersionNumber());
        userDTO.setLdap(dataObject.getLdap());
        return userDTO;
    }

    @Override
    public UserDO dtoToDo(UserDTO dto) {
        UserDO userDO = new UserDO();
        userDO.setId(dto.getId());
        userDO.setOrganizationId(dto.getOrganizationId());
        userDO.setEmail(dto.getEmail());
        userDO.setLanguage(dto.getLanguage());
        userDO.setEnabled(dto.getEnabled());
        userDO.setLocked(dto.getLocked());
        userDO.setPhone(dto.getPhone());
        userDO.setRealName(dto.getRealName());
        userDO.setLoginName(dto.getLoginName());
        userDO.setObjectVersionNumber(dto.getObjectVersionNumber());
        userDO.setLdap(dto.getLdap());
        return userDO;
    }

}
