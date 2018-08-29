package io.choerodon.oauth.api.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.oauth.api.dto.EmailSendDTO;
import io.choerodon.oauth.api.service.PasswordForgetService;
import io.choerodon.oauth.api.service.UserService;
import io.choerodon.oauth.core.password.PasswordPolicyManager;
import io.choerodon.oauth.core.password.domain.BasePasswordPolicyDO;
import io.choerodon.oauth.core.password.domain.BaseUserDO;
import io.choerodon.oauth.core.password.mapper.BasePasswordPolicyMapper;
import io.choerodon.oauth.core.password.record.PasswordRecord;
import io.choerodon.oauth.domain.entity.UserE;
import io.choerodon.oauth.infra.common.util.RedisTokenUtil;
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
    public Boolean send(String email, String loginName) {

        String token = redisTokenUtil.createShortToken();
        Map<String, Object> variables = new HashMap<>();

        variables.put("userName", loginName);
        variables.put("verifyCode", redisTokenUtil.store(RedisTokenUtil.SHORT_CODE, email, token));
        EmailSendDTO emailSendDTO = new EmailSendDTO("forgetPassword", email, variables);
        try {
            notifyFeignClient.postEmail(emailSendDTO);
            return true;
        } catch (CommonException e) {
            return false;
        }

    }

    @Override
    public Boolean check(String email, String captcha) {
        return redisTokenUtil.check(RedisTokenUtil.SHORT_CODE, email, captcha);
    }

    @Override
    public Boolean reset(UserE user, String captcha, String password) {
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
            return false;
        }
        user.setPassword(ENCODER.encode(password));
        UserE userE = userService.updateSelective(user);
        if (userE != null) {
            passwordRecord.updatePassword(user.getId(), ENCODER.encode(password));
            return true;
        }
        return false;
    }
}