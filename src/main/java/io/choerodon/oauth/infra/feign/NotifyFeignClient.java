package io.choerodon.oauth.infra.feign;


import io.choerodon.core.notify.NoticeSendDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.choerodon.oauth.infra.feign.fallback.NotifyFeignClientFallback;

/**
 * @author dongfan117@gmail.com
 */
@FeignClient(value = "notify-service", path = "/v1/notices", fallback = NotifyFeignClientFallback.class)
public interface NotifyFeignClient {

    @PostMapping
    void postNotice(@RequestBody NoticeSendDTO dto);
}
