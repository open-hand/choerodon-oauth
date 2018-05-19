package io.choerodon.oauth.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.common.BaseMapper;
import io.choerodon.oauth.infra.dataobject.UserDO;


/**
 * @author wuguokai
 */
public interface UserMapper extends BaseMapper<UserDO> {
    Page<UserDO> fulltextSearch(@Param("userDO") UserDO userDO,
                                @Param("param") String param);

    List<UserDO> queryByProjectId(@Param("projectId") Long projectId,
                                  @Param("userDO") UserDO userDO);

    List<UserDO> selectMembersByRoleId(@Param("role_id") Long roleId);

    List<UserDO> selectAsyncUsers(@Param("organizationId") Long organizationId, @Param("ids") List<Integer> ids);
}