package io.choerodon.oauth.domain.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.oauth.domain.service.IUserService;
import io.choerodon.oauth.infra.dataobject.UserDO;

/**
 * @author wuguokai
 */
@RefreshScope
@Service
public class IUserServiceImpl extends BaseServiceImpl<UserDO> implements IUserService {

    @Value("${choerodon.oauth.login.field:#{null}}")
    private String[] queryField;

    /**
     * 通过用户名、邮箱、手机号进行进行用户搜索
     *
     * @param field 用户名、邮箱或手机三者之一
     * @return 对应用户
     */
    @Override
    public UserDO findUser(String field) {
        Set<String> fieldSet = null;
        if (queryField != null) {
            fieldSet = new HashSet(Arrays.asList(queryField));
        }
        if (field == null) {
            return null;
        }
        UserDO userDO = new UserDO();
        userDO.setLoginName(field);
        List<UserDO> users = select(userDO);
        if (!users.isEmpty()) {
            return users.get(0);
        } else if (fieldSet != null
                && fieldSet.contains("mail")
                && Pattern.compile(UserDO.EMAIL_REGULAR_EXPRESSION).matcher(field).matches()) {
            userDO = new UserDO();
            userDO.setEmail(field);
            users = select(userDO);
            if (!users.isEmpty()) {
                return users.get(0);
            }
            return null;
        } else if (fieldSet != null && fieldSet.contains("phone")) {
            userDO = new UserDO();
            userDO.setPhone(field);
            users = select(userDO);
            if (!users.isEmpty()) {
                return users.get(0);
            }
        }
        return null;
    }

    @Override
    public UserDO findByLoginName(String loginName) {
        UserDO userDO = new UserDO();
        userDO.setLoginName(loginName);
        return selectOne(userDO);
    }
}
