package io.choerodon.oauth.app.service;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;

/**
 * @author wuguokai
 */
public interface PasswordForgetService {
    ResponseEntity<Map<String, String>> checkMailCode(HttpSession session, String emailAddress, String captchaCode, String captcha);

    ResponseEntity<Boolean> sendNotifyToken(HttpServletRequest request, String emailAddress);

    Boolean reset(Long userId, String password);
}
