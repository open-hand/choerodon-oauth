package io.choerodon.oauth.api.service.impl;

import java.util.*;

import io.choerodon.core.notify.NoticeSendDTO;
import io.choerodon.oauth.api.validator.UserPasswordValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import io.choerodon.core.exception.CommonException;
import io.choerodon.oauth.api.dto.PasswordForgetDTO;
import io.choerodon.oauth.api.dto.UserDTO;
import io.choerodon.oauth.api.service.PasswordForgetService;
import io.choerodon.oauth.api.service.UserService;
import io.choerodon.oauth.api.validator.UserValidator;
import io.choerodon.oauth.core.password.PasswordPolicyManager;
import io.choerodon.oauth.core.password.domain.BasePasswordPolicyDO;
import io.choerodon.oauth.core.password.domain.BaseUserDO;
import io.choerodon.oauth.core.password.mapper.BasePasswordPolicyMapper;
import io.choerodon.oauth.core.password.record.PasswordRecord;
import io.choerodon.oauth.domain.entity.UserE;
import io.choerodon.oauth.infra.common.util.RedisTokenUtil;
import io.choerodon.oauth.infra.enums.PasswordFindException;
import io.choerodon.oauth.infra.feign.NotifyFeignClient;

/**
 * @author wuguokai
 */
