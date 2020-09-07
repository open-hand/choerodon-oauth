package io.choerodon.oauth.api.controller.v1;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hzero.boot.oauth.domain.entity.BasePasswordPolicy;
import org.hzero.boot.oauth.domain.repository.BasePasswordPolicyRepository;
import org.hzero.boot.oauth.domain.service.UserPasswordService;
import org.hzero.core.message.MessageAccessor;
import org.hzero.oauth.domain.entity.User;
import org.hzero.oauth.infra.encrypt.EncryptClient;
import org.hzero.oauth.security.constant.SecurityAttributes;
import org.hzero.oauth.security.util.LoginUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import io.choerodon.oauth.api.vo.PasswordForgetDTO;
import io.choerodon.oauth.api.vo.SysSettingVO;
import io.choerodon.oauth.app.service.PasswordForgetService;
import io.choerodon.oauth.app.service.SystemSettingService;
import io.choerodon.oauth.infra.enums.PageUrlEnum;
import io.choerodon.oauth.infra.enums.PasswordFindException;

/**
 * @author wuguokai
 */
@Controller
@RequestMapping("/choerodon/password")
public class PasswordC7NController {

    private static final String DEFAULT_PAGE = "password-find";


    private String loginPage = "/oauth/choerodon/login";
    @Autowired
    private PasswordForgetService passwordForgetService;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private SystemSettingService systemSettingService;
    @Autowired
    private EncryptClient encryptClient;
    @Autowired
    BasePasswordPolicyRepository basePasswordPolicyRepository;
    @Autowired
    UserPasswordService userPasswordService;

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

        model.addAttribute("loginPage", loginPage);
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
        String publicKey = encryptClient.getPublicKey();
        model.addAttribute(LoginUtil.FIELD_PUBLIC_KEY, publicKey);
        model.addAttribute("loginPage", loginPage);
        return PageUrlEnum.RESET_URL.value();
    }

    /**
     * 进入修改默认密码页面
     *
     * @return path
     */
    @GetMapping(value = "/update_default_pwd_page")
    public String getUpdateDefaultPwdPage(HttpServletRequest request, Model model) {
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

        String publicKey = encryptClient.getPublicKey();
        model.addAttribute(LoginUtil.FIELD_PUBLIC_KEY, publicKey);
        model.addAttribute("loginPage", loginPage);
        return PageUrlEnum.UPDATE_DEFAULT_PWD_PAGE.value();
    }

    /**
     * 修改密码
     * @param pwd
     * @return
     */
    @PostMapping(value = "/update_password")
    @ResponseBody
    public ResponseEntity<PasswordForgetDTO> resetPassword(HttpSession session, @RequestParam("password") String pwd) {
        PasswordForgetDTO passwordForgetDTO;
        String decryptPassword = encryptClient.decrypt(pwd);
        SysSettingVO setting = systemSettingService.getSetting();
        String defaultPassword = setting.getDefaultPassword();

        User user = (User) session.getAttribute(SecurityAttributes.SECURITY_LOGIN_USER);
        BasePasswordPolicy basePasswordPolicy = basePasswordPolicyRepository.selectPasswordPolicy(user.getTenantId());

        if (decryptPassword.equals(defaultPassword) || basePasswordPolicy.getOriginalPassword().equals(decryptPassword)) {
            passwordForgetDTO = new PasswordForgetDTO(false);
            passwordForgetDTO.setCode(PasswordFindException.PASSWORD_EQUAL_DEFAULT_ERROR.value());
            passwordForgetDTO.setMessage(MessageAccessor.getMessage(PasswordFindException.PASSWORD_EQUAL_DEFAULT_ERROR.value()).desc());
            return new ResponseEntity<>(passwordForgetDTO, HttpStatus.OK);
        }

        if(!StringUtils.hasText(decryptPassword)) {
            passwordForgetDTO = new PasswordForgetDTO(false);
            passwordForgetDTO.setCode(PasswordFindException.PASSWORD_DOES_NOT_HAVE_TEXT.value());
            passwordForgetDTO.setMessage(MessageAccessor.getMessage(PasswordFindException.PASSWORD_DOES_NOT_HAVE_TEXT.value()).desc());
            return new ResponseEntity<>(passwordForgetDTO, HttpStatus.OK);
        }
        userPasswordService.updateUserPassword(user.getId(), decryptPassword);
        passwordForgetDTO = new PasswordForgetDTO();
        passwordForgetDTO.setSuccess(true);
        passwordForgetDTO.setUser(user);
        return ResponseEntity.ok(passwordForgetDTO);
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
            @RequestParam("password") String pwd) {
        PasswordForgetDTO passwordForgetDTO;
        if (!passwordForgetService.checkTokenAvailable(token)) {
            passwordForgetDTO = new PasswordForgetDTO(false);
            passwordForgetDTO.setCode(PasswordFindException.RESET_URL_INVAILED.value());
            passwordForgetDTO.setMessage(MessageAccessor.getMessage(PasswordFindException.RESET_URL_INVAILED.value()).desc());
            return new ResponseEntity<>(passwordForgetDTO, HttpStatus.OK);
        }
        if(!StringUtils.hasText(pwd)) {
            passwordForgetDTO = new PasswordForgetDTO(false);
            passwordForgetDTO.setCode(PasswordFindException.PASSWORD_DOES_NOT_HAVE_TEXT.value());
            passwordForgetDTO.setMessage(MessageAccessor.getMessage(PasswordFindException.PASSWORD_DOES_NOT_HAVE_TEXT.value()).desc());
            return new ResponseEntity<>(passwordForgetDTO, HttpStatus.OK);
        }
        return ResponseEntity.ok(passwordForgetService.resetPassword(token, pwd));
    }
}
