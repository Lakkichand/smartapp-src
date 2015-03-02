package com.zhidian.wifibox.table;

import android.provider.BaseColumns;

/**
 * 数据库表
 * 
 * @author zhaoyl
 * 
 */
public class InstallApkTable implements BaseColumns {

	public static final String TABLE_NAME = "installapk"; // 表名
	public static final String FIELD_PRIMARY_KEY = _ID;

	public static final String FIELD_BOXNUM = "boxnum";// 盒子编号
	public static final String FIELD_CODE = "code";// code
	public static final String FIELD_VERSION = "versionCode";// 版本
	public static final String FIELD_DOWNLOADURL = "downloadUrl";// 下载地址
	public static final String FIELD_STATUS = "status";// 验证结果。0：验证通过 1：验证失败
	public static final String FIELD_TIME = "installTime";// 安装时间
	public static final String FIELD_MSG = "msg";// 错误信息
	public static final String FIELD_UNLOAD_STATUS = "unload_status";// 上传服务器情况：0表示上传成功，1表示上传失败。

	// public static final String FIELD_
	// public static final String FIELD_
	// public static final String FIELD_

	/**
	 * 建表
	 */
	public static String getCreateSQL() {
		String createString = TABLE_NAME + " ( " + FIELD_PRIMARY_KEY
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_BOXNUM
				+ " TEXT, " + FIELD_CODE + " TEXT, " + FIELD_DOWNLOADURL
				+ " TEXT, " + FIELD_VERSION + " TEXT, " + FIELD_STATUS
				+ " TEXT, " + FIELD_TIME + " TEXT, " + FIELD_MSG + " TEXT, "
				+ FIELD_UNLOAD_STATUS + " TEXT " + ");";

		return "CREATE TABLE " + createString;
	}

	/**
	 * 删除表
	 */
	public static String getDropSQL() {
		return "DROP TABLE IF EXIST " + TABLE_NAME;
	}

}
