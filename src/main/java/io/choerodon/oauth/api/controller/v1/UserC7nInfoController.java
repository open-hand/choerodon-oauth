package io.choerodon.oauth.api.controller.v1;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.Objects;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.message.MessageAccessor;
import org.hzero.core.user.UserType;
import org.hzero.core.util.Results;
import org.hzero.oauth.config.SwaggerApiConfig;
import org.hzero.oauth.domain.entity.User;
import org.hzero.oauth.domain.repository.UserRepository;
import org.hzero.oauth.domain.service.UserLoginService;
import org.hzero.oauth.security.exception.LoginExceptions;
import org.hzero.oauth.security.util.LoginUtil;
import org.hzero.starter.captcha.domain.core.pre.CaptchaPreResult;
import org.hzero.starter.captcha.domain.sms.pre.SmsPreResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.oauth.api.vo.BindReMsgVO;
import io.choerodon.oauth.app.service.UserService;
import io.choerodon.oauth.infra.dto.UserE;
import io.choerodon.oauth.infra.mapper.UserMapper;

/**
 * Created by wangxiang on 2021/8/17
 */
@Api(tags = "User C7N Info")
@RestController("v1.UserC7nInfoController")
@RequestMapping("/choerodon")
public class UserC7nInfoController {

    private static final String PHONE_IS_NOT_BIND = "phone.is.not.bind";


    @Autowired
    private UserService userService;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserMapper userMapper;


    @ApiOperation(value = "非ldap用户绑定手机号的接口")
    @PostMapping("/bind/user/phone")
    public ResponseEntity<BindReMsgVO> bindUserPhone(@RequestParam(required = false) String phone,
                                                     @RequestParam(required = false) String captcha,
                                                     @RequestParam(required = false) String captchaKey) {
        return Results.success(userService.bindUserPhone(phone, captcha, captchaKey));
    }


    @ApiOperation(value = "请求发送验证码的接口")
    @PostMapping("/public/send-phone-captcha")
    @ResponseBody
    public ResponseEntity<CaptchaPreResult<?>> sendPhoneCaptcha(
            @RequestParam(defaultValue = BaseConstants.DEFAULT_CROWN_CODE) String internationalTelCode,
            @RequestParam("phone") String phone,
            @RequestParam(name = UserType.PARAM_NAME, required = false, defaultValue = UserType.DEFAULT_USER_TYPE) String userType,
            @RequestParam(required = false) String businessScope) {


        //未绑定的手机号不能发验证码
        CaptchaPreResult<?> checkPhoneBindResult = checkPhoneBind(phone);
        if (Objects.isNull(checkPhoneBindResult)) {
            CaptchaPreResult<?> captchaPreResult = userLoginService.sendPhoneCaptchaNew(internationalTelCode, phone,
                    UserType.ofDefault(userType), businessScope, true);
            return Results.success(captchaPreResult);
        } else {
            return Results.success(checkPhoneBindResult);
        }
    }

    private CaptchaPreResult<?> checkPhoneBind(String phone) {
        UserE userE = new UserE();
        userE.setPhone(phone);
        UserE user = userMapper.selectOne(userE);
        if (!user.getPhoneBind()) {
            return SmsPreResult.failure(MessageAccessor.getMessage(PHONE_IS_NOT_BIND, LoginUtil.getLanguageLocale()).desc());
        } else {
            return null;
        }
    }

}
