package io.choerodon.oauth.api.service.impl;

import org.springframework.stereotype.Service;

import io.choerodon.oauth.api.service.OrganizationService;
import io.choerodon.oauth.domain.entity.OrganizationE;
import io.choerodon.oauth.infra.mapper.OrganizationMapper;

/**
 * @author dongfan117@gmail.com
 */
@Service("organizationService")
public class OrganizationServiceImpl implements OrganizationService {

    private OrganizationMapper organizationMapper;

    public OrganizationServiceImpl(OrganizationMapper organizationMapper) {
        this.organizationMapper = organizationMapper;
    }

    @Override
    public OrganizationE queryOrganizationById(Long organizationId) {
        return organizationMapper.selectByPrimaryKey(organizationId);
    }
}
