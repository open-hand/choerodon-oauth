package io.choerodon.oauth.api.service;


import javax.servlet.http.HttpServletRequest;

import io.choerodon.oauth.domain.entity.UserE;

/**
 * @author dongfan117@gmail.com
 */
public interface UserService {
    UserE queryByLoginField(String field);

    UserE updateSelective(UserE userE);

    UserE checkUserByEmail(HttpServletRequest request, String email);

    UserE queryByEmail(String email);
}
