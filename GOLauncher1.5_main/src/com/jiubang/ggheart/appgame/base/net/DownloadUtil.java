/**
 * 
 */
package com.jiubang.ggheart.appgame.base.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.appcenter.help.AppsNetConstant;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppFileUtil;
import com.jiubang.ggheart.appgame.appcenter.help.RecommAppsUtils;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.data.AppGameNetLogControll;
import com.jiubang.ggheart.appgame.base.data.AppsDetailDownload;
import com.jiubang.ggheart.appgame.base.data.ClassificationExceptionRecord;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.ConstValue;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.billing.base.Consts;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 应用中心/游戏中心下载应用数据的工具类
 * 
 * @author liguoliang
 * 
 */
//CHECKSTYLE:OFF
public class DownloadUtil {

	/**
	 * 随机数对象，用于生成请求URL后面的随机数
	 */
	public static final Random sRandom = new Random(System.currentTimeMillis());

	/**
	 * 获取应用中心域名
	 * 
	 * @return
	 */
	public static String getAppCenterHost(Context context) {
		String requesUrl = AppsNetConstant.APP_CENTER_URL_CHINA;
		if (!GoStorePhoneStateUtil.isCnUser(context)) {
			requesUrl = AppsNetConstant.APP_CENTER_URL_OTHERS;
		}
		return requesUrl;
	}

	/**
	 * 获取备选应用中心域名
	 */
	public static String getAlternativeAppCenterHost(Context context) {
		String requesUrl = AppsNetConstant.APP_CENTER_URL_OTHERS;
		if (!GoStorePhoneStateUtil.isCnUser(context)) {
			requesUrl = AppsNetConstant.APP_CENTER_URL_CHINA;
		}
		return requesUrl;
	}

	/**
	 * 向服务器请求数据，返回数据流
	 * 
	 * @param headerInfo
	 * @param url
	 * @return
	 */
	@Deprecated
	public static InputStream requestData(String headerInfo, String url) {
		// 记录网络请求的url
		AppGameNetLogControll.getInstance().setUrl(
				AppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE, url);

		StringEntity se;
		InputStream inputStream = null;
		long time = System.currentTimeMillis();
		try {
			// TODO:XIEDEZHI 设置网络超时
			se = new StringEntity(headerInfo, "UTF-8");
			HttpPost hp = new HttpPost(url);
			hp.setEntity(se);
			HttpResponse httpResponse = new DefaultHttpClient().execute(hp);
			HttpEntity httpEntity = httpResponse.getEntity();
			if (httpEntity != null) {
				inputStream = httpEntity.getContent();
			}
			// 记录网络连接时间
			AppGameNetLogControll.getInstance().setConnectionTime(
					AppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE,
					System.currentTimeMillis() - time);
		} catch (Exception e) {
			e.printStackTrace();
			ClassificationExceptionRecord.getInstance().record(e);
			// 记录网络连接时间
			AppGameNetLogControll.getInstance().setConnectionTime(
					AppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE,
					System.currentTimeMillis() - time);
			// 记录异常信息，同时保存网络信息
			AppGameNetLogControll.getInstance().setExceptionCode(
					AppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE, e);
		}
		return inputStream;
	}

	/**
	 * 木瓜移动，回调url
	 * @param url
	 */
	public static void sendCBackUrl(final String url) {
		if (url == null || "".equals(url)) {
			return;
		}
		//本地没有网络时，就不发送
		if (!Machine.isNetworkOK(GOLauncherApp.getApplication())) {
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				//time 为当前时间，减去时间戳，为了和服务器的时间一致  
				int count = 5;
				Context context = GOLauncherApp.getApplication();
				while (count > 0) {
					String backUrl = url + "&ts=" + getSerTime(context);
					if (!requestData(context, backUrl)) {
						try {
							Thread.sleep(10 * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						count--;
					} else {
						break;
					}
				}
			}
		}).start();

	}
	
	/**
	 * 如果是木瓜移动的应用，需要回调url
	 * @param type  点击下载或进入详情
	 * @param cback 是否回调
	 * @param cbacktype 回调的类型，详情或下载
	 * @param url   回调的url
	 */
	public static void sendCBackUrl(int type, int cback, int cbacktype, String url) {
		if (url == null || "".equals(url)) {
			return;
		}
		if (cback == BoutiqueApp.BoutiqueAppInfo.NEED_TO_CBACK) {
			if ((cbacktype & type) == type) {
				sendCBackUrl(url);
			}
		}
	}

