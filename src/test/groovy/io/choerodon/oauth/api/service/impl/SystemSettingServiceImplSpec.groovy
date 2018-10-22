package io.choerodon.oauth.api.service.impl


import io.choerodon.oauth.infra.dataobject.SystemSettingDO
import io.choerodon.oauth.infra.mapper.SystemSettingMapper
import spock.lang.Specification

/**
 *
 * @author zmf
 *
 */
//@SpringBootTest(webEnvironment = RANDOM_PORT)
//@Import(IntegrationTestConfiguration)
class SystemSettingServiceImplSpec extends Specification {
//    @Autowired
//    private SystemSettingServiceImpl service

    def "GetSetting"() {
        given: "准备上下文"
        SystemSettingMapper mapper = Mock(SystemSettingMapper)
        List<SystemSettingDO> list = new ArrayList<>()
        SystemSettingDO record = new SystemSettingDO()
        record.setId(1L)
        list.add(record)
        mapper.selectAll() >> { return list }
        SystemSettingServiceImpl service = new SystemSettingServiceImpl(mapper)

        when: "调用方法"
        def value = service.getSetting()

        then: "校验结果"
        value.getId() == 1L
    }
}
