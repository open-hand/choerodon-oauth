package io.choerodon.oauth.infra.feign;

import javax.validation.Valid;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import io.choerodon.oauth.api.dto.EmailSendDTO;
import io.choerodon.oauth.api.dto.WsSendDTO;
import io.choerodon.oauth.infra.feign.fallback.NotifyFeignClientFallback;

/**
 * @author dongfan117@gmail.com
 */
@FeignClient(value = "notify-service", path = "/v1/notices", fallback = NotifyFeignClientFallback.class)
public interface NotifyFeignClient {

    @PostMapping("/emails")
    public void postEmail(@RequestBody EmailSendDTO dto);

    @PostMapping("/ws")
    void postPm(@RequestBody @Valid WsSendDTO dto);
}
