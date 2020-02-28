package io.choerodon.oauth.infra.enums;

/**
 * 〈功能简述〉
 * 〈页面地址枚举〉
 *
 * @author wanghao
 * @Date 2020/2/28 10:59
 */
public enum  PageUrlEnum {
    DEFAULT_PAGE("password-find"),
    RESET_URL("/oauth/password/reset_page");

    private final String value;

    PageUrlEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
