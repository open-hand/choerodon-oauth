package io.choerodon.oauth.app.service;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.oauth.api.dto.UserDTO;

/**
 * @author superlee
 */
public interface OrganizationUserService {
    UserDTO create(UserDTO userDTO, boolean checkPassword);

    Page<UserDTO> pageQuery(PageRequest pageRequest, UserDTO userDTO);

    UserDTO update(UserDTO userDTO);

    void delete(Long organizationId, Long id);

    void deleteAllUser(Long organizationId);
}
