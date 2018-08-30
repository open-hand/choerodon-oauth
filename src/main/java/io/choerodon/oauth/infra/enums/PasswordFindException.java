package io.choerodon.oauth.infra.enums;

/**
 * @author superlee
 */
public enum PasswordFindException {

    ACCOUNT_NOT_EXIST("accountNotExist"),
    USER_IS_ILLEGAL("userIsIllegal"),
    EMAIL_FORMAT_ILLEGAL("emailFormatIllegal"),
    LDAP_CANNOT_CHANGE_PASSWORD("ldapCannotChangePassword"),
    PASSWORD_NOT_EQUAL("passwordNotEqual"),
    CAPTCHA_ERROR("captchaError"),
    DISABLE_SEND("disableSend");

    private final String value;

    PasswordFindException(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
