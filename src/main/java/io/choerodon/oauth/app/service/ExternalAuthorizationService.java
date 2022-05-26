package io.choerodon.oauth.app.service;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

public interface ExternalAuthorizationService {
    OAuth2AccessToken authorizationByOpenId(String clientId, String clientSecret, String openId,String providerType);

    OAuth2AccessToken withinAuthorization(String username, String clientId);
}