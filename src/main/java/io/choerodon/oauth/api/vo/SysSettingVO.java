package io.choerodon.oauth.api.vo;


import io.choerodon.mybatis.domain.AuditDomain;

/**
 * @author zmf
 * @since 2018-10-15
 */
public class SysSettingVO extends AuditDomain {

    private Long id;

    private String favicon;

    private String systemLogo;

    private String systemTitle;

    private String systemName;

    private String defaultPassword;

    private String defaultLanguage;

    private Integer minPasswordLength;

    private Integer maxPasswordLength;

    private Boolean registerEnabled;

    private String registerUrl;

    private String resetGitlabPasswordUrl;

    private String themeColor;

    private Boolean enableUpdateDefaultPwd;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    public String getSystemLogo() {
        return systemLogo;
    }

    public void setSystemLogo(String systemLogo) {
        this.systemLogo = systemLogo;
    }

    public String getSystemTitle() {
        return systemTitle;
    }

    public void setSystemTitle(String systemTitle) {
        this.systemTitle = systemTitle;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public Integer getMinPasswordLength() {
        return minPasswordLength;
    }

    public void setMinPasswordLength(Integer minPasswordLength) {
        this.minPasswordLength = minPasswordLength;
    }

    public Integer getMaxPasswordLength() {
        return maxPasswordLength;
    }

    public void setMaxPasswordLength(Integer maxPasswordLength) {
        this.maxPasswordLength = maxPasswordLength;
    }

    public Boolean getRegisterEnabled() {
        return registerEnabled;
    }

    public void setRegisterEnabled(Boolean registerEnabled) {
        this.registerEnabled = registerEnabled;
    }

    public String getRegisterUrl() {
        return registerUrl;
    }

    public void setRegisterUrl(String registerUrl) {
        this.registerUrl = registerUrl;
    }

    public String getResetGitlabPasswordUrl() {
        return resetGitlabPasswordUrl;
    }

    public void setResetGitlabPasswordUrl(String resetGitlabPasswordUrl) {
        this.resetGitlabPasswordUrl = resetGitlabPasswordUrl;
    }

    public String getThemeColor() {
        return themeColor;
    }

    public void setThemeColor(String themeColor) {
        this.themeColor = themeColor;
    }

    public Boolean getEnableUpdateDefaultPwd() {
        return enableUpdateDefaultPwd;
    }

    public void setEnableUpdateDefaultPwd(Boolean enableUpdateDefaultPwd) {
        this.enableUpdateDefaultPwd = enableUpdateDefaultPwd;
    }
}
