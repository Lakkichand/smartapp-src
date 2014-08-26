package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 2014/5/22.
 */
public class User {
    private Long uid; // 用户Id
    private String userName; //用户名
    private String nickName; //昵称
    private String signature; //签名
    private String qq; //QQ号码
    private String contact; //电话号码
    private String bigAvatarUrl; //大头像地址
    private String smallAvatarUrl; //小头像地址
    private boolean isSign; //签到

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
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

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
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

    public boolean isSign() {
        return isSign;
    }

    public void setSign(boolean isSign) {
        this.isSign = isSign;
    }
}
