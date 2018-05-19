package io.choerodon.oauth.infra.common.util;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.session.SessionInformationExpiredEvent;
import org.springframework.security.web.session.SessionInformationExpiredStrategy;
import org.springframework.stereotype.Component;

/**
 * @author zhipeng.zuo
 */
@Component
public class SingleLoginSessionInformationExpiredStrategy implements SessionInformationExpiredStrategy {


    @Override
    public void onExpiredSessionDetected(SessionInformationExpiredEvent event)
            throws IOException, ServletException {
        HttpServletResponse response = event.getResponse();
        response.setHeader("Content-type", "text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().print("您的账号已在别处登录！要想继续访问，请刷新页面重新登录");
        response.flushBuffer();
    }

}
