package com.youle.gamebox.ui.api;

import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-7-1.
 */
public class LogoutApi extends AbstractApi {

    private String sid;


    @Override
    protected String getPath() {
        return "/gamebox/account/logout";
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }
}
