package io.choerodon.oauth.api.controller.v1;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.oauth.api.service.TokenService;
import io.choerodon.swagger.annotation.Permission;

/**
 * @author Eugen
 */

@RestController
@RequestMapping("/v1/token_manager")
public class TokenController {
    @Autowired
    private TokenService tokenService;

    public void setTokenService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @ApiOperation(value = "根据loginName删除token")
    @Permission(permissionWithin = true)
    @DeleteMapping("/all")
    public void deleteTokens(@RequestParam(value = "loginName") String loginName) {
        tokenService.deleteAllUnderUser(loginName);
    }

    @ApiOperation(value = "根据tokenId删除token")
    @Permission(permissionWithin = true)
    @DeleteMapping("/one")
    public void deleteToken(@RequestParam(value = "tokenId") String tokenId) {
        tokenService.deleteOne(tokenId);
    }
}
