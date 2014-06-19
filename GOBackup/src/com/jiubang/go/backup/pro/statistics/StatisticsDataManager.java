package com.jiubang.go.backup.pro.statistics;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.jiubang.go.backup.pro.util.Util;

/**
 * 统计数据管理
 *
 * @author GoBackup Dev Team
 */
public class StatisticsDataManager {
	// 统计数据保存的文件
	private static final String STATISTICS_PREFERENCE = "stat_pref";
	private static StatisticsDataManager sInstance = null;
	// 上传统计数据的间隔时间：8小时
	private static final long UPLOAD_INTERVAL = 8 * 60 * 60 * 1000;

	private StatisticsDataManager() {

	}

	public static synchronized StatisticsDataManager getInstance() {
		if (sInstance == null) {
			sInstance = new StatisticsDataManager();
		}
		return sInstance;
	}

	private Editor getEditor(Context context) {
		if (context != null) {
			SharedPreferences preferences = getPreferences(context);
			return preferences.edit();
		}
		return null;
	}

	private SharedPreferences getPreferences(Context context) {
		if (context != null) {
			return context.getSharedPreferences(STATISTICS_PREFERENCE, Context.MODE_PRIVATE);
		}
		return null;
	}

	public boolean increaseStatisticInt(Context context, String statisticKey, int value) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		int oldValue = getPreferences(context).getInt(statisticKey, 0);
		return getEditor(context).putInt(statisticKey, oldValue + value).commit();
	}

	public boolean decreaseStatisticInt(Context context, String statisticKey, int value) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		int oldValue = getPreferences(context).getInt(statisticKey, 0);
		return getEditor(context).putInt(statisticKey, oldValue - value).commit();
	}

	public int getStatisticInt(Context context, String statisticKey) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		return getPreferences(context).getInt(statisticKey, -1);
	}

	public boolean updateStatisticInt(Context context, String statisticKey, int newValue) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		return getEditor(context).putInt(statisticKey, newValue).commit();
	}

	public boolean updateStatisticBoolean(Context context, String statisticKey, boolean newValue) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		return getEditor(context).putBoolean(statisticKey, newValue).commit();
	}

	public boolean updateStatisticString(Context context, String statisticKey, String newValue) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		return getEditor(context).putString(statisticKey, newValue).commit();
	}

	public String getDeviceId(Context context) {
		if (context == null) {
			return null;
		}
		return getPreferences(context).getString(StatisticsKey.DEVICE_ID, null);
	}

	public int getTimesOfDeletingFromMenu(Context context) {
		if (context == null) {
			return 0;
		}
		return getPreferences(context).getInt(StatisticsKey.MENU_BATCH_DELETE, 0);
	}

	public int getTimesOfDeletingFromSetting(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		return getPreferences(context).getInt(StatisticsKey.SETTING_BATCH_DELETE, 0);
	}

	public boolean hasEnabledMergeContacts(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		return getPreferences(context).getBoolean(StatisticsKey.MERGE_CONTACTS, true);
	}

	public boolean onlyBackupContactsWithNumbers(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		return getPreferences(context).getBoolean(StatisticsKey.CONTACTS_BACKUP_SETTING, true);
	}

	public FunctionState getStateOfBackupOrRestoreAppData(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		int state = getPreferences(context).getInt(StatisticsKey.STATE_BACKUP_RESTORE_APP_DATA,
				FunctionState.UNKNOWN.ordinal());
		if (state == FunctionState.UNKNOWN.ordinal()) {
			if (Util.isRootRom(context)) {
				state = FunctionState.TRUE.ordinal();
			} else {
				state = FunctionState.DISABLE.ordinal();
			}
		}
		return FunctionState.values()[state];
	}

	public FunctionState getStateOfSilentRestore(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("invalid context!");
		}
		int state = getPreferences(context).getInt(StatisticsKey.STATE_SILENT_RESTORE,
				FunctionState.UNKNOWN.ordinal());
		if (state == FunctionState.UNKNOWN.ordinal()) {
			if (Util.isRootRom(context)) {
				state = FunctionState.TRUE.ordinal();
			} else {
				state = FunctionState.DISABLE.ordinal();
			}
		}
		return FunctionState.values()[state];
	}

	public boolean hasGoneIntoRootIntroduction(Context context) {
		if (context == null) {
			return false;
		}
		return getPreferences(context).getBoolean(StatisticsKey.GOINTO_ROOT_INTRODUCTION, false);
	}

	public boolean hasCheckedUpdate(Context context) {
		if (context == null) {
			return false;
		}
		return getPreferences(context).getBoolean(StatisticsKey.HAS_CHECK_UPDATE, false);
	}

	public long getLastUploadTime(Context context) {
		return getPreferences(context).getLong(StatisticsKey.LAST_UPLOAD_TIME, 0);
	}

	public void updateUploadTime(Context context, long time) {
		getEditor(context).putLong(StatisticsKey.LAST_UPLOAD_TIME, time).commit();
	}

	public long getUploadInterval() {
		return UPLOAD_INTERVAL;
	}

	public long getFirstLaunchTime(Context context) {
		return getPreferences(context).getLong(StatisticsKey.FIRST_LAUNCH_TIME, -1);
	}

	public void setFirstLaunchTime(Context context, long time) {
		getEditor(context).putLong(StatisticsKey.FIRST_LAUNCH_TIME, time).commit();
	}

	public long getPurchaseTime(Context context) {
		return getPreferences(context).getLong(StatisticsKey.PURCHASE_TIME, -1);
	}

	public void setPurchaseTime(Context context, long time) {
		getEditor(context).putLong(StatisticsKey.PURCHASE_TIME, time).commit();
	}

	public void setPurchaseSource(Context context, int purchaseSourceCode) {
		updateStatisticInt(context, StatisticsKey.PURCHASE_PREMIUM_VERSION_ENTRANCE,
				purchaseSourceCode);
	}

	public int getPurchaseSource(Context context) {
		return getPreferences(context).getInt(StatisticsKey.PURCHASE_PREMIUM_VERSION_ENTRANCE,
				StatisticsKey.PURCHASE_FROM_INVALID_VALUE);
	}

	//更新进入收费有介绍的次数
	public void setEnterPurchaseHelpActivityCount(Context context, int addCount) {
		int totalCount = getPreferences(context).getInt(
				StatisticsKey.PURCHASE_PREMIUM_VERSION_HELP, 0);
		if (addCount != 0) {
			totalCount += 1;
		} else {
			totalCount = 0;
		}
		updateStatisticInt(context, StatisticsKey.PURCHASE_PREMIUM_VERSION_HELP, totalCount);
	}

	//获得进入收费有介绍的次数
	public int getEnterPurchaseHelpActivityCount(Context context) {
		int totalCount = getPreferences(context).getInt(
				StatisticsKey.PURCHASE_PREMIUM_VERSION_HELP, 0);
		return totalCount;
	}

	//更新点击收费按钮次数
	public void setClickPurchaseButtonCount(Context context, int addCount) {
		int totalCount = getPreferences(context).getInt(StatisticsKey.PURCHASE_BUTTON_CLICK_COUNT,
				0);
		if (addCount != 0) {
			totalCount += 1;
		} else {
			totalCount = 0;
		}
		updateStatisticInt(context, StatisticsKey.PURCHASE_BUTTON_CLICK_COUNT, totalCount);
	}

	//获得点击收费按钮次数
	public int getClickPurchaseButtonCount(Context context) {
		int totalCount = getPreferences(context).getInt(StatisticsKey.PURCHASE_BUTTON_CLICK_COUNT,
				0);
		return totalCount;
	}

	//保存付费方式
	public void setPurchaseMethod(Context context, int purchaseMethod) {
		updateStatisticInt(context, StatisticsKey.PURCHASE_METHOD, purchaseMethod);
	}

	//获得付费方式
	public int getPurchaseMethod(Context context) {
		int purchaseMethod = getPreferences(context).getInt(StatisticsKey.PURCHASE_METHOD,
				StatisticsKey.PURCHASE_UNSELECT_DEFEAULT);
		return purchaseMethod;
	}

	//付费方式点击数
	public void setPurchaseMethodClickCount(Context context, int purchaseMethod) {
		updateStatisticInt(context, StatisticsKey.PURCHASE_METHOD_CLICK_COUNT, purchaseMethod);
	}

	//获得付费方式点击数
	public int getPurchaseMethodClickCount(Context context) {
		int purchaseMethodCount = getPreferences(context).getInt(
				StatisticsKey.PURCHASE_METHOD_CLICK_COUNT, 0);
		return purchaseMethodCount;
	}

	//设置付费行为页上传成功标志
	public void setUploadActionFlag(Context context, boolean success) {
		getEditor(context).putBoolean(StatisticsKey.UPLOAD_ACTION_FLAS, success).commit();
	}
	//获取付费行为页上传成功标志
	public boolean getUploadActionFlag(Context context) {
		return getPreferences(context).getBoolean(StatisticsKey.UPLOAD_ACTION_FLAS, true);
	}

	/**
	 * 状态
	 *
	 * @author GoBackup Dev Team
	 */
	public enum FunctionState {
		FALSE,
		TRUE,
		DISABLE,
		UNKNOWN;
	}

}
