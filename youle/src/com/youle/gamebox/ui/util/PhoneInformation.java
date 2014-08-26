/**
 * 
 */
package com.youle.gamebox.ui.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * @author wangsj
 * @time:2013-12-5 上午10:07:48
 * 
 */
public class PhoneInformation {
	private String sdkVersion = ""; // 手机系统版本号
	private String releaseVersion = "";// 系统版本
	private String resolution = ""; // 分辨率
	private String phoneModel = ""; // 手机型号
	private String appPackName = ""; // 包名称
	private String brand;//生产商
	private String imsi ;
	private int Width = 0; // 屏幕寬
	private int Height = 0; // 屏幕高

	private TelephonyManager telephonyManager;
	private String networkType = "0";// 手机网络类型 0-移动网络 1-wifi
	private String deviceCode;// 唯一的设备ID GSM手机的 IMEI 和 CDMA手机的 MEID. Return null
								// if device ID is notavailable.
	private String phoneNum;// 手机号： GSM手机的 MSISDN. 大多数情况并不能获取到 Return null if it
							// is unavailable.
	private String networkCountryIso;// 获取ISO标准的国家码，即国际长途区号。 注意：仅当用户已在网络注册后有效。
										// 在CDMA网络中结果也许不可靠。
	private String phoneType;// 手机类型： 例如： PHONE_TYPE_NONE 无信号 PHONE_TYPE_GSM
								// GSM信号 PHONE_TYPE_CDMA CDMA信号
	private String simOperatorName;// 服务商名称： 例如：中国移动、联通 SIM卡的状态必须是
									// SIM_STATE_READY(使用getSimState()判断).
	private CellLocation cellLocation;// 获取手机地域信息
	private NetworkInfo activeNetInfo;

	public PhoneInformation(Context context) {
		getPhoneInformation(context);
	}

	public String getBrand() {
		return brand;
	}

	public String getSdkVersion() {
		return sdkVersion;
	}

	public String getReleaseVersion() {
		return releaseVersion;
	}

	public String getResolution() {
		return resolution;
	}

	public String getPhoneModel() {
		return phoneModel;
	}

	public String getAppPackName() {
		return appPackName;
	}

	public int getWidth() {
		return Width;
	}

	public int getHeight() {
		return Height;
	}

	public TelephonyManager getTelephonyManager() {
		return telephonyManager;
	}

	public String getImsi() {
		return imsi;
	}

	public String getNetworkType() {
		return networkType;
	}

	public String getDeviceCode() {
		return deviceCode;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public String getNetworkCountryIso() {
		return networkCountryIso;
	}

	public String getPhoneType() {
		return phoneType;
	}

	public String getSimOperatorName() {
		return simOperatorName;
	}

	public CellLocation getCellLocation() {
		return cellLocation;
	}

	public NetworkInfo getActiveNetInfo() {
		return activeNetInfo;
	}

	// 获取手机基本信息
	private void getPhoneInformation(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		Width = dm.widthPixels;
		Height = dm.heightPixels;
		Log.d("init", "Width:" + Width + "     Height:" + Height);
		String height = String.valueOf(dm.heightPixels);
		String width = String.valueOf(dm.widthPixels);
		resolution = height + " x " + width;
		phoneModel = android.os.Build.MODEL;
		brand = android.os.Build.BRAND;
		releaseVersion = "Android " + android.os.Build.VERSION.RELEASE;
		sdkVersion = String.valueOf(android.os.Build.VERSION.SDK_INT);
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (telephonyManager != null) {
			deviceCode = telephonyManager.getDeviceId();
			imsi = telephonyManager.getSubscriberId();//获取手机IMSI号
		}
		if (checkNetworkState(context) == 1) {
			networkType = "wifi";
		} else {
			networkType = "数据网络";
		}
		
	}

	// 获取网络类型
	private void getTelephonyModel(Context context) {
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		// 用于判断网络类型
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (telephonyManager != null) {
			// 请求位置更新，如果更新将产生广播，接收对象为注册LISTEN_CELL_LOCATION的对象，需要的permission名称为ACCESS_COARSE_LOCATION
			deviceCode = telephonyManager.getDeviceId();
			networkType = convertNetworkType(telephonyManager.getNetworkType());
			if (activeNetInfo != null)
				networkType = String.valueOf(activeNetInfo.getType());
		}
	}

	/*
	 * 格式化网络类型表示
	 */
	public String convertNetworkType(int networkType) {
		switch (networkType) {
		case 0:
			return null;
		case 1:
			return "GPRS";
		case 2:
			return "EDGE";
		case 3:
			return "UMTS";
		case 4:
			return "UMTS";
		case 5:
			return "EVDO_0";
		case 6:
			return "EVDO_A";
		case 7:
			return "1xRTT";
		case 8:
			return "HSDPA";
		case 9:
			return "HSUPA";
		case 10:
			return "HSPA";
		default:
			return null;
		}
	}

	/*
	 * 格式化手机类型显示
	 */
	public String convertPhoneType(int phoneType) {
		switch (phoneType) {
		case 0:
			return "NONE";
		case 1:
			return "GSM";
		case 2:
			return "CDMA";
		case 3:
			return "SIP";
		default:
			return "NONE";
		}
	}

	/**
	 * 判断网上 是 2G 3G WIFI
	 * 
	 * @param context
	 * @return
	 */
	public static int checkNetworkState(Context activity) {
		int flag = 1;
		ConnectivityManager manager = (ConnectivityManager) activity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		State mobile = null;
		if (null != networkInfo) {
			mobile = networkInfo.getState();
		}
		State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		if (mobile == State.CONNECTED && wifi != State.CONNECTED) {
			flag = 0;
		} else if (wifi == State.CONNECTED && wifi == State.CONNECTING) {
			flag = 1;
		}
		return flag;
	}
}
