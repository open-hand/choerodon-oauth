package io.choerodon.oauth.security.social.dingTalk.connection;

import org.hzero.starter.social.core.common.api.SocialApi;
import org.hzero.starter.social.core.common.connect.SocialApiAdapter;
import org.hzero.starter.social.core.common.connect.SocialConnectionFactory;
import org.hzero.starter.social.core.common.connect.SocialServiceProvider;
import org.hzero.starter.social.core.provider.Provider;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.support.OAuth2Connection;

/**
 * @author hua.zhang03@hand-china.com
 * @date 2022-02-21 11:13
 */
public class DingTalkConnectionFactory extends SocialConnectionFactory {
	public DingTalkConnectionFactory(Provider provider, SocialServiceProvider serviceProvider, SocialApiAdapter apiAdapter) {
		super(provider, serviceProvider, apiAdapter);
	}

	@Override
	public Connection<SocialApi> createConnection(ConnectionData data) {
		return new OAuth2Connection<>(data, getServiceProvider(), new DingTalkApiAdapter(data.getProviderUserId()));
	}
}
