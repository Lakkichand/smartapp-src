package com.jiubang.ggheart.data.statistics;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Environment;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.go.util.file.FileUtil;
import com.go.util.window.OrientationControl;
import com.jiubang.ggheart.apps.appfunc.setting.FunAppSetting;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingVisualFontTabView;
import com.jiubang.ggheart.apps.desks.Preferences.view.DeskSettingVisualIconTabView;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.apps.desks.net.CryptTool;
import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.apps.gowidget.GoWidgetFinder;
import com.jiubang.ggheart.apps.gowidget.GoWidgetProviderInfo;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreStatisticsUtil;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.DesktopSettingInfo;
import com.jiubang.ggheart.data.info.EffectSettingInfo;
import com.jiubang.ggheart.data.info.GestureSettingInfo;
import com.jiubang.ggheart.data.info.GravitySettingInfo;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ScreenStyleConfigInfo;
import com.jiubang.ggheart.data.info.ShortCutSettingInfo;
import com.jiubang.ggheart.data.info.ThemeSettingInfo;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.launcher.LauncherEnv;
/**
 * 
 * <br>类描述:统计类。
 * <br>功能详细描述:
 * @date  [2012-9-7]
 */
public class Statistics {

	public static final long STATICTISC_USEDHOURS_FREQUENCY = AlarmManager.INTERVAL_HOUR; // 统计桌面使用时间频率
	// public static final long STATICTISC_USEDHOURS_FREQUENCY = 55*1000; //
	// 统计桌面使用时间频率，测试使用55秒
	
	private static final String RANDOM_DEVICE_ID = "random_device_id"; // IMEI存入sharedPreference中的key
	private static final String DEFAULT_RANDOM_DEVICE_ID = "0000000000000000"; // 默认随机IMEI
	private static final String SHAREDPREFERENCES_RANDOM_DEVICE_ID = "randomdeviceid"; // 保存IMEI的sharedPreference文件名
	private static final String STATISTICS_USER_COMMON = "statisticsusercommon"; // 保存IMEI的sharedPreference文件名
	private static final String USER_IS_COVER = "useriscover"; // 保存覆盖安装的key文件名

	public static final String STATISTICS_DATA_SEPARATE_STRING = "||"; // 统计数据各字段分隔符
	public static final String STATISTICS_DATA_LINEFEED = "\r\n"; // 统计数据各字段分隔符
	public static final String STATISTICS_DATA_ENCRYPT_KEY = "lvsiqiaoil611230"; // 统计数据加密密钥
	public static final String STATISTICS_DATA_CODE = "UTF-8"; // 统计数据使用的编码

//	private static final String STATISTICS_GOLOCKER_URI = "content://com.jiubang.goscreenlock/theme"; // 保存网络请求成功使用时间所使用的KEY
//	private static final String STATISTICS_GOLOCKER_UID = "uid"; // 保存网络请求成功使用时间所使用的KEY
//	private static final String STATISTICS_GOLOCKER_DEFAULT_THEME_PKG_NAME = "defaultThemePackageName"; // 保存网络请求成功使用时间所使用的KEY
//	private static final String STATISTICS_GOLOCKER_USING_THEME_PKG_NAME = "usingThemePackageName"; // 保存网络请求成功使用时间所使用的KEY

	public static final String GOSTORE_WIDGET_PACKAGE_NAME = "com.gau.go.launcherex.gowidget.gostore"; // 内置的GOStore
																										// Widget包名
	//内置的应用游戏中心widget包名
	public static final String APPGAME_WIDGET_PACKAGE_NAME = "com.gau.go.launcherex.gowidget.appgame";

	private static final String GOWIDGET_CHANNEL_NAME = "app_channel"; // GO
																		// Widget渠道号的名字

	private static final int GOLAUNCHER_PID = 1; // 桌面的产品ID

	private static final String CLIENT_MARK_ID = "1";	//客户端标识

	private static String sCountryMark = ""; // 公共信息里 国家的字段

	private Context mContext;

	private static final String THEME_DEFAULT = "com.gau.go.launcherex";
	private static final String THEME_UI30 = "default_theme_package_3";
	private static final String DEVICE_ID_SDPATH = LauncherEnv.Path.SDCARD
			+ LauncherEnv.Path.LAUNCHER_DIR + "/statistics/statistics/deviceId" + ".txt";

	// private StringBuffer testBuffer = null;
	public Statistics(Context context) {
		mContext = context;
	}
	/**
	 * 采用随机数的形式模拟imei号，避免申请权限。
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static String getVirtualIMEI(Context context) {
		String deviceidString = getDeviceIdFromSharedpreference(context);
		// Sharedpreference中没有找到，就从SDcard获取，如果还没有则自动生成一个，并保存下来
		if (deviceidString != null && deviceidString.equals(DEFAULT_RANDOM_DEVICE_ID)) {
			deviceidString = getDeviceIdFromSDcard();
			// 如果SD卡上拿到数据的话就视为旧用户，保存到Sharedpreference,如果拿不到数据就是新用户，随机生成。
			try {
				if (deviceidString == null) {
					long randomDeviceid = SystemClock.elapsedRealtime();
					// 获取随机数，并保存至sharedpreference
					Random rand = new Random();
					long randomLong = rand.nextLong();
					while (randomLong == Long.MIN_VALUE) {
						randomLong = rand.nextLong();
					}
					randomDeviceid += Math.abs(randomLong);
					deviceidString = String.valueOf(randomDeviceid);
					rand = null;
					saveDeviceIdToSDcard(deviceidString);
				} else {
					// 用户之前已经使用过GO桌面的标志。
					StatisticsData.saveIsOldUser(context);
				}
				saveDeviceIdToSharedpreference(context, deviceidString);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// Sharedpreference找到了deviceID但是SDcard上没有，就保存到SD卡上。
		} else if (getDeviceIdFromSDcard() == null) {
			saveDeviceIdToSDcard(deviceidString);
		}
		return deviceidString;
	}
	/**
	 * 获取桌面所有统计数据的方法
	 * @return
	 */
	public synchronized String getStatisticsData() {
		String statisticsDataString = null;
		StringBuffer statisticsDataBuffer = new StringBuffer();
		//		testBuffer = new StringBuffer();
		// 获取统计公共信息
		statisticsDataString = getPublicStatisticsData();

		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
			statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		}
		//		Toast.makeText(mContext, testBuffer, Toast.LENGTH_LONG).show();
		// 获取GO桌面自身应用部分的统计数据
		statisticsDataString = getGoLauncherStatisticsData();
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
			statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		}

		// 获取GO Widget应用部分的统计数据(包括GO精品Widget)
		statisticsDataString = getGoWidgetStatisticsData();
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
			statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		}
		// 获取GO精品应用部分的统计数据(及精品Widget统计)
		//		statisticsDataString = getGoStoreStatisticsData();
		//		if (statisticsDataString != null
		//				&& !"".equals(statisticsDataString.trim())) {
		//			statisticsDataBuffer.append(statisticsDataString);
		//			statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		//		}

		statisticsDataString = GoStoreAppStatistics.getInstance(mContext).queryAllData();
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
			statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		}

		// 获取GO锁屏应用的统计数据
//		statisticsDataString = getGoLockerAllStatisticsData();
//		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
//			statisticsDataBuffer.append(statisticsDataString);
			// statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
//		}

		// 获取GO桌面网络错误的统计数据
		try {
			statisticsDataString = getHttpExceptionStatisticsDate();
			if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
				statisticsDataBuffer.append(statisticsDataString);
				// statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
			}
		} catch (Throwable e) {

		}

		// 获取功能表搜索统计数据
		statisticsDataString = StatisticsAppFuncSearch.getSearchStatisticsData(mContext);
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
			statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		}

		//应用推荐统计数据
		//add by zhaojunjie
		statisticsDataString = getAppRecommendedStatisticsDate();
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
			statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		}

//		//游戏中心统计数据
//		//add by zhouxuewen
//		statisticsDataString = getGameCenterStatisticsDate();
//		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
//			statisticsDataBuffer.append(statisticsDataString);
//			statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
//		}

		//应用管理统计数据
		//add by zhaojunjie
		statisticsDataString = getAppManagementStatisticsDate();
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
			statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		}
		//获取桌面的安装统计
		statisticsDataString = getDeskAppStatisticsData();
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
			//statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		}

		//获取上传失败统计
		statisticsDataString = getNoUploadStatisticsDate();
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
			//statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		}

		//gui收费数据统计
		//add by yangbing
		statisticsDataString = GuiThemeStatistics.getInstance(mContext).queryAllData();
		//System.out.println("gui收费数据统计"+statisticsDataString);
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
			statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		}
		// GUI包名统计 add by yangbing 2012-07-09
		statisticsDataString = GuiThemeStatistics.getInstance(mContext).queryPackageData();
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
			statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		}

		// GUI包名统计 (For zip)
		statisticsDataString = GuiThemeStatistics.getInstance(mContext).queryPackageDataForZip();
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
			statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		}
		
		// GUI进入次数统计
		statisticsDataString = StatisticsData.getGuiEntry(mContext);
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
			statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		}
		// GUI　TAB点击统计
		statisticsDataString = StatisticsData.getGuiTabData(mContext);
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
			statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		}
		// 应用管理Tab统计数据
		// add by zhouxuewen
		statisticsDataString = getAppManagementTabStatisticsDate();
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
		}

