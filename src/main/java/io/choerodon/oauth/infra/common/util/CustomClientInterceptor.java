package io.choerodon.oauth.infra.common.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.oauth.infra.enums.AuthorizationTypeEnum;

/**
 * @author zongw.lee@gmail.com
 * @date 2019/10/18
 */
@Component
public class CustomClientInterceptor implements HandlerInterceptor {

    private static final String CLIENT_ID = "client_id";
    private static final String AUTHORIZATION_TYPE = "authorization_type";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authorizationType = request.getParameter(AUTHORIZATION_TYPE);
        // 不需要做普罗米修斯的客户端权限校验
        if (authorizationType.isEmpty()) {
            return true;
        }
        
        Long userId;
        String clientId = request.getParameter(CLIENT_ID);

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            userId = ((CustomUserDetails) principal).getUserId();
        } else {
            return false;
        }
        if (AuthorizationTypeEnum.PROMETHEUS_CLUSTER.value().equals(authorizationType)) {
            // todo: devops 校验用户是否拥有此客户端权限，如果校验不通过，返回原因
        }
        return true;
    }
}
