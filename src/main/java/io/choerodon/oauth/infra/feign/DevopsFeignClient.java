package io.choerodon.oauth.infra.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "devops-service")
public interface DevopsFeignClient {

}
