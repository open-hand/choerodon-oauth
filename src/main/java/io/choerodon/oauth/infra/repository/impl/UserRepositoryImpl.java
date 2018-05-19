package io.choerodon.oauth.infra.repository.impl;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.domain.Page;
import io.choerodon.core.exception.CommonException;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.oauth.domain.iam.entity.UserE;
import io.choerodon.oauth.domain.repository.UserRepository;
import io.choerodon.oauth.infra.dataobject.UserDO;
import io.choerodon.oauth.infra.mapper.UserMapper;

/**
 * @author superlee
 */
@Component
public class UserRepositoryImpl implements UserRepository {

    private UserMapper mapper;

    public UserRepositoryImpl(UserMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public UserE selectByLoginName(String loginName) {
        UserDO userDO = new UserDO();
        userDO.setLoginName(loginName);
        return ConvertHelper.convert(mapper.selectOne(userDO), UserE.class);
    }

    @Override
    public UserE insertSelective(UserE userE) {
        UserDO userDO = ConvertHelper.convert(userE, UserDO.class);
        if (mapper.insertSelective(userDO) != 1) {
            throw new CommonException("error.user.create");
        }
        return ConvertHelper.convert(mapper.selectByPrimaryKey(userDO.getId()), UserE.class);
    }

    @Override
    public Page<UserDO> pageQuery(PageRequest pageRequest, UserDO userDO, String param) {
        //TODO
        //language code 转描述
        return PageHelper.doPageAndSort(pageRequest, () -> mapper.fulltextSearch(userDO, param));
    }

    @Override
    public UserE selectByPrimaryKey(Long id) {
        return ConvertHelper.convert(mapper.selectByPrimaryKey(id), UserE.class);
    }

    @Override
    public UserE updateSelective(UserE userE) {
        UserDO userDO = ConvertHelper.convert(userE, UserDO.class);
        if (userDO.getObjectVersionNumber() == null) {
            throw new CommonException("error.user.objectVersionNumber.empty");
        }
        if (mapper.updateByPrimaryKeySelective(userDO) != 1) {
            throw new CommonException("error.user.update");
        }
        userDO = mapper.selectByPrimaryKey(userDO.getId());
        return ConvertHelper.convert(userDO, UserE.class);
    }

    @Override
    public void deleteById(Long id) {
        UserDO userDO = new UserDO();
        userDO.setId(id);
        if (mapper.deleteByPrimaryKey(userDO) != 1) {
            throw new CommonException("error.user.delete");
        }
    }

    @Override
    public void deleteByOrganizationId(Long organizationId) {
        UserDO userDO = new UserDO();
        userDO.setOrganizationId(organizationId);
        mapper.delete(userDO);
    }

    @Override
    public UserE findUserByEmailAddressEnable(String emailAddress) {
        UserDO userDO = new UserDO();
        userDO.setEmail(emailAddress);
        userDO.setEnabled(true);
        userDO = mapper.selectOne(userDO);
        return ConvertHelper.convert(userDO, UserE.class);
    }
}
