package com.youle.gamebox.ui.api.game;

import com.youle.gamebox.ui.api.AbstractApi;

/**
 * Created by Administrator on 14-6-16.
 */
public class GameUpdateApi extends AbstractApi {
    public void setPackageVersions(String packageVersions) {
        this.packageVersions = packageVersions;
    }

    private String packageVersions ;
    @Override
    protected String getPath() {
        return "/gamebox/game/checking/update";
    }
}
