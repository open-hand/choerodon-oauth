package io.choerodon.oauth.infra.dto;

import java.time.LocalDate;
import javax.persistence.Id;
import javax.persistence.Table;

import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by wangxiang on 2021/8/25
 */
@Table(name = "hiam_user_info")
public class UserInfoE extends AuditDomain {
    @Id
    private Long userId;
    private String companyName;
    private String invitationCode;
    private Long employeeId;
    private Long textId;
    private String securityLevelCode;
    private LocalDate startDateActive;
    private LocalDate endDateActive;
    private Integer userSource;
    private Integer phoneCheckFlag;
    private Integer emailCheckFlag;
    private Integer passwordResetFlag;
    private LocalDate lockedDate;
    private String dateFormat;
    private String timeFormat;
    private Long defaultTenantId;
    private LocalDate birthday;
    private String nickname;
    private Integer gender;
    private Long countryId;
    private Long regionId;
    private String addressDetail;
    private Long tenantId;
    private Boolean secCheckPhoneFlag;
    private Boolean secCheckEmailFlag;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getTextId() {
        return textId;
    }

    public void setTextId(Long textId) {
        this.textId = textId;
    }

    public String getSecurityLevelCode() {
        return securityLevelCode;
    }

    public void setSecurityLevelCode(String securityLevelCode) {
        this.securityLevelCode = securityLevelCode;
    }

    public LocalDate getStartDateActive() {
        return startDateActive;
    }

    public void setStartDateActive(LocalDate startDateActive) {
        this.startDateActive = startDateActive;
    }

    public LocalDate getEndDateActive() {
        return endDateActive;
    }

    public void setEndDateActive(LocalDate endDateActive) {
        this.endDateActive = endDateActive;
    }

    public Integer getUserSource() {
        return userSource;
    }

    public void setUserSource(Integer userSource) {
        this.userSource = userSource;
    }

    public Integer getPhoneCheckFlag() {
        return phoneCheckFlag;
    }

    public void setPhoneCheckFlag(Integer phoneCheckFlag) {
        this.phoneCheckFlag = phoneCheckFlag;
    }

    public Integer getEmailCheckFlag() {
        return emailCheckFlag;
    }

    public void setEmailCheckFlag(Integer emailCheckFlag) {
        this.emailCheckFlag = emailCheckFlag;
    }

    public Integer getPasswordResetFlag() {
        return passwordResetFlag;
    }

    public void setPasswordResetFlag(Integer passwordResetFlag) {
        this.passwordResetFlag = passwordResetFlag;
    }

    public LocalDate getLockedDate() {
        return lockedDate;
    }

    public void setLockedDate(LocalDate lockedDate) {
        this.lockedDate = lockedDate;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public Long getDefaultTenantId() {
        return defaultTenantId;
    }

    public void setDefaultTenantId(Long defaultTenantId) {
        this.defaultTenantId = defaultTenantId;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public Long getCountryId() {
        return countryId;
    }

    public void setCountryId(Long countryId) {
        this.countryId = countryId;
    }

    public Long getRegionId() {
        return regionId;
    }

    public void setRegionId(Long regionId) {
        this.regionId = regionId;
    }

    public String getAddressDetail() {
        return addressDetail;
    }

    public void setAddressDetail(String addressDetail) {
        this.addressDetail = addressDetail;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Boolean getSecCheckPhoneFlag() {
        return secCheckPhoneFlag;
    }

    public void setSecCheckPhoneFlag(Boolean secCheckPhoneFlag) {
        this.secCheckPhoneFlag = secCheckPhoneFlag;
    }

    public Boolean getSecCheckEmailFlag() {
        return secCheckEmailFlag;
    }

    public void setSecCheckEmailFlag(Boolean secCheckEmailFlag) {
        this.secCheckEmailFlag = secCheckEmailFlag;
    }
}
