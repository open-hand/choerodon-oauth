package io.choerodon.oauth.security.social.dingTalk.connection;

import org.hzero.starter.social.core.common.api.SocialApi;
import org.hzero.starter.social.core.common.connect.SocialApiAdapter;
import org.springframework.social.connect.ConnectionValues;

import io.choerodon.oauth.security.social.dingTalk.api.DingTalkUser;

/**
 * @author hua.zhang03@hand-china.com
 * @date 2022-02-21 11:12
 */
public class DingTalkApiAdapter extends SocialApiAdapter {

	public DingTalkApiAdapter() {
	}

	public DingTalkApiAdapter(String providerUserId) {
		super(providerUserId);
	}

	/**
	 * DingTalk api 与 connection做适配
	 * @param api
	 * @param values
	 */
	@Override
	public void setConnectionValues(SocialApi api, ConnectionValues values) {
		DingTalkUser user = (DingTalkUser) api.getUser(getProviderUserId());
		values.setProviderUserId(user.getUserid());
		values.setDisplayName(user.getName());
		values.setProviderUnionId(user.getUnionid());
	}
}
