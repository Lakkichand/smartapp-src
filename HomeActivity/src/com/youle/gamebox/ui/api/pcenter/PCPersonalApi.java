package com.youle.gamebox.ui.api.pcenter;

import com.ta.util.http.RequestParams;
import com.youle.gamebox.ui.api.AbstractApi;

/**
 * Created by Administrator on 2014/6/3.
 */
public class PCPersonalApi extends AbstractApi{
    private String uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    protected String getPath() {
        return "/gamebox/account/personal/"+getUid();
    }

    @Override
    public RequestParams getParams() {
        return null;
    }
}
