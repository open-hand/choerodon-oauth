package io.choerodon.oauth.infra.feign.fallback;

import io.choerodon.core.exception.CommonException;
import io.choerodon.oauth.api.dto.EmailSendDTO;
import io.choerodon.oauth.infra.feign.NotifyFeignClient;

/**
 * @author dongfan117@gmail.com
 */
public class NotifyFeignClientFallback implements NotifyFeignClient {
    private static final String FEIGN_ERROR = "notify.error";

    @Override
    public void postEmail(EmailSendDTO dto) {
        throw new CommonException(FEIGN_ERROR);
    }
}
