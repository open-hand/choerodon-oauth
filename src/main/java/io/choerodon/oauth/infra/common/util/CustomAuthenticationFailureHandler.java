package io.choerodon.oauth.infra.common.util;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import io.choerodon.oauth.infra.enums.LoginExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import io.choerodon.oauth.core.password.record.LoginRecord;
import io.choerodon.oauth.domain.service.IUserService;
import io.choerodon.oauth.infra.dataobject.UserDO;
import io.choerodon.oauth.infra.exception.CustomAuthenticationException;

/**
 * @author wuguokai
 */
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Autowired
    private LoginRecord loginRecord;
    @Autowired
    private IUserService iUserService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException,
            ServletException {
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
        UserDO userDO = iUserService.findUser(username);
        if (userDO != null
                && LoginExceptions.USERNAME_NOT_FOUND_OR_PASSWORD_IS_WRONG.value().equalsIgnoreCase(message)) {
            //TODO 在开启密码策略并且有错误次数时才记录
            loginRecord.loginError(userDO.getId());
        }
        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        redirectStrategy.sendRedirect(request, response, "/login?username=" + username);
    }
}
