package io.choerodon.oauth.infra.common.util;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
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

    @Autowired
    private ServletContext servletContext;

    @Value("${choerodon.oauth.login.path:/login}")
    private String loginPath;

    public void setOauthProperties(OauthProperties oauthProperties) {
        this.oauthProperties = oauthProperties;
    }

    public void setCustomTokenStore(CustomTokenStore customTokenStore) {
        this.customTokenStore = customTokenStore;
    }

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        String name = Optional.ofNullable(authentication).map(Authentication::getName).orElse("null");
        LOGGER.info("Logout:{}", name);
        if (oauthProperties.isClearToken()) {
            request.getSession().invalidate();
            String token = extractHeaderToken(request);
            if (token != null) {
                LOGGER.info("clear access token :{} ", token);
                customTokenStore.removeAccessToken(token);
                customTokenStore.removeRefreshToken(token);
            }
        }

        String referer = request.getHeader("Referer");
        if (referer != null) {
            response.sendRedirect(referer);
        } else {
            response.sendRedirect(servletContext.getContextPath() + loginPath);
        }
    }

    protected String extractHeaderToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null) {
            return header.replace("Bearer", "").trim();
        }
        return request.getParameter("access_token");
    }
}