@Service
public class PasswordForgetServiceImpl implements PasswordForgetService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordForgetServiceImpl.class);
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
    public static final String FORGET_PASSWORD = "forgetPassword";
    public static final String MODIFY_PASSWORD = "modifyPassword";
    private UserService userService;
    private BasePasswordPolicyMapper basePasswordPolicyMapper;
    private PasswordPolicyManager passwordPolicyManager;
    private PasswordRecord passwordRecord;
    @Autowired
    private NotifyFeignClient notifyFeignClient;
    @Autowired
    private RedisTokenUtil redisTokenUtil;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private MessageSource messageSource;
    private final UserPasswordValidator userPasswordValidator;

    public PasswordForgetServiceImpl(
            UserService userService,
            BasePasswordPolicyMapper basePasswordPolicyMapper,
            PasswordPolicyManager passwordPolicyManager,
            UserPasswordValidator userPasswordValidator,
            PasswordRecord passwordRecord) {
        this.userService = userService;
        this.basePasswordPolicyMapper = basePasswordPolicyMapper;
        this.passwordPolicyManager = passwordPolicyManager;
        this.passwordRecord = passwordRecord;
        this.userPasswordValidator = userPasswordValidator;
    }

    public void setNotifyFeignClient(NotifyFeignClient notifyFeignClient) {
        this.notifyFeignClient = notifyFeignClient;
    }

    public void setRedisTokenUtil(RedisTokenUtil redisTokenUtil) {
        this.redisTokenUtil = redisTokenUtil;
    }

    public void setUserValidator(UserValidator userValidator) {
        this.userValidator = userValidator;
    }

    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public PasswordForgetDTO checkUserByEmail(String email) {
        PasswordForgetDTO passwordForgetDTO = new PasswordForgetDTO(false);
        if (!userValidator.emailValidator(email)) {
            passwordForgetDTO.setMsg(messageSource.getMessage(PasswordFindException.EMAIL_FORMAT_ILLEGAL.value(), null, Locale.ROOT));
            passwordForgetDTO.setCode(PasswordFindException.EMAIL_FORMAT_ILLEGAL.value());
            return passwordForgetDTO;
        }

        UserE user = userService.queryByEmail(email);
        if (null == user) {
            passwordForgetDTO.setMsg(messageSource.getMessage(PasswordFindException.ACCOUNT_NOT_EXIST.value(), null, Locale.ROOT));
            passwordForgetDTO.setCode(PasswordFindException.ACCOUNT_NOT_EXIST.value());
            return passwordForgetDTO;
        }

        if (user.getLdap()) {
            passwordForgetDTO.setMsg(messageSource.getMessage(PasswordFindException.LDAP_CANNOT_CHANGE_PASSWORD.value(), null, Locale.ROOT));
            passwordForgetDTO.setCode(PasswordFindException.LDAP_CANNOT_CHANGE_PASSWORD.value());
            return passwordForgetDTO;
        }

        passwordForgetDTO.setSuccess(true);
        passwordForgetDTO.setUser(new UserDTO(user.getId(), user.getLoginName(), user.getEmail()));
        return passwordForgetDTO;
    }

    @Override
    public PasswordForgetDTO send(PasswordForgetDTO passwordForgetDTO) {
        PasswordForgetDTO passwordForgetDTO1 = this.checkDisable(passwordForgetDTO.getUser().getEmail());
        if (!passwordForgetDTO1.getSuccess()) {
            return passwordForgetDTO1;
        }
        String token = redisTokenUtil.createShortToken();
        Map<String, Object> variables = new HashMap<>();

        variables.put("userName", passwordForgetDTO.getUser().getLoginName());
        variables.put("verifyCode", redisTokenUtil.store(RedisTokenUtil.SHORT_CODE, passwordForgetDTO.getUser().getEmail(), token));
        redisTokenUtil.setDisableTime(passwordForgetDTO.getUser().getEmail());
        NoticeSendDTO noticeSendDTO = new NoticeSendDTO();
        NoticeSendDTO.User user = new NoticeSendDTO.User();
        user.setEmail(passwordForgetDTO.getUser().getEmail());
        List<NoticeSendDTO.User> users = new ArrayList<>();
        users.add(user);
        noticeSendDTO.setCode(FORGET_PASSWORD);
        noticeSendDTO.setTargetUsers(users);
        noticeSendDTO.setParams(variables);
        try {
            notifyFeignClient.postNotice(noticeSendDTO);
            return passwordForgetDTO;
        } catch (CommonException e) {
            passwordForgetDTO.setSuccess(false);
            LOGGER.warn("The mail send error. {} {}", e.getCode(), e);
            return passwordForgetDTO;
        }

    }

    @Override
    public PasswordForgetDTO check(PasswordForgetDTO passwordForgetDTO, String captcha) {
        passwordForgetDTO.setSuccess(redisTokenUtil.check(
                RedisTokenUtil.SHORT_CODE,
                passwordForgetDTO.getUser().getEmail(), captcha));
        return passwordForgetDTO;
    }

    @Override
    public PasswordForgetDTO reset(PasswordForgetDTO passwordForgetDTO, String captcha, String password) {
        UserE user = userService.queryByEmail(passwordForgetDTO.getUser().getEmail());
        this.redisTokenUtil.expire(user.getEmail(), captcha);
        try {
            BaseUserDO baseUserDO = new BaseUserDO();
            BeanUtils.copyProperties(user, baseUserDO);
            baseUserDO.setPassword(password);
            BasePasswordPolicyDO basePasswordPolicyDO = new BasePasswordPolicyDO();
            basePasswordPolicyDO.setOrganizationId(user.getOrganizationId());
            basePasswordPolicyDO = basePasswordPolicyMapper.selectOne(basePasswordPolicyDO);
            passwordPolicyManager.passwordValidate(password, baseUserDO, basePasswordPolicyDO);
            userPasswordValidator.validate(password, user.getOrganizationId(), true);
        } catch (CommonException e) {
            LOGGER.error(e.getMessage());
            passwordForgetDTO.setSuccess(false);
            passwordForgetDTO.setMsg(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return passwordForgetDTO;
        }
        user.setPassword(ENCODER.encode(password));
        UserE userE = userService.updateSelective(user);
        if (userE != null) {
            passwordRecord.updatePassword(user.getId(), ENCODER.encode(password));
            passwordForgetDTO.setSuccess(true);
            passwordForgetDTO.setUser(new UserDTO(userE.getId(), userE.getLoginName(), user.getEmail()));

            this.sendSiteMsg(user.getId(), user.getRealName());
            return passwordForgetDTO;
        }

        return new PasswordForgetDTO(false);
    }

    @Override
    public PasswordForgetDTO checkDisable(String email) {
        Long time = this.redisTokenUtil.getDisableTime(email);

        PasswordForgetDTO passwordForgetDTO = new PasswordForgetDTO();

        if (time != null) {
            passwordForgetDTO.setSuccess(false);
            passwordForgetDTO.setDisableTime(time);
            passwordForgetDTO.setMsg(messageSource.getMessage(PasswordFindException.DISABLE_SEND.value(), null, Locale.ROOT));
            passwordForgetDTO.setCode(PasswordFindException.DISABLE_SEND.value());
        }
        return passwordForgetDTO;
    }

    private void sendSiteMsg(Long userId, String userName) {
        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("userName", userName);
        NoticeSendDTO noticeSendDTO = new NoticeSendDTO();
        NoticeSendDTO.User user = new NoticeSendDTO.User();
        user.setId(userId);
        List<NoticeSendDTO.User> users = new ArrayList<>();
        users.add(user);
        noticeSendDTO.setCode(MODIFY_PASSWORD);
        noticeSendDTO.setTargetUsers(users);
        try {
            notifyFeignClient.postNotice(noticeSendDTO);
        } catch (CommonException e) {
            LOGGER.warn("The site msg send error. {} {}", e.getCode(), e);
        }
    }
}