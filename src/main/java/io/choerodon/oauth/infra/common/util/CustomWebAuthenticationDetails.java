package io.choerodon.oauth.infra.common.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

/**
 * @author wuguokai
 */
public class CustomWebAuthenticationDetails extends WebAuthenticationDetails {

    private String captcha;
    private String captchaCode;

    /**
     * 构造方法
     *
     * @param request 会提取captcha
     */
    public CustomWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        HttpSession session = request.getSession();
        captchaCode = (String) session.getAttribute("captchaCode");
        captcha = request.getParameter("captcha");
    }

    public String getCaptchaCode() {
        return captchaCode;
    }

    public String getCaptcha() {
        return captcha;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        if (!super.equals(object)) {
            return false;
        }

        CustomWebAuthenticationDetails that = (CustomWebAuthenticationDetails) object;

        return captcha != null ? captcha.equals(that.captcha) : that.captcha == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (captcha != null ? captcha.hashCode() : 0);
        return result;
    }
}
