package org.hzero.oauth.security.custom;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.naming.directory.DirContext;

import org.apache.commons.lang3.StringUtils;
import org.hzero.boot.oauth.domain.entity.BaseLdap;
import org.hzero.boot.oauth.domain.entity.BasePasswordPolicy;
import org.hzero.boot.oauth.domain.repository.BaseLdapRepository;
import org.hzero.common.HZeroService;
import org.hzero.core.captcha.CaptchaImageHelper;
import org.hzero.core.user.UserType;
import org.hzero.oauth.domain.entity.User;
import org.hzero.oauth.infra.encrypt.EncryptClient;
import org.hzero.oauth.security.config.SecurityProperties;
import org.hzero.oauth.security.constant.LoginException;
import org.hzero.oauth.security.constant.LoginSource;
import org.hzero.oauth.security.constant.LoginType;
import org.hzero.oauth.security.exception.AccountNotExistsException;
import org.hzero.oauth.security.exception.CustomAuthenticationException;
import org.hzero.oauth.security.exception.ErrorWithTimesException;
import org.hzero.oauth.security.exception.LoginExceptions;
import org.hzero.oauth.security.service.LoginRecordService;
import org.hzero.oauth.security.service.UserAccountService;
import org.hzero.oauth.security.util.LoginUtil;
import org.hzero.oauth.security.util.PasswordDecode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import io.choerodon.core.ldap.DirectoryType;

