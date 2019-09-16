package io.choerodon.oauth.api.validator

import io.choerodon.core.exception.CommonException
import io.choerodon.oauth.api.service.SystemSettingService
import io.choerodon.oauth.infra.dataobject.PasswordPolicyDO
import io.choerodon.oauth.api.vo.SysSettingVO
import io.choerodon.oauth.infra.mapper.PasswordPolicyMapper
import org.mockito.Mockito
import spock.lang.Specification

/**
 *
 * @author zmf
 *
 */
class UserPasswordValidatorSpec extends Specification {
    def "Validate"() {
        given: '配置validator'
        PasswordPolicyMapper mockPasswordPolicyMapper = Mockito.mock(PasswordPolicyMapper)
        SystemSettingService mockSystemSettingService = Mockito.mock(SystemSettingService)
        UserPasswordValidator userPasswordValidator = new UserPasswordValidator(mockPasswordPolicyMapper, mockSystemSettingService)

        and: '组织启用密码策略时'
        PasswordPolicyDO passwordPolicyDO = new PasswordPolicyDO()
        passwordPolicyDO.setEnablePassword(true)
        Mockito.when(mockPasswordPolicyMapper.selectOne(Mockito.any(PasswordPolicyDO))).thenReturn(passwordPolicyDO)

        when:
        boolean result = userPasswordValidator.validate("12", 1L, false)

        then: '校验结果'
        result

        and: '组织未启用密码策略，系统设置为空时'
        passwordPolicyDO.setEnablePassword(false)
        Mockito.when(mockSystemSettingService.getSetting()).thenReturn(null)

        when:
        result = userPasswordValidator.validate("12", 1L, false)

        then: '校验结果'
        result

        and: '组织未启用密码策略，系统设置不为空时'
        SysSettingVO setting = new SysSettingVO()
        setting.setMinPasswordLength(6)
        setting.setMaxPasswordLength(16)
        Mockito.when(mockSystemSettingService.getSetting()).thenReturn(setting)

        when: '测试无效的密码'
        result = userPasswordValidator.validate(" 1  234 ", 1L, false)

        then: '校验结果'
        !result

        when: '调用抛出异常'
        userPasswordValidator.validate("12", 1L, true)

        then: '校验结果'
        thrown(CommonException)

        when: '测试有效的密码'
        result = userPasswordValidator.validate("123456", 1L, true)

        then: '校验结果'
        result
    }
}
