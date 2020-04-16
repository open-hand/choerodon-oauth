package io.choerodon.oauth.app.service;

import io.choerodon.oauth.api.vo.SysSettingVO;

/**
 * 查询系统设置
 *
 * @author zmf
 * @since 2018-10-15
 */
public interface SystemSettingService {
    /**
     * 获取系统设置
     *
     * @return ，如果存在返回数据，否则返回 null
     */
    SysSettingVO getSetting();
}
