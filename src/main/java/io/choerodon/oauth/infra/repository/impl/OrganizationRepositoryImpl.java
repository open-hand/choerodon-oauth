package io.choerodon.oauth.infra.repository.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.oauth.domain.iam.entity.OrganizationE;
import io.choerodon.oauth.domain.repository.OrganizationRepository;
import io.choerodon.oauth.infra.dataobject.OrganizationDO;
import io.choerodon.oauth.infra.mapper.OrganizationMapper;

/**
 * @author wuguokai
 */
@Component
public class OrganizationRepositoryImpl implements OrganizationRepository {

    private OrganizationMapper organizationMapper;

    public OrganizationRepositoryImpl(OrganizationMapper organizationMapper) {
        this.organizationMapper = organizationMapper;
    }

    @Override
    public OrganizationE create(OrganizationE organizationE) {
        OrganizationDO organizationDO = ConvertHelper.convert(organizationE, OrganizationDO.class);
        int isInsert = organizationMapper.insertSelective(organizationDO);
        if (isInsert != 1) {
            int count = organizationMapper.selectCount(new OrganizationDO(organizationDO.getName()));
            if (count > 0) {
                throw new CommonException("error.organization.existed");
            }
            throw new CommonException("error.organization.create");
        }
        return ConvertHelper.convert(organizationMapper.selectByPrimaryKey(organizationDO.getId()), OrganizationE.class);
    }

    @Override
    public OrganizationE update(OrganizationE organizationE) {
        OrganizationDO organizationDO = ConvertHelper.convert(organizationE, OrganizationDO.class);
        int isUpdate = organizationMapper.updateByPrimaryKeySelective(organizationDO);
        if (isUpdate != 1) {
            throw new CommonException("error.organization.update");
        }
        return ConvertHelper.convert(organizationMapper.selectByPrimaryKey(organizationDO.getId()), OrganizationE.class);
    }

    @Override
    public OrganizationDO selectByPrimaryKey(Long organizationId) {
        return organizationMapper.selectByPrimaryKey(organizationId);
    }

    @Override
    public List<OrganizationE> queryAll() {
        return ConvertHelper.convertList(organizationMapper.selectAll(), OrganizationE.class);
    }

    @Override
    public Boolean deleteByKey(Long organizationId) {
        int isDelete = organizationMapper.deleteByPrimaryKey(organizationId);
        if (isDelete != 1) {
            throw new CommonException("error.organization.delete");
        }
        return true;
    }
}
