package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-6-26.
 */
public class MessageBean {
    private long id;//Long 消息Id
    private String  msgTitle;// String 消息类型标题
    private long uid;// Long 用户Id,仅用于 查看动态或留言信息时调用 App的api接口
    private String userName;// String 用户名
    private String toUserName;// String 对方用户名
    private String content;//String 内容
    private String time;//String 时间
    private boolean isRead;//Boolean 是否已读
    //Integer 消息类型【2:礼包、3:论坛、40：私信(you对ta说)、41：私信(ta对you说)、42：动态评论、43：动态回复、44：留言、45：留言回复、46：动态点赞、5:系统、7:活动推荐、8:游戏推荐）】
    private int msgType;
    private String dynamicTime;// String 动态发布时间（当动态为语音时才有： ）
    private String httpUrl;//String 链接地址 （为空时 则不显示链接）
    private long linkId;// Long 用于存放对应的 留言Id/动态Id/游戏Id/礼包Id之类。还便于安卓端 缓存图片或其他功能
    private boolean needNotify = true;

    public boolean isNeedNotify() {
        return needNotify;
    }

    public void setNeedNotify(boolean needNotify) {
        this.needNotify = needNotify;
    }

    public long getId() {
        return id;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
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

    public String getMsgTitle() {
        return msgTitle;
    }

    public long getUid() {
        return uid;
    }

    public String getUserName() {
        return userName;
    }

    public String getToUserName() {
        return toUserName;
    }

    public int getMsgType() {
        return msgType;
    }

    public String getDynamicTime() {
        return dynamicTime;
    }

    public long getLinkId() {
        return linkId;
    }

    public String getHttpUrl() {
        return httpUrl;
    }
}

