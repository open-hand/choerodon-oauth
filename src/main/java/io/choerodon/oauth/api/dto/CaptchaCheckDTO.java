package io.choerodon.oauth.api.dto;

import io.choerodon.oauth.infra.dataobject.PasswordPolicyDO;

public class CaptchaCheckDTO {
    private Boolean success;
    private String msg;
    private String code;
    private UserDTO user;
    private Long disableTime;
    private PasswordPolicyDO passwordPolicyDO;

    public CaptchaCheckDTO() {
    }

    public CaptchaCheckDTO(PasswordForgetDTO passwordForgetDTO, PasswordPolicyDO passwordPolicyDO) {
        this.success = passwordForgetDTO.getSuccess();
        this.msg = passwordForgetDTO.getMsg();
        this.code = passwordForgetDTO.getCode();
        this.user = passwordForgetDTO.getUser();
        this.disableTime = passwordForgetDTO.getDisableTime();
        this.passwordPolicyDO = passwordPolicyDO;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public Long getDisableTime() {
        return disableTime;
    }

    public void setDisableTime(Long disableTime) {
        this.disableTime = disableTime;
    }

    public PasswordPolicyDO getPasswordPolicyDO() {
        return passwordPolicyDO;
    }

    public void setPasswordPolicyDO(PasswordPolicyDO passwordPolicyDO) {
        this.passwordPolicyDO = passwordPolicyDO;
    }
}
