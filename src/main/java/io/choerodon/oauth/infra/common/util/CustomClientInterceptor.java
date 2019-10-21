package io.choerodon.oauth.infra.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.oauth.domain.entity.ClientE;
import io.choerodon.oauth.infra.enums.ClientTypeEnum;
import io.choerodon.oauth.infra.feign.DevopsFeignClient;
import io.choerodon.oauth.infra.mapper.ClientMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zongw.lee@gmail.com
 * @date 2019/10/18
 */
@Component
public class CustomClientInterceptor implements HandlerInterceptor {

    private static final String CLIENT_ID = "client_id";
    private ObjectMapper mapper = new ObjectMapper();
    private ClientMapper clientMapper;
    private DevopsFeignClient devopsFeignClient;

    public CustomClientInterceptor(ClientMapper clientMapper, DevopsFeignClient devopsFeignClient) {
        this.clientMapper = clientMapper;
        this.devopsFeignClient = devopsFeignClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long userId;
        String clientId = request.getParameter(CLIENT_ID);
        // 不需要做普罗米修斯的客户端权限校验
        if (!isClusterClient(clientId)) {
            return true;
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            userId = ((CustomUserDetails) principal).getUserId();
        } else {
            return false;
        }
        // 调用devops接口校验用户是否有访问集群的权限
        // TODO 待处理
        boolean res = devopsFeignClient.testForOauth(userId,1L).getBody();
        if (!res) {
            Map<String,String> map = new HashMap<>();
            map.put("code","403");
            map.put("message","权限不足");
            response.setCharacterEncoding("utf-8");
            response.getWriter().print(mapper.writeValueAsString(map));
        }
        return res;
    }
    private boolean isClusterClient(String clientName) {
        ClientE clientE = new ClientE();
        clientE.setName(clientName);
        clientE = clientMapper.selectOne(clientE);
        return ClientTypeEnum.CLUSTER.value().equals(clientE.getType());
    }
}
