package com.youle.gamebox.ui.api.dynamic;

import com.loopj.android.http.RequestParams;
import com.youle.gamebox.ui.api.AbstractApi;

import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Created by Administrator on 2014/6/3.
 */
public class DymaicCommentPublicApi extends AbstractApi{
    private String sid  ;//服务器登录标识
    private String did	;//动态Id
    private  String cid ;//回复评论ID
    private String content	;//内容，可选
    private File voice	;//语音，可选
    private long voiceTimeLen ;

    public void setVoiceTimeLen(long voiceTimeLen) {
        this.voiceTimeLen = voiceTimeLen;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setVoice(File voice) {
        this.voice = voice;
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

    @Override
    protected String getPath() {
        return "/gamebox/dynamic/comment/publish";
    }
}
