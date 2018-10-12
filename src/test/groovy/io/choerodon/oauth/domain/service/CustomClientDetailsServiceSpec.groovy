package io.choerodon.oauth.domain.service

import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.domain.entity.ClientE
import io.choerodon.oauth.infra.mapper.ClientMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.provider.NoSuchClientException
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class CustomClientDetailsServiceSpec extends Specification {
    @Autowired
    private CustomClientDetailsService customClientDetailsService
    private ClientMapper mockClientMapper = Mock(ClientMapper)

    void setup() {
        customClientDetailsService.setClientMapper(mockClientMapper)
    }

    def "LoadClientByClientId"() {
        given: "参数准备"
        def name = "name"
        and: "mock"
        mockClientMapper.selectOne(_) >> {
            return new ClientE(authorizedGrantTypes: "type1,type2", name: "name", secret: "secret", resourceIds: "1,2",
                    scope: "scope1,scope2", webServerRedirectUri: "uri1,uri2",
                    accessTokenValidity: null, refreshTokenValidity: null,
                    additionalInformation: '{"name":"value"}', autoApprove: "approve1,approve2")
        }
        when: "方法调用"
        customClientDetailsService.loadClientByClientId(name)
        then: "结果分析"
        noExceptionThrown()
    }
    def "LoadClientByClientId[parser addition info error]"() {
        given: "参数准备"
        def name = "name"
        and: "mock"
        mockClientMapper.selectOne(_) >> {
            return new ClientE(authorizedGrantTypes: "type1,type2", name: "name", secret: "secret", resourceIds: "1,2",
                    scope: "scope1,scope2", webServerRedirectUri: "uri1,uri2",
                    accessTokenValidity: null, refreshTokenValidity: null,
                    additionalInformation: '{"namevalue"}', autoApprove: "approve1,approve2")
        }
        when: "方法调用"
        customClientDetailsService.loadClientByClientId(name)
        then: "结果分析"
        noExceptionThrown()
    }

    def "LoadClientByClientId[Exception]"() {
        given: "参数准备"
        def name = "name"
        and: "mock"
        mockClientMapper.selectOne(_) >> { return null }
        when: "方法调用"
        customClientDetailsService.loadClientByClientId(name)
        then: "抛出异常"
        def e = thrown(NoSuchClientException)
        e.message == "No client found : " + name
    }
}
