package io.choerodon.oauth.infra.enums;

/**
 * @author superlee
 */
public enum LoginExceptions {

    USER_IS_NOT_ACTIVATED("userNotActive"),

    ACCOUNT_IS_LOCKED("accountLocked"),

    CAPTCHA_IS_NULL("captchaNull"),

    CAPTCHA_IS_WRONG("captchaWrong"),

    USERNAME_NOT_FOUND_OR_PASSWORD_IS_WRONG("usernameNotFoundOrPasswordIsWrong");

    private final String value;

    LoginExceptions(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
