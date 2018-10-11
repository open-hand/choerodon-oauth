package io.choerodon.oauth.infra.common.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Component;

import io.choerodon.oauth.api.service.UserService;
import io.choerodon.oauth.core.password.record.LoginRecord;
import io.choerodon.oauth.domain.entity.UserE;


/**
 * @author wuguokai
 */

@Component
public class CustomAuthenticationSuccessHandler extends
        SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginRecord loginRecord;

    @Value("${choerodon.default.redirect.url:/}")
    private String defaultUrl;

    @Value("${choerodon.oauth.login.ssl:false}")
    private boolean useSSL;

    @PostConstruct
    private void init() {
        this.setDefaultTargetUrl(defaultUrl);
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setLoginRecord(LoginRecord loginRecord) {
        this.loginRecord = loginRecord;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        String username = request.getParameter("username");
        UserE user = userService.queryByLoginField(username);
        user.setLastLoginAt(new Date());
        user.setPasswordAttempt(0);
        userService.updateSelective(user);
        loginRecord.loginSuccess(user.getId());

        DefaultSavedRequest saveRequest = null;
        HttpSession session = request.getSession(false);
        if (session != null) {
            saveRequest = (DefaultSavedRequest) session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
        }
        try {
            if (saveRequest == null) {
                super.onAuthenticationSuccess(request, response, authentication);
            }
            if (useSSL) {
                Field schemeField = DefaultSavedRequest.class.getDeclaredField("scheme");
                schemeField.setAccessible(true);
                schemeField.set(saveRequest, "https");
                Field portStringField = DefaultSavedRequest.class.getDeclaredField("serverPort");
                portStringField.setAccessible(true);
                portStringField.set(saveRequest, 443);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
