package io.choerodon.oauth.domain.service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;

import io.choerodon.mybatis.service.BaseServiceImpl;
import io.choerodon.oauth.domain.service.TokenService;
import io.choerodon.oauth.infra.dataobject.AccessTokenDO;

/**
 * Created by jiatong.li on 3/18/17.
 */
@Service
public class TokenServiceImpl extends BaseServiceImpl<AccessTokenDO> implements TokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public AccessTokenDO create(String additionalInfo) {
        Map map = null;
        try {
            map = OBJECT_MAPPER.readValue(additionalInfo, Map.class);
        } catch (IOException e) {
            LOGGER.info("additionalInfo 格式错误");
        }
        BaseClientDetails clientDetails = new BaseClientDetails();
        clientDetails.setAdditionalInformation(map);
        DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(UUID.randomUUID().toString());
        token.setExpiration(new Date(System.currentTimeMillis() + 100000));
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                clientDetails, "unknown", Collections.emptyList());
        OAuth2Request request = new OAuth2Request(null, null, null, true, null, null, null, null, null);
        OAuth2Authentication auth2Authentication = new OAuth2Authentication(request, authentication);
        AccessTokenDO accessToken = new AccessTokenDO();
        accessToken.setTokenId(extractTokenKey(token.getValue()));
        accessToken.setValue(token);
        accessToken.setUserName(clientDetails.getClientId());
        accessToken.setClientId(clientDetails.getClientSecret());
        accessToken.setAuth2Authentication(auth2Authentication);
        if (insert(accessToken) != 1) {
            return null;
        }
        return selectByPrimaryKey(accessToken.getTokenId());
    }

    private String extractTokenKey(String value) {
        if (value == null) {
            return null;
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
        }

        try {
            byte[] bytes = digest.digest(value.getBytes("UTF-8"));
            return String.format("%032x", new BigInteger(1, bytes));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
        }
    }
}
