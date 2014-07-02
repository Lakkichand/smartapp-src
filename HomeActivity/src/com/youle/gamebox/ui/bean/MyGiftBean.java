package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-6-23.
 */
public class MyGiftBean {
    private long id;//Long礼包Id
    private String  title;//String礼包标题
    private String  exchangeFrom;//String兑换开始时间
    private String  exchangeTo;//String兑换结束时间
    private String  activationCode;//String激活码
    private int status;//Integer 状态 0：正常 2：过期

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExchangeFrom() {
        return exchangeFrom;
    }

    public void setExchangeFrom(String exchangeFrom) {
        this.exchangeFrom = exchangeFrom;
    }

    public String getExchangeTo() {
        return exchangeTo;
    }

    public void setExchangeTo(String exchangeTo) {
        this.exchangeTo = exchangeTo;
    }

    public String getActivationCode() {
        return activationCode;
    }

    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
