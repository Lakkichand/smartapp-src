package com.youle.gamebox.ui.api;

/**
 * Created by Administrator on 14-6-27.
 */
public class SearchHomeApi extends AbstractApi {
    private String sid ;

    public void setSid(String sid) {
        this.sid = sid;
    }

    @Override
    protected String getPath() {
        return "/gamebox/search/tab";
    }
}
