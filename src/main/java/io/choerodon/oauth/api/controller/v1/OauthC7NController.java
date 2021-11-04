package io.choerodon.oauth.api.controller.v1;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.code.kaptcha.impl.DefaultKaptcha;

import io.choerodon.oauth.api.vo.SysSettingVO;
import io.choerodon.oauth.app.service.SystemSettingService;
import io.choerodon.oauth.infra.enums.LoginException;
import io.choerodon.oauth.infra.enums.ReturnPage;

import org.hzero.oauth.domain.entity.User;
import org.hzero.oauth.domain.service.UserLoginService;
import org.hzero.oauth.infra.encrypt.EncryptClient;
import org.hzero.oauth.security.config.SecurityProperties;
import org.hzero.oauth.security.constant.SecurityAttributes;
import org.hzero.oauth.security.util.LoginUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * @author wuguokai
 */
@RefreshScope
@Controller
@RequestMapping("/choerodon")
public class OauthC7NController {
    private static final String LOGIN_FILED = "username";
    private static final String USERNAME_NOT_FOUND_OR_PASSWORD_IS_WRONG = "usernameNotFoundOrPasswordIsWrong";

    private static final Logger LOGGER = LoggerFactory.getLogger(OauthC7NController.class);

    @Value("${choerodon.oauth.loginPage.profile:default}")
    private String loginProfile;

    private DefaultKaptcha captchaProducer;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private SystemSettingService systemSettingService;

    @Value("${choerodon.default.redirect.url:/}")
    private String defaultUrl;

    @Value("${choerodon.default.icp: }")
    private String icp;
    @Value("${choerodon.default.icpUrl: }")
    private String icpUrl;

    @Value("${choerodon.default.company: }")
    private String company;

    private final SecurityProperties securityProperties;

    private final EncryptClient encryptClient;

    public OauthC7NController(
            DefaultKaptcha captchaProducer,
            SecurityProperties securityProperties,
            EncryptClient encryptClient) {
        this.captchaProducer = captchaProducer;
        this.securityProperties = securityProperties;
        this.encryptClient = encryptClient;
    }

    @GetMapping(value = "/")
    public String index() {
        return "redirect:" + defaultUrl;
    }

    @GetMapping(value = "/login")
    public String login(HttpServletRequest request, Model model,
                        HttpSession session, @RequestParam(required = false) String device) {
        setModelSysSetting(model);
        // 是否加密
        if (securityProperties.getPassword().isEnableEncrypt()) {
            String publicKey = encryptClient.getPublicKey();
            model.addAttribute(LoginUtil.FIELD_PUBLIC_KEY, publicKey);
            session.setAttribute(LoginUtil.FIELD_PUBLIC_KEY, publicKey);
        }
        //默认登录页面
        ReturnPage returnPage = ReturnPage.getByProfile(loginProfile);
        if (!StringUtils.isEmpty(device)) {
            returnPage = ReturnPage.getByProfile(device);
        }

        User user = userLoginService.queryRequestUser(request);
        String userName = request.getParameter("username");
        // 错误消息
        String exceptionMessage = (String) session.getAttribute(SecurityAttributes.SECURITY_LAST_EXCEPTION);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(exceptionMessage)
                && (org.apache.commons.lang3.StringUtils.equalsIgnoreCase(user.getLoginName(), userName)
                || org.apache.commons.lang3.StringUtils.equalsIgnoreCase(user.getEmail(), userName))) {
            model.addAttribute(USERNAME_NOT_FOUND_OR_PASSWORD_IS_WRONG, exceptionMessage);
        }
        //如果用户为null  又有错误信息，则错误信息统一展示成用户名密码错误
        if (org.apache.commons.lang3.StringUtils.isNotBlank(exceptionMessage) && user == null) {
            model.addAttribute(USERNAME_NOT_FOUND_OR_PASSWORD_IS_WRONG, "用户名密码错误");
        }


        if (icp != null && !icp.equals("")) {
            model.addAttribute("icp", icp);
        }

        if (icpUrl != null && !icpUrl.equals("")) {
            model.addAttribute("icpUrl", icpUrl);
        }

        if (company != null && !company.equals("")) {
            model.addAttribute("company", company);
        }

        //如果是短信登录连同CaptchaKey一起返回
        String loginType = request.getParameter("type");
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(loginType)
                && org.apache.commons.lang3.StringUtils.equalsIgnoreCase(loginType, "sms")) {
            model.addAttribute("captchaKey", session.getAttribute("captchaKey"));
            model.addAttribute("phone", session.getAttribute("phone"));
        }

        if (user == null) {
            return returnPage.fileName();
        } else {
            if (user.getLdap()) {
                model.addAttribute("userName", user.getLoginName());
            } else {
                model.addAttribute("userName", user.getEmail());
            }
        }

        model.addAttribute("isNeedCaptcha", userLoginService.isNeedCaptcha(user));
        return returnPage.fileName();
    }

    private void setModelSysSetting(Model model) {
        SysSettingVO sysSettingVO = systemSettingService.getSetting();
        if (sysSettingVO == null) {
            sysSettingVO = new SysSettingVO();
        }
        model.addAttribute("systemName", sysSettingVO.getSystemName());
        String systemLogo = sysSettingVO.getSystemLogo();
        if (!StringUtils.isEmpty(systemLogo)) {
            // 为模版引擎统一数据
            model.addAttribute("systemLogo", systemLogo);
        }
        model.addAttribute("systemTitle", sysSettingVO.getSystemTitle());
        String favicon = sysSettingVO.getFavicon();
        if (!StringUtils.isEmpty(favicon)) {
            // 为模版引擎统一数据
            model.addAttribute("favicon", favicon);
        }
        if (sysSettingVO.getRegisterEnabled() != null && sysSettingVO.getRegisterEnabled() && !StringUtils.isEmpty(sysSettingVO.getRegisterUrl())) {
            model.addAttribute("registerUrl", sysSettingVO.getRegisterUrl());
        }
    }

    @GetMapping(value = "/public/captcha")
    public void createCaptcha(HttpServletRequest request,
                              HttpServletResponse response) {
        response.setDateHeader("Expires", 0);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        response.addHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");
        response.setContentType("image/jpeg");
        ServletOutputStream out = null;
        try {
            String capText = captchaProducer.createText();
            HttpSession session = request.getSession();
            session.setAttribute("captchaCode", capText);
            BufferedImage bi = captchaProducer.createImage(capText);
            out = response.getOutputStream();
            ImageIO.write(bi, "jpg", out);
            out.flush();
        } catch (Exception e) {
            LOGGER.info("create captcha fail: {}", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                    LOGGER.info("captcha output close fail: {}", e);
                }
            }
        }
    }


}
