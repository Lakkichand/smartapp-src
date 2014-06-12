package com.zhidian.wifibox.db.table;

import android.provider.BaseColumns;

/**
 * app安装量、卸载量统计表
 * 
 * @author zhaoyl
 * 
 */
public class AppInstallCountTable implements BaseColumns {
	public static final String TABLE_NAME = "app_stall_count"; // 表名
	public static final String FIELD_PRIMARY_KEY = _ID;

	public static final String FIELD_UUID = "uuId";// 手机唯一标识
	public static final String FIELD_BOXNUM = "boxNum";// 盒子编号
	public static final String FIELD_DOWNLOADSOURCE = "downloadSource";// 下载来源   0、门店下载 1、非门店下载
	public static final String FIELD_APPID = "appId";// appId
	public static final String FIELD_PACKAGENAME = "packageName";// 包名
	public static final String FIELD_VERSION = "version";// 版本号
	public static final String FIELD_INSTALLTIME = "installTime";// 操作时间
	public static final String FIELD_INSTALLTYPE = "installType";// 类型 0、安装 1、卸载
	public static final String FIELD_STATUS = "status";// 安装状态 0、失败 1、成功
	public static final String FIELD_DOWNLOADMODEL = "downloadModel";// 下载模式
																		// 0、急速
																		// 1、普通2、共享
	public static final String FIELD_NETWORKWAY = "networkWay";// 联网方式

	/**
	 * 建表
	 */
	public static String getCreateSQL() {
		String createString = TABLE_NAME + " ( " + FIELD_PRIMARY_KEY
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_BOXNUM
				+ " TEXT, " + FIELD_UUID + " TEXT, " + FIELD_DOWNLOADSOURCE
				+ " TEXT, " + FIELD_APPID + " TEXT, " + FIELD_PACKAGENAME
				+ " TEXT,  " + FIELD_DOWNLOADMODEL + " TEXT,  " + FIELD_VERSION 
				+ " TEXT,  "+ FIELD_INSTALLTYPE + " TEXT,  " + FIELD_STATUS 
				+ " TEXT,  " + FIELD_NETWORKWAY + " TEXT,  "
				+ FIELD_INSTALLTIME + " TEXT " + ");";

		return "CREATE TABLE " + createString;
	}

	/**
	 * 删除表
	 */
	public static String getDropSQL() {
		return "DROP TABLE IF EXIST " + TABLE_NAME;
	}
}
