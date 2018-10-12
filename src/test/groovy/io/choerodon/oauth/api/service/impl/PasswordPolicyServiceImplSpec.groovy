package io.choerodon.oauth.api.service.impl

import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.api.service.PasswordPolicyService
import io.choerodon.oauth.infra.dataobject.PasswordPolicyDO
import io.choerodon.oauth.infra.mapper.PasswordPolicyMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class PasswordPolicyServiceImplSpec extends Specification {
    @Autowired
    private PasswordPolicyServiceImpl passwordPolicyService
    private PasswordPolicyMapper mockPasswordPolicyMapper=Mock(PasswordPolicyMapper)
    void setup() {
        passwordPolicyService.setPasswordPolicyMapper(mockPasswordPolicyMapper)
    }

    def "QueryByOrgId"() {
        given:"参数准备"
        def orgId=1L
        when:"方法调用"
        def id = passwordPolicyService.queryByOrgId(orgId)
        then:"无异常抛出"
        noExceptionThrown()
    }
}
