package com.zhidian.wifibox.db.table;

import android.provider.BaseColumns;

public class AppDownloadSpeedTable implements BaseColumns {

	public static final String TABLE_NAME = "download_speed"; // 表名
	public static final String FIELD_PRIMARY_KEY = _ID;

	public static final String FIELD_UNIQUE = "app_unique";// 下载记录的唯一标示
	public static final String FIELD_UUID = "uuId";// 手机唯一标识
	public static final String FIELD_BOXNUM = "boxNum";// 盒子编号
	public static final String FIELD_APPID = "appId";// 应用Id
	public static final String FIELD_APPNAME = "appName";// 应用名称
	public static final String FIELD_DOWNLOADTIME = "downloadTime";// 下载时间
	public static final String FIELD_DOWNLOAD_SPEED = "downloadSpeed";// 下载速度
	public static final String FIELD_NETWORKWAY = "networkWay";// 联网方式
	public static final String FIELD_PACKAGENAME = "packageName";// 包名
	public static final String FIELD_VERSION = "version";// 版本号
	public static final String FIELD_DOWNLOADMODEL = "downloadModel";// 下载模式
																		// 0、急速
																		// 1、普通2、共享
	public static final String FIELD_DOWNLOADSOURCE = "downloadSource";// 下载来源
																		// 0、门店下载
																		// 1、非门店下载
	public static final String FIELD_CURRENTSIZE = "currentSize";// 当前已下载应用大小
	public static final String FIELD_TOTALSIZE = "totalSize";// 应用总大小

	/**
	 * 建表
	 */
	public static String getCreateSQL() {
		String createString = TABLE_NAME + " ( " + FIELD_PRIMARY_KEY
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_UNIQUE
				+ " TEXT, " + FIELD_BOXNUM + " TEXT, " + FIELD_UUID + " TEXT, "
				+ FIELD_APPID + " TEXT, " + FIELD_DOWNLOADTIME + " TEXT, "
				+ FIELD_DOWNLOADSOURCE + " TEXT, " + FIELD_DOWNLOADMODEL
				+ " TEXT, " + FIELD_VERSION + " TEXT, " + FIELD_PACKAGENAME
				+ " TEXT, " + FIELD_NETWORKWAY + " TEXT, "
				+ FIELD_DOWNLOAD_SPEED + " TEXT, " + FIELD_CURRENTSIZE
				+ " TEXT, " + FIELD_TOTALSIZE + " TEXT, " + FIELD_APPNAME
				+ " TEXT " + ");";

		return "CREATE TABLE " + createString;
	}

	/**
	 * 删除表
	 */
	public static String getDropSQL() {
		return "DROP TABLE IF EXIST " + TABLE_NAME;
	}

}
