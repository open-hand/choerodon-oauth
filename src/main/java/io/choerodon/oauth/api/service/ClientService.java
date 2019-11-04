package io.choerodon.oauth.api.service;

import io.choerodon.oauth.domain.entity.ClientE;

/**
 * @author wanghao
 * @Date 2019/11/1 15:33
 */
public interface ClientService {

    ClientE getClientByName(String name);
}
