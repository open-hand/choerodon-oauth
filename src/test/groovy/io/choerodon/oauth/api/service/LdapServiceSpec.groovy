package io.choerodon.oauth.api.service

import io.choerodon.core.exception.CommonException
import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.api.service.impl.LdapServiceImpl
import io.choerodon.oauth.domain.entity.LdapE
import io.choerodon.oauth.infra.mapper.LdapMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class LdapServiceSpec extends Specification {
    @Autowired
    private LdapService ldapService

    private LdapMapper mockLdapMapper = Mock(LdapMapper)

    void setup() {
        ldapService = new LdapServiceImpl()
        ldapService.setLdapMapper(mockLdapMapper)
    }

    def "QueryByOrgId"() {
        given: "参数准备"
        def orgId = 1

        and: "mock"
        mockLdapMapper.selectOne(_) >> { return new LdapE(organizationId: orgId) }

        when: "方法调用"
        def ldap = ldapService.queryByOrgId(orgId)
        then: "结果比对"
        noExceptionThrown()
        ldap.getOrganizationId() == orgId

    }

    def "QueryByOrgId[Exception]"() {
        given: "参数准备"
        def orgId = 1
        and: "mock"
        mockLdapMapper.selectOne(_) >> { return null }

        when: "方法调用"
        ldapService.queryByOrgId(orgId)

        then: "抛出异常"
        def e = thrown(CommonException)
        e.message == "error.ldap.not.exist"
    }
}
