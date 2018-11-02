package io.choerodon.oauth.infra.feign.fallback

import io.choerodon.core.exception.CommonException
import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.api.dto.EmailSendDTO
import io.choerodon.oauth.api.dto.WsSendDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class NotifyFeignClientFallbackSpec extends Specification {

    @Autowired
    private NotifyFeignClientFallback notifyFeignClientFallback

    def "PostEmail"() {
        when: "方法调用"
        notifyFeignClientFallback.postNotice(new EmailSendDTO())
        then: "异常抛出"
        def e = thrown(CommonException)
        e.message == "notify.error"
    }

    def "PostPm"() {
        when: "方法调用"
        notifyFeignClientFallback.postPm(new WsSendDTO())
        then: "异常抛出"
        def e = thrown(CommonException)
        e.message == "notify.error"
    }
}
