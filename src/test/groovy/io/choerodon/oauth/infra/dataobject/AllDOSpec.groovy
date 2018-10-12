package io.choerodon.oauth.infra.dataobject

import io.choerodon.oauth.IntegrationTestConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class AllDOSpec extends Specification {
    def "AccessTokenDO"() {
        given: "构造器"
        def accessTokenDO = new AccessTokenDO()
        def date = new Date()
        when: "覆盖set方法"
        accessTokenDO.setAuthenticationId("1")
        accessTokenDO.setClientId("1")
        accessTokenDO.setLastTime(date)
        accessTokenDO.setRefreshToken("refreshToken")
        accessTokenDO.setTokenId("1")
        accessTokenDO.setUserName("username")
        then: "测试get方法"
        accessTokenDO.getAuthenticationId() == "1"
        accessTokenDO.getClientId() == "1"
        accessTokenDO.getLastTime() == date
        accessTokenDO.getRefreshToken() == "refreshToken"
        accessTokenDO.getTokenId() == "1"
        accessTokenDO.getUserName() == "username"
    }

    def "PasswordPolicyDO"() {
        given: "构造器"
        def passwordPolicyDO = new PasswordPolicyDO()
        def id = 1L
        def code = "code"
        def name = "name"
        def organizationId = 1L
        def originalPassword = "originalPassword"
        def minLength = 1
        def maxLength = 5
        def maxErrorTime = 3600
        def digitsCount = 1
        def lowercaseCount = 3
        def uppercaseCount = 2
        def specialCharCount = 0
        def notUsername = true
        def regularExpression = "\\w"
        def notRecentCount = 0
        def enablePassword = true
        def enableSecurity = true
        def enableLock = true
        def lockedExpireTime = 3600
        def enableCaptcha = true
        def maxCheckCaptcha = 5
        when: "覆盖set方法"
        passwordPolicyDO.setId(id)
        passwordPolicyDO.setCode(code)
        passwordPolicyDO.setName(name)
        passwordPolicyDO.setObjectVersionNumber(1L)
        passwordPolicyDO.setDigitsCount(digitsCount)
        passwordPolicyDO.setEnableCaptcha(enableCaptcha)
        passwordPolicyDO.setEnableLock(enableLock)
        passwordPolicyDO.setEnablePassword(enablePassword)
        passwordPolicyDO.setEnableSecurity(enableSecurity)
        passwordPolicyDO.setLockedExpireTime(lockedExpireTime)
        passwordPolicyDO.setLowercaseCount(lowercaseCount)
        passwordPolicyDO.setMaxCheckCaptcha(maxCheckCaptcha)
        passwordPolicyDO.setMaxErrorTime(maxErrorTime)
        passwordPolicyDO.setMaxLength(maxLength)
        passwordPolicyDO.setMinLength(minLength)
        passwordPolicyDO.setNotRecentCount(notRecentCount)
        passwordPolicyDO.setNotUsername(notUsername)
        passwordPolicyDO.setOrganizationId(organizationId)
        passwordPolicyDO.setOriginalPassword(originalPassword)
        passwordPolicyDO.setRegularExpression(regularExpression)
        passwordPolicyDO.setSpecialCharCount(specialCharCount)
        passwordPolicyDO.setUppercaseCount(uppercaseCount)
        then: "覆盖get方法"
        passwordPolicyDO.getId()==id
        passwordPolicyDO.getCode()==code
        passwordPolicyDO.getName()==name
        passwordPolicyDO.getObjectVersionNumber()==1L
        passwordPolicyDO.getDigitsCount()==digitsCount
        passwordPolicyDO.getEnableCaptcha()==enableCaptcha
        passwordPolicyDO.getEnableLock()==enableLock
        passwordPolicyDO.getEnablePassword()==enablePassword
        passwordPolicyDO.getEnableSecurity()==enableSecurity
        passwordPolicyDO.getLockedExpireTime()==lockedExpireTime
        passwordPolicyDO.getLowercaseCount()==lowercaseCount
        passwordPolicyDO.getMaxCheckCaptcha()==maxCheckCaptcha
        passwordPolicyDO.getMaxErrorTime()==maxErrorTime
        passwordPolicyDO.getMaxLength()==maxLength
        passwordPolicyDO.getMinLength()==minLength
        passwordPolicyDO.getNotRecentCount()==notRecentCount
        passwordPolicyDO.getNotUsername()==notUsername
        passwordPolicyDO.getOrganizationId()==organizationId
        passwordPolicyDO.getOriginalPassword()==originalPassword
        passwordPolicyDO.getRegularExpression()==regularExpression
        passwordPolicyDO.getSpecialCharCount()==specialCharCount
        passwordPolicyDO.getUppercaseCount()==uppercaseCount
    }
}
