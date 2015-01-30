package com.zhidian.wifibox.db.table;

import android.provider.BaseColumns;

/**
 * 市场启动表
 * @author zhaoyl
 *
 */
public class SpkStartTable implements BaseColumns {
	
	public static final String TABLE_NAME = "spkstart"; //表名
	public static final String FIELD_PRIMARY_KEY = _ID;	
	public static final String FIELD_BOXNUM = "boxNum";//盒子编号
	public static final String FIELD_UUID = "uuId";//手机唯一标识
	public static final String FIELD_STARTTIME = "startTime";//启动时间
	public static final String FIELD_MAC = "mac";//mac
	
	/**
	 *  建表
	 */
	public static String getCreateSQL() {
		String createString = TABLE_NAME + " ( " + FIELD_PRIMARY_KEY
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_BOXNUM
				+ " TEXT, " + FIELD_UUID + " TEXT, " + FIELD_STARTTIME + " TEXT, "
				+ FIELD_MAC +  " TEXT " + ");";

		return "CREATE TABLE " + createString;
	}
	
	/**
	 *  删除表
	 */
	public static String getDropSQL() {
		return "DROP TABLE IF EXIST " + TABLE_NAME;
	}

}
