package io.choerodon.oauth.api.controller.v1

import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.api.service.TokenService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class TokenControllerSpec extends Specification {
    private static final String BASE_PATH = "/v1/token_manager"

    @Autowired
    TestRestTemplate testRestTemplate
    @Autowired
    TokenController tokenController
    TokenService tokenService = Mock(TokenService)

    void setup() {
        tokenController.setTokenService(tokenService)
    }

    def "DeleteTokens"() {
        given: "参数准备"
        def loginName = "test"
        when: "根据登录名删除token及session"
        HttpEntity<Object> httpEntity = new HttpEntity<>()
        def exchange = testRestTemplate.exchange(BASE_PATH + "/all?loginName={loginName}", HttpMethod.DELETE, httpEntity, Void, loginName)
        then: "结果判断"
        noExceptionThrown()
        exchange.statusCode.is2xxSuccessful()
        1 * tokenService.deleteAllUnderUser(loginName)
    }

    def "DeleteToken"() {
        given: "参数准备"
        def tokenId = "17f6c2b7b6982d17d82c2d86bf499191"
        when: "根据TokenId删除token及session"
        HttpEntity<Object> httpEntity = new HttpEntity<>()
        def exchange = testRestTemplate.exchange(BASE_PATH + "/one?tokenId={tokenId}", HttpMethod.DELETE, httpEntity, Void, tokenId)
        then: "结果判断"
        noExceptionThrown()
        exchange.statusCode.is2xxSuccessful()
        1 * tokenService.deleteOne(tokenId)
    }
}
