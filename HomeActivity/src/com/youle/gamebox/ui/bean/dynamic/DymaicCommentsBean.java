package com.youle.gamebox.ui.bean.dynamic;

/**
 * Created by Administrator on 2014/6/3.
 */
public class DymaicCommentsBean {
    private Long   id  ;//Long 评论Id
    private Long  uid  ;//Long 用户Id
    private String nickName  ;//String 昵称
    private Integer   type  ;//Integer 评论类型  1  ;//文字 2  ;//语音
    private String content  ;//String内容
    private String  voiceUrl  ;//String 语音地址
    private Integer  voiceSeconds  ;//Integer 语音长度
    private Boolean  isOwn  ;//Boolean是否属于自己拥有
    private Integer  level  ;//Integer 1  ;//动态的评论  2  ;//动态评论的回复
    private String rNickName  ;//String 回复评论的昵称 （当level=2时才有值）
    private Long   ruid  ;//Long 回复评论用户的Id（当level=2时才有值）

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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVoiceUrl() {
        return voiceUrl;
    }

    public void setVoiceUrl(String voiceUrl) {
        this.voiceUrl = voiceUrl;
    }

    public Integer getVoiceSeconds() {
        return voiceSeconds;
    }

    public void setVoiceSeconds(Integer voiceSeconds) {
        this.voiceSeconds = voiceSeconds;
    }

    public Boolean getIsOwn() {
        return isOwn;
    }

    public void setIsOwn(Boolean isOwn) {
        this.isOwn = isOwn;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
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