public class CustomAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomAuthenticationProvider.class);
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private CustomUserDetailsService userDetailsService;
    private BaseLdapRepository baseLdapRepository;
    private UserAccountService userAccountService;
    private LoginRecordService loginRecordService;
    private CaptchaImageHelper captchaImageHelper;
    private SecurityProperties securityProperties;
    private EncryptClient encryptClient;

    public CustomAuthenticationProvider(CustomUserDetailsService userDetailsService,
                                        BaseLdapRepository baseLdapRepository,
                                        UserAccountService userAccountService,
                                        LoginRecordService loginRecordService,
                                        CaptchaImageHelper captchaImageHelper,
                                        SecurityProperties securityProperties,
                                        EncryptClient encryptClient) {
        this.userDetailsService = userDetailsService;
        this.baseLdapRepository = baseLdapRepository;
        this.userAccountService = userAccountService;
        this.loginRecordService = loginRecordService;
        this.captchaImageHelper = captchaImageHelper;
        this.securityProperties = securityProperties;
        this.encryptClient = encryptClient;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return super.supports(authentication) &&
                UsernamePasswordAuthenticationToken.class.getTypeName().equals(authentication.getTypeName());
    }

    @Override
    public UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) {
        // 获取当前登录用户信息
        String loginField = null;
        String userType = null;
        Object details = authentication.getDetails();
        User user = null;

        if (details instanceof CustomWebAuthenticationDetails) {
            LOGGER.info("->>>>CustomWebAuthenticationDetails");
            loginField = ((CustomWebAuthenticationDetails) details).getLoginField();
            userType = ((CustomWebAuthenticationDetails) details).getUserType();
        } else if (details instanceof Map) {
            LOGGER.info("->>>>Map");
            loginField = (String) ((Map) details).get(LoginUtil.FIELD_LOGIN_FIELD);
            userType = (String) ((Map) details).get(UserType.PARAM_NAME);
        }
        LOGGER.info("->>>>loginField {}",loginField);
        LOGGER.info("->>>>userType {}",userType);
        LOGGER.info("->>>>username {}",username);
        if (loginField != null) {
            LOGGER.info("->>>>loginField ！= null");
            user = userAccountService.findLoginUser(loginField, username, UserType.ofDefault(userType));
        } else {
            LOGGER.info("->>>>loginField == null");
            user = userAccountService.findLoginUser(LoginType.ACCOUNT, username, UserType.ofDefault(userType));
        }
        if (user == null) {
            throw new AccountNotExistsException(LoginExceptions.USERNAME_OR_PASSWORD_ERROR.value());
        }

        loginRecordService.saveLocalLoginUser(user);

        // 校验用户账户有效性
        userAccountService.checkLoginUser(user);

        return getUserDetailsService().loadUserByUsername(username);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) {
        checkCaptcha(userDetails, authentication);
        checkPassword(userDetails, authentication);
    }
    /**
     * 检查验证码
     */
    protected void checkCaptcha(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) {
        // Web 请求
        if (authentication.getDetails() instanceof CustomWebAuthenticationDetails) {
            CustomWebAuthenticationDetails webDetails = (CustomWebAuthenticationDetails) authentication.getDetails();

            String captchaCode = webDetails.getCacheCaptcha();
            String captcha = webDetails.getInputCaptcha();

            checkCaptcha(captcha, captchaCode);
        }
        // 接口调用
        else if (authentication.getDetails() instanceof Map) {
            Map parameters = (Map) authentication.getDetails();
            String sourceType = getParameterFromMap(parameters, LoginUtil.FIELD_SOURCE_TYPE);

            // 如果未传入 source_type 则不校验验证码，认为是标准OAuth2.0 授权方式
            if (StringUtils.isBlank(sourceType) ||
                    (LoginSource.APP.value().equalsIgnoreCase(sourceType) && !securityProperties.isEnableAppCaptcha())) {
                return;
            }
            String captcha = getParameterFromMap(parameters, LoginUtil.FIELD_CAPTCHA);
            String captchaKey = getParameterFromMap(parameters, LoginUtil.FIELD_CAPTCHA_KEY);
            String captchaCode = captchaImageHelper.getCaptcha(HZeroService.Oauth.CODE, captchaKey);

            checkCaptcha(captcha, captchaCode);
        }
    }

    private String getParameterFromMap(Map parameters, String key) {
        return Optional.ofNullable(parameters.get(key)).map(Object::toString).orElse(null);
    }

    private void checkCaptcha(String captcha, String captchaCode) {
        User user = loginRecordService.getLocalLoginUser();
        if (userAccountService.isNeedCaptcha(user)) {
            if (StringUtils.isBlank(captcha)) {
                throw new AuthenticationServiceException(LoginExceptions.CAPTCHA_NULL.value());
            }
            if (StringUtils.isBlank(captchaCode) || !StringUtils.equalsIgnoreCase(captchaCode, captcha)) {
                throw new AuthenticationServiceException(LoginExceptions.CAPTCHA_ERROR.value());
            }
        }
    }

    /**
     * 检查密码
     */
    protected void checkPassword(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) {
        String credentials = null;
        try {
            // RSA 非对称加密
            if (securityProperties.getPassword().isEnableEncrypt()) {
                credentials = encryptClient.decrypt((String) authentication.getCredentials());
            }
            // Base64 解密
            else {
                credentials = new String(PasswordDecode.decode((String) authentication.getCredentials()), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            LOGGER.error("decode password error. ex={}", e.getMessage());
            throw new AuthenticationServiceException(LoginExceptions.DECODE_PASSWORD_ERROR.value());
        }
        String loginName = userDetails.getUsername();
        String userPassword = userDetails.getPassword();
        LOGGER.info("->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>.");
        LOGGER.info("loginName", loginName);
        LOGGER.info("userPassword", userPassword);
        boolean passed;
        User user = loginRecordService.getLocalLoginUser();
        LOGGER.info("credentials", credentials);
        if (user.getLdap()) {
            passed = ldapAuthentication(user.getOrganizationId(), loginName, credentials);
        } else {
            passed = ENCODER.matches(credentials, userPassword);
            // 检查是否强制修改密码
            if (passed) {
                checkPasswordModified(user);
            }
        }
        if (passed) {
            processPasswordLoginSuccess();
            return;
        }

        processPasswordLoginError();
    }

    /**
     * 密码认证成功处理
     */
    protected void processPasswordLoginSuccess() {
        User user = loginRecordService.getLocalLoginUser();
        loginRecordService.loginSuccess(user);
    }

    /**
     * 密码认证失败处理
     */
    protected void processPasswordLoginError() {
        LOGGER.error("->>>>>>>>>>>>>>validate password failed");
        // password error
        User loginUser = loginRecordService.getLocalLoginUser();
        long residualTimes = loginRecordService.loginError(loginUser);
        // 密码错误 已锁定
        if (loginUser.getLocked()) {
            throw new CustomAuthenticationException(LoginExceptions.LOGIN_ERROR_MORE_THEN_MAX_TIME.value());
        }
        // 密码错误 返回剩余次数
        else {
            if (residualTimes == -1) {
                throw new CustomAuthenticationException(LoginExceptions.USERNAME_OR_PASSWORD_ERROR.value());
            } else {
                ErrorWithTimesException ex = new ErrorWithTimesException(LoginExceptions.PASSWORD_ERROR.value(), residualTimes);
                ex.setErrorTimes(loginRecordService.getErrorTimes(loginUser));
                ex.setSurplusTimes(residualTimes);
                throw ex;
            }
        }
    }


    /**
     * 检查是否强制修改密码
     * @param user 用户
     */
    protected void checkPasswordModified(User user) {
        BasePasswordPolicy basePasswordPolicy = userAccountService.getPasswordPolicyByUser(user);
        // 校验密码是否过期
        if (basePasswordPolicy == null) {
            return;
        }
        if (userAccountService.isPasswordExpired(basePasswordPolicy, user)) {
            throw new CustomAuthenticationException(LoginExceptions.PASSWORD_EXPIRED.value());
        }

        // 校验是否开启了强制修改初始密码且当前用户未修改过初始密码
        if (userAccountService.isNeedForceModifyPassword(basePasswordPolicy, user)) {
            throw new CustomAuthenticationException(LoginExceptions.PASSWORD_FORCE_MODIFY.value());
        }
    }

    private boolean ldapAuthentication(Long organizationId, String loginName, String credentials) {
        BaseLdap ldap = baseLdapRepository.selectLdap(organizationId);
        if (ldap != null && ldap.getEnabled()) {
            LdapContextSource contextSource = new LdapContextSource();
            String url = ldap.getServerAddress() + ":" + ldap.getPort();
            contextSource.setUrl(url);
            contextSource.setBase(ldap.getBaseDn());
            contextSource.afterPropertiesSet();
            LdapTemplate ldapTemplate = new LdapTemplate(contextSource);
            // ad目录不设置会报错
            if (DirectoryType.MICROSOFT_ACTIVE_DIRECTORY.value().equals(ldap.getDirectoryType())) {
                ldapTemplate.setIgnorePartialResultException(true);
            }
            String userDn = null;
            boolean anonymousFetchFailed = false;
            try {
                List<String> names = ldapTemplate.search(
                        query()
                                .searchScope(SearchScope.SUBTREE)
                                .where("objectclass")
                                .is(ldap.getObjectClass())
                                .and(ldap.getLoginNameField())
                                .is(loginName),
                        new AbstractContextMapper<String>() {
                            @Override
                            protected String doMapFromContext(DirContextOperations ctx) {
                                return ctx.getNameInNamespace();
                            }
                        });
                userDn = getUserDn(names, ldap.getLoginNameField(), loginName);
            } catch (Exception e) {
                anonymousFetchFailed = true;
                LOGGER.error("ldap anonymous search objectclass = {}, {} = {} failed, exception {}",
                        ldap.getObjectClass(), ldap.getLoginNameField(), loginName, e);
            }
            if (anonymousFetchFailed) {
                userDn = accountAsUserDn2Authentication(loginName, ldap, contextSource, userDn);
            }
            return authentication(credentials, contextSource, userDn);
        } else {
            throw new AuthenticationServiceException(LoginException.LDAP_IS_DISABLE.value());
        }
    }

    private String accountAsUserDn2Authentication(String loginName, BaseLdap ldap, LdapContextSource contextSource, String userDn) {
        contextSource.setUserDn(ldap.getAccount());
        contextSource.setPassword(ldap.getLdapPassword());
        contextSource.afterPropertiesSet();
        LdapTemplate template = new LdapTemplate(contextSource);
        if (DirectoryType.MICROSOFT_ACTIVE_DIRECTORY.value().equals(ldap.getDirectoryType())) {
            template.setIgnorePartialResultException(true);
        }
        try {
            List<String> names = template.search(
                    query()
                            .searchScope(SearchScope.SUBTREE)
                            .where("objectclass")
                            .is(ldap.getObjectClass())
                            .and(ldap.getLoginNameField())
                            .is(loginName),
                    new AbstractContextMapper<String>() {
                        @Override
                        protected String doMapFromContext(DirContextOperations ctx) {
                            return ctx.getNameInNamespace();
                        }
                    });
            userDn = getUserDn(names, ldap.getLoginNameField(), loginName);
        } catch (Exception e) {
            LOGGER.error("use ldap account as userDn and password to authentication but search objectclass = {}, {} = {} failed, maybe the account or password is illegal, and check for the ldap config, exception {}",
                    ldap.getObjectClass(), ldap.getLoginNameField(), loginName, e);
        }
        return userDn;
    }

    private String getUserDn(List<String> names, String loginFiled, String loginName) {
        if (names.isEmpty()) {
            LOGGER.warn("user not found");
        } else if (names.size() == 1) {
            return names.get(0);
        } else {
            LOGGER.warn("user {} = {} is not unique", loginFiled, loginName);
        }
        return null;
    }

    private boolean authentication(String credentials, LdapContextSource contextSource, String userDn) {
        if (userDn == null) {
            return false;
        } else {
            DirContext ctx = null;
            try {
                ctx = contextSource.getContext(userDn, credentials);
                return true;
            } catch (Exception e) {
                logger.error("Login failed, userDn or credentials may be wrong, exception {}", e);
                return false;
            } finally {
                // It is imperative that the created DirContext instance is always closed
                LdapUtils.closeContext(ctx);
            }

        }
    }


    //
    // getter
    // ------------------------------------------------------------------------------


    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public BaseLdapRepository getBaseLdapRepository() {
        return baseLdapRepository;
    }

    public UserAccountService getUserAccountService() {
        return userAccountService;
    }

    public LoginRecordService getLoginRecordService() {
        return loginRecordService;
    }
}