//		//游戏中心Tab统计数据
//		//add by zhouxuewen
//		statisticsDataString = getGameCenterTabStatisticsDate();
//		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
//			statisticsDataBuffer.append(statisticsDataString);
//		}

		// 获取进入次数统计
		statisticsDataString = getEntryCountStatData();
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
		}

		// 获取搜索热词统计
		statisticsDataString = getSearchKeywordsStatData();
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
		}

		// 获取用户行为统计之桌面设置统计
		statisticsDataString = getUserDeskSettingStatData();
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
		}

		// 万能统计
		statisticsDataString = StatisticsData.getStatData(mContext);
		if (statisticsDataString != null && !"".equals(statisticsDataString.trim())) {
			statisticsDataBuffer.append(statisticsDataString);
		}
		// add by huyong 2012-05-21 for 增加是否绑定Gmail账号的参数信息
		// 正式上线屏蔽该统计信息
		/*
		 * statisticsDataString = getBindGmailStatisticsData(); if
		 * (statisticsDataString != null &&
		 * !"".equals(statisticsDataString.trim())) {
		 * statisticsDataBuffer.append(statisticsDataString); }
		 */
		// add by huyong 2012-05-21 for 增加是否绑定Gmail账号的参数信息 end

		// end
		// add by huyong 2011-12-05 for all apps data
		// 获取GOLauncher应用的统计数据
		// statisticsDataString = getAllAppsInfo();
		// if(statisticsDataString != null &&
		// !"".equals(statisticsDataString.trim())){
		// statisticsDataBuffer.append(statisticsDataString);
		// // statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
		// }
		// add by huyong 2011-12-05 for all apps data end

		// 对所有的统计数据进行UTF-8编码
		statisticsDataString = statisticsDataBuffer.toString();
		//		Log.i("aoriming","mStatisticsData = " + statisticsDataString);
		//		Log.i("aoriming","2hasExtra = " + hasExtra);
		// 把拿到的统计数据写到SD卡，便于测试
		 writeToSDCard(statisticsDataString, null);
		 System.out.print("! = " + statisticsDataString);
		//		Log.i("getView", "statisticsDataString:" + statisticsDataString);
		try {
			statisticsDataString = URLEncoder.encode(statisticsDataString, STATISTICS_DATA_CODE);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// 对所有的统计数据进行加密
		if (statisticsDataString != null) {
			statisticsDataString = CryptTool.encrypt(statisticsDataString,
					STATISTICS_DATA_ENCRYPT_KEY);
		}
		//		if (sdCardExist) {
		//			String sdDir = Environment.getExternalStorageDirectory().toString();// 获取跟目录
		//			FileUtil.saveByteToSDFile(statisticsDataString.getBytes(), sdDir + "/static2.txt");
		//		}
		//		Toast.makeText(mContext, "上传统计数据", Toast.LENGTH_LONG).show();
		return statisticsDataString;
	}
	
	/**
	 * <br>
	 * 功能简述:从SDcard获取随机生成的IMEI的方法 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	private static String getDeviceIdFromSDcard() {
		return getStringFromSDcard(DEVICE_ID_SDPATH);
	}

	private static String getStringFromSDcard(String filePath) {
		String string = null;
		try {
			boolean sdCardExist = Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
			if (sdCardExist) {
				byte[] bs = FileUtil.getByteFromSDFile(filePath);
				string = new String(bs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return string;
	}

	/**
	 * <br>
	 * 功能简述:保存随机生成的IMEI到SDcard上的方法 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 */
	private static void saveDeviceIdToSDcard(String deviceId) {
		writeToSDCard(deviceId, DEVICE_ID_SDPATH);
	}

	/**
	 * 把字符串写到SD卡的方法
	 * 测试时用的方法
	 * @param data
	 */
	private static void writeToSDCard(String data, String filePath) {
		if (data != null) {
			if (filePath == null) {
				filePath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.LAUNCHER_DIR
						+ "/statistics/androidid" + System.currentTimeMillis() + ".txt";
			}
			try {
				boolean sdCardExist = Environment.getExternalStorageState().equals(
						android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
				if (sdCardExist) {
					FileUtil.saveByteToSDFile(data.getBytes(), filePath);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获取统计数据公共信息的方法
	 * 
	 * @return
	 */
	private String getPublicStatisticsData() {

		String statisticsData = null;
		StringBuffer statisticsDataStringBuffer = new StringBuffer();

		// 统计数据上传时间
		Time time = new Time();
		time.setToNow();
		statisticsDataStringBuffer.append(time.format("%Y-%m-%d %H:%M:%S"));
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// 手机IMEI(修改成申请权限获取真实IMEI码),如果取不到，则使用默认的"0000000000000000"
		String deviceidString = getVirtualIMEI(mContext);

		if (null == deviceidString) {
			deviceidString = DEFAULT_RANDOM_DEVICE_ID;
		}
		// testBuffer.append("imei = " + deviceidString +
		// STATISTICS_DATA_LINEFEED);
		statisticsDataStringBuffer.append(deviceidString);
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// String phonenumString = telephonyManager.getLine1Number();
		// if (null == phonenumString)
		// {
		// phonenumString = "13800138000";
		// }
		// 手机号码，目前写死"88888888888"
		String phonenumString = "88888888888";
		statisticsDataStringBuffer.append(phonenumString);
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// 平台类型与版本
		statisticsDataStringBuffer.append("android-").append(android.os.Build.VERSION.RELEASE);
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// 手机ROM的名字
		statisticsDataStringBuffer.append(android.os.Build.DISPLAY);
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// 手机型号
		statisticsDataStringBuffer.append(android.os.Build.MODEL);
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// 桌面渠道号(UID)
		// testBuffer.append("uid = " + getUid(mContext));
		statisticsDataStringBuffer.append(getUid(mContext));
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// 应用id：1为桌面，2为短信，3.....
		statisticsDataStringBuffer.append(GOLAUNCHER_PID);
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// 桌面版本号
		statisticsDataStringBuffer.append(mContext.getString(R.string.curVersion));

		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING);
		// 使用时长，目前没有统计数据
		statisticsDataStringBuffer.append(getUseTime() * STATICTISC_USEDHOURS_FREQUENCY / 1000);
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING);

		// 语言和国家地区,格式为"zh-CN"
		// 国家语言
		statisticsDataStringBuffer.append(language(mContext));
		// 上一次网络请求成功的使用时间

		long useTime = getNetUseTime();

		if (useTime > 0) {
			statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(useTime);
		} else {
			statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(0);
		}

		// 统计运营商代码
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(getCnUser());

		//客户端标识
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(CLIENT_MARK_ID);

		//国家标识
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(
				getCountryMark(mContext));

		//ROOT统计
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(
				StatisticsData.getRootInfo(mContext));

		// 是否老用户统计
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(
				StatisticsData.getIsOldUser(mContext));

		//机器的等级。
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(
				StatisticsData.getDeviceLevel());

		//GO id(暂用IMEI作为go id)
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(deviceidString);

		//Android id
		statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(
				Machine.getAndroidId());

		statisticsData = statisticsDataStringBuffer.toString();

		return statisticsData;
	}

	/**
	 * 获取用户行为统计数据的方法
	 * 
	 * @author zhouxuewen
	 * @return
	 */
	private String getUserActionStatisticsData() {
		// TODO Auto-generated method stub
		String statisticsData = null;
		if (mContext != null) {
			GoSettingControler goSettingControler = GoSettingControler.getInstance(mContext);
			if (goSettingControler != null) {
				StringBuffer statisticsDataBuffer = new StringBuffer();

				// 一级ID为7
				statisticsDataBuffer.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_USER_ACTION)
						.append(STATISTICS_DATA_SEPARATE_STRING)
						.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_BASE_USER_ACTION)
						.append(STATISTICS_DATA_SEPARATE_STRING)
						.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_BAES_ACTION)
						.append(STATISTICS_DATA_SEPARATE_STRING);

				// 统计1，用户屏幕数
				statisticsDataBuffer.append(StatisticsData.sSCREEN_COUNT).append(
						STATISTICS_DATA_SEPARATE_STRING);

				// 统计3，其中之前没有用户屏幕高宽
				DisplayMetrics dm = new DisplayMetrics();
				dm = mContext.getResources().getDisplayMetrics();
				statisticsDataBuffer.append(dm.heightPixels)
						.append(STATISTICS_DATA_SEPARATE_STRING);
				statisticsDataBuffer.append(dm.widthPixels).append(STATISTICS_DATA_SEPARATE_STRING);

				DesktopSettingInfo desktopSettingInfo = goSettingControler.getDesktopSettingInfo();
				if (desktopSettingInfo != null) {
					//桌面图标行列数
					statisticsDataBuffer.append(desktopSettingInfo.mRow)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(desktopSettingInfo.mColumn)
							.append(STATISTICS_DATA_SEPARATE_STRING);
				} else {
					statisticsDataBuffer.append("").append(STATISTICS_DATA_SEPARATE_STRING)
							.append("").append(STATISTICS_DATA_SEPARATE_STRING);
				}

				ShortCutSettingInfo shortCutSettingInfo = goSettingControler
						.getShortCutSettingInfo();
				if (shortCutSettingInfo != null) {
					// DOCK条行数
					statisticsDataBuffer.append(shortCutSettingInfo.mRows).append(
							STATISTICS_DATA_SEPARATE_STRING);
				} else {
					statisticsDataBuffer.append("").append(STATISTICS_DATA_SEPARATE_STRING);
				}

				// 统计9，备份功能
				statisticsDataBuffer.append(StatisticsData.readStringData(mContext,
						IPreferencesIds.BACKUP, "0"));

				// // 统计4，功能表隐藏功能的活跃用户
				// statisticsDataBuffer.append(
				// StatisticsData.readStringData(mContext,
				// StatisticsData.HIDE_APP, "0")).append(
				// STATISTICS_DATA_SEPARATE_STRING);

				statisticsData = statisticsDataBuffer.toString();
			}
		}
		return statisticsData;
	}

	/**
	 * 获取用户行为统计-桌面设置数据的方法
	 * 
	 * @author zhouxuewen
	 * @return
	 */
	private String getUserDeskSettingStatData() {
		// TODO Auto-generated method stub
		String statisticsData = null;
		if (mContext != null) {
			GoSettingControler goSettingControler = GoSettingControler.getInstance(mContext);
			FunAppSetting funAppSetting = goSettingControler.getFunAppSetting();
			ShortCutSettingInfo shortCutSetting = goSettingControler.getShortCutSettingInfo();
			ScreenSettingInfo screenSetting = goSettingControler.getScreenSettingInfo();
			DesktopSettingInfo desktopSetting = goSettingControler.getDesktopSettingInfo();
			ScreenStyleConfigInfo screenStyleInfo = goSettingControler.getScreenStyleSettingInfo();
			GravitySettingInfo gravitySetting = goSettingControler.getGravitySettingInfo();
			EffectSettingInfo effectSetting = goSettingControler.getEffectSettingInfo();
			ThemeSettingInfo themeSetting = goSettingControler.getThemeSettingInfo();
			String curTheme = ThemeManager.getInstance(mContext).getCurThemePackage();
			if (goSettingControler != null) {
				StringBuffer statisticsDataBuffer = new StringBuffer();

				// 统一头部
				String head = StatisticsFuncId.STATICTISC_LEVEL1_FUNID_USER_ACTION
						+ STATISTICS_DATA_SEPARATE_STRING
						+ StatisticsFuncId.STATICTISC_LEVEL2_FUNID_DESK_SETTING
						+ STATISTICS_DATA_SEPARATE_STRING;

				for (int i = 1; i <= 6; i++) {
					String headLevel1 = head + i + STATISTICS_DATA_SEPARATE_STRING;
					switch (i) {
						case 1 :
							for (int o = 1; o <= 4; o++) {
								String headLevel2 = headLevel1 + o
										+ STATISTICS_DATA_SEPARATE_STRING;
								switch (o) {
									case 1 :
										for (int j = 1; j <= 5; j++) {
											String headLevel3 = headLevel2 + j
													+ STATISTICS_DATA_SEPARATE_STRING;
											String data = "";
											switch (j) {
												case 1 :
													// 背景设置——壁纸裁剪模式
													data = "1";
													if (!screenSetting.mWallpaperScroll) {
														data = "2";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;
												case 2 :
													// 背景设置——快捷条背景
													data = "2";
													if (!shortCutSetting.mBgPicSwitch) {
														data = "1";
													} else if (shortCutSetting.mBgiscustompic) {
														data = "3";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;
												case 3 :
													// 背景设置——功能表背景
													data = "2";
													int bgSetingId = funAppSetting.getBgSetting();
													if (bgSetingId == FunAppSetting.BG_NON) {
														data = "1";
													} else if (bgSetingId == FunAppSetting.BG_CUSTOM) {
														data = "3";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;
												case 4 :
													// 背景设置——模糊壁纸功能
													data = "0";
													if (funAppSetting.getBlurBackground() == 1) {
														data = "1";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;
												case 5 :
													// 背景设置——tab背景
													data = "4";
													String tabHomeBg = funAppSetting
															.getTabHomeBgSetting();
													if (tabHomeBg.equals(curTheme)) {
														data = "1";
													} else if (tabHomeBg.equals(THEME_DEFAULT)
															|| tabHomeBg
																	.equals(ThemeManager.DEFAULT_THEME_PACKAGE_3_NEWER)
															|| tabHomeBg
																	.equals(ThemeManager.DEFAULT_THEME_PACKAGE_3)) {
														data = "2";
													} else if (tabHomeBg.equals(THEME_UI30)) {
														data = "3";
													} else {
														data = "4";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;

												default :
													break;
											}
										}
										break;
									case 2 :
										for (int j = 1; j <= 7; j++) {
											String headLevel3 = headLevel2 + j
													+ STATISTICS_DATA_SEPARATE_STRING;
											String data = "";
											String info = "";
											switch (j) {
												case 1 :
													// 图标设置—图标大小
													data = "2";
													int iconSize = desktopSetting
															.getIconSizeStyle();
													switch (iconSize) {
														case DeskSettingVisualIconTabView.LARGE_ICON_SIZE :
															data = "1";
															break;
														case DeskSettingVisualIconTabView.DEFAULT_ICON_SIZE :
															data = "2";
															break;
														case DeskSettingVisualIconTabView.DIY_ICON_SIZE :
															data = "3";
															break;
														default :
															break;
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;
												case 2 :
													// 图标设置—显示图标底图
													data = "0";
													if (desktopSetting.mShowIconBase) {
														data = "1";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;
												case 3 :
													// 图标设置—图标高亮背景
													data = "2";
													int pressColor = desktopSetting.mPressColor;
													;
													// 11330817 是高亮的默认色值
													if (pressColor == 11330817) {
														data = "1";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;
												case 4 :
													// 图标设置—主题图标
													data = "2";
													info = screenStyleInfo.getIconStyle();
													if (info.equals(curTheme)) {
														data = "1";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;
												case 5 :
													// 图标设置—快捷条图标风格
													data = "2";
													info = shortCutSetting.mStyle;
													if (info.equals(curTheme)) {
														data = "1";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;
												case 6 :
													// 图标设置—文件夹图标风格
													data = "2";
													info = screenStyleInfo.getFolderStyle();
													if (info.equals(curTheme)) {
														data = "1";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;
												case 7 :
													// 图标设置—菜单图标风格
													data = "2";
													info = screenStyleInfo.getGGmenuStyle();
													if (info.equals(THEME_DEFAULT)
															|| info.equals(ThemeManager.DEFAULT_THEME_PACKAGE_3_NEWER)
															|| info.equals(ThemeManager.DEFAULT_THEME_PACKAGE_3)) {
														data = "1";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;
												default :
													break;
											}
										}

										break;

									case 3 :
										for (int j = 1; j <= 3; j++) {
											String headLevel3 = headLevel2 + j
													+ STATISTICS_DATA_SEPARATE_STRING;
											String data = "";
											switch (j) {
												case 1 :
													// 文字显示设置—程度颜色设置
													data = "0";
													if (desktopSetting.mCustomTitleColor) {
														data = "1";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;
												case 2 :
													// 文字显示设置—程度名大小
													data = "2";
													int size = desktopSetting.getFontSizeStyle();
													if (size == DeskSettingVisualFontTabView.DEFAULT_FONT_SIZE) {
														data = "1";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;
												case 3 :
													// 文字显示设置—字体
													data = "2";
													FontBean font = goSettingControler
															.getUsedFontBean();
													if (FontBean.FONTFILETYPE_SYSTEM == font.mFontFileType) {
														data = "1";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;

												default :
													break;
											}
										}
										break;
									case 4 :
										for (int j = 1; j <= 3; j++) {
											String headLevel3 = headLevel2 + j
													+ STATISTICS_DATA_SEPARATE_STRING;
											String data = "";
											String info = "";
											switch (j) {
												case 1 :
													// 指示器设置—指示器风格
													data = "2";
													info = screenStyleInfo.getIndicatorStyle();
													if (info.equals(curTheme)) {
														data = "1";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;
												case 2 :
													// 指示器设置—指示器位置
													data = "2";
													info = screenSetting.mIndicatorPosition;
													if (info.equals(ScreenIndicator.INDICRATOR_ON_TOP)) {
														data = "1";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;
												case 3 :
													// 指示器设置—桌面指示器
													data = "2";
													if (screenSetting.mAutoHideIndicator) {
														data = "3";
													} else if (screenSetting.mEnableIndicator) {
														data = "1";
													}
													statisticsDataBuffer.append(headLevel3)
															.append(data)
															.append(STATISTICS_DATA_LINEFEED);
													break;

												default :
													break;
											}
										}
										break;

									default :
										break;
								}
							}
							break;
						case 2 :
							for (int o = 1; o <= 9; o++) {
								String headLevel2 = headLevel1 + o
										+ STATISTICS_DATA_SEPARATE_STRING;
								String data = "";
								switch (o) {
									case 1 :
										// 屏幕设置—桌面指示器
										data = "1";
										int type = gravitySetting.mOrientationType;
										if (type == OrientationControl.VERTICAL) {
											data = "2";
										} else if (type == OrientationControl.HORIZONTAL) {
											data = "3";
										} else if (type == OrientationControl.IGNOREBORAD) {
											data = "4";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 2 :
										// 屏幕设置—桌面行列数(自动调整图标）
										data = "0";
										if (desktopSetting.mAutofit) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 3 :
										// 屏幕设置—显示状态栏
										data = "0";
										if (desktopSetting.mShowStatusbar) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 4 :
										// 屏幕设置—应用图标标签
										data = "1";
										int titleStyle = desktopSetting.mTitleStyle;
										if (titleStyle == 0) {
											data = "2";
										} else if (titleStyle == 2) {
											data = "3";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 5 :
										// 屏幕设置—循环滚动
										data = "0";
										if (screenSetting.mScreenLooping) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 6 :
										// 屏幕设置—屏幕切换速度
										data = "4";
										int speed = effectSetting.mScrollSpeed;
										if (60 == speed) {
											data = "1";
										} else if (75 == speed) {
											data = "2";
										} else if (45 == speed) {
											data = "3";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 7 :
										// 屏幕设置—循环模式
										data = "0";
										if (shortCutSetting.mAutoRevolve) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 8 :
										// 屏幕设置—显示快捷条
										data = "0";
										if (ShortCutSettingInfo.sEnable) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 9 :
										// 屏幕设置—图标自适应
										data = "0";
										if (shortCutSetting.mAutoFit) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;

									default :
										break;
								}
							}

							break;

						case 3 :
							for (int o = 1; o <= 9; o++) {
								String headLevel2 = headLevel1 + o
										+ STATISTICS_DATA_SEPARATE_STRING;
								String data = "";
								switch (o) {
									case 1 :
										// 功能表设置—功能表滚屏模式
										data = "0";
										int direction = funAppSetting.getTurnScreenDirection();
										int loop = funAppSetting.getScrollLoop();
										data = "1";
										if (FunAppSetting.SCREEN_SCROLL_HORIZONTAL == direction
												&& 1 == loop) {
											data = "4";
										} else if (FunAppSetting.SCREEN_SCROLL_HORIZONTAL == direction) {
											data = "3";
										} else if (FunAppSetting.SCREEN_SCROLL_VERTICAL == direction
												&& 1 == loop) {
											data = "2";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 2 :
										// 功能表设置—功能表行列数
										data = "5";
										int standard = funAppSetting.getLineColumnNum();
										if (standard == FunAppSetting.LINECOLUMNNUMXY_SPARSE) {
											data = "1";
										} else if (standard == FunAppSetting.LINECOLUMNNUMXY_MIDDLE) {
											data = "2";
										} else if (standard == FunAppSetting.LINECOLUMNNUMXY_THICK) {
											data = "4";
										} else if (standard == FunAppSetting.LINECOLUMNNUMXY_MIDDLE_2) {
											data = "3";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 3 :
										// 功能表设置—显示功能表选项卡
										data = "0";
										if (funAppSetting.getShowTabRow() == 1) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 4 :
										// 功能表设置—显示功能表程序名
										data = "0";
										if (funAppSetting.getAppNameVisiable() == FunAppSetting.APPNAMEVISIABLEYES) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 5 :
										// 功能表设置—应用更新提示
										data = "0";
										if (funAppSetting.getAppUpdate() == FunAppSetting.ON) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 6 :
										// 功能表设置—显示底部操作栏
										data = "0";
										if (funAppSetting.getShowActionBar() == FunAppSetting.ON) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 7 :
										// 功能表设置—只显示主页按钮
										data = "0";
										if (funAppSetting.getShowHomeKeyOnly() == FunAppSetting.ON) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 8 :
										// 功能表设置—锁定列表
										data = "0";
										if (StatisticsData.getUseRecordPreferences(mContext,
												StatisticsData.APPFUNC_APPLIST_ITEM)) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 9 :
										// 功能表设置—显示锁定程序
										data = "0";
										if (funAppSetting.getShowNeglectApp() == FunAppSetting.SHOWAPPS) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;

									default :
										break;
								}
							}
							break;
						case 4 :
							for (int o = 1; o <= 2; o++) {
								String headLevel2 = headLevel1 + o
										+ STATISTICS_DATA_SEPARATE_STRING;
								String data = "";
								switch (o) {
									case 1 :
										// 手势设置—启用功能表上滑手势
										data = "0";
										if (funAppSetting.getGlideUpAction() == FunAppSetting.ON) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 2 :
										// 手势设置—启用功能表下滑手势
										data = "0";
										if (funAppSetting.getGlideDownAction() == FunAppSetting.ON) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									default :
										break;
								}
							}
							break;
						case 5 :
							for (int o = 1; o <= 7; o++) {
								String headLevel2 = headLevel1 + o
										+ STATISTICS_DATA_SEPARATE_STRING;
								String data = "";
								String info = "";
								switch (o) {
									case 1 :
										// 高级设置—高质量绘图
										data = "0";
										if (themeSetting.mHighQualityDrawing) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 2 :
										// 高级设置—支持透明通知栏
										data = "0";
										if (themeSetting.mTransparentStatusbar) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 3 :
										// 高级设置—常驻内存
										data = "0";
										if (themeSetting.mIsPemanentMemory) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 4 :
										// 高级设置—阻止强制关闭
										data = "0";
										if (themeSetting.mPreventForceClose) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 5 :
										// 高级设置—检查垃圾数据
										data = "0";
										if (StatisticsData.getUseRecordPreferences(mContext,
												StatisticsData.CLEANDIRTYDATA_ITEM)) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 6 :
										// 高级设置—桌面搬家
										data = "0";
										if (StatisticsData.getUseRecordPreferences(mContext,
												StatisticsData.DESKMIGRATE_ITEM)) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 7 :
										// 高级设置—自动云查杀
										data = "0";
//										if (themeSetting.mCloudSecurity) {
//											data = "1";
//										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									default :
										break;
								}
							}
							break;
						case 6 :
							for (int o = 1; o <= 4; o++) {
								String headLevel2 = headLevel1 + o
										+ STATISTICS_DATA_SEPARATE_STRING;
								String data = "";
								String info = "";
								switch (o) {
									case 1 :
										// 备份&恢复—备份GO桌面
										data = "0";
										if (StatisticsData.getUseRecordPreferences(mContext,
												StatisticsData.BACKUP_ITEM)) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 2 :
										// 备份&恢复—恢复备份
										data = "0";
										if (StatisticsData.getUseRecordPreferences(mContext,
												StatisticsData.RESETBACKUP_ITEM)) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 3 :
										// 备份&恢复—恢复默认
										data = "0";
										if (StatisticsData.getUseRecordPreferences(mContext,
												StatisticsData.RESETDEFAULT_ITEM)) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									case 4 :
										// 备份&恢复—GO备份
										data = "0";
										if (StatisticsData.getUseRecordPreferences(mContext,
												StatisticsData.GOBACKUP_ITEM)) {
											data = "1";
										}
										statisticsDataBuffer.append(headLevel2).append(data)
												.append(STATISTICS_DATA_LINEFEED);
										break;
									default :
										break;
								}
							}
							break;
						default :
							break;
					}
				}
				statisticsData = statisticsDataBuffer.toString();
			}
		}
		return statisticsData;
	}

	/**
	 * 获取屏幕切换特效统计数据的方法
	 * 
	 * @author zhouxuewen
	 * @return
	 */
	private String getDeskEffectStatisticsData() {
		// TODO Auto-generated method stub
		String statisticsData = null;
		if (mContext != null) {
			GoSettingControler goSettingControler = GoSettingControler.getInstance(mContext);
			if (goSettingControler != null) {
				StringBuffer statisticsDataBuffer = new StringBuffer();

				// 一级ID为7
				statisticsDataBuffer.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_USER_ACTION)
						.append(STATISTICS_DATA_SEPARATE_STRING)
						.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_BASE_USER_ACTION)
						.append(STATISTICS_DATA_SEPARATE_STRING)
						.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_DESK_EFFECT)
						.append(STATISTICS_DATA_SEPARATE_STRING);

				// 统计6，屏幕切换类型
				EffectSettingInfo effectInfo = goSettingControler.getEffectSettingInfo();
				statisticsDataBuffer.append(effectInfo.mEffectorType);

				statisticsData = statisticsDataBuffer.toString();
			}
		}
		return statisticsData;
	}

	/**
	 * 获取功能进特效统计数据的方法
	 * 
	 * @author zhouxuewen
	 * @return
	 */
//	private String getAppFuncEffectStatisticsData() {
//		// TODO Auto-generated method stub
//		String statisticsData = null;
//		if (mContext != null) {
//			GoSettingControler goSettingControler = GoSettingControler.getInstance(mContext);
//			if (goSettingControler != null) {
//				StringBuffer statisticsDataBuffer = new StringBuffer();
//
//				// 一级ID为7
//				statisticsDataBuffer.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_USER_ACTION)
//						.append(STATISTICS_DATA_SEPARATE_STRING)
//						.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_BASE_USER_ACTION)
//						.append(STATISTICS_DATA_SEPARATE_STRING)
//						.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_APPFUNC_EFFECT)
//						.append(STATISTICS_DATA_SEPARATE_STRING);
//
//				// 统计8，功能表特效
//				statisticsDataBuffer.append(StatisticsData.getAppInOutlEffectId(mContext)).append(
//						STATISTICS_DATA_SEPARATE_STRING);
//				statisticsDataBuffer.append(StatisticsData.getAppfuncHorizontalEffectId(mContext))
//						.append(STATISTICS_DATA_SEPARATE_STRING);
//				statisticsDataBuffer.append(StatisticsData.getAppfuncVerticalEffectId(mContext));
//
//				statisticsData = statisticsDataBuffer.toString();
//			}
//		}
//		return statisticsData;
//	}

	/**
	 * 获取手势设定统计数据的方法
	 * 
	 * @author zhouxuewen
	 * @return
	 */
	private String getGestureActionStatisticsData() {
		// TODO Auto-generated method stub
		String statisticsData = null;
		if (mContext != null) {
			GoSettingControler goSettingControler = GoSettingControler.getInstance(mContext);
			if (goSettingControler != null) {
				StringBuffer statisticsDataBuffer = new StringBuffer();

				// 一级ID为7
				statisticsDataBuffer.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_USER_ACTION)
						.append(STATISTICS_DATA_SEPARATE_STRING)
						.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_BASE_USER_ACTION)
						.append(STATISTICS_DATA_SEPARATE_STRING)
						.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_GESTURE_ACTION)
						.append(STATISTICS_DATA_SEPARATE_STRING);

				// 统计10，手势统计
				// HOME键
				int gestureId = 0;
				GestureSettingInfo gHomeInfo = goSettingControler
						.getGestureSettingInfo(GestureSettingInfo.GESTURE_HOME_ID);
				gestureId = gHomeInfo.mGestureAction;
				if (gestureId == -1) {
					gestureId = gHomeInfo.mGoShortCut;
				}
				statisticsDataBuffer.append(gestureId).append(STATISTICS_DATA_SEPARATE_STRING);
				// 上滑
				GestureSettingInfo gUpInfo = goSettingControler
						.getGestureSettingInfo(GestureSettingInfo.GESTURE_UP_ID);
				gestureId = gUpInfo.mGestureAction;
				if (gestureId == -1) {
					gestureId = gUpInfo.mGoShortCut;
				}
				statisticsDataBuffer.append(gestureId).append(STATISTICS_DATA_SEPARATE_STRING);
				// 下滑
				GestureSettingInfo gDownInfo = goSettingControler
						.getGestureSettingInfo(GestureSettingInfo.GESTURE_DOWN_ID);
				gestureId = gDownInfo.mGestureAction;
				if (gestureId == -1) {
					gestureId = gDownInfo.mGoShortCut;
				}
				statisticsDataBuffer.append(gestureId);

				statisticsData = statisticsDataBuffer.toString();
			}
		}
		return statisticsData;
	}

	/**
	 * 获取快捷条的统计数据的方法
	 * 
	 * @author zhouxuewen
	 * @return
	 */
	private String getShortStatisticsData(GoSettingControler goSettingControler) {
		// TODO Auto-generated method stub
		ShortCutSettingInfo shortCutSettingInfo = goSettingControler.getShortCutSettingInfo();
		String statisticsData = null;
		if (shortCutSettingInfo != null) {
			StringBuffer statisticsDataBuffer = new StringBuffer();
			statisticsDataBuffer.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_USER_ACTION)
					.append(STATISTICS_DATA_SEPARATE_STRING)
					.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_BASE_USER_ACTION)
					.append(STATISTICS_DATA_SEPARATE_STRING)
					.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_AUTO_FIT_ICON)
					.append(STATISTICS_DATA_SEPARATE_STRING)
					.append((shortCutSettingInfo.mAutoFit) ? "1" : "0")
					.append(STATISTICS_DATA_LINEFEED);
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}
	/**
	 * <br>功能简述:获取用户桌面行为统计数据的方法
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private String getUserBehaviorStatisticsData(String type) {
		String statisticsData = null;
		if (mContext != null) {
			StringBuffer statisticsDataBuffer = new StringBuffer();
			Map<String, ?> conutMap = StatisticsData.getUserActionData(type);
			int statictiscId;
			if (type.equals(IPreferencesIds.DESK_ACTION_DATA)) {
				statictiscId = StatisticsFuncId.STATICTISC_LEVEL3_FUNID_DESK_ACTION;
			} else {
				statictiscId = StatisticsFuncId.STATICTISC_LEVEL3_FUNID_FUNC_ACTION;
			}
			Set<String> keySet = conutMap.keySet();
			for (String key : keySet) {
				Object obj = conutMap.get(key);
				int record = 0;
				if (obj instanceof Integer) {
					record = (Integer) obj;
				}
				if (record != 0) {
					statisticsDataBuffer
							.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_USER_ACTION)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_DESK_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING).append(statictiscId)
							.append(STATISTICS_DATA_SEPARATE_STRING).append(key)
							.append(STATISTICS_DATA_SEPARATE_STRING).append(record)
							.append(STATISTICS_DATA_LINEFEED);
				}
			}
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}
	/**
	 * <br>功能简述:桌面tab
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private String getThemeTabStatisticsData() {
		String statisticsData = null;
		if (mContext != null) {
			StringBuffer statisticsDataBuffer = new StringBuffer();
			Map<String, ?> themeConutMap = StatisticsData.getThemeTabData();
			Set<String> keySet = themeConutMap.keySet();
			for (String key : keySet) {
				int record = (Integer) themeConutMap.get(key);
				if (record != 0) {
					statisticsDataBuffer
							.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_USER_ACTION)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_DESK_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_THEME_TAB)
							.append(STATISTICS_DATA_SEPARATE_STRING).append(key)
							.append(STATISTICS_DATA_SEPARATE_STRING).append(record)
							.append(STATISTICS_DATA_LINEFEED);
				}
			}
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}
	/**
	 * 获取菜单的统计数据的方法
	 * 
	 * @author zhouxuewen
	 * @return
	 */
	private String getMenuStatisticsData() {
		String statisticsData = null;
		if (mContext != null) {
			StringBuffer statisticsDataBuffer = new StringBuffer();
			Map<String, ?> menuConutMap = StatisticsData.getMenuCountData(mContext);
			Set<String> keySet = menuConutMap.keySet();
			for (String key : keySet) {
				Object obj = menuConutMap.get(key);
				int record = 0;
				if (obj != null && obj instanceof Integer) {
					record = (Integer) obj;
				}
				if (record != 0) {
					statisticsDataBuffer
							.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_USER_ACTION)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_MEUN_COUNT)
							.append(STATISTICS_DATA_SEPARATE_STRING).append(key)
							.append(STATISTICS_DATA_SEPARATE_STRING).append(record)
							.append(STATISTICS_DATA_LINEFEED);
				}
			}
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}

	/**
	 * 获取GO桌面桌面安装统计
	 * 
	 * @return
	 */
	private String getDeskAppStatisticsData() {
		String statisticsData = null;
		if (mContext != null) {
			StringBuffer statisticsDataBuffer = new StringBuffer();

			Map<String, ?> data = StatisticsData.getAllAppData(mContext);
			if (data != null) {
				Set<String> keys = data.keySet();
				for (String key : keys) {
					statisticsDataBuffer.append(
							StatisticsFuncId.STATICTISC_LEVEL1_FUNID_FUNTAB_APP_DATA).append(
							STATISTICS_DATA_SEPARATE_STRING);
					Object obj = data.get(key);
					String reason = null;
					if (obj != null && obj instanceof String) {
						reason = (String) obj;
					}
					String[] item = null;
					if (reason != null && !reason.equals("")) {
						item = reason.split(";");
					}

					if (item != null && item.length > 1) {
						int clickCoutn = Integer.valueOf(item[0]);
						int installCoutn = Integer.valueOf(item[1]);
						String mapId = "0";
						String id = "0";
						
						if (item.length > 5) {
							mapId = item[4];
							id = item[5];
						}
						
						if (clickCoutn > 0 && installCoutn == 0) {
							if (AppUtils.isAppExist(mContext, key)) {
								installCoutn++;
							}
						}
						
						
						
						statisticsDataBuffer.append(key)
								.append(STATISTICS_DATA_SEPARATE_STRING)
								.append(String.valueOf(clickCoutn))
								.append(STATISTICS_DATA_SEPARATE_STRING)
								.append(String.valueOf(installCoutn))
								.append(STATISTICS_DATA_SEPARATE_STRING)
								.append(String.valueOf(mapId))
								.append(STATISTICS_DATA_SEPARATE_STRING)
								.append(String.valueOf(id))
								.append(STATISTICS_DATA_LINEFEED);
					}
				}
			}
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}

	/**
	 * 获取桌面自身应用部分的统计数据的方法
	 * 
	 * @return
	 */
	private String getGoLauncherStatisticsData() {
		String statisticsData = null;
		StringBuffer statisticsDataStringBuffer = new StringBuffer();
		GoSettingControler goSettingControler = GoSettingControler.getInstance(mContext);
		// 获取桌面基本信息统计
		String deskBaseStatisticsData = getUserActionStatisticsData();
		if (deskBaseStatisticsData != null && !"".equals(deskBaseStatisticsData.trim())) {
			statisticsDataStringBuffer.append(deskBaseStatisticsData);
			statisticsDataStringBuffer.append(STATISTICS_DATA_LINEFEED);
		}

		// 获取屏幕特效
		String deskEffectStatisticsData = getDeskEffectStatisticsData();
		if (deskEffectStatisticsData != null && !"".equals(deskEffectStatisticsData.trim())) {
			statisticsDataStringBuffer.append(deskEffectStatisticsData);
			statisticsDataStringBuffer.append(STATISTICS_DATA_LINEFEED);
		}

		// 获取功能进特效
//		String appFuncEffectStatisticsData = getAppFuncEffectStatisticsData();
//		if (appFuncEffectStatisticsData != null && !"".equals(appFuncEffectStatisticsData.trim())) {
//			statisticsDataStringBuffer.append(appFuncEffectStatisticsData);
//			statisticsDataStringBuffer.append(STATISTICS_DATA_LINEFEED);
//		}

		// 获取手势操作
		String gestureActionStatisticsData = getGestureActionStatisticsData();
		if (gestureActionStatisticsData != null && !"".equals(gestureActionStatisticsData.trim())) {
			statisticsDataStringBuffer.append(gestureActionStatisticsData);
			statisticsDataStringBuffer.append(STATISTICS_DATA_LINEFEED);
		}

		// 统计7，获取桌面菜单信息统计
		String menuStatisticsData = getMenuStatisticsData();
		if (menuStatisticsData != null && !"".equals(menuStatisticsData.trim())) {
			statisticsDataStringBuffer.append(menuStatisticsData);
		}

		// 获取快捷条统计
		String shortStatisticsData = getShortStatisticsData(goSettingControler);
		if (shortStatisticsData != null && !"".equals(shortStatisticsData.trim())) {
			statisticsDataStringBuffer.append(shortStatisticsData);
		}
		// 获取桌面行为统计
		String deskActionStatisticsData = getUserBehaviorStatisticsData(IPreferencesIds.DESK_ACTION_DATA);
		if (deskActionStatisticsData != null && !"".equals(deskActionStatisticsData.trim())) {
			statisticsDataStringBuffer.append(deskActionStatisticsData);
		}
		// 获取功能表行为统计
		String funcActionStatisticsData = getUserBehaviorStatisticsData(IPreferencesIds.APP_FUNC_ACTION_DATA);
		if (funcActionStatisticsData != null && !"".equals(funcActionStatisticsData.trim())) {
			statisticsDataStringBuffer.append(funcActionStatisticsData);
		}
		// 获取桌面主题tab行为统计
		String themeTabStatisticsData = getThemeTabStatisticsData();
		if (themeTabStatisticsData != null && !"".equals(themeTabStatisticsData.trim())) {
			statisticsDataStringBuffer.append(themeTabStatisticsData);
		}
		// // 获取桌面设置 路径： 菜单桌面设置->屏幕设置->桌面设置分类
		// String deskSettingStatisticsData = getDeskSettingStatisticsData();
		// if (deskSettingStatisticsData != null
		// && !"".equals(deskSettingStatisticsData.trim())) {
		// statisticsDataStringBuffer.append(deskSettingStatisticsData);
		// statisticsDataStringBuffer.append(STATISTICS_DATA_LINEFEED);
		// }
		// // 获取屏幕切换设置 路径： 菜单桌面设置->屏幕设置->屏幕切换分类
		// String displaySettingStatisticsData =
		// getDisplaySettingStatisticsData();
		// if (displaySettingStatisticsData != null
		// && !"".equals(displaySettingStatisticsData.trim())) {
		// statisticsDataStringBuffer.append(displaySettingStatisticsData);
		// statisticsDataStringBuffer.append(STATISTICS_DATA_LINEFEED);
		// }
		// // 获取DOCK条设置 路径： 菜单桌面设置->屏幕设置->快捷条设置分类
		// String dockSettingStatisticsData = getDockSettingStatisticsData();
		// if (dockSettingStatisticsData != null
		// && !"".equals(dockSettingStatisticsData.trim())) {
		// statisticsDataStringBuffer.append(dockSettingStatisticsData);
		// }
		statisticsData = statisticsDataStringBuffer.toString();
		return statisticsData;
	}

	/**
	 * 获取进入次数统计内容
	 * @author zhouxuewen
	 * @return
	 */
	private String getEntryCountStatData() {
		String statisticsData = null;
		if (mContext != null) {
			StringBuffer statisticsDataBuffer = new StringBuffer();
			Map<String, ?> map = StatisticsData.getEntryCount(mContext);
			Set<String> keySet = map.keySet();
			for (String key : keySet) {
				Object obj = map.get(key);
				int record = 0;
				if (obj != null && obj instanceof Integer) {
					record = (Integer) map.get(key);
				}
				if (record != 0) {
					statisticsDataBuffer.append(key).append(record)
							.append(STATISTICS_DATA_LINEFEED);
				}
			}
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}

	private String getSearchKeywordsStatData() {
		String statisticsData = null;
		if (mContext != null) {
			StringBuffer statisticsDataBuffer = new StringBuffer();
			Map<String, ?> map = StatisticsData.getSearchKeywordStat(mContext);
			Set<String> keySet = map.keySet();
			for (String key : keySet) {
				Object obj = map.get(key);
				String record = null;
				if (obj != null && obj instanceof String) {
					record = (String) obj;
				}
				if (record != null) {
					statisticsDataBuffer.append(key).append(record)
							.append(STATISTICS_DATA_LINEFEED);
				}
			}
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}

	/**
	 * 获取桌面设置的统计数据的方法 路径： 菜单桌面设置->屏幕设置->桌面设置分类
	 * 
	 * @return
	 */
	private String getDeskSettingStatisticsData() {
		String statisticsData = null;
		if (mContext != null) {
			GoSettingControler goSettingControler = GoSettingControler.getInstance(mContext);
			if (goSettingControler != null) {
				DesktopSettingInfo desktopSettingInfo = goSettingControler.getDesktopSettingInfo();
				if (desktopSettingInfo != null) {
					StringBuffer statisticsDataBuffer = new StringBuffer();
					// 桌面程序名
					statisticsDataBuffer
							.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOLAUNCHER)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_DESK_SCREEN_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_DESK_SHOWPATTEM_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(mContext.getString(R.string.show_app_name))
							.append(STATISTICS_DATA_SEPARATE_STRING);
					int showPattem = 0;
					if (desktopSettingInfo.mShowTitle) {
						showPattem = 1;
					}
					statisticsDataBuffer.append(showPattem).append(STATISTICS_DATA_LINEFEED);
					// 桌面行列数
					statisticsDataBuffer
							.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOLAUNCHER)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_DESK_SCREEN_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_DESK_ROWCOLUMN_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(mContext.getString(R.string.screen_rows_cols))
							.append(STATISTICS_DATA_SEPARATE_STRING);
					statisticsDataBuffer.append(desktopSettingInfo.mRow).append("-")
							.append(desktopSettingInfo.mColumn);
					statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
					// 桌面状态栏
					statisticsDataBuffer
							.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOLAUNCHER)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_DESK_SCREEN_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_DESK_SHOWSTATUSBAR_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(mContext.getString(R.string.statusbar))
							.append(STATISTICS_DATA_SEPARATE_STRING);
					int showStatus = 0;
					if (desktopSettingInfo.mShowStatusbar) {
						showStatus = 1;
					}
					statisticsDataBuffer.append(showStatus);
					statisticsData = statisticsDataBuffer.toString();
				}
			}
		}
		return statisticsData;
	}

	/**
	 * 获取屏幕切换设置的统计数据的方法 路径： 菜单桌面设置->屏幕设置->屏幕切换分类
	 * 
	 * @return
	 */
	private String getDisplaySettingStatisticsData() {
		String statisticsData = null;
		if (mContext != null) {
			GoSettingControler goSettingControler = GoSettingControler.getInstance(mContext);
			if (goSettingControler != null) {
				ScreenSettingInfo screenSettingInfo = goSettingControler.getScreenSettingInfo();
				ScreenStyleConfigInfo screenStyleConfigInfo = goSettingControler
						.getScreenStyleSettingInfo();
				if (screenSettingInfo != null) {
					StringBuffer statisticsDataBuffer = new StringBuffer();
					// 壁纸滚动
					statisticsDataBuffer
							.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOLAUNCHER)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_DESK_SCREEN_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_DISPLAY_WALLPAPERSCROLL_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(mContext.getString(R.string.wallpaper_scrolling))
							.append(STATISTICS_DATA_SEPARATE_STRING);
					int wallpaperScrolling = 0;
					if (screenSettingInfo.mWallpaperScroll) {
						wallpaperScrolling = 1;
					}
					statisticsDataBuffer.append(wallpaperScrolling)
							.append(STATISTICS_DATA_LINEFEED);
					// 屏幕循环切换
					statisticsDataBuffer
							.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOLAUNCHER)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_DESK_SCREEN_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_DISPLAY_SCREENLOOPING_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(mContext.getString(R.string.screen_looping))
							.append(STATISTICS_DATA_SEPARATE_STRING);
					int screenLooping = 0;
					if (screenSettingInfo.mScreenLooping) {
						screenLooping = 1;
					}
					statisticsDataBuffer.append(screenLooping).append(STATISTICS_DATA_LINEFEED);
					// 屏幕切换效果
					EffectSettingInfo effectSettingInfo = goSettingControler.getEffectSettingInfo();
					if (effectSettingInfo != null) {
						statisticsDataBuffer
								.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOLAUNCHER)
								.append(STATISTICS_DATA_SEPARATE_STRING)
								.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_DESK_SCREEN_SETTING)
								.append(STATISTICS_DATA_SEPARATE_STRING)
								.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_DISPLAY_TRANSITION_SETTING)
								.append(STATISTICS_DATA_SEPARATE_STRING)
								.append(mContext.getString(R.string.screen_transition_effect))
								.append(STATISTICS_DATA_SEPARATE_STRING)
								.append(effectSettingInfo.mScrollSpeed).append("-")
								.append(effectSettingInfo.mBackSpeed)
								.append(STATISTICS_DATA_LINEFEED);
					}
					// 页面指示器
					statisticsDataBuffer
							.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOLAUNCHER)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_DESK_SCREEN_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_DISPLAY_ENABLEINDICATOR_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(mContext.getString(R.string.screen_indicator))
							.append(STATISTICS_DATA_SEPARATE_STRING);
					int screenIndicator = 0;
					if (screenSettingInfo.mEnableIndicator) {
						screenIndicator = 1;
					}
					statisticsDataBuffer.append(screenIndicator).append(STATISTICS_DATA_LINEFEED);
					// 指示器自动隐藏
					statisticsDataBuffer
							.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOLAUNCHER)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_DESK_SCREEN_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_DISPLAY_AUTOHIDEINDICATOR_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(mContext.getString(R.string.screen_indicator_autohide))
							.append(STATISTICS_DATA_SEPARATE_STRING);
					int screenIndicatorAutohide = 0;
					if (screenSettingInfo.mAutoHideIndicator) {
						screenIndicatorAutohide = 1;
					}
					statisticsDataBuffer.append(screenIndicatorAutohide).append(
							STATISTICS_DATA_LINEFEED);
					// 指示器模式
					statisticsDataBuffer
							.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOLAUNCHER)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_DESK_SCREEN_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_DISPLAY_INDICATORSHOWMODE_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(mContext.getString(R.string.screen_indicator_showmode))
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(screenStyleConfigInfo.getIndicatorStyle());

					statisticsData = statisticsDataBuffer.toString();
				}
			}
		}
		return statisticsData;
	}

	/**
	 * 获取DOCK条设置的统计数据的方法 路径： 菜单桌面设置->屏幕设置->快捷条设置分类
	 * 
	 * @return
	 */
	private String getDockSettingStatisticsData() {
		// TODO Auto-generated method stub
		String statisticsData = null;
		if (mContext != null) {
			GoSettingControler goSettingControler = GoSettingControler.getInstance(mContext);
			if (goSettingControler != null) {
				ShortCutSettingInfo shortCutSettingInfo = goSettingControler
						.getShortCutSettingInfo();
				if (shortCutSettingInfo != null) {
					StringBuffer statisticsDataBuffer = new StringBuffer();
					// DOCK条行数
					statisticsDataBuffer
							.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOLAUNCHER)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_DESK_SCREEN_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_DOCK_ROW_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(mContext.getString(R.string.Dockrow))
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(shortCutSettingInfo.mRows).append(STATISTICS_DATA_LINEFEED);
					// DOCK条循环
					statisticsDataBuffer
							.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOLAUNCHER)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_DESK_SCREEN_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_DOCK_REVOLVE_SETTING)
							.append(STATISTICS_DATA_SEPARATE_STRING)
							.append(mContext.getString(R.string.autocycle))
							.append(STATISTICS_DATA_SEPARATE_STRING);
					int revolve = 0;
					if (shortCutSettingInfo.mAutoRevolve) {
						revolve = 1;
					}
					statisticsDataBuffer.append(revolve);
					statisticsData = statisticsDataBuffer.toString();
				}
			}
		}
		return statisticsData;
	}

	/**
	 * 获取GO Widget部分的统计数据的方法(包括GO精品Widget)
	 * 
	 * @return
	 */
	private String getGoWidgetStatisticsData() {
		String statisticsData = null;
		if (mContext != null) {
			StringBuffer statisticsDataStringBuffer = new StringBuffer();
			// 如果有安装Widget
			// 获取所有添加到桌面的Widget的统计数据
			Map<String, String> activeGoWidgetStatisticData = AppCore.getInstance()
					.getGoWidgetManager().getStatisticData();
			// 扫描所有已经安装的Widget,获取信息(GO精品Widget除外)
			GoWidgetFinder goWidgetFinder = new GoWidgetFinder(mContext);
			goWidgetFinder.scanAllInstalledGoWidget();
			HashMap<String, GoWidgetProviderInfo> allGoWidgetInfosMap = goWidgetFinder
					.getGoWidgetInfosMap();
			if (allGoWidgetInfosMap != null && allGoWidgetInfosMap.size() > 0) {
				PackageManager packageManager = mContext.getPackageManager();
				GoWidgetProviderInfo goWidgetProviderInfo = null;
				PackageInfo packageInfo = null;
				String packageName = null;
				String versionName = null;
				Resources resources = null;
				int resId = -1;
				String uID = null;
				String activeData = null;
				Set<String> keySet = allGoWidgetInfosMap.keySet();
				for (String key : keySet) {
					goWidgetProviderInfo = allGoWidgetInfosMap.get(key);
					if (goWidgetProviderInfo != null) {
						// 获取包名
						packageName = goWidgetProviderInfo.mProvider.provider.getPackageName();
						if (packageName != null && !"".equals(packageName.trim())) {
							// 一级功能ID
							statisticsDataStringBuffer
									.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOWIDGET);
							// 包名
							statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
									.append(packageName);
							// 获取版本号和渠道号
							try {
								packageInfo = packageManager.getPackageInfo(packageName, 0);
								resources = packageManager.getResourcesForApplication(packageName);
							} catch (NameNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (Exception e) {
								// TODO: handle exception for android.os.TransactionTooLargeException
								e.printStackTrace();
							}
							// 版本号
							versionName = "";
							if (packageInfo != null) {
								versionName = packageInfo.versionName;
							}
							statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
									.append(versionName);
							// 渠道号
							uID = "";
							if (resources != null) {
								resId = resources.getIdentifier(GOWIDGET_CHANNEL_NAME, "string",
										packageName);
								if (resId != 0) {
									uID = resources.getString(resId);
								}
							}
							statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
									.append(uID);
							// 是否添加到桌面和统计数据
							if (activeGoWidgetStatisticData.containsKey(packageName)) {
								// 有添加到桌面
								statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
										.append(1);

								activeData = activeGoWidgetStatisticData.get(packageName);
								if (activeData != null && !"".equals(activeData.trim())) {
									// 如果获取到数据
									statisticsDataStringBuffer
											.append(STATISTICS_DATA_SEPARATE_STRING).append(1)
											.append(STATISTICS_DATA_SEPARATE_STRING)
											.append(activeData);
								} else {
									// 如果没有获取到数据
									statisticsDataStringBuffer.append(
											STATISTICS_DATA_SEPARATE_STRING).append(0);
								}
							} else {
								// 没有添加到桌面
								statisticsDataStringBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
										.append(0).append(STATISTICS_DATA_SEPARATE_STRING)
										.append(0);
							}
						}
					}
					statisticsDataStringBuffer.append(STATISTICS_DATA_LINEFEED);
				}
			}
			
			// 应用中心或GO精品WIDGET放置统计
			
			// 应用中心WIDGET是否放置桌面
			String isAppWidgetActive = activeGoWidgetStatisticData.containsKey(APPGAME_WIDGET_PACKAGE_NAME) ? "1" : "0";
			// GO精品WIDGET是否放置桌面
			String isGoStorteWidgetActive = activeGoWidgetStatisticData.containsKey(GOSTORE_WIDGET_PACKAGE_NAME) ? "1" : "0";
			
			// 一级功能ID
			statisticsDataStringBuffer.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_NEW_APP_STAT);
			// 二级或三级功能ID
			statisticsDataStringBuffer.append("||1||1||");
			statisticsDataStringBuffer.append(isAppWidgetActive).append(STATISTICS_DATA_LINEFEED);
			
			// 一级功能ID
			statisticsDataStringBuffer.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_NEW_APP_STAT);
			// 二级或三级功能ID
			statisticsDataStringBuffer.append("||1||2||");
			statisticsDataStringBuffer.append(isGoStorteWidgetActive).append(STATISTICS_DATA_LINEFEED);
			
			statisticsData = statisticsDataStringBuffer.toString();
		}
		return statisticsData;
	}

	/**
	 * 获取GO精品部分的统计数据的方法
	 * 
	 * @return
	 */
	private String getGoStoreStatisticsData() {
		String statisticsData = null;
		StringBuffer statisticsDataStringBuffer = new StringBuffer();
		// 获取GO精品自身的统计数据
		String goSotreItselfStatisticsData = getGoSotreItselfStatisticsData();
		if (goSotreItselfStatisticsData != null && !"".equals(goSotreItselfStatisticsData.trim())) {
			statisticsDataStringBuffer.append(goSotreItselfStatisticsData);
		}
		statisticsData = statisticsDataStringBuffer.toString();
		return statisticsData;
	}

	/**
	 * 获取GO精品自身的统计数据的方法
	 * 
	 * @return
	 */
	private String getGoSotreItselfStatisticsData() {
		String statisticsData = null;
		if (mContext != null) {
			StringBuffer statisticsDataBuffer = new StringBuffer();
			//			// 一级功能ID
			//			statisticsDataBuffer
			//					.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOSTOREAPP);
			//			// 二级功能ID
			//			statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
			//					.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_GOSTORE);
			//			// 精品渠道号
			//			statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
			//					.append(GoStorePhoneStateUtil.getGoStoreUid(mContext));
			// 是否进入过GO精品
			//			int onceEnter = 0;
			//			if (GoStoreStatisticsUtil.getOnceEnterGostore(mContext,
			//					GoStoreStatisticsUtil.ONCE_ENTER_GOSTORE)) {
			//				// 进入过GO精品
			//				onceEnter = 1;
			//				statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
			//						.append(onceEnter);
			//				int onceEnterByMenu = 0;
			//				if (GoStoreStatisticsUtil.getOnceEnterGostore(mContext,
			//						GoStoreStatisticsUtil.ONCE_ENTER_GOSTORE_BY_MENU)) {
			//					onceEnterByMenu = 1;
			//				}
			//				statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
			//						.append(onceEnterByMenu);
			//				int onceEnterByWidget = 0;
			//				if (GoStoreStatisticsUtil.getOnceEnterGostore(mContext,
			//						GoStoreStatisticsUtil.ONCE_ENTER_GOSTORE_BY_WIDGET)) {
			//					onceEnterByWidget = 1;
			//				}
			//				statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
			//						.append(onceEnterByWidget);
			//				//获取五个新入口活跃记录
			//				int[] entryList = GoStoreStatisticsUtil.getEntryLively(mContext);
			//				if(entryList != null && entryList.length >= 5){
			//					statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
			//					.append(entryList[0]);
			//					statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
			//					.append(entryList[1]);
			//					statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
			//					.append(entryList[2]);
			//					statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
			//					.append(entryList[3]);
			//					statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
			//					.append(entryList[4]);
			//				}
			//			} else {
			// 如果没有进入过GO精品
			//				statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING)
			//						.append(onceEnter)
			//						.append(STATISTICS_DATA_SEPARATE_STRING).append(0)
			//						.append(STATISTICS_DATA_SEPARATE_STRING).append(0)
			//						.append(STATISTICS_DATA_SEPARATE_STRING).append(0)
			//						.append(STATISTICS_DATA_SEPARATE_STRING).append(0)
			//						.append(STATISTICS_DATA_SEPARATE_STRING).append(0)
			//						.append(STATISTICS_DATA_SEPARATE_STRING).append(0)
			//						.append(STATISTICS_DATA_SEPARATE_STRING).append(0);
			//			}
			// 内容统计数据
			ArrayList<String> contentStatisticsRecords = GoStoreStatisticsUtil
					.getContentUserDataList(mContext);
			if (contentStatisticsRecords != null && contentStatisticsRecords.size() > 0) {
				for (String record : contentStatisticsRecords) {
					// 一级功能ID
					statisticsDataBuffer
							.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOSTOREAPP);
					// 二级功能ID
					statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(
							StatisticsFuncId.STATICTISC_LEVEL2_FUNID_GOSTORE);
					// 精品渠道号
					statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(
							GoStorePhoneStateUtil.getGoStoreUid(mContext));
					// 三级功能ID
					statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(
							StatisticsFuncId.STATICTISC_LEVEL3_FUNID_GOSTORE_CONTENT);
					if (record != null) {
						record = record.replaceAll(GoStoreStatisticsUtil.USERDATA_IEMT_SPERATE,
								STATISTICS_DATA_SEPARATE_STRING);
					}
					statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(record);
					statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
				}
			}
			//插件统计
			statisticsDataBuffer.append(GoStoreStatisticsUtil.getWidgetRecord(mContext));
			//			// 分类统计数据
			//			ArrayList<String> sortStatisticsRecords = GoStoreStatisticsUtil
			//					.getSortUserDataList(mContext);
			//			if (sortStatisticsRecords != null
			//					&& sortStatisticsRecords.size() > 0) {
			//				for (String record : sortStatisticsRecords) {
			//					statisticsDataBuffer.append(STATISTICS_DATA_LINEFEED);
			//					// 一级功能ID
			//					statisticsDataBuffer
			//							.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOSTOREAPP);
			//					// 二级功能ID
			//					statisticsDataBuffer
			//							.append(STATISTICS_DATA_SEPARATE_STRING)
			//							.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_GOSTORE);
			//					// 精品渠道号
			//					statisticsDataBuffer
			//							.append(STATISTICS_DATA_SEPARATE_STRING).append(
			//									GoStorePhoneStateUtil
			//											.getGoStoreUid(mContext));
			//					// 三级功能ID
			//					statisticsDataBuffer
			//							.append(STATISTICS_DATA_SEPARATE_STRING)
			//							.append(StatisticsFuncId.STATICTISC_LEVEL3_FUNID_GOSTORE_SORT);
			//					if (record != null) {
			//						record = record.replaceAll(
			//								GoStoreStatisticsUtil.USERDATA_IEMT_SPERATE,
			//								STATISTICS_DATA_SEPARATE_STRING);
			//					}
			//					statisticsDataBuffer
			//							.append(STATISTICS_DATA_SEPARATE_STRING).append(
			//									record);
			//				}
			//			}
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}

	/**
	 * 获取GO锁屏应用的统计方法
	 * 
	 * @return
	 */
