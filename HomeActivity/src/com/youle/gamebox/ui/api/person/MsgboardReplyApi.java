package com.youle.gamebox.ui.api.person;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-7-1.
 */
public class MsgboardReplyApi extends AbstractApi {

    private String sid;
    private String mid;//回复给谁
    private String content;

    @Override
    protected String getPath() {
        return "/gamebox/account/msgboard/reply";
    }

    public void setMid(String mid) {
        this.mid = mid;
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
