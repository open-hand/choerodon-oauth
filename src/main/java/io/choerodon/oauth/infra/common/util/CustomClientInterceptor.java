package io.choerodon.oauth.infra.common.util;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;
import io.choerodon.oauth.api.service.ClientService;
import io.choerodon.oauth.domain.entity.ClientE;
import io.choerodon.oauth.infra.enums.ClientTypeEnum;
import io.choerodon.oauth.infra.feign.DevopsFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zongw.lee@gmail.com
 * @date 2019/10/18
 */
@Component
public class CustomClientInterceptor implements HandlerInterceptor {

    private static final String CLIENT_ID = "client_id";
    private static final String AUTHORIZE = "/**/oauth/authorize";
    private static final String CODE = "code";
    private static final String RESPONSE_TYPE = "response_type";
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomClientInterceptor.class);
    private ClientService clientService;
    private DevopsFeignClient devopsFeignClient;
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    public CustomClientInterceptor(ClientService clientService, DevopsFeignClient devopsFeignClient) {
        this.clientService = clientService;
        this.devopsFeignClient = devopsFeignClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(antPathMatcher.match(AUTHORIZE,request.getRequestURI()) && CODE.equals(request.getParameter(RESPONSE_TYPE)))) {
            return true;
        }
        Long userId;
        String clientId = request.getParameter(CLIENT_ID);
        ClientE client = clientService.getClientByName(clientId);
        LOGGER.info("start to handle client:, clientId:{}", clientId);
        if (client == null) {
            throw new NoSuchClientException("No client found : " + clientId);
        }
        // 不需要做普罗米修斯的客户端权限校验
        if (!ClientTypeEnum.CLUSTER.value().equals(client.getSourceType())) {
            return true;
        }

        CustomUserDetails customUserDetails = DetailsHelper.getUserDetails();
        if (customUserDetails == null || customUserDetails.getUserId() == null) {
            LOGGER.info("=========不能拿到userId");
            throw new AccessDeniedException("未登录");
        }
        userId = customUserDetails.getUserId();

        LOGGER.info("start to check user's cluster permission: userId:{}", userId);
        // 调用devops接口校验用户是否有访问集群的权限
        Boolean result = devopsFeignClient.checkUserClusterPermission(client.getSourceId(), userId).getBody();
        if (Boolean.FALSE.equals(result)) {
            throw new AccessDeniedException("权限不足");
        }
        return true;
    }
}
