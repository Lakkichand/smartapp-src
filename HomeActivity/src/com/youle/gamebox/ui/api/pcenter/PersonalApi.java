package com.youle.gamebox.ui.api.pcenter;

import com.ta.util.http.RequestParams;
import com.youle.gamebox.ui.api.AbstractApi;

/**
 * Created by Administrator on 2014/6/27.
 */
public class PersonalApi extends AbstractApi {
    private Long uid;
    private String sid;
    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    @Override


    protected String getPath() {
        return "/gamebox/account/personal/"+getUid();
    }


}
