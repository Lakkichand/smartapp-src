package com.youle.gamebox.ui.api;

import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-6-3.
 */
public class GameTagApi extends AbstractApi {
    @Override
    protected String getPath() {
        return "/gamebox/category/tab";
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET ;
    }
}
