package io.choerodon.oauth.domain.repository;

import io.choerodon.oauth.domain.oauth.entity.ClientE;

/**
 * Created by WUGUOKAI on 2018/3/26.
 */
public interface ClientRepository {
    ClientE selectByName(String name);
}
