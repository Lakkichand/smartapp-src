package com.jiubang.ggheart.appgame.base.data;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 取tab栏数据时的错误信息记录工具类，用于网络错误邮箱反馈，单例模式
 * 
 * @author xiedezhi
 * 
 */
//CHECKSTYLE:OFF
public class ClassificationExceptionRecord {
	/**
	 * 未知网络状态
	 */
	private final String NETWORK_STATE_UNKNOW = "UNKNOW";
	/**
	 * 当前网络状态为wifi连接
	 */
	private final String NETWORK_STATE_WIFI = "WIFI";
	/**
	 * 当前网络状态为gprs
	 */
	private final String NETWORK_STATE_2G = "2G";
	/**
	 * 当前网络状态为3G或4G
	 */
	private final String NETWORK_STATE_3G4G = "3G/4G";

	/**
	 * 实例对象
	 */
	private volatile static ClassificationExceptionRecord instance = null;
	/**
	 * 错误日志列表
	 */
	private List<String> logs = null;
	/**
	 * 是否开启记录功能
	 */
	private volatile boolean isStart = false;
	/**
	 * 请求数据的url
	 */
	private String url = null;
	/**
	 * 服务器IP
	 */
	private String serverIP = null;
	/**
	 * 当前网络状态，开始记录的时候记下网络状态
	 */
	private String networkState = null;
	/**
	 * 本机ip，开始记录的时候记下网络状态
	 */
	private String ip = null;
	/**
	 * 开启记录的线程，用于预防其他线程也把错误信息记录下来
	 */
	private int threadHashcode = Integer.MIN_VALUE;

	private ClassificationExceptionRecord() {
		logs = new ArrayList<String>();
	}

	public static ClassificationExceptionRecord getInstance() {
		if (instance == null) {
			synchronized (ClassificationExceptionRecord.class) {
				if (instance == null) {
					instance = new ClassificationExceptionRecord();
				}
			}
		}
		return instance;
	}

	/**
	 * 开启记录功能
	 * 
	 * @param context
	 * @param url
	 *            取tab栏数据的url
	 * @param thread
	 *            开启记录的线程，用于预防其他线程也把错误信息记录下来
	 */
	public void startRecord(Context context, Thread thread) {
		// 把之前保存在SD卡的错误信息附件删除
		File file = new File(LauncherEnv.Path.APP_MANAGER_CLASSIFICATION_EXCEPTION_RECORD_PATH);
		if (file.exists()) {
			file.delete();
		}
		if (thread == null) {
			Log.e("ClassificationExceptionRecord", "startRecord thread == null");
			isStart = false;
			return;
		}
		// 如果网络状态异常，则不开启记录功能
		if (!Machine.isNetworkOK(context)) {
			isStart = false;
			return;
		}
		this.threadHashcode = thread.hashCode();
		this.url = null;
		this.networkState = buildNetworkState(context);
		this.ip = buildLocalIp(context);
		if (logs == null) {
			logs = new ArrayList<String>();
		}
		logs.clear();
		isStart = true;
	}

	/**
	 * 停止记录功能
	 */
	public void stopRecord() {
		isStart = false;
		this.threadHashcode = Integer.MIN_VALUE;
		logs.clear();
	}

	/**
	 * 如果开启了记录器，就把请求数据的url记录下来
	 * 
	 * @param url
	 */
	public void markUrl(String url) {
		if (!isStart || this.threadHashcode != Thread.currentThread().hashCode()) {
			return;
		}
		this.url = url;
		this.serverIP = buildServerIP();
	}

	/**
	 * 是否有记录错误信息
	 * 
	 * @return
	 */
	public boolean hasRecords() {
		if (!isStart || this.threadHashcode != Thread.currentThread().hashCode() || logs == null
				|| logs.size() <= 0) {
			return false;
		}
		return true;
	}

	/**
	 * 记录错误信息
	 * 
	 * @param e
	 *            异常
	 */
	public void record(Throwable e) {
		if (!isStart) {
			Log.e("ClassificationExceptionRecord", "record !isStart");
			return;
		}
		if (this.threadHashcode != Thread.currentThread().hashCode()) {
			Log.e("ClassificationExceptionRecord",
					"record this.threadHashcode != Thread.currentThread().hashCode()");
			return;
		}
		if (e == null) {
			Log.e("ClassificationExceptionRecord", "record e == null");
			return;
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			e.printStackTrace(pw);
			String log = sw.toString();
			log += "Exception time：" + getBJTime() + "\n\r";
			logs.add(log);
		} finally {
			pw.close();
		}
	}

