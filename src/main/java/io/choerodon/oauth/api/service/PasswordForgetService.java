package io.choerodon.oauth.api.service;

import io.choerodon.oauth.api.dto.PasswordForgetDTO;

/**
 * @author wuguokai
 */
public interface PasswordForgetService {
    PasswordForgetDTO checkUserByEmail(String email);

    PasswordForgetDTO send(PasswordForgetDTO passwordForgetDTO);

    PasswordForgetDTO check(PasswordForgetDTO passwordForgetDTO, String captcha);

    PasswordForgetDTO reset(PasswordForgetDTO passwordForgetDTO, String captcha, String password);
}