//	private String getGoLockerAllStatisticsData() {
//		String statisticsData = null;
//		if (mContext != null) {
//			StringBuffer statisticsDataBuffer = new StringBuffer();
//			// 是否有启用锁屏
//			boolean isUserLocker = AppUtils
//					.isServiceRunning(mContext, LauncherEnv.Plugin.LOCKER_PACKAGE,
//							LauncherEnv.Plugin.LOCKER_SERVICE_CLASS_NAME);
//			// 从GO锁屏所提供的 ContentProvider里面获取必要的数据
//			HashMap<String, String> goLockerHashMap = getStatisticsDataFromGoLockerContentProvider(mContext);
//			// 获取GO锁屏主程序统计数据
//			String data = getGoLockerMainAppStatisticsData(isUserLocker, goLockerHashMap);
//			if (data != null && !"".equals(data.trim())) {
//				statisticsDataBuffer.append(data).append(STATISTICS_DATA_LINEFEED);
//			}
//			// 获取GO锁屏主题的统计数据
//			data = getGoLockerThemeStatisticsData(isUserLocker, goLockerHashMap);
//			if (data != null && !"".equals(data.trim())) {
//				statisticsDataBuffer.append(data);
//			}
//			statisticsData = statisticsDataBuffer.toString();
//		}
//		return statisticsData;
//	}

	/**
	 * 获取GO锁屏主程序统计数据的方法
	 * 
	 * @param isUserLocker
	 * @return
	 */
