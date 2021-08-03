package io.choerodon.oauth.infra.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import io.choerodon.mybatis.annotation.MultiLanguageField;

/**
 * Created by wangxiang on 2021/8/2
 */
@Table(name = "hpfm_tenant")
public class TenantE {
    @Id
    @GeneratedValue
    @ApiModelProperty("租户ID")
    private Long tenantId;
    @NotBlank
    @Length(
            max = 120
    )
    @MultiLanguageField
    @ApiModelProperty("租户名称")
    private String tenantName;
    @NotBlank
    @Length(
            max = 15
    )
    @ApiModelProperty("租户编号")
    @Pattern(
            regexp = "^[a-zA-Z0-9][a-zA-Z0-9-_./]*$"
    )
    private String tenantNum;
    @NotNull
    @Range(
            max = 1L,
            min = 0L
    )
    @ApiModelProperty("是否启用")
    private Integer enabledFlag;
    @ApiModelProperty("限制用户数")
    private Integer limitUserQty;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getTenantNum() {
        return tenantNum;
    }

    public void setTenantNum(String tenantNum) {
        this.tenantNum = tenantNum;
    }

    public Integer getEnabledFlag() {
        return enabledFlag;
    }

    public void setEnabledFlag(Integer enabledFlag) {
        this.enabledFlag = enabledFlag;
    }

    public Integer getLimitUserQty() {
        return limitUserQty;
    }

    public void setLimitUserQty(Integer limitUserQty) {
        this.limitUserQty = limitUserQty;
    }
}
