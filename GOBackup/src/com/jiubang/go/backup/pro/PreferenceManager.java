package com.jiubang.go.backup.pro;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * GO备份设置项管理类，提供基础的读写设置接口，并包含所有的设置项的key
 *
 * @author maiyongshen
 */
public class PreferenceManager {
	private static final String GO_BACKUP_PREFERENCES_NAME = "go_backup_preferences";
	// public static final String KEY_SHOULD_SHOW_ROOT_TAB_PROMT =
	// "should_show_root_tab_prompt";
	// public static final String KEY_SHOULD_SHOW_BACKUP_ALERT_DIALOG =
	// "should_show_backup_alert_dialog";
	// public static final String KEY_SHOULD_SHOW_RESTORE_ALERT_DIALOG =
	// "should_show_restore_alert_dialog";
	// public static final String KEY_SHOULD_RESTORE_APP_SILENTLY =
	// "should_restore_app_silently";
	// public static final String KEY_SHOULD_CHECK_UPDATE =
	// "should_check_update";
	// public static final String KEY_BACKUP_SORT_TYPE = "backup_sort_type";
	// public static final String KEY_RESTORE_SORT_TYPE = "resotre_sort_type";
	public static final String KEY_HAS_SHOWN_BACKUP_PROMPT = "has_shown_backup_prompt";
	public static final String KEY_HAS_SHOWN_RESTORE_PROMPT = "has_shown_restore_prompt";
	public static final String KEY_SHOULD_BACKUP_RESTORE_APP_DATA = "should_backup_restore_app_data";
	public static final String KEY_ONLAY_BACKUP_CONTACT_HAS_NUMBER = "only_backup_contact_has_number";
	public static final String KEY_DISCARD_DUPLICATE_CONTACTS = "discard_duplicate_contacts";
	public static final String KEY_HAS_COPY_BIANRY_FILE = "has_copy_binary_file";
	public static final String KEY_BINARY_FILE_VERSION = "binary_file_version"; // busybox以及backup文件版本
	public static final String KEY_GOACCOUNT_USER_ID = "go_account_user_id";
	public static final String KEY_GOACCOUNT_USER_NAME = "go_account_user_name";
	public static final String KEY_CHECK_UPDATE_TIME = "check_update_time";
	public static final String KEY_SHOULD_SHOW_ROOT_ALERT_DIALOG = "should_show_root_alert_dialog";
	public static final String KEY_GOBACKUP_ROOT_PATH = "gobackup_root_path";
	public static final String KEY_ENABLE_SMART_MERGING = "enable_smart_merging";

	public static final String KEY_TUTORIAL_MERGE_SHOW = "key_tutorial_merge_show";

	public static final String KEY_NETWORK_BACKUP_TYPE = "key_network_backup_type";
	public static final String KEY_NETWORK_BACKUP_LOGIN_ID = "key_network_backup_login_id";

	public static final String KEY_HAS_SHOWN_PAY_HELP_PAGE = "key_has_shown_pay_help_page";

	public static final String KEY_BACKUP_CONTACTS_PHOTO = "backup_contacts_photo";

	//联系人RawContact对应的version map，用于匹配联系人是否变更
	public static final String KEY_NEXT_CONTACT_CHAGE_CHECK_TIME = "key_next_contact_chage_check_time";
	public static final String KEY_CONTACT_CHANGE_PUSH = "key_contact_change_push";
	
	public static final String KEY_HAS_SHOWN_FREEZE_APP_TUTORIAL = "has_shown_freeze_app_tutorial";

	// 备份路径
	public static final String KEY_BACKUP_SD_PATH = "key_backup_sd_path";
	
	private static final String KEY_SHOW_NONE_SD_CARD_ALERT = "should_show_none_sd_card_alert";
	
	public static final String KEY_HAS_SHOWN_CLOUD_BACKUP_TAB = "has_shown_cloud_backup_tab";
	public static final String KEY_HAS_SHOWN_APP_TOOLS_TAB = "has_shown_app_tools_tab";
	
	// 主页面 恢复备份新功能提示
	public static final String KEY_RESTORE_BACKUP_NEW_FEATURE = "restore_backup_new_feature";
	public static final String KEY_BACKUP_DETAILS_NEW_FEATURE = "backup_details_new_feature";

	private static PreferenceManager sInstance;

	private PreferenceManager() {

	}

	public static synchronized PreferenceManager getInstance() {
		if (sInstance == null) {
			sInstance = new PreferenceManager();
		}
		return sInstance;
	}

	public Editor getEditor(Context context) {
		if (context != null) {
			SharedPreferences preferences = getPreferences(context);
			return preferences.edit();
		}
		return null;
	}

	private SharedPreferences getPreferences(Context context) {
		if (context != null) {
			return context.getSharedPreferences(GO_BACKUP_PREFERENCES_NAME, Context.MODE_PRIVATE);
		}
		return null;
	}

	public boolean getBoolean(Context context, String key, boolean defValue) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		SharedPreferences preferences = getPreferences(context);
		return preferences.getBoolean(key, defValue);
	}

	public float getFloat(Context context, String key, float defValue) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		SharedPreferences preferences = getPreferences(context);
		return preferences.getFloat(key, defValue);
	}

	public int getInt(Context context, String key, int defValue) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		SharedPreferences preferences = getPreferences(context);
		return preferences.getInt(key, defValue);
	}

	public long getLong(Context context, String key, long defValue) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		SharedPreferences preferences = getPreferences(context);
		return preferences.getLong(key, defValue);
	}

	public String getString(Context context, String key, String defValue) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		SharedPreferences preferences = getPreferences(context);
		return preferences.getString(key, defValue);
	}

	public boolean putBoolean(Context context, String key, boolean value) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		Editor editor = getEditor(context);
		return editor.putBoolean(key, value).commit();
	}

	public boolean putInt(Context context, String key, int value) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		Editor editor = getEditor(context);
		return editor.putInt(key, value).commit();
	}

	public boolean putLong(Context context, String key, long value) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		Editor editor = getEditor(context);
		return editor.putLong(key, value).commit();
	}

	public boolean putString(Context context, String key, String value) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		Editor editor = getEditor(context);
		return editor.putString(key, value).commit();
	}
	
	public void enableShowNoneSdCardAlert(Context context, boolean enable) {
		putBoolean(context, KEY_SHOW_NONE_SD_CARD_ALERT, enable);
	}
	
	public boolean isNoneSdCardAlertEnabled(Context context) {
		return getBoolean(context, KEY_SHOW_NONE_SD_CARD_ALERT, true);
	}
}
