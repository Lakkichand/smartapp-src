package com.youle.gamebox.ui.api.dynamic;

import com.youle.gamebox.ui.api.AbstractApi;

/**
 * Created by Administrator on 14-7-15.
 */
public class DeleteCommentApi extends AbstractApi {
    public String sid;
    public String did;
    public String cid;

    @Override
    protected String getPath() {
        return "/gamebox/dynamic/delete";
    }
}
