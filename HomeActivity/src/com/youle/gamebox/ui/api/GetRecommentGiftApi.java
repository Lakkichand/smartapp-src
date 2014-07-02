package com.youle.gamebox.ui.api;

import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-6-24.
 */
public class GetRecommentGiftApi extends AbstractApi {
    private String sid;
    private String packages;

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getPackages() {
        return packages;
    }

    public void setPackages(String packages) {
        this.packages = packages;
    }

    @Override
    protected String getPath() {
        return "/gamebox/spree";
    }

}