//	private String getGoLockerMainAppStatisticsData(boolean isUserLocker,
//			HashMap<String, String> goLockerHashMap) {
//		String statisticsData = null;
//		if (mContext != null) {
//			// GO锁屏主程序统计信息
//			if (AppUtils.isAppExist(mContext, new Intent(ICustomAction.ACTION_LOCKER))) {
//				StringBuffer statisticsDataBuffer = new StringBuffer();
//				// 如果锁屏主程序有安装
//				// 一级功能点ID和二级功能点ID
//				statisticsDataBuffer.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOLOCKER)
//						.append(STATISTICS_DATA_SEPARATE_STRING)
//						.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_GOLOCKER_MAIN);
//				// 主程序版本名称
//				String versionName = "";
//				PackageInfo packageInfo = AppUtils.getAppPackageInfo(mContext,
//						LauncherEnv.Plugin.LOCKER_PACKAGE);
//				if (packageInfo != null) {
//					versionName = packageInfo.versionName;
//				}
//				statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(versionName);
//				// 主程序渠道号
//				String uid = "200";
//				if (goLockerHashMap != null) {
//					uid = goLockerHashMap.get(STATISTICS_GOLOCKER_UID);
//					if (null == uid) {
//						uid = "200";
//					}
//				}
//				statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(uid);
//				// 锁屏是否有启用
//				int isLocker = 0;
//				if (isUserLocker) {
//					isLocker = 1;
//				}
//				statisticsDataBuffer.append(STATISTICS_DATA_SEPARATE_STRING).append(isLocker);
//				statisticsData = statisticsDataBuffer.toString();
//			}
//		}
//		return statisticsData;
//	}

	/**
	 * 获取GO锁屏主题的统计数据
	 * 
	 * @param isUserLocker
	 * @return
	 */
