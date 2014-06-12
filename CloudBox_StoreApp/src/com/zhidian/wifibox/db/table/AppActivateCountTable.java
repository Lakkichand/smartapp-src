package com.zhidian.wifibox.db.table;

import android.provider.BaseColumns;

/**
 * app激活量统计表
 * 
 * @author zhaoyl
 * 
 */
public class AppActivateCountTable implements BaseColumns {
	public static final String TABLE_NAME = "app_activate_count"; // 表名
	public static final String FIELD_PRIMARY_KEY = _ID;

	public static final String FIELD_UUID = "uuId";// 手机唯一标识
	public static final String FIELD_BOXNUM = "boxNum";// 盒子编号
	public static final String FIELD_DOWNLOADSOURCE = "downloadSource";// 下载来源   0、门店下载 1、非门店下载
	public static final String FIELD_APPID = "appId";// appId
	public static final String FIELD_PACKAGENAME = "packageName";// 包名
	public static final String FIELD_VERSION = "version";// 版本号
	public static final String FIELD_ACTIVATETIME = "activateTime";// 激活时间
	public static final String FIELD_INSTALLTIME = "installTime";// 安装时间
	public static final String FIELD_ISNETWORK = "isNetwork";// 是否联网 0、否 1、是
	public static final String FIELD_ISINSERTSD = "isInsertSD";// 是否插入SD卡 0、没  1、有																
	public static final String FIELD_DOWNLOADMODEL = "downloadModel";// 下载模式 0、急速 1、普通2、共享
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
				+ " TEXT,  " + FIELD_ACTIVATETIME + " TEXT,  " + FIELD_ISNETWORK 
				+ " TEXT,  " + FIELD_NETWORKWAY + " TEXT,  "+ FIELD_ISINSERTSD + " TEXT,  "
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
