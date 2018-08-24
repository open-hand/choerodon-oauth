package io.choerodon.oauth.infra.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import io.choerodon.oauth.api.service.LdapService;
import io.choerodon.oauth.api.service.OrganizationService;
import io.choerodon.oauth.api.service.UserService;
import io.choerodon.oauth.core.password.PasswordPolicyManager;
import io.choerodon.oauth.core.password.PasswordPolicyType;
import io.choerodon.oauth.core.password.domain.BasePasswordPolicyDO;
import io.choerodon.oauth.core.password.domain.BaseUserDO;
import io.choerodon.oauth.core.password.mapper.BasePasswordPolicyMapper;
import io.choerodon.oauth.core.password.record.PasswordRecord;
import io.choerodon.oauth.core.password.service.BaseUserService;
import io.choerodon.oauth.domain.entity.LdapE;
import io.choerodon.oauth.domain.entity.OrganizationE;
import io.choerodon.oauth.domain.entity.UserE;
import io.choerodon.oauth.infra.common.util.ldap.LdapUtil;
import io.choerodon.oauth.infra.enums.LoginExceptions;
import io.choerodon.oauth.infra.exception.CustomAuthenticationException;
import io.choerodon.oauth.infra.mapper.OrganizationMapper;

/**
 * @author wuguokai
 */
@Service
public class ChoerodonAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private static final String DATA_FORMAT = "MM月dd日 HH:mm";
    private static final Logger logger = LoggerFactory.getLogger(ChoerodonAuthenticationProvider.class);
    @Value("${spring.application.name:oauth-server}")
    private String serviceName;
    @Autowired
    private CustomUserDetailsServiceImpl userDetailsService;
    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private BasePasswordPolicyMapper basePasswordPolicyMapper;
    @Autowired
    private BaseUserService baseUserService;
    @Autowired
    private PasswordRecord passwordRecord;
    @Autowired
    private PasswordPolicyManager passwordPolicyManager;
    @Autowired
    private LdapService ldapService;
    @Autowired
    private UserService userService;

    @Override
    protected UserDetails retrieveUser(
            String username,
            UsernamePasswordAuthenticationToken authentication) {
        //获取当前登录用户信息
        UserE user = userService.queryByLoginField(username);
        if (user == null) {
            throw new AuthenticationServiceException(LoginExceptions.USERNAME_NOT_FOUND_OR_PASSWORD_IS_WRONG.value());
        }
        checkOrganization(user.getOrganizationId());

        BasePasswordPolicyDO passwordPolicy = basePasswordPolicyMapper.findByOrgId(user.getOrganizationId());
        //登录认证策略
        BaseUserDO baseUserDO = new BaseUserDO();
        BeanUtils.copyProperties(user, baseUserDO);
        Map returnMap = passwordPolicyManager.loginValidate("password", baseUserDO, passwordPolicy);
        Object lock = null;
        if (returnMap != null) {
            lock = returnMap.get(PasswordPolicyType.MAX_ERROR_TIME.getValue());
        }
        if (lock != null && !((Boolean) lock)
                && (user.getLocked() == null || (user.getLocked() != null && !user.getLocked()))) {
            //DONE 锁定用户
            Integer lockExpireTime = passwordPolicy.getLockedExpireTime();
            logger.info("begin lock user, userId is: {} ", baseUserDO.getId());
            baseUserService.lockUser(baseUserDO.getId(), lockExpireTime);
            user = userService.queryByLoginField(username);
        }
        if (!user.getEnabled()) {
            throw new AuthenticationServiceException(LoginExceptions.USER_IS_NOT_ACTIVATED.value());
        }
        //判断用户是否被锁
        long nowTime = System.currentTimeMillis();
        if (user.getLocked() != null && user.getLocked()) {
            Date lockDate = user.getLockedUntilAt();
            if (lockDate != null) {
                Long lockUntilTime = lockDate.getTime();
                if (lockUntilTime > nowTime) {
                    //账号处于被锁期间,返回登录页面
                    throw new CustomAuthenticationException(LoginExceptions.ACCOUNT_IS_LOCKED.value(),
                            new SimpleDateFormat(DATA_FORMAT).format(lockDate));
                } else {
                    //给用户解锁
                    user.setLocked(false);
                    userService.updateSelective(user);
                    passwordRecord.unLockUser(user.getId());
                }
            }
        }
        return userDetailsService.loadUserByUsername(username);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) {
        String rawPassword = new String(PasswordDecode.decode((String) authentication.getCredentials()));
        String username = userDetails.getUsername();
        String passWd = userDetails.getPassword();

        if (authentication.getDetails() instanceof CustomWebAuthenticationDetails) {
            CustomWebAuthenticationDetails details = (CustomWebAuthenticationDetails)
                    authentication.getDetails();
            String captchaCode = details.getCaptchaCode();
            String captcha = details.getCaptcha();
            UserE user = userService.queryByLoginField(username);
            BasePasswordPolicyDO passwordPolicy = basePasswordPolicyMapper.findByOrgId(user.getOrganizationId());
            BaseUserDO baseUserDO = new BaseUserDO();
            BeanUtils.copyProperties(user, baseUserDO);
            if (passwordPolicyManager.isNeedCaptcha(passwordPolicy, baseUserDO)) {
                if (captchaCode == null || captcha == null || "".equals(captcha)) {
                    throw new AuthenticationServiceException(LoginExceptions.CAPTCHA_IS_NULL.value());
                } else if (!captchaCode.equalsIgnoreCase(captcha)) {
                    throw new AuthenticationServiceException(LoginExceptions.CAPTCHA_IS_WRONG.value());
                }
            }
        }
        checkPassword(username, rawPassword, passWd);
    }

    private void checkPassword(String loginName, String credentials, String userPassword) {
        boolean isPass;
        UserE user = userService.queryByLoginField(loginName);
        if (user.getLdap()) {
            LdapE ldap = ldapService.queryByOrgId(user.getOrganizationId());
            //ldap登陆，如果ldap停用或者不存在，则返回false，登录失败
            if (ldap != null && ldap.getEnabled()) {
                isPass = LdapUtil.authenticate(loginName, credentials, ldap) != null;
            } else {
                isPass = false;
            }
        } else {
            BCryptPasswordEncoder encode = new BCryptPasswordEncoder();
            isPass = encode.matches(credentials, userPassword);
        }
        if (isPass) {
            return;
        }
        throw new AuthenticationServiceException(LoginExceptions.USERNAME_NOT_FOUND_OR_PASSWORD_IS_WRONG.value());
    }


    private void checkOrganization(Long orgId) {
        OrganizationE organization = organizationService.queryOrganizationById(orgId);
        if (null == organization) {
            throw new AuthenticationServiceException(LoginExceptions.ORGANIZATION_NOT_EXIST.value());
        }
        if (false == organization.getEnabled()) {
            throw new AuthenticationServiceException(LoginExceptions.ORGANIZATION_NOT_ENABLE.value());
        }
    }
}