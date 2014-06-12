package com.jiubang.ggheart.appgame.appcenter.help;

import java.io.File;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.base.data.AppsDetailDownload;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemePurchaseManager;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreAppInforUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 应用推荐模块，工具类
 * 
 * @author zhoujun
 * 
 */
public class RecommAppsUtils {

	// 应用推荐默认显示系统图标大小
	private static final int APP_ICON_WIDTH = 80;
	private static final int APP_ICON_HEIGHT = 80;

	/**
	 * 得到文件名(包括后缀)
	 * 
	 * @param fileName
	 * @return
	 */
	public static String formatFileName(String fileName) {
		if (fileName == null) {
			return null;
		}
		if (fileName.lastIndexOf(File.separator) >= 0) {
			return fileName.substring(fileName.lastIndexOf(File.separator) + 1);
		}
		return null;
	}

	/**
	 * 得到文件名(不包括后缀)
	 * 
	 * @param fileName
	 * @return
	 */
	public static String getSimpleName(String fileName) {
		String str = fileName;
		if (str == null) {
			return null;
		}
		if (str.lastIndexOf(File.separator) >= 0) {
			str = str.substring(str.lastIndexOf(File.separator) + 1);
		}
		if (str.indexOf(".") >= 0) {
			str = str.substring(0, str.lastIndexOf("."));
		}

		return str;
	}

