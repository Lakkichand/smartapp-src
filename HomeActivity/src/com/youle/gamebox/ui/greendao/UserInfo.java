package com.youle.gamebox.ui.greendao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table USER_INFO.
 */
public class UserInfo {

    private Long uid;
    private String sid;
    private String userName;
    private String nickName;
    private String signature;
    private Boolean isSign;
    private String qq;
    private Integer score;
    private String contact;
    private String bigAvatarUrl;
    private String smallAvatarUrl;
    private Long lastLogin;

    public UserInfo() {
    }

    public UserInfo(Long uid) {
        this.uid = uid;
    }

    public UserInfo(Long uid, String sid, String userName, String nickName, String signature, Boolean isSign, String qq, Integer score, String contact, String bigAvatarUrl, String smallAvatarUrl, Long lastLogin) {
        this.uid = uid;
        this.sid = sid;
        this.userName = userName;
        this.nickName = nickName;
        this.signature = signature;
        this.isSign = isSign;
        this.qq = qq;
        this.score = score;
        this.contact = contact;
        this.bigAvatarUrl = bigAvatarUrl;
        this.smallAvatarUrl = smallAvatarUrl;
        this.lastLogin = lastLogin;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public Boolean getIsSign() {
        return isSign;
    }

    public void setIsSign(Boolean isSign) {
        this.isSign = isSign;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getBigAvatarUrl() {
        return bigAvatarUrl;
    }

    public void setBigAvatarUrl(String bigAvatarUrl) {
        this.bigAvatarUrl = bigAvatarUrl;
    }

    public String getSmallAvatarUrl() {
        return smallAvatarUrl;
    }

    public void setSmallAvatarUrl(String smallAvatarUrl) {
        this.smallAvatarUrl = smallAvatarUrl;
    }

    public Long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Long lastLogin) {
        this.lastLogin = lastLogin;
    }

}
