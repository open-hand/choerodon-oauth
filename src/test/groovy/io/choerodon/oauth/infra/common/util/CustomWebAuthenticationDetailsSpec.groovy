package io.choerodon.oauth.infra.common.util

import io.choerodon.oauth.IntegrationTestConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class CustomWebAuthenticationDetailsSpec extends Specification {
    private CustomWebAuthenticationDetails customWebAuthenticationDetails


    void setup() {
        def session = Mock(HttpSession)
        session.getAttribute("captchaCode") >> { return "captchaCode" }
        def httpServletRequest = Mock(HttpServletRequest)
        httpServletRequest.getSession() >> { return session }
        httpServletRequest.getParameter("captcha") >> { return "captcha" }
        customWebAuthenticationDetails = new CustomWebAuthenticationDetails(httpServletRequest)
    }

    def "GetCaptchaCode"() {
        when: "方法调用"
        def code = customWebAuthenticationDetails.getCaptchaCode()
        then: "结果比对"
        code == "captchaCode"
    }

    def "GetCaptcha"() {
        when: "方法调用"
        def code = customWebAuthenticationDetails.getCaptcha()
        then: "结果比对"
        code == "captcha"
    }

    def "Equals"() {
        given: "参数准备"
        def session2 = Mock(HttpSession)
        session2.getAttribute("captchaCode") >> { return "captchaCode" }
        def httpServletRequest2 = Mock(HttpServletRequest)
        httpServletRequest2.getSession() >> { return session2 }
        httpServletRequest2.getParameter("captcha") >> { return "captcha" }
        def object = new CustomWebAuthenticationDetails(httpServletRequest2)

        when: "方法调用"
        def equals1 = customWebAuthenticationDetails.equals(customWebAuthenticationDetails)
        then: "结果校验"
        equals1 == true

        when: "方法调用"
        def equals2 = customWebAuthenticationDetails.equals(null)
        then: "结果校验"
        equals2 == false

        when: "方法调用"
        def equals3 = customWebAuthenticationDetails.equals(object)
        then: "结果校验"
        equals3 == true
    }

    def "HashCode"() {
        when: "方法调用"
        def code = customWebAuthenticationDetails.hashCode()
        then: "结果校验"
        noExceptionThrown()
        code != null
    }
}
