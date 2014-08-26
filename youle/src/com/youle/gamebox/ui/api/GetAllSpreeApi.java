package com.youle.gamebox.ui.api;

import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-6-24.
 */
public class GetAllSpreeApi extends AbstractApi {
    private String keyword;
    @NoteParam
    private String gameId;

    private String sid ;

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    protected String getPath() {
        if (gameId == null) {
            return "/gamebox/spree/list";
        } else {
            return "/gamebox/game/" + gameId + "/spree";
        }
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }
}
