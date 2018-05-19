package io.choerodon.oauth.app.service;

import io.choerodon.oauth.infra.dataobject.NotifyToken;

/**
 * @author wuguokai
 */
public interface NotifyTokenService {
    NotifyToken checkToken(NotifyToken notifyToken);
}
