package io.choerodon.oauth.app.service;


import io.choerodon.oauth.infra.dto.UserE;

/**
 * @author dongfan117@gmail.com
 */
public interface UserService {
    UserE queryByLoginField(String field);

    UserE updateSelective(UserE userE);

    UserE queryByEmail(String email);

    Boolean bindUserPhone(String phone, String captcha, String bindUserPhone);
}