	public static BitmapDrawable loadAppIcon(String iconPath, Context context) {
		try {
			Bitmap bitmap = null;
			Bitmap newBitmap = null;
			if (iconPath != null && !"".equals(iconPath)) {
				bitmap = BitmapFactory.decodeFile(iconPath);
			}

			if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
				Log.e("RecommAppsUtils", iconPath + " is not exist");
				return null;
			}
			int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
			float scale = densityDpi / GoStorePublicDefine.STANDARD_DENSITYDPI;
			newBitmap = zoomBitmap(bitmap, (int) (APP_ICON_WIDTH * scale),
					(int) (APP_ICON_HEIGHT * scale));
			// Log.d("RecommAppsUtils", "densityDpi value : " + densityDpi);
			return new BitmapDrawable(context.getResources(), newBitmap);
		} catch (OutOfMemoryError error) {
			error.printStackTrace();
		}
		return null;
	}

	public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		Matrix matrix = new Matrix();
		float scaleWidht = ((float) w) / width;
		float scaleHeight = ((float) h) / height;
		matrix.postScale(scaleWidht, scaleHeight);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
		return newbmp;
	}

	/**
	 * 根据包名判断，是否已经安装
	 * 
	 * @param context
	 * @param packageName
	 * @param versionName
	 * @return
	 */
	public static boolean isInstalled(Context context, String packageName, String versionName) {
		PackageManager manager = context.getPackageManager();
		PackageInfo info;
		try {
			info = manager.getPackageInfo(packageName, 0);
			if (info != null) {
				if (versionName == null || "".equals(versionName)
						|| versionName.equals(info.versionName)
						|| "Varies with device".equals(info.versionName)) {
					return true;
				}
			}
		} catch (NameNotFoundException e) {
			// e.printStackTrace();
		}
		return false;
	}

	/**
	 * 获取设备分辨率
	 * 
	 * @param context
	 * @return
	 */
	public static String getDisplay(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wMgr.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		return width + "*" + height;
	}

	/**
	 * 生成向服务器请求传递的header
	 * 
	 * 注意：该方法头信息默认clientid为1，代表应用游戏中心请求数据，如果clientid不为1，请使用另一个方法createHttpHeader(Context context, String pversion, int clientId)
	 * 
	 * @param context
	 * @param data
	 * @param pversion
	 *            当前协议的版本
	 */
	public static JSONObject createHttpHeader(Context context, String pversion) {
		return createHttpHeader(context, pversion, 1);
	}
	
	/**
	 * 生成向服务器请求传递的header，需要传入clientId
	 * 
	 * @param pversion 协议版本号
	 * @param clientId 访问id，1:应用&游戏中心 2:功能表搜索 3：插件搜索 4:内置中心
	 * 
	 * @author xiedezhi
	 */
	public static JSONObject createHttpHeader(Context context, String pversion,
			int clientId) {

		if (context != null) {
			JSONObject data = new JSONObject();
			String imei = Statistics.getVirtualIMEI(context);
			try {
				data.put("launcherid", imei);
				data.put("imsi", "360002");
				data.put("hasmarket", GoStoreAppInforUtil.isExistGoogleMarket(context) ? 1 : 0);
				// lang 带上区域信息，如zh_cn,en_us
				// Locale locale = Locale.getDefault();
				data.put("lang", "en");
				data.put("local", "US");
				data.put("channel", GoStorePhoneStateUtil.getUid(context));
				data.put("sys", Build.MODEL);
				data.put("sdk", Build.VERSION.SDK_INT);
				data.put("dpi", getDisplay(context));
				data.put("pversion", pversion);
				data.put("netlog",
						DownloadUtil.getNetLog(context, AppsDetailDownload.KEY_NETLOG_MARK));
				data.put("net", buildNetworkState(context));
				//增加访问id，1:应用&游戏中心 2:功能表搜索 3：插件搜索 4:内置中心
				data.put("clientid", clientId);
				String androidId = Machine.getAndroidId();
				if (androidId == null) {
					androidId = "";
				}
				data.put("androidid", androidId);
				data.put("cversion", buildVersion(context));
				// 2.9增加字段，是否支持内购
				data.put("sbuy", Integer.parseInt(GoStorePhoneStateUtil.isAppInSupported(context)));
				// 是否为官方包
				data.put("official", 0);
				// TODO 木瓜token
//				data.put("mgtoken", GoMarketPublicUtil.getInstance(context).getAfToken());
				// 3.2增加字段，vip用户，0：普通用户，1：vip用户，2：超级vip用户
				data.put("vip", ThemePurchaseManager.getCustomerLevel(context));
				// 3.3增加字段，为了是vip用户能够正常解析go锁屏的资源包；
				//需要服务端处理go锁屏版本的兼容（旧版本go锁屏不支持资源包主题）
				data.put("lockervc", getGoLocherVersion(context));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return data;
		}
		return null;
	}

	/**
	 * 获取用户运营商代码
	 * 
	 * @return
	 */
	public static String getCnUser(Context context) {
		String simOperator = "000";
		try {
			if (context != null) {
				// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
				TelephonyManager manager = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				simOperator = manager.getSimOperator();
			}
		} catch (Throwable e) {
			// TODO: handle exception
		}

		return simOperator;
	}

	/**
	 * 获取SIM卡所在的国家
	 * 
	 * @author xiedezhi
	 * @param context
	 * @return 当前手机sim卡所在的国家，如果没有sim卡，取本地语言代表的国家
	 */
	public static String local(Context context) {
		String ret = null;
		// 根据桌面语言设置请求的语言信息
		DeskResourcesConfiguration dc = DeskResourcesConfiguration.getInstance();
		Locale locale = null;
		if (dc != null) {
			locale = dc.getmLocale();
		}
		if (locale == null) {
			locale = Locale.getDefault();
		}
		try {
			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telManager != null) {
				ret = telManager.getSimCountryIso();
			}
		} catch (Throwable e) {
			// e.printStackTrace();
		}

		if (ret == null || ret.equals("")) {
			ret = locale.getCountry().toLowerCase();
		}
		return null == ret ? "error" : ret;
	}

	/**
	 * 获取语言和国家地区的方法 格式: SIM卡方式：cn 系统语言方式：zh-CN
	 * 
	 * @return
	 */
	public static String language(Context context) {

		String ret = null;
		// 根据桌面语言设置请求的语言信息
		DeskResourcesConfiguration dc = DeskResourcesConfiguration.getInstance();
		Locale locale = null;
		if (dc != null) {
			locale = dc.getmLocale();
		}
		if (locale == null) {
			locale = Locale.getDefault();
		}
		try {
			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telManager != null) {
				ret = telManager.getSimCountryIso();
				if (ret != null && !ret.equals("")) {
					ret = String.format("%s_%s", locale.getLanguage().toLowerCase(),
							ret.toLowerCase());
				}
			}
		} catch (Throwable e) {
			// e.printStackTrace();
		}

		if (ret == null || ret.equals("")) {
			ret = String.format("%s_%s", locale.getLanguage().toLowerCase(), locale.getCountry()
					.toLowerCase());
		}
		return null == ret ? "error" : ret;
	}

	/**
	 * 获取当前网络状态，wifi，GPRS，3G，4G
	 * 
	 * @param context
	 * @return
	 */
	private static String buildNetworkState(Context context) {
		// build Network conditions
		String ret = "";
		try {
			ConnectivityManager manager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkinfo = manager.getActiveNetworkInfo();
			if (networkinfo.getType() == ConnectivityManager.TYPE_WIFI) {
				ret = "WIFI";
			} else if (networkinfo.getType() == ConnectivityManager.TYPE_MOBILE) {
				int subtype = networkinfo.getSubtype();
				switch (subtype) {
					case TelephonyManager.NETWORK_TYPE_1xRTT :
					case TelephonyManager.NETWORK_TYPE_CDMA :
					case TelephonyManager.NETWORK_TYPE_EDGE :
					case TelephonyManager.NETWORK_TYPE_GPRS :
					case TelephonyManager.NETWORK_TYPE_IDEN :
						// 2G
						ret = "2G" /*+ "(typeid = " + networkinfo.getType() + "  typename = "
									+ networkinfo.getTypeName() + "  subtypeid = "
									+ networkinfo.getSubtype() + "  subtypename = "
									+ networkinfo.getSubtypeName() + ")"*/;
						break;
					case TelephonyManager.NETWORK_TYPE_EVDO_0 :
					case TelephonyManager.NETWORK_TYPE_EVDO_A :
					case TelephonyManager.NETWORK_TYPE_HSDPA :
					case TelephonyManager.NETWORK_TYPE_HSPA :
					case TelephonyManager.NETWORK_TYPE_HSUPA :
					case TelephonyManager.NETWORK_TYPE_UMTS :
						// 3G,4G
						ret = "3G/4G" /*+ "(typeid = " + networkinfo.getType() + "  typename = "
										+ networkinfo.getTypeName() + "  subtypeid = "
										+ networkinfo.getSubtype() + "  subtypename = "
										+ networkinfo.getSubtypeName() + ")"*/;
						break;
					case TelephonyManager.NETWORK_TYPE_UNKNOWN :
					default :
						// unknow
						ret = "UNKNOW" /*+ "(typeid = " + networkinfo.getType() + "  typename = "
										+ networkinfo.getTypeName() + "  subtypeid = "
										+ networkinfo.getSubtype() + "  subtypename = "
										+ networkinfo.getSubtypeName() + ")"*/;
						break;
				}
			} else {
				ret = "UNKNOW" /*+ "(typeid = " + networkinfo.getType() + "  typename = "
								+ networkinfo.getTypeName() + ")"*/;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 获取桌面版本号
	 * 
	 * @author xiedezhi
	 * 
	 * @param context
	 * @return
	 */
	public static String buildVersion(Context context) {
		String ret = "";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			ret = pi.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * 将bitmap转成drawable
	 * 
	 * @param drawable
	 * @return
	 */
	public static Bitmap drawableToBitmap(Context context, Drawable drawable) {

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}
	
	public static int getGoLocherVersion(Context context) {
		if (AppUtils.isAppExist(context, new Intent(ICustomAction.ACTION_LOCKER))) {
			PackageManager manager = context.getPackageManager();
			try {
				PackageInfo info = manager.getPackageInfo(LauncherEnv.Plugin.LOCKER_PACKAGE, 0);
				return info.versionCode;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				return 0;
			}
		} else {
			return 0;
		}
	}
}