	/**
	 * 记录错误信息，如果服务器下发的数据状态值不为“OK”，则把服务器下发的数据记录下来
	 * 
	 * @param info
	 *            错误信息
	 */
	public void record(String info) {
		if (!isStart || this.threadHashcode != Thread.currentThread().hashCode() || info == null) {
			return;
		}
		info += "\nException time：" + getBJTime() + "\n\r";
		logs.add(info);
	}

	/**
	 * 获取北京时间
	 * 
	 */
	private String getBJTime() {
		java.util.Locale locale = java.util.Locale.CHINA;
		String pattern = "yyyy-MM-dd kk:mm:ss zZ";
		java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(pattern, locale);
		java.util.Date date = new java.util.Date();
		String bjTime = df.format(date);
		return bjTime;
	}

	/**
	 * 预加载没网络的情况，要先删除附件，避免在错误页有反馈按钮
	 */
	public void deleteAttachment() {
		// 把之前保存在SD卡的错误信息附件删除
		File file = new File(LauncherEnv.Path.APP_MANAGER_CLASSIFICATION_EXCEPTION_RECORD_PATH);
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * 生成邮件反馈的附件
	 * 
	 * @return 附件的本地路径名
	 */
	public String buildAttachment(Context context) {
		try {
			if (!isStart) {
				Log.e("ClassificationExceptionRecord", "buildAttachment record not start !");
				return null;
			}
			if (this.threadHashcode != Thread.currentThread().hashCode()) {
				Log.e("ClassificationExceptionRecord",
						"buildAttachment this.thread != Thread.currentThread()");
			}
			if (this.logs == null || logs.size() <= 0) {
				return null;
			}
			// build Launcher VPS
			String vps = buildVPS(context);
			// build country code
			String countryCode = buildCountryCode(context);
			// sim operator
			String simOperator = buildSimOperator(context);
			// build Network conditions
			String networkState = this.networkState;
			// build Client IP
			String ip = this.ip;
			// build Launcher Version
			String version = buildVersion(context);
			// request url
			String url = "url = " + this.url;
			// server ip
			String serverIP = this.serverIP;
			// logs
			String logs = buildLogs();
			// save attachment to SD card
			String attachment = vps + "\n" + countryCode + "\n" + simOperator + "\n" + networkState
					+ "\n" + ip + "\n" + version + "\n" + url + "\n" + serverIP + "\n" + logs;
			String filename = LauncherEnv.Path.APP_MANAGER_CLASSIFICATION_EXCEPTION_RECORD_PATH;
			FileUtil.saveByteToSDFile(attachment.getBytes("UTF-8"), filename);
			return filename;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取桌面vps
	 * 
	 * @param context
	 * @return
	 */
	private String buildVPS(Context context) {
		String imei = Statistics.getVirtualIMEI(context);
		String vps = DownloadUtil.getVps(context, imei);
		return "vps = " + vps;
	}

	/**
	 * 获取当前网络状态，wifi，GPRS，3G，4G
	 * 
	 * @param context
	 * @return
	 */
	private String buildNetworkState(Context context) {
		// build Network conditions
		String ret = "";
		try {
			ConnectivityManager manager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkinfo = manager.getActiveNetworkInfo();
			if (networkinfo.getType() == ConnectivityManager.TYPE_WIFI) {
				ret = NETWORK_STATE_WIFI;
			} else if (networkinfo.getType() == ConnectivityManager.TYPE_MOBILE) {
				int subtype = networkinfo.getSubtype();
				switch (subtype) {
				case TelephonyManager.NETWORK_TYPE_1xRTT:
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_EDGE:
				case TelephonyManager.NETWORK_TYPE_GPRS:
				case TelephonyManager.NETWORK_TYPE_IDEN:
					// 2G
					ret = NETWORK_STATE_2G + "(typeid = " + networkinfo.getType() + "  typename = "
							+ networkinfo.getTypeName() + "  subtypeid = "
							+ networkinfo.getSubtype() + "  subtypename = "
							+ networkinfo.getSubtypeName() + ")";
					break;
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
				case TelephonyManager.NETWORK_TYPE_HSDPA:
				case TelephonyManager.NETWORK_TYPE_HSPA:
				case TelephonyManager.NETWORK_TYPE_HSUPA:
				case TelephonyManager.NETWORK_TYPE_UMTS:
					// 3G,4G
					ret = NETWORK_STATE_3G4G + "(typeid = " + networkinfo.getType()
							+ "  typename = " + networkinfo.getTypeName() + "  subtypeid = "
							+ networkinfo.getSubtype() + "  subtypename = "
							+ networkinfo.getSubtypeName() + ")";
					break;
				case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				default:
					// unknow
					ret = NETWORK_STATE_UNKNOW + "(typeid = " + networkinfo.getType()
							+ "  typename = " + networkinfo.getTypeName() + "  subtypeid = "
							+ networkinfo.getSubtype() + "  subtypename = "
							+ networkinfo.getSubtypeName() + ")";
					break;
				}
			} else {
				ret = NETWORK_STATE_UNKNOW + "(typeid = " + networkinfo.getType() + "  typename = "
						+ networkinfo.getTypeName() + ")";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "net = " + ret;
	}

	/**
	 * 获取本机ip
	 * 
	 * @param context
	 * @return
	 */
	private String buildLocalIp(Context context) {
		String ret = "";
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
					.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
						.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						ret += inetAddress.getHostAddress().toString() + "  ";
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "Client IP = " + ret;
	}

	/**
	 * 获取桌面版本号
	 * 
	 * @param context
	 * @return
	 */
	private String buildVersion(Context context) {
		String ret = "";
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
			ret = pi.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "version = " + ret;
	}

	/**
	 * 生成错误日志
	 * 
	 * @return
	 */
	private String buildLogs() {
		if (logs == null || logs.size() <= 0) {
			return "";
		}
		String ret = "";
		for (String log : logs) {
			ret += log + "\n";
		}
		return "log = " + ret;
	}

	/**
	 * 获取语言和国家地区的方法 格式: SIM卡方式：cn 系统语言方式：zh-CN
	 * 
	 * @param context
	 * @return
	 */
	private String buildCountryCode(Context context) {
		String ret = null;
		try {
			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telManager != null) {
				ret = telManager.getSimCountryIso();
				if (ret != null && !ret.equals("")) {
					ret = String.format("%s || %s", "Local Language:"
							+ Locale.getDefault().getLanguage().toLowerCase(), "SIM Country ISO:"
							+ ret.toLowerCase());
				}
			}
		} catch (Throwable e) {
			// e.printStackTrace();
		}
		if (ret == null || ret.equals("")) {
			ret = String.format("%s || %s", "Local Language:"
					+ Locale.getDefault().getLanguage().toLowerCase(), "Local Country:"
					+ Locale.getDefault().getCountry().toLowerCase());
		}
		return "country = " + ret;
	}

	/**
	 * 获取运营商代码
	 * 
	 * @return
	 */
	private String buildSimOperator(Context context) {
		String ret = "SIM Opeartor = ";
		try {
			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telManager != null) {
				ret += telManager.getSimOperator();
				ret += "  OpeartorName = " + telManager.getSimOperatorName();
			}
			return ret;
		} catch (Throwable e) {
		}
		return "SIM Opeartor = NULL";
	}

	/**
	 * 获取服务器ip
	 * 
	 * @return
	 */
	private String buildServerIP() {
		if (this.url != null && !this.url.equals("")) {
			try {
				URL url = new URL(this.url);
				String host = url.getHost();
				InetAddress address = InetAddress.getByName(host);
				return "Server IP = " + address.getHostAddress();
			} catch (Exception e) {
			}
		}
		return "Server IP = UNKNOW";
	}

	/**
	 * 发送反馈邮件
	 * 
	 * @param context
	 */
	public static void sendFeedbackMail(Context context) {
		// 开启邮箱，发送邮件
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String[] receiver = new String[] { "goreport@goforandroid.com" };
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, receiver);
		String subject = "GO Launcher EX(v" + context.getString(R.string.curVersion)
				+ ") AppCenter/GameZone Feedback";
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
		String body = "\n\n";
		emailIntent.putExtra(Intent.EXTRA_TEXT, body);
		Uri uri = Uri.parse("file://"
				+ LauncherEnv.Path.APP_MANAGER_CLASSIFICATION_EXCEPTION_RECORD_PATH);
		emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
		emailIntent.setType("plain/text");
		try {
			context.startActivity(emailIntent);
		} catch (Exception e) {
			Toast.makeText(context, R.string.appgame_error_record_noemail, Toast.LENGTH_SHORT)
					.show();
			e.printStackTrace();
		}
	}
}
