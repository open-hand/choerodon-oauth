package io.choerodon.oauth.api.service.impl;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.choerodon.oauth.infra.dto.RouteMemberRuleDTO;
import io.choerodon.oauth.infra.mapper.RouteMemberRuleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.oauth.api.service.PrincipalService;
import io.choerodon.oauth.domain.entity.ClientE;
import io.choerodon.oauth.domain.entity.UserE;
import io.choerodon.oauth.infra.mapper.ClientMapper;
import io.choerodon.oauth.infra.mapper.UserMapper;

@Service
public class PrincipalServiceImpl implements PrincipalService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrincipalServiceImpl.class);

    @Autowired
    ClientMapper clientMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    private RouteMemberRuleMapper routeMemberRuleMapper;

    private ObjectMapper mapper = new ObjectMapper();

    /**
     * 用于Userdetail获取时，往Userdetail中添加客户端信息（Userdetail中user为客户端创建人）
     */
    @Override
    public Principal setClientDetailUserDetails(Principal principal) {
        String clientName = ((OAuth2Authentication) principal).getPrincipal().toString();
        ClientE clientE = new ClientE();
        clientE.setName(clientName);
        clientE = clientMapper.selectOne(clientE);
        if (clientE == null || clientE.getId() == null) {
            throw new CommonException("setClientDetailUserDetails.client.not.exist");
        }
        CustomUserDetails user = new CustomUserDetails(clientName,
                "unknown password", Collections.emptyList());
        UserE userE = userMapper.selectByPrimaryKey(clientE.getCreatedBy());
        user.eraseCredentials();
        //添加client创建人信息
        if (userE != null) {
            user.setUserId(userE.getId());
            user.setLanguage(userE.getLanguage());
            user.setAdmin(userE.getAdmin());
            user.setTimeZone(userE.getTimeZone());
            user.setOrganizationId(userE.getOrganizationId());
            user.setEmail(userE.getEmail());
            addRouteRuleCode(user);
        }
        //添加client信息
        user.setOrganizationId(clientE.getOrganizationId());
        user.setClientId(clientE.getId());
        user.setClientName(clientE.getName());
        int accessTokenValidity = clientE.getAccessTokenValidity() != null ? clientE.getAccessTokenValidity().intValue() : 3600;
        user.setClientAccessTokenValiditySeconds(accessTokenValidity);
        int refreshTokenValidity = clientE.getRefreshTokenValidity() != null ? clientE.getRefreshTokenValidity().intValue() : 3600;
        user.setClientRefreshTokenValiditySeconds(refreshTokenValidity);
        user.setClientAuthorizedGrantTypes(StringUtils
                .commaDelimitedListToSet(clientE.getAuthorizedGrantTypes()));
        user.setClientAutoApproveScopes(StringUtils.commaDelimitedListToSet(clientE.getAutoApprove()));
        user.setClientRegisteredRedirectUri(StringUtils
                .commaDelimitedListToSet(clientE.getWebServerRedirectUri()));
        user.setClientResourceIds(StringUtils.commaDelimitedListToSet(clientE.getResourceIds()));
        user.setClientScope(StringUtils.commaDelimitedListToSet(clientE.getScope()));
        String json = clientE.getAdditionalInformation();
        if (json != null) {
            try {
                Map<String, Object> additionalInformation = mapper.readValue(json, Map.class);
                user.setAdditionInfo(additionalInformation);
            } catch (Exception e) {
                LOGGER.warn("parser addition info error: {}", e);
            }
        }

        //构建返回Principal  （原Principal添加客户端及用户信息）
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                user, ((OAuth2Authentication) principal).getCredentials(),
                ((OAuth2Authentication) principal).getAuthorities());

        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(((OAuth2Authentication) principal).getOAuth2Request(), usernamePasswordAuthenticationToken);

        copyDetails((OAuth2Authentication) principal, oAuth2Authentication);

        return oAuth2Authentication;
    }

    @Override
    public void addRouteRuleCode(CustomUserDetails customUserDetails) {
        RouteMemberRuleDTO record = new RouteMemberRuleDTO();
        Long userId = customUserDetails.getUserId();
        if (userId == null) {
            return;
        }
        record.setUserId(userId);
        RouteMemberRuleDTO routeMemberRule = routeMemberRuleMapper.selectOne(record);
        if (routeMemberRule == null) {
            return;
        }
        customUserDetails.setRouteRuleCode(routeMemberRule.getRouteRuleCode());
    }


    /**
     * details复制
     */
    private void copyDetails(Authentication source, Authentication dest) {
        if ((dest instanceof AbstractAuthenticationToken) && (dest.getDetails() == null)) {
            AbstractAuthenticationToken token = (AbstractAuthenticationToken) dest;
            token.setDetails(source.getDetails());
        }
    }
}
