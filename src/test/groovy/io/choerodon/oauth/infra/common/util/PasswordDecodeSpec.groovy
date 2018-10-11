package io.choerodon.oauth.infra.common.util

import io.choerodon.oauth.IntegrationTestConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class PasswordDecodeSpec extends Specification {

    def "Decode"() {
        given: "参数准备"
        def str = "nBHqz7VGB5ze="
        when: "方法调用"
        PasswordDecode.decode(str)
        then: "结果分析"
        noExceptionThrown()
    }
}