//	private String getGoLockerThemeStatisticsData(boolean isUserLocker,
//			HashMap<String, String> goLockerHashMap) {
//		String statisticsData = null;
//		if (mContext != null) {
//			StringBuffer statisticsDataBuffer = new StringBuffer();
//			// 正在使用的主题包名
//			String curThemePkgName = null;
//			if (isUserLocker && goLockerHashMap != null) {
//				// 如果启用了锁屏
//				// 从锁屏那里获取正在使用的主题包名,如果没有启用锁屏，就不需要获取了
//				curThemePkgName = goLockerHashMap.get(STATISTICS_GOLOCKER_USING_THEME_PKG_NAME);
//			}
//			// 内置主题包命
//			String defaultThemePkgName = null;
//			if (goLockerHashMap != null) {
//				defaultThemePkgName = goLockerHashMap
//						.get(STATISTICS_GOLOCKER_DEFAULT_THEME_PKG_NAME);
//			}
//			// 是否正在使用内置主题
//			boolean isUseDefTheme = false;
//			if (curThemePkgName != null && !"".equals(curThemePkgName.trim())
//					&& defaultThemePkgName != null && !"".equals(defaultThemePkgName.trim())
//					&& curThemePkgName.equals(defaultThemePkgName)) {
//				isUseDefTheme = true;
//			}
//			PackageManager pm = mContext.getPackageManager();
//			if (pm != null) {
//				// 搜索可能存在未知， 如符合样式标准的程序 (它们就是主题)
//				Intent searchIntent = new Intent(ICustomAction.ACTION_LOCKER_THEME);
//				searchIntent.addCategory(LauncherEnv.Plugin.LOCKER_THEME_CATEGORY);
//				List<ResolveInfo> themes = pm.queryIntentActivities(searchIntent, 0);
//				if (themes != null && themes.size() > 0) {
//					String sPackageName = null;
//					String versionName = null;
//					PackageInfo packageInfo = null;
//					int isUse = 0;
//					for (ResolveInfo info : themes) {
//						// 重置
//						sPackageName = null;
//						versionName = null;
//						packageInfo = null;
//						isUse = 0;
//						// 包名
//						sPackageName = info.activityInfo.packageName;
//						// 一级、二级功能点ID以及包名
//						statisticsDataBuffer
//								.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOLOCKER)
//								.append(STATISTICS_DATA_SEPARATE_STRING)
//								.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_GOLOCKER_THEME)
//								.append(STATISTICS_DATA_SEPARATE_STRING).append(sPackageName)
//								.append(STATISTICS_DATA_SEPARATE_STRING);
//						// 版本名称
//						packageInfo = AppUtils.getAppPackageInfo(mContext, sPackageName);
//						if (packageInfo != null) {
//							versionName = packageInfo.versionName;
//						}
//						if (null == versionName) {
//							versionName = "";
//						}
//						statisticsDataBuffer.append(versionName).append(
//								STATISTICS_DATA_SEPARATE_STRING);
//						// 是否正在使用
//						if (!isUseDefTheme && curThemePkgName != null
//								&& !"".equals(curThemePkgName.trim())
//								&& curThemePkgName.equals(sPackageName)) {
//							isUse = 1;
//						}
//						statisticsDataBuffer.append(isUse).append(STATISTICS_DATA_LINEFEED);
//					}
//				}
//			}
//			// 内置主题数据
//			if (defaultThemePkgName != null && !"".equals(defaultThemePkgName.trim())) {
//				// 如果能获取到内置主题包命，就把该数据添加进去
//				statisticsDataBuffer.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOLOCKER)
//						.append(STATISTICS_DATA_SEPARATE_STRING)
//						.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_GOLOCKER_DEFAULT_THEME)
//						.append(STATISTICS_DATA_SEPARATE_STRING).append(defaultThemePkgName)
//						.append(STATISTICS_DATA_SEPARATE_STRING);
//				int isUse = 0;
//				if (isUseDefTheme) {
//					isUse = 1;
//				}
//				statisticsDataBuffer.append(isUse);
//			}
//			statisticsData = statisticsDataBuffer.toString();
//		}
//		return statisticsData;
//	}

	private String getHttpExceptionStatisticsDate() {
		String statisticsData = null;
		if (mContext != null) {
			StringBuffer statisticsDataBuffer = new StringBuffer();
			Map<String, ?> date = StatisticsData.getHttpExceptionDate(mContext);
			Set<String> keys = date.keySet();
			for (String key : keys) {
				statisticsDataBuffer
						.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_HTTP_EXCEPTOIN).append(
								STATISTICS_DATA_SEPARATE_STRING);
				Object obj = date.get(key);
				String reason = null;
				if (obj != null && obj instanceof String) {
					reason = (String) obj;
				}
				statisticsDataBuffer.append(reason).append(STATISTICS_DATA_LINEFEED);
			}
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}

	/**
	 * @author zhouxuewen
	 * @return 统计没有上传的数据表
	 */
	private String getNoUploadStatisticsDate() {
		String statisticsData = null;
		if (mContext != null) {
			StringBuffer statisticsDataBuffer = new StringBuffer();
			Map<String, ?> date = StatisticsData.getNoUploadData(mContext);
			Set<String> keys = date.keySet();
			for (String key : keys) {
				statisticsDataBuffer
						.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_NO_UPLOAD_DATA).append(
								STATISTICS_DATA_SEPARATE_STRING);
				statisticsDataBuffer.append(key).append(STATISTICS_DATA_LINEFEED);
			}
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}

	/**
	 * 取得应用管理统计数据
	 * @author zhaojunjie
	 * @return
	 */
	private String getAppManagementStatisticsDate() {
		String statisticsData = null;
		if (mContext != null) {
			StringBuffer statisticsDataBuffer = new StringBuffer();
			Map<String, ?> date = AppManagementStatisticsUtil.getAllDate(mContext);
			Set<String> keys = date.keySet();
			for (String key : keys) {
				Object obj = date.get(key);
				String reason = null;
				if (obj != null && obj instanceof String) {
					reason = (String) obj;
				}
				statisticsDataBuffer.append(reason).append(STATISTICS_DATA_LINEFEED);
			}
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}

	/**
	 * 取得应用管理Tab统计数据
	 * @author zhouxuewen
	 * @return
	 */
	private String getAppManagementTabStatisticsDate() {
		String statisticsData = null;
		if (mContext != null) {
			StringBuffer statisticsDataBuffer = new StringBuffer();
			Map<String, ?> date = AppManagementStatisticsUtil.getTabClickDate(mContext);
			Set<String> keys = date.keySet();
			String uid = getUid(mContext);
			for (String key : keys) {
				Object obj = date.get(key);
				int reason = 0;
				if (obj != null && obj instanceof Integer) {
					reason = (Integer) obj;
				}
				String[] keyReason = key.split(AppManagementStatisticsUtil.ITEM_KEY_SPERATE);
				String tabId = "0";
				String entryId = "0";
				if (keyReason != null && keyReason.length >= 2) {
					tabId = keyReason[0];
					entryId = keyReason[1];
				}
				statisticsDataBuffer
						.append(StatisticsFuncId.STATICTISC_LEVEL1_APPMANAGEMENT_TAB_CLICK)
						.append(STATISTICS_DATA_SEPARATE_STRING).append(tabId)
						.append(STATISTICS_DATA_SEPARATE_STRING).append(String.valueOf(reason))
						.append(STATISTICS_DATA_SEPARATE_STRING).append(entryId)
						.append(STATISTICS_DATA_SEPARATE_STRING).append(uid)
						.append(STATISTICS_DATA_SEPARATE_STRING)
						.append(AppManagementStatisticsUtil.NEW_VERSION)
						.append(STATISTICS_DATA_LINEFEED);
			}
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}

//	/**
//	 * 取得游戏中心Tab统计数据
//	 * @author zhouxuewen
//	 * @return
//	 */
//	private String getGameCenterTabStatisticsDate() {
//		String statisticsData = null;
//		if (mContext != null) {
//			StringBuffer statisticsDataBuffer = new StringBuffer();
//			Map<String, ?> date = GameCenterStatisticsUtil.getTabClickDate(mContext);
//			Set<String> keys = date.keySet();
//			String uid = getUid(mContext);
//			for (String key : keys) {
//				Object obj = date.get(key);
//				int reason = 0;
//				if (obj != null && obj instanceof Integer) {
//					reason = (Integer) obj;
//				}
//				String[] keyReason = key.split(GameCenterStatisticsUtil.ITEM_KEY_SPERATE);
//				String tabId = "0";
//				String entryId = "0";
//				if (keyReason != null && keyReason.length >= 2) {
//					tabId = keyReason[0];
//					entryId = keyReason[1];
//				}
//				statisticsDataBuffer
//						.append(StatisticsFuncId.STATICTISC_LEVEL1_GAMEGEMENT_TAB_CLICK)
//						.append(STATISTICS_DATA_SEPARATE_STRING).append(tabId)
//						.append(STATISTICS_DATA_SEPARATE_STRING).append(String.valueOf(reason))
//						.append(STATISTICS_DATA_SEPARATE_STRING).append(entryId)
//						.append(STATISTICS_DATA_SEPARATE_STRING).append(uid)
//						.append(STATISTICS_DATA_SEPARATE_STRING)
//						.append(GameCenterStatisticsUtil.NEW_VERSION_ID)
//						.append(STATISTICS_DATA_LINEFEED);
//			}
//			statisticsData = statisticsDataBuffer.toString();
//		}
//		return statisticsData;
//	}

	/**
	 * 取得应用推荐统计数据
	 * @author zhaojunjie
	 * @return
	 */
	private String getAppRecommendedStatisticsDate() {
		String statisticsData = null;
		if (mContext != null) {
			StringBuffer statisticsDataBuffer = new StringBuffer();
			Map<String, ?> date = AppRecommendedStatisticsUtil.getAllDate(mContext);
			Set<String> keys = date.keySet();
			for (String key : keys) {
				Object obj = date.get(key);
				String reason = null;
				if (obj != null && obj instanceof String) {
					reason = (String) obj;
				}
				String pkgName = key.substring(0,
						key.indexOf(AppManagementStatisticsUtil.KEY_SPERATE));
				if (AppUtils.isAppExist(mContext, pkgName)) {
					String[] reasons = reason
							.split(StatisticsData.STATISTICS_DATA_SEPARATE_STRING_ITEM);
					try {
						int clickCount = Integer.valueOf(reasons[4]);
						int installCount = Integer.valueOf(reasons[6]);
						if (clickCount > 0 && installCount == 0) {
							installCount++;
						}
						reasons[6] = String.valueOf(installCount);
						StringBuffer newReason = new StringBuffer();
						for (int i = 0; i < reasons.length; i++) {
							newReason.append(reasons[i]).append(STATISTICS_DATA_SEPARATE_STRING);
						}
						// 删除最尾的“||”
						newReason.delete(newReason.lastIndexOf(STATISTICS_DATA_SEPARATE_STRING),
								newReason.length());
						reason = newReason.toString();
					} catch (ArrayIndexOutOfBoundsException e) {
						// TODO: handle exception
					}
				}
				statisticsDataBuffer.append(reason).append(STATISTICS_DATA_LINEFEED);
			}
			statisticsData = statisticsDataBuffer.toString();
		}
		return statisticsData;
	}

//	/**
//	 * 取得游戏中心统计数据
//	 * @author zhaojunjie
//	 * @return
//	 */
//	private String getGameCenterStatisticsDate() {
//		String statisticsData = null;
//		if (mContext != null) {
//			StringBuffer statisticsDataBuffer = new StringBuffer();
//			Map<String, ?> date = GameCenterStatisticsUtil.getAllDate(mContext);
//			Set<String> keys = date.keySet();
//			for (String key : keys) {
//				Object obj = date.get(key);
//				String reason = null;
//				if (obj != null && obj instanceof String) {
//					reason = (String) obj;
//				}
//				String pkgName = key
//						.substring(0, key.indexOf(GameCenterStatisticsUtil.KEY_SPERATE));
//				if (AppUtils.isAppExist(mContext, pkgName)) {
//					try {
//						String[] reasons = reason
//								.split(StatisticsData.STATISTICS_DATA_SEPARATE_STRING_ITEM);
//						int clickCount = Integer.valueOf(reasons[4]);
//						int installCount = Integer.valueOf(reasons[6]);
//						if (clickCount > 0 && installCount == 0) {
//							installCount++;
//						}
//						reasons[6] = String.valueOf(installCount);
//						StringBuffer newReason = new StringBuffer();
//						for (int i = 0; i < reasons.length; i++) {
//							newReason.append(reasons[i]).append(STATISTICS_DATA_SEPARATE_STRING);
//						}
//						// 删除最尾的“||”
//						newReason.delete(newReason.lastIndexOf(STATISTICS_DATA_SEPARATE_STRING),
//								newReason.length());
//						reason = newReason.toString();
//					} catch (ArrayIndexOutOfBoundsException e) {
//						// TODO: handle exception
//					}
//				}
//				statisticsDataBuffer.append(reason).append(STATISTICS_DATA_LINEFEED);
//			}
//			statisticsData = statisticsDataBuffer.toString();
//		}
//		return statisticsData;
//	}

	/**
	 * 从GO锁屏提供的ContentProvider获取所需统计数据的方法
	 * 
	 * @param context
	 * @return
	 */
//	private HashMap<String, String> getStatisticsDataFromGoLockerContentProvider(Context context) {
//		HashMap<String, String> statisticsDataHashMap = null;
//		if (context != null) {
//			ContentResolver contentResolver = context.getContentResolver();
//			if (contentResolver != null) {
//				Uri golockerUri = Uri.parse(STATISTICS_GOLOCKER_URI);
//				Cursor cursor = null;
//				try {
//					cursor = contentResolver.query(golockerUri, null, null, null, null);
//					if (cursor != null && cursor.moveToFirst()) {
//						statisticsDataHashMap = new HashMap<String, String>();
//						statisticsDataHashMap.put(STATISTICS_GOLOCKER_UID,
//								cursor.getString(cursor.getColumnIndex(STATISTICS_GOLOCKER_UID)));
//						statisticsDataHashMap.put(STATISTICS_GOLOCKER_USING_THEME_PKG_NAME, cursor
//								.getString(cursor
//										.getColumnIndex(STATISTICS_GOLOCKER_USING_THEME_PKG_NAME)));
//						statisticsDataHashMap
//								.put(STATISTICS_GOLOCKER_DEFAULT_THEME_PKG_NAME,
//										cursor.getString(cursor
//												.getColumnIndex(STATISTICS_GOLOCKER_DEFAULT_THEME_PKG_NAME)));
//					}
//				} catch (Exception e) {
//					// TODO: handle exception
//				} finally {
//					if (cursor != null) {
//						cursor.close();
//					}
//				}
//			}
//		}
//		return statisticsDataHashMap;
//	}

	/**
	 * 获取是否绑定gmail账号的统计数据
	 */
	public String getBindGmailStatisticsData() {
		String result = null;
		if (mContext != null) {
			StringBuffer statisticsData = new StringBuffer();
			//是否绑定Gmail账号
			int isBindGmail = isBindGmail(mContext) ? 1 : 0;
			statisticsData.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_IS_BIND_GMAIL)
					.append(STATISTICS_DATA_SEPARATE_STRING).append(isBindGmail);
			result = statisticsData.toString();
		}

		return result;

	}

	/**
	 * 清理统计数据的方法
	 */
	public void clearStatisticsData() {
		// 清理GO Launcher数据
		clearGoLauncherStatisticsData();
		// 清理GO Widget数据
		AppCore.getInstance().getGoWidgetManager().clearStatisticData();
		// 清理GOStore统计数据
		clearGoStoreStatisticsData();
		// 清理AllAppsInfo的统计信息
		// StatisticsAppsInfoData.resetStatisticsAllDataInfos(mContext);
		// 清理功能表搜索统计数据
		StatisticsAppFuncSearch.clean(mContext);
		AppManagementStatisticsUtil.getInstance();
		//清空应用管理统计数据
		//add by zhaojunjie
		AppManagementStatisticsUtil.delRecords(mContext);
		AppRecommendedStatisticsUtil.getInstance();
		//清空应用推荐统计数据
		//add by zhaojunjie
		AppRecommendedStatisticsUtil.delRecords(mContext);
//		GameCenterStatisticsUtil.getInstance();
		//清空游戏中心统计数据
		//add by zhouxuewen
//		GameCenterStatisticsUtil.delRecords(mContext);
		//清空GUI统计数据
		//add by yangbing
		GuiThemeStatistics.getInstance(mContext).clearData();

		GoStoreAppStatistics.getInstance(mContext).clearAllData();
	}

	/**
	 * 清理GOLauncher模块统计数据
	 */
	private void clearGoLauncherStatisticsData() {
		if (mContext != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.STATISTICS_DATA_FILE_NAME, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				sharedPreferences.clear();
				sharedPreferences.commit();
			}
		}
	}

	/**
	 * 清理GOStore模块统计数据
	 */
	private void clearGoStoreStatisticsData() {
		// TODO Auto-generated method stub
		// 清理GOStore自身统计数据
		GoStoreStatisticsUtil.clearStatisticsData(mContext);
	}

	/**
	 * 统计数据上传失败
	 */
	public void exceptionStatisticsData() {
		// 通知GO Widget统计数据上传失败
		AppCore.getInstance().getGoWidgetManager().notifyStatisticError();
	}

	/**
	 * 获取语言和国家地区的方法 格式:
	 * SIM卡方式：cn
	 * 系统语言方式：zh-CN
	 * @return
	 */
	public static String language(Context context) {

		String ret = null;

		try {
			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telManager != null) {
				ret = telManager.getSimCountryIso();
				if (ret != null && !ret.equals("")) {
					ret = String.format("%s-%s", Locale.getDefault().getLanguage(), ret);
				}
			}
			if (ret == null || ret.equals("")) {
				ret = String.format("%s-%s", Locale.getDefault().getLanguage(), Locale.getDefault()
						.getCountry());
			}

		} catch (Throwable e) {
			//			 e.printStackTrace();
		}

		//拆分为国家标识
		setCountryMark(ret);

		return null == ret ? "error" : ret;
	}

	/**
	 * 设置为国家标识
	 * @param language
	 * @author zhaojunjie
	 */
	private static void setCountryMark(String language) {
		if (language != null) {
			if (language.indexOf("-") > -1) {
				sCountryMark = language.substring(language.indexOf("-") + 1);
			} else {
				sCountryMark = language;
			}
		} else {
			sCountryMark = "error";
		}
	}

	/**
	 * 取国家标识
	 * @param context
	 * @author zhaojunjie
	 * @return
	 */
	private static String getCountryMark(Context context) {
		// 由于国家标识是在通过取语言时赋值
		// 所以当sCountryMark没被赋值时
		// 先执行language(context)
		if (sCountryMark.equals("")) {
			language(context);
		}
		return sCountryMark;
	}

	/**
	 * 功能简述:判断是否是200渠道包
	 * 功能详细描述:
	 * 注意:
	 * @param context
	 * @return true for 200渠道，false for 非200渠道 
	 */
	public static boolean is200ChannelUid(Context context) {
		String uid = getUid(context);
		return (uid != null && (uid.equals("200") || uid.equals("373"))) ? true : false;
	}

	/**
	 * 获取桌面渠道号的方法
	 * 
	 * @param context
	 * @return
	 */
	public static String getUid(Context context) {
		try {
			String uid = null;
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					IPreferencesIds.UID_CONFIG, Context.MODE_PRIVATE);
			uid = sharedPreferences.getString(IPreferencesIds.UID_CONFIG_KEY, "").trim();
			if (!uid.equals("")) {
				return uid;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		final String defaultUid = "1";
		// 从资源获取流
		InputStream is = null;
		is = context.getResources().openRawResource(R.raw.uid);
		// 读取流内容
		byte[] buffer = new byte[1024];
		try {
			int len = is.read(buffer);
			if (len <= 0) {
				//避免文件为空，不能正常返回值
				return defaultUid;
			}
			byte[] data = new byte[len];
			for (int i = 0; i < len; i++) {
				data[i] = buffer[i];
			}
			// 生成字符串
			String dataStr = new String(data);
			dataStr.trim();
			if (data != null && dataStr.contains("\r\n")) {
				// 去掉回车键
				dataStr = dataStr.replaceAll("\r\n", "");
			}
			return dataStr;
		} catch (IOException e) {
			e.printStackTrace();
			//  IO异常
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return defaultUid;
	}
	/**
	 * 保存随机生成的IMEI的方法
	 * 
	 * @param context
	 * @param deviceId
	 */
	private static void saveDeviceIdToSharedpreference(Context context, String deviceId) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				SHAREDPREFERENCES_RANDOM_DEVICE_ID, Context.MODE_PRIVATE);
		sharedPreferences.putString(RANDOM_DEVICE_ID, deviceId);
		sharedPreferences.commit();
	}
	/**
	 * 获取随机生成的IMEI的方法
	 * 
	 * @author huyong
	 * @return
	 */
	private static String getDeviceIdFromSharedpreference(Context context) {
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				SHAREDPREFERENCES_RANDOM_DEVICE_ID, Context.MODE_PRIVATE);
		return sharedPreferences.getString(RANDOM_DEVICE_ID, DEFAULT_RANDOM_DEVICE_ID);
	}
	/**
	 * 更新桌面使用时间的统计数据的方法
	 */
	public synchronized void increaseUseTime() {
		// Log.i("getView", "increaseUseTime");
		if (mContext != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.STATISTICS_DATA_FILE_NAME, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				int useTime = sharedPreferences.getInt(IPreferencesIds.STATISTICS_USE_TIME_KEY, 0);
				++useTime;
				// Log.i("getView", "increaseUseTime--useTime:"+useTime);
				sharedPreferences.putInt(IPreferencesIds.STATISTICS_USE_TIME_KEY, useTime);
				sharedPreferences.commit();
			}
		}
	}

	/**
	 * 获取桌面使用时间的统计数据的方法
	 */
	private synchronized int getUseTime() {
		// Log.i("getView", "getUseTime");
		int useTime = 0;
		if (mContext != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.STATISTICS_DATA_FILE_NAME, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				useTime = sharedPreferences.getInt(IPreferencesIds.STATISTICS_USE_TIME_KEY, 0);
			}
		}
		// Log.i("getView", "getUseTime--useTime:"+useTime);
		return useTime;
	}

	/**
	 * 保存网络使用时间的方法
	 */
	public void saveNetUseTime(long useTime) {
		if (mContext != null && useTime > 0) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.STATISTICS_DATA_FILE_NAME, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				sharedPreferences.putLong(IPreferencesIds.STATISTICS_NET_TIME_KEY, useTime);
				sharedPreferences.commit();
				// Log.i("getView",
				// "saveNetUseTime-NetUseTime ----->"+useTime);
			}
		}
	}

	/**
	 * 获取网络使用时间的方法
	 */
	public long getNetUseTime() {
		long useTime = 0;
		if (mContext != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.STATISTICS_DATA_FILE_NAME, Context.MODE_PRIVATE);
			if (sharedPreferences != null) {
				useTime = sharedPreferences.getLong(IPreferencesIds.STATISTICS_NET_TIME_KEY, 0);
			}
		}
		// Log.i("getView", "getNetUseTime-NetUseTime ----->"+useTime);
		return useTime;
	}

	// add by huyong 2011-12-05 for all apps data
	private String getAllAppsInfo() {
		// 完整统计信息包括：
		// 1级功能号id = 5
		// 2级功能号id = 1
		// 具体内容
		final String separate = STATISTICS_DATA_SEPARATE_STRING;
		StringBuffer sBuffer = new StringBuffer();

		// 加上统计功能号id，以规范数据格式
		sBuffer.append(StatisticsFuncId.STATICTISC_LEVEL1_FUNID_GOALLAPPS).append(separate)
				.append(StatisticsFuncId.STATICTISC_LEVEL2_FUNID_GOALLAPPS_INFO).append(separate);

		// 具体内容
		String statistics = StatisticsAppsInfoData.getAllAppsInfo(mContext, true);
		sBuffer.append(statistics);

		// just for test 写数据到sd卡中
		//		writeToSDCard(sBuffer.toString());
		return sBuffer.toString();

	}

	// add by huyong 2011-12-05 for all apps data end

	/**
	 * 获取用户运营商代码
	 * 
	 * @author zhouxuewen
	 */
	private String getCnUser() {
		String simOperator = "000";
		try {
			if (mContext != null) {
				// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
				TelephonyManager manager = (TelephonyManager) mContext
						.getSystemService(Context.TELEPHONY_SERVICE);
				simOperator = manager.getSimOperator();
			}
		} catch (Throwable e) {
			// TODO: handle exception
		}

		return simOperator;
	}

	/**
	 * 是否绑定Gmail账号
	 * requires <uses-permission android:name="android.permission.GET_ACCOUNTS" />
	 * @param context
	 * @return 
	 */
	public static boolean isBindGmail(Context context) {
		boolean result = false;
		try {
			AccountManager accountManager = AccountManager.get(context);
			Account[] gmailAccounts = accountManager.getAccountsByType("com.google");
			result = gmailAccounts != null && gmailAccounts.length > 0;
		} catch (Exception e) {
		}
		return result;
	}

	/**
	 * 记录覆盖安装标记
	 * @author zhouxuewen
	 * @param context
	 * @param isNew 0是新安装，1为覆盖安装
	 */
	public static void setUserCover(Context context, String isNew) {
		try {
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					STATISTICS_USER_COMMON, Context.MODE_PRIVATE);
			sharedPreferences.putString(USER_IS_COVER, isNew);
			sharedPreferences.commit();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * 获取覆盖安装标记
	 * @author zhouxuewen
	 * @param context
	 * @return 0是新安装，1为覆盖安装
	 */
	public static String getUserCover(Context context) {
		String isCover = "1";
		try {
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					STATISTICS_USER_COMMON, Context.MODE_PRIVATE);
			isCover = sharedPreferences.getString(USER_IS_COVER, "1");
		} catch (Exception e) {
			// TODO: handle exception
		}
		return isCover;
	}

	/**
	 * <br>
	 * 功能简述:从SDcard获取随机生成的IMEI的方法 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 * 
	 * @return
	 */
	/*private static String getDeviceIdFromSDcard() {
		return getStringFromSDcard(DEVICE_ID_SDPATH);
	}

	private static String getStringFromSDcard(String filePath) {
		String string = null;
		try {
			boolean sdCardExist = Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
			if (sdCardExist) {
				byte[] bs = FileUtil.getByteFromSDFile(filePath);
				string = new String(bs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return string;
	}

	*//**
	 * <br>
	 * 功能简述:保存随机生成的IMEI到SDcard上的方法 <br>
	 * 功能详细描述: <br>
	 * 注意:
	 *//*
	private static void saveDeviceIdToSDcard(String deviceId) {
		writeToSDCard(deviceId, DEVICE_ID_SDPATH);
	}

	*//**
	 * 把字符串写到SD卡的方法
	 * 测试时用的方法
	 * @param data
	 *//*
	private static void writeToSDCard(String data, String filePath) {
		if (data != null) {
			if (filePath == null) {
				filePath = LauncherEnv.Path.SDCARD + LauncherEnv.Path.LAUNCHER_DIR
						+ "/statistics/statistics" + System.currentTimeMillis() + ".txt";
			}
			try {
				boolean sdCardExist = Environment.getExternalStorageState().equals(
						android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
				if (sdCardExist) {
					FileUtil.saveByteToSDFile(data.getBytes(), filePath);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}*/
}
