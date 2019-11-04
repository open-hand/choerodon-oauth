package io.choerodon.oauth.api.service.impl;

import io.choerodon.oauth.api.service.ClientService;
import io.choerodon.oauth.domain.entity.ClientE;
import io.choerodon.oauth.infra.mapper.ClientMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author wanghao
 * @Date 2019/11/1 15:33
 */
@Service
public class ClientServiceImpl implements ClientService {

    private ClientMapper clientMapper;

    public ClientServiceImpl(ClientMapper clientMapper) {
        this.clientMapper = clientMapper;
    }


    @Override
    public ClientE getClientByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        ClientE record = new ClientE();
        record.setName(name);
        return clientMapper.selectOne(record);
    }
}
