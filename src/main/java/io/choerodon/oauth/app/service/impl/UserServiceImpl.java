package io.choerodon.oauth.app.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import io.choerodon.core.exception.CommonException;

import io.choerodon.oauth.api.validator.UserValidator;
import io.choerodon.oauth.api.vo.BindReMsgVO;
import io.choerodon.oauth.app.service.UserService;
import io.choerodon.oauth.infra.dto.UserE;
import io.choerodon.oauth.infra.dto.UserInfoE;
import io.choerodon.oauth.infra.mapper.UserInfoMapper;
import io.choerodon.oauth.infra.mapper.UserMapper;

import org.apache.commons.lang3.StringUtils;
import org.hzero.boot.oauth.domain.entity.BaseUserInfo;
import org.hzero.boot.oauth.infra.mapper.BaseUserInfoMapper;
import org.hzero.boot.oauth.util.CustomBCryptPasswordEncoder;
import org.hzero.common.HZeroService;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.hzero.core.user.PlatformUserType;
import org.hzero.core.user.UserType;
import org.hzero.core.util.AssertUtils;
import org.hzero.oauth.domain.entity.User;
import org.hzero.oauth.domain.repository.UserRepository;
import org.hzero.oauth.infra.encrypt.EncryptClient;
import org.hzero.oauth.security.custom.CustomAuthenticationProvider;
import org.hzero.oauth.security.exception.CustomAuthenticationException;
import org.hzero.oauth.security.exception.ErrorWithTimesException;
import org.hzero.oauth.security.exception.LoginExceptions;
import org.hzero.oauth.security.service.LoginRecordService;
import org.hzero.oauth.security.sms.SmsAuthenticationDetails;
import org.hzero.starter.captcha.domain.sms.valid.SmsValidResult;
import org.hzero.starter.captcha.infra.builder.CaptchaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * @author dongfan117@gmail.com
 */
