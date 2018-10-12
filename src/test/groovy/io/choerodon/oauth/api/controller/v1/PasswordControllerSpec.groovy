package io.choerodon.oauth.api.controller.v1

import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.api.dto.PasswordForgetDTO
import io.choerodon.oauth.api.dto.UserDTO
import io.choerodon.oauth.api.service.PasswordForgetService
import io.choerodon.oauth.api.service.PasswordPolicyService
import io.choerodon.oauth.api.service.UserService
import io.choerodon.oauth.domain.entity.UserE
import io.choerodon.oauth.infra.enums.PasswordFindException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class PasswordControllerSpec extends Specification {
    @Autowired
    TestRestTemplate testRestTemplate
    @Autowired
    PasswordController passwordController
    private PasswordForgetService mockPasswordForgetService = Mock(PasswordForgetService)
    private MessageSource mockMessageSource = Mock(MessageSource)
    private PasswordPolicyService mockPasswordPolicyService = Mock(PasswordPolicyService)
    private UserService mockUserService = Mock(UserService)

    void setup() {
        passwordController.setPasswordForgetService(mockPasswordForgetService)
        passwordController.setMessageSource(mockMessageSource)
        passwordController.setUserService(mockUserService)
        passwordController.setPasswordPolicyService(mockPasswordPolicyService)
    }

    def "Find"() {
        when: '找回密码'
        def entity = testRestTemplate.getForEntity(
                "/password/find", String)
        then: '结果分析'
        entity.statusCode.is2xxSuccessful()
        noExceptionThrown()
    }

    def "Send"() {
        given: "准备邮件地址"
        def emailAddress = "test@test.com"
        and: "mock校验结果"
        mockPasswordForgetService.checkUserByEmail(emailAddress) >> { return checkResult }
        when: '向send接口发送post请求'
        def codesEntity = testRestTemplate.postForEntity("/password/send?emailAddress={emailAddress}", null, PasswordForgetDTO, emailAddress)
        then: '结果分析'
        noExceptionThrown()
        codesEntity.statusCode.is2xxSuccessful()
        num * mockPasswordForgetService.send(_)
        where: "根据checkUser结果进行分支覆盖"
        checkResult                           || num
        new PasswordForgetDTO(success: false) || 0
        new PasswordForgetDTO(success: true)  || 1
    }


    def "Check"() {
        given: "准备参数"
        def emailAddress = "test@test.com"
        def captcha = "captcha"
        and: "mock校验结果"
        mockPasswordForgetService.checkUserByEmail(emailAddress) >> { return checkResult1 }
        mockPasswordForgetService.check(_, _) >> { return checkResult2 }
        mockUserService.queryByEmail(emailAddress) >> { return new UserE(organizationId: 1L) }
        when: '向check接口发送post请求'
        def codesEntity = testRestTemplate.postForEntity("/password/check?emailAddress={emailAddress}&captcha={captcha}", null, PasswordForgetDTO, emailAddress, captcha)
        then: '结果分析'
        noExceptionThrown()
        codesEntity.statusCode.is2xxSuccessful()
        where: "根据checkUser结果进行分支覆盖"
        checkResult1                          || checkResult2
        new PasswordForgetDTO(success: false) || new PasswordForgetDTO(success: false)
        new PasswordForgetDTO(success: true)  || new PasswordForgetDTO(success: false)
        new PasswordForgetDTO(success: true)  || new PasswordForgetDTO(success: true)

    }

    def "Reset"() {
        given: "准备参数"
        def emailAddress = "test@test.com"
        def captcha = "captcha"
        def userId = 1L
        def pwd = "pwd"
        and: "mock"
        mockPasswordForgetService.checkUserByEmail(emailAddress) >> { return checkResult }
        mockPasswordForgetService.check(_, _) >> { return new PasswordForgetDTO(success: success) }
        when: '向check接口发送post请求'
        def codesEntity = testRestTemplate.postForEntity("/password/reset?emailAddress={emailAddress}&captcha={captcha}&userId={userId}&password={pwd}&password1={pwd1}", null, PasswordForgetDTO, emailAddress, captcha, userId, pwd, pwd1)


        then: '结果分析'
        noExceptionThrown()
        codesEntity.statusCode.is2xxSuccessful()
        num1 * mockMessageSource.getMessage(PasswordFindException.PASSWORD_NOT_EQUAL.value(), null, _)
        num2 * mockMessageSource.getMessage(PasswordFindException.USER_IS_ILLEGAL.value(), null, _)
        num3 * mockPasswordForgetService.reset(_, _, _)

        where: "分支覆盖"
        pwd1   | checkResult                                                                               | success || num1 | num2 | num3
        "pwd1" | new PasswordForgetDTO(success: false)                                                     | false   || 1    | 0    | 0
        "pwd"  | new PasswordForgetDTO(success: false)                                                     | false   || 0    | 0    | 0
        "pwd"  | new PasswordForgetDTO(success: true, user: new UserDTO(2L, "loginName", "test@test.com")) | false   || 0    | 1    | 0
        "pwd"  | new PasswordForgetDTO(success: true, user: new UserDTO(1L, "loginName", "test@test.com")) | false   || 0    | 0    | 0
        "pwd"  | new PasswordForgetDTO(success: true, user: new UserDTO(1L, "loginName", "test@test.com")) | true    || 0    | 0    | 1

    }

    def "CheckDisable"() {
        given: "准备邮件地址"
        def emailAddress = "test@test.com"
        when: '向send接口发送post请求'
        def codesEntity = testRestTemplate.postForEntity("/password/check_disable?emailAddress={emailAddress}", null, PasswordForgetDTO, emailAddress)
        then: '结果分析'
        noExceptionThrown()
        codesEntity.statusCode.is2xxSuccessful()
        1 * mockPasswordForgetService.checkDisable(emailAddress)
    }
}
