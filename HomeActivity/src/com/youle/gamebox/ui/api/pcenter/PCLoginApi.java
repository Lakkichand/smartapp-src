package com.youle.gamebox.ui.api.pcenter;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 2014/6/3.
 */
public class PCLoginApi  extends AbstractApi{
    private String userName;
    private String password;
    private String packageVersions ;

    public String getPackageVersions() {
        return packageVersions;
    }

    public void setPackageVersions(String packageVersions) {
        this.packageVersions = packageVersions;
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

    @Override
    protected String getPath() {
        return "/gamebox/account/login";
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }
}
