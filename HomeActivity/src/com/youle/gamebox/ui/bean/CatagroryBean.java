package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-6-20.
 */
public class CatagroryBean {
    private long id;//Long 游戏Id
    private String  iconUrl;//String 图标地址
    private String name;//String 游戏名称
    private int amount;//Integer 攻略数量
    private int source ;

    public int getSource() {
        return source;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
