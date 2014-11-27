package com.youle.gamebox.ui.api;

import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-6-23.
 */
public class MyGiftApi extends AbstractApi {
    private String sid ;
    @Override
    protected String getPath() {
        return "/gamebox/account/spree";
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
}
