package com.zhidian.wifibox.db.table;

import android.provider.BaseColumns;

/**
 * 市场下载app包名
 * 
 * @author zhaoyl
 * 
 */
public class AppPackgeTable implements BaseColumns{

	public static final String TABLE_NAME = "download_packagename"; // 表名
	public static final String FIELD_PRIMARY_KEY = _ID;
	
	public static final String FIELD_PACKAGENAME = "packageName";// 包名
	public static final String FIELD_APPID = "appId";
	public static final String FIELD_DOWNLOADSOURCE = "downloadSource";// 下载来源
	public static final String FIELD_INSTALLTIME = "installTime";// 安装时间
	public static final String FIELD_DOWNLOADMODEL = "downloadModel";// 下载模式
	public static final String FIELD_VERSION = "version";// 版本号
	public static final String FIELD_ACTIVIT = "activit"; //是否激活，0表示未激活，1表示已激活
	
	/**
	 * 建表
	 */
	public static String getCreateSQL() {
		String createString = TABLE_NAME + " ( " + FIELD_PRIMARY_KEY
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ FIELD_APPID + " TEXT, " + FIELD_DOWNLOADSOURCE + " TEXT, "
				+ FIELD_INSTALLTIME + " TEXT, " + FIELD_DOWNLOADMODEL + " TEXT, "
				+ FIELD_VERSION + " TEXT, " + FIELD_ACTIVIT + " TEXT, " + 
				FIELD_PACKAGENAME + " TEXT " + ");";

		return "CREATE TABLE " + createString;
	}

	/**
	 * 删除表
	 */
	public static String getDropSQL() {
		return "DROP TABLE IF EXIST " + TABLE_NAME;
	}
}
