package io.choerodon.oauth.security.social.dingTalk.api;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.hzero.starter.social.core.common.api.AbstractSocialApi;
import org.hzero.starter.social.core.common.api.SocialUser;
import org.hzero.starter.social.core.exception.ProviderUserNotFoundException;
import org.hzero.starter.social.core.exception.SocialErrorCode;
import org.hzero.starter.social.core.provider.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import io.choerodon.oauth.util.RequestUtil;

/**
 * 通过 access_token 和 code 获取用户信息
 * @author hua.zhang03@hand-china.com
 * @date 2022-02-21 10:56
 */
public class DefaultDingTalkApi extends AbstractSocialApi implements DingTalkApi {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDingTalkApi.class);
	private static final String WEB = "WEB";
	private static final String USER_ID = "/result/userid";

	private String userInfoUrl;
	private String code;
	private String accessToken;
	private String unionId;

	public DefaultDingTalkApi(String accessToken, Provider provider) {
		super(accessToken.split("#")[0]);
		if (accessToken.contains("#")) {
			String[] arr = accessToken.split("#");
			if (arr.length > 1) {
				this.code = arr[1];
				this.unionId = arr[2];
			}
		}

		this.userInfoUrl=provider.getUserInfoUrl()+ "?access_token=" + accessToken.split("#")[0];
		this.accessToken = accessToken;
	}

	@Override
	public SocialUser getUser(String providerUserId) {
		String way = RequestUtil.obtainParameter("way", "H5");
//		if (WEB.equals(way)) {
			LOGGER.info("open user unionId is {}", unionId);
			Map<String, Object> body = new HashMap<>(16);
			body.put("userid", getUserId(unionId));
			JsonNode user = this.getRestTemplate().postForObject("https://oapi.dingtalk.com/topapi/v2/user/get?access_token=" + accessToken, new HttpEntity<>(body), JsonNode.class);
			if (user != null && !StringUtils.isBlank(user.at(USER_ID).asText())) {
				DingTalkUser dingTalkUser = new DingTalkUser();
				dingTalkUser.setUserid(user.at("/result/userid").asText());
				dingTalkUser.setUnionid(unionId);
				dingTalkUser.setName(user.at("/result/name").asText());
				LOGGER.info("web bind user is {}", dingTalkUser);
				return dingTalkUser;
			} else {
				LOGGER.info("not found provider user, result user");
				throw new ProviderUserNotFoundException(SocialErrorCode.PROVIDER_USER_NOT_FOUND);
			}
//		} else {
//			return getUserByH5();
//		}
	}

	/**
	 *  通过免登码实现单点
	 * @return 三方用户
	 */
	private DingTalkUser getUserByH5 () {
		// 获取用户信息
		Map<String, Object> requestBody = new HashMap<>(16);
		requestBody.put("code", this.code);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		JsonNode userInfo = this.getRestTemplate().postForObject(userInfoUrl, new HttpEntity<>(requestBody, headers), JsonNode.class);
		if (userInfo != null && !StringUtils.isBlank(userInfo.at(USER_ID).asText())) {
			DingTalkUser dingTalkUser = new DingTalkUser();
			dingTalkUser.setUserid(userInfo.at("/result/userid").asText());
			dingTalkUser.setDeviceId(userInfo.at("/result/device_id").asText());
			dingTalkUser.setSys(userInfo.at("/result/sys").asBoolean());
			dingTalkUser.setSysLevel(userInfo.at("/result/device_id").asInt());
			dingTalkUser.setAssociatedUnionid(userInfo.at("/result/associated_unionid").asText());
			dingTalkUser.setUnionid(userInfo.at("/result/unionid").asText());
			dingTalkUser.setName(userInfo.at("/result/name").asText());
			LOGGER.info("h5 bind user is {}", dingTalkUser);
			return dingTalkUser;
		}else {
			LOGGER.info("not found provider user, result user={}", userInfo);
			throw new ProviderUserNotFoundException(SocialErrorCode.PROVIDER_USER_NOT_FOUND);
		}
	}

	/**
	 * @param unionId 用户unionId
	 * @return 三方用户userid
	 */
	private String getUserId (String unionId) {
		Map<String, Object> request = new HashMap<>(16);
		request.put("unionid", unionId);
		JsonNode jsonNode = this.getRestTemplate().postForObject("https://oapi.dingtalk.com/topapi/user/getbyunionid?access_token=" + accessToken, new HttpEntity<>(request), JsonNode.class);
		if (jsonNode != null && !StringUtils.isBlank(jsonNode.at(USER_ID).asText())) {
			return jsonNode.at("/result/userid").asText();
		} else {
			LOGGER.info("get userid by unionId error");
			throw new ProviderUserNotFoundException(SocialErrorCode.PROVIDER_USER_NOT_FOUND);
		}
	}
}
