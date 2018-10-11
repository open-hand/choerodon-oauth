package io.choerodon.oauth.infra.common.util

import io.choerodon.oauth.infra.config.OauthProperties
import io.choerodon.oauth.infra.mapper.AccessTokenMapper
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore
import spock.lang.Specification

import javax.sql.DataSource

@PrepareForTest([JdbcTokenStore.class])
@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;")
@PowerMockRunnerDelegate(Sputnik.class)
class CustomTokenStoreSpec extends Specification {
    CustomTokenStore customTokenStore
    private OauthProperties mockOauthProperties = Mock(OauthProperties)
    private AccessTokenMapper mockAccessTokenMapper = Mock(AccessTokenMapper)
    private ChoerodonAuthenticationKeyGenerator mockAuthenticationKeyGenerator = Mock(ChoerodonAuthenticationKeyGenerator)
    private DataSource datasource = Mock(DataSource)

    void setup() {
        customTokenStore = new CustomTokenStore(datasource, mockAuthenticationKeyGenerator)
        customTokenStore.setOauthProperties(mockOauthProperties)
        customTokenStore.setAccessTokenMapper(mockAccessTokenMapper)
    }

    def "GetAccessToken"() {
        given: "参数准备"
        def authentication = Mock(OAuth2Authentication)
        authentication.isClientOnly() >> { return false }
        authentication.getName() >> { return "name" }
        def request = Mock(OAuth2Request)
        request.getClientId() >> { return "1" }
        authentication.getOAuth2Request() >> { return request }

        and: "mock"
        mockOauthProperties.isEnabledSingleLogin() >> { return true }
        PowerMockito.suppress(PowerMockito.method(JdbcTokenStore.class, "getAccessToken", OAuth2Authentication.class))

        when: "方法调用"
        customTokenStore.getAccessToken(authentication)

        then: "无异常抛出，方法被调用"
        noExceptionThrown()
        1 * mockAuthenticationKeyGenerator.extractKey(_)
        1 * mockAccessTokenMapper.selectTokens(_, _, _)
        1 * mockAccessTokenMapper.deleteTokens(_, _, _)
    }

    def "RemoveAccessToken"() {
        given: "mock父类方法"
        PowerMockito.suppress(PowerMockito.method(JdbcTokenStore.class, "removeAccessToken", String.class))

        when: "方法调用"
        customTokenStore.removeAccessToken("")

        then: "结果分析"
        noExceptionThrown()
        1 * mockAccessTokenMapper.selectByPrimaryKey(_)
    }
}
