package io.choerodon.oauth.api.dto

import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.infra.dataobject.PasswordPolicyDO
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class AllDTOSpec extends Specification {
    def "CaptchaCheckDTO"() {
        given: "参数准备"
        def pwdPolicyDO = new PasswordPolicyDO()
        when: 'set方法覆盖'
        def dto = new CaptchaCheckDTO()
        dto.setCode("code")
        dto.setDisableTime(new Date().getTime())
        dto.setMsg("msg")
        dto.setPasswordPolicyDO(pwdPolicyDO)
        dto.setUser(new UserDTO(1L, "loginName", "email @test.com"))
        dto.setSuccess(false)
        then: 'get方法覆盖'
        noExceptionThrown()
        dto.getSuccess() == false
    }

    def "EmailSendDTO"() {
        when: "创建DTO"
        def o = new EmailSendDTO("code", "destinatiomEmail@qq.com", new HashMap<String, Object>())
        then: "get方法覆盖"
        o.getCode() == "code"
        o.getDestinationEmail() == "destinatiomEmail@qq.com"
        o.getVariables().isEmpty()
        noExceptionThrown()
    }

    def "WsSendDTO"() {
        when: "创建DTO"
        def o = new WsSendDTO()
        o.setCode("code")
        o.setParams(new HashMap<String, Object>())
        o.setId(1L)
        o.setTemplateCode("templateCode")
        then: "get方法覆盖"
        o.getCode() == "code"
        o.getId() == 1L
        o.getParams().isEmpty()
        o.getTemplateCode() == "templateCode"
    }
}
