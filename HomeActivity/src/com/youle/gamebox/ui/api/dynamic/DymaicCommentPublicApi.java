package com.youle.gamebox.ui.api.dynamic;

import com.youle.gamebox.ui.api.AbstractApi;

/**
 * Created by Administrator on 2014/6/3.
 */
public class DymaicCommentPublicApi extends AbstractApi{
    private String sid  ;//服务器登录标识
    private String did	;//动态Id
    private String content	;//内容，可选
    private String voice	;//语音，可选

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    @Override
    protected String getPath() {
        return "/gamebox/dynamic/comment/publish";
    }
}
