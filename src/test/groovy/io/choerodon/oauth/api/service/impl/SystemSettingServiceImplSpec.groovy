package io.choerodon.oauth.api.service.impl


import io.choerodon.oauth.infra.dto.SysSettingDTO
import io.choerodon.oauth.infra.enums.SysSettingEnum
import io.choerodon.oauth.infra.mapper.SysSettingMapper
import spock.lang.Specification

/**
 *
 * @author zmf
 */
class SystemSettingServiceImplSpec extends Specification {

    def "GetSetting"() {
        given: "准备上下文"
        SysSettingMapper mapper = Mock(SysSettingMapper)
        List<SysSettingDTO> list = new ArrayList<>()
        SysSettingDTO record = new SysSettingDTO()
        record.settingKey = SysSettingEnum.SYSTEM_TITLE.value()
        record.settingValue = "test"
        list.add(record)
        mapper.selectAll() >> { return list }
        SystemSettingServiceImpl service = new SystemSettingServiceImpl(mapper)

        when: "调用方法"
        def value = service.getSetting()

        then: "校验结果"
        value.getSystemTitle() == "test"
    }
}
