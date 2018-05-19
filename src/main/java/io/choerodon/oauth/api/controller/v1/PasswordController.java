package io.choerodon.oauth.api.controller.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

import io.choerodon.oauth.api.dto.RegisterFormDTO;
import io.choerodon.oauth.app.service.PasswordForgetService;
import io.choerodon.oauth.infra.dataobject.NotifyToken;
import io.choerodon.oauth.infra.feign.NotificationFeign;

/**
 * @author wuguokai
 */
@Controller
public class PasswordController {

    private PasswordForgetService passwordForgetService;
    private NotificationFeign notificationFeign;
    @Autowired
    private MessageSource messageSource;

    public PasswordController(PasswordForgetService passwordForgetService, NotificationFeign notificationFeign) {
        this.passwordForgetService = passwordForgetService;
        this.notificationFeign = notificationFeign;
    }

    /**
     * 进入找回密码页面
     *
     * @return path
     */
    @RequestMapping(value = "/forgetPassword", method = RequestMethod.GET)
    public String find() {
        return "password-find";
    }

    /**
     * 验证邮箱和页面验证码
     *
     * @param registerForm registerForm
     * @param session      session
     * @return map
     */
    @RequestMapping(value = "/password/email", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> checkEmailAndCode(@ModelAttribute RegisterFormDTO registerForm, HttpSession session) {
        String captchaCode = ((String) session.getAttribute("captchaCode")).toLowerCase();
        String captcha = registerForm.getCaptcha().toLowerCase();
        String emailAddress = registerForm.getEmailAddress();
        return passwordForgetService.checkMailCode(session, emailAddress, captchaCode, captcha);
    }

    /**
     * 发送验证码
     *
     * @param request request
     * @return 是否成功
     */
    @RequestMapping(value = "/password/code", method = RequestMethod.POST)
    public ResponseEntity<Boolean> sendNotifyToken(HttpServletRequest request) {
        String emailAddress = request.getParameter("emailAddress");
        if (emailAddress != null) {
            request.getSession().setAttribute("emailAddress", emailAddress);
        }
        return passwordForgetService.sendNotifyToken(request, emailAddress);
    }

    /**
     * 验证邮箱验证码
     *
     * @param verificationCode verificationCode
     * @param request          request
     * @return 验证信息
     */
    @RequestMapping(value = "/password/{verificationCode}", method = RequestMethod.GET)
    public ResponseEntity<String> verificationCode(@PathVariable String
                                                           verificationCode, HttpServletRequest request) {
        HttpSession session = request.getSession();
        Long notifyTokenId = (Long) session.getAttribute("notifyTokenId");
        if (notifyTokenId == null) {
            String res = "验证码错误";
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
        NotifyToken notifyToken = notificationFeign.findNotifyToken(notifyTokenId).getBody();
        if (notifyToken != null && verificationCode.equalsIgnoreCase(notifyToken.getToken())) {
            String res = "验证码正确";
            return new ResponseEntity<>(res, HttpStatus.OK);
        } else {
            String res = "验证码错误";
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 重置密码
     *
     * @param request request
     * @return 验证信息
     */
    @RequestMapping(value = "/password/reset", method = RequestMethod.POST)
     @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> resetPassword(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("userId");
        String password = request.getParameter("password");
        Long notifyTokenId = (Long) session.getAttribute("notifyTokenId");
        NotifyToken notifyToken = notificationFeign.findNotifyToken(notifyTokenId).getBody();
        String msg = "reset.failed";
        if (notifyToken == null) {
            msg = "notifyToken.wrong";
        } else {
            Boolean isReset = passwordForgetService.reset(userId, password);
            if (isReset) {
                notificationFeign.deleteNotifyToken(notifyTokenId);
                msg = "reset.success";
            }
        }
        return new ResponseEntity<>(msg, HttpStatus.OK);
    }
}
