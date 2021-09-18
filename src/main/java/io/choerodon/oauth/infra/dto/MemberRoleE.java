package io.choerodon.oauth.infra.dto;

import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hzero.starter.keyencrypt.core.Encrypt;

import io.choerodon.mybatis.domain.AuditDomain;

/**
 * Created by wangxiang on 2021/8/2
 */
@Table(name = "iam_member_role")
public class MemberRoleE extends AuditDomain {

    @Id
    @GeneratedValue
    @Encrypt
    private Long id;
    @ApiModelProperty("角色ID，传入角色ID或角色编码")
    @Encrypt
    private Long roleId;
    @ApiModelProperty("成员ID")
    @Encrypt
    private Long memberId;
    @ApiModelProperty("成员类型，用户-user/客户端-client")
    private String memberType;
    private Long sourceId;
    private String sourceType;
    @Column(
            name = "h_assign_level"
    )
    @ApiModelProperty("分配层级，租户层-organization/组织层-org")
    private String assignLevel;
    @ApiModelProperty("分配层级值，租户层-角色所属租户ID/组织层-组织ID")
    @Column(
            name = "h_assign_level_value"
    )
    private Long assignLevelValue;
    @ApiModelProperty("有效期起")
    private LocalDate startDateActive;
    @ApiModelProperty("有效期止")
    private LocalDate endDateActive;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberType() {
        return memberType;
    }

    public void setMemberType(String memberType) {
        this.memberType = memberType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getAssignLevel() {
        return assignLevel;
    }

    public void setAssignLevel(String assignLevel) {
        this.assignLevel = assignLevel;
    }

    public Long getAssignLevelValue() {
        return assignLevelValue;
    }

    public void setAssignLevelValue(Long assignLevelValue) {
        this.assignLevelValue = assignLevelValue;
    }

    public LocalDate getStartDateActive() {
        return startDateActive;
    }

    public void setStartDateActive(LocalDate startDateActive) {
        this.startDateActive = startDateActive;
    }

    public LocalDate getEndDateActive() {
        return endDateActive;
    }

    public void setEndDateActive(LocalDate endDateActive) {
        this.endDateActive = endDateActive;
    }
}
