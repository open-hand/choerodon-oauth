package org.hzero.oauth.security.custom.event;

import java.util.Objects;
import javax.annotation.Nonnull;

import org.hzero.oauth.domain.entity.AuditLogin;
import org.hzero.oauth.domain.entity.User;
import org.hzero.oauth.domain.repository.AuditLoginRepository;
import org.hzero.oauth.domain.service.AuditLoginService;
import org.hzero.oauth.security.constant.SecurityAttributes;
import org.hzero.oauth.security.event.TokenEvent;
import org.hzero.oauth.security.service.LoginRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.core.oauth.DetailsHelper;

/**
 * @author zhangxiaowei 2020/11/09
 *
 * 临时覆盖hzero 慢查询逻辑 辅助登录逻辑暂时不要
 */
@Order(60)
@Component
public class TokenAuditListener implements ApplicationListener<TokenEvent> {

    @Autowired
    private AuditLoginService auditLoginService;

    @Autowired
    private LoginRecordService loginRecordService;

    @Autowired
    private AuditLoginRepository auditLoginRepository;

    @Override
    public void onApplicationEvent(@Nonnull TokenEvent event) {
//        User user = loginRecordService.getLocalLoginUser();
//        CustomUserDetails customUserDetails = null;
//        if (Objects.nonNull(user)) {
//            customUserDetails = new CustomUserDetails(user.getLoginName(), "");
//            customUserDetails.setUserId(user.getId());
//        } else {
//            if (SecurityAttributes.FIELD_TOKEN_GRANT_TYPE_AUTH_CODE.equals(event.getTokenRequest().getGrantType())) {
//                AuditLogin loginAudit = new AuditLogin();
//                loginAudit.setAccessToken(
//                    event.getTokenRequest().getRequestParameters().get(SecurityAttributes.SECURITY_LOGIN_CODE));
//                loginAudit = auditLoginRepository.selectOne(loginAudit);
//                if (loginAudit != null) {
//                    customUserDetails = new CustomUserDetails(loginAudit.getLoginName(), "");
//                    customUserDetails.setUserId(loginAudit.getUserId());
//                }
//            } else {
//                customUserDetails = DetailsHelper.getUserDetails();
//            }
//        }
//
//        auditLoginService.addLoginRecord(
//            event.getServletRequest(),
//            event.getAccessToken().getValue(),
//            event.getTokenRequest().getClientId(),
//            customUserDetails,
//            SecurityAttributes.FIELD_TOKEN_ENDPOINT,
//            event.getTokenRequest().getGrantType()
//        );
    }
}
