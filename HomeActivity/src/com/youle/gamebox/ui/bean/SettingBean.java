package com.youle.gamebox.ui.bean;

/**
 * Created by Administrator on 14-4-28.
 */
public class SettingBean {
    private boolean isPush = true ;//推送
    private boolean deleteAfterInstall= true ; //安装后删除
    private boolean autoInstall  = true ;
    private boolean notifyVoice = true ;//提示音
    private boolean autoUpdate = false;
    private boolean cleanCach=true ;
    private boolean cachLost = true;
    private boolean showImage = true ;

    public boolean isShowImage() {
        return showImage;
    }

    public void setShowImage(boolean showImage) {
        this.showImage = showImage;
    }

    public boolean isPush() {
        return isPush;
    }

    public void setPush(boolean isPush) {
        this.isPush = isPush;
    }

    public boolean isCachLost() {
        return cachLost;
    }

    public void setCachLost(boolean cachLost) {
        this.cachLost = cachLost;
    }

    public boolean isDeleteAfterInstall() {
        return deleteAfterInstall;
    }

    public void setDeleteAfterInstall(boolean deleteAfterInstall) {
        this.deleteAfterInstall = deleteAfterInstall;
    }

    public boolean isAutoInstall() {
        return autoInstall;
    }

    public void setAutoInstall(boolean autoInstall) {
        this.autoInstall = autoInstall;
    }

    public boolean isNotifyVoice() {
        return notifyVoice;
    }

    public void setNotifyVoice(boolean notifyVoice) {
        this.notifyVoice = notifyVoice;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public boolean isCleanCach() {
        return cleanCach;
    }

    public void setCleanCach(boolean cleanCach) {
        this.cleanCach = cleanCach;
    }
}
