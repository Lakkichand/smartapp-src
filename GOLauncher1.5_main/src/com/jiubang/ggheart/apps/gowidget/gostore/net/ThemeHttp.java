package com.jiubang.ggheart.apps.gowidget.gostore.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.go.util.AppUtils;
import com.jiubang.ggheart.apps.desks.diy.themescan.ThemePurchaseManager;
import com.jiubang.ggheart.apps.gowidget.gostore.GoStoreCore;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.net.util.HttpUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreAppInforUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;
import com.jiubang.ggheart.data.statistics.Statistics;
import com.jiubang.ggheart.data.theme.LockerManager;
import com.jiubang.ggheart.data.theme.OnlineThemeGetter;

/**
 * ThemeHttp
 */
public class ThemeHttp {

	public static final int BTYPE_GOSTORE = 0;
	public static final int BTYPE_APPSLIST = 1;

	private static final String PVERSION = "30"; // 请求协议版本号
	public static final String PS = "24"; // 每页包含的记录条数
	private static final int FUNID_MAIN_VIEW = 19; // 商城协议，请求首页数据的funid号
	private static final int FUNID_MAIN_RECOMMEND = 2; // 首页推荐列表

	public static final int ENTRY_DEFAULT = 0;
	public static final int ENTRY_GOSTORE_WIDGET = 1;

	// edit by chenguanyu
	/**
	 * 拼接标准url地址
	 * 
	 * @author huyong
	 * @param context
	 * @param funid
	 *            ：功能号id
	 * @return
	 */
	public static String compoundStandardUrl(Context context, int funid) {
		// 则使用默认地址
		String url = GoStorePublicDefine.URL_HOST3;
		url = compoundStandardUrlWithHost(context, url);
		url += "&funid=" + funid;
		// 当funid=10或者2时，请求推荐列表和分类，要添加类型标识别，0：首页推荐 1：widget推荐
		if (funid == FUNID_MAIN_VIEW || funid == FUNID_MAIN_RECOMMEND) {
			url += "&ty=" + 1;
		}
		return url;
	}

	/**
	 * 拼接标准url地址
	 * 
	 * @author huyong
	 * @param context
	 * @param hostaddress
	 *            ：目标地址
	 * @return
	 */
	public static String compoundStandardUrlWithHost(Context context, String hostaddress) {
		if (hostaddress == null) {
			return null;
		}
		String result = null;
		StringBuilder urlBuilder = new StringBuilder(hostaddress);
		String connectMark = "&";
		if (hostaddress.indexOf("?") < 0) {
			urlBuilder.append("?");
		} else {
			urlBuilder.append(connectMark);
		}
		String imei = GoStorePhoneStateUtil.getVirtualIMEI(context);
		urlBuilder.append("vps=" + HttpUtil.getVps(context, imei, 0) + connectMark);
		if (GoStoreCore.IS_CHANNEL_SHUTDOWN) {
			urlBuilder.append("channel=" + GoStorePhoneStateUtil.getUid(context) + connectMark);
		} else {
			urlBuilder.append("channel=" + GoStorePhoneStateUtil.getGoStoreUid(context)
					+ connectMark);
		}
		// lang 带上区域信息，如zh_cn,en_us
		Locale locale = Locale.getDefault();
		String language = String.format("%s_%s", locale.getLanguage().toLowerCase(), locale
				.getCountry().toLowerCase());
		urlBuilder.append("lang=" + language + connectMark);
		String isFee = "1";
		if (GoStorePhoneStateUtil.isCnUser(context)
				|| !GoStoreAppInforUtil.isExistGoogleMarket(context)) {
			isFee = "0";
		}
		urlBuilder.append("isfee=" + isFee + connectMark);

		// net 运营商，判断是否为中国运营商，是：0；不是：1
		String isCn = "1";
		if (GoStorePhoneStateUtil.isCnUser(context)) {
			isCn = "0";
		}
		urlBuilder.append("net=" + isCn + connectMark);
		// 用户覆盖安装标志
		urlBuilder.append("ow=" + Statistics.getUserCover(context) + connectMark);
		// 协议版本号
		urlBuilder.append("pversion=" + PVERSION + connectMark);
		urlBuilder.append("ps=" + PS + connectMark);
		urlBuilder.append("btype=" + BTYPE_GOSTORE);

		result = urlBuilder.toString();

		return result;

	}

