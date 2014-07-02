package com.youle.gamebox.ui.api.game;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.NoteParam;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-7-1.
 */
public class GetPersonGameApi extends AbstractApi {
    @NoteParam
    private String uid;

    private String sid;
    public void setId(String uid) {
        this.uid = uid;
    }

    @Override
    protected String getPath() {
        return "/gamebox/account/personal/"+uid;
    }

    public void setSource(String sid) {
        this.sid = sid;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }
}
