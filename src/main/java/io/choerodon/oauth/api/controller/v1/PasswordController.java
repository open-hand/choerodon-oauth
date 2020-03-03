package io.choerodon.oauth.api.controller.v1;

import io.choerodon.oauth.api.dto.CaptchaCheckDTO;
import io.choerodon.oauth.api.dto.PasswordForgetDTO;
import io.choerodon.oauth.api.service.PasswordForgetService;
import io.choerodon.oauth.api.service.PasswordPolicyService;
import io.choerodon.oauth.api.service.SystemSettingService;
import io.choerodon.oauth.api.service.UserService;
import io.choerodon.oauth.api.vo.SysSettingVO;
import io.choerodon.oauth.infra.dataobject.PasswordPolicyDO;
import io.choerodon.oauth.infra.enums.PageUrlEnum;
import io.choerodon.oauth.infra.enums.PasswordFindException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

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
    private PasswordPolicyService passwordPolicyService;
    @Autowired
    private UserService userService;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private SystemSettingService systemSettingService;


    public void setPasswordForgetService(PasswordForgetService passwordForgetService) {
        this.passwordForgetService = passwordForgetService;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setPasswordPolicyService(PasswordPolicyService passwordPolicyService) {
        this.passwordPolicyService = passwordPolicyService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * 进入找回密码页面
     *
     * @return path
     */
    @GetMapping(value = "/find")
    public String find(HttpServletRequest request, Model model) {
        request.getSession().removeAttribute("userId");
        request.getSession().removeAttribute("userName");
        SysSettingVO sysSettingVO = systemSettingService.getSetting();
        if (sysSettingVO == null) {
            sysSettingVO = new SysSettingVO();
        }
        model.addAttribute("systemName", sysSettingVO.getSystemName());
        if (!StringUtils.isEmpty(sysSettingVO.getSystemLogo())) {
            model.addAttribute("systemLogo", sysSettingVO.getSystemLogo());
        }
        model.addAttribute("systemTitle", sysSettingVO.getSystemTitle());
        if (!StringUtils.isEmpty(sysSettingVO.getFavicon())) {
            model.addAttribute("favicon", sysSettingVO.getFavicon());
        }
        return DEFAULT_PAGE;
    }

    /**
     * 发送重置密码邮件
     * @param emailAddress
     * @return
     */
    @PostMapping(value = "/send_reset_email")
    @ResponseBody
    public ResponseEntity<PasswordForgetDTO> sendResetEmail(@RequestParam("emailAddress") String emailAddress) {
        return new ResponseEntity<>(passwordForgetService.sendResetEmail(emailAddress), HttpStatus.OK);
    }

    /**
     * 进入重置密码页面
     *
     * @return path
     */
    @GetMapping(value = "/reset_page/{token}")
    public String getResetPasswordPage(HttpServletRequest request, Model model,
                                @PathVariable("token") String token) {
        SysSettingVO sysSettingVO = systemSettingService.getSetting();
        if (sysSettingVO == null) {
            sysSettingVO = new SysSettingVO();
        }
        model.addAttribute("systemName", sysSettingVO.getSystemName());
        if (!StringUtils.isEmpty(sysSettingVO.getSystemLogo())) {
            model.addAttribute("systemLogo", sysSettingVO.getSystemLogo());
        }
        model.addAttribute("systemTitle", sysSettingVO.getSystemTitle());
        if (!StringUtils.isEmpty(sysSettingVO.getFavicon())) {
            model.addAttribute("favicon", sysSettingVO.getFavicon());
        }
        if (!passwordForgetService.checkTokenAvailable(token)) {
            model.addAttribute("success", "false");
        } else {
            model.addAttribute("success", "true");
        }
        return PageUrlEnum.RESET_URL.value();
    }

    /**
     * 重置密码
     * @param token
     * @param pwd
     * @param pwd1
     * @return
     */
    @PostMapping(value = "/reset_password")
    @ResponseBody
    public ResponseEntity<PasswordForgetDTO> resetPassword(
            @RequestParam("token") String token,
            @RequestParam("password") String pwd,
            @RequestParam("password1") String pwd1) {
        PasswordForgetDTO passwordForgetDTO;
        if (!passwordForgetService.checkTokenAvailable(token)) {
            passwordForgetDTO = new PasswordForgetDTO(false);
            passwordForgetDTO.setCode(PasswordFindException.PASSWORD_NOT_EQUAL.value());
            passwordForgetDTO.setMsg(messageSource.getMessage(PasswordFindException.PASSWORD_NOT_EQUAL.value(), null, Locale.ROOT));
            return new ResponseEntity<>(passwordForgetDTO, HttpStatus.OK);
        }
        if (!pwd.equals(pwd1)) {
            passwordForgetDTO = new PasswordForgetDTO(false);
            passwordForgetDTO.setCode(PasswordFindException.PASSWORD_NOT_EQUAL.value());
            passwordForgetDTO.setMsg(messageSource.getMessage(PasswordFindException.PASSWORD_NOT_EQUAL.value(), null, Locale.ROOT));
            return new ResponseEntity<>(passwordForgetDTO, HttpStatus.OK);
        }
        if(!StringUtils.hasText(pwd)) {
            passwordForgetDTO = new PasswordForgetDTO(false);
            passwordForgetDTO.setCode(PasswordFindException.PASSWORD_DOES_NOT_HAVE_TEXT.value());
            passwordForgetDTO.setMsg(messageSource.getMessage(PasswordFindException.PASSWORD_DOES_NOT_HAVE_TEXT.value(), null, Locale.ROOT));
            return new ResponseEntity<>(passwordForgetDTO, HttpStatus.OK);
        }
        return ResponseEntity.ok(passwordForgetService.resetPassword(token, pwd));
    }
}
