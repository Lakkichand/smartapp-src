package com.youle.gamebox.ui.api.game;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-6-16.
 */
public class GameCategroryApi extends AbstractApi {
    @Override
    protected String getPath() {
        return "/gamebox/category/game";
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }
}
