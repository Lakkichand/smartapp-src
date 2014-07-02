package com.youle.gamebox.ui.api.pcenter;

import com.ta.util.http.RequestParams;
import com.youle.gamebox.ui.api.AbstractApi;

/**
 * Created by Administrator on 2014/6/3.
 */
public class MymsgboardApi extends AbstractApi{
    private String sid;
    private String latestId;
    private Long uid;
    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getLatestId() {
        return latestId;
    }

    public void setLatestId(String latestId) {
        this.latestId = latestId;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    @Override
    protected String getPath() {
        return "/gamebox/account/msgboard/"+getUid();
    }

    @Override
    public RequestParams getParams() {
        return null;
    }
}
