package com.youle.gamebox.ui.api.dynamic;

import com.youle.gamebox.ui.api.AbstractApi;

/**
 * Created by Administrator on 2014/6/3.
 */
public class DymaicPublishApi extends AbstractApi{
    private String sid	;//服务器登录标识
    private String appId	;//游戏Id，@的游戏Id，可选
    private String content	;//内容，可选
    private String image	;//图片，可选
    private String voice	;//语音，可选


    @Override
    protected String getPath() {
        return "/gamebox/dynamic/publish";
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }
}
