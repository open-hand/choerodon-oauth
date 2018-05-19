package io.choerodon.oauth.domain.repository;

import java.util.List;

import io.choerodon.oauth.domain.iam.entity.OrganizationE;
import io.choerodon.oauth.infra.dataobject.OrganizationDO;


/**
 * @author wuguokai
 */
public interface OrganizationRepository {

    OrganizationE create(OrganizationE organizationE);

    OrganizationE update(OrganizationE organizationE);

    OrganizationDO selectByPrimaryKey(Long organizationId);

    Boolean deleteByKey(Long organizationId);

    List<OrganizationE> queryAll();
}
