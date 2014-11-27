package com.youle.gamebox.ui.api.game;

import com.youle.gamebox.ui.api.AbstractApi;

/**
 * Created by Administrator on 14-6-11.
 */
public class HomeGameApi extends AbstractApi {
    @Override
    protected String getPath() {
        return "/gamebox/home/spread/games";
    }
}
