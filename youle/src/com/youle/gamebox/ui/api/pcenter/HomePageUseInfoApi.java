package com.youle.gamebox.ui.api.pcenter;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 2014/6/3.
 */
public class HomePageUseInfoApi extends AbstractApi{

    private String sid ;

    public String getSid() {
        return sid;
    }

    private Long uid;

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    @Override
    protected String getPath() {
        return "/gamebox/account/personal/"+uid;

    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.GET;
    }
}
