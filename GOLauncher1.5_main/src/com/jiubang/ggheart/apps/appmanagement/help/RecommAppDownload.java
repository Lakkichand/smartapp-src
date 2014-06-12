package com.jiubang.ggheart.apps.appmanagement.help;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.jiubang.ggheart.apps.appmanagement.bean.RecommendedApp;
import com.jiubang.ggheart.apps.appmanagement.bean.RecommendedAppCategory;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.ConstValue;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreAppInforUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
import com.jiubang.ggheart.launcher.LauncherEnv;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-9-27]
 */
public class RecommAppDownload {
	private static final String TAG = "RecommAppDownload";
	private HttpPost mHttpPost;
	private Context mContext;
	private static final String PVERSION = "1.0"; // 请求协议版本号
	// private static RecommAppDownload instance;

	// 国内域名
	private static final String RECOMM_APP_URL_CHINA = "http://gorcmdapp.3g.net.cn";
	// 国外域名
	private static final String RECOMM_APP_URL_OTHERS = "http://gorcmdapp.goforandroid.com";
	private static final String URL_RECOMM_APP_LIST = "/recommendedapp/recommend.do?funid=1&rd=";

	// 缓存应用推荐信息的文件目录
	private static final String SAVE_APP_INFO_PATH = LauncherEnv.Path.APP_MANAGER_RECOMMEND_INFO_PATH
			+ "appInfo.txt";

	public RecommAppDownload(Context context) {
		mContext = context;
	}

	// public static synchronized RecommAppDownload getInstance(Context context)
	// {
	// if (instance == null) {
	// instance = new RecommAppDownload(context);
	// }
	// return instance;
	// }

