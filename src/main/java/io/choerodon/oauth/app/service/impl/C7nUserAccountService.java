package io.choerodon.oauth.app.service.impl;

import com.google.common.base.Joiner;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.hzero.boot.oauth.domain.repository.BaseClientRepository;
import org.hzero.boot.oauth.domain.repository.BasePasswordPolicyRepository;
import org.hzero.boot.oauth.domain.service.BaseUserService;
import org.hzero.boot.oauth.policy.PasswordPolicyManager;
import org.hzero.core.base.BaseConstants;
import org.hzero.oauth.domain.entity.User;
import org.hzero.oauth.domain.repository.UserRepository;
import org.hzero.oauth.security.config.SecurityProperties;
import org.hzero.oauth.security.exception.CustomAuthenticationException;
import org.hzero.oauth.security.exception.LoginExceptions;
import org.hzero.oauth.security.service.impl.DefaultUserAccountService;
import org.springframework.util.CollectionUtils;

import io.choerodon.oauth.infra.dto.MemberRoleE;
import io.choerodon.oauth.infra.dto.TenantE;
import io.choerodon.oauth.infra.mapper.MemberRoleMapper;
import io.choerodon.oauth.infra.mapper.TenantMapper;


/**
 * Created by wangxiang on 2021/8/2
 */
public class C7nUserAccountService extends DefaultUserAccountService {
    private MemberRoleMapper memberRoleMapper;
    private TenantMapper tenantMapper;

    public C7nUserAccountService(UserRepository userRepository,
                                 BaseUserService baseUserService,
                                 PasswordPolicyManager passwordPolicyManager,
                                 BasePasswordPolicyRepository basePasswordPolicyRepository,
                                 BaseClientRepository baseClientRepository,
                                 SecurityProperties securityProperties,
                                 MemberRoleMapper memberRoleMapper,
                                 TenantMapper tenantMapper) {
        super(userRepository, baseUserService, passwordPolicyManager, basePasswordPolicyRepository, baseClientRepository, securityProperties);


        this.memberRoleMapper = memberRoleMapper;
        this.tenantMapper = tenantMapper;
    }

    @Override
    protected void checkUserTenant(User user) {
        // 判断租户是否有效
        if (user.getTenantName() == null) {
            throw new CustomAuthenticationException(LoginExceptions.TENANT_INVALID.value());
        }
        //用户拥有项目或者组织权限的组织，全部被禁用，用户才不能登录
        //查询用户拥有权限的租户（包括平台层）以及在其他组织下有项目权限没有组织角色的组织
        MemberRoleE memberRoleE = new MemberRoleE();
        memberRoleE.setMemberId(user.getId());
        memberRoleE.setMemberType("user");
        List<MemberRoleE> memberRoleES = memberRoleMapper.select(memberRoleE);
        Set<Long> tenantIds = memberRoleES.stream().map(MemberRoleE::getSourceId).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(tenantIds)) {
            return;
        }
        //看看是不是所有的组织都被禁用
        List<TenantE> tenantES = tenantMapper.selectByIds(Joiner.on(BaseConstants.Symbol.COMMA).join(tenantIds));
        List<TenantE> enableTenants = tenantES.stream().filter(tenantE -> BaseConstants.Flag.YES.equals(tenantE.getEnabledFlag())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(enableTenants)) {
            throw new CustomAuthenticationException(LoginExceptions.TENANT_DISABLED.value());
        }

    }
}
