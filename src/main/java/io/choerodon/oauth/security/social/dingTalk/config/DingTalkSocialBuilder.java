package io.choerodon.oauth.security.social.dingTalk.config;

import org.hzero.starter.social.core.common.configurer.SocialConnectionFactoryBuilder;
import org.hzero.starter.social.core.common.connect.SocialConnectionFactory;
import org.hzero.starter.social.core.provider.Provider;
import org.springframework.context.annotation.Configuration;

import io.choerodon.oauth.security.social.dingTalk.connection.DingTalkApiAdapter;
import io.choerodon.oauth.security.social.dingTalk.connection.DingTalkConnectionFactory;
import io.choerodon.oauth.security.social.dingTalk.connection.DingTalkServiceProvider;
import io.choerodon.oauth.security.social.dingTalk.connection.DingTalkTemplate;

/**
 * @author hua.zhang03@hand-china.com
 * @create 2022-02-21 11:10
 */
@Configuration
public class DingTalkSocialBuilder implements  SocialConnectionFactoryBuilder {
	/**
	 * 获取授权码地址
	 */
	private static final String URL_AUTHORIZE = "";
	/**
	 *  获取令牌地址
	 */
	private static final String URL_GET_ACCESS_TOKEN = "https://oapi.dingtalk.com/gettoken";
	/**
	 * 获取用户信息的地址
	 */
	private static final String URL_GET_USER_INFO = "https://oapi.dingtalk.com/topapi/v2/user/getuserinfo";

	@Override
	public String getProviderId() {
		return "ding_talk";
	}

	@Override
	public SocialConnectionFactory buildConnectionFactory(Provider provider) {
		provider.setAuthorizeUrl(URL_AUTHORIZE);
		provider.setAccessTokenUrl(URL_GET_ACCESS_TOKEN);
		provider.setUserInfoUrl(URL_GET_USER_INFO);
		DingTalkApiAdapter dingTalkApiAdapter = new DingTalkApiAdapter();
		DingTalkTemplate dingTalkTemplate = new DingTalkTemplate(provider);
		DingTalkServiceProvider dingTalkServiceProvider = new DingTalkServiceProvider(provider, dingTalkTemplate);
		return new DingTalkConnectionFactory(provider, dingTalkServiceProvider, dingTalkApiAdapter);
	}
}