	/**
	 * 向服务器请求推荐应用信息
	 * 
	 * @return
	 */
	public ArrayList<RecommendedAppCategory> requestData() {
		JSONObject obj = new JSONObject();
		JSONObject postdataJsonObject = getPostJson(mContext, obj, null);
		Random random = new Random(new Date().getTime());
		String requesUrl = RECOMM_APP_URL_CHINA;
		if (!GoStorePhoneStateUtil.isCnUser(mContext)) {
			requesUrl = RECOMM_APP_URL_OTHERS;
		}
		mHttpPost = new HttpPost(requesUrl + URL_RECOMM_APP_LIST + random.nextLong());
		// 绑定到请求 Entry
		StringEntity se;
		try {
			se = new StringEntity(postdataJsonObject.toString());
			mHttpPost.setEntity(se);
			HttpResponse httpResponse = new DefaultHttpClient().execute(mHttpPost);
			// Log.d(TAG, "requestData time :" + System.currentTimeMillis());
			return parseMsg(httpResponse.getEntity());

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 生成向服务器传递的参数信息
	 * 
	 * @param context
	 * @param phead
	 * @param msgId
	 * @return
	 */
	private JSONObject getPostJson(Context context, JSONObject phead, String msgId) {
		if (phead == null) {
			phead = new JSONObject();
		}
		JSONObject request = new JSONObject();
		compoundNameValuePairs(context, phead);
		try {
			request.put("phead", phead);

			// 如果本地没有数据，传递1，否则传递0
			int must = hasLocalData() ? 0 : 1;
			request.put("must", must);
			request.put("mark", getMark());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return request;
	}

	private void compoundNameValuePairs(Context context, JSONObject data) {
		if (context != null) {
			if (data == null) {
				data = new JSONObject();
			}
			String imei = GoStorePhoneStateUtil.getVirtualIMEI(context);
			try {
				data.put("launcherid", imei);
				data.put("imsi", getCnUser());
				data.put("hasmarket", GoStoreAppInforUtil.isExistGoogleMarket(context) ? 1 : 0);
				// lang 带上区域信息，如zh_cn,en_us
				Locale locale = Locale.getDefault();
				// String language = String.format("%s_%s", locale.getLanguage()
				// .toLowerCase(), locale.getCountry().toLowerCase());
				data.put("lang", language(context));
				data.put("local", locale.getCountry().toLowerCase());
				data.put("channel", GoStorePhoneStateUtil.getUid(context));
				data.put("sys", Build.MODEL);
				data.put("sdk", Build.VERSION.SDK_INT);
				data.put("dpi", RecommAppsUtils.getDisplay(mContext));
				data.put("pversion", PVERSION);

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private boolean hasLocalData() {
		File file = new File(SAVE_APP_INFO_PATH);
		if (file != null && file.exists()) {
			return true;
		}
		return false;
	}

	private void saveMark(String mark) {
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				IPreferencesIds.APP_MANAGER_RECOMMEND, Context.MODE_PRIVATE);
		sharedPreferences.putString(IPreferencesIds.APP_MANAGER_RECOMMEND_MARK, mark);
		sharedPreferences.commit();
	}

	private String getMark() {
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				IPreferencesIds.APP_MANAGER_RECOMMEND, Context.MODE_PRIVATE);
		return sharedPreferences.getString(IPreferencesIds.APP_MANAGER_RECOMMEND_MARK, "");
	}

	private JSONObject parseMsgListStreamData(final InputStream in) {
		try {
			String jsonString = RecommAppFileUtil.zipData(in);
			if (jsonString != null) {
				// Log.d(TAG, "list:" + jsonString);
				JSONObject jsonObject = new JSONObject(jsonString);
				JSONObject result = jsonObject.getJSONObject(MessageListBean.TAG_RESULT);
				int status = result.getInt(MessageListBean.TAG_STATUS);
				if (status == ConstValue.STATTUS_OK) {
					// 解析数据
					return jsonObject;
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	 * 解析返回的数据
	 * 
	 * @param obj
	 * @param msgContent
	 * @return
	 */
	private ArrayList<RecommendedAppCategory> parseMsg(final HttpEntity entity) {
		try {
			JSONObject obj = parseMsgListStreamData(entity.getContent());
			if (obj != null) {
				int hasNew = obj.getInt("hasnew");
				if (hasNew == 1) { // 有更新数据

					JSONArray appCtgArray = obj.getJSONArray("recommend");

					// Log.d(TAG, "parseMsg time :" +
					// System.currentTimeMillis());
					String appInfoStr = appCtgArray.toString();
					// Log.d(TAG, appInfoStr);

					FileUtil.saveByteToSDFile(appInfoStr.getBytes("UTF-8"), SAVE_APP_INFO_PATH);

					saveMark(obj.getString("mark"));
					// Log.d(TAG,
					// "saveByteToSDFile time :"
					// + System.currentTimeMillis());
					return praseAppCtgInfo(appCtgArray);
				}
			}

			// 没有更新,使用本地数据
			String appInfo = RecommAppFileUtil.readFileToString(SAVE_APP_INFO_PATH);

			if (appInfo != null && !"".equals(appInfo)) {
				return praseAppCtgInfo(appInfo);
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private ArrayList<RecommendedAppCategory> praseAppCtgInfo(String appInfo) {
		try {
			// System.out.println("appInfo : "+appInfo);
			JSONArray appCtgArray = new JSONArray(appInfo);
			return praseAppCtgInfo(appCtgArray);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 解析应用分类信息
	 * 
	 * @param appCtgArray
	 * @return
	 */
	private ArrayList<RecommendedAppCategory> praseAppCtgInfo(JSONArray appCtgArray) {
		if (appCtgArray != null) {
			RecommendedAppCategory recomAppCtg = null;
			int count = appCtgArray.length();
			ArrayList<RecommendedAppCategory> recomAppCtgList = new ArrayList<RecommendedAppCategory>(
					count);
			for (int i = 0; i < count; i++) {
				JSONObject obj = (JSONObject) appCtgArray.opt(i);
				if (obj != null) {
					try {
						recomAppCtg = new RecommendedAppCategory();

						recomAppCtg.mTypeId = obj.getString("typeid"); // 分类id
						recomAppCtg.mName = obj.getString("name"); // 分类名称
						recomAppCtg.mIcon = obj.getString("icon"); // 分类图标url(备用)
						recomAppCtg.mViewtype = obj.getInt("viewtype"); // 展现方式
						recomAppCtg.mViewlocal = obj.getInt("viewlocal"); // 展现位置(备用)
						recomAppCtg.mCount = obj.getInt("count"); // 包含的应用数量

						JSONArray appArray = obj.getJSONArray("apps"); // 推荐应用单元数组
						recomAppCtg.mRecommendedAppList = praseAppInfo(appArray);

						recomAppCtgList.add(recomAppCtg);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
			return recomAppCtgList;
		}
		return null;
	}

	/**
	 * 解析应用信息
	 * 
	 * @param appArray
	 * @return
	 */
	private ArrayList<RecommendedApp> praseAppInfo(JSONArray appArray) {
		if (appArray != null) {
			int appCount = appArray.length();
			RecommendedApp recomApp = null;
			ArrayList<RecommendedApp> recommendedAppList = new ArrayList<RecommendedApp>(appCount);
			for (int j = 0; j < appCount; j++) {
				JSONObject appObject = (JSONObject) appArray.opt(j);
				if (appObject != null) {
					try {
						recomApp = new RecommendedApp();

						recomApp.mAppId = appObject.getString("appid");
						recomApp.mTypeId = appObject.getString("typeid"); // 所属分类id
						recomApp.mPackname = appObject.getString("packname"); // 应用包名
						recomApp.mName = appObject.getString("name"); // 应用名称
						recomApp.mIconUrl = appObject.getString("icon"); // 应用图标url
						recomApp.mVersion = appObject.getString("version"); // 版本号
						recomApp.mVersioncode = appObject.getString("versioncode"); // 版本code
						recomApp.mSize = appObject.getString("size"); // 安装包大小
						recomApp.mSummary = appObject.getString("summary"); // 简介
						recomApp.mDownloadtype = appObject.getInt("downloadtype"); // 下载类型
						recomApp.mDownloadurl = appObject.getString("downloadurl"); // 下载地址
						recomApp.mDetailtype = appObject.getInt("detailtype"); // 详情类型
						recomApp.mDetailurl = appObject.getString("detailurl"); // 下载地址

						recomApp.mUnusual = appObject.getInt("unusual"); // 是否特别推荐(备用)

						recommendedAppList.add(recomApp);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return recommendedAppList;
		}
		return null;
	}

	/**
	 * 获取用户运营商代码
	 * 
	 * @return
	 */
	private String getCnUser() {
		String simOperator = "000";
		try {
			if (mContext != null) {
				// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
				TelephonyManager manager = (TelephonyManager) mContext
						.getSystemService(Context.TELEPHONY_SERVICE);
				simOperator = manager.getSimOperator();
			}
		} catch (Throwable e) {
			// TODO: handle exception
		}

		return simOperator;
	}

	/**
	 * 获取语言和国家地区的方法 格式: SIM卡方式：cn 系统语言方式：zh-CN
	 * 
	 * @return
	 */
	private static String language(Context context) {

		String ret = null;

		try {
			TelephonyManager telManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			if (telManager != null) {
				ret = telManager.getSimCountryIso();
				if (ret != null && !ret.equals("")) {
					ret = String.format("%s_%s", Locale.getDefault().getLanguage().toLowerCase(),
							ret.toLowerCase());
				}
			}
		} catch (Throwable e) {
			// e.printStackTrace();
		}

		if (ret == null || ret.equals("")) {
			ret = String.format("%s_%s", Locale.getDefault().getLanguage().toLowerCase(), Locale
					.getDefault().getCountry().toLowerCase());
		}
		return null == ret ? "error" : ret;
	}
}
