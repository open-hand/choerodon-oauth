package io.choerodon.oauth.infra.mapper;

import org.apache.ibatis.annotations.Param;

import io.choerodon.oauth.infra.dto.ClientE;


/**
 * 〈功能简述〉
 * 〈〉
 *
 * @author wanghao
 * @since 2020/6/1 14:51
 */
public interface ClientC7nMapper {

    ClientE getClientByName(@Param("clientId") String clientId);
}
