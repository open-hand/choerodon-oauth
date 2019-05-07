package io.choerodon.oauth.domain.entity;

import io.choerodon.mybatis.entity.BaseDTO;

import javax.persistence.*;


/**
 * @author wuguokai
 * @author superlee
 */
@Table(name = "fd_organization")
public class OrganizationE extends BaseDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String code;

    @Column(name = "is_enabled")
    private Boolean enabled;

    private OrganizationE() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
