//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.hzero.oauth.security.social;

import java.io.IOException;
import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hzero.oauth.security.config.SecurityProperties;
import org.hzero.oauth.security.event.LoginEvent;
import org.hzero.oauth.security.util.RequestUtil;
import org.hzero.starter.social.core.security.SocialSuccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;

/**
 * c7n覆盖私有方法{@link #determineTargetUrl}
 */
public class CustomSocialSuccessHandler extends SocialSuccessHandler implements ApplicationContextAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomSocialSuccessHandler.class);
    private SecurityProperties securityProperties;
    private ApplicationContext applicationContext;

    public CustomSocialSuccessHandler(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @PostConstruct
    private void init() {
        this.setDefaultTargetUrl(this.securityProperties.getLogin().getSuccessUrl());
    }

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        LoginEvent loginEvent = new LoginEvent(request);
        this.applicationContext.publishEvent(loginEvent);
        super.onAuthenticationSuccess(request, response, authentication);
    }

    /**
     *
     * @param request
     * @param response
     * @return 覆盖重定向地址 todo
     */
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
        String targetUrl = RequestUtil.getBaseURL(request) + "/oauth/authorize?response_type=token&client_id=" + this.securityProperties.getLogin().getDefaultClientId() + "&redirect_uri=" + this.securityProperties.getLogin().getSuccessUrl();
        LOGGER.debug("Using default authorize target url: [{}]", targetUrl);
        return targetUrl;
    }

    protected SecurityProperties getSecurityProperties() {
        return this.securityProperties;
    }

    protected ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
