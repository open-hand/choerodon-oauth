package io.choerodon.oauth.api.dto;

public class CaptchaCheckDTO {
    private Boolean success;
    private String msg;
    private String code;
    private UserDTO user;
    private Long disableTime;
    private PasswordPolicyDTO passwordPolicyDTO;

    public CaptchaCheckDTO() {
    }

    public CaptchaCheckDTO(PasswordForgetDTO passwordForgetDTO, PasswordPolicyDTO passwordPolicyDTO) {
        this.success = passwordForgetDTO.getSuccess();
        this.msg = passwordForgetDTO.getMsg();
        this.code = passwordForgetDTO.getCode();
        this.user = passwordForgetDTO.getUser();
        this.disableTime = passwordForgetDTO.getDisableTime();
        this.passwordPolicyDTO = passwordPolicyDTO;
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

    public PasswordPolicyDTO getPasswordPolicyDTO() {
        return passwordPolicyDTO;
    }

    public void setPasswordPolicyDTO(PasswordPolicyDTO passwordPolicyDTO) {
        this.passwordPolicyDTO = passwordPolicyDTO;
    }
}
