package io.choerodon.oauth.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.oauth.infra.dataobject.ClientDO;

/**
 * @author wuguokai
 */
public interface ClientMapper extends BaseMapper<ClientDO> {
    List<ClientDO> awesomeSelect(@Param("organizationId") Long organizationId,
                                 @Param("client") ClientDO clientDO);

    ClientDO selectByName(@Param("name") String name);
}
