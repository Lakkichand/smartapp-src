package com.youle.gamebox.ui.bean.pcenter;

/**
 * Created by Administrator on 2014/6/24.
 */
public class MymsgboardCommentsBean {
    private Long id; //Long 留言Id
    private Long uid; //Long 用户Id
    private String nickName; //String 昵称
    private String content; //String内容
    private Boolean isOwn; //Boolean是否属于自己拥有
    private String rNickName; //String 回复留言人的昵称
    private Long ruid; //Long 回复留言人用户的Id
    private  boolean canReply ;//是否可以回复

    public boolean isCanReply() {
        return canReply;
    }

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

    public Boolean getIsOwn() {
        return isOwn;
    }

    public void setIsOwn(Boolean isOwn) {
        this.isOwn = isOwn;
    }

    public String getrNickName() {
        return rNickName;
    }

    public void setrNickName(String rNickName) {
        this.rNickName = rNickName;
    }

    public Long getRuid() {
        return ruid;
    }

    public void setRuid(Long ruid) {
        this.ruid = ruid;
    }
}
