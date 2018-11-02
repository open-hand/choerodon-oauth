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
}
