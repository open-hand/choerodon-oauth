package io.choerodon.oauth.infra.common.util;

import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.web.client.RestTemplate;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.oauth.domain.service.IUserService;
import io.choerodon.oauth.infra.dataobject.UserDO;

/**
 * @author wuguokai
 */
public class CustomTokenServices extends DefaultTokenServices {
    private static final String K8S_TOKEN_PREFIX = "k8s-";
    private static final String K8S_USER_NAME = "k8s_default_user";
    private static final String K8S_TOKEN_VALIDATE_URL = "http://devops-kubernetes-service/v1/token?token={1}";
    private static final Logger logger = LoggerFactory.getLogger(CustomTokenServices.class);
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private IUserService userService;

    @Override
    public OAuth2Authentication loadAuthentication(String accessTokenValue) {
        if (accessTokenValue.startsWith(K8S_TOKEN_PREFIX)) {
            String token = accessTokenValue.substring(4);
            try {
                //调用k8s服务验证k8s token的合法性
                String result = restTemplate.postForEntity(K8S_TOKEN_VALIDATE_URL, null, String.class, token).getBody();
                if (!result.equals("true")) {
                    logger.info("token is not valid");
                    return null;
                }
                new DefaultOAuth2AccessToken(UUID.randomUUID().toString());
                UserDO user = userService.findByLoginName(K8S_USER_NAME);
                if (user == null) {
                    logger.info("user k8s_default_user do not exist");
                    return null;
                }
                CustomUserDetails customUserDetails
                        = new CustomUserDetails(user.getLoginName(), "UNKNOWN PASSWORD", Collections.emptyList());
                customUserDetails.setEmail(user.getEmail());
                customUserDetails.setTimeZone(user.getTimeZone());
                customUserDetails.setUserId(user.getId());
                customUserDetails.setOrganizationId(user.getOrganizationId());
                customUserDetails.setLanguage(user.getLanguage());
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        customUserDetails, "unknown", Collections.emptyList());
                OAuth2Request request = new OAuth2Request(null, null, null, true, null, null, null, null, null);
                return new OAuth2Authentication(request, authentication);
            } catch (Exception e) {
                return null;
            }
        }

        OAuth2Authentication result = super.loadAuthentication(accessTokenValue);
        if (result.getUserAuthentication().getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails detail = (CustomUserDetails) result.getUserAuthentication().getPrincipal();
            UserDO userDO = userService.findByLoginName(detail.getUsername());
            if (userDO != null) {
                detail.setLanguage(userDO.getLanguage());
                detail.setTimeZone(userDO.getTimeZone());
                detail.setEmail(userDO.getEmail());
                result.setDetails(detail);
            }
        }
        return result;
    }
}
