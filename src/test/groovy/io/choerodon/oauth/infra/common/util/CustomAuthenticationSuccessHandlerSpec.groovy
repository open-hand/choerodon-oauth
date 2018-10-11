package io.choerodon.oauth.infra.common.util

import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.api.service.UserService
import io.choerodon.oauth.core.password.record.LoginRecord
import io.choerodon.oauth.domain.entity.UserE
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
class CustomAuthenticationSuccessHandlerSpec extends Specification {

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler
    private LoginRecord mockLoginRecord = Mock(LoginRecord)
    private UserService mockUserService = Mock(UserService)

    void setup() {
        customAuthenticationSuccessHandler.setLoginRecord(mockLoginRecord)
        customAuthenticationSuccessHandler.setUserService(mockUserService)
        customAuthenticationSuccessHandler.setUseSSL(true)
    }

    def "OnAuthenticationSuccess"() {
        given: "参数准备"
        def request = Mock(HttpServletRequest)
        request.getParameter("username") >> { return "username" }
        request.getSession(false) >> { return new MockHttpSession() }
        def response = Mock(HttpServletResponse)
        def authentication = Mock(Authentication)

        and: "mock"
        mockUserService.queryByLoginField(_) >> { return new UserE(id: 1L) }

        when: "方法调用"
        customAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication)

        then: "无异常抛出"
        noExceptionThrown()
        1 * mockLoginRecord.loginSuccess(_)
        1 * mockUserService.updateSelective(_)
    }
}
