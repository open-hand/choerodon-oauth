package io.choerodon.oauth.infra.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import io.choerodon.oauth.api.vo.SysSettingVO;
import io.choerodon.oauth.infra.dto.SysSettingDTO;
import io.choerodon.oauth.infra.enums.SysSettingEnum;

public class SysSettingUtils {

    private SysSettingUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 系统配置列表转为系统配置Map
     *
     * @param settingDTOS 系统配置列表
     * @return 返回null或者系统配置Map
     */
    private static Map<String, String> listToMap(List<SysSettingDTO> settingDTOS) {
        if (CollectionUtils.isEmpty(settingDTOS)) {
            return null;
        }
        Map<String, String> settingDTOMap = new HashMap<>();
        settingDTOS.forEach(settingDTO -> settingDTOMap.put(settingDTO.getSettingKey(), settingDTO.getSettingValue()));
        return settingDTOMap;
    }

    /**
     * 系统配置列表转为系统配置VO
     *
     * @param settingDTOS 系统配置列表
     * @return 返回null或者系统配置VO
     */
    public static SysSettingVO listToSysSettingVo(List<SysSettingDTO> settingDTOS) {
        Map<String, String> settingDTOMap = listToMap(settingDTOS);
        if (ObjectUtils.isEmpty(settingDTOMap)) {
            return null;
        }
        // 基本信息
        SysSettingVO sysSettingVO = new SysSettingVO();
        sysSettingVO.setFavicon(settingDTOMap.get(SysSettingEnum.FAVICON.value()));
        sysSettingVO.setSystemLogo(settingDTOMap.get(SysSettingEnum.SYSTEM_LOGO.value()));
        sysSettingVO.setSystemTitle(settingDTOMap.get(SysSettingEnum.SYSTEM_TITLE.value()));
        sysSettingVO.setSystemName(settingDTOMap.get(SysSettingEnum.SYSTEM_NAME.value()));
        sysSettingVO.setDefaultLanguage(settingDTOMap.get(SysSettingEnum.DEFAULT_LANGUAGE.value()));
        sysSettingVO.setRegisterUrl(settingDTOMap.get(SysSettingEnum.REGISTER_URL.value()));
        sysSettingVO.setResetGitlabPasswordUrl(settingDTOMap.get(SysSettingEnum.RESET_GITLAB_PASSWORD_URL.value()));
        sysSettingVO.setThemeColor(settingDTOMap.get(SysSettingEnum.THEME_COLOR.value()));
        String registerEnabled = settingDTOMap.get(SysSettingEnum.REGISTER_ENABLED.value());
        if (!ObjectUtils.isEmpty(registerEnabled)) {
            sysSettingVO.setRegisterEnabled(Boolean.valueOf(registerEnabled));
        }
        // 密码策略
        sysSettingVO.setDefaultPassword(settingDTOMap.get(SysSettingEnum.DEFAULT_PASSWORD.value()));
        String minPwd = settingDTOMap.get(SysSettingEnum.MIN_PASSWORD_LENGTH.value());
        String maxPwd = settingDTOMap.get(SysSettingEnum.MAX_PASSWORD_LENGTH.value());
        if (!ObjectUtils.isEmpty(minPwd)) {
            sysSettingVO.setMinPasswordLength(Integer.valueOf(minPwd));
        }
        if (!ObjectUtils.isEmpty(maxPwd)) {
            sysSettingVO.setMaxPasswordLength(Integer.valueOf(maxPwd));
        }
        return sysSettingVO;
    }
}
