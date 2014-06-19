package com.jiubang.go.backup.pro.statistics;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.text.TextUtils;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.data.IRecord;
import com.jiubang.go.backup.pro.data.RestorableRecord;
import com.jiubang.go.backup.pro.model.AppSizeLoader;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.product.manage.ProductManager;
import com.jiubang.go.backup.pro.product.manage.ProductPayInfo;
import com.jiubang.go.backup.pro.schedules.BackupPlanDBHelper;
import com.jiubang.go.backup.pro.util.Util;

/**
 * 上传GO备份的统计数据工具类
 *
 * @author maiyongshen
 */
public class StatisticsTool {
	private static final String SEPARATOR = "||";
	private static final String LINE_BREAK = "\r\n";

	private static final int FUNCTION_ID_BACKUP_COUNT = 1;
	private static final int FUNCTION_ID_BACKUP_CONTENT = 2;
	private static final int FUNCTION_ID_BACKUP_PREFERENCE = 3;
	private static final int FUNCTION_ID_USER_INFO = 4;
	private static final int FUNCTION_ID_FUCTION = 5;

	private static final int BACKUP_CONTENT_SMS = 1;
	private static final int BACKUP_CONTENT_CONTACTS = 2;
	private static final int BACKUP_CONTENT_GO_LAUNCHER_SETTTING = 3;
	private static final int BACKUP_CONTENT_CALL_LOG = 4;

	private static final int PF_MENU_BATCH_DELETE = 1;
	private static final int PF_SETTING_BATCH_DELETE = 2;
	private static final int PF_CONTACTS_BACKUP_SETTING = 3;
	private static final int PF_BACKUP_RESTORE_APP_DATA = 4;
	private static final int PF_SILENT_RESTORE = 5;
	private static final int PF_ROOT_INTRODUCTION = 6;
	private static final int PF_CHECK_UPDATE = 7;
	private static final int PF_MERGE_CONTACTS = 8;

	private static final int BACKUP_ALL_CONTACTS = 0;
	private static final int BACKUP_ONLY_CONTACTS_WITH_NUMBERS = 1;

	private static final int STATE_FALSE = 0;
	private static final int STATE_TRUE = 1;

	private static final int ENABLED = 1;
	private static final int DISABLED = 0;

	// 一级功能点4，用户信息
	// 用户手机应用程序大小
	private static final int USER_APP_SIZE_INFO = 1;
	// 用户购买高级版的时间信息
	private static final int USER_TIME_INFO = 2;
	// 用户从设置菜单进入“高级版”的次数
	private static final int PREMIUM_PAGE_ENTRANCE_MENU = 3;
	// 用户从备份大小限制进入“高级版”的次数
	private static final int PREMIUM_PAGE_ENTRANCE_BACKUP_SIZE_LIMIT = 4;
	// 用户从备份系统设置进入“高级版”的次数
	private static final int PREMIUM_PAGE_ENTRANCE_BACKUP_SYSTEM_SETTING = 5;
	// 用户从云端备份进入“高级版”的次数
	private static final int PREMIUM_PAGE_ENTRANCE_CLOUD_BACKUP = 6;
	// 用户购买高级版的入口标识
	private static final int PURCHASE_ENTRANCE = 7;
	// 用户从编辑备份包功能进入“高级版”的次数
	private static final int PREMIUM_PAGE_ENTRANCE_EDIT_BACKUP = 8;
	// 用户从单独备份应用数据功能进入“高级版”的次数
	private static final int PREMIUM_PAGE_ENTRANCE_BACKUP_APP_DATA_ONLY = 9;

	// 一级功能点5，程序功能统计
	// 是否启用本地定时备份
	private static final int ENABLE_SCHEDULE_BAKCUP = 1;

