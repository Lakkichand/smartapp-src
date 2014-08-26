package com.zhidian.wifibox.db.table;

import android.provider.BaseColumns;

/**
 * 市场spk安装表
 * @author zhaoyl
 *
 */
public class InstallSpkTable implements BaseColumns{
	public static final String TABLE_NAME = "installspk"; //表名
	public static final String FIELD_PRIMARY_KEY = _ID;
	
	public static final String FIELD_BOXNUM = "boxNum";//盒子编号
	public static final String FIELD_UUID = "uuId";//手机唯一标识
	public static final String FIELD_INSTALLTIME = "installTime";//安装时间
	public static final String FIELD_INSTALLPACKAGENAME = "installPackageName"; //已安装包	
	public static final String FIELD_MANUFACTURER = "manufacturer";//手机产商
	public static final String FIELD_MODEL = "model";//手机型号
	public static final String FIELD_VERSION = "version";//系统版本
	public static final String FIELD_SIMOPERATORNAME= "simOperatorName";//营运商
	public static final String FIELD_NETWORKCOUNTRYISO = "networkCountryIso";//ISO
	public static final String FIELD_MAC = "mac";//手机mac地址
	public static final String FIELD_IMEI = "imei";//手机IMEI
	public static final String FIELD_IMSI = "imsi";//手机IMSI
	
	/**
	 *  建表
	 */
	public static String getCreateSQL() {
		String createString = TABLE_NAME + " ( " + FIELD_PRIMARY_KEY
				+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + FIELD_BOXNUM
				+ " TEXT, " + FIELD_UUID + " TEXT, " + FIELD_INSTALLTIME + " TEXT, "
				+ FIELD_INSTALLPACKAGENAME + " TEXT, " + FIELD_MANUFACTURER + " TEXT,  "				
				+ FIELD_MODEL + " TEXT,  "+ FIELD_VERSION + " TEXT,  "
				+ FIELD_SIMOPERATORNAME + " TEXT,  "+ FIELD_NETWORKCOUNTRYISO + " TEXT,  "
				+ FIELD_MAC + " TEXT,  "+ FIELD_IMEI + " TEXT,  "
				+ FIELD_IMSI + " TEXT " + ");";

		return "CREATE TABLE " + createString;
	}
	
	/**
	 *  删除表
	 */
	public static String getDropSQL() {
		return "DROP TABLE IF EXIST " + TABLE_NAME;
	}

}
