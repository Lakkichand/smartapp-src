package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-6-24.
 */
public class HotGiftBean {
    private long id;//Long 礼包Id，
    private String iconUrl;//String 图标地址，
    private String gName;//String 游戏名称，
    private String sName;//String 礼包名
    private int total;//Integer 礼包总个数
    private int rest;//Integer 剩余礼包数
    private int status;//Integer 状态：status 0：正常 1：领取完毕 2：过期 3：没开启 4：已领

    public int getTotal() {
        return total;
    }

    public int getRest() {
        return rest;
    }

    public int getStatus() {
        return status;
    }

    public long getId() {
        return id;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getgName() {
        return gName;
    }

    public String getsName() {
        return sName;
    }
}

