package io.choerodon.oauth.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.oauth.infra.dataobject.AccessTokenDO;

/**
 * @author wuguokai
 */
public interface AccessTokenMapper extends BaseMapper<AccessTokenDO> {
    @Select("SELECT token_id, authentication_id "
            + "FROM oauth_access_token "
            + "WHERE user_name=#{name} AND client_id = #{client} AND authentication_id <> #{id}")
    List<AccessTokenDO> selectTokens(@Param("name") String username, @Param("client") String client,
                                     @Param("id") String authenticationId);

    @Delete("delete from oauth_access_token where user_name = #{name} and client_id = #{client}"
            + " and authentication_id <> #{id}")
    void deleteTokens(@Param("name") String username, @Param("client") String client,
                      @Param("id") String authenticationId);
}
