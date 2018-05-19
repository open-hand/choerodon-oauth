package io.choerodon.oauth.domain.service;

import io.choerodon.mybatis.service.BaseService;
import io.choerodon.oauth.infra.dataobject.AccessTokenDO;

/**
 * Created by jiatong.li on 3/18/17.
 */
public interface TokenService extends BaseService<AccessTokenDO> {

    AccessTokenDO create(String additionalInfo);
}
