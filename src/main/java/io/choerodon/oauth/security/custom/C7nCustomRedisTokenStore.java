package io.choerodon.oauth.security.custom;

import java.util.Date;

import org.hzero.oauth.security.custom.CustomRedisTokenStore;
import org.hzero.oauth.security.util.LoginUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.store.redis.JdkSerializationStrategy;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStoreSerializationStrategy;
import org.springframework.session.SessionRepository;

/**
 * @author scp
 * @date 2020/9/17
 * @description
 * 复写renewalAccessToken方法 根据过期时间判断是否需要token续期
 */
public class C7nCustomRedisTokenStore extends CustomRedisTokenStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(C7nCustomRedisTokenStore.class);

    private static final String ACCESS = "access:";
    private static final String AUTH_TO_ACCESS = "auth_to_access:";
    private static final String AUTH = "auth:";
    private static final String CLIENT_ID_TO_ACCESS = "client_id_to_access:";
    private static final String UNAME_TO_ACCESS = "uname_to_access:";
    private static final String UNAME_TO_ACCESS_APP = "uname_to_access_app:";
    private static final String LNAME_TO_ACCESS = "lname_to_access:";
    private static final String LNAME_TO_ACCESS_APP = "lname_to_access_app:";
    private static final String ACCESS_TO_SESSION = "access_to_session:";
    private final RedisConnectionFactory connectionFactory;
    private LoginUtil loginUtil;
    private SessionRepository sessionRepository;
    private boolean accessTokenAutoRenewal;
    private String prefix = "";
    private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();
    private RedisTokenStoreSerializationStrategy serializationStrategy = new JdkSerializationStrategy();

    private RedisConnection getConnection() {
        return connectionFactory.getConnection();
    }


    public C7nCustomRedisTokenStore(RedisConnectionFactory connectionFactory, LoginUtil loginUtil, SessionRepository sessionRepository, boolean accessTokenAutoRenewal) {
        super(connectionFactory, loginUtil, sessionRepository, accessTokenAutoRenewal);
        this.connectionFactory = connectionFactory;
        this.loginUtil = loginUtil;
        this.sessionRepository = sessionRepository;
        this.accessTokenAutoRenewal = accessTokenAutoRenewal;

    }

    @Override
    public void setAuthenticationKeyGenerator(AuthenticationKeyGenerator authenticationKeyGenerator) {
        this.authenticationKeyGenerator = authenticationKeyGenerator;
    }

    @Override
    public void setSerializationStrategy(RedisTokenStoreSerializationStrategy serializationStrategy) {
        this.serializationStrategy = serializationStrategy;
    }


    @Override
    public void renewalAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication, int accessTokenValiditySeconds) {
        LOGGER.info("======test!!!!!!!! ====================");
        // 未开启自动续期 并且 自动下线也未开启
        if (!accessTokenAutoRenewal) {
            return;
        }
        // 如果 refresh token 过期 则不允许续期
        OAuth2RefreshToken refreshToken = token.getRefreshToken();
        boolean refreshTokenExpired = refreshToken instanceof DefaultExpiringOAuth2RefreshToken
                && isExpired(((DefaultExpiringOAuth2RefreshToken) refreshToken).getExpiration());
        // 无操作下线时间 优先级大于 默认客户端超时时间(相当于一次 refresh token 操作)
        long seconds = accessTokenValiditySeconds;
        // 如果开启了无操作自动下线
        if (seconds > 0) {
            String uKey = UNAME_TO_ACCESS;
            String lKey = LNAME_TO_ACCESS;
            if (loginUtil.isMobileDeviceLogin(authentication)) {
                uKey = UNAME_TO_ACCESS_APP;
                lKey = LNAME_TO_ACCESS_APP;
            }
            // 重新保存 access_token
            byte[] accessKey = serializeKey(ACCESS + token.getValue());
            byte[] authKey = serializeKey(AUTH + token.getValue());
            byte[] authToAccessKey = serializeKey(AUTH_TO_ACCESS + authenticationKeyGenerator.extractKey(authentication));
            byte[] clientId = serializeKey(CLIENT_ID_TO_ACCESS + authentication.getOAuth2Request().getClientId());
            byte[] uKeyBytes = serializeKey(uKey + getApprovalKey(authentication));
            byte[] accessSessionKey = serializeKey(ACCESS_TO_SESSION + token.getValue());
            byte[] lKeyBytes = authentication.getUserAuthentication() == null ? null : serializeKey(lKey + authentication.getUserAuthentication().getName());
            RedisConnection conn = getConnection();
            try {
                try {
                    conn.openPipeline();
                } catch (UnsupportedOperationException e) {
                    LOGGER.debug("Currently RedisConnection[" + conn.getClass() + "] does not support the use of pipes, ignore it.");
                }
                // 如果 refresh token 未过期 && 开启了 access token 续期 = 刷新 access token
                if (!refreshTokenExpired && accessTokenAutoRenewal) {
                    if (token.getExpiration().getTime() > new Date(System.currentTimeMillis() - (30 * 60 * 1000L)).getTime()) {
                        LOGGER.info("======time ====================");
                        Date expireDate = new Date(System.currentTimeMillis() + (accessTokenValiditySeconds * 1000L));
                        ((DefaultOAuth2AccessToken) token).setExpiration(expireDate);
                        byte[] serializedAccessToken = serialize(token);
                        conn.set(accessKey, serializedAccessToken);
                        conn.set(authToAccessKey, serializedAccessToken);
                        conn.rPush(clientId, serializedAccessToken);
                    }
                }
                conn.expire(accessKey, seconds);
                conn.expire(authKey, seconds);
                conn.expire(authToAccessKey, seconds);
                conn.expire(clientId, seconds);
                conn.expire(uKeyBytes, seconds);
                conn.expire(accessSessionKey, seconds);
                if (lKeyBytes != null) {
                    conn.expire(lKeyBytes, seconds);
                }
                try {
                    conn.closePipeline();
                } catch (UnsupportedOperationException e) {
                    LOGGER.debug("Currently RedisConnection[" + conn.getClass() + "] does not support the use of pipes, ignore it.");
                }
            } finally {
                conn.close();
            }
        }
    }

    private byte[] serialize(Object object) {
        return serializationStrategy.serialize(object);
    }


    private boolean isExpired(Date expiration) {
        return expiration != null && expiration.before(new Date());
    }

    private static String getApprovalKey(OAuth2Authentication authentication) {
        String userName = authentication.getUserAuthentication() == null ? ""
                : authentication.getUserAuthentication().getName();
        return getApprovalKey(authentication.getOAuth2Request().getClientId(), userName);
    }

    private static String getApprovalKey(String clientId, String userName) {
        return clientId + (userName == null ? "" : ":" + userName);
    }

    private byte[] serializeKey(String object) {
        return serialize(prefix + object);
    }


}
