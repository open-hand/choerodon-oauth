package io.choerodon.oauth.api.service;


import io.choerodon.oauth.domain.entity.UserE;

/**
 * @author dongfan117@gmail.com
 */
public interface UserService {
    UserE queryByLoginField(String field);

    UserE queryByPrimaryKey(Long id);

    UserE updateSelective(UserE userE);
}
