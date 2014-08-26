package com.zhidian.wifibox.db.table;

import android.provider.BaseColumns;

/**
 * 市场第一次启动上传数据到插件数据表
 * 
 * @author zhaoyl
 * 
 */
public class SpkFirstTable implements BaseColumns {

	public static final String TABLE_NAME = "firstspk"; // 表名
	public static final String FIELD_PRIMARY_KEY = _ID;

	public static final String FIELD_BOXNUM = "boxNum";// 盒子编号
	public static final String FIELD_UUID = "uuId";// 手机唯一标识
	public static final String FIELD_INSTALLTIME = "installTime";// 安装时间
	public static final String FIELD_INSTALLPACKAGENAME = "installPackageName"; // 已安装包

	/**
	 * 建表
	 */
	public static String getCreateSQL() {
		String createString = TABLE_NAME + " ( " + FIELD_PRIMARY_KEY
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_BOXNUM
				+ " TEXT, " + FIELD_UUID + " TEXT, " + FIELD_INSTALLTIME
				+ " TEXT, " + FIELD_INSTALLPACKAGENAME + " TEXT " + ");";

		return "CREATE TABLE " + createString;
	}
	
	/**
	 *  删除表
	 */
	public static String getDropSQL() {
		return "DROP TABLE IF EXIST " + TABLE_NAME;
	}
}
