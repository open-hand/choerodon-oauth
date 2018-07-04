package io.choerodon.oauth.infra.common.util;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.session.data.redis.RedisOperationsSessionRepository;
import org.springframework.stereotype.Component;

import io.choerodon.oauth.infra.config.OauthProperties;

/**
 * @author wuguokai
 */
@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomLogoutSuccessHandler.class);

    @Autowired
    private OauthProperties oauthProperties;

    @Autowired
    private CustomTokenStore customTokenStore;

    @Value("server.contextPath:")
    private String contentPath;

    @Autowired
    private RedisOperationsSessionRepository redisOperationsSessionRepository;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException {
        LOGGER.info("Logout:{}", authentication != null ? authentication.getName() : "null");
        if (oauthProperties.isClearToken()) {
            String sessionId = request.getSession().getId();
            request.getSession().invalidate();
            String value = request.getHeader("Authorization");
            if (value != null) {
                value = value.replace("Bearer", "").trim();
                LOGGER.info("clear access token :{} ", value);
                customTokenStore.removeAccessToken(value);
                customTokenStore.removeRefreshToken(value);

                redisOperationsSessionRepository.delete(sessionId);
            }
        }
        String redirect = request.getHeader("Redirect") != null ? request.getHeader("Redirect").toLowerCase() : null;
        if (redirect != null) {
            response.setStatus(200);
            if (redirect.equals("no redirect")) {
                String data = "ok";
                OutputStream stream = response.getOutputStream();
                stream.write(data.getBytes("UTF-8"));
                return;
            }
        }

        String referer = request.getHeader("Referer");
        if (referer != null) {
            response.sendRedirect(referer);
        } else {
            response.sendRedirect(contentPath + "/login");
        }
    }

}
