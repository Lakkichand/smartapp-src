package com.youle.gamebox.ui.api.person;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.NoteParam;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-7-1.
 */
public class MyGameApi extends AbstractApi {
    private String uid;
    private String packages ;
    private String sid;

    public void setPackages(String packages) {
        this.packages = packages;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    protected String getPath() {
        return "/gamebox/account/personal/game";
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

}
