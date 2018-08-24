package io.choerodon.oauth.api.service;

import io.choerodon.oauth.domain.entity.OrganizationE;

/**
 * @author dongfan117@gmail.com
 */
public interface OrganizationService {

    OrganizationE queryOrganizationById(Long organizationId);
}
