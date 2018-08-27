package io.choerodon.oauth.api.service;

import io.choerodon.oauth.domain.entity.UserE;

/**
 * @author wuguokai
 */
public interface PasswordForgetService {

    Boolean send(String email, String loginName);

    Boolean check(String email, String captcha);

    Boolean reset(UserE user, String captcha, String password);
}
