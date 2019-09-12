package io.choerodon.oauth.api.service.impl;

import org.springframework.stereotype.Service;

import io.choerodon.oauth.api.service.SystemSettingService;
import io.choerodon.oauth.api.vo.SysSettingVO;
import io.choerodon.oauth.infra.mapper.SysSettingMapper;
import io.choerodon.oauth.infra.util.SysSettingUtils;


/**
 * @author zmf
 * @since 2018-10-15
 */
@Service
public class SystemSettingServiceImpl implements SystemSettingService {

    private SysSettingMapper sysSettingMapper;

    public SystemSettingServiceImpl(SysSettingMapper sysSettingMapper) {
        this.sysSettingMapper = sysSettingMapper;
    }

    @Override
    public SysSettingVO getSetting() {
        return SysSettingUtils.listToSysSettingVo(sysSettingMapper.selectAll());
    }
}
