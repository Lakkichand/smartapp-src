package com.youle.gamebox.ui.api.game;

import com.youle.gamebox.ui.api.AbstractApi;

/**
 * Created by Administrator on 14-6-25.
 */
public class SearchGameApi extends AbstractApi {
    private String keyword;

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    protected String getPath() {
        return "/gamebox/game/list";
    }
}
