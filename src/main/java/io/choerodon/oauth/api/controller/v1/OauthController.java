package io.choerodon.oauth.api.controller.v1;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.security.Principal;
import java.util.*;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.MessageSource;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import io.choerodon.oauth.api.dto.OrganizationDTO;
import io.choerodon.oauth.app.service.OrganizationService;
import io.choerodon.oauth.core.password.PasswordPolicyManager;
import io.choerodon.oauth.core.password.domain.BasePasswordPolicyDO;
import io.choerodon.oauth.core.password.domain.BaseUserDO;
import io.choerodon.oauth.core.password.mapper.BasePasswordPolicyMapper;
import io.choerodon.oauth.domain.oauth.entity.ClientE;
import io.choerodon.oauth.domain.repository.ClientRepository;
import io.choerodon.oauth.domain.service.IUserService;
import io.choerodon.oauth.domain.service.TokenService;
import io.choerodon.oauth.infra.dataobject.AccessTokenDO;
import io.choerodon.oauth.infra.dataobject.UserDO;


/**
 * @author wuguokai
 */
@RefreshScope
@Controller
public class OauthController {
    private static final String ATTRIBUTE_NAME = "SPRING_SECURITY_SAVED_REQUEST";
    private static final String USERNAME_NOT_FOUND_OR_PASSWORD_WRONG_EXCEPTION = "usernameNotFoundOrPasswordIsWrong";
    private static final String INDEX_TEMPLATE_NAME = "index-default";
    private static final Logger LOGGER = LoggerFactory.getLogger(OauthController.class);
    private static Map<String, String> loginWayMap = new LinkedHashMap<>();

    static {
        loginWayMap.put(/*ExtraLoginWay.EMAIL*/"mail", "邮箱");
        loginWayMap.put(/*ExtraLoginWay.PHONE*/"phone", "手机");
    }

    @Value("${choerodon.oauth.loginPage.profile:theCustomLoginPageProfile}")
    private String loginProfile;
    @Value("${choerodon.oauth.loginPage.title:Choerodon}")
    private String loginTitle;
    @Value("${choerodon.oauth.login.field:#{null}}")
    private String[] extraLoginWays;
    private Locale currentLocale = Locale.ROOT;

    private MessageSource messageSource;
    private OrganizationService organizationService;
    private DefaultKaptcha captchaProducer;

    private TokenService tokenService;

    private BasePasswordPolicyMapper basePasswordPolicyMapper;
    private PasswordPolicyManager passwordPolicyManager;
    private ClientRepository clientRepository;
    @Autowired
    private IUserService iUserService;

    @Value("${choerodon.default.redirect.url:/}")
    private String defaultUrl;

    public OauthController(MessageSource messageSource, OrganizationService organizationService,
                           DefaultKaptcha captchaProducer, TokenService tokenService,
                           PasswordPolicyManager passwordPolicyManager,
                           BasePasswordPolicyMapper basePasswordPolicyMapper,
                           ClientRepository clientRepository) {
        this.messageSource = messageSource;
        this.organizationService = organizationService;
        this.captchaProducer = captchaProducer;
        this.tokenService = tokenService;
        this.passwordPolicyManager = passwordPolicyManager;
        this.basePasswordPolicyMapper = basePasswordPolicyMapper;
        this.clientRepository = clientRepository;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() {
        return "redirect:" + defaultUrl;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(HttpServletRequest request, Model model, HttpSession session, @RequestParam(required = false) String device) {
        List<String> loginWays = initLoginWay();
        String returnPage = INDEX_TEMPLATE_NAME;
        Map<String, String> error = new HashMap<>();
        error.put(USERNAME_NOT_FOUND_OR_PASSWORD_WRONG_EXCEPTION, null);
        // error.put("passwordWrong",null);
        model.addAttribute("title", loginTitle);
        model.addAttribute("loginWays", String.join("/", loginWays));
        if (!loginProfile.equals("theCustomLoginPageProfile")) {
            URL url = this.getClass().getResource("/templates/index-" + loginProfile + ".html");
            if (url != null) {
                model.addAllAttributes(error);
                returnPage = "index-" + loginProfile;
            }
        }
        if ("mobile".equals(device)) {
            returnPage = "index-mobile";
        }
        String username = (String) session.getAttribute("username");
        String errorCode = (String) session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        Object[] params = (Object[]) session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION_PARAMS");
        session.removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        session.removeAttribute("username");
        session.removeAttribute("SPRING_SECURITY_LAST_EXCEPTION_PARAMS");
        //登录验证码策略
        String[] clients = {};
        if (session.getAttribute(ATTRIBUTE_NAME) != null) {
            clients = ((DefaultSavedRequest) session.getAttribute(ATTRIBUTE_NAME)).getParameterValues("client_id");
        }
        OrganizationDTO organization = null;
        if (clients != null && clients.length != 0) {
            ClientE clientE = clientRepository.selectByName(clients[0]);
            if (clientE != null) {
                organization = organizationService.queryOrganizationById(clientE.getOrganizationId());
            }
        }
        if (username == null) {
            return returnPage;
        }
        UserDO userDO = iUserService.findUser(username);
        if (userDO != null) {
            organization = organizationService.queryOrganizationById(userDO.getOrganizationId());
        }
        if (organization != null) {
            BaseUserDO baseUserDO = new BaseUserDO();
                if (userDO != null) {
                    BeanUtils.copyProperties(userDO, baseUserDO);
                    BasePasswordPolicyDO passwordPolicy = basePasswordPolicyMapper.findByOrgId(organization.getId());
                    boolean showCaptcha = !userDO.getLocked() && passwordPolicyManager.isNeedCaptcha(passwordPolicy, baseUserDO);
                    model.addAttribute("isNeedCaptcha",showCaptcha);
                }
        }
        //数据库中无该用户
        if (userDO == null) {
            error.put(USERNAME_NOT_FOUND_OR_PASSWORD_WRONG_EXCEPTION, messageSource.getMessage(USERNAME_NOT_FOUND_OR_PASSWORD_WRONG_EXCEPTION, null, "登录名不存在或登录名与密码错误", currentLocale));
            model.addAllAttributes(error);
            return returnPage;
        }
        if (organization == null) {
            error.put("loginFailed", messageSource.getMessage("error.organization.not.exist", null,"用户或客户端所属组织不存在", currentLocale));
        }
        if (errorCode != null) {
            error.put(errorCode, messageSource.getMessage(errorCode, params, "登录失败", currentLocale));
        }
        model.addAllAttributes(error);
        return returnPage;
    }

    private List<String> initLoginWay() {
        List<String> loginWays = new ArrayList<>();
        loginWays.add("用户名");
        if (extraLoginWays != null && extraLoginWays.length > 0) {
            for (String way : extraLoginWays) {
                String hint = loginWayMap.get(way);
                if (!StringUtils.isEmpty(hint)) {
                    loginWays.add(hint);
                }
            }
        }
        return loginWays;
    }

    @RequestMapping(value = "/public/captcha")
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

    @ResponseBody
    @RequestMapping("/api/user")
    public Principal user(Principal principal) {
        return principal;
    }

    @ResponseBody
    @RequestMapping(value = "/api/client/access", method = RequestMethod.POST)
    public AccessTokenDO clientAccess(@RequestBody String additionalInfo) {
        return tokenService.create(additionalInfo);
    }
}
