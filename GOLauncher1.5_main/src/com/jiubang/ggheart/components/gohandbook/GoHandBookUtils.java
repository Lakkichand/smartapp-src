package com.jiubang.ggheart.components.gohandbook;

import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.gau.go.launcherex.R;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.INetRecord;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.ConstValue;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.HttpUtil;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageHttp;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.SimpleHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.components.DeskResourcesConfiguration;

/**
 * 
 * <br>类描述:Go手册工具类
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-5]
 */
public class GoHandBookUtils {
	public static final String HOSTURL_BASE = "http://launchermsg.3g.cn/golaunchermsg/msgservice.do?";
	public static final int REQUEST_URL_SUCCESS = 1; // 请求URL成功
	public static final int REQUEST_URL_FAIL = 0; // 请求URL成功
	private static final String TYPE_URL = "1"; // 请求协议版本号

	/**
	 * <br>功能简述:请求接口获取indexUrl
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param handler
	 */
	public static void getNetworkIndexUrlData(final Context context, final Handler handler) {
		if (context == null || handler == null) {
			return;
		}

		String url = getUrl(); //获取URL地址

		JSONObject requestJson = getRequestUrlJson(context);
		//Log.e("lch", "地址url：" + url);
		//Log.e("lch", "请求参数1：" + requestJson.toString());
		try {
			THttpRequest request = new THttpRequest(url, requestJson.toString().getBytes(),
					new IConnectListener() {
						@Override
						public void onStart(THttpRequest arg0) {
							//Log.e("lch", "onStart");
						}

						@Override
						public void onFinish(THttpRequest request, IResponse response) {
							//Log.e("lch", "onFinish");
							//解析请求数据
							String urlString = responseJson(response);
							if (urlString != null && !urlString.equals("")) {
								sendHandlerMsg(handler, REQUEST_URL_SUCCESS, urlString);
							} else {
								sendHandlerMsg(handler, REQUEST_URL_FAIL, null);
							}
						}

						@Override
						public void onException(THttpRequest arg0, int arg1) {
							//Log.e("lch", "onException = " + arg1);
							sendHandlerMsg(handler, REQUEST_URL_FAIL, null);
						}
					});

			request.setOperator(new StringOperator()); //设置返回数据类型-字符串

			//设置报错提示
			request.setNetRecord(new INetRecord() {

				@Override
				public void onTransFinish(THttpRequest arg0, Object arg1, Object arg2) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onStartConnect(THttpRequest arg0, Object arg1, Object arg2) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onException(Exception e, Object arg1, Object arg2) {
					//e.printStackTrace(); //打印出HTTP请求真实的错误信息
				}

				@Override
				public void onConnectSuccess(THttpRequest arg0, Object arg1, Object arg2) {
					// TODO Auto-generated method stub

				}
			});
			SimpleHttpAdapter httpAdapter = SimpleHttpAdapter.getInstance(context);
			httpAdapter.addTask(request);
		} catch (Exception e) {
			//Log.e("lch", "e:" + e.toString());
			e.printStackTrace();
			sendHandlerMsg(handler, REQUEST_URL_FAIL, null);
			return;
		}
	}

	/**
	 * <br>功能简述:获取URL地址
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public static String getUrl() {
		StringBuffer buffer = new StringBuffer(HOSTURL_BASE);
		Random random = new Random(new Date().getTime()); //随机数
		buffer.append("funid=3&rd=" + random.nextLong());
		random = null;
		return buffer.toString();
	}

	/**
	 * <br>功能简述:设置请求参数
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @return
	 */
	public static JSONObject getRequestUrlJson(Context context) {
		JSONObject request = new JSONObject();
		JSONObject pheadJson = setPheadJson(context);

		try {
			request.put("phead", pheadJson);
			request.put("types", TYPE_URL); //1:go桌面用户使用手册
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return request;
	}

	/**
	 * <br>功能简述:设置参数Phead信息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @return
	 */
	private static JSONObject setPheadJson(Context context) {
		JSONObject pheadJson = new JSONObject();
		if (context != null) {
			String imei = GoStorePhoneStateUtil.getVirtualIMEI(context);
			try {
				pheadJson.put("vps", HttpUtil.getVps(context, imei)); //设备信息 vps
				pheadJson.put("launcherid", imei); //桌面id
				pheadJson.put("channel", GoStorePhoneStateUtil.getUid(context)); // 渠道号

				// lang 带上区域信息，如zh_cn,en_us
				String languageString = GoHandBookUtils.getLanguage(context);
				String countryString = GoHandBookUtils.getCountry(context);
				String language = String.format("%s_%s", languageString, countryString);
				//Log.i("lch", "language111：" + language);
				pheadJson.put("lang", language); //语言
				pheadJson.put("local", countryString); //区域(国家)
				pheadJson.put("pversion", MessageHttp.PVERSION); //协议版本

				String curVersion = context.getString(R.string.curVersion);
				pheadJson.put("cversion", curVersion); //桌面的版本号, String例如：3.16
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return pheadJson;
	}

	/**
	 * <br>功能简述:解析请求数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param response
	 * @return
	 */
	public static String responseJson(IResponse response) {

		if (response != null && response.getResponse() != null
				&& (response.getResponse() instanceof String)) {
			try {
				//Log.e("lch", "返回数据：" + response.getResponse().toString());
				JSONObject json = new JSONObject(response.getResponse().toString());
				JSONObject result = json.getJSONObject(MessageListBean.TAG_RESULT);
				int status = result.getInt(MessageListBean.TAG_STATUS);
				if (status == ConstValue.STATTUS_OK) {
					JSONObject surls = json.getJSONObject("surls");
					String urlString = surls.optString(TYPE_URL, "");
					return urlString;
				} else {
					return null;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	/**
	 * <br>功能简述:发送消息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param handler
	 * @param what
	 * @param urlString
	 */
	public static void sendHandlerMsg(Handler handler, int what, String urlString) {
		Message msg = new Message();
		msg.what = what;
		msg.obj = urlString;
		handler.sendMessage(msg);
	}

	/**
	 * 获取当前语言（桌面自带/系统）
	 * 
	 * @return
	 */
	public static String getLanguage(Context context) {
		Locale locale = null;
		if (DeskResourcesConfiguration.createInstance(context) != null) {
			locale = DeskResourcesConfiguration.getInstance().getmLocale();
		}
		//如果获取桌面的local为空。则获取系统的local
		if (locale == null) {
			locale = Locale.getDefault();
		}
		return locale.getLanguage().toLowerCase();
	}

	/**
	 * 获取国家地区,首先根据SIM卡获取区域
	 * 
	 * @return
	 */
	public static String getCountry(Context context) {

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
					ret = ret.toLowerCase();
				}
			}
		} catch (Throwable e) {
			// e.printStackTrace();
		}
		if (ret == null || ret.equals("")) {
			ret = Locale.getDefault().getCountry().toLowerCase();
		}
		return null == ret ? "error" : ret;
	}
}
