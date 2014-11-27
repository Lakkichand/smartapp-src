package com.youle.gamebox.ui.api.pcenter;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 2014/6/13.
 */
public class PCUserInfoModfyNickNameApi extends AbstractApi {
    String sid;
    String nickName;

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    protected String getPath() {
        return "/gamebox/account/update/nickname";
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }
}
