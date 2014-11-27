package com.youle.gamebox.ui.bean.pcenter;

import com.youle.gamebox.ui.greendao.GameBean;

import java.util.List;

/**
 * Created by Administrator on 2014/6/27.
 */
public class PersonalBean {
    private Long uid ;//Long 用户Id
    private String avatarUrl	;//头像地址 String
    private String nickName	;//昵称 String
    private Boolean signed	;//是否签到 ;//Boolean
    private String email;//String 邮箱
    private Integer score;//  积分

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }


    public Boolean getSigned() {
        return signed;
    }

    public void setSigned(Boolean signed) {
        this.signed = signed;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }
}
