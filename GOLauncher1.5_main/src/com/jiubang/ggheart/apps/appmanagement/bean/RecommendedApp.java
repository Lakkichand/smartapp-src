package com.jiubang.ggheart.apps.appmanagement.bean;

/**
 * 推荐应用
 * 
 * @author zhoujun
 * 
 */
public class RecommendedApp {
	// public int mId;
	public String mAppId; // 应用id
	public String mTypeId; // 所属分类id
	public String mMarketid; // 安卓市场id
	public String mPackname; // 应用包名
	public String mName; // 应用名称
	public String mIconUrl; // 应用图标url
	public String mVersion; // 版本号
	public String mVersioncode; // 版本code
	public String mSize; // 安装包大小
	public String mSummary; // 简介
	public int mDownloadtype; // 下载方式：1：ftp下载，2：电子市场下载 3：电子市场web版页面
	public String mDownloadurl; // 下载地址
	public int mDetailtype; // 详情类型 1：go精品界面，2：电子市场页面3：电子市场web版页面
	public String mDetailurl; // 详情地址
	public int mUnusual = 0; // 是否特别推荐(备用)

	public String mIconLocalPath; // 应用图标本地路径
	public String mApkLocalPath; // apk 本地存放位置
	// public BitmapDrawable mIcon; //应用图标

	public long mAlreadyDownloadSize;
	public int mPercent;

	// public static final int STATUS_NORMAL = 7;
	public static final int STATUS_WAITING_DOWNLOAD = 1;
	public static final int STATUS_DOWNLOADING = 2;
	public static final int STATUS_DOWNLOAD_COMPLETED = 3;
	public static final int STATUS_DOWNLOAD_FAILED = 4;
	public static final int STATUS_GET_READY = 5;
	public static final int STATUS_CANCELING = 6;

	public static final int STATUS_FOR_NOT_INSTALL = 7;
	public static final int STATUS_FOR_INSTALL = 8;
	public static final int STATUS_FOR_UPDATE = 9;

	public int mStatus = STATUS_FOR_NOT_INSTALL;

	public static final int DOWNLOAD_TYPE_FTP = 1;
	public static final int DOWNLOAD_TYPE_MARKET = 2;
	public static final int DOWNLOAD_TYPE_WEB = 3;

	public int getStatus() {
		return mStatus;
	}

	public void setStatus(int status) {
		this.mStatus = status;
	}
}
