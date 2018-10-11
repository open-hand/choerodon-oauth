package io.choerodon.oauth.api.service

import io.choerodon.core.exception.CommonException
import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.api.dto.PasswordForgetDTO
import io.choerodon.oauth.api.dto.UserDTO
import io.choerodon.oauth.api.service.impl.PasswordForgetServiceImpl
import io.choerodon.oauth.api.validator.UserValidator
import io.choerodon.oauth.core.password.PasswordPolicyManager
import io.choerodon.oauth.core.password.mapper.BasePasswordPolicyMapper
import io.choerodon.oauth.core.password.record.PasswordRecord
import io.choerodon.oauth.domain.entity.UserE
import io.choerodon.oauth.infra.common.util.RedisTokenUtil
import io.choerodon.oauth.infra.feign.NotifyFeignClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class PasswordForgetServiceSpec extends Specification {

    private PasswordForgetService passwordForgetService

    private UserService mockUserService = Mock(UserService)
    private BasePasswordPolicyMapper mockBasePasswordPolicyMapper = Mock(BasePasswordPolicyMapper)
    private PasswordPolicyManager mockPasswordPolicyManager = Mock(PasswordPolicyManager)
    private PasswordRecord mockPasswordRecord = Mock(PasswordRecord)
    private NotifyFeignClient mockNotifyFeignClient = Mock(NotifyFeignClient)
    private RedisTokenUtil mockRedisTokenUtil = Mock(RedisTokenUtil)
    private UserValidator mockUserValidator = Mock(UserValidator)
    private MessageSource mockMessageSource = Mock(MessageSource)

    void setup() {
        passwordForgetService = new PasswordForgetServiceImpl(mockUserService, mockBasePasswordPolicyMapper, mockPasswordPolicyManager, mockPasswordRecord)
        passwordForgetService.setNotifyFeignClient(mockNotifyFeignClient)
        passwordForgetService.setRedisTokenUtil(mockRedisTokenUtil)
        passwordForgetService.setUserValidator(mockUserValidator)
        passwordForgetService.setMessageSource(mockMessageSource)
    }

    def "CheckUserByEmail"() {
        given: '参数准备'
        def email = "test@test.com"

        and: 'mock'
        mockUserValidator.emailValidator(email) >> { return flag1 }
        mockUserService.queryByEmail(email) >> { return user }

        when: '方法调用'
        passwordForgetService.checkUserByEmail(email)

        then: '结果分析'
        noExceptionThrown()

        where: 'mock结果情况'
        flag1 | user
        false | null
        true  | null
        true  | new UserE(ldap: true)
        true  | new UserE(ldap: false)
    }

    def "Send"() {
        given: "参数准备"
        def passwordForgetDTO = new PasswordForgetDTO(user: new UserDTO(1, "loginName", "test@test.com"))

        and: 'mock'
        mockRedisTokenUtil.getDisableTime(_) >> { return time }

        when: "方法调用"
        passwordForgetService.send(passwordForgetDTO)

        then: "无异常抛出，方法调用如下"
        noExceptionThrown()
        num1 * mockRedisTokenUtil.createShortToken()
        num2 * mockRedisTokenUtil.store(_, _, _)
        num3 * mockMessageSource.getMessage(_, _, _)
        num4 * mockNotifyFeignClient.postEmail(_)

        where: "分支覆盖"
        time                 | num1 | num2 | num3 | num4
        new Date().getTime() | 0    | 0    | 1    | 0
        null                 | 1    | 1    | 0    | 1

    }

    def "Send[Exception]"() {
        given: "参数准备"
        def passwordForgetDTO = new PasswordForgetDTO(user: new UserDTO(1, "loginName", "test@test.com"))

        and: 'mock'
        mockRedisTokenUtil.getDisableTime(_) >> { return null }
        mockNotifyFeignClient.postEmail(_) >> { throw new CommonException("") }

        when: "方法调用"
        passwordForgetService.send(passwordForgetDTO)

        then: "无异常抛出，方法调用如下"
        noExceptionThrown()
        1 * mockRedisTokenUtil.createShortToken()
        1 * mockRedisTokenUtil.store(_, _, _)
        0 * mockMessageSource.getMessage(_, _, _)
    }


    def "Check"() {
        given: "参数准备"
        def passwordForgetDTO = new PasswordForgetDTO(user: new UserDTO(1, "loginName", "test@test.com"))
        def captcha = "captcha"

        when: "方法调用"
        passwordForgetService.check(passwordForgetDTO, captcha)

        then: "无异常抛出，方法调用如下"
        noExceptionThrown()
        1 * mockRedisTokenUtil.check(_, _, _)
    }

    def "Reset"() {
        given: "参数准备"
        def passwordForgetDTO = new PasswordForgetDTO(user: new UserDTO(1, "loginName", "test@test.com"))
        def captcha = "captcha"
        def password = "password"

        and: 'mock'
        mockUserService.queryByEmail(passwordForgetDTO.getUser().getEmail()) >> {
            return new UserE(id: 1L, realName: "realName", email: "test@test.com")
        }
        mockUserService.updateSelective(_) >> { return userE }
        mockNotifyFeignClient.postPm(_) >> { throw new CommonException("") }

        when: "方法调用"
        passwordForgetService.reset(passwordForgetDTO, captcha, password)

        then: "无异常抛出，方法调用如下"
        noExceptionThrown()
        1 * mockPasswordPolicyManager.passwordValidate(_, _, _)
        1 * mockRedisTokenUtil.expire(_, _)
        1 * mockBasePasswordPolicyMapper.selectByPrimaryKey(_)
        1 * mockBasePasswordPolicyMapper.findByOrgId(_)
        num * mockPasswordRecord.updatePassword(_, _)

        where: "分支覆盖"
        userE                                                           | num
        null                                                            | 0
        new UserE(id: 1L, realName: "realName", email: "test@test.com") | 1
    }

    def "Reset[Exception]"() {
        given: "参数准备"
        def passwordForgetDTO = new PasswordForgetDTO(user: new UserDTO(1, "loginName", "test@test.com"))
        def captcha = "captcha"
        def password = "password"

        and: 'mock'
        mockUserService.queryByEmail(passwordForgetDTO.getUser().getEmail()) >> {
            return new UserE(id: 1L, realName: "realName", email: "test@test.com")
        }
        mockPasswordPolicyManager.passwordValidate(_, _, _) >> { throw new CommonException("") }

        when: "方法调用"
        passwordForgetService.reset(passwordForgetDTO, captcha, password)

        then: "无异常抛出，方法调用如下"
        noExceptionThrown()
        1 * mockRedisTokenUtil.expire(_, _)
        1 * mockBasePasswordPolicyMapper.selectByPrimaryKey(_)
        1 * mockBasePasswordPolicyMapper.findByOrgId(_)
    }
}
