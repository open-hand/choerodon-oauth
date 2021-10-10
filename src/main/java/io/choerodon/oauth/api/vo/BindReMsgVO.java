package io.choerodon.oauth.api.vo;

/**
 * Created by wangxiang on 2021/8/26
 */
public class BindReMsgVO {
    private Boolean status;

    private String message;

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
