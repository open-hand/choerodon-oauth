package io.choerodon.oauth.infra.common.util.ldap

import io.choerodon.oauth.IntegrationTestConfiguration
import io.choerodon.oauth.domain.entity.LdapE
import org.junit.runner.RunWith
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.modules.junit4.PowerMockRunnerDelegate
import org.spockframework.runtime.Sputnik
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import spock.lang.Specification

import javax.naming.NamingEnumeration
import javax.naming.NamingException
import javax.naming.ldap.LdapContext

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration)
class LdapUtilSpec extends Specification {

    def "Authenticate"() {
        given: "参数准备"
        def username = "username"
        def password = "password"
        def ldap = new LdapE()
        ldap.setServerAddress("ldap://ac.hand-china.com")
        ldap.setBaseDn("ou=employee,dc=hand-china,dc=com")
        ldap.setPort("389")
        ldap.setUseSSL(true)
        ldap.setLoginNameField("employeeNumber")
        ldap.setRealNameField("displayName")
        ldap.setEmailField("mail")
        ldap.setPhoneField("mobile")
        when: "方法调用"
        LdapUtil.authenticate(username, password, ldap)
        then: "结果分析"
        noExceptionThrown()
    }

//    def "Authenticate-2"() {
//        given: "参数准备"
//        def username = "username"
//        def password = "password"
//        def ldap = new LdapE()
//        ldap.setServerAddress("ldap://ac.hand-china.com")
//        ldap.setBaseDn("ou=employee,dc=hand-china,dc=com")
//        ldap.setPort("389")
//        ldap.setUseSSL(true)
//        ldap.setLoginNameField("employeeNumber")
//        ldap.setRealNameField("displayName")
//        ldap.setEmailField("mail")
//        ldap.setPhoneField("mobile")
//        and: "mock"
//        PowerMockito.mockStatic(LdapUtil.class)
//        PowerMockito.when(LdapUtil.ldapConnect(ldap.getServerAddress(), ldap.getBaseDn(), ldap.getPort(), ldap.getUseSSL())).thenReturn(Mock(LdapContext))
//        when: "方法调用"
//        LdapUtil.authenticate(username, password, ldap)
//        then: "结果分析"
//        noExceptionThrown()
//    }

    def "GetUserDn"() {
        given: "参数准备"
        def namingEnumeration = new NamingEnumeration() {
            @Override
            Object next() throws NamingException {
                return null
            }

            @Override
            boolean hasMore() throws NamingException {
                return false
            }

            @Override
            void close() throws NamingException {

            }

            @Override
            boolean hasMoreElements() {
                return false
            }

            @Override
            Object nextElement() {
                return null
            }
        }
        namingEnumeration.hasMoreElements() >> { return true }

        def ldapContext = Mock(LdapContext)
        ldapContext.search(_, _, _) >> { return namingEnumeration }

        def userName = "userName"
        def ldap = new LdapE()
        ldap.setServerAddress("ldap://ac.hand-china.com")
        ldap.setBaseDn("ou=employee,dc=hand-china,dc=com")
        ldap.setPort("389")
        ldap.setUseSSL(true)
        ldap.setLoginNameField("employeeNumber")
        ldap.setRealNameField("displayName")
        ldap.setEmailField("mail")
        ldap.setPhoneField("mobile")
        when: "方法调用"
        LdapUtil.getUserDn(ldapContext, ldap, userName)
        then: "结果分析"
        noExceptionThrown()
    }

    def "LdapAuthenticate"() {
        given: "参数准备"
        def ldapContext = Mock(LdapContext)
        def userDn = ""
        def password = "pwd"
        when: "方法调用"
        LdapUtil.ldapAuthenticate(ldapContext, userDn, password)
        then: "无异常抛出"
        noExceptionThrown()
    }
}
