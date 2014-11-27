package com.youle.gamebox.ui.bean.pcenter;

/**
 * Created by Administrator on 2014/6/3.
 */
public class PCRegisterBean {
    private Long uid;   // Long用户Id
    private String userName;   //String用户名
    private String nickName;   //String昵称
    private String signature;   //String 签名
    private String qq;   //String QQ号码
    private String contact;   //String 电话号码
    private String bigAvatarUrl;   //String 大头像地址
    private String smallAvatarUrl;   //String 小头像地址

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
}
