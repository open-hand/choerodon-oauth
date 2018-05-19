package io.choerodon.oauth.domain.iam.entity;

import io.choerodon.core.exception.CommonException;
import io.choerodon.oauth.domain.repository.UserRepository;

/**
 * @author wuguokai
 * @author superlee
 */
public class OrganizationE {

    private Long id;

    private String name;

    private Long objectVersionNumber;

    private UserRepository userRepository;

    private OrganizationE() {
    }

    public OrganizationE(Long id, String name, Long objectVersionNumber, UserRepository userRepository) {
        this.id = id;
        this.name = name;
        this.objectVersionNumber = objectVersionNumber;
        this.userRepository = userRepository;
    }

    public OrganizationE created() {
        return null;
    }

    public Boolean deleted() {
        return false;
    }

    public OrganizationE editInfo() {
        return null;
    }

    public UserE addUser(UserE userE) {
        if (userRepository.selectByLoginName(userE.getLoginName()) != null) {
            throw new CommonException("error.entity.organization.user.exists");
        }
        //TODO
        //密码策略待添加
        //默认添加用户未锁定
        userE.unlocked();
        userE.encodePassword();
        return userRepository.insertSelective(userE);
        //TODO
        //初始化角色
        //用户创建成功发事件
    }

    public UserE updateUser(UserE userE) {
        if (userE.getPassword() != null) {
            //TODO
            //检查密码策略，待添加
            userE.encodePassword();
        }
        return userRepository.updateSelective(userE);
    }

    public void removeUserById(Long id) {
        userRepository.deleteById(id);
    }

    public void removeAllUser() {
        userRepository.deleteByOrganizationId(this.getId());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getObjectVersionNumber() {
        return objectVersionNumber;
    }

    public void setObjectVersionNumber(Long objectVersionNumber) {
        this.objectVersionNumber = objectVersionNumber;
    }
}
