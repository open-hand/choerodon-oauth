package io.choerodon.oauth.api.controller.v1;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import io.choerodon.oauth.api.service.PasswordForgetService;
import io.choerodon.oauth.api.service.UserService;
import io.choerodon.oauth.domain.entity.UserE;
import io.choerodon.oauth.infra.enums.PasswordFindException;

/**
 * @author wuguokai
 */
@Controller
@RequestMapping("/password")
public class PasswordController {

    private static String DEFAULT_PAGE = "password-find";
    @Autowired
    private PasswordForgetService passwordForgetService;

    @Autowired
    private UserService userService;
    @Autowired
    private MessageSource messageSource;

    public PasswordController() {
    }

    /**
     * 进入找回密码页面
     *
     * @return path
     */
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public String find(HttpServletRequest request) {
        request.getSession().removeAttribute("userId");
        return DEFAULT_PAGE;
    }


    @PostMapping(value = "/send")
    @ResponseBody
    public ResponseEntity<Boolean> send(HttpServletRequest request) {
        String emailAddress = request.getParameter("emailAddress");
        UserE user = userService.checkUserByEmail(request, emailAddress);
        if (null == user) {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
        return new ResponseEntity<>(passwordForgetService.send(emailAddress, user.getLoginName()), HttpStatus.OK);
    }

    @PostMapping(value = "/check")
    @ResponseBody
    public ResponseEntity<Boolean> check(HttpServletRequest request) {
        String emailAddress = request.getParameter("emailAddress");
        String captcha = request.getParameter("captcha");
        UserE user = userService.checkUserByEmail(request, emailAddress);
        if (null == user) {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
        request.getSession().setAttribute("userId", user.getId());
        return new ResponseEntity<>(passwordForgetService.check(emailAddress, captcha), HttpStatus.OK);
    }

    @PostMapping(value = "/reset")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Boolean> reset(HttpServletRequest request) {

        String emailAddress = request.getParameter("emailAddress");
        String captcha = request.getParameter("captcha");
        Long userId = (Long) request.getSession().getAttribute("userId");
        String pwd = request.getParameter("password");
        String pwd1 = request.getParameter("password1");

        if (!pwd.equals(pwd1)) {
            request.getSession().setAttribute("errorCode", PasswordFindException.PASSWORD_NOT_EQUAL.value());
            request.getSession().setAttribute("errorMsg", messageSource.getMessage(PasswordFindException.EMAIL_FORMAT_ILLEGAL.value(), null, Locale.ROOT));
            return new ResponseEntity<>(false, HttpStatus.OK);
        }

        UserE user = userService.checkUserByEmail(request, emailAddress);
        if (null == user) {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
        if (userId != user.getId()) {
            request.getSession().setAttribute("errorCode", PasswordFindException.USER_IS_ILLEGAL.value());
            request.getSession().setAttribute("errorMsg", messageSource.getMessage(PasswordFindException.USER_IS_ILLEGAL.value(), null, Locale.ROOT));
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
        if (!passwordForgetService.check(emailAddress, captcha)) {
            request.getSession().setAttribute("errorCode", PasswordFindException.CAPTCHA_ERROR.value());
            request.getSession().setAttribute("errorMsg", messageSource.getMessage(PasswordFindException.CAPTCHA_ERROR.value(), null, Locale.ROOT));
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
        return new ResponseEntity<>(passwordForgetService.reset(user, captcha, pwd), HttpStatus.OK);
    }
}
