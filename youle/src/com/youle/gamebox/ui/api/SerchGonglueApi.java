package com.youle.gamebox.ui.api;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-6-25.
 */
public class SerchGonglueApi extends AbstractApi {
    private String keyword ;
    private String gameId ;
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
    protected String getPath() {
        if(keyword!=null) {
            return "/gamebox/gonglue/list";
        }else {
            return  "/gamebox/game/"+gameId+"/gonglue" ;
        }
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }
}
