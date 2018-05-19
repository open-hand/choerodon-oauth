package io.choerodon.oauth.infra.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.choerodon.oauth.infra.dataobject.NotifyToken;

/**
 * @author wuguokai
 */
@FeignClient(value = "notification-service"/*, fallback = FeignMailServiceImpl.class*/)
public interface NotificationFeign {
    /**
     * 创建notifyToken
     *
     * @param source     source
     * @param sourceType sourceType
     * @return 验证码
     */
    @RequestMapping(value = "/v1/notify_token/reset_password", method = RequestMethod.POST)
    ResponseEntity<NotifyToken> createNotifyToken(@RequestParam("source") String source, @RequestParam("sourceType") String sourceType);

    /**
     * 查询notifyToken
     *
     * @return
     */
    /**
     * 查询notifyToken
     *
     * @param notifyTokenId notifyTokenId
     * @return 验证码
     */
    @RequestMapping(value = "/v1/notify_token/{id}", method = RequestMethod.GET)
    ResponseEntity<NotifyToken> findNotifyToken(@PathVariable("id") Long notifyTokenId);

    /**
     * 删除notifyToken
     *
     * @param notifyTokenId notifyTokenId
     * @return 是否成功
     */
    @RequestMapping(value = "/v1/notify_token/{id}", method = RequestMethod.DELETE)
    ResponseEntity<Boolean> deleteNotifyToken(@PathVariable("id") Long notifyTokenId);
}
