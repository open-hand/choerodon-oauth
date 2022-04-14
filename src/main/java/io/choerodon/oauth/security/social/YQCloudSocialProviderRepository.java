package io.choerodon.oauth.security.social;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hzero.boot.oauth.domain.entity.BaseOpenApp;
import org.hzero.boot.oauth.domain.repository.BaseOpenAppRepository;
import org.hzero.oauth.security.social.CustomSocialProviderRepository;
import org.hzero.starter.social.core.provider.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Primary
@Component
public class YQCloudSocialProviderRepository extends CustomSocialProviderRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(YQCloudSocialProviderRepository.class);

    @Autowired
    private BaseOpenAppRepository baseOpenAppRepository;

    @Override
    public List<Provider> getProvider(String providerId) {
        BaseOpenApp params = new BaseOpenApp();
        params.setType(providerId);
        params.setEnabledFlag(true);
        List<BaseOpenApp> apps = this.baseOpenAppRepository.select(params);
        return CollectionUtils.isEmpty(apps) ? Collections.emptyList() : apps.stream().map((app) ->
                new Provider(providerId, "pc", app.getAppId(), app.getAppSecret(), null, app.getTenantId())).collect(Collectors.toList());
    }
}
