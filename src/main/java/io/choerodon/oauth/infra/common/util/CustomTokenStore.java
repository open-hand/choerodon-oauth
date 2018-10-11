package io.choerodon.oauth.infra.common.util;

import javax.sql.DataSource;

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
    @Value("${hook.token:#{null}}")
    private String hook;

    @Autowired
    private OauthProperties oauthProperties;

    @Autowired
    private AccessTokenMapper accessTokenMapper;

    @Autowired
    private ChoerodonAuthenticationKeyGenerator authenticationKeyGenerator;

    @Autowired
    public CustomTokenStore(DataSource dataSource,
                            ChoerodonAuthenticationKeyGenerator authenticationKeyGenerator) {
        super(dataSource);
        setAuthenticationKeyGenerator(authenticationKeyGenerator);
    }

    public void setOauthProperties(OauthProperties oauthProperties) {
        this.oauthProperties = oauthProperties;
    }

    public void setAccessTokenMapper(AccessTokenMapper accessTokenMapper) {
        this.accessTokenMapper = accessTokenMapper;
    }

    public void setAuthenticationKeyGenerator(ChoerodonAuthenticationKeyGenerator authenticationKeyGenerator) {
        this.authenticationKeyGenerator = authenticationKeyGenerator;
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        if (oauthProperties.isEnabledSingleLogin() && !authentication.isClientOnly()) {
            String key = authenticationKeyGenerator.extractKey(authentication);
            String username = authentication.getName();
            String clientId = authentication.getOAuth2Request().getClientId();
            accessTokenMapper.selectTokens(username, clientId, key);
            accessTokenMapper.deleteTokens(username, clientId, key);

        }
        return super.getAccessToken(authentication);
    }

    @Override
    public void removeAccessToken(String tokenValue) {
        String tokenId = extractTokenKey(tokenValue);
        accessTokenMapper.selectByPrimaryKey(tokenId);
        super.removeAccessToken(tokenValue);
    }

}
