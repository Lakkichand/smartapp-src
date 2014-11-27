package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-4-28.
 */
public class LatestGameBean {

    private long appId;//游戏Id
    private String name;//游戏名称
    private String iconPath;//游戏图标地址
    private String category;//游戏类型名称
    private String operationsStatus;//游戏状态
    private String stars;//游戏星级
    private boolean recommend;//是否推荐
    private String version;//游戏版本
    private String packageName;
    private String downUrl;

    public String getDownUrl() {
        return downUrl;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOperationsStatus() {
        return operationsStatus;
    }

    public void setOperationsStatus(String operationsStatus) {
        this.operationsStatus = operationsStatus;
    }

    public String getStars() {
        return stars;
    }

    public void setStars(String stars) {
        this.stars = stars;
    }

    public boolean isRecommend() {
        return recommend;
    }

    public void setRecommend(boolean recommend) {
        this.recommend = recommend;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
