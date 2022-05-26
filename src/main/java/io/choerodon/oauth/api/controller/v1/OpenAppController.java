package io.choerodon.oauth.api.controller.v1;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.oauth.app.service.ExternalAuthorizationService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author scp
 * @since 2022/5/26
 */
@Api(tags = "Open App ")
@RestController("v1.OpenAppController")
@RequestMapping("/choerodon/open_app")
public class OpenAppController {
    @Autowired
    private ExternalAuthorizationService externalAuthorizationService;

    @PostMapping(value = "/authorization_by_openId")
    public OAuth2AccessToken authorizationByOpenId(@RequestParam("client_id") String clientId,
                                                   @RequestParam("client_secret") String clientSecret,
                                                   @RequestParam("open_id") String openId,
                                                   @RequestParam(value = "provider_type", required = false, defaultValue = "yqcloud") String providerType) {
        return externalAuthorizationService.authorizationByOpenId(clientId, clientSecret, openId, providerType);
    }
}
