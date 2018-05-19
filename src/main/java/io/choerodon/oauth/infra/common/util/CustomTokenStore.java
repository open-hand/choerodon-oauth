package io.choerodon.oauth.infra.common.util;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.stereotype.Service;

import io.choerodon.oauth.infra.config.OauthProperties;
import io.choerodon.oauth.infra.mapper.AccessTokenMapper;

/**
 * @author wuguokai
 */
@Service
public class CustomTokenStore extends JdbcTokenStore {
    private static final Logger logger = LoggerFactory.getLogger(CustomTokenStore.class);
    @Value("${hook.token:#{null}}")
    private String hook;
    // @Autowired
    // private TokenLogsService tokenLogsService;

    // @Autowired
    // private ClientService clientService;

    @Autowired
    private OauthProperties oauthProperties;

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    @Autowired
    private ChoerodonDAuthenticationKeyGenerator authenticationKeyGenerator;

    // @Autowired
    // private TokenRedisStore tokenRedisStore;

    @Autowired
    public CustomTokenStore(DataSource dataSource,
                            ChoerodonDAuthenticationKeyGenerator authenticationKeyGenerator) {
        super(dataSource);
        setAuthenticationKeyGenerator(authenticationKeyGenerator);
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        try {
            authentication.getPrincipal();
            /*
            TokenLog tokenLogs = new TokenLog();
            if (principal instanceof CustomUserDetails) {

                CustomUserDetails details = (CustomUserDetails) principal;
                tokenLogs.setUserId(details.getUserId());
            }
            tokenLogs.setTokenId(extractTokenKey(token.getValue()));
            tokenLogs.setClientId(clientService.selectByName(
                    authentication.getOAuth2Request().getClientId()).getId());
            tokenLogs.setTokenAccessType(authentication.getOAuth2Request().getGrantType());
            tokenLogs.setTokenAccessTime(new Date());
            tokenLogsService.insert(tokenLogs);
            if (hook != null) {
                ResponseEntity<String> response = template.postForEntity(hook, tokenLogs, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    logger.warn("web hook {} status code: {}", hook, response.getStatusCode());
                }
            }
            */
        } catch (Exception e) {
            logger.warn("token store exception: {}", e);
        }

        super.storeAccessToken(token, authentication);
        // tokenRedisStore.setCacheByTokenId(extractTokenKey(token.getValue()), token, authentication);
        // tokenRedisStore.setCacheByAuthId(authenticationKeyGenerator.extractKey(authentication), token);
    }


    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        if (oauthProperties.isEnabledSingleLogin() && !authentication.isClientOnly()) {
            String key = authenticationKeyGenerator.extractKey(authentication);
            String username = authentication.getName();
            String clientId = authentication.getOAuth2Request().getClientId();
            accessTokenMapper.selectTokens(username, clientId, key);
            /*
                for (AccessTokenDO token : tokenList) {
                    tokenRedisStore.removeCache(token);
                }
            */
            accessTokenMapper.deleteTokens(username, clientId, key);

        }
        return super.getAccessToken(authentication);
    }

    @Override
    public void removeAccessToken(String tokenValue) {
        String tokenId = extractTokenKey(tokenValue);
        accessTokenMapper.selectByPrimaryKey(tokenId);
        // tokenRedisStore.removeCache(accessToken);
        super.removeAccessToken(tokenValue);
    }
    /*
    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        // OAuth2AccessToken token = tokenRedisStore.getTokenByTokenId(extractTokenKey(tokenValue));
        // if (token == null) {
        //      token = super.readAccessToken(tokenValue);
        // }
        // return token;
        return super.readAccessToken(tokenValue);
    }

    @Override
    public OAuth2Authentication readAuthentication(String token) {

        // OAuth2Authentication authentication = tokenRedisStore.getAuthByTokenId(extractTokenKey(token));
        // if (authentication == null) {
        // authentication = super.readAuthentication(token);
        // }
        // return authentication;
        return super.readAuthentication(token);
    }
        */
}
