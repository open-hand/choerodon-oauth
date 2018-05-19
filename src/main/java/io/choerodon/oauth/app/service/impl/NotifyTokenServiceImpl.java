package io.choerodon.oauth.app.service.impl;

import org.springframework.stereotype.Service;

import io.choerodon.oauth.app.service.NotifyTokenService;
import io.choerodon.oauth.infra.dataobject.NotifyToken;

/**
 * @author wuguokai
 */
@Service
public class NotifyTokenServiceImpl implements NotifyTokenService {
    @Override
    public NotifyToken checkToken(NotifyToken notifyToken) {
        return null;
    }
}
