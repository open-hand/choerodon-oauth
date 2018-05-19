package io.choerodon.oauth.api.dto;

/**
 * @author wuguokai
 */
public class LockUserPayload {
    public static final String EVENT_TYPE_LOCK_USER = "lockUser";

    private Long userId;
    private Integer lockedExpireTime;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getLockedExpireTime() {
        return lockedExpireTime;
    }

    public void setLockedExpireTime(Integer lockedExpireTime) {
        this.lockedExpireTime = lockedExpireTime;
    }
}
