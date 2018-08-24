package io.choerodon.oauth.app.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.event.producer.execute.EventProducerTemplate;
import io.choerodon.oauth.api.dto.MessageDTO;
import io.choerodon.oauth.api.service.UserService;
import io.choerodon.oauth.app.service.PasswordForgetService;
import io.choerodon.oauth.core.password.PasswordPolicyManager;
import io.choerodon.oauth.core.password.domain.BasePasswordPolicyDO;
import io.choerodon.oauth.core.password.domain.BaseUserDO;
import io.choerodon.oauth.core.password.mapper.BasePasswordPolicyMapper;
import io.choerodon.oauth.core.password.record.PasswordRecord;
import io.choerodon.oauth.domain.entity.UserE;
import io.choerodon.oauth.infra.dataobject.NotifyToken;
import io.choerodon.oauth.infra.enums.SourceType;
import io.choerodon.oauth.infra.enums.TokenType;
import io.choerodon.oauth.infra.feign.NotificationFeign;

/**
 * @author wuguokai
 */
@Service
public class PasswordForgetServiceImpl implements PasswordForgetService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordForgetServiceImpl.class);
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
    @Value("${spring.application.name:oauth-server}")
    private String serviceName;
    @Value("${choerodon.reset-password.check:false}")
    private Boolean checkPassword;
    private EventProducerTemplate producerTemplate;
    private NotificationFeign notificationFeign;
    private UserService userService;
    private BasePasswordPolicyMapper basePasswordPolicyMapper;
    private PasswordPolicyManager passwordPolicyManager;
    private PasswordRecord passwordRecord;
    private MessageSource messageSource;
    private Locale currentLocale = LocaleContextHolder.getLocale();


    public PasswordForgetServiceImpl(
            EventProducerTemplate producerTemplate,
            NotificationFeign notificationFeign,
            UserService userService,
            BasePasswordPolicyMapper basePasswordPolicyMapper,
            PasswordPolicyManager passwordPolicyManager,
            PasswordRecord passwordRecord, MessageSource messageSource) {
        this.producerTemplate = producerTemplate;
        this.notificationFeign = notificationFeign;
        this.userService = userService;
        this.basePasswordPolicyMapper = basePasswordPolicyMapper;
        this.passwordPolicyManager = passwordPolicyManager;
        this.passwordRecord = passwordRecord;
        this.messageSource = messageSource;
    }

    @Override
    public ResponseEntity<Map<String, String>> checkMailCode(HttpSession session, String emailAddress, String captchaCode, String captcha) {
        String msg;
        if (captcha == null) {
            msg = "captcha.null";
            Map<String, String> map = new HashMap<>();
            map.put("code", messageSource.getMessage(msg, null, currentLocale));
            return new ResponseEntity(map, HttpStatus.BAD_REQUEST);
        } else if (!captcha.equals(captchaCode)) {
            msg = "captcha.wrong";
            Map<String, String> map = new HashMap<>();
            map.put("code", messageSource.getMessage(msg, null, currentLocale));
            return new ResponseEntity(map, HttpStatus.BAD_REQUEST);
        } else {
            //判断邮箱是否有
            if (emailAddress != null && !"".equals(emailAddress)) {
                UserE user = userService.queryByLoginField(emailAddress);
                if (user != null) {
                    if (user.getLdap()) {
                        msg = "email.ldap";
                        Map<String, String> map = new HashMap<>();
                        map.put("email", messageSource.getMessage(msg, null, currentLocale));
                        return new ResponseEntity(map, HttpStatus.BAD_REQUEST);
                    } else {
                        //跳转到第二个页面
                        Map<String, String> map = new HashMap<>();
                        map.put("emailAddress", emailAddress);
                        session.setAttribute("userId", user.getId());
                        session.removeAttribute("captchaCode");
                        return new ResponseEntity(map, HttpStatus.OK);
                    }
                } else {
                    msg = "email.unexist";
                    Map<String, String> map = new HashMap<>();
                    map.put("email", messageSource.getMessage(msg, null, currentLocale));
                    return new ResponseEntity(map, HttpStatus.BAD_REQUEST);
                }
            } else {
                msg = "email.null";
                Map<String, String> map = new HashMap<>();
                map.put("email", messageSource.getMessage(msg, null, currentLocale));
                return new ResponseEntity(map, HttpStatus.BAD_REQUEST);
            }
        }
    }

    @Override
    public ResponseEntity<Boolean> sendNotifyToken(HttpServletRequest request, String emailAddress) {
        NotifyToken notifyToken = notificationFeign.createNotifyToken(emailAddress, SourceType.EMAIL).getBody();
        if (notifyToken != null) {
            request.getSession().setAttribute("notifyTokenId", notifyToken.getId());
            //DONE 发送邮件事件
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setType(SourceType.EMAIL);
            messageDTO.setReceiveAccount(emailAddress);
            messageDTO.setTemplateCode("verify_code_email");
            Map<String, Object> map = new HashMap<>();
            map.put("code", notifyToken.getToken());
            messageDTO.setParams(map);
            Exception result = producerTemplate.execute(TokenType.RESET_DROWSSAP, TokenType.RESET_DROWSSAP,
                    serviceName, messageDTO, (String uuid) -> {
                    });
            if (result != null) {
                return new ResponseEntity<>(false, HttpStatus.OK);
            }
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.OK);
    }

    @Override
    public Boolean reset(Long userId, String password) {
        //DONE 重置密码
        UserE userE = userService.queryByPrimaryKey(userId);
        if (userE == null) {
            return false;
        }
        //密码策略校验
        if (checkPassword) {
            try {
                BaseUserDO baseUserDO = new BaseUserDO();
                BeanUtils.copyProperties(userE, baseUserDO);
                baseUserDO.setPassword(password);
                BasePasswordPolicyDO basePasswordPolicyDO
                        = basePasswordPolicyMapper.selectByPrimaryKey(basePasswordPolicyMapper.findByOrgId(userE.getOrganizationId()));
                passwordPolicyManager.passwordValidate(password, baseUserDO, basePasswordPolicyDO);
            } catch (CommonException e) {
                LOGGER.error(e.getMessage());
                return false;
            }
        }
        userE.setPassword(ENCODER.encode(password));
        userE.setLastPasswordUpdatedAt(new Date());
        userE = userService.updateSelective(userE);
        if (userE != null) {
            passwordRecord.updatePassword(userId, ENCODER.encode(password));
            return true;
        }
        return false;
    }
}