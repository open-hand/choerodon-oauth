package io.choerodon.oauth.infra.enums;

/**
 * @author zongw.lee@gmail.com
 * @date 2019/10/18
 */
public enum AuthorizationTypeEnum {

    PROMETHEUS_CLUSTER("prometheusCluster");

    private final String value;

    AuthorizationTypeEnum(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
