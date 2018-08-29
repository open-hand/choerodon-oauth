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

import io.choerodon.oauth.api.dto.PasswordForgetDTO;
import io.choerodon.oauth.api.service.PasswordForgetService;
import io.choerodon.oauth.api.service.UserService;
import io.choerodon.oauth.infra.enums.PasswordFindException;

/**
 * @author wuguokai
 */
@Controller
@RequestMapping("/password")
public class PasswordController {

    private static final String DEFAULT_PAGE = "password-find";
    @Autowired
    private PasswordForgetService passwordForgetService;

    @Autowired
    private UserService userService;
    @Autowired
    private MessageSource messageSource;

    /**
     * 进入找回密码页面
     *
     * @return path
     */
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public String find(HttpServletRequest request) {
        request.getSession().removeAttribute("userId");
        request.getSession().removeAttribute("userName");
        return DEFAULT_PAGE;
    }


    @PostMapping(value = "/send")
    @ResponseBody
    public ResponseEntity<PasswordForgetDTO> send(HttpServletRequest request) {
        String emailAddress = request.getParameter("emailAddress");
        PasswordForgetDTO passwordForgetDTO = passwordForgetService.checkUserByEmail(emailAddress);
        if (!passwordForgetDTO.getSuccess()) {
            return new ResponseEntity<>(passwordForgetDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>(passwordForgetService.send(passwordForgetDTO), HttpStatus.OK);
    }

    @PostMapping(value = "/check")
    @ResponseBody
    public ResponseEntity<PasswordForgetDTO> check(HttpServletRequest request) {
        String emailAddress = request.getParameter("emailAddress");
        String captcha = request.getParameter("captcha");

        PasswordForgetDTO passwordForgetDTO = passwordForgetService.checkUserByEmail(emailAddress);
        if (!passwordForgetDTO.getSuccess()) {
            return new ResponseEntity<>(passwordForgetDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>(passwordForgetService.check(passwordForgetDTO, captcha), HttpStatus.OK);
    }

    @PostMapping(value = "/reset")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<PasswordForgetDTO> reset(HttpServletRequest request) {

        String emailAddress = request.getParameter("emailAddress");
        String captcha = request.getParameter("captcha");
        Long userId = Long.valueOf(request.getParameter("userId"));

        String pwd = request.getParameter("password");
        String pwd1 = request.getParameter("password1");
        PasswordForgetDTO passwordForgetDTO;
        if (!pwd.equals(pwd1)) {
            passwordForgetDTO = new PasswordForgetDTO(false);

            passwordForgetDTO.setCode(PasswordFindException.PASSWORD_NOT_EQUAL.value());
            passwordForgetDTO.setMsg(messageSource.getMessage(PasswordFindException.PASSWORD_NOT_EQUAL.value(), null, Locale.ROOT));
            return new ResponseEntity<>(passwordForgetDTO, HttpStatus.OK);
        }

        passwordForgetDTO = passwordForgetService.checkUserByEmail(emailAddress);
        if (!passwordForgetDTO.getSuccess()) {
            return new ResponseEntity<>(passwordForgetDTO, HttpStatus.OK);
        }
        if (!userId.equals(passwordForgetDTO.getUser().getId())) {
            passwordForgetDTO = new PasswordForgetDTO(false);
            passwordForgetDTO.setCode(PasswordFindException.USER_IS_ILLEGAL.value());
            passwordForgetDTO.setMsg(messageSource.getMessage(PasswordFindException.USER_IS_ILLEGAL.value(), null, Locale.ROOT));
            return new ResponseEntity<>(passwordForgetDTO, HttpStatus.OK);
        }
        passwordForgetDTO = passwordForgetService.check(passwordForgetDTO, captcha);
        if (!passwordForgetDTO.getSuccess()) {
            return new ResponseEntity<>(passwordForgetDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>(passwordForgetService.reset(passwordForgetDTO, captcha, pwd), HttpStatus.OK);
    }
}
