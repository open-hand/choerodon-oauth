package io.choerodon.oauth.api.controller.v1;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

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

    /**
     * 进入找回密码页面
     *
     * @return path
     */
    @GetMapping(value = "/find")
    public String find(HttpServletRequest request, Model model) {
        request.getSession().removeAttribute("userId");
        request.getSession().removeAttribute("userName");
       // todo 数据迁移后恢复
//        SysSettingVO sysSettingVO = systemSettingService.getSetting();
//        if (sysSettingVO == null) {
//            sysSettingVO = new SysSettingVO();
//        }
        SysSettingVO sysSettingVO = new SysSettingVO();
        sysSettingVO.setSystemName("Choerodon");
        sysSettingVO.setRegisterEnabled(true);
        sysSettingVO.setSystemLogo("");
        sysSettingVO.setSystemTitle("Choerodon | 多云应用技术集成平台");
        sysSettingVO.setFavicon("");
        sysSettingVO.setRegisterUrl("http://choerodon.staging.saas.hand-china.com/#/base/register-organization");
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
        // todo 数据迁移后恢复
//        SysSettingVO sysSettingVO = systemSettingService.getSetting();
//        if (sysSettingVO == null) {
//            sysSettingVO = new SysSettingVO();
//        }
        SysSettingVO sysSettingVO = new SysSettingVO();
        sysSettingVO.setSystemName("Choerodon");
        sysSettingVO.setRegisterEnabled(true);
        sysSettingVO.setSystemLogo("");
        sysSettingVO.setSystemTitle("Choerodon | 多云应用技术集成平台");
        sysSettingVO.setFavicon("");
        sysSettingVO.setRegisterUrl("http://choerodon.staging.saas.hand-china.com/#/base/register-organization");
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
        model.addAttribute("loginPage", loginPage);
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
            @RequestParam("password") String pwd) {
        PasswordForgetDTO passwordForgetDTO;
        if (!passwordForgetService.checkTokenAvailable(token)) {
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
