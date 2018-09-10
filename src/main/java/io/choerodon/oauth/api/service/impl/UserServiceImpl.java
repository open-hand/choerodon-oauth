package io.choerodon.oauth.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.oauth.api.service.UserService;
import io.choerodon.oauth.api.validator.UserValidator;
import io.choerodon.oauth.domain.entity.UserE;
import io.choerodon.oauth.infra.mapper.UserMapper;

/**
 * @author dongfan117@gmail.com
 */
@Service("userService")
public class UserServiceImpl implements UserService {

    @Value("${choerodon.oauth.login.field:mail,phone}")
    private String[] queryField;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private MessageSource messageSource;

    @Override
    public UserE queryByLoginField(String field) {
        if (field == null) {
            return null;
        }

        UserE userE = new UserE();
        userE.setLoginName(field);
        userE = userMapper.selectOne(userE);
        if (null != userE) {
            return userE;
        }
        return this.queryByEmailOrPhone(field);
    }

    @Override
    public UserE updateSelective(UserE userE) {
        if (userMapper.updateByPrimaryKeySelective(userE) != 1) {
            throw new CommonException("error.user.update");
        }
        return userMapper.selectByPrimaryKey(userE.getId());
    }

    private UserE queryByEmailOrPhone(String field) {
        UserE user = new UserE();
        if (userValidator.emailValidator(field)) {
            user.setEmail(field);
        } else if (userValidator.phoneValidator(field)) {
            user.setPhone(field);
        } else {
            return null;
        }
        return userMapper.selectOne(user);
    }

    @Override
    public UserE queryByEmail(String email) {
        UserE user = new UserE();
        user.setEmail(email);
        return userMapper.selectOne(user);
    }
}
