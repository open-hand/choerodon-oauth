package io.choerodon.oauth.infra.common.util

import io.choerodon.oauth.IntegrationTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class RedisTokenUtilSpec extends Specification {
    @Autowired
    private RedisTokenUtil redisTokenUtil
    @Autowired
    private StringRedisTemplate mockRedisTemplate

    void setup() {
        redisTokenUtil.setRedisTemplate(mockRedisTemplate)
    }

    def "CreateLongToken"() {
        when: "方法调用"
        def token = redisTokenUtil.createLongToken()
        then: "结果分析"
        noExceptionThrown()
        token != null
    }

    def "CreateShortToken"() {
        when: "方法调用"
        def token = redisTokenUtil.createShortToken()
        then: "结果分析"
        noExceptionThrown()
        token != null
    }

    def "Store"() {
        given: "参数准备"
        def type = "type"
        def key = "key"
        def token = "token"
        and: "mock"
        def valueOperations = Mock(ValueOperations)
        mockRedisTemplate.opsForValue() >> { return valueOperations }
        when: "方法调用"
        def store = redisTokenUtil.store(type, key, token)
        then: "结果分析"
        noExceptionThrown()
        store == token
    }

    def "SetDisableTime"() {
        given: "参数准备"
        def key = "key"
        and: "mock"
        def valueOperations = Mock(ValueOperations)
        mockRedisTemplate.opsForValue() >> { return valueOperations }
        when: "方法调用"
        def time = redisTokenUtil.setDisableTime(key)
        then: "结果分析"
        noExceptionThrown()
        time != null
    }

    def "GetDisableTime"() {
        given: "参数准备"
        def key = "key"
        and: "mock"
        def valueOperations = Mock(ValueOperations)
        mockRedisTemplate.opsForValue() >> { return valueOperations }
        when: "方法调用"
        def time = redisTokenUtil.getDisableTime(key)
        then: "结果分析"
        noExceptionThrown()
        time == null
    }

    def "Expire"() {
        given: "参数准备"
        def type = "type"
        def key = "key"
        when: "方法调用"
        redisTokenUtil.expire(type, key)
        then: "结果分析"
        noExceptionThrown()
    }

    def "Check"() {
        given: "参数准备"
        def type = "type"
        def key = "key"
        def token = "token"
        and: "mock"
        def valueOperations = Mock(ValueOperations)
        mockRedisTemplate.opsForValue() >> { return valueOperations }
        when: "方法调用"
        def check = redisTokenUtil.check(type, key, token)
        then: "结果分析"
        noExceptionThrown()
        check == false
    }
}
