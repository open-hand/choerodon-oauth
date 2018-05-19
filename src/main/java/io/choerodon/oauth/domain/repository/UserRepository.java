package io.choerodon.oauth.domain.repository;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.oauth.domain.iam.entity.UserE;
import io.choerodon.oauth.infra.dataobject.UserDO;

/**
 * @author dongfan117@gmail.com
 */
public interface UserRepository {
    UserE selectByLoginName(String loginName);

    UserE insertSelective(UserE userE);

    Page<UserDO> pageQuery(PageRequest pageRequest, UserDO userDO, String param);

    UserE selectByPrimaryKey(Long id);

    UserE updateSelective(UserE userE);

    void deleteById(Long id);

    void deleteByOrganizationId(Long organizationId);

    UserE findUserByEmailAddressEnable(String emailAddress);

}
