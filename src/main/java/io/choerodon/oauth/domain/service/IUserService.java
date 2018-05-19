package io.choerodon.oauth.domain.service;

import io.choerodon.mybatis.service.BaseService;
import io.choerodon.oauth.infra.dataobject.UserDO;

/**
 * @author wuguokai
 */
public interface IUserService extends BaseService<UserDO> {
    UserDO findUser(String field);

    UserDO findByLoginName(String loginName);
}
