package com.youle.gamebox.ui.api.game;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.NoteParam;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-6-23.
 */
public class GetGameDetailApi extends AbstractApi {
    @NoteParam
    private String id ;
    private String source ;
    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected String getPath() {
        return "/gamebox/game/"+id;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }
}
