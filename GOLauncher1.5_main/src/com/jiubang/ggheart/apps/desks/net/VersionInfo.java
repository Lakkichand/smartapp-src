package com.jiubang.ggheart.apps.desks.net;

public class VersionInfo {

	public static int UPDATE_FORCE = 1; // 强制更新
	public static int UPDATE_TIP = 2; // 提示更新
	public static int NORMAL_VERSION = 3; // 正常版本
	public static int SOFT_MAINTENANCE = 4; // 软件维护
	public static int SYSTEM_INFO = 5; // 系统提示
	public static int USER_DEFINED = 6; // 自定义

	public int mAction = 0; // 动作
	public String mTipInfo; // 提示信息
	public String mDownUrl; // 下载地址
	public String mReleaseDate;// 发布日期
	public String mNewVer; // 最新版本号
	public String mReleaseNote;// 版本描述信息

}
