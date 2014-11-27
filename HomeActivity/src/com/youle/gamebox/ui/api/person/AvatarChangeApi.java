package com.youle.gamebox.ui.api.person;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.NoteParam;
import com.youle.gamebox.ui.http.HttpMethod;

import java.io.File;

/**
 * Created by Administrator on 14-7-1.
 */
public class AvatarChangeApi extends AbstractApi {

    @Override
    public String getContentType() {
        return "image/jpeg";
       // return "multipart/form-data";
    }

    private String sid;
    private File avatar;

    public void setAvatar(File avatar) {
        this.avatar = avatar;
    }

    @Override
    protected String getPath() {
        return "/gamebox/account/avatar/change";
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }
}
