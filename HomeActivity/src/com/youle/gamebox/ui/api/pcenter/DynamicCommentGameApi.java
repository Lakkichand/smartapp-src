package com.youle.gamebox.ui.api.pcenter;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * 获取本地游戏API
 */
public class DynamicCommentGameApi extends AbstractApi {

	private String sid;
    private String packages ;

    public void setPackages(String packages) {
        this.packages = packages;
    }

    public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	@Override
	protected String getPath() {
		return "/gamebox/dynamic/game/choose";
	}

}
