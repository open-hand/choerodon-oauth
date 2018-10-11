package io.choerodon.oauth.infra.common.util

import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.api.service.UserService
import io.choerodon.oauth.domain.entity.UserE
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class CustomUserDetailsServiceImplSpec extends Specification {
    @Autowired
    private CustomUserDetailsServiceImpl customUserDetailsService
    private UserService mockUserService = Mock(UserService)

    void setup() {
        customUserDetailsService.setUserService(mockUserService)
    }

    def "LoadUserByUsername"() {
        given: "参数准备"
        def username = ""
        def userE = new UserE(loginName: "loginName",password: "password",id: 1L,language: "ZH",timeZone: "tz",email: "269975182@qq.com",organizationId: 1L,admin: false)
        and: "mock"
        mockUserService.queryByLoginField(username) >> { return userE }
        when: "方法调用"
        def details = customUserDetailsService.loadUserByUsername(username)
        then: "无异常抛出"
        noExceptionThrown()
        details.getPassword().equals(userE.getPassword())
        details.getUserId().equals(userE.getId())
        details.getLanguage().equals(userE.getLanguage())
        details.getTimeZone().equals(userE.getTimeZone())
        details.getEmail().equals(userE.getEmail())
        details.getOrganizationId().equals(userE.getOrganizationId())
        details.getAdmin().equals(userE.getAdmin())

    }
}
