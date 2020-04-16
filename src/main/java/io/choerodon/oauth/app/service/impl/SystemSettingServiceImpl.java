package io.choerodon.oauth.app.service.impl;

import io.choerodon.oauth.api.vo.SysSettingVO;
import io.choerodon.oauth.app.service.SystemSettingService;
import io.choerodon.oauth.infra.mapper.SysSettingMapper;
import io.choerodon.oauth.infra.util.SysSettingUtils;
import org.springframework.stereotype.Service;


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
