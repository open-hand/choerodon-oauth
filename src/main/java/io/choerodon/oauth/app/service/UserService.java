package io.choerodon.oauth.app.service;


import io.choerodon.oauth.api.vo.BindReMsgVO;
import io.choerodon.oauth.infra.dto.UserE;

/**
 * @author dongfan117@gmail.com
 */
public interface UserService {
    UserE queryByLoginField(String field);

    UserE updateSelective(UserE userE);

    UserE queryByEmail(String email);

    BindReMsgVO bindUserPhone(String phone, String captcha, String bindUserPhone);

    BindReMsgVO updateUserPhone(String phone, String captcha, String captchaKey, String password, String type);

}
