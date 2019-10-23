package io.choerodon.oauth.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "devops-service")
public interface DevopsFeignClient {
    //TODO 待devops提供接口
    @GetMapping(value = "/test_for_oauth")
    ResponseEntity<Boolean> testForOauth(
            @RequestParam(value = "user_id") Long userId,
            @RequestParam(value = "client_id") Long clientId);

}
