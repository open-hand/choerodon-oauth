package io.choerodon.oauth.infra.common.util;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import io.choerodon.core.oauth.CustomUserDetails;
import io.choerodon.oauth.domain.service.IUserService;
import io.choerodon.oauth.infra.dataobject.UserDO;

/**
 * @author wuguokai
 */
@Service
@SuppressWarnings("unchecked")
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private IUserService userService;

    /**
     * 通过用户名加载用户对象
     *
     * @param username 用户名
     * @return 用户对象，带权限列表
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        UserDO userDO = userService.findUser(username);
        CustomUserDetails details
                = new CustomUserDetails(userDO.getLoginName(), userDO.getPassword(), Collections.emptyList());
        details.setUserId(userDO.getId());
        details.setLanguage(userDO.getLanguage());
        details.setTimeZone(userDO.getTimeZone());
        details.setEmail(userDO.getEmail());
        details.setOrganizationId(userDO.getOrganizationId());
        /*
        if(userDO.getAdditionInfo() != null){
            try{
                details.setAdditionInfo(MAPPER.readValue(userDO.getAdditionInfo(), Map.class));
            }catch (Exception e){
                LOGGER.warn("parser addition info error: {}", e);
            }
        }
        */
        return details;
    }
}
