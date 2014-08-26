package com.youle.gamebox.ui.api;

import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 2014/5/4.
 */
public class SpreeGameListApi extends  AbstractApi{
    private String keyword=""; //搜索关键词， 可选

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    protected String getPath() {
        return "/gamebox/spree";
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }
}
