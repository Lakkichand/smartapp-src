package com.youle.gamebox.ui.api.person;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.http.HttpMethod;

import java.io.File;

/**
 * Created by Administrator on 14-7-1.
 */
public class MsgboardPublishApi extends AbstractApi {

    private String sid;
    private String uid;//留言给谁
    private String content;

    @Override
    protected String getPath() {
        return "/gamebox/account/msgboard/publish";
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }
}
