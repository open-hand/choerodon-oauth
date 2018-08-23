package io.choerodon.oauth.infra.common.util;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.oauth.api.service.UserService;
import io.choerodon.oauth.domain.entity.UserE;

/**
 * @author wuguokai
 */
@Service
@SuppressWarnings("unchecked")
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserService userService;

    /**
     * 通过用户名加载用户对象
     *
     * @param username 用户名
     * @return 用户对象，带权限列表
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        UserE user = userService.queryByLoginField(username);
        CustomUserDetails details = new CustomUserDetails(
                user.getLoginName(), user.getPassword(), Collections.emptyList());
        details.setUserId(user.getId());
        details.setLanguage(user.getLanguage());
        details.setTimeZone(user.getTimeZone());
        details.setEmail(user.getEmail());
        details.setOrganizationId(user.getOrganizationId());
        details.setAdmin(user.getAdmin());
        return details;
    }
}
