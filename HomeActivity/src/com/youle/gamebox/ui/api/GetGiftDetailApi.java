package com.youle.gamebox.ui.api;

import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-6-27.
 */
public class GetGiftDetailApi extends AbstractApi {
    private String id ;
    private String sid;

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }

    @Override
    protected String getPath() {
        return "/gamebox/spree/"+id;
    }
}
