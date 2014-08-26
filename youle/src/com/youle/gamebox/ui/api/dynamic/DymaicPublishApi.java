package com.youle.gamebox.ui.api.dynamic;

import com.loopj.android.http.RequestParams;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.http.HttpMethod;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Created by Administrator on 2014/6/3.
 */
public class DymaicPublishApi extends AbstractApi{
    private String sid	;//服务器登录标识
    private String appId	;//游戏Id，@的游戏Id，可选
    private String content	;//内容，可选
    private File image	;//图片，可选
    private  File voice	;//语音，可选
    private long voiceTimeLen ;

    public void setVoiceTimeLen(long voiceTimeLen) {
        this.voiceTimeLen = voiceTimeLen;
    }

    @Override
    protected String getPath() {
        return "/gamebox/dynamic/publish";
    }



    @Override
    public RequestParams getParams() {
        RequestParams p = super.getParams();
        byte[] b = {1,2,32} ;
        p.put("xxx",new ByteArrayInputStream(b));
        return  p;
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

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }

    public File getVoice() {
        return voice;
    }

    public void setVoice(File voice) {
        this.voice = voice;
    }
}
