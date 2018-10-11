package io.choerodon.oauth.api.validator

import io.choerodon.oauth.IntegrationTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class UserValidatorSpec extends Specification {
    @Autowired
    private UserValidator userValidator

    def "EmailValidator"() {
        expect: "期待邮箱格式是否合法"
        userValidator.emailValidator(email) == result
        where: "结果比对"
        email         || result
        "text@qq.com" || true
        "invalid"     || false
    }

    def "PhoneValidator"() {
        expect: "期待手机格式是否合法"
        userValidator.phoneValidator(phone) == result
        where: "结果比对"
        phone         || result
        "13311029103" || true
        "invalid"     || false
    }
}
