package io.choerodon.oauth.app.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import org.hzero.oauth.domain.entity.Client;
import org.hzero.oauth.domain.entity.UserOpenAccount;
import org.hzero.oauth.domain.repository.ClientRepository;
import org.hzero.oauth.infra.mapper.UserOpenAccountPlusMapper;
import org.hzero.oauth.security.custom.CustomUserDetailsService;
import org.hzero.oauth.security.util.LoginUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import io.choerodon.core.exception.CommonException;
import io.choerodon.oauth.app.service.ExternalAuthorizationService;

/**
 * @author scp
 * @since 2022/5/26
 */
@Service
public class ExternalAuthorizationServiceImpl implements ExternalAuthorizationService {
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private AuthorizationServerTokenServices authorizationServerTokenServices;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private UserOpenAccountPlusMapper userOpenAccountMapper;


    @Override
    public OAuth2AccessToken authorizationByOpenId(String clientId, String clientSecret, String openId, String providerType) {
        Client client = clientRepository.selectOne(new Client().setName(clientId).setSecret(clientSecret));
        if (client == null) {
            throw new CommonException("error.client.idOrSecret");
        }
        UserOpenAccount userOpenAccount = userOpenAccountMapper.selectOne(new UserOpenAccount().setOpenId(openId).setOpenAppCode(providerType).setTenantIdR(client.getOrganizationId()));
        if (userOpenAccount == null) {
            throw new CommonException("error.openId.noBind");
        }
        return withinAuthorization(userOpenAccount.getUsername(), clientId);
    }

    @Override
    public OAuth2AccessToken withinAuthorization(String username, String clientId) {
        HashMap<String, String> authorizationParameters = new HashMap<>();
        authorizationParameters.put("scope", "default");
        authorizationParameters.put("username", username);
        authorizationParameters.put("client_id", clientId);
        authorizationParameters.put("grant_type", "password");

        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        Set<String> responseType = new HashSet<>();
        responseType.add("password");

        Set<String> scopes = new HashSet<>();
        scopes.add("default");

        OAuth2Request authorizationRequest = new OAuth2Request(authorizationParameters,
                clientId, authorities, true, scopes,
                null, "", responseType, null);
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        request.getSession().setAttribute(LoginUtil.FIELD_CLIENT_ID, clientId);
        UserDetails details = customUserDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(details, null, authorities);
        OAuth2Authentication authenticationRequest = new OAuth2Authentication(authorizationRequest, authenticationToken);
        authenticationRequest.setAuthenticated(true);
        return authorizationServerTokenServices.createAccessToken(authenticationRequest);
    }
}
