package com.youle.gamebox.ui.bean.pcenter;

/**
 * Created by Administrator on 2014/6/3.
 */
public class PCVisitorBean {
    private Long uid;  //Long访客用户Id
    private Long nickName;  //Long 昵称
    private String avatarUrl;  //String 头像地址
    private String time;  //String 时间

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public Long getNickName() {
        return nickName;
    }

    public void setNickName(Long nickName) {
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
