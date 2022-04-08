package org.hzero.boot.oauth.domain.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;

/**
 * 三方网站管理
 * 当时参考的yqcloud 覆盖表 其实c7n没有必要重新设计表覆盖
 * @author wuguokai
 */
@ApiModel("三方网站")
@VersionAudit
@ModifyAudit
@Table(name = "oauth_open_app")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseOpenApp extends AuditDomain {

    public static final String FIELD_ID = "id";

    @ApiModelProperty("表Id")
    @Id
    @GeneratedValue
    @Encrypt
    private Long id;
    @ApiModelProperty("应用类型")
    @NotBlank
    private String type;
    @ApiModelProperty("租户Id")
    private Long tenantId;
    @ApiModelProperty("第三方平台方appid")
    @NotBlank
    private String appId;
    @ApiModelProperty("appid对应的秘钥")
    @NotBlank
    private String appSecret;
    @ApiModelProperty("是否启用")
    private Boolean enabledFlag;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public Boolean getEnabledFlag() {
        return enabledFlag;
    }

    public void setEnabledFlag(Boolean enabledFlag) {
        this.enabledFlag = enabledFlag;
    }
}
