package io.choerodon.oauth.api.vo;

import org.hzero.oauth.domain.entity.User;

/**
 * @author dongfan117@gmail.com
 */
public class PasswordForgetDTO {
    private Boolean success;
    private String message;
    private String code;
    private User user;
    private Long disableTime;

    public PasswordForgetDTO() {
        this.setSuccess(true);
    }

    public PasswordForgetDTO(Boolean success) {
        this.setSuccess(success);
    }

    public Boolean getSuccess() {
        return success;
    }


    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getDisableTime() {
        return disableTime;
    }

    public void setDisableTime(Long disableTime) {
        this.disableTime = disableTime;
    }
}
