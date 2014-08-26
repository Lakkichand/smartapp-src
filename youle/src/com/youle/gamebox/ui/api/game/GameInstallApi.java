package com.youle.gamebox.ui.api.game;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.NoteParam;

/**
 * Created by Administrator on 14-6-16.
 */
public class GameInstallApi extends AbstractApi {
    private String packageNames ;


    public void setPackageNames(String packageNames) {
        this.packageNames = packageNames;
    }

    @Override
    protected String getPath() {
        return "/gamebox/game/checking/local";
    }
}
