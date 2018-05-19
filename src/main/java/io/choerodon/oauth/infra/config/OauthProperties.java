package io.choerodon.oauth.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zhipeng.zuo
 * @author wuguokai
 */
@ConfigurationProperties(prefix = "choerodon.oauth")
public class OauthProperties {

    private boolean clearToken = false;

    private boolean enabledSingleLogin = false;

    private int accessTokenValiditySeconds = 24 * 3600;

    public OauthProperties() {
        //
    }

    public boolean isClearToken() {
        return clearToken;
    }

    public void setClearToken(boolean clearToken) {
        this.clearToken = clearToken;
    }

    public boolean isEnabledSingleLogin() {
        return enabledSingleLogin;
    }

    public void setEnabledSingleLogin(boolean enabledSingleLogin) {
        this.enabledSingleLogin = enabledSingleLogin;
    }

    public int getAccessTokenValiditySeconds() {
        return accessTokenValiditySeconds;
    }

    public void setAccessTokenValiditySeconds(int accessTokenValiditySeconds) {
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
    }
}
