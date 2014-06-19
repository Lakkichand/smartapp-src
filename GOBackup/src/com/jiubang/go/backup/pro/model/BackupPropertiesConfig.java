package com.jiubang.go.backup.pro.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import android.content.Context;

/**
 * @author maiyongshen
 */
public class BackupPropertiesConfig {
	public static final String BACKUP_PROPERTIES_FILE_NAME = "backup.propertie"; // 配置文件名
	public static final String P_DATABASE_VERSION = "database_version"; // 备份配置数据库版本
	public static final String P_OS_VERSION = "os_version"; // 操作系统版本
	public static final String P_SOFTWARE_VERSION_CODE = "soft_version_code"; // 软件版本号
	public static final String P_SOFTWARE_VERSION_NAME = "soft_version_name"; // 软件版本名

	public static final String P_BACKUP_TIME = "backup_time";
	public static final String P_ISROOT = "isRoot"; // 备份是否是root的系统
	public static final String P_BACKUP_SIZE = "backup_size"; // 备份总大小
	public static final String P_BACKUP_SYSTEM_DATA_ITEM_COUNT = "system_data_item_count"; // 备份系统数据个数
	public static final String P_BACKUP_APP_ITEM_COUNT = "app_item_count"; // 备份app个数
	public static final String P_BACKUP_SMS_COUNT = "sms_count"; // 备份短信个数
	public static final String P_BACKUP_CALLLOG_COUNT = "calllog_count"; // 备份通话记录个数
	public static final String P_BACKUP_CONTACTS_COUNT = "contacts_count"; // 备份联系人个数
	public static final String P_BACKUP_WIFI_PATH = "wifi_path"; // 备份的wifi路径
	public static final String P_BACKUP_ACCOUNT_PATH = "account_path"; // account路径
	public static final String P_BACKUP_DICTIONAY_WORD_COUNT = "dictionary_word_count"; // 用户词典内单词个数
	public static final String P_BACKUP_MMS_COUNT = "mms_count"; // 备份彩信个数
	public static final String P_BACKUP_IMAGE_COUNT = "image_count"; //备份图片个数

	private Properties mProp;
	private String mFileName;

	public BackupPropertiesConfig(String fileName) {
		mProp = new Properties();
		mFileName = fileName;
	}

	public BackupPropertiesConfig(Context ctx, String fileName) {
		loadProper(ctx, fileName);
	}

	private Properties loadProper(Context ctx, String fileName) {
		if (ctx == null || fileName == null) {
			return null;
		}
		File file = new File(fileName);
		if (!file.exists()) {
			return null;
		}

		FileInputStream fis = null;
		Properties prop = new Properties();
		try {
			fis = new FileInputStream(file);
			prop.load(fis);
			fis.close();
			fis = null;
		} catch (Exception e) {
			e.printStackTrace();
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			prop = null;
			return null;
		}

		mProp = prop;
		mFileName = fileName;
		return prop;
	}

	public boolean saveProper(Context ctx/* , Properties prop, *//*
																	* String
																	* fileName
																	*/) {
		if (ctx == null /* || prop == null || fileName == null */) {
			return false;
		}
		if (mProp == null || mFileName == null) {
			return false;
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(mFileName, false);
			mProp.store(fos, null);
			fos.close();
			fos = null;
		} catch (Exception e) {
			e.printStackTrace();
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			return false;
		}
		return true;
	}

	public Object put(String key, String value) {
		return mProp != null ? mProp.setProperty(key, value) : null;
	}

	public String get(String key) {
		return mProp != null ? mProp.getProperty(key) : null;
	}

	public boolean containsKey(String key) {
		return mProp != null ? mProp.containsKey(key) : false;
	}
}
