package com.jiubang.ggheart.appgame.base.data;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Locale;

import org.apache.http.conn.ConnectTimeoutException;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.jiubang.ggheart.data.statistics.Statistics;

/**
 * 封装收集网络信息的类
 * 
 * @author zhoujun
 * 
 */
public class AppGameNetInfoLog {
	//
	// 序号 名称 参数类型 说明 备注
	// 1 net String 网络类型 wifi,GPRS,3G,4G等
	// 2 mpo String 手机运营商 　
	// 3 sip String 连接的服务器ip 域名解析到的服务器ip
	// 4 ct String 建立连接时间 单位ms
	// 5 ds String 下载数据的速度 单位kb/s
	// 6 fc String 连接失败的http错误码 　
	// 7 dt String 数据采集时间 北京时间
	// 8 imei String IMEI码 　
	// 9 local String 地区 　
	// 10 type int 访问类型 1：获取应用列表2：下载软件
	// 11 url Strung 访问的url
	// 各字段以||分隔，每条请求的网络信息占一行：
	// net||mpo||sip||ct||ds||fc||dt||imei||local||type||url

	private Context mContext;

	/**
	 * 统计数据各字段分隔符
	 */
	private static final String STATISTICS_DATA_SEPARATE_STRING = "||";

	/**
	 * 未知网络状态
	 */
	private static final String NETWORK_STATE_UNKNOW = "UNKNOW";
	/**
	 * 当前网络状态为wifi连接
	 */
	private static final String NETWORK_STATE_WIFI = "WIFI";
	/**
	 * 当前网络状态为gprs
	 */
	private static final String NETWORK_STATE_2G = "2G";
	/**
	 * 当前网络状态为3G或4G
	 */
	private static final String NETWORK_STATE_3G4G = "3G/4G";

	/**
	 * 请求数据的url
	 */
	private String mUrl = null;
	/**
	 * 服务器IP
	 */
	private String mServerIP = null;
	/**
	 * 当前网络状态，开始记录的时候记下网络状态
	 */
	private String mNetworkState = null;

	/**
	 * 网络连接时间
	 */
	private long mConnectionTime;

	/**
	 * 下载速度
	 */
	private String mDwnloadSpeed;

	/**
	 * 异常错误码
	 */
	private String mFaildCode;

	/**
	 * 异常错误信息
	 */
	private String mFaildMsg;

	/**
	 * 访问类型 1：获取应用列表2：下载软件;3:获取详情
	 */
	private int mType;

	/**
	 * 访问类型，获取应用列表
	 */
	public static final int NETLOG_TYPE_FOR_APP_LIST = 1;

	/**
	 * 访问类型；下载软件
	 */
	public static final int NETLOG_TYPE_FOR_DOWNLOAD_APK = 2;

	/**
	 * 访问类型； 应用详情
	 */
	public static final int NETLOG_TYPE_FOR_APP_DETAIL = 3;
	/**
	 * 访问类型； 应用搜索
	 */
	public static final int NETLOG_TYPE_FOR_APP_SEARCH = 4;

	/**
	 * 网络连接类型	默认为普通连接
	 */
	private int mLinkType = LINK_TYPE_NORMAL;

	/**
	 * 网络连接类型	普通连接
	 */
	public static final int LINK_TYPE_NORMAL = 1;

	/**
	 * 网络连接类型	长连接
	 */
	public static final int LINK_TYPE_ALIVE = 2;

	public void setmConnectionTime(long mConnectionTime) {
		this.mConnectionTime = mConnectionTime;
	}

	public void setmDwnloadSpeed(String dwnloadSpeed) {
		this.mDwnloadSpeed = dwnloadSpeed;
	}

	public Context getContext() {
		return mContext;
	}

	public AppGameNetInfoLog(Context context, int type) {
		this.mContext = context;
		this.mUrl = null;
		this.mNetworkState = buildNetworkState(context);
		this.mType = type;
		this.mDwnloadSpeed = "9999";
		this.mConnectionTime = 0;
		this.mFaildCode = "";
		this.mFaildMsg = "";
	}

	/**
	 * 如果开启了记录器，就把请求数据的url记录下来
	 * 
	 * @param url
	 */
	public void setUrl(String url) {
		this.mUrl = url;
		this.mServerIP = buildServerIP();
	}

	/**
	 * 设置网络连接类型
	 * @param linkType
	 */
	public void setLinkType(int linkType) {
		mLinkType = linkType;
	}

