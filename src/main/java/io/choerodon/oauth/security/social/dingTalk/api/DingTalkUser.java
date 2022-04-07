package io.choerodon.oauth.security.social.dingTalk.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hzero.starter.social.core.common.api.SocialUser;

/**
 * dingTalk 用户信息
 * @author hua.zhang03@hand-china.com
 * @date 2022-02-21 10:54
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DingTalkUser implements SocialUser {

	private String userid;

	private String deviceId;

	private Boolean sys;

	private Number sysLevel;

	private String associatedUnionid;

	private String unionid;

	private String name;

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Boolean getSys() {
		return sys;
	}

	public void setSys(Boolean sys) {
		this.sys = sys;
	}

	public Number getSysLevel() {
		return sysLevel;
	}

	public void setSysLevel(Number sysLevel) {
		this.sysLevel = sysLevel;
	}

	public String getAssociatedUnionid() {
		return associatedUnionid;
	}

	public void setAssociatedUnionid(String associatedUnionid) {
		this.associatedUnionid = associatedUnionid;
	}

	public String getUnionid() {
		return unionid;
	}

	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
