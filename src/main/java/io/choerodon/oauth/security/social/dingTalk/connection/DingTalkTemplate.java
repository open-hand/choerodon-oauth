package io.choerodon.oauth.security.social.dingTalk.connection;

import java.util.Map;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.OapiSnsGetuserinfoBycodeRequest;
import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse;
import com.google.common.base.Charsets;
import com.taobao.api.ApiException;
import org.hzero.starter.social.core.common.connect.SocialTemplate;
import org.hzero.starter.social.core.provider.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.choerodon.oauth.util.RequestUtil;

/**
 * @author hua.zhang03@hand-china.com
 * @date 2022-02-21 11:13
 */
public class DingTalkTemplate extends SocialTemplate {

	private static final Logger LOGGER = LoggerFactory.getLogger(DingTalkTemplate.class);
	private static final String WEB = "WEB";

	private String clientId;
	private String clientSecret;
	private String accessTokenUrl;
	private Provider provider;

	public DingTalkTemplate(Provider provider) {
		super(provider);
		setUseParametersForClientAuthentication(true);
		this.clientId = provider.getAppId();
		this.clientSecret = provider.getAppKey();
		this.accessTokenUrl = provider.getAccessTokenUrl() + "?appkey=" + this.clientId + "&appsecret=" + this.clientSecret;
		this.provider = provider;
	}

	public AccessGrant exchangeForAccess(String authorizationCode, String redirectUri, MultiValueMap<String, String> additionalParameters) {
		String unionId;
		Map<String, Object> result = (Map)this.getRestTemplate().getForObject(this.accessTokenUrl, Map.class, new Object[0]);
		if (result == null) {
			throw new RestClientException("access token endpoint returned empty result");
		} else {
			int errCode = (Integer)result.get("errcode");
			String errMsg = (String)result.get("errmsg");
			if (errCode != 0) {
				throw new RestClientException(errMsg);
			} else {
				String way = RequestUtil.obtainParameter("way", "H5");
//				if (WEB.equals(way)) {
					unionId = getUnionId(authorizationCode, provider);
//				} else {
//					unionId = "union";
//				}
				String accessToken = result.get("access_token") + "#" + authorizationCode + "#" + unionId;
				Long expireIn = Long.valueOf(String.valueOf(result.get("expires_in")));
				return this.createAccessGrant(accessToken, (String)null, (String)null, expireIn, (Map)null);
			}
		}
	}

	protected RestTemplate createRestTemplate() {
		RestTemplate restTemplate = super.createRestTemplate();
		restTemplate.getMessageConverters().add(new StringHttpMessageConverter(Charsets.UTF_8));
		return restTemplate;
	}

	/**
	 * @param code 扫码临时授权码
	 * @return 三方用户unionId
	 */
	private String getUnionId (String code, Provider provider) {
		OapiSnsGetuserinfoBycodeResponse response;
		String unionId = null;
		DefaultDingTalkClient defaultDingTalkClient = new DefaultDingTalkClient("https://oapi.dingtalk.com/sns/getuserinfo_bycode");
		OapiSnsGetuserinfoBycodeRequest oapiSnsGetuserinfoBycodeRequest = new OapiSnsGetuserinfoBycodeRequest();
		oapiSnsGetuserinfoBycodeRequest.setTmpAuthCode(code);
		LOGGER.info("TmpAuthCode {}", code);
		try {
			response = defaultDingTalkClient.execute(oapiSnsGetuserinfoBycodeRequest, provider.getAppId(), provider.getAppKey());
			unionId = response.getUserInfo().getUnionid();
		} catch (ApiException e) {
			LOGGER.info("get unionId by authCode error");
			e.printStackTrace();
		}
		return unionId;
	}
}
