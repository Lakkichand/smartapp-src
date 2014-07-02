package com.youle.gamebox.ui.api.game;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.NoteParam;

/**
 * Created by Administrator on 14-6-17.
 */
public class CategroryListApi extends AbstractApi {
    @NoteParam
    public static int GAME = 1;
    @NoteParam
    public static int TAG = 2;
    @NoteParam
    private int type;
    private long id ;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CategroryListApi(int type) {
        this.type = type;
    }

    @Override
    protected String getPath() {
        if (type == GAME) {
            return "/gamebox/category/game/list";
        } else {
            return "/gamebox/category/tab/list";
        }
    }
}
