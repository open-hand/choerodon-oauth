package io.choerodon.oauth.api.service.impl;

import io.choerodon.oauth.api.service.SystemSettingService;
import io.choerodon.oauth.infra.dataobject.SystemSettingDO;
import io.choerodon.oauth.infra.mapper.SystemSettingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author zmf
 * @since 2018-10-15
 */
@Service
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingMapper systemSettingMapper;

    @Autowired
    public SystemSettingServiceImpl(SystemSettingMapper systemSettingMapper) {
        this.systemSettingMapper = systemSettingMapper;
    }

    @Override
    public SystemSettingDO getSetting() {
        List<SystemSettingDO> records = systemSettingMapper.selectAll();
        return records.size() == 0 ? null : records.get(0);
    }
}
