package com.youle.gamebox.ui.api.game;

import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.NoteParam;

/**
 * Created by Administrator on 14-7-7.
 */
public class AddGameCommentApi extends AbstractApi {
    @NoteParam
    private long appId;

    @Override
    protected String getPath() {
        return "/gamebox/game/" + appId + "/comment/publish";
    }

    private String sid;//	服务器登录标识
    private int source;//	1：Y6 2：91 3：媒介 4：效果
    private long score;//	分数(一颗星2分、半颗星1分，最高10分)
    private String content;//	内容

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
