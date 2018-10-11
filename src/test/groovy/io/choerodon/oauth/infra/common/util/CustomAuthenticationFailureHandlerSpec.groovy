package io.choerodon.oauth.infra.common.util

import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.api.service.UserService
import io.choerodon.oauth.core.password.record.LoginRecord
import io.choerodon.oauth.domain.entity.UserE
import io.choerodon.oauth.infra.exception.CustomAuthenticationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockHttpSession
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class CustomAuthenticationFailureHandlerSpec extends Specification {
    @Autowired
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler
    private LoginRecord mockLoginRecord = Mock(LoginRecord)
    private UserService mockUserService = Mock(UserService)

    void setup() {
        customAuthenticationFailureHandler.setLoginRecord(mockLoginRecord)
        customAuthenticationFailureHandler.setUserService(mockUserService)
    }

    def "OnAuthenticationFailure"() {
        given: "参数准备"
        def request = Mock(HttpServletRequest)
        request.getParameter("username") >> { return "username" }
        request.getSession(false) >> { return new MockHttpSession() }
        def response = Mock(HttpServletResponse)
        def exception = Mock(CustomAuthenticationException)
        exception.getMessage() >> { return "usernameNotFoundOrPasswordIsWrong" }

        and: "mock"
        mockUserService.queryByLoginField(_) >> { return new UserE(id: 1L) }

        when: "方法调用"
        customAuthenticationFailureHandler.onAuthenticationFailure(request, response, exception)

        then: "无异常抛出"
        noExceptionThrown()
        1 * mockLoginRecord.loginError(_)
    }
}
