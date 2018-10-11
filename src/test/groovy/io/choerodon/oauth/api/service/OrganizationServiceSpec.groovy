package io.choerodon.oauth.api.service

import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.api.service.impl.OrganizationServiceImpl
import io.choerodon.oauth.infra.mapper.OrganizationMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class OrganizationServiceSpec extends Specification {
    @Autowired
    private OrganizationService organizationService

    private OrganizationMapper mockOrganizationMapper = Mock(OrganizationMapper)

    void setup() {
        organizationService = new OrganizationServiceImpl(mockOrganizationMapper)
    }

    def "QueryOrganizationById"() {
        given: "参数准备"
        def organizationId = 1
        when: "方法调用"
        organizationService.queryOrganizationById(organizationId)
        then: "结果分析"
        noExceptionThrown()
        1 * mockOrganizationMapper.selectByPrimaryKey(organizationId)
    }
}
