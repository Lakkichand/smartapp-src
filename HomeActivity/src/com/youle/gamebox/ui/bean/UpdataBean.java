package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-7-28.
 */
public class UpdataBean {
    private String version;//String App版本
    private String url;//String 更新文件下载路径
    private String updateTime;//String 更新时间
    private String size;//String  更新文件大小KB
    private String updateDesc;// String 更新功能说明
    private boolean isForce;// Boolean 是否强制更新

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUpdateDesc() {
        return updateDesc;
    }

    public void setUpdateDesc(String updateDesc) {
        this.updateDesc = updateDesc;
    }

    public boolean isForce() {
        return isForce;
    }

    public void setForce(boolean isForce) {
        this.isForce = isForce;
    }
}
