package com.youle.gamebox.ui.bean.pcenter;

/**
 * Created by Administrator on 2014/6/25.
 */
public class MyVisitorBean {
    private Long uid;  //Long访客用户Id
    private String nickName;  //String 昵称
    private String avatarUrl;  //String 头像地址
    private String time;  //String 时间

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
