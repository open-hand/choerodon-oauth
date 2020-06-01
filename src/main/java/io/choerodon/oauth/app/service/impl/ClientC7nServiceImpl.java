package io.choerodon.oauth.app.service.impl;

import org.springframework.stereotype.Service;

import io.choerodon.oauth.app.service.ClientC7nService;
import io.choerodon.oauth.infra.dto.ClientE;
import io.choerodon.oauth.infra.mapper.ClientC7nMapper;

/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/6/1 14:47
 */
@Service
public class ClientC7nServiceImpl implements ClientC7nService {

    private ClientC7nMapper clientC7nMapper;

    public ClientC7nServiceImpl(ClientC7nMapper clientC7nMapper) {
        this.clientC7nMapper = clientC7nMapper;
    }

    @Override
    public ClientE getClientByName(String clientId) {

        return clientC7nMapper.getClientByName(clientId);
    }
}
