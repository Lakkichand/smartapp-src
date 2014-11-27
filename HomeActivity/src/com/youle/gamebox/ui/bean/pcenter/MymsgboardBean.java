package com.youle.gamebox.ui.bean.pcenter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2014/6/3.
 */
public class MymsgboardBean implements Serializable {
    private Long id; //Long 留言Id
    private Long uid; //Long留言用户Id
    private String nickName; //String 昵称
    private String content; //String 内容
    private String avatarUrl; //String 头像地址
    private String time; //String 时间
    private Boolean isOwn; //Boolean 是否属于自己发的
    private String tipMsg ;// String  提醒消息(回复了你的留言、给你留言了)
    private List<MymsgboardCommentsBean> comments; //  评论列表

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public Boolean getIsOwn() {
        return isOwn;
    }

    public void setIsOwn(Boolean isOwn) {
        this.isOwn = isOwn;
    }

    public String getTipMsg() {
        return tipMsg;
    }

    public void setTipMsg(String tipMsg) {
        this.tipMsg = tipMsg;
    }

    public List<MymsgboardCommentsBean> getComments() {
        return comments;
    }

    public void setComments(List<MymsgboardCommentsBean> comments) {
        this.comments = comments;
    }
}
