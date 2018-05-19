package io.choerodon.oauth.app.service.impl;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.oauth.api.dto.UserDTO;
import io.choerodon.oauth.app.service.OrganizationUserService;
import io.choerodon.oauth.domain.iam.entity.OrganizationE;
import io.choerodon.oauth.domain.iam.entity.UserE;
import io.choerodon.oauth.domain.repository.OrganizationRepository;
import io.choerodon.oauth.domain.repository.UserRepository;
import io.choerodon.oauth.infra.dataobject.OrganizationDO;
import io.choerodon.oauth.infra.dataobject.UserDO;

/**
 * @author superlee
 */
@Component
public class OrganizationUserServiceImpl implements OrganizationUserService {
    private static final String ERROR_ORGANIZATION_NOT_EXIST = "error.organization.not.exist";
    private OrganizationRepository organizationRepository;

    private UserRepository userRepository;

    public OrganizationUserServiceImpl(OrganizationRepository organizationRepository, UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserDTO create(UserDTO userDTO, boolean checkPassword) {
        if (userDTO.getPassword() == null) {
            throw new CommonException("error.user.password.empty");
        }
        OrganizationDO organizationDO = organizationRepository.selectByPrimaryKey(userDTO.getOrganizationId());
        if (organizationDO == null) {
            throw new CommonException(ERROR_ORGANIZATION_NOT_EXIST);
        }
        OrganizationE organizationE = ConvertHelper.convert(organizationDO, OrganizationE.class);
        return ConvertHelper.convert(
                organizationE.addUser(
                        ConvertHelper.convert(userDTO, UserE.class)), userDTO.getClass());
    }

    @Override
    public Page<UserDTO> pageQuery(PageRequest pageRequest, UserDTO userDTO) {
        Page<UserDO> userDOPage =
                userRepository.pageQuery(pageRequest, ConvertHelper.convert(userDTO, UserDO.class), userDTO.getParam());
        return ConvertPageHelper.convertPage(userDOPage, UserDTO.class);
    }

    @Override
    public UserDTO update(UserDTO userDTO) {
        OrganizationDO organizationDO = organizationRepository.selectByPrimaryKey(userDTO.getOrganizationId());
        if (organizationDO == null) {
            throw new CommonException(ERROR_ORGANIZATION_NOT_EXIST);
        }
        OrganizationE organizationE = ConvertHelper.convert(organizationDO, OrganizationE.class);
        return ConvertHelper.convert(
                organizationE.updateUser(
                        ConvertHelper.convert(userDTO, UserE.class)), userDTO.getClass());
    }

    @Override
    public void delete(Long organizationId, Long id) {
        OrganizationDO organizationDO = organizationRepository.selectByPrimaryKey(organizationId);
        if (organizationDO == null) {
            throw new CommonException(ERROR_ORGANIZATION_NOT_EXIST);
        }
        OrganizationE organizationE = ConvertHelper.convert(organizationDO, OrganizationE.class);
        organizationE.removeUserById(id);
    }

    @Override
    public void deleteAllUser(Long organizationId) {
        OrganizationDO organizationDO = organizationRepository.selectByPrimaryKey(organizationId);
        if (organizationDO == null) {
            throw new CommonException(ERROR_ORGANIZATION_NOT_EXIST);
        }
        OrganizationE organizationE = ConvertHelper.convert(organizationDO, OrganizationE.class);
        organizationE.removeAllUser();
    }
}
