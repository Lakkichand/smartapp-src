package com.youle.gamebox.ui.api;

import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-7-28.
 */
public class CheckUpdataApi extends AbstractApi {
    private int versionCode;
    private String channelCode;

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    @Override
    protected String getPath() {
        return "/gamebox/version";
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }
}
