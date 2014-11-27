package com.youle.gamebox.ui.api;

import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-4-30.
 */
public class LatestGameApi extends AbstractApi {
    private String category="latest" ;
    @Override
    protected String getPath() {
        return "/netgame/rank.html";
    }


    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET ;
    }
}
