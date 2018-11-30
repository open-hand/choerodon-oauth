package io.choerodon.oauth.api.service.impl

import io.choerodon.core.exception.CommonException
import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.api.service.TokenService
import io.choerodon.oauth.infra.dataobject.AccessTokenDO
import io.choerodon.oauth.infra.mapper.AccessTokenMapper
import io.choerodon.oauth.infra.mapper.RefreshTokenMapper
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken
import org.springframework.security.oauth2.common.util.SerializationUtils
import org.springframework.session.FindByIndexNameSessionRepository
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class TokenServiceImplSpec extends Specification {
    private TokenService tokenService
    private StringRedisTemplate redisTemplate = Mock(StringRedisTemplate)
    private FindByIndexNameSessionRepository findByIndexNameSessionRepository = Mock(FindByIndexNameSessionRepository)
    private AccessTokenMapper accessTokenMapper = Mock(AccessTokenMapper)
    private RefreshTokenMapper refreshTokenMapper = Mock(RefreshTokenMapper)

    void setup() {
        tokenService = new TokenServiceImpl(redisTemplate, findByIndexNameSessionRepository, accessTokenMapper, refreshTokenMapper)
    }

    def "DeleteOne[Exception]"() {
        given: "参数准备"
        def tokenId1 = "17f6c2b7b6982d17d82c2d86bf499191"
        def tokenId2 = "16c7e0b1c4cd777c94be483187a88534"
        def token = new DefaultOAuth2AccessToken(tokenId2)
        def map = new HashMap<String, String>()
        map.put("sessionId", "13687d8a-8f4e-419e-83e7-52f0ae60e2bf")
        token.setAdditionalInformation(map)
        def accessTokenDO = new AccessTokenDO()
        accessTokenDO.setToken(SerializationUtils.serialize(token))
        accessTokenDO.setRefreshToken(tokenId1)
        and: "mock"
        accessTokenMapper.selectByPrimaryKey(tokenId2) >> { return accessTokenDO }
        when: "方法调用"
        tokenService.deleteOne(tokenId1)
        then: "抛出异常"
        def e = thrown(CommonException)
        e.message == "error.delete.token.not.exist"

        when: "方法调用"
        tokenService.deleteOne(tokenId2)
        then: "无异常抛出"
        noExceptionThrown()
        1 * redisTemplate.delete(_)
        1 * accessTokenMapper.deleteByPrimaryKey(tokenId2)
        1 * refreshTokenMapper.deleteByPrimaryKey(tokenId1)
    }


    def "DeleteAllUnderUser"() {
        given: "参数准备"
        def loginName = "test"
        def select = new ArrayList<AccessTokenDO>()
        def accessTokenDO1 = new AccessTokenDO()
        def accessTokenDO2 = new AccessTokenDO()
        accessTokenDO1.setRefreshToken("17f6c2b7b6982d17d82c2d86bf499191")
        accessTokenDO2.setRefreshToken("16c7e0b1c4cd777c94be483187a88534")
        select.add(accessTokenDO1)
        select.add(accessTokenDO2)
        def byIndexNameAndIndexValue = new HashMap<String, Object>()
        and: "mock"
        accessTokenMapper.select(_) >> { return select }
        findByIndexNameSessionRepository.findByIndexNameAndIndexValue(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, loginName) >> {
            return byIndexNameAndIndexValue
        }
        when: "方法调用"
        tokenService.deleteAllUnderUser(loginName)
        then: "无异常抛出，方法调用正常"
        noExceptionThrown()
        select.size() * refreshTokenMapper.deleteByPrimaryKey(_)
        1 * accessTokenMapper.deleteUsersToken(loginName)
        1 * redisTemplate.delete(_)
    }
}
