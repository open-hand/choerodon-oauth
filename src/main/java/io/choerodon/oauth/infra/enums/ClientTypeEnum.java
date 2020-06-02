package io.choerodon.oauth.infra.enums;

/**
 * @author wanghao
 * @Date 2019/10/21 14:49
 */
public enum ClientTypeEnum {
    CLUSTER("cluster");

    private final String value;

    ClientTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
