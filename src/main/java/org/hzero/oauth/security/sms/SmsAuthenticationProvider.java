package org.hzero.oauth.security.sms;

import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.hzero.common.HZeroService;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.hzero.core.user.UserType;
import org.hzero.oauth.domain.entity.User;
import org.hzero.oauth.security.constant.LoginType;
import org.hzero.oauth.security.exception.AccountNotExistsException;
import org.hzero.oauth.security.exception.CustomAuthenticationException;
import org.hzero.oauth.security.exception.ErrorWithTimesException;
import org.hzero.oauth.security.exception.LoginExceptions;
import org.hzero.oauth.security.service.LoginRecordService;
import org.hzero.oauth.security.service.UserAccountService;
import org.hzero.starter.captcha.domain.core.repository.RedisCaptchaRepository;
import org.hzero.starter.captcha.domain.sms.valid.SmsValidResult;
import org.hzero.starter.captcha.infra.builder.CaptchaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * 短信登录认证器
 * <p>
 * 参考 {@link AbstractUserDetailsAuthenticationProvider}，{@link DaoAuthenticationProvider}
 *
 * @author bojiangzhou 2019/02/25
 */
public class SmsAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsAuthenticationProvider.class);

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    private final UserDetailsService userDetailsService;
    private final UserAccountService userAccountService;
    private final LoginRecordService loginRecordService;
    @Autowired
    private RedisHelper redisHelper;

    public SmsAuthenticationProvider(UserDetailsService userDetailsService,
                                     UserAccountService userAccountService,
                                     LoginRecordService loginRecordService) {
        this.userDetailsService = userDetailsService;
        this.userAccountService = userAccountService;
        this.loginRecordService = loginRecordService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(SmsAuthenticationToken.class, authentication,
                "Only SmsAuthenticationToken is supported");

        String mobile = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();

        authenticationVerify((SmsAuthenticationToken) authentication);

        UserDetails user = retrieveUser(mobile, (SmsAuthenticationToken) authentication);
        Assert.notNull(user, "retrieveUser returned null - a violation of the interface contract");

        additionalAuthenticationChecks(user, (SmsAuthenticationToken) authentication);

        return createSuccessAuthentication(user, authentication, user);
    }

    protected UserDetails retrieveUser(String mobile, SmsAuthenticationToken authentication)
            throws AuthenticationException {
        // 获取当前登录用户信息
        User user = userAccountService.findLoginUser(LoginType.SMS, mobile, getUserType(authentication));
        if (user == null) {
            LOGGER.info("Sms authentication failure, user not found by mobile: [{}]", mobile);
            throw new AccountNotExistsException(LoginExceptions.LOGIN_MOBILE_CAPTCHA_NULL.value());
        }

        loginRecordService.saveLocalLoginUser(user);

        // 校验用户账户有效性
        userAccountService.checkLoginUser(user);

        return getUserDetailsService().loadUserByUsername(mobile);
    }

    protected void authenticationVerify(SmsAuthenticationToken authentication) throws AuthenticationException {
        Assert.isInstanceOf(SmsAuthenticationDetails.class, authentication.getDetails());
        SmsAuthenticationDetails details = (SmsAuthenticationDetails) authentication.getDetails();
        String mobile = (authentication.getPrincipal() == null) ? "NONE_PROVIDED" : authentication.getName();
        // 检查验证码
        String inputCaptcha = details.getInputCaptcha();
        String captchaKey = details.getCaptchaKey();
        String businessScope = details.getBusinessScope();
        if (StringUtils.isEmpty(inputCaptcha)) {
            LOGGER.info("Sms authentication failure, captcha incorrect. mobile: [{}], captcha: [{}]", mobile, inputCaptcha);
            throw new CustomAuthenticationException(LoginExceptions.LOGIN_MOBILE_CAPTCHA_NULL.value());
        }
        if (StringUtils.isEmpty(captchaKey)) {
            boolean isPhoneSendCode = false;
            //把已经发送验证码的手机号全给找出来
            Set<String> keys = redisHelper.keys("hoth:captcha:p:default:captcha:*");
            if (CollectionUtils.isEmpty(keys)) {
                throw new CustomAuthenticationException("please.get.captcha.key.first");
            }
            for (String key : keys) {
                String phoneAndCode = redisHelper.strGet(key);
                if (!StringUtils.isEmpty(phoneAndCode)) {
                    String[] split = phoneAndCode.split(BaseConstants.Symbol.LOWER_LINE);
                    if (StringUtils.equalsIgnoreCase(mobile, split[0])) {
                        isPhoneSendCode = true;
                    }
                }
            }
            if (!isPhoneSendCode) {
                throw new CustomAuthenticationException("please.get.captcha.key.first");
            }
        }

        // 短信校验
        SmsValidResult smsValidResult = CaptchaBuilder.Valid.Sms.of(captchaKey, inputCaptcha)
                .setPrefix(HZeroService.Oauth.CODE)
                .setUserType(getUserType(authentication).value())
                .setBusinessScope(businessScope)
                .setCheckMobile(mobile)
                .execute();

        if (smsValidResult.isFailure()) {
            ErrorWithTimesException ex = new ErrorWithTimesException(smsValidResult.getMessage());
            ex.setErrorTimes(smsValidResult.getErrorTimes());
            ex.setSurplusTimes(smsValidResult.getSurplusTimes());
            throw ex;
        }
    }

    protected void additionalAuthenticationChecks(UserDetails userDetails, SmsAuthenticationToken authentication)
            throws AuthenticationException {

    }

    private UserType getUserType(SmsAuthenticationToken authentication) {
        String userType = null;
        Object details = authentication.getDetails();
        if (details instanceof SmsAuthenticationDetails) {
            userType = ((SmsAuthenticationDetails) details).getUserType();
        } else if (details instanceof Map) {
            userType = (String) ((Map) details).get(UserType.PARAM_NAME);
        }
        return UserType.ofDefault(userType);
    }

    protected Authentication createSuccessAuthentication(Object principal, Authentication authentication, UserDetails user) {
        // 返回认证结果
        SmsAuthenticationToken result = new SmsAuthenticationToken(principal, authoritiesMapper.mapAuthorities(user.getAuthorities()));
        result.setDetails(authentication.getDetails());
        return result;
    }

    /**
     * 只有 {@link SmsAuthenticationToken} 类型才使用该认证器
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return (SmsAuthenticationToken.class.isAssignableFrom(authentication));
    }

    protected UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }
}
