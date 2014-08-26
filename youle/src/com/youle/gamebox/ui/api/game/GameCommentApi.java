package com.youle.gamebox.ui.api.game;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-6-23.
 */
public class GameCommentApi extends AbstractApi {
    private String id;

    public GameCommentApi(String id) {
        this.id = id;
    }

    @Override
    protected String getPath() {
        return "/gamebox/game/" + id + "/comment";
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }
}
