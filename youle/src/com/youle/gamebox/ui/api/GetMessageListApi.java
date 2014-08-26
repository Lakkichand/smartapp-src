package com.youle.gamebox.ui.api;

/**
 * Created by Administrator on 14-6-26.
 */
public class GetMessageListApi extends AbstractApi {
    private String sid;
    private int type;

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    protected String getPath() {
        return "gamebox/account/message/list";
    }
}
