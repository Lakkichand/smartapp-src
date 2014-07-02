package com.youle.gamebox.ui.api;

/**
 * Created by Administrator on 14-6-23.
 */
public class MyGiftApi extends AbstractApi {
    private String sid ;
    @Override
    protected String getPath() {
        return "/gamebox/account/spree";
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
}
