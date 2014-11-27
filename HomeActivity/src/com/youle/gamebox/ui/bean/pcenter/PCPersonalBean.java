package com.youle.gamebox.ui.bean.pcenter;


import com.youle.gamebox.ui.greendao.GameBean;

import java.util.List;

/**
 * Created by Administrator on 2014/6/3.
 */
public class PCPersonalBean {
    private String avatarUrl ;//头像地址 String
    private String nickName;//	昵称 String
    private Integer amount	;//游戏数量 Integer
    private Boolean signed	;//是否签到 Boolean
    private List<GameBean> data	;//List<游戏基本对象>

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

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Boolean getSigned() {
        return signed;
    }

    public void setSigned(Boolean signed) {
        this.signed = signed;
    }

    public List<GameBean> getData() {
        return data;
    }

    public void setData(List<GameBean> data) {
        this.data = data;
    }
}
