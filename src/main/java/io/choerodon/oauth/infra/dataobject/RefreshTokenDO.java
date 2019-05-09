package io.choerodon.oauth.infra.dataobject;

import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.choerodon.mybatis.entity.BaseDTO;

/**
 * Created by Eugen on 11/29/2018.
 */
@Table(name = "oauth_refresh_token")
public class RefreshTokenDO extends BaseDTO {

    @Id
    private String tokenId;

    @JsonIgnore
    private byte[] authentication;

    @JsonIgnore
    private byte[] token;

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public byte[] getAuthentication() {
        return authentication;
    }

    public void setAuthentication(byte[] authentication) {
        this.authentication = authentication;
    }

    public byte[] getToken() {
        return token;
    }

    public void setToken(byte[] token) {
        this.token = token;
    }
}
