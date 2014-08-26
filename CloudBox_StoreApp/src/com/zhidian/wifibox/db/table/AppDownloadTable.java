package com.zhidian.wifibox.db.table;

import android.provider.BaseColumns;

/**
 * app下载量统计表
 * 
 * @author zhaoyl
 * 
 */
public class AppDownloadTable implements BaseColumns{
	public static final String TABLE_NAME = "download"; // 表名
	public static final String FIELD_PRIMARY_KEY = _ID;

	public static final String FIELD_UUID = "uuId";// 手机唯一标识
	public static final String FIELD_BOXNUM = "boxNum";// 盒子编号
	public static final String FIELD_DOWNLOADSOURCE = "downloadSource";// 下载来源   0、门店下载 1、非门店下载
	public static final String FIELD_APPID = "appId";// appId
	public static final String FIELD_PACKAGENAME = "packageName";// 包名
	public static final String FIELD_VERSION = "version";// 版本号
	public static final String FIELD_DOWNLOADMODEL = "downloadModel";// 下载模式 0、急速 1、普通2、共享
	public static final String FIELD_NETWORKWAY = "networkWay";// 联网方式
	public static final String FIELD_DOWNLOADTIME = "downloadTime";// 下载时间

	/**
	 * 建表
	 */
	public static String getCreateSQL() {
		String createString = TABLE_NAME + " ( " + FIELD_PRIMARY_KEY
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_BOXNUM
				+ " TEXT, " + FIELD_UUID + " TEXT, " + FIELD_DOWNLOADSOURCE
				+ " TEXT, " + FIELD_APPID + " TEXT, " + FIELD_PACKAGENAME
				+ " TEXT,  " + FIELD_DOWNLOADMODEL + " TEXT,  " + FIELD_VERSION
				+ " TEXT,  " + FIELD_NETWORKWAY + " TEXT,  "
				+ FIELD_DOWNLOADTIME + " TEXT " + ");";

		return "CREATE TABLE " + createString;
	}

	/**
	 * 删除表
	 */
	public static String getDropSQL() {
		return "DROP TABLE IF EXIST " + TABLE_NAME;
	}
}
