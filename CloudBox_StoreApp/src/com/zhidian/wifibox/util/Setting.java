package com.zhidian.wifibox.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * SharedPreferences保存数据工具
 * 
 * @author zhaoyl
 */
public class Setting {
	private static Setting instance;

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
	public static final String METER_UPDATE_TIME = "METER_UPDATE_TIME";// 咪表更新时间
	public static final String FIRST_UPDATE_CONTENT = "first_update_content";// 管理内容第一次更新后显示
	public static final String OPEN_APP_TIME = "OPEN_APP_TIME"; // 最近一次打开应用的时间
	// 被保护的应用
	public static final String PROTECT_APP = "PROTECT_APP";

	private SharedPreferences spf;

	@SuppressLint("WorldWriteableFiles")
	@SuppressWarnings("deprecation")
	public Setting(Context context) {
		this.spf = context.getSharedPreferences(SHARED_PREFERENCE_NAME,
				Context.MODE_WORLD_WRITEABLE);
	}

	public static Setting getInstance(Context context) {
		if (instance == null) {
			instance = new Setting(context);
		}
		return instance;
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

}
