package com.youle.gamebox.ui.api;

import com.youle.gamebox.ui.http.HttpMethod;

/**
 * Created by Administrator on 14-7-9.
 */
public class InstallOrDeleteApi extends AbstractApi {
    @NoteParam
    public static final int INSTALL = 0;
    @NoteParam
    public static final int DELETE = -1;
    private String sid;
    private String packageName;
    private int type;
    private String channelCode;
    private String version;
    private String networkType;
    private String deviceCode;
    private String resolution;
    private String phoneModel;
    private String releaseVersion;

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

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setType(int type) {
        this.type = type;
    }


    @Override
    protected String getPath() {
        return "/gamebox/game/manager";
    }
}
