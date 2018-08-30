package io.choerodon.oauth.api.dto;

/**
 * @author dongfan117@gmail.com
 */
public class PasswordForgetDTO {
    private Boolean success;
    private String msg;
    private String code;
    private UserDTO user;
    private Long disableTime;

    public PasswordForgetDTO() {
        this.success = true;
    }

    public PasswordForgetDTO(Boolean success) {
        this.success = success;
    }

    public PasswordForgetDTO(String msg, String code) {
        this.success = false;
        this.msg = msg;
        this.code = code;
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
}
