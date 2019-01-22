package io.choerodon.oauth.api.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Service;

import io.choerodon.core.exception.CommonException;
import io.choerodon.oauth.api.service.TokenService;
import io.choerodon.oauth.infra.dataobject.AccessTokenDO;
import io.choerodon.oauth.infra.mapper.AccessTokenMapper;
import io.choerodon.oauth.infra.mapper.RefreshTokenMapper;

/**
 * @author Eugen
 */
@Service
public class TokenServiceImpl implements TokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);

    private static final String SESSION_KEY_PREFIX = "spring:session:sessions:";
    private StringRedisTemplate redisTemplate;
    private FindByIndexNameSessionRepository findByIndexNameSessionRepository;
    private AccessTokenMapper accessTokenMapper;
    private RefreshTokenMapper refreshTokenMapper;

    public TokenServiceImpl(StringRedisTemplate redisTemplate, FindByIndexNameSessionRepository findByIndexNameSessionRepository, AccessTokenMapper accessTokenMapper, RefreshTokenMapper refreshTokenMapper) {
        this.redisTemplate = redisTemplate;
        this.findByIndexNameSessionRepository = findByIndexNameSessionRepository;
        this.accessTokenMapper = accessTokenMapper;
        this.refreshTokenMapper = refreshTokenMapper;
    }

    @Override
    public void deleteOne(String tokenId) {
        //筛选token
        AccessTokenDO accessTokenDO = accessTokenMapper.selectByPrimaryKey(tokenId);
        //token不存在
        if (accessTokenDO == null) {
            throw new CommonException("error.delete.token.not.exist");
        }
        //提取sessionId
        DefaultOAuth2AccessToken deserialize = (DefaultOAuth2AccessToken) SerializationUtils.deserialize(accessTokenDO.getToken());
        //删除redis session
        redisTemplate.delete(SESSION_KEY_PREFIX + deserialize.getAdditionalInformation().get("sessionId"));
        //删除db accessToken/refreshToken
        accessTokenMapper.deleteByPrimaryKey(tokenId);
        refreshTokenMapper.deleteByPrimaryKey(accessTokenDO.getRefreshToken());
        LOGGER.info("delete token,tokenId:{},sessionId:{}",tokenId,deserialize.getAdditionalInformation().get("sessionId"));
    }

    @Override
    public void deleteAllUnderUser(String loginName) {
        //筛选token
        AccessTokenDO accessTokenDO = new AccessTokenDO();
        accessTokenDO.setUserName(loginName);
        List<AccessTokenDO> select = accessTokenMapper.select(accessTokenDO);
        //删除loginName下的所有db accessToken
        accessTokenMapper.deleteUsersToken(loginName);
        //删除对应的db refreshToken
        select.forEach(t ->
                refreshTokenMapper.deleteByPrimaryKey(t.getRefreshToken())
        );
        //删除userId下的所有redis session
        Map<String, Object> byIndexNameAndIndexValue = findByIndexNameSessionRepository.findByIndexNameAndIndexValue(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, loginName);
        redisTemplate.delete(byIndexNameAndIndexValue.keySet().stream().map(s -> SESSION_KEY_PREFIX + s).collect(Collectors.toList()));
    }


    @Override
    public void deleteList(List<String> tokenList) {
        tokenList.forEach(t -> deleteOne(t));
    }
}
