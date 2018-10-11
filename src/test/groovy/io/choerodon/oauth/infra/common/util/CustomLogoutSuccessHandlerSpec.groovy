package io.choerodon.oauth.infra.common.util

import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.infra.config.OauthProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.core.Authentication
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class CustomLogoutSuccessHandlerSpec extends Specification {

    @Autowired
    private CustomLogoutSuccessHandler customLogoutSuccessHandler
    private OauthProperties mockOauthProperties = Mock(OauthProperties)
    private CustomTokenStore mockCustomTokenStore = Mock(CustomTokenStore)

    void setup() {
        customLogoutSuccessHandler.setOauthProperties(mockOauthProperties)
        customLogoutSuccessHandler.setCustomTokenStore(mockCustomTokenStore)
    }

    def "OnLogoutSuccess"() {
        given: "参数准备"
        def request = Mock(HttpServletRequest)
        request.getSession() >> {return new MockHttpSession()}
        request.getHeader("Authorization") >> { return header }
        request.getHeader("Referer") >> { return referer }
        def response = Mock(HttpServletResponse)
        def authentication = Mock(Authentication)

        and: "mock"
        mockOauthProperties.isClearToken() >> { return true }

        when: "方法调用"
        customLogoutSuccessHandler.onLogoutSuccess(request, response, authentication)

        then: "无异常抛出"
        noExceptionThrown()
        num1 * mockCustomTokenStore.removeAccessToken(_)
        num2 * mockCustomTokenStore.removeRefreshToken(_)

        where: "分支覆盖"
        header                                  | referer || num1 | num2
        null                                    | null    || 0    | 0
        "Bearer f9d4f24f-d258-4354-bf19-e985c4" | ""      || 1    | 1
    }

    def "ExtractHeaderToken"() {
    }
}
