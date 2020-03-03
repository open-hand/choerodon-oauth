package io.choerodon.oauth.api.service;

import io.choerodon.oauth.api.dto.PasswordForgetDTO;

/**
 * @author wuguokai
 */
public interface PasswordForgetService {
    PasswordForgetDTO checkUserByEmail(String email);


    PasswordForgetDTO checkDisable(String email);

    PasswordForgetDTO sendResetEmail(String email);

    /**
     * 校验token是否有效
     * @param token
     * @return
     */
    boolean checkTokenAvailable(String token);

    /**
     * 重置密码
     * @param token
     * @param password
     * @return
     */
    PasswordForgetDTO resetPassword(String token, String password);
}
