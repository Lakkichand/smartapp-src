package com.youle.gamebox.ui.api;

/**
 * Created by Administrator on 14-7-18.
 */
public class GetSearTipApi extends AbstractApi {
    private int type;
    private String keyword;

    public void setType(int type) {
        this.type = type;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    protected String getPath() {
        return "/gamebox/ajaxSearch";
    }
}
