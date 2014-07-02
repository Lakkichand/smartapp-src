package com.youle.gamebox.ui.api;

import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-6-20.
 */
public class MyCategroyApi extends AbstractApi {
   private String  sid;
   private String  packages;

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    @Override
    protected String getPath() {
        return "/gamebox/account/gonglue";
    }
}
