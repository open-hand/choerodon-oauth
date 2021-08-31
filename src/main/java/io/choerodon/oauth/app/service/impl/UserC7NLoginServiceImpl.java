package io.choerodon.oauth.app.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.Receiver;
import org.hzero.common.HZeroService;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.captcha.CaptchaResult;
import org.hzero.core.message.MessageAccessor;
import org.hzero.core.user.UserType;
import org.hzero.oauth.domain.entity.User;
import org.hzero.oauth.domain.repository.UserRepository;
import org.hzero.oauth.domain.service.impl.UserLoginServiceImpl;
import org.hzero.oauth.security.exception.LoginExceptions;
import org.hzero.oauth.security.util.LoginUtil;
import org.hzero.starter.captcha.domain.core.pre.CaptchaPreResult;
import org.hzero.starter.captcha.domain.sms.pre.SmsPreResult;
import org.hzero.starter.captcha.infra.builder.CaptchaBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import io.choerodon.oauth.infra.dto.UserE;
import io.choerodon.oauth.infra.mapper.UserMapper;


/**
 * Created by wangxiang on 2021/8/17
 */
public class UserC7NLoginServiceImpl extends UserLoginServiceImpl {

    private static final String LDAP_PHONE_ERROR_MSG = "ldap.users.please.log.in.with.an.account";

    private static final String PHONE_IS_NOT_BIND = "phone.is.not.bind";

    private static final String SMS_MESSAGE_CODE = "SMS_CAPTCHA_NOTICE";


    @Autowired
    private MessageClient messageClient;


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取验证码
     *
     * @param internationalTelCode
     * @param phone
     * @param userType
     * @param businessScope
     * @param checkRegistered
     * @return
     */
    @Override
    public CaptchaPreResult<?> sendPhoneCaptchaNew(String internationalTelCode, String phone, UserType userType, String businessScope, boolean checkRegistered) {
        CaptchaPreResult<?> captchaPreResult = null;
        if (checkRegistered) {
            User user = userRepository.selectLoginUserByPhone(phone, userType);
            if (Objects.isNull(user)) {
                return SmsPreResult.failure(MessageAccessor.getMessage(LoginExceptions.PHONE_NOT_FOUND.value(), LoginUtil.getLanguageLocale()).desc());
            } else {
                //判断是不是ldap用户
                if (user.getLdap()) {
                    return SmsPreResult.failure(MessageAccessor.getMessage(LDAP_PHONE_ERROR_MSG, LoginUtil.getLanguageLocale()).desc());
                }
                UserE userE = userMapper.selectByPrimaryKey(user.getId());
                if (!userE.getPhoneBind()) {
                    return SmsPreResult.failure(MessageAccessor.getMessage(PHONE_IS_NOT_BIND, LoginUtil.getLanguageLocale()).desc());
                }

            }
        }

        // 获取验证码
        captchaPreResult = CaptchaBuilder.Pre.Sms.of(phone)
                .setPrefix(HZeroService.Oauth.CODE)
                .setCrownCode(internationalTelCode)
                .setUserType(userType.value())
                .setBusinessScope(businessScope)
                .execute();

        if (captchaPreResult.isFailure()) {
            captchaPreResult.clearCaptcha();
            return captchaPreResult;
        }

        Map<String, String> params = new HashMap<>(2);
        params.put("code", captchaPreResult.getCaptcha());
        try {
            messageClient.async().sendMessage(BaseConstants.DEFAULT_TENANT_ID, SMS_MESSAGE_CODE, null,
                    Collections.singletonList(new Receiver().setPhone(phone).setIdd(internationalTelCode)), params, Collections.singletonList("SMS"));
        } catch (Exception e) {
            // 消息发送异常
            captchaPreResult = SmsPreResult.failure(MessageAccessor.getMessage("hoth.warn.captcha.sendPhoneCaptchaError", LoginUtil.getLanguageLocale()).desc());
        }

        captchaPreResult.clearCaptcha();

        return captchaPreResult;
    }
}
