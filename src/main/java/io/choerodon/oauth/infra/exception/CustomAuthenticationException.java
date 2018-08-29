package io.choerodon.oauth.infra.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author dongfan117@gmail.com
 */
public class CustomAuthenticationException extends AuthenticationException {
    private final String value;

    public CustomAuthenticationException(String msg, Throwable t, String value) {
        super(msg, t);
        this.value = value;
    }

    public CustomAuthenticationException(String msg, String value) {
        super(msg);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
