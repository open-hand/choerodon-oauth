package io.choerodon.oauth.api.controller.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hzero.core.util.Results;
import org.hzero.oauth.config.SwaggerApiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.oauth.api.vo.BindReMsgVO;
import io.choerodon.oauth.app.service.UserService;
import io.choerodon.swagger.annotation.Permission;

/**
 * Created by wangxiang on 2021/8/17
 */
@Api(tags = "User C7N Info")
@RestController("v1.UserC7nInfoController")
@RequestMapping("/choerodon")
public class UserC7nInfoController {

    @Autowired
    private UserService userService;


    @ApiOperation(value = "非ldap用户绑定手机号的接口")
    @PostMapping("/bind/user/phone")
    public ResponseEntity<BindReMsgVO> bindUserPhone(@RequestParam(required = false) String phone,
                                                     @RequestParam(required = false) String captcha,
                                                     @RequestParam(required = false) String captchaKey) {
        return Results.success(userService.bindUserPhone(phone, captcha, captchaKey));
    }
}
