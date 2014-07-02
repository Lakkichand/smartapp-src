package com.youle.gamebox.ui.api.game;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-6-3.
 */
public class IndexOrdinaryApi extends AbstractApi {
    @Override
    protected String getPath() {
        return "/gamebox/home/spread/ordinary";
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }
}
