package com.youle.gamebox.ui.api;

/**
 * Created by Administrator on 14-6-25.
 */
public class GonglueApi extends AbstractApi {
    private String sid;
    private String packages;

    @Override
    protected String getPath() {
        return "/gamebox/gonglue";
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }
}
