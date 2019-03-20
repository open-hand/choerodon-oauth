package io.choerodon.oauth.domain.service;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.choerodon.core.oauth.CustomClientDetails;
import io.choerodon.oauth.domain.entity.ClientE;
import io.choerodon.oauth.infra.mapper.ClientMapper;

/**
 * @author wuguokai
 */
@Service
@SuppressWarnings("unchecked")
public class CustomClientDetailsService implements ClientDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomClientDetailsService.class);
    @Autowired
    private ClientMapper clientMapper;

    public void setClientMapper(ClientMapper clientMapper) {
        this.clientMapper = clientMapper;
    }

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public ClientDetails loadClientByClientId(String name) {
        ClientE clientE = this.selectByName(name);
        if (clientE == null) {
            throw new NoSuchClientException("No client found : " + name);
        }
        String redirectUrl = clientE.getWebServerRedirectUri();
        if (!redirectUrl.endsWith("/")) {
            clientE.setWebServerRedirectUri(redirectUrl + "/");
        }
        CustomClientDetails clientDetails = new CustomClientDetails();
        clientDetails.setAuthorizedGrantTypes(StringUtils
                .commaDelimitedListToSet(clientE.getAuthorizedGrantTypes()));
        clientDetails.setClientId(clientE.getName());
        clientDetails.setClientSecret(clientE.getSecret());
        clientDetails.setResourceIds(StringUtils.commaDelimitedListToSet(clientE.getResourceIds()));
        clientDetails.setScope(StringUtils.commaDelimitedListToSet(clientE.getScope()));
        clientDetails.setRegisteredRedirectUri(StringUtils
                .commaDelimitedListToSet(clientE.getWebServerRedirectUri()));
        clientDetails.setAuthorities(Collections.emptyList());
        int accessTokenValidity = clientE.getAccessTokenValidity() != null ? clientE.getAccessTokenValidity().intValue() : 3600;
        clientDetails.setAccessTokenValiditySeconds(accessTokenValidity);
        int refreshTokenValidity = clientE.getRefreshTokenValidity() != null ? clientE.getRefreshTokenValidity().intValue() : 3600;
        clientDetails.setRefreshTokenValiditySeconds(refreshTokenValidity);
        clientDetails.setOrganizationId(1L);
        String json = clientE.getAdditionalInformation();
        if (json != null) {
            try {
                Map<String, Object> additionalInformation = mapper.readValue(json, Map.class);
                clientDetails.setAdditionalInformation(additionalInformation);
            } catch (Exception e) {
                LOGGER.warn("parser addition info error: {}", e);
            }
        }
        clientDetails.setAutoApproveScopes(StringUtils.commaDelimitedListToSet(clientE.getAutoApprove()));
        return clientDetails;
    }

    private ClientE selectByName(String name) {
        ClientE client = new ClientE();
        client.setName(name);
        return clientMapper.selectOne(client);
    }
}
