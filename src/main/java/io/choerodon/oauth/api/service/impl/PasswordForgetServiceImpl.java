package io.choerodon.oauth.api.service.impl;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.oauth.api.dto.EmailSendDTO;
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

    public PasswordForgetServiceImpl(
            UserService userService,
            BasePasswordPolicyMapper basePasswordPolicyMapper,
            PasswordPolicyManager passwordPolicyManager,
            PasswordRecord passwordRecord) {
        this.userService = userService;
        this.basePasswordPolicyMapper = basePasswordPolicyMapper;
        this.passwordPolicyManager = passwordPolicyManager;
        this.passwordRecord = passwordRecord;
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

        String token = redisTokenUtil.createShortToken();
        Map<String, Object> variables = new HashMap<>();

        variables.put("userName", passwordForgetDTO.getUser().getLoginName());
        variables.put("verifyCode", redisTokenUtil.store(RedisTokenUtil.SHORT_CODE, passwordForgetDTO.getUser().getEmail(), token));
        EmailSendDTO emailSendDTO = new EmailSendDTO("forgetPassword", passwordForgetDTO.getUser().getEmail(), variables);
        try {
            notifyFeignClient.postEmail(emailSendDTO);
            return passwordForgetDTO;
        } catch (CommonException e) {
            passwordForgetDTO.setSuccess(false);
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
            BasePasswordPolicyDO basePasswordPolicyDO
                    = basePasswordPolicyMapper.selectByPrimaryKey(basePasswordPolicyMapper.findByOrgId(user.getOrganizationId()));
            passwordPolicyManager.passwordValidate(password, baseUserDO, basePasswordPolicyDO);
        } catch (CommonException e) {
            LOGGER.error(e.getMessage());
            passwordForgetDTO.setSuccess(false);
            return passwordForgetDTO;
        }
        user.setPassword(ENCODER.encode(password));
        UserE userE = userService.updateSelective(user);
        if (userE != null) {
            passwordRecord.updatePassword(user.getId(), ENCODER.encode(password));
            passwordForgetDTO.setSuccess(true);
            passwordForgetDTO.setUser(new UserDTO(userE.getId(), userE.getLoginName(), user.getEmail()));
            return passwordForgetDTO;
        }

        return new PasswordForgetDTO(false);
    }
}