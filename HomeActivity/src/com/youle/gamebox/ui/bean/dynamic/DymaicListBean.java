package com.youle.gamebox.ui.bean.dynamic;

import java.util.List;

/**
 * Created by Administrator on 2014/6/3.
 */
public class DymaicListBean {
    private Long id  ;//Long 动态Id
    private String action  ;//String动态的动作，例如:发表了、正在玩、获得了成就...等
    private Long uid  ;//Long 用户Id
    private String nickName  ;//String 昵称
    private String avatarUrl  ;//String 头像地址
    private String time  ;//String时间
    private String content  ;//String内容
    private String imageUrl  ;//String 图片地址
    private String imageThumbnailUrl  ;//String图片缩略图地址
    private Long appId  ;//Long 游戏Id
    private String name  ;//String 游戏名
    private String iconUrl  ;//String 游戏图标地址
    private String explain  ;//String 游戏简介
    private String voiceUrl  ;//String 语音地址
    private Integer voiceSeconds  ;//Integer 语音长度
    private Integer tAmount  ;//Integer 文字评论数量
    private Integer vAmount  ;//Integer 语音评论数量
    private Integer lAmount  ;//Integer 赞的数量
    private Boolean isOwn  ;//Boolean是否属于自己拥有
    private Boolean isLike  ;//Boolean是否已经赞过
    private Long totalPages  ;//Long 评论总页数
    private String praiseNames; //String 点赞人名称(多个)
    private String tipMsg; //String  提醒消息(回复了你的动态、评论了你的动态)

    private List<DymaicCommentsBean> comments  ;//List<A> 评论列表 A

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageThumbnailUrl() {
        return imageThumbnailUrl;
    }

    public void setImageThumbnailUrl(String imageThumbnailUrl) {
        this.imageThumbnailUrl = imageThumbnailUrl;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getExplain() {
        return explain;
    }

    public void setExplain(String explain) {
        this.explain = explain;
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

    public Integer gettAmount() {
        return tAmount;
    }

    public void settAmount(Integer tAmount) {
        this.tAmount = tAmount;
    }

    public Integer getvAmount() {
        return vAmount;
    }

    public void setvAmount(Integer vAmount) {
        this.vAmount = vAmount;
    }

    public Integer getlAmount() {
        return lAmount;
    }

    public void setlAmount(Integer lAmount) {
        this.lAmount = lAmount;
    }

    public Boolean getIsOwn() {
        return isOwn;
    }

    public void setIsOwn(Boolean isOwn) {
        this.isOwn = isOwn;
    }

    public Boolean getIsLike() {
        return isLike;
    }

    public void setIsLike(Boolean isLike) {
        this.isLike = isLike;
    }

    public Long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Long totalPages) {
        this.totalPages = totalPages;
    }

    public List<DymaicCommentsBean> getComments() {
        return comments;
    }

    public String getPraiseNames() {
        return praiseNames;
    }

    public void setPraiseNames(String praiseNames) {
        this.praiseNames = praiseNames;
    }

    public String getTipMsg() {
        return tipMsg;
    }

    public void setTipMsg(String tipMsg) {
        this.tipMsg = tipMsg;
    }

    public void setComments(List<DymaicCommentsBean> comments) {
        this.comments = comments;
    }

}
