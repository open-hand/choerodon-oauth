package io.choerodon.oauth.domain.oauth.converter;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertorI;
import io.choerodon.oauth.api.dto.LdapDTO;
import io.choerodon.oauth.domain.oauth.entity.LdapE;
import io.choerodon.oauth.infra.dataobject.LdapDO;

/**
 * @author wuguokai
 */
@Component
public class LdapConverter implements ConvertorI<LdapE, LdapDO, LdapDTO> {
    @Override
    public LdapE dtoToEntity(LdapDTO dto) {
        LdapE ldapE = new LdapE();
        BeanUtils.copyProperties(dto, ldapE);
        return ldapE;
    }

    @Override
    public LdapDTO entityToDto(LdapE entity) {
        LdapDTO ldapDTO = new LdapDTO();
        BeanUtils.copyProperties(entity, ldapDTO);
        return ldapDTO;
    }

    @Override
    public LdapE doToEntity(LdapDO dataObject) {
        LdapE ldapE = new LdapE();
        BeanUtils.copyProperties(dataObject, ldapE);
        return ldapE;
    }

    @Override
    public LdapDO entityToDo(LdapE entity) {
        LdapDO ldapDO = new LdapDO();
        BeanUtils.copyProperties(entity, ldapDO);
        return ldapDO;
    }

    @Override
    public LdapDTO doToDto(LdapDO dataObject) {
        LdapDTO ldapDTO = new LdapDTO();
        BeanUtils.copyProperties(dataObject, ldapDTO);
        return ldapDTO;
    }

    @Override
    public LdapDO dtoToDo(LdapDTO dto) {
        LdapDO ldapDO = new LdapDO();
        BeanUtils.copyProperties(dto, ldapDO);
        return ldapDO;
    }
}
