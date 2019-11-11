package io.choerodon.oauth.infra.dto;

import io.choerodon.mybatis.entity.BaseDTO;
import io.swagger.annotations.ApiModelProperty;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * FD_ROUTE_MEMBER_RULE DTO
 *
 * @author pengyuhua
 * @date 2019/10/25
 */
@Table(name = "fd_route_member_rule")
public class RouteMemberRuleDTO extends BaseDTO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "主键ID")
    private Long id;
    @ApiModelProperty(value = "用户ID/必填")
    private Long userId;
    @ApiModelProperty(value = "路由编码/必填")
    private String routeRuleCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRouteRuleCode() {
        return routeRuleCode;
    }

    public void setRouteRuleCode(String routeRuleCode) {
        this.routeRuleCode = routeRuleCode;
    }
}
