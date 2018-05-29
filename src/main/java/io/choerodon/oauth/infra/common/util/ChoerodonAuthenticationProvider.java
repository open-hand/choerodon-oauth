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

import io.choerodon.oauth.app.service.LdapService;
import io.choerodon.oauth.core.password.PasswordPolicyManager;
import io.choerodon.oauth.core.password.PasswordPolicyType;
import io.choerodon.oauth.core.password.domain.BasePasswordPolicyDO;
import io.choerodon.oauth.core.password.domain.BaseUserDO;
import io.choerodon.oauth.core.password.mapper.BasePasswordPolicyMapper;
import io.choerodon.oauth.core.password.record.PasswordRecord;
import io.choerodon.oauth.core.password.service.BaseUserService;
import io.choerodon.oauth.domain.repository.OrganizationRepository;
import io.choerodon.oauth.domain.service.IUserService;
import io.choerodon.oauth.infra.common.util.ldap.LdapUtil;
import io.choerodon.oauth.infra.dataobject.LdapDO;
import io.choerodon.oauth.infra.dataobject.OrganizationDO;
import io.choerodon.oauth.infra.dataobject.UserDO;
import io.choerodon.oauth.infra.exception.CustomAuthenticationException;

/**
 * @author wuguokai
 */
@Service
public class ChoerodonAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private static final String DATA_FORMAT = "MM月dd日 HH:mm";
    private static final Logger LOGGER = LoggerFactory.getLogger(ChoerodonAuthenticationProvider.class);
    @Value("${spring.application.name:oauth-server}")
    private String serviceName;
    @Autowired
    private CustomUserDetailsServiceImpl userDetailsService;
    @Autowired
    private OrganizationRepository organizationRepository;
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
    private IUserService userService;

    @Override
    protected UserDetails retrieveUser(String username,
                                       UsernamePasswordAuthenticationToken authentication) {
        //获取当前登录用户信息
        UserDO userDO = userService.findUser(username);
        if (userDO == null) {
            throw new AuthenticationServiceException("usernameNotFoundOrPasswordIsWrong");
        }
        OrganizationDO org = organizationRepository.selectByPrimaryKey(userDO.getOrganizationId());
        BasePasswordPolicyDO passwordPolicy = basePasswordPolicyMapper.findByOrgId(org.getId());
        //登录认证策略
        BaseUserDO baseUserDO = new BaseUserDO();
        BeanUtils.copyProperties(userDO, baseUserDO);
        Map returnMap = passwordPolicyManager.loginValidate("password", baseUserDO, passwordPolicy);
        Object lock = null;
        if (returnMap != null) {
            lock = returnMap.get(PasswordPolicyType.MAX_ERROR_TIME.getValue());
        }
        if (lock != null && !((Boolean) lock)
                && (userDO.getLocked() == null || (userDO.getLocked() != null && !userDO.getLocked()))) {
            //DONE 锁定用户
            Integer lockExpireTime = passwordPolicy.getLockedExpireTime();
            LOGGER.info("begin lock user, userId is: {} ", baseUserDO.getId());
            baseUserService.lockUser(baseUserDO.getId(), lockExpireTime);
            userDO = userService.findUser(username);
        }
        if (!userDO.getEnabled()) {
            throw new AuthenticationServiceException("userNotActive");
        }
        //判断用户是否被锁
        long nowTime = System.currentTimeMillis();
        if (userDO.getLocked() != null && userDO.getLocked()) {
            Date lockDate = userDO.getLockedUntilAt();
            if (lockDate != null) {
                Long lockUntilTime = lockDate.getTime();
                if (lockUntilTime > nowTime) {
                    //账号处于被锁期间,返回登录页面
                    throw new CustomAuthenticationException("accountLocked",
                            new SimpleDateFormat(DATA_FORMAT).format(lockDate));
                } else {
                    //给用户解锁
                    userDO.setLocked(false);
                    userService.updateByPrimaryKey(userDO);
                    passwordRecord.unLockUser(userDO.getId());
                }
            }
        }
        return userDetailsService.loadUserByUsername(username);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) {
        String rawPassword = (String) authentication.getCredentials();
        String username = userDetails.getUsername();
        String passWd = userDetails.getPassword();

        if (authentication.getDetails() instanceof CustomWebAuthenticationDetails) {
            CustomWebAuthenticationDetails details = (CustomWebAuthenticationDetails)
                    authentication.getDetails();
            String captchaCode = details.getCaptchaCode();
            String captcha = details.getCaptcha();
            UserDO userDO = userService.findUser(username);
            OrganizationDO org = organizationRepository.selectByPrimaryKey(userDO.getOrganizationId());
            BasePasswordPolicyDO passwordPolicy = basePasswordPolicyMapper.findByOrgId(org.getId());
            BaseUserDO baseUserDO = new BaseUserDO();
            BeanUtils.copyProperties(userDO, baseUserDO);
            if (passwordPolicyManager.isNeedCaptcha(passwordPolicy, baseUserDO)) {
                if (captchaCode == null || captcha == null || "".equals(captcha)) {
                    throw new AuthenticationServiceException("captchaNull");
                } else if (!captchaCode.equals(captcha)) {
                    throw new AuthenticationServiceException("captchaWrong");
                }
            }
        }
        checkPassword(username, rawPassword, passWd);
    }

    public void checkPassword(String loginName, String credentials, String userPassword) {
        boolean isPass;
        UserDO userDO = userService.findByLoginName(loginName);
        if (userDO.getLdap()) {
            LdapDO ldap = ldapService.queryByLoginName(loginName);
            isPass = LdapUtil.authenticate(loginName, credentials, ldap) != null;
        } else {
            BCryptPasswordEncoder encode = new BCryptPasswordEncoder();
            isPass = encode.matches(credentials, userPassword);
        }
        if (isPass) {
            return;
        }
        //通过组织过去相应密码策略
        OrganizationDO organizationDO = organizationRepository.selectByPrimaryKey(userDO.getOrganizationId());
        if (organizationDO == null) {
            throw new AuthenticationServiceException("error.organization.not.exist");
        }
        throw new AuthenticationServiceException("usernameNotFoundOrPasswordIsWrong");
    }
}