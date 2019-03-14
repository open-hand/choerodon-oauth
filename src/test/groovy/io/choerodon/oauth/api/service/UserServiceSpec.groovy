package io.choerodon.oauth.api.service

import io.choerodon.core.exception.CommonException
import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.api.service.impl.UserServiceImpl
import io.choerodon.oauth.api.validator.UserValidator
import io.choerodon.oauth.domain.entity.UserE
import io.choerodon.oauth.infra.mapper.UserMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class UserServiceSpec extends Specification {
    @Autowired
    private UserServiceImpl userService

    private UserMapper mockUserMapper = Mock(UserMapper)
    private UserValidator mockUserValidator = Mock(UserValidator)

    void setup() {
        userService.setUserMapper(mockUserMapper)
        userService.setUserValidator(mockUserValidator)
    }

//    def "QueryByLoginField"() {
//        given: "参数准备"
//        and: 'mock'
//        mockUserMapper.selectOne(_) >> { return userE }
//        mockUserValidator.emailValidator(_) >> { return flag1 }
//        mockUserValidator.phoneValidator(_) >> { return flag2 }
//
//        when: '方法调用'
//        userService.queryByLoginField(field)
//
//        then: '结果分析'
//        noExceptionThrown()
//        where: '参数条件'
//        field            | userE                             | flag1 | flag2
//        null             | null                              | false | false
//        "test1@test.com" | new UserE(loginName: "loginName") | false | false
//        "test2@test.com" | null                              | true  | false
//        "test3@test.com" | null                              | false | true
//        "test4@test.com" | null                              | false | false
//    }

    def "UpdateSelective"() {
        given: '参数准备'
        def userE = new UserE(id: 1)
        and: 'mock'
        mockUserMapper.updateByPrimaryKeySelective(userE) >> { return 1 }
        when: '方法调用'
        userService.updateSelective(userE)
        then: '结果分析'
        noExceptionThrown()
        1 * mockUserMapper.selectByPrimaryKey(userE.getId())
    }

    def "UpdateSelective[Exception]"() {
        given: '参数准备'
        def userE = new UserE(id: 1)
        and: 'mock'
        mockUserMapper.updateByPrimaryKeySelective(userE) >> { return 0 }
        when: '方法调用'
        userService.updateSelective(userE)
        then: '结果分析'
        def e = thrown(CommonException)
        e.message == "error.user.update"
        0 * mockUserMapper.selectByPrimaryKey(userE.getId())
    }

    def "QueryByEmail"() {
        given: '参数准备'
        def email = "test1@test.com"
        when: '方法调用'
        userService.queryByEmail(email)
        then: '结果分析'
        noExceptionThrown()
        1 * mockUserMapper.selectOne(_)
    }
}
