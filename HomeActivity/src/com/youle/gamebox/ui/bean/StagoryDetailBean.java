package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-6-27.
 */
public class StagoryDetailBean {
    private long id;//Long攻略Id
    private String title;//String标题
    private String date;//String 日期
    private String content;//String 内容
    private String iconUrl;//String 游戏图标url
    private String gameName;//String 游戏名称
    private String size;//String 游戏大小
    private int score;//Integer 分数
    private long appId;//Long 游戏Id
    private String downloadUrl;//String应用下载路径
    private String packageName;//String游戏包名
    private String version;//String版本
    private String forumUrl;//String 讨论区
    private String  downloads;//String下载量
    private String  category;// String 游戏分类名称
    private boolean hasSpree;// Boolean 是否有礼包
    private int source;//Integer 游戏来源 1：Y6 2：91 3：媒介 4：效果

    public int getSource() {
        return source;
    }

    public String getDownloads() {
        return downloads;
    }

    public String getCategory() {
        return category;
    }

    public boolean isHasSpree() {
        return hasSpree;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getGameName() {
        return gameName;
    }

    public String getSize() {
        return size;
    }

    public int getScore() {
        return score;
    }

    public long getAppId() {
        return appId;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getVersion() {
        return version;
    }

    public String getForumUrl() {
        return forumUrl;
    }
}
