package io.choerodon.oauth.infra.feign;

import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "devops-service")
public interface DevopsFeignClient {
    @GetMapping(value = "/v1/checks/clusterCheck")
    ResponseEntity<Boolean> checkUserClusterPermission(
            @ApiParam(value = "集群ID", required = true)
            @RequestParam(value = "cluster_id") Long clusterId,
            @ApiParam(value = "用户ID", required = true)
            @RequestParam(value = "user_id") Long userId);
}