	/**
	 * 记录错误信息
	 * 
	 * @param e
	 *            异常
	 */
	public void setExceptionCode(Throwable e) {
		// e.printStackTrace();
		// 1000：无网络(本地网络不通)；1001：连接不通(地址错误或者服务器故障)；1002：建立连接超时；1003：传输数据时连接被重置；
		// 1004：其他io错误；1005其他非io错误
		// UnknownHostException SocketException
		// ConnectTimeoutException
		// ConnectException
		// IOException
		if (e != null) {
			if (e instanceof UnknownHostException) {
				mFaildCode = "1001";
			} else if (e instanceof ConnectTimeoutException) {
				mFaildCode = "1002";
			} else if (e instanceof SocketException) {
				mFaildCode = "1003";
			} else if (e instanceof ConnectException) {
				mFaildCode = "1000";
			} else if (e instanceof IOException) {
				mFaildCode = "1004";
			} else {
				mFaildCode = "1005";
			}
			mFaildMsg = e.toString();
		}
	}

	/**
	 * 获取北京时间
	 * 
	 */
	private String getBJTime() {
		String pattern = "yyyy-MM-dd kk:mm:ss";
		java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(pattern);
		java.util.Date date = new java.util.Date();
		String bjTime = df.format(date);
		return bjTime;
	}

	public String createNetLogData() {
		StringBuffer stringBuffer = new StringBuffer();

		stringBuffer.append(this.mNetworkState).append(STATISTICS_DATA_SEPARATE_STRING);
		stringBuffer.append(buildSimOperator(mContext)).append(STATISTICS_DATA_SEPARATE_STRING)
				.append(this.mServerIP).append(STATISTICS_DATA_SEPARATE_STRING)
				.append(this.mConnectionTime).append(STATISTICS_DATA_SEPARATE_STRING)
				.append(this.mDwnloadSpeed).append(STATISTICS_DATA_SEPARATE_STRING)
				.append(mFaildCode).append(STATISTICS_DATA_SEPARATE_STRING).append(getBJTime())
				.append(STATISTICS_DATA_SEPARATE_STRING)
				.append(Statistics.getVirtualIMEI(mContext))
				.append(STATISTICS_DATA_SEPARATE_STRING).append(local(mContext))
				.append(STATISTICS_DATA_SEPARATE_STRING).append(this.mType)
				.append(STATISTICS_DATA_SEPARATE_STRING).append(this.mUrl)
				.append(STATISTICS_DATA_SEPARATE_STRING).append(this.mFaildMsg)
				.append(STATISTICS_DATA_SEPARATE_STRING).append(this.mLinkType);
		return stringBuffer.toString();
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
					case TelephonyManager.NETWORK_TYPE_1xRTT :
					case TelephonyManager.NETWORK_TYPE_CDMA :
					case TelephonyManager.NETWORK_TYPE_EDGE :
					case TelephonyManager.NETWORK_TYPE_GPRS :
					case TelephonyManager.NETWORK_TYPE_IDEN :
						// 2G
						ret = NETWORK_STATE_2G;
						break;
					case TelephonyManager.NETWORK_TYPE_EVDO_0 :
					case TelephonyManager.NETWORK_TYPE_EVDO_A :
					case TelephonyManager.NETWORK_TYPE_HSDPA :
					case TelephonyManager.NETWORK_TYPE_HSPA :
					case TelephonyManager.NETWORK_TYPE_HSUPA :
					case TelephonyManager.NETWORK_TYPE_UMTS :
						// 3G,4G
						ret = NETWORK_STATE_3G4G;
						break;
					case TelephonyManager.NETWORK_TYPE_UNKNOWN :
					default :
						// unknow
						ret = NETWORK_STATE_UNKNOW;
						break;
				}
			} else {
				ret = NETWORK_STATE_UNKNOW;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 获取运营商代码
	 * 
	 * @return
	 */
	private String buildSimOperator(Context context) {
		String ret = "";
		try {
			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telManager != null) {
				ret = telManager.getSimOperator();
			}
		} catch (Throwable e) {
		}
		return ret;
	}

	/**
	 * 获取SIM卡所在的国家
	 * 
	 * @author xiedezhi
	 * @param context
	 * @return 当前手机sim卡所在的国家，如果没有sim卡，取本地语言代表的国家
	 */
	private static String local(Context context) {
		String ret = null;
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
			ret = Locale.getDefault().getCountry().toLowerCase();
		}
		return null == ret ? "error" : ret;
	}

	/**
	 * 获取服务器ip
	 * 
	 * @return
	 */
	private String buildServerIP() {
		if (this.mUrl != null && !this.mUrl.equals("")) {
			try {
				URL url = new URL(this.mUrl);
				String host = url.getHost();
				InetAddress address = InetAddress.getByName(host);
				return address.getHostAddress();
			} catch (Exception e) {
			}
		}
		return "UNKNOW";
	}
}
