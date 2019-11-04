package io.choerodon.oauth.infra.common.util;

import io.choerodon.oauth.api.service.ClientService;
import io.choerodon.oauth.api.service.UserService;
import io.choerodon.oauth.domain.entity.ClientE;
import io.choerodon.oauth.domain.entity.UserE;
import io.choerodon.oauth.infra.config.OauthProperties;
import io.choerodon.oauth.infra.dataobject.AccessTokenDO;
import io.choerodon.oauth.infra.feign.DevopsFeignClient;
import io.choerodon.oauth.infra.mapper.AccessTokenMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.sql.DataSource;
import java.util.Date;
import java.util.HashMap;

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
    private ClientService clientService;

    @Autowired
    private UserService userService;

    @Autowired
    private DevopsFeignClient devopsFeignClient;

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
        super.setAuthenticationKeyGenerator(authenticationKeyGenerator);
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

    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        HashMap<String, Object> additionalInfo = new HashMap<>();
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        String sessionId = "";
        if (attr != null) {
            sessionId = attr.getRequest().getSession(true).getId();
        }
        additionalInfo.put("createTime", new Date());
        additionalInfo.put("sessionId", sessionId);
        ((DefaultOAuth2AccessToken) token).setAdditionalInformation(additionalInfo);
        super.storeAccessToken(token, authentication);
    }
    public AccessTokenDO findAccessTokenByTokenValue(String tokenValue) {
        AccessTokenDO record = new AccessTokenDO();
        record.setTokenId(extractTokenKey(tokenValue));
        return accessTokenMapper.selectOne(record);
    }

    public Boolean checkPrometheusToken(String tokenValue) {
        OAuth2AccessToken token = super.readAccessToken(tokenValue);
        if (token == null) {
            return false;
        }

        if (token.isExpired()) {
            return false;
        }

        AccessTokenDO accessToken = findAccessTokenByTokenValue(tokenValue);
        if (accessToken == null) {
            return false;
        }
        ClientE client = clientService.getClientByName(accessToken.getClientId());
        if (client == null) {
            return false;
        }
        UserE userE = userService.queryByLoginField(accessToken.getUserName());
        if (userE == null) {
            return false;
        }

        Boolean result = devopsFeignClient.checkUserClusterPermission(client.getSourceId(), userE.getId()).getBody();
        if (Boolean.FALSE.equals(result)) {
            return false;
        }

        return true;
    }

}
