package io.choerodon.oauth.app.service;


import org.hzero.core.user.UserType;
import org.hzero.starter.captcha.domain.core.pre.CaptchaPreResult;

import io.choerodon.oauth.api.vo.BindReMsgVO;
import io.choerodon.oauth.infra.dto.UserE;

/**
 * @author dongfan117@gmail.com
 */
public interface UserService {
    UserE queryByLoginField(String field);

    UserE updateSelective(UserE userE);

    UserE queryByEmail(String email);

    BindReMsgVO bindUserPhone(String phone, String captcha, String bindUserPhone, String loginName);

    BindReMsgVO updateUserPhone(String phone, String verifyKey, String loginName, String type);

    BindReMsgVO verifyCaptcha(String phone, String captcha, String captchaKey);

    BindReMsgVO verifyPassword(String loginName, String password);

    CaptchaPreResult<?> newSendPhoneCaptcha(String internationalTelCode, String phone, UserType ofDefault, String businessScope, boolean b);
}
