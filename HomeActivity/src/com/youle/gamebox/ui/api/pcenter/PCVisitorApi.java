package com.youle.gamebox.ui.api.pcenter;

import com.youle.gamebox.ui.api.AbstractApi;

/**
 * Created by Administrator on 2014/6/3.
 */
public class PCVisitorApi extends AbstractApi {
    private String sid;

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    @Override
    protected String getPath() {
        return "/gamebox/account/visitor/list";
    }
}
