package com.youle.gamebox.ui.api.dynamic;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.NoteParam;

/**
 * Created by Administrator on 14-7-10.
 */
public class PraiseApi extends AbstractApi {
    @NoteParam
    public static final int AGREE = 0;
    @NoteParam
    public static final int NOT_AGREE = 1;
    String sid;
    long did;
    int type;

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setDid(long did) {
        this.did = did;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    protected String getPath() {
        return "gamebox/dynamic/praise";
    }
}
