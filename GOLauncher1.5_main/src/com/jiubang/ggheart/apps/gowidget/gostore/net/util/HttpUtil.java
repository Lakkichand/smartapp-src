package com.jiubang.ggheart.apps.gowidget.gostore.net.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.go.util.device.Machine;
import com.jiubang.ggheart.apps.gowidget.gostore.net.ThemeHttp;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreAppInforUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;

/**
 * 公共工具类
 * 
 * @author HuYong
 * @version 1.0
 */
public class HttpUtil {

	// private static final String VPS_VERSION = "01.01.00"; //之前分辨率出现计算错误的VPS版本
	private static final String VPS_VERSION = "01.01.01"; // 目前在用的VPS版本

	private static final int BUILDER_LENGTH = 64;
	private static final int STRING_LENGTH = 0xFF;

	/**
	 * 根据vps定义规范，获取本机vps信息(没有经过UTF-8编码)
	 * 
	 * @param context
	 * @param imei
	 * @return vps字符串
	 */
	public static String getVps(Context context, String imei, int entryId) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wMgr.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;

		StringBuilder vpsStringBuilder = new StringBuilder(BUILDER_LENGTH);
		vpsStringBuilder.append("1#");
		vpsStringBuilder.append("Android#");
		vpsStringBuilder.append(Build.MODEL + "#");
		vpsStringBuilder.append(imei + "#");
		vpsStringBuilder.append("166#");

		//把大的放后面
		if (width > height) {
			vpsStringBuilder.append(height + "_" + width + "#");
		} else {
			vpsStringBuilder.append(width + "_" + height + "#");
		}
		// vpsStringBuilder.append(VPS_VERSION);

		// update by zhoujun,添加三个字段：#sdk#imsi#是否安装电子市场(0:未安装 1：已安装)
		vpsStringBuilder.append(VPS_VERSION + "#");
		vpsStringBuilder.append(Build.VERSION.SDK_INT + "#");
		vpsStringBuilder.append(getCnUser(context) + "#");
		int isExistGoogleMarket = GoStoreAppInforUtil.isExistGoogleMarket(context) ? 1 : 0;
		vpsStringBuilder.append(isExistGoogleMarket + "#");

