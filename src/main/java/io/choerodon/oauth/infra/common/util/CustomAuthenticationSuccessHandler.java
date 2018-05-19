package io.choerodon.oauth.infra.common.util;

import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import io.choerodon.oauth.core.password.record.LoginRecord;
import io.choerodon.oauth.domain.service.IUserService;
import io.choerodon.oauth.infra.dataobject.UserDO;


/**
 * @author wuguokai
 */

@Component
public class CustomAuthenticationSuccessHandler extends
        SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private IUserService userService;

    @Autowired
    private LoginRecord loginRecord;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest,
                                        HttpServletResponse httpServletResponse,
                                        Authentication authentication)
            throws IOException, ServletException {

        String username = httpServletRequest.getParameter("username");
        UserDO userDO = userService.findUser(username);
        userDO.setLastLoginAt(new Date());
        userDO.setPasswordAttempt(0);
        userService.updateByPrimaryKeySelective(userDO);
        loginRecord.loginSuccess(userDO.getId());
        super.onAuthenticationSuccess(httpServletRequest, httpServletResponse, authentication);

    }
}
