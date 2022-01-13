package io.choerodon.oauth.security.custom;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.mybatis.domian.Language;
import org.hzero.oauth.domain.entity.User;
import org.hzero.oauth.domain.repository.UserRepository;
import org.hzero.oauth.security.config.SecurityProperties;
import org.hzero.oauth.security.constant.SecurityAttributes;
import org.hzero.oauth.security.custom.CustomAuthenticationSuccessHandler;
import org.hzero.oauth.security.service.LoginRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * 登录成功处理器
 *
 * @author bojiangzhou 2019/02/25
 */
public class C7nCustomAuthenticationSuccessHandler extends CustomAuthenticationSuccessHandler {
    /**
     * 用户信息服务对象
     */
    private LoginRecordService loginRecordService;
    @Autowired
    private UserRepository userRepository;
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(C7nCustomAuthenticationSuccessHandler.class);

    public C7nCustomAuthenticationSuccessHandler(SecurityProperties securityProperties) {
        super(securityProperties);
    }

    @Autowired
    public void setLoginRecordService(LoginRecordService loginRecordService) {
        this.loginRecordService = loginRecordService;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        User localLoginUser = loginRecordService.getLocalLoginUser();
        String language = getUserPageLanguage();
        if (ObjectUtils.isNotEmpty(language)) {
            User dbUser = userRepository.selectByPrimaryKey(localLoginUser.getId());
            User user = new User();
            user.setLanguage(StringUtils.isBlank(language) ? null : language);
            user.setId(localLoginUser.getId());
            user.setObjectVersionNumber(dbUser.getObjectVersionNumber());
            this.userRepository.updateOptional(user, new String[]{"language"});
        }
        super.onAuthenticationSuccess(request, response, authentication);

    }

    /**
     * 取用户界面选择的语言
     *
     * @return 用户界面选择的语言
     */
    @Nullable
    private String getUserPageLanguage() {
        // 从session中获取数据
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (Objects.isNull(requestAttributes)) {
            return null;
        }

        Object initLangFlagAttribute = requestAttributes.getAttribute(SecurityAttributes.FIELD_INIT_LANG_FLAG,
                RequestAttributes.SCOPE_SESSION);
        // 如果初始化标识不为false，就说明是初始化操作，此时不从session中取语言
        if (initLangFlagAttribute instanceof Boolean && BooleanUtils.isNotFalse((Boolean) initLangFlagAttribute)) {
            return null;
        }

        Object attribute = requestAttributes.getAttribute(SecurityAttributes.FIELD_LANG, RequestAttributes.SCOPE_SESSION);
        if (Objects.isNull(attribute)) {
            return null;
        }

        if (attribute instanceof String) {
            return (String) attribute;
        } else if (attribute instanceof Language) {
            return ((Language) attribute).getCode();
        } else if (attribute instanceof org.hzero.oauth.domain.entity.Language) {
            return ((org.hzero.oauth.domain.entity.Language) attribute).getCode();
        }

        return null;
    }
}