		//是否有SD卡
		String hasSdCard = "0";
		if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			hasSdCard = "1";
		}
		//lang和region两个字段不用再上传，转空串，所以补两个#
		vpsStringBuilder.append(hasSdCard + "###");

		//连网类型 0 为非WIFI,1为WIFI
		String net = "0";
		if (GoStorePhoneStateUtil.isWifiEnable(context) && ThemeHttp.ENTRY_GOSTORE_WIDGET != entryId) {
			//是WIFI并且不是WIDGET进来的
			net = "1";
		}
		vpsStringBuilder.append(net + "#");

		//ROOT状态
		vpsStringBuilder.append(GoStorePhoneStateUtil.isRoot(context) + "#");

		//桌面versioncode，暂时无作用，所以传0
		vpsStringBuilder.append("0#");

		//是否支持应用内付费
		vpsStringBuilder.append(GoStorePhoneStateUtil.isAppInSupported(context) + "#");
		
		//用户电话号码(200包不拿用户电话号码，所以传空）
		vpsStringBuilder.append("#");
		
		//用户Android Id
		vpsStringBuilder.append(Machine.getAndroidId());
		
		// update by zhoujun end 2012-5-14
		String vps = vpsStringBuilder.toString();
		// edit by chenguanyu

		try {
			vps = URLEncoder.encode(vps, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		// end edit
		return vps;
	}

	/**
	 * 获取用户运营商代码
	 * 
	 * @author zhoujun
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
	 * 循环异或，一般用于加密
	 * 
	 * @param bytes
	 * @param keys
	 * @return
	 */
	private static byte[] xor(byte[] bytes, byte key) {
		byte[] result = new byte[bytes.length];
		for (int i = 0; i < bytes.length; i++) {
			result[i] = (byte) ((bytes[i]) ^ key);
		}
		return result;
	}

	private static byte[] xor(byte[] bytes, byte[] keys) {
		byte[] result = bytes;
		for (int i = 0; i < keys.length; i++) {
			result = xor(result, keys[i]);
		}
		return result;
	}

	/**
	 * 通过指定的key，对字符串进行加密，若src为空，则返回null；若keys为空，则src不处理，直接返回。
	 * 
	 * @author huyong
	 * @param src
	 *            待加密字符串
	 * @param keys
	 *            加密密钥
	 * @return 加密后的字符串
	 */
	public static String encrypt(String src, String keys) {
		if (null == src) {
			return null;
		}
		if (null == keys) {
			return src;
		}
		try {
			byte[] result = xor(src.getBytes("utf-8"), keys.getBytes("utf-8"));
			return Base64.encodeBytes(result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 产生MD5加密串
	 * 
	 * @author huyong
	 * @param plainText
	 * @param charset
	 * @return
	 */
	private static String to32BitString(String plainText, String charset) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.reset();
			if (charset != null) {
				md.update(plainText.getBytes(charset));
			} else {
				md.update(plainText.getBytes());
			}
			byte[] byteArray = md.digest();
			StringBuffer md5Buf = new StringBuffer();
			for (int i = 0; i < byteArray.length; i++) {
				if (Integer.toHexString(STRING_LENGTH & byteArray[i]).length() == 1) {
					md5Buf.append("0").append(Integer.toHexString(STRING_LENGTH & byteArray[i]));
				} else {
					md5Buf.append(Integer.toHexString(STRING_LENGTH & byteArray[i]));
				}
			}
			return md5Buf.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 产生32位md5加密字符串
	 * 
	 * @param s
	 *            待加密的字符串
	 * @return md5加密处理后的字符串
	 */
	public final static String mD5generator(String s) {
		// String charset =System.getProperties()
		return to32BitString(s, null);
	}

	/**
	 * 产生32位md5加密字符串
	 * 
	 * @param s
	 *            待加密的字符串
	 * @param charset
	 *            字符编码
	 * @return md5加密处理后的字符串
	 */
	public final static String mD5generator(String s, String charset) {
		return to32BitString(s, charset);
	}

	/**
	 * 写日志接口
	 * 
	 * @author huyong
	 * @param logFileName
	 *            log日志文件名，为null，则默认写入到AdSDK_Log.txt文件中。
	 * @param logText
	 *            写入的log内容
	 */
	public final static void writeLog(String logText, String logFileName) {
		// 获取扩展SD卡设备状态
		String sDStateString = android.os.Environment.getExternalStorageState();
		// 拥有可读可写权限
		if (sDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {
			try {
				// 获取扩展存储设备的文件目录
				File sdFile = android.os.Environment.getExternalStorageDirectory();

				// 打开文件
				String logFilePath = "AdSDK_Log.txt";
				if (logFileName != null && logFileName.length() > 0) {
					logFilePath = logFileName;
				}
				File logFile = new File(sdFile.getAbsolutePath() + File.separator + logFilePath);

				// 判断是否存在,不存在则创建
				if (!logFile.exists()) {
					logFile.createNewFile();
				}

				// 写数据
				FileOutputStream outputStream = new FileOutputStream(logFile, true);
				outputStream.write(logText.getBytes());
				String endLine = "\n";
				outputStream.write(endLine.getBytes());

				outputStream.close();

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			} // end of try

		} // end of if(MEDIA_MOUNTED)
	}

	/**
	 * 删除日志文件接口
	 * 
	 * @author huyong
	 * @param logFileName
	 *            log日志文件名，为null，则默认写入到AdSDK_Log.txt文件中。
	 * @return true for delete ,false for no delete.
	 */
	public boolean delLog(String logFileName) {
		// 获取扩展SD卡设备状态
		String sDStateString = android.os.Environment.getExternalStorageState();
		// 拥有可读可写权限
		if (sDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {
			try {
				// 获取扩展存储设备的文件目录
				File sdFile = android.os.Environment.getExternalStorageDirectory();

				// 打开文件
				String logFilePath = "AdSDK_Log.txt";
				if (logFileName != null && logFileName.length() > 0) {
					logFilePath = logFileName;
				}
				File logFile = new File(sdFile.getAbsolutePath() + File.separator + logFilePath);

				// 判断是否存在,不存在则创建
				if (logFile.exists()) {
					return logFile.delete();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return false;
	}
}
