package io.choerodon.oauth.api.controller.v1;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import io.choerodon.oauth.api.dto.PasswordForgetDTO;
import io.choerodon.oauth.api.service.PasswordForgetService;
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
    private MessageSource messageSource;

    public void setPasswordForgetService(PasswordForgetService passwordForgetService) {
        this.passwordForgetService = passwordForgetService;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * 进入找回密码页面
     *
     * @return path
     */
    @GetMapping(value = "/find")
    public String find(HttpServletRequest request) {
        request.getSession().removeAttribute("userId");
        request.getSession().removeAttribute("userName");
        return DEFAULT_PAGE;
    }


    @PostMapping(value = "/send")
    @ResponseBody
    public ResponseEntity<PasswordForgetDTO> send(@RequestParam("emailAddress") String emailAddress) {
        PasswordForgetDTO passwordForgetDTO = passwordForgetService.checkUserByEmail(emailAddress);
        if (!passwordForgetDTO.getSuccess()) {
            return new ResponseEntity<>(passwordForgetDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>(passwordForgetService.send(passwordForgetDTO), HttpStatus.OK);
    }

    @PostMapping(value = "/check")
    @ResponseBody
    public ResponseEntity<PasswordForgetDTO> check(
            @RequestParam("emailAddress") String emailAddress,
            @RequestParam("captcha") String captcha) {
        PasswordForgetDTO passwordForgetDTO = passwordForgetService.checkUserByEmail(emailAddress);
        if (!passwordForgetDTO.getSuccess()) {
            return new ResponseEntity<>(passwordForgetDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>(passwordForgetService.check(passwordForgetDTO, captcha), HttpStatus.OK);
    }

    @PostMapping(value = "/reset")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<PasswordForgetDTO> reset(
            @RequestParam("emailAddress") String emailAddress,
            @RequestParam("captcha") String captcha,
            @RequestParam("userId") Long userId,
            @RequestParam("password") String pwd,
            @RequestParam("password1") String pwd1) {
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

    @PostMapping(value = "/check_disable")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<PasswordForgetDTO> checkDisable(@RequestParam("emailAddress") String emailAddress) {
        return new ResponseEntity<>(passwordForgetService.checkDisable(emailAddress), HttpStatus.OK);
    }
}
