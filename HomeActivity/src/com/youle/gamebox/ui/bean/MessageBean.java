package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-6-26.
 */
public class MessageBean {
    private long id;//Long 消息Id
    private String content;//String 内容
    private String time;//String 时间
    private boolean isRead;//Boolean 是否已读
    private int type;//Integer 消息类型
    private String httpUrl;//String 链接地址

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }

    public boolean isRead() {
        return isRead;
    }

    public int getType() {
        return type;
    }

    public String getHttpUrl() {
        return httpUrl;
    }
}

