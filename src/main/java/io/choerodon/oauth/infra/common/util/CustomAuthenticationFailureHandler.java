package io.choerodon.oauth.infra.common.util;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import io.choerodon.oauth.api.service.UserService;
import io.choerodon.oauth.core.password.record.LoginRecord;
import io.choerodon.oauth.domain.entity.UserE;
import io.choerodon.oauth.infra.enums.LoginException;
import io.choerodon.oauth.infra.exception.CustomAuthenticationException;

/**
 * @author wuguokai
 */
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Value("${choerodon.oauth.login.path:/oauth/login}")
    private String loginPath;
    @Autowired
    private LoginRecord loginRecord;
    @Autowired
    private UserService userService;

    public void setLoginRecord(LoginRecord loginRecord) {
        this.loginRecord = loginRecord;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("username",
                    username);
            session.setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception.getMessage());
            if (exception instanceof CustomAuthenticationException) {
                session.setAttribute("SPRING_SECURITY_LAST_EXCEPTION_PARAMS",
                        ((CustomAuthenticationException) exception).getParameters());
            }

        }
        String message = null;
        if (exception != null) {
            message = exception.getMessage();
        }
        UserE user = userService.queryByLoginField(username);
        if (user != null
                && LoginException.USERNAME_NOT_FOUND_OR_PASSWORD_IS_WRONG.value().equalsIgnoreCase(message)) {
            loginRecord.loginError(user.getId());
        }
        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        redirectStrategy.sendRedirect(request, response, loginPath + "?username=" + username);
    }
}