	/**
	 * 获取本地当前时间，和服务器匹配
	 * @param context
	 * @return 返回服务器当前时间
	 */
	public static long getSerTime(Context context) {
		String serTimeStr = getMark(context, AppsDetailDownload.KEY_SEVERTIME_MARK);
		String localTimeStr = getMark(context, AppsDetailDownload.KEY_LOCALTIME_MARK);
		if ("".equals(serTimeStr) || "".equals(localTimeStr)) {
			return System.currentTimeMillis() / 1000;
		} else {
			long serviceTime = Long.valueOf(serTimeStr);
			long localTime = Long.valueOf(localTimeStr);
			long currTime = System.currentTimeMillis() / 1000 - localTime + serviceTime;
			if (Consts.DEBUG) {
				Log.d(Consts.TAG, "serviceTime:" + serviceTime + ",localTime:" + localTime);
			}
			return currTime;
		}
	}

	/**
	 * 向服务器请求数据，返回数据流
	 * 
	 * @param headerInfo
	 * @param url
	 * @return
	 */
	public static void sendDataByPost(String url, String statisticsData) {
		if (url == null || statisticsData == null) {
			return;
		}
		StringEntity se;
		try {
			// TODO 设置网络超时
			se = new StringEntity(statisticsData, "UTF-8");
			HttpPost hp = new HttpPost(url);
			hp.setEntity(se);
			HttpResponse httpResponse = new DefaultHttpClient().execute(hp);
			HttpEntity httpEntity = httpResponse.getEntity();
			if (httpEntity != null) {
				httpEntity.getContent();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 保存服务器下发的时间
	 * @param context
	 * @param severtime
	 */
	public static void saveSerTime(Context context, long severtime) {

		if (severtime == -1) {
			severtime = System.currentTimeMillis() / 1000;
		}
		DownloadUtil.saveNetLog(context, AppsDetailDownload.KEY_SEVERTIME_MARK,
				String.valueOf(severtime));
		DownloadUtil.saveNetLog(context, AppsDetailDownload.KEY_LOCALTIME_MARK,
				String.valueOf(System.currentTimeMillis() / 1000));
		if (Consts.DEBUG) {
			Log.e(Consts.TAG, "receiver severtime: " + severtime);
		}
	}
	/**
	 * 木瓜移动的应用，向服务器回调url
	 * @param context
	 * @param url
	 * @return
	 */
	private static boolean requestData(Context context, String url) {
		if (Machine.isNetworkOK(context)) {
			// 创建HttpGet实例
			HttpGet httpGet = new HttpGet(url);
			InputStream input = null;
			try {
				HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
				HttpEntity httpEntity = httpResponse.getEntity();
				if (httpEntity != null) {
					input = httpEntity.getContent();
					byte[] data = new byte[1];
					data[0] = (byte) input.read();
					String resultStr = new String(data);
					if ("1".equals(resultStr)) {
						//成功
						return true;
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
				return true;
			}
			finally {
				try {
					if (input != null) {
						input.close();
						input = null;
					}

				} catch (IOException e) {

				}
			}
		}
		return false;
	}

	//	/**
	//	 * httpGet请求，只向服务器发送请求，不接收返回的数据
	//	 * 
	//	 * @param url
	//	 */
	//	public static void requestData(String url) {
	//		if (url == null || "".equals(url)) {
	//			return;
	//		}
	//		// 创建HttpGet实例
	//		HttpGet httpGet = new HttpGet(url);
	//		InputStream input = null;
	//		try {
	//			HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
	//			HttpEntity httpEntity = httpResponse.getEntity();
	//			if (httpEntity != null) {
	//				input = httpEntity.getContent();
	//				byte[] data = new byte[1];
	//				data[0] = ( byte ) input.read();;
	//				String resultStr = new String(data);
	//				if ("1".equals(resultStr)) {
	//					//成功
	//				}
	//			}
	//		} catch (ClientProtocolException e) {
	//			e.printStackTrace();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		} finally {
	//			try {
	//				if (input != null) {
	//					input.close();
	//					input = null;
	//				}
	//
	//			} catch (IOException e) {
	//
	//			}
	//
	//		}
	//	}

	public static void saveMark(Context context, String key, String mark) {
		// SharedPreferences sharedPreferences = context.getSharedPreferences(
		// LauncherEnv.APP_MANAGER_RECOMMEND, Context.MODE_PRIVATE);
		// SharedPreferences.Editor editor = sharedPreferences.edit();
		// editor.putString(key, mark);
		// editor.commit();
		setSharePreferences(context, key, mark);
	}

	public static String getMark(Context context, String key) {
		// SharedPreferences sharedPreferences = context.getSharedPreferences(
		// LauncherEnv.APP_MANAGER_RECOMMEND, Context.MODE_PRIVATE);
		// return sharedPreferences.getString(key, "");
		return getSharePreferences(context, key);
	}

	public static void saveNetLog(Context context, String key, String mark) {
		if (context == null) {
			return;
		}
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				IPreferencesIds.APP_MANAGER_RECOMMEND, Context.MODE_PRIVATE);
		sharedPreferences.putString(key, mark);
		sharedPreferences.commit();
	}

	public static String getNetLog(Context context, String key) {
		if (context == null) {
			return "";
		}
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				IPreferencesIds.APP_MANAGER_RECOMMEND, Context.MODE_PRIVATE);
		return sharedPreferences.getString(key, "");
	}

	/**
	 * 保存已经点击过的有特效的appid
	 */
	public static void saveViewedEffectApp(Context context, String appid) {
		if (context == null) {
			return;
		}
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				IPreferencesIds.APP_MANAGER_RECOMMEND, Context.MODE_PRIVATE);
		sharedPreferences.putBoolean(AppsNetConstant.APP_EFFECT_MARK_PREFIX + appid, true);
		sharedPreferences.commit();
	}

	/**
	 * 检查应用是否已经被点击过
	 */
	public static boolean checkViewedEffectApp(Context context, String appid) {
		//TODO:XIEDEZHI 出现过几次滑动列表时报错，不知道是不是这里操作sharedPreferences导致的
		if (context == null) {
			return false;
		}
		PreferencesManager sharedPreferences = new PreferencesManager(context,
				IPreferencesIds.APP_MANAGER_RECOMMEND, Context.MODE_PRIVATE);
		return sharedPreferences.getBoolean(AppsNetConstant.APP_EFFECT_MARK_PREFIX + appid, false);
	}

	private static void setSharePreferences(Context context, String key, String value) {
		if (context != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					IPreferencesIds.APP_MANAGER_RECOMMEND, Context.MODE_PRIVATE);
			sharedPreferences.putString(key, value);
			sharedPreferences.commit();
		}
	}

	private static String getSharePreferences(Context context, String key) {
		if (context != null) {
			PreferencesManager sharedPreferences = new PreferencesManager(context,
					IPreferencesIds.APP_MANAGER_RECOMMEND, Context.MODE_PRIVATE);
			return sharedPreferences.getString(key, "");
		}
		return "";
	}

	/**
	 * 生成向服务器传递的参数信息
	 * 
	 * @param context
	 * @param phead
	 * @param must
	 *            如果本地没有数据，传递1，否则传递0
	 * @param mark
	 *            上一次请求，服务器返回的值
	 * @param pversion
	 *            向服务器请求的协议版本号
	 * @return
	 */
	// public static JSONObject getPostJson(Context context, int must,
	// String mark, String pversion) {
	// JSONObject request = new JSONObject();
	// JSONObject phead = RecommAppsUtils.createHttpHeader(context, pversion);
	// try {
	// request.put("phead", phead);
	// request.put("must", must);
	// request.put("mark", mark);
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// return request;
	// }

	public static JSONObject parseMsgListStreamData(final InputStream in, final boolean isZipData) {
		if (in == null) {
			return null;
		}
		try {
			String jsonString = null;
			if (isZipData) {
				jsonString = RecommAppFileUtil.unzipDataAndLog(in);
			} else {
				long time = System.currentTimeMillis();
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				byte[] buff = new byte[1024];
				int len = -1;
				try {
					while ((len = in.read(buff)) != -1) {
						buffer.write(buff, 0, len);
					}
				} catch (IOException e) {
					e.printStackTrace();
					ClassificationExceptionRecord.getInstance().record(e);
					// 记录异常信息，同时保存网络信息
					AppGameNetLogControll.getInstance().setExceptionCode(
							AppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE, e);
					return null;
				}

				byte[] data = buffer.toByteArray();

				// 统计下载速度 old_bytes/time2
				long time2 = System.currentTimeMillis() - time;
				if (time2 > 0) {
					String speed = String.valueOf(data.length / time2);
					AppGameNetLogControll.getInstance().setDownloadSpeed(
							AppGameNetLogControll.DEFAULT_CURRENT_THREAD_CODE, speed);
				}

				jsonString = new String(data);

			}
			if (jsonString != null) {
				// Log.d(TAG, "list:" + jsonString);
				JSONObject jsonObject = new JSONObject(jsonString);
				JSONObject result = jsonObject.getJSONObject(MessageListBean.TAG_RESULT);
				int status = result.getInt(MessageListBean.TAG_STATUS);
				if (status == ConstValue.STATTUS_OK) {
					// 解析数据
					return jsonObject;
				} else {
					Log.i("DownloadUtil", "parseMsgListStreamData result = " + result);
					// 记录错误信息
					ClassificationExceptionRecord.getInstance().record(
							"服务器数据异常：" + result.toString());
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			ClassificationExceptionRecord.getInstance().record(e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		return null;
	}

	/**
	 * 生成向服务器传递的参数信息
	 * 
	 * @param context
	 * @param pversion
	 *            版本号
	 * @param musts
	 *            must列表，与分类id列表对应。如果本地没有数据，传递1，否则传递0
	 * @param marks
	 *            mark列表，与分类id列表对应。
	 * @param typeIds
	 *            分类id列表
	 * @param access
	 *            入口(仅在获取顶级tab栏数据时传) 1:快捷方式进入2:menu进入
	 * @param pageId
	 *            获取的页码，页码从1开始(获取专题推荐列表用到)
	 * @param itp 
	 *            id类型  0:与渠道区域相关的分类节点(默认)  1:与渠道区域无关的分类id  2:人工配置挂接用的虚拟id    
	 * @return
	 * 
	 * @author xiedezhi
	 */
	public static JSONObject getPostJson(Context context, String pversion, int[] musts,
			String[] marks, int[] typeIds, int access, int pageId, int itp) {
		JSONObject request = new JSONObject();

		if (musts == null || marks == null || typeIds == null) {
			return request;
		}
		try {
			int count = typeIds.length;
			JSONArray typeArray = new JSONArray();
			JSONObject typeObj = null;
			for (int i = 0; i < count; i++) {
				typeObj = new JSONObject();

				typeObj.put("typeid", typeIds[i]);
				typeObj.put("itp", itp);
				typeObj.put("must", musts[i]);
				typeObj.put("mark", marks[i]);
				typeObj.put("pageid", pageId);
				typeObj.put("access", access);

				typeArray.put(typeObj);
			}
			JSONObject phead = RecommAppsUtils.createHttpHeader(context, pversion);
			request.put("phead", phead);
			request.put("reqs", typeArray);
		} catch (JSONException e) {
			e.printStackTrace();
			ClassificationExceptionRecord.getInstance().record(e);
		}
		return request;
	}

	/**
	 * 应用中心/游戏中心根据vps定义规范，获取本机vps信息
	 * 
	 * @param context
	 * @param imei
	 * @return vps字符串
	 */
	public static String getVps(Context context, String imei) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager wMgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		wMgr.getDefaultDisplay().getMetrics(dm);
		// int width = (int) (dm.widthPixels * dm.density);
		// int height = (int) (dm.heightPixels * dm.density);
		int width = dm.widthPixels;
		int height = dm.heightPixels;

		StringBuilder vpsStringBuilder = new StringBuilder(64);
		vpsStringBuilder.append("1#");
		vpsStringBuilder.append("Android#");
		vpsStringBuilder.append(Build.MODEL + "#");
		vpsStringBuilder.append(imei + "#");
		vpsStringBuilder.append("166#");
		vpsStringBuilder.append(width + "_" + height + "#");
		vpsStringBuilder.append("01.01.00");
		String vps = vpsStringBuilder.toString();
		try {
			vps = URLEncoder.encode(vps, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return vps;
	}
	
	/**
	 * 获取url的主机名
	 */
	public static String getHost(String url) {
		if (url != null) {
			try {
				URI uri = new URI(url);
				if (uri == null) {
					return null;
				}
				return uri.getHost();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
