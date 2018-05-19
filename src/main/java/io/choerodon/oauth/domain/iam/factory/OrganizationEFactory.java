package io.choerodon.oauth.domain.iam.factory;

import org.springframework.stereotype.Component;

import io.choerodon.core.convertor.ApplicationContextHelper;
import io.choerodon.oauth.domain.iam.entity.OrganizationE;
import io.choerodon.oauth.domain.repository.UserRepository;

/**
 * @author superlee
 */
@Component
public class OrganizationEFactory {

    public static OrganizationE createOrganizationE(
            Long id, String name, Long objectVersionNumber) {
        UserRepository userRepository = ApplicationContextHelper.getSpringFactory().getBean(UserRepository.class);
        return new OrganizationE(id, name, objectVersionNumber, userRepository);
    }
}
