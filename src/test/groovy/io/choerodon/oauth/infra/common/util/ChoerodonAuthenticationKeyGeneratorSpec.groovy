package io.choerodon.oauth.infra.common.util

import io.choerodon.oauth.IntegrationTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.web.authentication.WebAuthenticationDetails
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class ChoerodonAuthenticationKeyGeneratorSpec extends Specification {
    @Autowired
    private ChoerodonAuthenticationKeyGenerator choerodonAuthenticationKeyGenerator

    def "ExtractKey"() {
        given: "参数准备"
        def authorizationRequest = Mock(OAuth2Request)
        authorizationRequest.getClientId() >> { return "clientId" }
        authorizationRequest.getScope() >> {
            def set = new HashSet<String>()
            set.add("string")
            return set
        }
        def authentication = Mock(OAuth2Authentication)
        authentication.getOAuth2Request() >> { return authorizationRequest }
        authentication.isClientOnly() >> { return false }
        authentication.getName() >> { return "userName" }
        def webAuthenticationDetails = Mock(WebAuthenticationDetails)
        webAuthenticationDetails.getSessionId() >> { return "sessionId" }
        def userAuthentication = Mock(UsernamePasswordAuthenticationToken)
        userAuthentication.getDetails() >> { return webAuthenticationDetails }
        authentication.getUserAuthentication() >> { return userAuthentication }
//        and: "mock静态方法"
//
//        def digest = Mock(MessageDigest)
//        digest.digest(_) >> {
//            def b = new byte[1]
//            b[1] = 1
//            return b
//        }
//        PowerMockito.mockStatic(MessageDigest.class)
//        PowerMockito.when(MessageDigest.getInstance("MD5")).thenReturn(digest)

        when: "方法调用"
        choerodonAuthenticationKeyGenerator.extractKey(authentication)
        then: "无异常抛出"
        noExceptionThrown()
    }
}
