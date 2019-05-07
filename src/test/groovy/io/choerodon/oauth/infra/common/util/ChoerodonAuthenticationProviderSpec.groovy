package io.choerodon.oauth.infra.common.util

import io.choerodon.core.oauth.CustomUserDetails
import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.api.service.LdapService
import io.choerodon.oauth.api.service.OrganizationService
import io.choerodon.oauth.api.service.UserService
import io.choerodon.oauth.core.password.PasswordPolicyManager
import io.choerodon.oauth.core.password.domain.BasePasswordPolicyDTO
import io.choerodon.oauth.core.password.mapper.BasePasswordPolicyMapper
import io.choerodon.oauth.core.password.record.PasswordRecord
import io.choerodon.oauth.core.password.service.BaseUserService
import io.choerodon.oauth.domain.entity.LdapE
import io.choerodon.oauth.domain.entity.OrganizationE
import io.choerodon.oauth.domain.entity.UserE
import io.choerodon.oauth.infra.exception.CustomAuthenticationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class ChoerodonAuthenticationProviderSpec extends Specification {
    @Autowired
    private ChoerodonAuthenticationProvider choerodonAuthenticationProvider

    private CustomUserDetailsServiceImpl mockUserDetailsService = Mock(CustomUserDetailsServiceImpl)
    private OrganizationService mockOrganizationService = Mock(OrganizationService)
    private BasePasswordPolicyMapper mockBasePasswordPolicyMapper = Mock(BasePasswordPolicyMapper)
    private BaseUserService mockBaseUserService = Mock(BaseUserService)
    private PasswordRecord mockPasswordRecord = Mock(PasswordRecord)
    private PasswordPolicyManager mockPasswordPolicyManager = Mock(PasswordPolicyManager)
    private LdapService mockLdapService = Mock(LdapService)
    private UserService mockUserService = Mock(UserService)

    void setup() {
        choerodonAuthenticationProvider.setUserDetailsService(mockUserDetailsService)
        choerodonAuthenticationProvider.setOrganizationService(mockOrganizationService)
        choerodonAuthenticationProvider.setBasePasswordPolicyMapper(mockBasePasswordPolicyMapper)
        choerodonAuthenticationProvider.setBaseUserService(mockBaseUserService)
        choerodonAuthenticationProvider.setPasswordRecord(mockPasswordRecord)
        choerodonAuthenticationProvider.setPasswordPolicyManager(mockPasswordPolicyManager)
        choerodonAuthenticationProvider.setLdapService(mockLdapService)
        choerodonAuthenticationProvider.setUserService(mockUserService)
    }

    def "RetrieveUser[Exception]"() {
        given: "参数准备"
        def username = "username"
        def authentication = new UsernamePasswordAuthenticationToken(1, "")
        and: "mock"
        mockUserService.queryByLoginField(_) >> { return user }
        mockOrganizationService.queryOrganizationById(_) >> { return organization }
        mockBasePasswordPolicyMapper.selectOne(_) >> { return new BasePasswordPolicyDTO(lockedExpireTime: 3600) }
        mockPasswordPolicyManager.loginValidate(_, _, _) >> {
            Map returnMap = new HashMap()
            returnMap.put("maxErrorTime", false)
            return returnMap
        }
        when: "方法调用"
        choerodonAuthenticationProvider.retrieveUser(username, authentication)
        then: "结果分析"
        def e = thrown(exception)
        e.message == msg
        where: "异常测试"
        user                                                                                                                         | organization                      || exception                      | msg
        null                                                                                                                         | null                              || AuthenticationServiceException | "usernameNotFoundOrPasswordIsWrong"
        new UserE(organizationId: 1L)                                                                                                | null                              || AuthenticationServiceException | "organizationNotExist"
        new UserE(organizationId: 1L)                                                                                                | new OrganizationE(enabled: false) || AuthenticationServiceException | "organizationNotEnable"
        new UserE(id: 1L, organizationId: 1L, enabled: false)                                                                        | new OrganizationE(enabled: true)  || AuthenticationServiceException | "userNotActive"
        new UserE(id: 1L, organizationId: 1L, enabled: true, locked: true, lockedUntilAt: new Date(new Date().getTime() + 10000000)) | new OrganizationE(enabled: true)  || CustomAuthenticationException  | "accountLocked"
    }

    def "RetrieveUser"() {
        given: "参数准备"
        def username = "username"
        def authentication = new UsernamePasswordAuthenticationToken(1, "")
        and: "mock"
        mockUserService.queryByLoginField(_) >> {
            return new UserE(id: 1L, organizationId: 1L, enabled: true, locked: true, lockedUntilAt: new Date(new Date().getTime() - 10000000))
        }
        mockOrganizationService.queryOrganizationById(_) >> { return new OrganizationE(enabled: true) }
        mockBasePasswordPolicyMapper.findByOrgId(_) >> { return new BasePasswordPolicyDTO(lockedExpireTime: 3600) }
        mockPasswordPolicyManager.loginValidate(_, _, _) >> {
            Map returnMap = new HashMap()
            returnMap.put("maxErrorTime", false)
            return returnMap
        }
        when: "方法调用"
        choerodonAuthenticationProvider.retrieveUser(username, authentication)
        then: "结果分析"
        noExceptionThrown()
    }

    def "AdditionalAuthenticationChecks[Exception]"() {
        given: "参数准备"
        def userDetail = Mock(CustomUserDetails)
        def authentication = Mock(UsernamePasswordAuthenticationToken)
        def details = Mock(CustomWebAuthenticationDetails)

        and: "mock"
        userDetail.getUsername() >> { return "username" }
        userDetail.getPassword() >> { return "pwd" }
        details.getCaptcha() >> { return captcha }
        details.getCaptchaCode() >> { return captchaCode }
        authentication.getDetails() >> { return details }
        authentication.getCredentials() >> { return "" }
        mockUserService.queryByLoginField(_) >> { return new UserE(organizationId: 1L, ldap: isLdap) }
        mockPasswordPolicyManager.isNeedCaptcha(_, _) >> { return flag }
        mockLdapService.queryByOrgId(_) >> { return new LdapE(enabled: false) }

        when: "方法调用"
        choerodonAuthenticationProvider.additionalAuthenticationChecks(userDetail, authentication)

        then: "结果分析"
        def e = thrown(exception)
        e.message == msg

        where:
        "异常测试"
        flag  | captcha   | captchaCode | isLdap || exception                      | msg
        true  | null      | null        | false  || AuthenticationServiceException | "captchaNull"
        true  | "captcha" | "captcha1"  | false  || AuthenticationServiceException | "captchaWrong"
        false | "captcha" | "captcha"   | false  || AuthenticationServiceException | "usernameNotFoundOrPasswordIsWrong"
        false | "captcha" | "captcha"   | true   || AuthenticationServiceException | "ldapIsDisable"
    }
}
