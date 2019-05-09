package io.choerodon.oauth.infra.mapper;

import java.util.List;

import io.choerodon.mybatis.common.Mapper;
import org.apache.ibatis.annotations.Param;

import io.choerodon.oauth.infra.dataobject.AccessTokenDO;

/**
 * @author wuguokai
 */
public interface AccessTokenMapper extends Mapper<AccessTokenDO> {

    List<AccessTokenDO> selectTokens(@Param("name") String username, @Param("client") String client,
                                     @Param("id") String authenticationId);


    void deleteTokens(@Param("name") String username, @Param("client") String client,
                      @Param("id") String authenticationId);

    void deleteUsersToken(@Param("name") String userName);
}
