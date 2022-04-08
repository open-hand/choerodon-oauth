package io.choerodon.oauth.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.hzero.oauth.security.constant.SecurityAttributes;
import org.hzero.oauth.security.util.LoginUtil;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class RequestUtil {
    public static String obtainParameter(String parameterName, String defaultValue) {
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        return obtainParameter(request, parameterName, defaultValue);
    }

    /**
     * 获取请求中的参数，默认从 request 中获取，获取不到从 session 中保存的 request 中获取
     *
     * @param request       HttpServletRequest
     * @param parameterName 参数名称
     * @param defaultValue  默认值
     * @return 参数值
     */
    public static String obtainParameter(HttpServletRequest request, String parameterName, String defaultValue) {
        if (parameterName.equals(LoginUtil.FIELD_CLIENT_ID)){
            String clientIdByHost = request.getHeader("Host").split("\\.")[0];
            if (!clientIdByHost.equals("api")){
                return clientIdByHost;
            }
        }
        String parameterValue = request.getParameter(parameterName);
        if (StringUtils.isNotBlank(parameterValue)) {
            return parameterValue;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return defaultValue;
        }
        Object sessionAttribute = session.getAttribute(parameterName);
        if (sessionAttribute instanceof String){
            return (String) sessionAttribute;
        }
        DefaultSavedRequest saveRequest = (DefaultSavedRequest) session.getAttribute(SecurityAttributes.SECURITY_SAVED_REQUEST);
        if (saveRequest != null) {
            String[] values = saveRequest.getParameterValues(parameterName);
            if (values != null) {
                parameterValue = StringUtils.defaultIfBlank(values[0], defaultValue);
            }
        }
        parameterValue = StringUtils.defaultIfBlank(parameterValue, defaultValue);
        return parameterValue;
    }
}
