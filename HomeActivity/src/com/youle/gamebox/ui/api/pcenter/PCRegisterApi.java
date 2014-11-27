package com.youle.gamebox.ui.api.pcenter;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 2014/6/3.
 */
public class PCRegisterApi extends AbstractApi{
    private String userName;
    private String password;
    @Override
    protected String getPath() {
        return "/gamebox/account/register";
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
