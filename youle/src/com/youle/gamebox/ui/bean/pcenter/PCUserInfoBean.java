package com.youle.gamebox.ui.bean.pcenter;

/**
 * Created by Administrator on 2014/6/3.
 */
public class PCUserInfoBean {
    private Long uid;   // Long用户Id
    private String userName;   //String用户名
    private String nickName;   //String昵称
    private String signature;   //String 签名
    private String qq;   //String QQ号码
    private String email;   //String 电话号码
    private String bigAvatarUrl;   //String 大头像地址
    private String smallAvatarUrl;   //String 小头像地址
    private boolean isSign;
    private int score ;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

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
