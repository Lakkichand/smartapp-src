package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-6-23.
 */
public class GameComentBean {
    private long id;//Long 评论Id
    private String avatarUrl;//String头像Url地址
    private String nickName;//String 昵称
    private String date;//String 时间
    private String content;//String内容
    private long uid;//Long 用户Id

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }
}
