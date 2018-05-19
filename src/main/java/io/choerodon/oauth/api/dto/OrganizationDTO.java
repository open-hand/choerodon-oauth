package io.choerodon.oauth.api.dto;

import javax.validation.constraints.Size;

/**
 * @author wuguokai
 */
public class OrganizationDTO {
    private Long id;

    @Size(min = 1, max = 32, message = "error.organizationName.size")
    private String name;

    private Long objectVersionNumber;

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

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
