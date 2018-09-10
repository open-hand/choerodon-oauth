package io.choerodon.oauth.infra.common.util;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author dongfan117@gmail.com
 */
@Component
public class RedisTokenUtil {

    public static final String SHORT_CODE = "short";
    public static final String LONG_CODE = "uuid";
    public static final String NAME_SPACE = "choerodon:token";
    @Value("${choerodon.password.reset.invalidSecond:600}")
    private Long invalidSecond;
    @Value("${choerodon.password.reset.disableSecond:60}")
    private Long disableSecond;
    @Autowired
    private StringRedisTemplate redisTemplate;

    public String createLongToken() {
        return UUID.randomUUID().toString();
    }

    public String createShortToken() {
        return String.valueOf(new Random().nextInt(899999) + 100000);
    }

    public String store(String type, String key, String token) {
        this.redisTemplate.opsForValue().set(createKey(type, key), token, invalidSecond, TimeUnit.SECONDS);
        return token;
    }

    public Long setDisableTime(String key) {
        Long disableTime = System.currentTimeMillis() / 1000 + disableSecond;
        this.redisTemplate.opsForValue().set(createKey("valid", key), String.valueOf(disableTime), disableSecond, TimeUnit.SECONDS);
        return disableTime;
    }

    public Long getDisableTime(String key) {
        String disableTime = this.redisTemplate.opsForValue().get(createKey("valid", key));
        return disableTime != null ? Long.valueOf(disableTime) : null;
    }

    public void expire(String type, String key) {
        this.redisTemplate.delete(createKey(type, key));
    }

    public boolean check(String type, String key, String token) {
        return token.equals(this.redisTemplate.opsForValue().get(createKey(type, key) + ""));
    }

    private String createKey(String type, String key) {
        return NAME_SPACE + ":" + type + ":" + key;
    }
}
