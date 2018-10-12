package io.choerodon.oauth.infra.exception

import io.choerodon.oauth.IntegrationTestConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class CustomAuthenticationExceptionSpec extends Specification {
    def "GetParameters"() {
        when: "构造异常"
        def object = "object"
        def customAuthenticationException = new CustomAuthenticationException("msg", Mock(Throwable), object)
        def parameters = customAuthenticationException.getParameters()
        then: "结果校验"
        parameters != null
        parameters[0] == object
    }
}
