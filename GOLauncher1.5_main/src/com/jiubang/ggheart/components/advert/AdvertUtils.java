package com.jiubang.ggheart.components.advert;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.gau.go.launcherex.R;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.INetRecord;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.AppUtils;
import com.go.util.device.Machine;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.ConstValue;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.HttpUtil;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.MessageHttp;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.SimpleHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.components.gohandbook.SharedPreferencesUtil;
import com.jiubang.ggheart.components.gohandbook.StringOperator;
import com.jiubang.ggheart.data.info.ShortCutInfo;

/**
 * 
 * <br>类描述:15屏广告请求工具类
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-9-5]
 */
public class AdvertUtils {

	/**
	 * <br>功能简述:请求接口获取indexUrl
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param handler
	 */
	public static void getAdvertData(final Context context, final Handler handler) {
		if (context == null || handler == null) {
			return;
		}

		String url = AdvertConstants.getUrl("1"); //获取URL地址

		JSONObject requestJson = getRequestUrlJson(context);
		log("地址url：" + url);
		log("请求参数1：" + requestJson.toString());
		try {
			THttpRequest request = new THttpRequest(url, requestJson.toString().getBytes(),
					new IConnectListener() {
						@Override
						public void onStart(THttpRequest arg0) {
							//Log.e("lch", "onStart");
						}

						@Override
						public void onFinish(THttpRequest request, IResponse response) {
							//解析请求数据
							ArrayList<AdvertInfo> advertInfosList = responseJson(context, response);
							
//							log("返回图标个数:" + advertInfosList.size());
							
							if (advertInfosList != null && advertInfosList.size() > 0) {
								sendHandlerMsg(handler, AdvertConstants.GET_ADVERT_DATA_SUCCESS,
										advertInfosList);
							} else {
								sendHandlerMsg(handler, AdvertConstants.GET_ADVERT_DATA_FAIL, null);
							}
						}

						@Override
						public void onException(THttpRequest arg0, int arg1) {
							//Log.e("lch", "onException = " + arg1);
							sendHandlerMsg(handler, AdvertConstants.GET_ADVERT_DATA_FAIL, null);
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
			sendHandlerMsg(handler, AdvertConstants.GET_ADVERT_DATA_FAIL, null);
			return;
		}
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
		JSONObject pheadJson = getPheadJson(context);
		long ltsString = getLtsString(context);
		try {
			request.put("phead", pheadJson);
			request.put("lts", ltsString); //上次获取消息时服务器下发的lts值
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
	private static JSONObject getPheadJson(Context context) {
		JSONObject pheadJson = new JSONObject();
		if (context != null) {
			String imei = GoStorePhoneStateUtil.getVirtualIMEI(context);
			try {
				pheadJson.put("vps", HttpUtil.getVps(context, imei)); //设备信息 vps
				pheadJson.put("launcherid", imei); //桌面id
				pheadJson.put("channel", GoStorePhoneStateUtil.getUid(context)); // 渠道号
				Locale locale = Locale.getDefault();
				String language = String.format("%s_%s", locale.getLanguage().toLowerCase(), locale
						.getCountry().toLowerCase());
				pheadJson.put("lang", language);
				pheadJson.put("local", locale.getCountry().toLowerCase());

				pheadJson.put("pversion", MessageHttp.PVERSION); //协议版本

				String curVersion = context.getString(R.string.curVersion);
				pheadJson.put("cversion", curVersion); //桌面的版本号, String例如：3.16
				pheadJson.put("sdklevel", getAndroidSDKVersion()); //sdklevel
				pheadJson.put("androidid", Machine.getAndroidId()); //androidid

				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return pheadJson;
	}

	/**
	 * <br>功能简述:获取SDK版本号
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	 public static int getAndroidSDKVersion() {
	        int version = 0;
	        try {
	            version = Integer.valueOf(android.os.Build.VERSION.SDK);
	        } catch (NumberFormatException e) {
	        	
	        }
	        return version;
	    }
	
	/**
	 * <br>功能简述:上次获取消息时服务器下发的lts值
	 * <br>功能详细描述:第一次获取消息该值传0
	 * <br>注意:
	 * @return
	 */
	private static long getLtsString(Context context) {
		SharedPreferencesUtil preferencesUtil = new SharedPreferencesUtil(context);
		String ltsString = preferencesUtil.getString(AdvertConstants.LTS_REQUEST_TIME, "0");
		try {
			long lts = Long.parseLong(ltsString);
			return lts;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * <br>功能简述:保存上次获取消息时服务器下发的lts值
	 * <br>功能详细描述:第一次获取消息该值传0
	 * <br>注意:
	 * @return
	 */
	private static void saveLtsString(Context context, String ltsString) {
		if (context != null && ltsString != null) {
			SharedPreferencesUtil preferencesUtil = new SharedPreferencesUtil(context);
			preferencesUtil.saveString(AdvertConstants.LTS_REQUEST_TIME, "0");
		}
	}

	/**
	 * <br>功能简述:解析请求数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param response
	 * @return
	 */
	public static ArrayList<AdvertInfo> responseJson(Context context, IResponse response) {
		if (response != null && response.getResponse() != null
				&& (response.getResponse() instanceof String)) {
			try {
				String responseString = response.getResponse().toString();
				log("返回数据：" + responseString);
				JSONObject json = new JSONObject(responseString);
				JSONObject result = json.getJSONObject(MessageListBean.TAG_RESULT);
				int status = result.getInt(MessageListBean.TAG_STATUS);

				//请求成功
				if (status == ConstValue.STATTUS_OK) {
					//保存请求时间
					String ltsString = String.valueOf(json.optLong("lts"));
					saveLtsString(context, ltsString);

					JSONArray msgsArray = json.getJSONArray("msgs");
					ArrayList<AdvertInfo> advertInfosList = getAdvrtArrary(context, msgsArray, true);
					return advertInfosList;
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param msgsArray
	 * @param isFile 是否需要判断是否文件夹
	 * @return
	 */
	public static ArrayList<AdvertInfo> getAdvrtArrary(Context context, JSONArray msgsArray,
			boolean isFile) {
		if (msgsArray == null) {
			return null;
		}
		ArrayList<AdvertInfo> advertInfosList = new ArrayList<AdvertInfo>();
		try {
			int msgsSize = msgsArray.length();
			for (int i = 0; i < msgsSize; i++) {
				AdvertInfo advertInfo = new AdvertInfo();
				JSONObject msgJsonObject = msgsArray.getJSONObject(i);
				advertInfo.mId = msgJsonObject.optString("id");
				advertInfo.mTitle = msgJsonObject.optString("title");
				advertInfo.mPackageName = msgJsonObject.optString("packagename");
				advertInfo.mIcon = msgJsonObject.optString("icon");
				advertInfo.mActtype = msgJsonObject.optInt("acttype");
				advertInfo.mActvalue = msgJsonObject.optString("actvalue");
				int screen = msgJsonObject.optInt("screen", 0);

				if (screen > 0) {
					advertInfo.mScreen = screen - 1;	//后台返回的是第1屏和5.桌面要对应-1
				} else {
					advertInfo.mScreen = 0;
				}
				advertInfo.mPos = msgJsonObject.optInt("pos", -1);
				advertInfo.mStartTime = msgJsonObject.optString("stime_start");
				advertInfo.mEndTime = msgJsonObject.optString("stime_end");
				advertInfo.mIsfile = msgJsonObject.optInt("isfile", 0);
				advertInfo.mIscarousel = msgJsonObject.optInt("iscarousel", 0);
				advertInfo.mClickurl = msgJsonObject.optString("clickurl");
				advertInfo.mMapid = msgJsonObject.optString("mapid");

				//判断是否文件夹
				if (advertInfo.mIsfile == 1 && isFile) {
					JSONArray fileArray = msgJsonObject.getJSONArray("filemsg");
					advertInfo.mFilemsg = getAdvrtArrary(context, fileArray, false);
				}

				String packName = advertInfo.mPackageName;
				boolean isAppExist = AppUtils.isAppExist(context, packName);
				//如果该包名不存在才需要判断，如果存在就不需要插入了
				if (!isAppExist && 1 <= advertInfo.mPos && advertInfo.mPos <= 16) {
					advertInfosList.add(advertInfo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return advertInfosList;
	}

	/**
	 * <br>功能简述:发送消息
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param handler
	 * @param what
	 * @param urlString
	 */
	public static void sendHandlerMsg(Handler handler, int what, Object urlString) {
		if (handler != null) {
			Message msg = new Message();
			msg.what = what;
			msg.obj = urlString;
			handler.sendMessage(msg);
		}
	}

	public static void getNetImageData(final Context context, AdvertInfo advertInfo,
			final Handler handler) {
		if (context == null || advertInfo == null || advertInfo.mIcon == null || handler == null) {
			sendHandlerMsg(handler, AdvertConstants.DOWN_IMAGE_FAIL, null);
			return;
		}
		String url = advertInfo.mIcon;
		log("地址url：" + url);
		try {
			THttpRequest request = new THttpRequest(url, null, new IConnectListener() {
				@Override
				public void onStart(THttpRequest arg0) {

				}

				@Override
				public void onFinish(THttpRequest request, IResponse response) {
					if (response != null && response.getResponse() != null
							&& (response.getResponse() instanceof Integer)) {
						int state = (Integer) response.getResponse();
						if (state == AdvertConstants.DOWN_IMAGE_SUCCESS) {
							sendHandlerMsg(handler, AdvertConstants.DOWN_IMAGE_SUCCESS, null);
						} else {
							sendHandlerMsg(handler, AdvertConstants.DOWN_IMAGE_FAIL, null);
						}
					} else {
						sendHandlerMsg(handler, AdvertConstants.DOWN_IMAGE_FAIL, null);
					}
				}

				@Override
				public void onException(THttpRequest arg0, int arg1) {
					sendHandlerMsg(handler, AdvertConstants.DOWN_IMAGE_FAIL, null);
				}
			});

			request.setOperator(new NetImageOperator(advertInfo)); //设置返回数据类型-字符串
			SimpleHttpAdapter httpAdapter = SimpleHttpAdapter.getInstance(context);
			httpAdapter.addTask(request);

		} catch (Exception e) {
			e.printStackTrace();
			sendHandlerMsg(handler, AdvertConstants.DOWN_IMAGE_FAIL, null);
			return;
		}
	}

	//=====================数据统计

	/**
	 * <br>功能简述:请求接口获取indexUrl
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param handler
	 */
	public static void requestAdvertStatistics(final Context context, final Handler handler, String clickUrl) {
		if (context == null) {
			sendHandlerMsg(handler, AdvertConstants.STATISTICS_REQUEST_FAIL, null);
			return;
		}

		String url = AdvertConstants.getUrl("2"); //获取URL地址

		JSONObject requestJson = getRequestStatisticsJson(context, clickUrl);
		log("地址url：" + url);
		log("请求参数：" + requestJson.toString());
		try {
			THttpRequest request = new THttpRequest(url, requestJson.toString().getBytes(),
					new IConnectListener() {
						@Override
						public void onStart(THttpRequest arg0) {

						}

						@Override
						public void onFinish(THttpRequest request, IResponse response) {
							boolean isOk = responseStatistics(context, response);
							if (isOk) {
								sendHandlerMsg(handler, AdvertConstants.STATISTICS_REQUEST_SUCCESS,
										null);
							} else {
								sendHandlerMsg(handler, AdvertConstants.STATISTICS_REQUEST_FAIL,
										null);
							}
						}

						@Override
						public void onException(THttpRequest arg0, int arg1) {
							sendHandlerMsg(handler, AdvertConstants.STATISTICS_REQUEST_FAIL, null);
						}
					});

			request.setOperator(new StringOperator()); //设置返回数据类型-字符串
			SimpleHttpAdapter httpAdapter = SimpleHttpAdapter.getInstance(context);
			httpAdapter.addTask(request);
		} catch (Exception e) {
			e.printStackTrace();
			sendHandlerMsg(handler, AdvertConstants.STATISTICS_REQUEST_FAIL, null);
			return;
		}
	}

	/**
	 * <br>功能简述:设置请求参数
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @return
	 */
	public static JSONObject getRequestStatisticsJson(Context context, String clickUrl) {
		JSONObject request = new JSONObject();
		JSONObject pheadJson = getPheadJson(context);
		String statString = getStatString(context);
		try {
			request.put("phead", pheadJson);
			request.put("stat", statString); //上次获取消息时服务器下发的lts值
			if (clickUrl != null) {
				request.put("clickurl", clickUrl);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return request;
	}

	/**
	 * 获取GO桌面桌面安装统计
	 * 
	 * @return
	 */
	private static String getStatString(Context context) {
		try {
			StringBuffer stringBuffer = new StringBuffer();
			if (context != null) {
				Map<String, ?> data = getAllAppData(context);
				if (data != null) {
					Set<String> keys = data.keySet();
					for (String key : keys) {
						StringBuffer oneBuffer = new StringBuffer();

						Object obj = data.get(key);
						String reason = null;
						if (obj != null && obj instanceof String) {
							reason = (String) obj;
						}

						String[] item = null;
						if (reason != null && !reason.equals("")) {
							item = reason.split(";");
						}
//						log("reasonString:" + reason);
//						log("getStatString item.length:" + item.length);
						if (item != null && item.length == 4) {
							String messageId = item[0];	//消息ID
							String clickCount = item[1];	//点击数量
							String installCount = item[2]; //安装数量
							String mapIdString = item[3]; //统计id
							long time = System.currentTimeMillis();

							//判断点击量和安装量是否为0
							if (!(clickCount.equals("0") && installCount.equals("0"))) {
								oneBuffer.append(time).append("#");			//日志记录的时间戳
								oneBuffer.append(messageId).append("#");	//消息id
								oneBuffer.append(clickCount).append("#"); 	//点击量
								oneBuffer.append(installCount).append("#"); //安装量
								oneBuffer.append(time + 1).append("#"); 	//上传id
								oneBuffer.append(mapIdString);  //统计id
							}
						}
						if (!oneBuffer.toString().equals("")) {
							stringBuffer.append(oneBuffer).append("&");
						}
					}
				}
			}
			return stringBuffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	// 获取桌面应用点击及安装统计全部数据
	public static Map<String, ?> getAllAppData(Context context) {
		PreferencesManager sp = new PreferencesManager(context, IPreferencesIds.ADVERT_SCREEN_DATA,
				Context.MODE_PRIVATE);

		return sp.getAll();
	}

	/**
	 * <br>功能简述:解析统计返回数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param response
	 * @return
	 */
	public static boolean responseStatistics(Context context, IResponse response) {
		//解析请求数据
		if (response != null && response.getResponse() != null
				&& (response.getResponse() instanceof String)) {
			try {
				String responseString = response.getResponse().toString();
				log("返回数据：" + responseString);
				JSONObject json = new JSONObject(responseString);
				String isok = json.optString("isok");
				if (isok.equals("1")) {
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * <br>功能简述:保存15屏幕推荐图标包名列表
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param packageName
	 */
	public static void setInstallListCache(Context context, ShortCutInfo info) {
		Intent intent = ((ShortCutInfo) info).mIntent;
		if (intent != null
				&& intent.getComponent() != null
				&& intent.getComponent().getPackageName() != null
				&& !intent.getComponent().getPackageName().equals("")) {
			String packageName = intent.getComponent().getPackageName();
			SharedPreferencesUtil preferencesUtil = new SharedPreferencesUtil(context);
			String cacheString = preferencesUtil.getString(AdvertConstants.ADVERT_PACKAGE_NAME_LIST, "");
			cacheString = cacheString + packageName + ";";
			preferencesUtil.saveString(AdvertConstants.ADVERT_PACKAGE_NAME_LIST, cacheString);
			log("cacheString:" + cacheString);
		}
	}
	
	/**
	 * <br>功能简述:清除安装列表缓存
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param packageName
	 */
	public static void clearInstallListCache(Context context) {
		log("清空安装列表缓存！");
		SharedPreferencesUtil preferencesUtil = new SharedPreferencesUtil(context);
		preferencesUtil.saveString(AdvertConstants.ADVERT_PACKAGE_NAME_LIST, "");
	}
	
	/**
	 * <br>功能简述:判断是否15屏幕广告推荐的图标
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean checkInAdvertList(Context context, String packageName) {
		if (packageName == null || packageName.equals("")) {
			return false;
		}
		
		SharedPreferencesUtil preferencesUtil = new SharedPreferencesUtil(context);
		String cacheString = preferencesUtil.getString(AdvertConstants.ADVERT_PACKAGE_NAME_LIST, "");
		if (cacheString.equals("")) {
			return false;
		}
		
		if (cacheString.contains(packageName)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void log(String content) {
//		Log.i("lch2", content);
	}
}
