package io.choerodon.oauth.security.social.dingTalk.connection;

import org.hzero.starter.social.core.common.api.SocialApi;
import org.hzero.starter.social.core.common.connect.SocialServiceProvider;
import org.hzero.starter.social.core.common.connect.SocialTemplate;
import org.hzero.starter.social.core.provider.Provider;

import io.choerodon.oauth.security.social.dingTalk.api.DefaultDingTalkApi;

/**
 * @author hua.zhang03@hand-china.com
 * @date 2022-02-21 11:13
 */
public class DingTalkServiceProvider extends SocialServiceProvider {

	private Provider provider;

	public DingTalkServiceProvider(Provider provider, SocialTemplate template) {
		super(provider, template);
		this.provider = provider;
	}

	@Override
	public SocialApi getSocialApi(String accessToken) {
		return new DefaultDingTalkApi(accessToken, provider);
	}
}