	// end edit

	/**
	 * 返回POST请求数据的方法
	 * 
	 * @param context
	 * @param nameValuePairs
	 *            各模块各自的请求参数
	 * @param funid
	 *            功能号,如果小于0,则不设置该参数
	 * @return
	 */
	public static byte[] getPostData(Context context, List<NameValuePair> nameValuePairs, int funid) {
		return getPostData(context, nameValuePairs, funid, PS);
	}

	public static byte[] getPostData(Context context, List<NameValuePair> nameValuePairs,
			int funid, String ps) {
		return getPostData(context, nameValuePairs, funid, ps, ThemeHttp.ENTRY_DEFAULT);
	}

	public static byte[] getPostData(Context context, List<NameValuePair> nameValuePairs,
			int funid, String ps, int entryid) {
		if (nameValuePairs == null) {
			nameValuePairs = new ArrayList<NameValuePair>();
		}
		compoundNameValuePairs(context, nameValuePairs, funid, BTYPE_GOSTORE, ps, entryid);
		UrlEncodedFormEntity resultEntity = null;
		byte[] postData = null;
		try {
			resultEntity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
			postData = EntityUtils.toByteArray(resultEntity);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return postData;
	}

	private static void compoundNameValuePairs(Context context, List<NameValuePair> nameValuePairs,
			int funid, int bType) {
		compoundNameValuePairs(context, nameValuePairs, funid, bType, PS);
	}

	private static void compoundNameValuePairs(Context context, List<NameValuePair> nameValuePairs,
			int funid, int bType, String ps) {
		compoundNameValuePairs(context, nameValuePairs, funid, bType, PS, ThemeHttp.ENTRY_DEFAULT);
	}

	private static void compoundNameValuePairs(Context context, List<NameValuePair> nameValuePairs,
			int funid, int bType, String ps, int entryId) {
		if (context != null) {
			BasicNameValuePair basicNameValuePair = null;

			if (nameValuePairs == null) {
				nameValuePairs = new ArrayList<NameValuePair>();
			}
			// vps
			String imei = GoStorePhoneStateUtil.getVirtualIMEI(context);
			basicNameValuePair = new BasicNameValuePair("vps", HttpUtil.getVps(context, imei,
					entryId));
			nameValuePairs.add(basicNameValuePair);
			// 根据之前OnlineThemeGetter的getPostValuePairs方法中存入的nameValuePairs来判断，
			//是否为锁屏type，如果是锁屏则上传锁屏自身的uid
			if (nameValuePairs != null
					&& nameValuePairs.size() > 3
					&& nameValuePairs.get(2).getValue()
							.equals(String.valueOf(OnlineThemeGetter.TYPE_LOCKER_FEATURED))
					&& nameValuePairs.get(1).getValue()
							.equals(String.valueOf(OnlineThemeGetter.THEME_APPUID))) {
				basicNameValuePair = new BasicNameValuePair("channel",
						LockerManager.getLockerUid(context));
			}
			// channel
			// 渠道开关判断
			else if (GoStoreCore.IS_CHANNEL_SHUTDOWN) {
				basicNameValuePair = new BasicNameValuePair("channel",
						GoStorePhoneStateUtil.getUid(context));
			} else {
				basicNameValuePair = new BasicNameValuePair("channel",
						GoStorePhoneStateUtil.getGoStoreUid(context));
			}
			nameValuePairs.add(basicNameValuePair);
			// lang 带上区域信息，如zh_cn,en_us
			// Locale locale = Locale.getDefault();
			// String language = String.format("%s_%s",
			// locale.getLanguage().toLowerCase(),
			// locale.getCountry().toLowerCase());
			String language = language(context);
			basicNameValuePair = new BasicNameValuePair("lang", language);
			nameValuePairs.add(basicNameValuePair);
			// isfee
			String isFee = "1";
			if (GoStorePhoneStateUtil.isCnUser(context) || !AppUtils.isMarketExist(context)) {
				// 若是中国用户，或是未安装电子市场，则均为不能收费用户。
				isFee = "0";
			}
			basicNameValuePair = new BasicNameValuePair("isfee", isFee);
			nameValuePairs.add(basicNameValuePair);
			// net 运营商，判断是否为中国运营商，是：0；不是：1
			String isCn = "1";
			if (GoStorePhoneStateUtil.isCnUser(context)) {
				isCn = "0";
			}
			basicNameValuePair = new BasicNameValuePair("net", isCn);
			nameValuePairs.add(basicNameValuePair);
			// pversion 协议版本号
			basicNameValuePair = new BasicNameValuePair("pversion", PVERSION);
			nameValuePairs.add(basicNameValuePair);
			// ps
			basicNameValuePair = new BasicNameValuePair("ps", ps);
			nameValuePairs.add(basicNameValuePair);
			// btype
			basicNameValuePair = new BasicNameValuePair("btype", String.valueOf(bType));
			nameValuePairs.add(basicNameValuePair);
			// ow 覆盖安装标记 add by zhouxuewen
			basicNameValuePair = new BasicNameValuePair("ow", Statistics.getUserCover(context));
			nameValuePairs.add(basicNameValuePair);
			// funid
			if (funid >= 0) {
				basicNameValuePair = new BasicNameValuePair("funid", String.valueOf(funid));
				nameValuePairs.add(basicNameValuePair);
			}

			int vip = ThemePurchaseManager.getCustomerLevel(context);
			basicNameValuePair = new BasicNameValuePair("vip", String.valueOf(vip));
			nameValuePairs.add(basicNameValuePair);
			try {
				PackageInfo info = context.getPackageManager().getPackageInfo(
						context.getPackageName(), 0);
				basicNameValuePair = new BasicNameValuePair("lvercode",
						String.valueOf(info.versionCode));
				nameValuePairs.add(basicNameValuePair);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	/**
	 * 获取语言和国家地区的方法 格式: SIM卡方式：cn 系统语言方式：zh-CN
	 * 
	 * @return
	 */
	private static String language(Context context) {

		String ret = null;
		Locale locale = null;
		if (DeskResourcesConfiguration.getInstance() != null) {
			locale = DeskResourcesConfiguration.getInstance().getmLocale();
		}
		//如果获取桌面的local为空。则获取系统的local
		if (locale == null) {
			locale = Locale.getDefault();
		}
		try {

			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			// SIM卡状态
			boolean simCardUnable = telManager.getSimState() != TelephonyManager.SIM_STATE_READY;
			String simOperator = telManager.getSimOperator();
			// 如果SIM卡正常可用
			if (!(simCardUnable || TextUtils.isEmpty(simOperator))) {
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
			ret = String.format("%s_%s", locale.getLanguage().toLowerCase(), Locale.getDefault()
					.getCountry().toLowerCase());
		}
		return null == ret ? "error" : ret;
	}

	/**
	 * 返回获取程序列表更新的POST请求数据的方法
	 * 
	 * @author huyong
	 * @param context
	 * @param nameValuePairs
	 * @param funid
	 * @param staticsData
	 * @param isPrintStaticsLog
	 *            ：是否需要打印统计日志
	 * @return
	 */
	public static byte[] getAppsListPostData(Context context, List<NameValuePair> nameValuePairs,
			int funid, String staticsData, boolean isPrintStaticsLog) {
		byte[] postData = null;
		if (nameValuePairs == null) {
			nameValuePairs = new ArrayList<NameValuePair>();
		}
		compoundNameValuePairs(context, nameValuePairs, funid, BTYPE_APPSLIST);

		int isPrintStatics = isPrintStaticsLog ? 1 : 0;
		BasicNameValuePair basicNameValuePair = new BasicNameValuePair("islog",
				String.valueOf(isPrintStatics));
		nameValuePairs.add(basicNameValuePair);
		// statics统计数据
		basicNameValuePair = new BasicNameValuePair("apps", staticsData);
		nameValuePairs.add(basicNameValuePair);

		UrlEncodedFormEntity resultEntity = null;
		try {
			resultEntity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
			postData = EntityUtils.toByteArray(resultEntity);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return postData;
	}

	/**
	 * 获取网关
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static String getProxyHost(Context context) {
		return Proxy.getHost(context);
	}

	public static int getProxyPort(Context context) {
		return Proxy.getPort(context);
	}

	/**
	 * 是否cmwap连接
	 * 
	 * @author huyong
	 * @param context
	 * @return
	 */
	public static boolean isCWWAPConnect(Context context) {
		boolean result = false;
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivity.getActiveNetworkInfo();
		if (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE) {
			if (Proxy.getDefaultHost() != null || Proxy.getHost(context) != null) {
				result = true;
			}
		}

		return result;
	}

}
