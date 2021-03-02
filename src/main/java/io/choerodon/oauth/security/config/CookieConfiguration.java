package io.choerodon.oauth.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: scp
 * @Description:
 * @Date: Created in 2021/3/1
 * @Modified By:
 */
@ConfigurationProperties(prefix = "server.servlet.session.cookie")
@Configuration("cookieProperties")
public class CookieConfiguration {
    private boolean secure;
    private String sameSite;

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public String getSameSite() {
        return sameSite;
    }

    public void setSameSite(String sameSite) {
        this.sameSite = sameSite;
    }
}
