package io.choerodon.oauth.infra.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidRequestException;
import org.springframework.security.oauth2.common.exceptions.RedirectMismatchException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.endpoint.RedirectResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author flyleft
 */
public class ChoerodonRedirectResolver implements RedirectResolver {

    private static final Logger log = LoggerFactory.getLogger(ChoerodonRedirectResolver.class);

    private Collection<String> redirectGrantTypes = Arrays.asList("implicit", "authorization_code");

    private boolean matchSubdomains = true;

    private boolean matchPorts = true;

    public void setMatchSubdomains(boolean matchSubdomains) {
        this.matchSubdomains = matchSubdomains;
    }

    public void setMatchPorts(boolean matchPorts) {
        this.matchPorts = matchPorts;
    }

    public void setRedirectGrantTypes(Collection<String> redirectGrantTypes) {
        this.redirectGrantTypes = new HashSet<>(redirectGrantTypes);
    }

    @Override
    public String resolveRedirect(String requestedRedirect, ClientDetails client) {

        Set<String> authorizedGrantTypes = client.getAuthorizedGrantTypes();
        if (authorizedGrantTypes.isEmpty()) {
            throw new InvalidGrantException("A client must have at least one authorized grant type.");
        }
        if (!containsRedirectGrantType(authorizedGrantTypes)) {
            throw new InvalidGrantException(
                    "A redirect_uri can only be used by implicit or authorization_code grant types.");
        }

        Set<String> registeredRedirectUris = client.getRegisteredRedirectUri();
        if (registeredRedirectUris == null || registeredRedirectUris.isEmpty()) {
            throw new InvalidRequestException("At least one redirect_uri must be registered with the client.");
        }
        return obtainMatchingRedirect(registeredRedirectUris, requestedRedirect);
    }

    private boolean containsRedirectGrantType(Set<String> grantTypes) {
        for (String type : grantTypes) {
            if (redirectGrantTypes.contains(type)) {
                return true;
            }
        }
        return false;
    }


    protected boolean redirectMatches(String requestedRedirect, String redirectUri) {
        try {
            URL req = new URL(requestedRedirect);
            URL reg = new URL(redirectUri);

            int requestedPort = req.getPort() != -1 ? req.getPort() : req.getDefaultPort();
            int registeredPort = reg.getPort() != -1 ? reg.getPort() : reg.getDefaultPort();

            boolean portsMatch = !matchPorts || (registeredPort == requestedPort);

            if (reg.getProtocol().equals(req.getProtocol()) &&
                    hostMatches(reg.getHost(), req.getHost()) &&
                    portsMatch) {
                return StringUtils.cleanPath(req.getPath()).startsWith(StringUtils.cleanPath(reg.getPath()));
            }
        } catch (MalformedURLException e) {
            log.debug("RedirectMatches error", e);
        }
        return requestedRedirect.equals(redirectUri);
    }


    protected boolean hostMatches(String registered, String requested) {
        if (matchSubdomains) {
            return registered.equals(requested) || requested.endsWith("." + registered);
        }
        return registered.equals(requested);
    }


    private String obtainMatchingRedirect(Set<String> redirectUris, String requestedRedirect) {
        Assert.notEmpty(redirectUris, "Redirect URIs cannot be empty");

        if (redirectUris.size() == 1 && requestedRedirect == null) {
            return redirectUris.iterator().next();
        }

        for (String redirectUri : redirectUris) {
            if (requestedRedirect != null && redirectMatches(requestedRedirect, redirectUri)) {
                // Initialize with the registered redirect-uri
                UriComponentsBuilder redirectUriBuilder = UriComponentsBuilder.fromUriString(redirectUri);

                UriComponents requestedRedirectUri = UriComponentsBuilder.fromUriString(requestedRedirect).build();

                if (this.matchSubdomains) {
                    redirectUriBuilder.host(requestedRedirectUri.getHost());
                }
                if (!this.matchPorts) {
                    redirectUriBuilder.port(requestedRedirectUri.getPort());
                }
                redirectUriBuilder.replaceQuery(requestedRedirectUri.getQuery());
                redirectUriBuilder.fragment(null);
                return redirectUriBuilder.build().toUriString();
            }
        }

        throw new RedirectMismatchException("Invalid redirect: " + requestedRedirect
                + " does not match one of the registered values.");
    }
}
