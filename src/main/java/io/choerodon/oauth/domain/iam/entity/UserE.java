package io.choerodon.oauth.domain.iam.entity;

import java.util.Date;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author dongfan117@gmail.com
 * @author superleader8@gamial.com
 */
public class UserE {

    //线程安全的，放心用。
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private Long id;

    private String loginName;

    private String email;

    private Long organizationId;

    private String password;

    private String realName;

    private String phone;

    private String imageUrl;

    private String profilePhoto;

    private String language;

    private String timeZone;

    private Date lastPasswordUpdatedAt;

    private Date lastLoginAt;

    private Boolean isEnabled;

    private Boolean isLocked;

    private Boolean isLdap;

    private Date lockedUntilAt;

    private Integer passwordAttempt;

    private Long objectVersionNumber;

    private UserE() {
    }

    public UserE(Long id, String loginName, String email, Long organizationId, String password, String realName,
                 String phone, String imageUrl, String profilePhoto, String language, String timeZone,
                 Date lastPasswordUpdatedAt, Date lastLoginAt, Boolean isEnabled, Boolean isLocked,
                 Boolean isLdap, Date lockedUntilAt, Integer passwordAttempt,
                 Long objectVersionNumber) {
        this.id = id;
        this.loginName = loginName;
        this.email = email;
        this.organizationId = organizationId;
        this.password = password;
        this.realName = realName;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.profilePhoto = profilePhoto;
        this.language = language;
        this.timeZone = timeZone;
        this.lastPasswordUpdatedAt = lastPasswordUpdatedAt;
        this.lastLoginAt = lastLoginAt;
        this.isEnabled = isEnabled;
        this.isLocked = isLocked;
        this.isLdap = isLdap;
        this.lockedUntilAt = lockedUntilAt;
        this.passwordAttempt = passwordAttempt;
        this.objectVersionNumber = objectVersionNumber;
    }

    public Long getId() {
        return id;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getEmail() {
        return email;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public String getPassword() {
        return password;
    }

    public String getRealName() {
        return realName;
    }

    public String getPhone() {
        return phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public String getLanguage() {
        return language;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public Date getLastPasswordUpdatedAt() {
        return lastPasswordUpdatedAt;
    }

    public Date getLastLoginAt() {
        return lastLoginAt;
    }

    public Boolean getEnabled() {
        return isEnabled;
    }

    public Boolean getLocked() {
        return isLocked;
    }

    public Boolean getLdap() {
        return isLdap;
    }

    public Date getLockedUntilAt() {
        return lockedUntilAt;
    }

    public Integer getPasswordAttempt() {
        return passwordAttempt;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void unlocked() {
        this.isLocked = false;
    }

    public void locked() {
        this.isLocked = true;
    }

    public void encodePassword() {
        this.password = ENCODER.encode(this.getPassword());
    }
}
