package com.jiubang.ggheart.apps.gowidget.gostore.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Random;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.gau.go.launcherex.R;
import com.go.util.file.FileUtil;
import com.jiubang.ggheart.appgame.appcenter.component.CommandManager;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.GoStoreChannelControl;
import com.jiubang.ggheart.billing.PurchaseSupportedManager;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * <br>类描述:精品用户手机信息获取工具类
 * <br>功能详细描述:
 * 
 * @author  zhouxuewen
 * @date  [2012-9-12]
 */
public class GoStorePhoneStateUtil {

	public static final int NETTYPE_MOBILE = 0; // 中国移动
	public static final int NETTYPE_UNICOM = 1; // 中国联通
	public static final int NETTYPE_TELECOM = 2; // 中国电信

	// // 判定用户是否为中国大陆用户,用运营商的方式判断
	// public static boolean isCnUser(Context context)
	// {
	// boolean result = false;
	// if(context != null){
	// // 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
	// TelephonyManager manager = (TelephonyManager)
	// context.getSystemService(Context.TELEPHONY_SERVICE);
	//
	// String simOperator = manager.getSimOperator();
	// String locationCode = null;
	// if (simOperator != null && simOperator.length() >= 3)
	// {
	// // 获取前3位
	// locationCode = simOperator.substring(0, 3);
	//
	// // 中国大陆的前5位是(46000)，上边只取3前三位
	// if (locationCode.equals("460"))
	// {
	// // 简体中文
	// result = true;
	// }
	// }
	// }
	// return result;
	// }// end decide

	public static boolean isCnUser(Context context) {
		boolean result = false;

		if (context != null) {
			// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
			TelephonyManager manager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			// SIM卡状态
			boolean simCardUnable = manager.getSimState() != TelephonyManager.SIM_STATE_READY;
			String simOperator = manager.getSimOperator();

			if (simCardUnable || TextUtils.isEmpty(simOperator)) {
				// 如果没有SIM卡的话simOperator为null，然后获取本地信息进行判断处理
				// 获取当前国家或地区，如果当前手机设置为简体中文-中国，则使用此方法返回CN
				String curCountry = Locale.getDefault().getCountry();
				if (curCountry != null && curCountry.contains("CN")) {
					// 如果获取的国家信息是CN，则返回TRUE
					result = true;
				} else {
					// 如果获取不到国家信息，或者国家信息不是CN
					result = false;
				}
			} else if (simOperator.startsWith("460")) {
				// 如果有SIM卡，并且获取到simOperator信息。
				/**
				 * 中国大陆的前5位是(46000) 中国移动：46000、46002 中国联通：46001 中国电信：46003
				 */
				result = true;
			}
		}

		return result;
	}