	// 默认随机IMEI号
	private static final String DEFAULT_VIRTUAL_DEVICE_ID = "0000000000000000";
	// 统计数据使用的编码
	private static final String STATISTICS_DATA_CODE = "UTF-8";
	// 统计数据加密密钥
	private static final String STATISTICS_DATA_ENCRYPT_KEY = "lvsiqiaoil611230";
	// GO备份统计服务器上传地址
	private static final String UPLOAD_URL = "http://goupdate.3g.cn/GOClientData/DR";
	//	 private static final String UPLOAD_URL = "http://192.168.214.145:8080/goLogger/bak";
	// 服务器响应超时时间
	private static final int HTTP_REQUEST_TIMEOUT = 30 * 1000;
	private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	// 电子市场渠道号
	private static final int CHANNEL_GOOGLE_MARKET = 200;

	public synchronized static void uploadStatisticsData(final Context context,
			final boolean isIntoActivity) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				uploadBaseStatisticsInfoInternal(context, isIntoActivity);
				StatisticsDataManager sdm = StatisticsDataManager.getInstance();
				if (!sdm.getUploadActionFlag(context)) {
					uploadPayActionStatisticsInfo(context);
				}
			}
		}).start();
	}

	private static void uploadBaseStatisticsInfoInternal(Context context, boolean isIntoActivity) {
		if (context == null) {
			return;
		}
		if (!checkNetworkState(context)) {
			return;
		}
		String statisticsInfo = getBasicStatisticsData(context, isIntoActivity);
		if (statisticsInfo == null || statisticsInfo.equals("")) {
			statisticsInfo = "";
		}

		String individualExtensionInfo = getIndividualExtensionInfo(context);
		if (!TextUtils.isEmpty(individualExtensionInfo)) {
			statisticsInfo += LINE_BREAK;
			statisticsInfo += individualExtensionInfo;
		}

		try {
			statisticsInfo = URLEncoder.encode(statisticsInfo, STATISTICS_DATA_CODE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (statisticsInfo != null) {
			statisticsInfo = CryptTool.encrypt(statisticsInfo, STATISTICS_DATA_ENCRYPT_KEY);
		}
		URL uploadUrl = null;
		try {
			uploadUrl = new URL(UPLOAD_URL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		postStatisticsInfo(context, uploadUrl, statisticsInfo);
	}

	//上传付费行为
	public static void uploadPayActionStatisticsInfo(Context context) {
		StatisticsDataManager sdm = StatisticsDataManager.getInstance();
		String statisticsInfo = getPayActionStatisticInfo(context);
		if (statisticsInfo == null || statisticsInfo.equals("")) {
			return;
		}
		try {
			statisticsInfo = URLEncoder.encode(statisticsInfo, STATISTICS_DATA_CODE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (statisticsInfo != null) {
			statisticsInfo = CryptTool.encrypt(statisticsInfo, STATISTICS_DATA_ENCRYPT_KEY);
		}
		URL uploadUrl = null;
		try {
			uploadUrl = new URL(UPLOAD_URL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		boolean success = postStatisticsInfo(context, uploadUrl, statisticsInfo);
		if (success) {
			//上传成功后，删除一些信息
			cleanUpdata(context, sdm);
		} else {
			sdm.setUploadActionFlag(context, false);
		}

	}

	//上传APP个性行为
	public static void uploadAPPIndividualExtensionInfo(Context context) {
		String statisticsInfo = getIndividualExtensionInfo(context);
		if (statisticsInfo == null || statisticsInfo.equals("")) {
			return;
		}
		try {
			statisticsInfo = URLEncoder.encode(statisticsInfo, STATISTICS_DATA_CODE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (statisticsInfo != null) {
			statisticsInfo = CryptTool.encrypt(statisticsInfo, STATISTICS_DATA_ENCRYPT_KEY);
		}
		URL uploadUrl = null;
		try {
			uploadUrl = new URL(UPLOAD_URL);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}
		postStatisticsInfo(context, uploadUrl, statisticsInfo);
	}

	private static String getBasicStatisticsData(Context context, boolean isIntoMainActivity) {
		if (context == null) {
			return null;
		}
		StringBuffer data = new StringBuffer();
		try {

			//日志序列
			data.append(13);
			data.append(SEPARATOR);

			//Android ID
			String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
			data.append(deviceId);
			data.append(SEPARATOR);

			//日志打印时间
			long currentTime = System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT);
			String dateString = formatter.format(currentTime);
			data.append(dateString);
			data.append(SEPARATOR);

			//国家
			String countryCode = Util.getCountryCode(context);
			data.append(countryCode);
			data.append(SEPARATOR);

			// 渠道号
			data.append(getProductChannelCode(context));
			data.append(SEPARATOR);

			// GO备份版本名
			data.append(Util.getVersionName(context));
			data.append(SEPARATOR);

			//是否付费用户
			data.append(Util.isInland(context) ? 0 : ProductManager.isPaid(context)
					|| ProductPayInfo.sIsPaidUserByKey ? 1 : 0);
			data.append(SEPARATOR);

			//是否进入主程序
			data.append(isIntoMainActivity ? 1 : 0);
			data.append(SEPARATOR);

			// 虚拟的IMEI号
			data.append(getVirtualDeviceId(context));
			data.append(SEPARATOR);

			// GO备份版本号
			data.append(Util.getVersionCode(context));

			// 手机的Root状态
			//			data.append(Util.isRootRom(context) ? STATE_TRUE : STATE_FALSE);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return data.toString();
	}

	private static String getAdvancedStatisticsData(Context context) {
		if (context == null) {
			return null;
		}
		StringBuffer data = new StringBuffer();
		try {
			final String virtualDeviceId = getVirtualDeviceId(context);

			// 用户的备份项数量
			data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_BACKUP_COUNT)
					.append(SEPARATOR).append(getBackupCount(context)).append(LINE_BREAK);

			// 备份项内容
			// 短信备份数量
			data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_BACKUP_CONTENT)
					.append(SEPARATOR).append(BACKUP_CONTENT_SMS).append(SEPARATOR)
					.append(getSmsBackupCount(context)).append(LINE_BREAK);

			// 联系人备份数量
			data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_BACKUP_CONTENT)
					.append(SEPARATOR).append(BACKUP_CONTENT_CONTACTS).append(SEPARATOR)
					.append(getContactsBackupCount(context)).append(LINE_BREAK);

			// 通话记录备份数量
			data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_BACKUP_CONTENT)
					.append(SEPARATOR).append(BACKUP_CONTENT_CALL_LOG).append(SEPARATOR)
					.append(getCallLogBackupCount(context)).append(LINE_BREAK);

			// GO桌面设置备份数量
			data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_BACKUP_CONTENT)
					.append(SEPARATOR).append(BACKUP_CONTENT_GO_LAUNCHER_SETTTING)
					.append(SEPARATOR).append(getGOLauncherSettingBackupCount(context))
					.append(LINE_BREAK);

			StatisticsDataManager sdm = StatisticsDataManager.getInstance();

			// 设置选项内容
			// 用户从菜单进入“批量删除”的次数
			data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_BACKUP_PREFERENCE)
					.append(SEPARATOR).append(PF_MENU_BATCH_DELETE).append(SEPARATOR)
					.append(sdm.getTimesOfDeletingFromMenu(context)).append(LINE_BREAK);

			// 用户从设置项进入“批量删除”的次数
			data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_BACKUP_PREFERENCE)
					.append(SEPARATOR).append(PF_SETTING_BATCH_DELETE).append(SEPARATOR)
					.append(sdm.getTimesOfDeletingFromSetting(context)).append(LINE_BREAK);

			// 联系人备份设置
			boolean onlyBackupContactsWithNumbers = sdm.onlyBackupContactsWithNumbers(context);
			data.append(virtualDeviceId)
					.append(SEPARATOR)
					.append(FUNCTION_ID_BACKUP_PREFERENCE)
					.append(SEPARATOR)
					.append(PF_CONTACTS_BACKUP_SETTING)
					.append(SEPARATOR)
					.append(onlyBackupContactsWithNumbers
							? BACKUP_ONLY_CONTACTS_WITH_NUMBERS
							: BACKUP_ALL_CONTACTS).append(LINE_BREAK);

			// 是否勾选备份应用程序数据
			data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_BACKUP_PREFERENCE)
					.append(SEPARATOR).append(PF_BACKUP_RESTORE_APP_DATA).append(SEPARATOR)
					.append(sdm.getStateOfBackupOrRestoreAppData(context).ordinal())
					.append(LINE_BREAK);

			// 是否勾选静默恢复
			data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_BACKUP_PREFERENCE)
					.append(SEPARATOR).append(PF_SILENT_RESTORE).append(SEPARATOR)
					.append(sdm.getStateOfSilentRestore(context).ordinal()).append(LINE_BREAK);

			// 是否勾选合并联系人
			data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_BACKUP_PREFERENCE)
					.append(SEPARATOR).append(PF_MERGE_CONTACTS).append(SEPARATOR)
					.append(sdm.hasEnabledMergeContacts(context) ? STATE_TRUE : STATE_FALSE)
					.append(LINE_BREAK);

			// 是否进入ROOT权限说明页
			data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_BACKUP_PREFERENCE)
					.append(SEPARATOR).append(PF_ROOT_INTRODUCTION).append(SEPARATOR)
					.append(sdm.hasGoneIntoRootIntroduction(context) ? STATE_TRUE : STATE_FALSE)
					.append(LINE_BREAK);

			// 是否检查过更新
			data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_BACKUP_PREFERENCE)
					.append(SEPARATOR).append(PF_CHECK_UPDATE).append(SEPARATOR)
					.append(sdm.hasCheckedUpdate(context) ? STATE_TRUE : STATE_FALSE)
					.append(LINE_BREAK);

			// 用户应用程序大小
			long allUserAppSize = new AppSizeLoader(context, BackupManager.getInstance()
					.getUserAppInfoList(context)).calcAllAppSize();
			data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_USER_INFO)
					.append(SEPARATOR).append(USER_APP_SIZE_INFO).append(SEPARATOR)
					.append(allUserAppSize).append(LINE_BREAK);

			// 用户首次启动时间与购买时间
			long firstLaunchTime = sdm.getFirstLaunchTime(context);
			if (firstLaunchTime > 0) {
				data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_USER_INFO)
						.append(SEPARATOR).append(USER_TIME_INFO).append(SEPARATOR)
						.append(firstLaunchTime).append(SEPARATOR);
				long purchaseTime = sdm.getPurchaseTime(context);
				if (purchaseTime > 0) {
					data.append(purchaseTime).append(LINE_BREAK);
				} else {
					data.append(LINE_BREAK);
				}
			}

			// 用户是否启用定时备份
			data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_FUCTION)
					.append(SEPARATOR).append(ENABLE_SCHEDULE_BAKCUP).append(SEPARATOR)
					.append(isScheduleBackupEnabled(context) ? ENABLED : DISABLED)
					.append(LINE_BREAK);

			// 用户从设置菜单进入高级版页面的次数
			data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_USER_INFO)
					.append(SEPARATOR).append(PREMIUM_PAGE_ENTRANCE_MENU).append(SEPARATOR)
					.append(sdm.getStatisticInt(context, StatisticsKey.PREMIUM_ENTRANCE_MENU))
					.append(LINE_BREAK);

			// 用户从备份大小限制进入高级版页面的次数
			data.append(virtualDeviceId)
					.append(SEPARATOR)
					.append(FUNCTION_ID_USER_INFO)
					.append(SEPARATOR)
					.append(PREMIUM_PAGE_ENTRANCE_BACKUP_SIZE_LIMIT)
					.append(SEPARATOR)
					.append(sdm.getStatisticInt(context,
							StatisticsKey.PREMIUM_ENTRANCE_BACKUP_SIZE_LIMIT)).append(LINE_BREAK);

			// 用户从备份系统数据进入高级版页面的次数
			data.append(virtualDeviceId)
					.append(SEPARATOR)
					.append(FUNCTION_ID_USER_INFO)
					.append(SEPARATOR)
					.append(PREMIUM_PAGE_ENTRANCE_BACKUP_SYSTEM_SETTING)
					.append(SEPARATOR)
					.append(sdm.getStatisticInt(context,
							StatisticsKey.PREMIUM_ENTRANCE_BACKUP_SYSTEM_SETTING))
					.append(LINE_BREAK);

			// 用户从云端备份进入高级版页面的次数
			data.append(virtualDeviceId)
					.append(SEPARATOR)
					.append(FUNCTION_ID_USER_INFO)
					.append(SEPARATOR)
					.append(PREMIUM_PAGE_ENTRANCE_CLOUD_BACKUP)
					.append(SEPARATOR)
					.append(sdm.getStatisticInt(context,
							StatisticsKey.PREMIUM_ENTRANCE_CLOUD_BACKUP)).append(LINE_BREAK);

			data.append(virtualDeviceId)
					.append(SEPARATOR)
					.append(FUNCTION_ID_USER_INFO)
					.append(SEPARATOR)
					.append(PREMIUM_PAGE_ENTRANCE_EDIT_BACKUP)
					.append(SEPARATOR)
					.append(sdm
							.getStatisticInt(context, StatisticsKey.PREMIUM_ENTRANCE_EDIT_BACKUP))
					.append(LINE_BREAK);

			data.append(virtualDeviceId)
					.append(SEPARATOR)
					.append(FUNCTION_ID_USER_INFO)
					.append(SEPARATOR)
					.append(PREMIUM_PAGE_ENTRANCE_BACKUP_APP_DATA_ONLY)
					.append(SEPARATOR)
					.append(sdm.getStatisticInt(context,
							StatisticsKey.PREMIUM_ENTRANCE_BACKUP_APP_DATA_ONLY))
					.append(LINE_BREAK);

			// 用户购买高级版的入口
			int purchaseSource = sdm.getPurchaseSource(context);
			if (purchaseSource != StatisticsKey.PURCHASE_FROM_INVALID_VALUE) {
				data.append(virtualDeviceId).append(SEPARATOR).append(FUNCTION_ID_USER_INFO)
						.append(SEPARATOR).append(PURCHASE_ENTRANCE).append(SEPARATOR)
						.append(purchaseSource).append(LINE_BREAK);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return data.toString();
	}

	//用户付费行为
	public static String getPayActionStatisticInfo(Context context) {
		if (context == null) {
			return null;
		}
		StatisticsDataManager sdm = StatisticsDataManager.getInstance();
		StringBuffer data = new StringBuffer();
		try {

			//日志序列
			data.append(15);
			data.append(SEPARATOR);

			//Android ID
			String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
			data.append(deviceId);
			data.append(SEPARATOR);

			//日志打印时间
			long currentTime = System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT);
			String dateString = formatter.format(currentTime);
			data.append(dateString);
			data.append(SEPARATOR);

			//付费入口
			data.append(sdm.getPurchaseSource(context));
			data.append(SEPARATOR);

			//进入高级功能介绍页面次数
			data.append(sdm.getEnterPurchaseHelpActivityCount(context));
			data.append(SEPARATOR);

			//购买按钮点击数
			data.append(sdm.getClickPurchaseButtonCount(context));
			data.append(SEPARATOR);

			//付费方式
			data.append(sdm.getPurchaseMethod(context));
			data.append(SEPARATOR);

			//付费方式点击数
			data.append(sdm.getPurchaseMethodClickCount(context));
			data.append(SEPARATOR);

			//成功购买次数
			data.append(sdm.getPurchaseTime(context) > -1 ? 1 : 0);
			data.append(SEPARATOR);

			// 虚拟的IMEI号
			data.append(getVirtualDeviceId(context));
			data.append(SEPARATOR);

			//国家
			String countryCode = Util.getCountryCode(context);
			data.append(countryCode);
			data.append(SEPARATOR);

			// 渠道号
			data.append(getProductChannelCode(context));
			data.append(SEPARATOR);

			// GO备份版本号
			data.append(Util.getVersionCode(context));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return data.toString();
	}

	//	个性化统计扩展协议
	public static String getIndividualExtensionInfo(Context context) {
		if (context == null) {
			return null;
		}
		StringBuffer data = new StringBuffer();
		try {

			//日志序列
			data.append(16);
			data.append(SEPARATOR);

			//Android ID
			String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
			data.append(deviceId);
			data.append(SEPARATOR);

			//日志打印时间
			long currentTime = System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT);
			String dateString = formatter.format(currentTime);
			data.append(dateString);
			data.append(SEPARATOR);

			//产品标识
			data.append(4);
			data.append(SEPARATOR);

			//统计项
			data.append(1);
			data.append(SEPARATOR);

			// 统计值
			data.append(Util.isRootRom(context) ? STATE_TRUE : STATE_FALSE);
			data.append(SEPARATOR);

			// 虚拟的IMEI号
			data.append(getVirtualDeviceId(context));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data.toString();

	}

	private static void cleanUpdata(Context context, StatisticsDataManager sdm) {
		if (sdm == null) {
			return;
		}
		//进入高级功能介绍页面次数清零
		sdm.setEnterPurchaseHelpActivityCount(context, 0);
		//购买按钮点击数清零
		sdm.setClickPurchaseButtonCount(context, 0);
		//付费方式点击数清零
		sdm.setPurchaseMethodClickCount(context, 0);
		//成功购买次数清零
		sdm.setPurchaseTime(context, -1);
		sdm.setPurchaseMethod(context, StatisticsKey.PURCHASE_UNSELECT_DEFEAULT);
		//上传成功
		sdm.setUploadActionFlag(context, true);

	}

	public static String getVirtualDeviceId(Context context) {
		String deviceId = null;
		if (context != null) {
			deviceId = getDeviceIdFromPreference(context);
		}
		if (deviceId == null || deviceId.equals("")) {
			long randomDeviceid = SystemClock.elapsedRealtime();
			try {
				Random rand = new Random();
				long randomNumber = rand.nextLong();
				randomDeviceid += Math.abs(randomNumber == Long.MIN_VALUE ? 0 : randomNumber);
				deviceId = String.valueOf(randomDeviceid);
				StatisticsDataManager.getInstance().updateStatisticString(context,
						StatisticsKey.DEVICE_ID, deviceId);
			} catch (Exception e) {
				e.printStackTrace();
				deviceId = null;
			}
		}
		return deviceId == null ? DEFAULT_VIRTUAL_DEVICE_ID : deviceId;
	}

	private static String getDeviceIdFromPreference(Context context) {
		if (context == null) {
			return null;
		}
		return StatisticsDataManager.getInstance().getDeviceId(context);
	}

	public static int getProductChannelCode(Context context) {

		if (context == null) {
			return CHANNEL_GOOGLE_MARKET;
		}
		int channel = CHANNEL_GOOGLE_MARKET;
		InputStream is = null;
		try {
			is = context.getResources().openRawResource(R.raw.uid);
		} catch (Resources.NotFoundException e) {
			return channel;
		}
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			try {
				String string = reader.readLine();
				channel = Integer.valueOf(string);
			} finally {
				reader.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return channel;
	}

	private static String getLocaleInfo() {
		Locale locale = Locale.getDefault();
		return String.format("%s-%s", locale.getLanguage(), locale.getCountry());
	}

	private static int getBackupCount(Context context) {
		//		final int count = BackupManager.getInstance().getRecordCount();
		final int count = BackupManager.getInstance().getAllRestoreRecordsCount();
		return count;
	}

	private static int getSmsBackupCount(Context context) {
		if (getBackupCount(context) == 0) {
			return 0;
		}
		int sum = 0;
		List<IRecord> records = BackupManager.getInstance().getAllRestoreRecords();
		if (records == null || records.size() < 1) {
			return 0;
		}
		for (IRecord record : records) {
			if (((RestorableRecord) record).hasSmsEntry()) {
				sum++;
			}
		}
		return sum;
	}

	private static int getContactsBackupCount(Context context) {
		if (getBackupCount(context) == 0) {
			return 0;
		}
		int sum = 0;
		List<IRecord> records = BackupManager.getInstance().getAllRestoreRecords();
		if (records == null || records.size() < 1) {
			return 0;
		}
		for (IRecord record : records) {
			if (((RestorableRecord) record).hasContactsEntry()) {
				sum++;
			}
		}
		return sum;
	}

	private static int getCallLogBackupCount(Context context) {
		if (getBackupCount(context) == 0) {
			return 0;
		}
		int sum = 0;
		List<IRecord> records = BackupManager.getInstance().getAllRestoreRecords();
		if (records == null || records.size() < 1) {
			return 0;
		}
		for (IRecord record : records) {
			if (((RestorableRecord) record).hasCallLogEntry()) {
				sum++;
			}
		}
		return sum;
	}

	private static int getGOLauncherSettingBackupCount(Context context) {
		if (getBackupCount(context) == 0) {
			return 0;
		}
		int sum = 0;
		List<IRecord> records = BackupManager.getInstance().getAllRestoreRecords();
		if (records == null || records.size() < 1) {
			return 0;
		}
		for (IRecord record : records) {
			if (((RestorableRecord) record).hasGoLauncherSettingEntry()) {
				sum++;
			}
		}
		return sum;
	}

	private static boolean checkNetworkState(Context context) {
		if (context == null) {
			return false;
		}
		boolean result = false;
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkInfo networkInfo = cm.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				result = true;
			}
		}
		return result;
	}

	// 用户是否打开定时备份开关
	private static boolean isScheduleBackupEnabled(Context context) {
		boolean ret = false;
		try {
			BackupPlanDBHelper db = new BackupPlanDBHelper(context);
			Cursor cursor = db.getEnabledPlansCursor();
			try {
				return cursor != null && cursor.getCount() > 0;
			} finally {
				if (cursor != null) {
					cursor.close();
				}
				db.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	private static boolean postStatisticsInfo(Context context, URL url, String data) {
		if (url == null || data == null || "".equals(data)) {
			return false;
		}
		final int m3 = 3;
		boolean result = false;
		int retry = m3;
		int count = 0;
		while (count < retry) {
			count++;
			try {
				// 使用HttpURLConnection打开连接
				HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
				// 因为这个是post请求,设立需要设置为true
				urlConn.setDoOutput(true);
				urlConn.setDoInput(true);
				// 设置以POST方式
				urlConn.setRequestMethod("POST");
				// Post 请求不能使用缓存
				urlConn.setUseCaches(false);
				urlConn.setInstanceFollowRedirects(true);
				urlConn.setConnectTimeout(HTTP_REQUEST_TIMEOUT);
				urlConn.setReadTimeout(HTTP_REQUEST_TIMEOUT);

				// 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的
				urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				// 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
				// 要注意的是connection.getOutputStream会隐含的进行connect。
				urlConn.connect();
				// DataOutputStream流
				DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());

				// 将要上传的内容写入流中
				out.writeBytes(data);

				// 刷新、关闭
				out.flush();
				out.close();

				if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
					result = true;
					break;
				}
			} catch (SocketTimeoutException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		// 更新统计上传时间
		if (result) {
			StatisticsDataManager.getInstance().updateUploadTime(context,
					System.currentTimeMillis());
		}
		return result;
	}
}
