package io.choerodon.oauth.app.service.impl;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.hzero.boot.message.MessageClient;
import org.hzero.boot.message.entity.MessageSender;
import org.hzero.boot.message.entity.Receiver;
import org.hzero.boot.oauth.domain.entity.BaseUser;
import org.hzero.boot.oauth.domain.service.UserPasswordService;
import org.hzero.boot.oauth.policy.PasswordPolicyManager;
import org.hzero.core.message.MessageAccessor;
import org.hzero.core.user.UserType;
import org.hzero.oauth.domain.entity.User;
import org.hzero.oauth.domain.repository.UserRepository;
import org.hzero.oauth.infra.encrypt.EncryptClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import io.choerodon.core.exception.CommonException;
import io.choerodon.oauth.api.validator.UserValidator;
import io.choerodon.oauth.api.vo.PasswordForgetDTO;
import io.choerodon.oauth.app.service.PasswordForgetService;
import io.choerodon.oauth.app.service.UserService;
import io.choerodon.oauth.infra.enums.PasswordFindException;
import io.choerodon.oauth.infra.util.RedisTokenUtil;

/**
 * @author wuguokai
 */
@Service
public class PasswordForgetServiceImpl implements PasswordForgetService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordForgetServiceImpl.class);
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
    public static final String FORGET = "forgetPassword";   // 忘记密码消息code
    public static final String MODIFY = "modifyPassword";   // 修改密码消息code
    public static final String RESET_PAGE = "/oauth/choerodon/password/reset_page";
    private UserService userService;
    private PasswordPolicyManager passwordPolicyManager;

    @Autowired
    private UserPasswordService userPasswordService;
    @Value("${hzero.gateway.url: http://api.example.com}")
    private String gatewayUrl;
    @Value("${hzero.reset-password.resetUrlExpireMinutes: 10}")
    private Long resetUrlExpireMinutes;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    protected MessageClient messageClient;
    @Autowired
    private RedisTokenUtil redisTokenUtil;
    @Autowired
    private UserValidator userValidator;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EncryptClient encryptClient;

    public PasswordForgetServiceImpl(
            UserService userService,
            PasswordPolicyManager passwordPolicyManager) {
        this.userService = userService;
        this.passwordPolicyManager = passwordPolicyManager;
    }

    @Override
    public PasswordForgetDTO checkUserByEmail(String email) {
        PasswordForgetDTO passwordForgetDTO = new PasswordForgetDTO(false);
        if (!userValidator.emailValidator(email)) {
            passwordForgetDTO.setMsg(MessageAccessor.getMessage(PasswordFindException.EMAIL_FORMAT_ILLEGAL.value()).desc());
            passwordForgetDTO.setCode(PasswordFindException.EMAIL_FORMAT_ILLEGAL.value());
            return passwordForgetDTO;
        }
        User user = userRepository.selectLoginUserByEmail(email, UserType.ofDefault());
        if (null == user) {
            passwordForgetDTO.setMsg(MessageAccessor.getMessage(PasswordFindException.ACCOUNT_NOT_EXIST.value()).desc());
            passwordForgetDTO.setCode(PasswordFindException.ACCOUNT_NOT_EXIST.value());
            return passwordForgetDTO;
        }

        if (Boolean.TRUE.equals(user.getLdap())) {
            passwordForgetDTO.setMsg(MessageAccessor.getMessage(PasswordFindException.LDAP_CANNOT_CHANGE_PASSWORD.value()).desc());
            passwordForgetDTO.setCode(PasswordFindException.LDAP_CANNOT_CHANGE_PASSWORD.value());
            return passwordForgetDTO;
        }

        passwordForgetDTO.setSuccess(true);
        passwordForgetDTO.setUser(user);
        return passwordForgetDTO;
    }

    @Override
    public PasswordForgetDTO checkDisable(String email) {
        Long time = this.redisTokenUtil.getDisableTime(email);

        PasswordForgetDTO passwordForgetDTO = new PasswordForgetDTO();

        if (time != null) {
            passwordForgetDTO.setSuccess(false);
            passwordForgetDTO.setDisableTime(time);
            passwordForgetDTO.setMsg(MessageAccessor.getMessage(PasswordFindException.DISABLE_SEND.value()).desc());
            passwordForgetDTO.setCode(PasswordFindException.DISABLE_SEND.value());
        }
        return passwordForgetDTO;
    }

    @Override
    public PasswordForgetDTO sendResetEmail(String email) {
        String keyType = RedisTokenUtil.SHORT_CODE;
        // 校验邮箱
        PasswordForgetDTO passwordForgetDTO = checkUserByEmail(email);
        if (Boolean.FALSE.equals(passwordForgetDTO.getSuccess())) {
            return passwordForgetDTO;
        }
        // 校验60秒内是否发送过邮件
        PasswordForgetDTO passwordForgetDTO1 = this.checkDisable(passwordForgetDTO.getUser().getEmail());
        if (Boolean.FALSE.equals(passwordForgetDTO1.getSuccess())) {
            return passwordForgetDTO1;
        }

        // 如果之前发送的token未失效，则让之前发送的token失效
        String emailKey = redisTokenUtil.createKey(keyType, email);
        String tokenKey = redisTokenUtil.getValueByKey(emailKey);
        if (tokenKey != null) {
            redisTokenUtil.expireByKey(tokenKey);
        }

        // 60秒内不能重复发送邮件，记录不能发送的时间
        redisTokenUtil.setDisableTime(passwordForgetDTO.getUser().getEmail());

        tokenKey = generateTokenKey(passwordForgetDTO.getUser().getEmail());
        String redirectUrl = gatewayUrl + RESET_PAGE + "/" + tokenKey;
        // 保存token和email，保存两条记录
        // emailKey -> tokenKey
        // tokenKey -> email
        redisTokenUtil.storeByKey(emailKey, tokenKey, resetUrlExpireMinutes, TimeUnit.MINUTES);
        redisTokenUtil.storeByKey(tokenKey, email, resetUrlExpireMinutes, TimeUnit.MINUTES);

//        Map<String, Object> variables = new HashMap<>();
//        variables.put("userName", passwordForgetDTO.getUser().getLoginName());
//        variables.put("redirectUrl", redirectUrl);


//        NoticeSendDTO noticeSendDTO = new NoticeSendDTO();
//        NoticeSendDTO.User user = new NoticeSendDTO.User();
//        user.setEmail(passwordForgetDTO.getUser().getEmail());
//        List<NoticeSendDTO.User> users = new ArrayList<>();
//        users.add(user);
//        noticeSendDTO.setCode(FORGET);
//        noticeSendDTO.setTargetUsers(users);
//        noticeSendDTO.setParams(variables);
        MessageSender messageSender=new MessageSender();
        // 消息code
        messageSender.setMessageCode(FORGET);
        // 默认为0L,都填0L,可不填写
        messageSender.setTenantId(0L);

        // 消息参数 消息模板中${projectName}
        Map<String,String> argsMap=new HashMap<>();
        String username = passwordForgetDTO.getUser().getLdap() ? passwordForgetDTO.getUser().getLoginName() : passwordForgetDTO.getUser().getEmail();
        argsMap.put("userName",  username);
        argsMap.put("redirectUrl",redirectUrl);
        messageSender.setArgs(argsMap);


        // 接收者
        List<Receiver> receiverList=new ArrayList<>();
        Receiver receiver=new Receiver();
        receiver.setUserId(passwordForgetDTO.getUser().getId());
        // 发送邮件消息时 必填
        receiver.setEmail(passwordForgetDTO.getUser().getEmail());
        // 必填
        receiver.setTargetUserTenantId(passwordForgetDTO.getUser().getTenantId());
        receiverList.add(receiver);
        messageSender.setReceiverAddressList(receiverList);

        // 发送消息
        try {
           messageClient.sendMessage(messageSender);
            return passwordForgetDTO;
        } catch (CommonException e) {
            passwordForgetDTO.setSuccess(false);
            passwordForgetDTO.setMsg("发送失败，请重试");
            LOGGER.warn("The mail send error. {} {}", e.getCode(), e);
            return passwordForgetDTO;
        }
    }

    @Override
    public boolean checkTokenAvailable(String token) {
        String value = redisTokenUtil.getValueByKey(token);
        return value != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PasswordForgetDTO resetPassword(String token, String password) {
        // 使用token，查出存在redis里的email,然后再根据email查出用户信息
        String email = getEmailByToken(token);
        User user = userRepository.selectLoginUserByEmail(email, UserType.ofDefault());
        PasswordForgetDTO passwordForgetDTO = new PasswordForgetDTO();
        String decryptPassword = encryptClient.decrypt(password);
        try {
            BaseUser baseUser = new BaseUser();
            BeanUtils.copyProperties(user, baseUser);
            baseUser.setPassword(decryptPassword);
            passwordPolicyManager.passwordValidate(decryptPassword, user.getOrganizationId(), baseUser);
        } catch (CommonException e) {
            LOGGER.error(e.getMessage());
            passwordForgetDTO.setSuccess(false);
            passwordForgetDTO.setMsg(e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return passwordForgetDTO;
        }

        if (user != null) {
            userPasswordService.updateUserPassword(user.getId(), decryptPassword);
            passwordForgetDTO.setSuccess(true);
            redisTokenUtil.expireByKey(token);
            passwordForgetDTO.setUser(user);

            this.sendSiteMsg(user);
            return passwordForgetDTO;
        }

        return new PasswordForgetDTO(false);
    }

    private String getEmailByToken(String token) {
        return redisTokenUtil.getValueByKey(token);
    }

    private String generateTokenKey(String email) {
        String token = UUID.randomUUID().toString() + passwordEncoder.encode(email);
        // 去掉特殊字符"/"
        token = token.replaceAll("\\/","");
        return token;
    }

    private void sendSiteMsg(User user) {
        MessageSender messageSender=new MessageSender();
        // 消息code
        messageSender.setMessageCode(MODIFY);
        // 默认为0L,都填0L,可不填写
        messageSender.setTenantId(0L);

        // 消息参数 消息模板中${projectName}
        Map<String,String> argsMap=new HashMap<>();
        String username = user.getLdap() ? user.getLoginName() : user.getEmail();

        argsMap.put("userName", username);
        messageSender.setArgs(argsMap);


        // 接收者
        List<Receiver> receiverList=new ArrayList<>();
        Receiver receiver=new Receiver();
        receiver.setUserId(user.getId());
        // 发送邮件消息时 必填
        receiver.setEmail(user.getEmail());
        // 必填
        receiver.setTargetUserTenantId(user.getTenantId());
        receiverList.add(receiver);
        messageSender.setReceiverAddressList(receiverList);

        // 发送消息
        try {
            messageClient.sendMessage(messageSender);
        } catch (CommonException e) {
            LOGGER.warn("The site msg send error. {} {}", e.getCode(), e);
        }
    }


    // todo delete???
    private void sendSiteMsg(Long userId, String userName) {

        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("userName", userName);
//        NoticeSendDTO noticeSendDTO = new NoticeSendDTO();
//        NoticeSendDTO.User user = new NoticeSendDTO.User();
//        user.setId(userId);
//        List<NoticeSendDTO.User> users = new ArrayList<>();
//        users.add(user);
//        noticeSendDTO.setCode(MODIFY);
//        noticeSendDTO.setTargetUsers(users);
        try {
//            notifyFeignClient.postNotice(noticeSendDTO);
        } catch (CommonException e) {
            LOGGER.warn("The site msg send error. {} {}", e.getCode(), e);
        }
    }
}