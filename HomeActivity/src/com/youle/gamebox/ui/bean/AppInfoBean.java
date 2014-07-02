/**
 *
 */
package com.youle.gamebox.ui.bean;

import java.io.Serializable;

import android.R.integer;
import android.graphics.drawable.Drawable;
import com.google.gson.annotations.Expose;

/**
 * @author wangsj
 * @time:2013-11-19 上午11:49:52
 */
public class AppInfoBean {
    private Long appId;  //游戏id
    private String name; //游戏名称
    private String packageName; //包名称
    private String version; //版本
    private String versionCode;
    private String size; //大小
    private String updateTime; //更新时间
    private String downUrl; //下载地址
    private String content; //游戏描述
    private boolean boolUpdate = false; //是否更新
    private String oldVersionName; //旧版本
    private int sourse = 1; //平台来源
    private long forumId = 0; //论坛id
    private String iconPath; //icon地址
    private String filePath;
    private boolean isLocation = false;//是否本地，不是本地表明是网络上玩过的

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getDownUrl() {
        return downUrl;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }



    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isBoolUpdate() {
        return boolUpdate;
    }

    public void setBoolUpdate(boolean boolUpdate) {
        this.boolUpdate = boolUpdate;
    }

    public String getOldVersionName() {
        return oldVersionName;
    }

    public void setOldVersionName(String oldVersionName) {
        this.oldVersionName = oldVersionName;
    }

    public int getSourse() {
        return sourse;
    }

    public void setSourse(int sourse) {
        this.sourse = sourse;
    }

    public long getForumId() {
        return forumId;
    }

    public void setForumId(long forumId) {
        this.forumId = forumId;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public boolean isLocation() {
        return isLocation;
    }

    public void setLocation(boolean isLocation) {
        this.isLocation = isLocation;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }
}
