package io.choerodon.oauth.infra.repository.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.oauth.domain.oauth.entity.ClientE;
import io.choerodon.oauth.domain.repository.ClientRepository;
import io.choerodon.oauth.infra.dataobject.ClientDO;
import io.choerodon.oauth.infra.mapper.ClientMapper;

/**
 * @author wuguokai
 */
@Component
public class ClientRepositoryImpl implements ClientRepository {

    private ClientMapper clientMapper;

    public ClientRepositoryImpl(ClientMapper clientMapper) {
        this.clientMapper = clientMapper;
    }

    @Override
    public ClientE selectByName(String name) {
        ClientDO example = new ClientDO();
        example.setName(name);
        List<ClientDO> clientDOList = clientMapper.select(example);
        if (!clientDOList.isEmpty()) {
            return ConvertHelper.convert(clientDOList.get(0), ClientE.class);
        }
        return null;
    }
}
