package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-6-23.
 */
public class GameComentBean {
    private long id;//Long 评论Id
    private String avatarUrl;//String头像Url地址
    private String userName;//String 昵称
    private String createTime;//String 时间
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
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
