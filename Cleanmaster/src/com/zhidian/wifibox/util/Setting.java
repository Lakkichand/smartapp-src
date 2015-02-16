package com.zhidian.wifibox.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences保存数据工具
 * 
 * @author zhaoyl
 */
public class Setting {
	public static final String SHARED_PREFERENCE_NAME = "wifibox_setting";

	public static final String APPID = "appid";
	public static final String INSTALL_TIME = "install_time"; // 安装时间
	public static final String FIRST_TIME = "first_time"; // 第一次安装
	public static final String WIFI_BOX = "wifi_box"; // 盒子编号
	public static final String TIME_ONLINE = "time_online"; // 允许上网时间
	public static final String INSTALL_STATUS = "install_status"; // 市场spk上传数据状态（上传成功与否）
	public static final String SETTING_ISLOADIMAGER_KEY = "isloadimage";
	public static final String INSTALL_AFTER_DOWNLOAD = "install_after_download"; // 下载完成立即安装
	public static final String DELETE_AFTER_INSTALL = "delete_after_install"; // 安装完成删除软件包
	public static final String UPDATE_TIME = "update_time"; // 版本更新时间
	public static final String UPDATE_COUNT = "UPDATE_COUNT"; // 可更新个数
	public static final String HAS_SHOW_CMODETIP = "HAS_SHOW_CMODETIP";// 是否已经展示普通模式的提示页面
	public static final String HAS_SHOW_XMODETIP = "HAS_SHOW_XMODETIP";// 是否已经展示极速模式的提示页面
	public static final String METER_LAST_POINT = "METER_LAST_POINT";// 咪表上次得分
	public static final String METER_NEED_CALCULATE = "METER_NEED_CALCULATE";// 咪表是否要重新跑分
	public static final String FIRST_UPDATE_CONTENT = "first_update_content";// 管理内容第一次更新后显示
	public static final String OPEN_APP_TIME = "OPEN_APP_TIME"; // 最近一次打开应用的时间
	public static final String TODAYTIME = "todaytime";
	public static final String TIMESTAMP = "TIMESTAMP";// 时间戳
	// 被保护的应用
	public static final String PROTECT_APP = "PROTECT_APP";
	
	public static final String BLACK_APP = "BLACK_APP";
	// 是否已经上传phone表
	public static final String HASREGISTERUSERINFO = "hasRegisterUserInfo";
	// 是否已经展示H5引导页
	public static final String SHOW_H5INTRO = "SHOW_H5INTRO";
	// 上一次展示门店广告的时间
	public static final String SHOW_ADVERTISEMENT_TIME = "SHOW_ADVERTISEMENT_TIME";
	// 上次获取某盒子的时间
	public static final String MIBAO_LOCATION_TIME_PREFIX = "MIBAO_LOCATION_TIME_PREFIX_";
	// 第一次记录运行应用的时间
	public static final String FIRST_RECORD_RUNNINGAPP = "FIRST_RECORD_RUNNINGAPP";
	// 某应用最后一次运行时间
	public static final String APP_LAST_RUNNING_TIME = "_APP_LAST_RUNNING_TIME";
	// 手机清理页面打开次数
	public static final String CLEANMASTER_OPEN_COUNT = "CLEANMASTER_OPEN_COUNT";
	// 成功下载应用个数
	public static final String DOWNLOAD_SUCCESS_COUNT = "DOWNLOAD_SUCCESS_COUNT";
	// 启动装机大师次数
	public static final String OPEN_APP_COUNT = "OPEN_APP_COUNT";
	// 超速下载次数
	public static final String X_DOWNLOAD_COUNT = "X_DOWNLOAD_COUNT";
	// 应用使用总时长
	public static final String APP_USED_TOTAL_TIME = "APP_USED_TOTAL_TIME";
	// 连接内部盒子次数
	public static final String CONNECT_INTERNAL_WIFI_COUNT = "CONNECT_INTERNAL_WIFI_COUNT";
	// 某应用连续下载失败次数
	public static final String APP_DOWNLOAD_FAIL_COUNT = "_APP_DOWNLOAD_FAIL_COUNT";

	private SharedPreferences spf;

	public Setting(Context context) {
		this.spf = context.getSharedPreferences(SHARED_PREFERENCE_NAME,
				Context.MODE_PRIVATE);
	}

	public void putBoolean(String key, boolean value) {
		spf.edit().putBoolean(key, value).commit();
	}

	public boolean getBoolean(String key) {
		return spf.getBoolean(key, false);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		return spf.getBoolean(key, defaultValue);
	}

	public void putString(String key, String value) {
		spf.edit().putString(key, value).commit();
	}

	public String getString(String key) {
		return spf.getString(key, "");
	}

	public void putInt(String key, int value) {
		spf.edit().putInt(key, value).commit();
	}

	public int getInt(String key) {
		return spf.getInt(key, 0);
	}

	public long getLong(String key) {
		return spf.getLong(key, 0);
	}

	public void putLong(String key, long value) {
		spf.edit().putLong(key, value).commit();
	}
	
	public void remove(String key) {
		spf.edit().remove(key).commit();
	}

}
