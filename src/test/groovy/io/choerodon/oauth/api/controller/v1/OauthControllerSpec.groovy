package io.choerodon.oauth.api.controller.v1

import com.google.code.kaptcha.impl.DefaultKaptcha
import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.api.service.SystemSettingService
import io.choerodon.oauth.api.service.UserService
import io.choerodon.oauth.core.password.PasswordPolicyManager
import io.choerodon.oauth.core.password.domain.BasePasswordPolicyDO
import io.choerodon.oauth.core.password.mapper.BasePasswordPolicyMapper
import io.choerodon.oauth.domain.entity.UserE
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Import
import org.springframework.ui.Model
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession
import java.security.Principal

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class OauthControllerSpec extends Specification {
    @Autowired
    TestRestTemplate testRestTemplate
    @Autowired
    private OauthController oauthController
    private MessageSource mockMessageSource = Mock(MessageSource)
    private DefaultKaptcha mockCaptchaProducer = Mock(DefaultKaptcha)
    private BasePasswordPolicyMapper mockBasePasswordPolicyMapper = Mock(BasePasswordPolicyMapper)
    private PasswordPolicyManager mockPasswordPolicyManager = Mock(PasswordPolicyManager)
    private UserService mockUserService = Mock(UserService)

    @Autowired
    private SystemSettingService systemSettingService

    void setup() {
        oauthController.setMessageSource(mockMessageSource)
        oauthController.setPasswordPolicyManager(mockPasswordPolicyManager)
        oauthController.setBasePasswordPolicyMapper(mockBasePasswordPolicyMapper)
        oauthController.setCaptchaProducer(mockCaptchaProducer)
        oauthController.setUserService(mockUserService)
        oauthController.setLoginProfile("undefined")
    }

    def "Index"() {
        when: '发送请求'
        def entity = testRestTemplate.getForEntity(
                "/", String)
        then: '结果分析'
        entity.statusCode.is3xxRedirection()
        noExceptionThrown()
    }

    def "Login"() {
        given: "参数准备"
        def device = "device"
        when: '发送请求'
        def entity = testRestTemplate.getForEntity(
                "/login?device={device}", String, device)
        then: '结果分析'
        entity.statusCode.is2xxSuccessful()
        noExceptionThrown()
        1 * systemSettingService.getSetting() >> { null }
    }

    def "Login-2"() {
        given: "参数准备"
        def device = "mobile"
        def request = Mock(HttpServletRequest)
        def model = Mock(Model)
        def session = Mock(HttpSession)
        and: "mock"
        session.getAttribute("username") >> { return "userName" }
        session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION") >> { return "error1" }
        mockUserService.queryByLoginField(_) >> { return user }
        mockBasePasswordPolicyMapper.findByOrgId(_) >> { return Mock(BasePasswordPolicyDO) }
        mockPasswordPolicyManager.isNeedCaptcha(_, _) >> { return true }
        when: '方法调用'
        def login = oauthController.login(request, model, session, device)
        then: '结果分析'
        noExceptionThrown()
        login == returnPage
        1 * systemSettingService.getSetting() >> { null }
        where: "比对"
        user                                                 || returnPage
        null                                                 || "index-mobile"
        new UserE(locked: false, organizationId: 1L, id: 1L) || "index-mobile"
    }

    def "CreateCaptcha"() {
        when: '发送请求'
        testRestTemplate.getForEntity(
                "/public/captcha", Object)
        then: '结果分析'
        noExceptionThrown()
    }

    def "User"() {
        when: '发送请求'
        testRestTemplate.getForEntity(
                "/api/user", Object)
        then: '结果分析'
        noExceptionThrown()
    }
}
