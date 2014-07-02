package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-6-24.
 */
public class GiftBean {
    public static final int NOMOR = 0;// 正常
    public static final int HAVE_NO = 1; //领取完毕
    public static final int TIME_OUT = 2; //过期
    public static final int NOT_START = 3; //没开启
    public static final int HAS_GOT = 4; //没开启
    private long id;//Long礼包Id
    private String title;//String礼包标题
    private int total;//Integer 总数
    private int rest;//Integer剩余个数
    private String content;//String 礼包内容
    private int status;//Integer 状态 0：正常 1：领取完毕 2：过期 3：没开启 4：已领

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getTotal() {
        return total;
    }

    public int getRest() {
        return rest;
    }

    public String getContent() {
        return content;
    }

    public int getStatus() {
        return status;
    }
}
