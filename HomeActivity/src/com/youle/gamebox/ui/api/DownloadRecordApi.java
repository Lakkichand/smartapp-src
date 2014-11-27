package com.youle.gamebox.ui.api;

/**
 * Created by Administrator on 14-7-25.
 */
public class DownloadRecordApi extends AbstractApi {
    public static final int START = 0;
    public static final int END = 1;
    private String sid;
    private int type;
    private String appId;
    private String channelCode;
    private String version;
    private String networkType;
    private String deviceCode;
    private String resolution;
    private String phoneModel;
    private String releaseVersion;

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public void setPhoneModel(String phoneModel) {
        this.phoneModel = phoneModel;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    @Override
    protected String getPath() {
        return "/gamebox/game/download/behaviour";
    }

}