@Service("userService")
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${choerodon.oauth.login.field:mail,phone}")
    private String[] queryField;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private PasswordEncoder passwordEncoder = new CustomBCryptPasswordEncoder();
    @Autowired
    private RedisHelper redisHelper;
    @Autowired
    private EncryptClient encryptClient;

    @Override
    public UserE queryByLoginField(String field) {
        if (field == null) {
            return null;
        }

        UserE userE = new UserE();
        userE.setLoginName(field);
        userE = userMapper.selectOne(userE);
        if (null != userE) {
            return userE;
        }
        return this.queryByEmailOrPhone(field);
    }

    @Override
    public UserE updateSelective(UserE userE) {
        if (userMapper.updateByPrimaryKeySelective(userE) != 1) {
            throw new CommonException("error.user.update");
        }
        return userMapper.selectByPrimaryKey(userE.getId());
    }

    private UserE queryByEmailOrPhone(String field) {
        UserE user = new UserE();
        if (userValidator.emailValidator(field)) {
            user.setEmail(field);
            return userMapper.selectOne(user);
        } else if (userValidator.phoneValidator(field)) {
            user.setPhone(field);
            return selectFirstEnable(user);
        }
        return null;
    }

    private UserE selectFirstEnable(UserE userE) {
        List<UserE> userES = userMapper.select(userE);
        List<UserE> enabled = userES.stream().filter(u -> u.getEnabled().equals(true)).collect(Collectors.toList());
        if (!enabled.isEmpty()) {
            return enabled.get(0);
        }
        return null;
    }

    @Override
    public UserE queryByEmail(String email) {
        UserE user = new UserE();
        user.setEmail(email);
        return userMapper.selectOne(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BindReMsgVO bindUserPhone(String phone, String inputCaptcha, String captchaKey) {
        BindReMsgVO bindReMsgVO = new BindReMsgVO();
        try {
            AssertUtils.notNull(phone, "hoth.warn.captcha.phoneNotNull");
            // 检查验证码
            validSmsCode(phone, inputCaptcha, captchaKey);
            //2.跟新数据库
            UserE userE = new UserE();
            userE.setPhone(phone);
            UserE dbUser = userMapper.selectOne(userE);
            AssertUtils.notNull(dbUser, "error.user.is.null");

            AssertUtils.isTrue(!dbUser.getLdap(), "ldap.account.not.support.binding.phone");

            UserInfoE userInfoE = userInfoMapper.selectByPrimaryKey(dbUser.getId());
            AssertUtils.isTrue(!dbUser.getLdap(), "ldap.account.not.support.binding.phone");
            if (!Objects.isNull(userInfoE)) {
                if (userInfoE.getPhoneCheckFlag().intValue() != BaseConstants.Flag.YES.intValue()) {
                    userInfoE.setPhoneCheckFlag(BaseConstants.Flag.YES);
                    userInfoMapper.updateByPrimaryKey(userInfoE);
                }
            }
            dbUser.setPhoneBind(Boolean.TRUE);
            userMapper.updateByPrimaryKey(dbUser);
            bindReMsgVO.setStatus(Boolean.TRUE);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
        return bindReMsgVO;

    }

    @Override
    public BindReMsgVO updateUserPhone(String phone, String verifyKey, String type) {
        BindReMsgVO bindReMsgVO = new BindReMsgVO();
        AssertUtils.notNull(phone, "hoth.warn.captcha.phoneNotNull");
        try {
            User user = userRepository.selectLoginUserByPhone(phone, UserType.ofDefault(UserType.DEFAULT_USER_TYPE));
            AssertUtils.notNull(user, "error.user.is.null");
            AssertUtils.isTrue(!user.getLdap(), "ldap.account.not.support.binding.phone");
            if (StringUtils.equalsIgnoreCase(type, "captcha")) {
                AssertUtils.notNull(phone, "hoth.warn.captcha.phoneNotNull");
                // 检查验证码是否校验通过
                String redisKey = redisHelper.strGet("phone:" + phone);
                if (!StringUtils.equalsIgnoreCase(redisKey, verifyKey)) {
                    throw new CommonException("hoth.warn.captchaWrong");
                }

            } else if (StringUtils.equalsIgnoreCase(type, "password")) {
                AssertUtils.notNull(phone, "hoth.warn.update.passwordNotNull");
                //校验非ldap用户的密码
                String redisKey = redisHelper.strGet("password:" + user.getLoginName());
                if (!StringUtils.equalsIgnoreCase(redisKey, verifyKey)) {
                    throw new CommonException("error.password");
                }
            } else {
                throw new CommonException("unsupported.way.to.change.mobile");
            }
            //更新进数据库
            UserE userE = new UserE();
            BeanUtils.copyProperties(user, userE);
            userE.setPhoneBind(Boolean.FALSE);
            userE.setPhone(phone);
            userMapper.updateByPrimaryKey(userE);
            bindReMsgVO.setStatus(Boolean.TRUE);

        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
        return bindReMsgVO;
    }


    @Override
    public BindReMsgVO verifyCaptcha(String phone, String captcha, String captchaKey) {
        AssertUtils.notNull(phone, "hoth.warn.captcha.phoneNotNull");
        User user = userRepository.selectLoginUserByPhone(phone, UserType.ofDefault(UserType.DEFAULT_USER_TYPE));
        AssertUtils.notNull(user, "error.user.is.null");
        AssertUtils.isTrue(!user.getLdap(), "ldap.account.not.support.binding.phone");
        BindReMsgVO bindReMsgVO = new BindReMsgVO();
        try {
            // 检查验证码
            validSmsCode(phone, captcha, captchaKey);
            bindReMsgVO.setStatus(Boolean.TRUE);
        } catch (Exception e) {
            throw new CommonException(e.getMessage());
        }
        //生成唯一的key存入redis,等成功更新完成后，删除这个值
        String verifyKey = UUID.randomUUID().toString();
        String tokenCache = "phone:" + phone;
        redisHelper.strSet(tokenCache, verifyKey);
        bindReMsgVO.setKey(verifyKey);
        return bindReMsgVO;
    }

    @Override
    public BindReMsgVO verifyPassword(String loginName, String password) {
        User user = userRepository.selectLoginUserByLoginName(loginName);
        AssertUtils.notNull(user, "error.user.is.null");
        AssertUtils.isTrue(!user.getLdap(), "ldap.account.not.support.binding.phone");
        String decrypt = encryptClient.decrypt(password);
        boolean matches = passwordEncoder.matches(decrypt, user.getPassword());
        BindReMsgVO bindReMsgVO = new BindReMsgVO();
        if (matches) {
            bindReMsgVO.setStatus(Boolean.TRUE);
            //校验成功存一个key 在redis
            String passwordKey = UUID.randomUUID().toString();
            String tokenCache = "password:" + loginName;
            redisHelper.strSet(tokenCache, passwordKey);
            bindReMsgVO.setKey(passwordKey);
        } else {
            bindReMsgVO.setStatus(Boolean.FALSE);
            bindReMsgVO.setMessage("passwordWrong");
        }
        return bindReMsgVO;
    }

    private void validSmsCode(String phone, String inputCaptcha, String captchaKey) {

        String businessScope = "";
        if (StringUtils.isAnyEmpty(inputCaptcha, captchaKey)) {
            LOGGER.info("Sms authentication failure, captcha incorrect. mobile: [{}], captcha: [{}]", phone, inputCaptcha);
            throw new CommonException("choerodon.hoth.warn.captcha.null");
        }

        // 短信校验
        SmsValidResult smsValidResult = CaptchaBuilder.Valid.Sms.of(captchaKey, inputCaptcha)
                .setPrefix(HZeroService.Oauth.CODE)
                .setUserType("P")
                .setBusinessScope(businessScope)
                .setCheckMobile(phone)
                .execute();

        if (smsValidResult.isFailure()) {
            ErrorWithTimesException ex = new ErrorWithTimesException(smsValidResult.getMessage());
            ex.setErrorTimes(smsValidResult.getErrorTimes());
            ex.setSurplusTimes(smsValidResult.getSurplusTimes());
            throw ex;
        }
    }
}