	/**
	 * 是否国外用户或国内的有电子市场的用户，true for yes，or false for no
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static boolean isOverSeaOrExistMarket(Context context) {
		boolean result = false;
		boolean isCnUser = isCnUser(context);
		// 全部的国外用户 + 有电子市场的国内用户
		if (isCnUser) {
			// 是国内用户，则进一步判断是否有电子市场
			result = GoStoreAppInforUtil.isExistGoogleMarket(context);
		} else {
			// 是国外用户
			result = true;
		}
		return result;
	}

	/**
	 * 采用随机数的形式模拟imei号，避免申请权限。
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static String getVirtualIMEI(Context context) {
		/**
		 * 
		 * <br>类描述:内部类
		 * <br>功能详细描述:
		 * 
		 * @author  zhouxuewen
		 * @date  [2012-9-12]
		 */
		class Statistics {
			private final static String RANDOM_DEVICE_ID = "random_device_id"; // 存入sharedPreference中的key
			private final static String DEFAULT_RANDOM_DEVICE_ID = "0000000000000000"; // 默认随机IMEI
			private final static String SHAREDPREFERENCES_RANDOM_DEVICE_ID = "randomdeviceid"; // sharedPreference文件名
			private final String mDEVICE_ID_SDPATH = LauncherEnv.Path.SDCARD
					+ LauncherEnv.Path.LAUNCHER_DIR + "/statistics/statistics/deviceId" + ".txt";
			/**
			 * 采用随机数的形式模拟imei号，避免申请权限。
			 * 
			 * @author huyong
			 * @param context
			 * @return
			 */
			public String getVirtualIMEI(Context context) {
				String deviceidString = getDeviceIdFromSharedpreference(context);
				// Sharedpreference中没有找到，就从SDcard获取，如果还没有则自动生成一个，并保存下来
				if (deviceidString != null && deviceidString.equals(DEFAULT_RANDOM_DEVICE_ID)) {
					deviceidString = getDeviceIdFromSDcard();
					//如果SD卡上拿到数据的话就视为旧用户，保存到Sharedpreference,如果拿不到数据就是新用户，随机生成。
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
							//TODO 用户之前已经使用过GO桌面的标志。
							StatisticsData.saveIsOldUser(context);
						}
						saveDeviceIdToSharedpreference(context, deviceidString);
					} catch (Exception e) {
						e.printStackTrace();
					}
					//Sharedpreference找到了deviceID但是SDcard上没有，就保存到SD卡上。
				} else if (getDeviceIdFromSDcard() == null) {
					saveDeviceIdToSDcard(deviceidString);
				}
				return deviceidString;
			}
			/**
			 * <br>功能简述:从SDcard获取随机生成的IMEI的方法
			 * <br>功能详细描述:
			 * <br>注意:
			 * @return
			 */
			private String getDeviceIdFromSDcard() {
				return getStringFromSDcard(mDEVICE_ID_SDPATH);
			}
			private String getStringFromSDcard(String filePath) {
				String string = null;
				try {
					boolean sdCardExist = Environment.getExternalStorageState().equals(
							android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
					if (sdCardExist) {
						byte[] bs = FileUtil.getByteFromSDFile(filePath);
						string = new String(bs);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return string;
			}
			/**
			 * <br>功能简述:保存随机生成的IMEI到SDcard上的方法
			 * <br>功能详细描述:
			 * <br>注意:
			 */
			private void saveDeviceIdToSDcard(String deviceId) {
				writeToSDCard(deviceId, mDEVICE_ID_SDPATH);
			}
			private void saveDeviceIdToSharedpreference(Context context, long deviceId) {
				PreferencesManager sharedPreferences = new PreferencesManager(context,
						SHAREDPREFERENCES_RANDOM_DEVICE_ID, Context.MODE_PRIVATE);
				String device = String.valueOf(deviceId);
				sharedPreferences.putString(RANDOM_DEVICE_ID, device);
				sharedPreferences.commit();
			}
			private void saveDeviceIdToSharedpreference(Context context, String deviceId) {
				PreferencesManager sharedPreferences = new PreferencesManager(context,
						SHAREDPREFERENCES_RANDOM_DEVICE_ID, Context.MODE_PRIVATE);
				sharedPreferences.putString(RANDOM_DEVICE_ID, deviceId);
				sharedPreferences.commit();
			}
			/**
			 * 从Sharedpreference获取上次保存的主题包名
			 * 
			 * @author huyong
			 * @return
			 */
			private String getDeviceIdFromSharedpreference(Context context) {
				PreferencesManager sharedPreferences = new PreferencesManager(context,
						SHAREDPREFERENCES_RANDOM_DEVICE_ID, Context.MODE_PRIVATE);
				return sharedPreferences.getString(RANDOM_DEVICE_ID, DEFAULT_RANDOM_DEVICE_ID);
			}
			/**
			 * 把字符串写到SD卡的方法
			 * 
			 * @param data
			 */
			private void writeToSDCard(String data, String filePath) {
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		return new Statistics().getVirtualIMEI(context);
	}

	/**
	 * 从res/raw/uid.txt文件中获取渠道id
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static String getUid(Context context) {
		String uid = "200"; // uid默认为200
		if (context == null) {
			return uid;
		}
		// 从资源获取流
		InputStream is = context.getResources().openRawResource(R.raw.uid);
		try {
			byte[] buffer = new byte[64];
			int len = is.read(buffer); // 读取流内容
			if (len > 0) {
				uid = new String(buffer, 0, len).trim(); // 生成字符串
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return uid;
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
	 * 从res/raw/gostore_uid.txt文件中获取渠道id
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static String getGoStoreUid(Context context) {
		String gostoreUid = GoStoreChannelControl.GOSTORE_DEFAULT_CHANNEL; // 默认渠道号
		if (context == null) {
			return gostoreUid;
		}
		// 从资源获取流
		InputStream is = context.getResources().openRawResource(R.raw.gostore_uid);
		if (is != null) {
			try {
				byte[] buffer = new byte[64];
				int len = is.read(buffer); // 读取流内容
				if (len > 0) {
					gostoreUid = new String(buffer, 0, len).trim(); // 生成字符串
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return gostoreUid;
	}

	/**
	 * 获取网络类型
	 * 
	 * @author huyong
	 * @param context
	 * @return 1 for 移动，2 for 联通，3 for 电信，-1 for 不能识别
	 */
	public static int getNetWorkType(Context context) {
		int netType = -1;
		// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
		TelephonyManager manager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String simOperator = manager.getSimOperator();
		if (simOperator != null) {
			if (simOperator.startsWith("46000") || simOperator.startsWith("46002")) {
				// 因为移动网络编号46000下的IMSI已经用完，
				// 所以虚拟了一个46002编号，134/159号段使用了此编号
				// 中国移动
				netType = NETTYPE_MOBILE;
			} else if (simOperator.startsWith("46001")) {
				// 中国联通
				netType = NETTYPE_UNICOM;
			} else if (simOperator.startsWith("46003")) {
				// 中国电信
				netType = NETTYPE_TELECOM;
			}
		}
		return netType;
	}

	/**
	 * 检测手机WIFI有没有打开的方法
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifiEnable(Context context) {
		boolean result = false;
		if (context != null) {
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivityManager != null) {
				NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()
						&& networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
					result = true;
				}
			}
		}
		return result;
	}

	//获取本地IP函数
	public static String getLocalIPAddress() {
		try {
			for (Enumeration<NetworkInterface> mEnumeration = NetworkInterface
					.getNetworkInterfaces(); mEnumeration.hasMoreElements();) {
				NetworkInterface intf = mEnumeration.nextElement();
				for (Enumeration<InetAddress> enumIPAddr = intf.getInetAddresses(); enumIPAddr
						.hasMoreElements();) {
					InetAddress inetAddress = enumIPAddr.nextElement();
					//如果不是回环地址
					if (!inetAddress.isLoopbackAddress()) {
						//直接返回本地IP地址
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
		}
		return null;
	}

	/**
	 * 判断手机是否ROOT过,ROOT是稳定状态，所以读取后会保存下来，下次从保存记录中拿取，减少使用时间
	 * @author zhouxuewen
	 * @param context
	 * @return 【0：否 ， 1：是】
	 */
	public static String isRoot(Context context) {
		/**
		 * 
		 * <br>类描述:内部类
		 * <br>功能详细描述:
		 * 
		 * @author  zhouxuewen
		 * @date  [2012-9-12]
		 */
		class Root {
			private final static String ROOT_INFO_DATA = "rootinfodata"; // 存入sharedPreference中的key
			private final static String ROOT_INFO = "rootinfo"; // sharedPreference文件名

			public String getRoot(Context context) {
				String rootInfo = getRootInfoFromSharedpreference(context);

				if (rootInfo != null && rootInfo.equals("")) {
					try {
						if (CommandManager.findSu()) {
							rootInfo = "1";
						} else {
							rootInfo = "0";
						}
						saveRootInfoToSharedpreference(context, rootInfo);
					} catch (Exception e) {
						// TODO: handle exception
						e.printStackTrace();
					}
				}
				return rootInfo;
			}

			private void saveRootInfoToSharedpreference(Context context, String rootInfo) {
				PreferencesManager sharedPreferences = new PreferencesManager(context, ROOT_INFO,
						Context.MODE_PRIVATE);
				String device = String.valueOf(rootInfo);
				sharedPreferences.putString(ROOT_INFO_DATA, device);
				sharedPreferences.commit();
			}

			private String getRootInfoFromSharedpreference(Context context) {
				PreferencesManager sharedPreferences = new PreferencesManager(context, ROOT_INFO,
						Context.MODE_PRIVATE);
				return sharedPreferences.getString(ROOT_INFO_DATA, "");
			}

		}

		return new Root().getRoot(context);

	}

	/**
	 * 判断手机是否支持AppIn,状态是稳定状态，所以读取后会保存下来，下次从保存记录中拿取，减少使用时间
	 * @author zhouxuewen
	 * @param context
	 * @return 【0：否 ， 1：是】
	 */
	public static String isAppInSupported(Context context) {
		boolean supported = PurchaseSupportedManager.checkBillingSupported(context);
		return supported ? "1" : "0";
	}

	/**
	 * 判断SDCard是否可以读写的方法 如果没有SDCard则返回false
	 * 
	 * @return 如果可以读写，则返回true,否则返回false
	 */
	public static boolean isSDCardAccess() {
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
	}

	/**
	 * 检测手机网络是否可用的方法
	 * 
	 * @return 可用返回TRUE,否则返回FALSE
	 */
	public static boolean isNetWorkAvailable(Context context) {
		boolean result = false;
		if (context != null) {
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivityManager != null) {
				NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
				if (networkInfo != null && networkInfo.isConnected()) {
					result = true;
				}
			}
		}
		return result;
	}

}
