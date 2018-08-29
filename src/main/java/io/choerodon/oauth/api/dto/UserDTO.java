package io.choerodon.oauth.api.dto;

/**
 * @author dongfan117@gmail.com
 */
public class UserDTO {
    private Long id;
    private String loginName;
    private String email;

    public UserDTO(Long id, String loginName, String email) {
        this.id = id;
        this.loginName = loginName;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
