package com.youle.gamebox.ui.bean;

import java.util.List;

/**
 * Created by Administrator on 14-6-23.
 */
public class GameDetailBean {
    private long id;//Long游戏Id
    private String name;//String游戏名称
    private String iconUrl;//String游戏图标地址
    private String downloads;//String下载量
    private String size;//String 游戏大小
    private int score;//Integer游戏评分(最高10分，1分半颗星)
    private int source;//Integer 游戏来源 1：Y6 2：91 3：媒介 4：效果
    private String  packageName;//String游戏包名
    private String  version;//String版本
    private String  downloadUrl;//String应用下载路径
    private int status;//Integer 状态 0：正常 1：禁用 当status=1时，只能查看，不能下载
    private String  language;//String 语言
    private String  date;//String 时间
    private String content;//String游戏内容说明
    private String forumUrl;//String 讨论区
    private List<String> screenshotsUrls ;
    private List<LimitBean> sprees ;
    private List<LimitBean> gonglues ;
    private List<LimitBean> news ;
    private List<LimitBean> specials ;
    private List<MiniGameBean> games ;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getDownloads() {
        return downloads;
    }

    public String getSize() {
        return size;
    }

    public int getScore() {
        return score;
    }

    public int getSource() {
        return source;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getVersion() {
        return version;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public int getStatus() {
        return status;
    }

    public String getLanguage() {
        return language;
    }

    public String getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public String getForumUrl() {
        return forumUrl;
    }

    public List<String> getScreenshotsUrls() {
        return screenshotsUrls;
    }

    public List<LimitBean> getSprees() {
        return sprees;
    }

    public List<LimitBean> getGonglues() {
        return gonglues;
    }

    public List<LimitBean> getNews() {
        return news;
    }

    public List<LimitBean> getSpecials() {
        return specials;
    }

    public List<MiniGameBean> getGames() {
        return games;
    }
}
