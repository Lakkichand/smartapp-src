package com.youle.gamebox.ui.api;

import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-6-27.
 */
public class StagoryDetailApi extends AbstractApi {
    @NoteParam
    private String id ;

    public void setId(String id) {
        this.id = id;
    }

    @Override
    protected String getPath() {
        return "/gamebox/gonglue/"+id;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }
}
