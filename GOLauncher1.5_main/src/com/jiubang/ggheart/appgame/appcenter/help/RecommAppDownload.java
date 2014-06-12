package com.jiubang.ggheart.appgame.appcenter.help;

//package com.jiubang.ggheart.appmanagement.help;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.UnsupportedEncodingException;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//import java.util.Random;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.util.Log;
//
//import com.jiubang.ggheart.appmanagement.bean.BoutiqueApp;
//import com.jiubang.ggheart.appmanagement.bean.RecommendedApp;
//import com.jiubang.ggheart.appmanagement.bean.RecommendedAppCategory;
//import com.jiubang.ggheart.appmanagement.component.AppsDetail;
//import com.jiubang.ggheart.appmgr.game.net.GameNetConstant;
//import com.jiubang.ggheart.appmgr.game.net.GameNetUtil;
//import com.jiubang.ggheart.apps.desks.diy.messagecenter.ConstValue;
//import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean;
//import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
//import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStorePhoneStateUtil;
//import com.jiubang.ggheart.launcher.LauncherEnv;
//
//public class RecommAppDownload {
//	//private static final String TAG = "RecommAppDownload";
//	private Context mContext;
///*	private static final String PVERSION = "1.0"; // 请求协议版本号
//	private static final String FEATURE_APP_PVERSION = "1.0";
//	private static final String FEATURE_GAME_PVERSION = "1.0";
//
//	private static final String PVERSION_RTOPIC = "1.0"; // 专题下应用请求协议版本号
//
//	// 正式服务器地址
//	// 应用中心国内域名
//	private static final String APP_CENTER_URL_CHINA = "http://goappcenter.3g.net.cn";
//	
//	// 应用中心国外域名
//	private static final String APP_CENTER_URL_OTHERS = "http://goappcenter.goforandroid.com";*/
//	
//	// 测试后台服务器地址
//	// 应用中心国内域名
////	private static final String APP_CENTER_URL_CHINA = "http://ggtest.3g.net.cn:8011";
////	
////	// 应用中心国外域名
////	private static final String APP_CENTER_URL_OTHERS = "http://ggtest.3g.net.cn:8011";
//	
///*	// 应用中心分类推荐
//	private static final String APP_CENTER_CATEGORIES_PATH = "/recommendedapp/recommend.do?funid=1&rd=";
//	
//	// 应用中心精品推荐
//	private static final String APP_CENTER_FEATURE_PATH = "/recommendedapp/jprecmd.do?funid=1&rd=";*/
//	
///*	// 应用中心热门免费地址路径
//	public static final String APP_CENTER_TOPFREE_PATH = "/recommendedapp/rtopic.do?tid=4&rd=";
//	
//	// 应用中心免费新品地址路径
//	public static final String APP_CENTER_NEWFREE_PATH = "/recommendedapp/rtopic.do?tid=5&rd=";
//	
//	// 应用中心装机必备地址路径
//	public static final String APP_CENTER_MUSTHAVE_PATH = "/recommendedapp/rtopic.do?tid=6&rd=";*/
//
///*	// 缓存应用推荐信息的文件目录
//	private static final String SAVE_CATEGORIES_APP_INFO_PATH = LauncherEnv.Path.APP_MANAGER_RECOMMEND_INFO_PATH
//			+ "categories_app.txt";
//	private static final String SAVE_CATEGORIES_GAME_INFO_PATH = LauncherEnv.Path.APP_MANAGER_RECOMMEND_INFO_PATH
//			+ "categories_game.txt";
//	private static final String SAVE_FEATURE_APP_INFO_PATH = LauncherEnv.Path.APP_MANAGER_RECOMMEND_INFO_PATH
//			+ "feature_app.txt";
//	private static final String SAVE_FEATURE_GAME_INFO_PATH = LauncherEnv.Path.APP_MANAGER_RECOMMEND_INFO_PATH
//			+ "feature_game.txt";*/
//
//	public RecommAppDownload(Context context) {
//		mContext = context;
//	}
//
///*	*//**
//	 * 获取精品应用信息
//	 * 
//	 * @author xiedezhi
//	 *//*
//	public ArrayList<BoutiqueApp> requestFeatureAppData() {
//		Random random = new Random(new Date().getTime());
//		String requesUrl = getAppCenterHost(mContext);
//		String url = requesUrl + APP_CENTER_FEATURE_PATH + random.nextLong();
//
//		int must = hasLocalData(SAVE_FEATURE_APP_INFO_PATH) ? 0 : 1;
//		JSONObject postdataJsonObject = getPostJson(mContext, must,
//				getMark(LauncherEnv.APP_MANAGER_FEATURE_APP_MARK),
//				FEATURE_APP_PVERSION);
//		InputStream input = requestData(postdataJsonObject.toString(), url);
//		if (input == null) {
//			return null;
//		}
//		JSONObject json = parseMsgListStreamData(input, false);
//		ArrayList<BoutiqueApp> ret = parseFeatureMsg(json,
//				SAVE_FEATURE_APP_INFO_PATH,
//				LauncherEnv.APP_MANAGER_FEATURE_APP_MARK);
//		if (ret != null) {
//			for (BoutiqueApp app : ret) {
//				app.pertain = AppsDetail.START_TYPE_APPRECOMMENDED;
//			}
//		}
//		return ret;
//	}*/
//
///*	*//**
//	 * 获取精品游戏信息
//	 * 
//	 * @author xiedezhi
//	 *//*
//	public ArrayList<BoutiqueApp> requestFeatureGameData() {
//		Log.i("XIEDEZHI", "requestFeatureGameData");
//		String host = GameNetUtil.getGameCenterHost(mContext);
//		String url = host + GameNetConstant.GAME_CENTER_FEATURE_PATH
//				+ (new Random(new Date().getTime())).nextLong();
//		int must = hasLocalData(SAVE_FEATURE_GAME_INFO_PATH) ? 0 : 1;
//		JSONObject postdataJsonObject = getPostJson(mContext, must,
//				getMark(LauncherEnv.APP_MANAGER_FEATURE_GAME_MARK),
//				FEATURE_GAME_PVERSION);
//		InputStream input = requestData(postdataJsonObject.toString(), url);
//		if (input == null) {
//			return null;
//		}
//		JSONObject json = parseMsgListStreamData(input, false);
//		ArrayList<BoutiqueApp> ret = parseFeatureMsg(json,
//				SAVE_FEATURE_GAME_INFO_PATH,
//				LauncherEnv.APP_MANAGER_CATEGORIES_GAME_MARK);
//		if (ret != null) {
//			for (BoutiqueApp app : ret) {
//				app.pertain = AppsDetail.START_TYPE_GAMERECOMENDED;
//			}
//		}
//		return ret;
//	}*/
//
///*	*//**
//	 * 获取应用推荐信息，包括分类和应用信息
//	 *//*
//	public ArrayList<RecommendedAppCategory> requestCategoriesAppData() {
//		Random random = new Random(new Date().getTime());
//		String requesUrl = getAppCenterHost(mContext);
//		String url = requesUrl + APP_CENTER_CATEGORIES_PATH + random.nextLong();
//		int must = hasLocalData(SAVE_CATEGORIES_APP_INFO_PATH) ? 0 : 1;
//		JSONObject postdataJsonObject = getPostJson(mContext, must,
//				getMark(LauncherEnv.APP_MANAGER_CATEGORIES_APP_MARK), PVERSION);
//		InputStream input = requestData(postdataJsonObject.toString(), url);
//		if (input == null) {
//			return null;
//		}
//		JSONObject obj = parseMsgListStreamData(input, true);
//		return parseMsg(obj, SAVE_CATEGORIES_APP_INFO_PATH,
//				LauncherEnv.APP_MANAGER_CATEGORIES_APP_MARK);
//	}
//
//	public ArrayList<RecommendedAppCategory> requestCategoriesGameData() {
//		String host = GameNetUtil.getGameCenterHost(mContext);
//		String url = host + GameNetConstant.GAME_CENTER_CATEGORIES_PATH
//				+ (new Random(new Date().getTime())).nextLong();
//		int must = hasLocalData(SAVE_CATEGORIES_GAME_INFO_PATH) ? 0 : 1;
//		JSONObject postdataJsonObject = getPostJson(mContext, must,
//				getMark(LauncherEnv.APP_MANAGER_CATEGORIES_GAME_MARK), PVERSION);
//		InputStream input = requestData(postdataJsonObject.toString(), url);
//		if (input == null) {
//			return null;
//		}
//		JSONObject obj = parseMsgListStreamData(input, true);
//		return parseMsg(obj, SAVE_CATEGORIES_GAME_INFO_PATH,
//				LauncherEnv.APP_MANAGER_CATEGORIES_GAME_MARK);
//	}*/
//
//	/**
//	 * 获取专题下面的应用
//	 * 
//	 * @param url
//	 * @return
//	 *//*
//	public ArrayList<RecommendedApp> requestData(String url) {
//		JSONObject request = new JSONObject();
//		JSONObject phead = RecommAppsUtils.createHttpHeader(mContext,
//				PVERSION_RTOPIC);
//		try {
//			request.put("phead", phead);
//			InputStream input = requestData(request.toString(), url);
//			if (input == null) {
//				Log.i("ABEN",
//						"RecommAppDownload requestData RecommendedApp input is null");
//				return null;
//			}
//			String appInfo = RecommAppFileUtil.readToString(input, "UTF-8");
//			if (appInfo == null || "".equals(appInfo)) {
//				Log.i("ABEN",
//						"RecommAppDownload requestData RecommendedApp appInfo is null");
//				return null;
//			}
//			JSONObject obj = new JSONObject(appInfo);
//			if (obj != null) {
//				JSONObject result = obj
//						.getJSONObject(MessageListBean.TAG_RESULT);
//				int status = result.getInt(MessageListBean.TAG_STATUS);
//				Log.i("ABEN",
//						"RecommAppDownload requestData RecommendedApp result = "
//								+ result.toString());
//				if (status == ConstValue.STATTUS_OK) {
//					JSONArray appCtgArray = obj.getJSONArray("apps");
//					return praseAppInfo(appCtgArray);
//				} else {
//					Log.i("ABEN",
//							"RecommAppDownload requestData RecommendedApp result = "
//									+ result.toString());
//				}
//			}
//
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//
//		return null;
//	}*/
//
///*	*//**
//	 * 获取域名
//	 * 
//	 * @return
//	 *//*
//	public static String getAppCenterHost(Context context) {
//		String requesUrl = APP_CENTER_URL_CHINA;
//		if (!GoStorePhoneStateUtil.isCnUser(context)) {
//			requesUrl = APP_CENTER_URL_OTHERS;
//		}
//		return requesUrl;
//	}*/
//
////	/**
////	 * 向服务器请求数据，返回数据流
////	 * 
////	 * @param headerInfo
////	 * @param url
////	 * @return
////	 */
////	public static InputStream requestData(String headerInfo, String url) {
////		StringEntity se;
////		try {
////			se = new StringEntity(headerInfo);
////			HttpPost hp = new HttpPost(url);
////			hp.setEntity(se);
////			HttpResponse httpResponse = new DefaultHttpClient().execute(hp);
////			HttpEntity httpEntity = httpResponse.getEntity();
////			if (httpEntity != null) {
////				return httpEntity.getContent();
////			}
////		} catch (UnsupportedEncodingException e) {
////			e.printStackTrace();
////		} catch (IOException e) {
////			e.printStackTrace();
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
////		return null;
////	}
//
///*	*//**
//	 * 生成向服务器传递的参数信息
//	 * 
//	 * @param context
//	 * @param phead
//	 * @param must
//	 *            如果本地没有数据，传递1，否则传递0
//	 * @param mark
//	 *            上一次请求，服务器返回的值
//	 * @param pversion
//	 *            向服务器请求的协议版本号
//	 * @return
//	 *//*
//	private JSONObject getPostJson(Context context, int must, String mark,
//			String pversion) {
//		JSONObject request = new JSONObject();
//		JSONObject phead = RecommAppsUtils.createHttpHeader(context, pversion);
//		try {
//			request.put("phead", phead);
//			request.put("must", must);
//			request.put("mark", mark);
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//		return request;
//	}*/
//
//	public static JSONObject parseMsgListStreamData(final InputStream in, final boolean isZipData) {
//		if (in == null) {
//			return null;
//		}
//		try {
//			String jsonString = null;
//			if (isZipData) {
//				jsonString = RecommAppFileUtil.zipData(in);
//			} else {
//				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//				byte[] buff = new byte[1024];
//				int len = -1;
//				try {
//					while ((len = in.read(buff)) != -1) {
//						buffer.write(buff, 0, len);
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				jsonString = new String(buffer.toByteArray());
//			}
//			if (jsonString != null) {
//				// Log.d(TAG, "list:" + jsonString);
//				JSONObject jsonObject = new JSONObject(jsonString);
//				JSONObject result = jsonObject
//						.getJSONObject(MessageListBean.TAG_RESULT);
//				int status = result.getInt(MessageListBean.TAG_STATUS);
//				if (status == ConstValue.STATTUS_OK) {
//					// 解析数据
//					return jsonObject;
//				} else {
//					Log.i("ABEN",
//							"RecommAppDownload parseMsgListStreamData result = "
//									+ result);
//				}
//			}
//		} catch (JSONException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (in != null) {
//					in.close();
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//		}
//		return null;
//	}
//
//	/**
//	 * 精品应用的数据cellsize要按照一定的顺序排列，不然会导致排版有问题
//	 * 
//	 * @author xiedezhi
//	 * 
//	 * @param origin
//	 *            服务器下发的精品数据列表
//	 * @return 修复后的精品数据列表
//	 *//*
//	private ArrayList<BoutiqueApp> fixedFeatureAppOrder(List<BoutiqueApp> origin) {
//		if (origin == null) {
//			return null;
//		}
//		ArrayList<BoutiqueApp> ret = new ArrayList<BoutiqueApp>();
//		for (int i = 0; i < origin.size(); i++) {
//			int cellsize = origin.get(i).cellsize;
//			if (cellsize == 1) {
//				if (i + 1 >= origin.size()) {
//					continue;
//				}
//				int cellsize1 = origin.get(i + 1).cellsize;
//				if (cellsize1 == 1) {
//					ret.add(origin.get(i));
//					ret.add(origin.get(i + 1));
//					i++;
//					continue;
//				} else if (cellsize1 == 2) {
//					if (i + 2 >= origin.size()) {
//						continue;
//					}
//					int cellsize2 = origin.get(i + 2).cellsize;
//					if (cellsize2 == 1) {
//						ret.add(origin.get(i));
//						ret.add(origin.get(i + 1));
//						ret.add(origin.get(i + 2));
//						i += 2;
//						continue;
//					}
//				}
//			} else if (cellsize == 2) {
//				if (i + 1 >= origin.size()) {
//					continue;
//				}
//				int cellsize1 = origin.get(i + 1).cellsize;
//				if (cellsize1 == 2) {
//					ret.add(origin.get(i));
//					ret.add(origin.get(i + 1));
//					i++;
//					continue;
//				} else if (cellsize1 == 1) {
//					if (i + 2 >= origin.size()) {
//						continue;
//					}
//					int cellsize2 = origin.get(i + 2).cellsize;
//					if (cellsize2 == 1) {
//						ret.add(origin.get(i));
//						ret.add(origin.get(i + 1));
//						ret.add(origin.get(i + 2));
//						i += 2;
//					}
//				}
//			} else if (cellsize == 4) {
//				ret.add(origin.get(i));
//				continue;
//			}
//		}
//		for (int i = 0; i < ret.size(); i++) {
//			BoutiqueApp app = ret.get(i);
//			app.index = i + 1;// 位置信息要从1开始
//		}
//		origin.clear();
//		return ret;
//	}*/
//
///*	*//**
//	 * 解析返回的精品应用数据
//	 * 
//	 * @author xiedezhi
//	 *//*
//	private ArrayList<BoutiqueApp> parseFeatureMsg(JSONObject obj,
//			String filepath, String markkey) {
//		try {
//			if (obj != null) {
//				int hasNew = obj.getInt("hasnew");
//				if (hasNew == 1) {
//					FileUtil.saveByteToSDFile(obj.toString().getBytes("UTF-8"),
//							filepath);
//					saveMark(markkey, obj.optString("mark", ""));
//					ArrayList<BoutiqueApp> origin = praseFeatureApp(obj);
//					return fixedFeatureAppOrder(origin);
//				}
//			}
//			// 没有更新,使用本地数据
//			String appInfo = RecommAppFileUtil.readFileToString(filepath);
//			if (appInfo != null && !"".equals(appInfo)) {
//				JSONObject json = new JSONObject(appInfo);
//				ArrayList<BoutiqueApp> origin = praseFeatureApp(json);
//				return fixedFeatureAppOrder(origin);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//
//		return null;
//	}*/
//
//	/**
//	 * 解析返回的数据
//	 * 
//	 * @param obj
//	 * @param msgContent
//	 * @return
//	 *//*
//	private ArrayList<RecommendedAppCategory> parseMsg(JSONObject obj,
//			String filepath, String markkey) {
//		try {
//			if (obj != null) {
//				int hasNew = obj.getInt("hasnew");
//				if (hasNew == 1) { // 有更新数据
//
//					JSONArray appCtgArray = obj.getJSONArray("recommend");
//					String appInfoStr = appCtgArray.toString();
//
//					FileUtil.saveByteToSDFile(appInfoStr.getBytes("UTF-8"),
//							filepath);
//					saveMark(markkey, obj.getString("mark"));
//					return praseAppCtgInfo(appCtgArray);
//				}
//			}
//
//			// 没有更新,使用本地数据
//			String appInfo = RecommAppFileUtil.readFileToString(filepath);
//
//			if (appInfo != null && !"".equals(appInfo)) {
//
//				JSONArray appCtgArray = new JSONArray(appInfo);
//				return praseAppCtgInfo(appCtgArray);
//				// return praseAppCtgInfo(appInfo);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//
//		return null;
//	}*/
//
//	// private ArrayList<RecommendedAppCategory> praseAppCtgInfo(String appInfo)
//	// {
//	// try {
//	// // System.out.println("appInfo : "+appInfo);
//	// JSONArray appCtgArray = new JSONArray(appInfo);
//	// return praseAppCtgInfo(appCtgArray);
//	// } catch (JSONException e) {
//	// e.printStackTrace();
//	// }
//	// return null;
//	// }
//
//	/**
//	 * 解析精品应用信息
//	 * 
//	 * @author xiedezhi
//	 *//*
//	private ArrayList<BoutiqueApp> praseFeatureApp(JSONObject obj) {
//		if (obj == null) {
//			return null;
//		}
//		int typeid = obj.optInt("typeid", Integer.MIN_VALUE);
//		JSONArray array = null;
//		try {
//			array = obj.getJSONArray("recommend");
//		} catch (JSONException e) {
//			e.printStackTrace();
//			return null;
//		}
//		int count = array.length();
//		ArrayList<BoutiqueApp> ret = new ArrayList<BoutiqueApp>();
//		for (int i = 0; i < count; i++) {
//			try {
//				JSONObject json = (JSONObject) array.opt(i);
//				Log.i("XIEDEZHI", json.toString());
//				BoutiqueApp app = new BoutiqueApp();
//				app.typeid = typeid;
//				app.rid = json.optInt("rid", Integer.MIN_VALUE);
//				app.type = json.optInt("type", Integer.MIN_VALUE);
//				app.acttype = json.optInt("acttype", Integer.MIN_VALUE);
//				app.actvalue = json.optString("actvalue", null);
//				app.cellsize = json.optInt("cellsize", Integer.MIN_VALUE);
//				app.viewtype = json.optInt("viewtype", Integer.MIN_VALUE);
//				app.pic = json.optString("pic", null);
//				app.hasLoadedPic = false;
//				app.name = json.optString("name", null);
//				JSONObject info = json.optJSONObject("appinfo");
//				if (info != null) {
//					app.info.appid = info.optString("appid", null);
//					app.info.packname = info.optString("packname", null);
//					app.info.name = info.optString("name", null);
//					app.info.icon = info.optString("icon", null);
//					app.info.version = info.optString("version", null);
//					app.info.versioncode = info.optString("versioncode", null);
//					app.info.size = info.optString("size", null);
//					app.info.summary = info.optString("summary", null);
//					app.info.grade = info.optInt("grade", Integer.MIN_VALUE);
//					app.info.price = info.optString("price", null);
//					app.info.developer = info.optString("developer", null);
//					app.info.devgrade = info.optInt("devgrade",
//							Integer.MIN_VALUE);
//				}
//				ret.add(app);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		return ret;
//	}*/
//
//	/**
//	 * 解析应用分类信息
//	 * 
//	 * @param appCtgArray
//	 * @return
//	 *//*
//	private ArrayList<RecommendedAppCategory> praseAppCtgInfo(
//			JSONArray appCtgArray) {
//		if (appCtgArray != null) {
//			RecommendedAppCategory recomAppCtg = null;
//			int count = appCtgArray.length();
//			ArrayList<RecommendedAppCategory> recomAppCtgList = new ArrayList<RecommendedAppCategory>(
//					count);
//			for (int i = 0; i < count; i++) {
//				JSONObject obj = (JSONObject) appCtgArray.opt(i);
//				if (obj != null) {
//					try {
//						recomAppCtg = new RecommendedAppCategory();
//
//						recomAppCtg.mTypeId = obj.getString("typeid"); // 分类id
//						recomAppCtg.mName = obj.getString("name"); // 分类名称
//						recomAppCtg.mIcon = obj.getString("icon"); // 分类图标url(备用)
//						recomAppCtg.mViewtype = obj.getInt("viewtype"); // 展现方式
//						recomAppCtg.mViewlocal = obj.getInt("viewlocal"); // 展现位置(备用)
//						recomAppCtg.mCount = obj.getInt("count"); // 包含的应用数量
//
//						JSONArray appArray = obj.getJSONArray("apps"); // 推荐应用单元数组
//						recomAppCtg.mRecommendedAppList = praseAppInfo(appArray);
//
//						recomAppCtgList.add(recomAppCtg);
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//
//			}
//			return recomAppCtgList;
//		}
//		return null;
//	}*/
//
//	/**
//	 * 解析应用信息
//	 * 
//	 * @param appArray
//	 * @return
//	 *//*
//	private ArrayList<RecommendedApp> praseAppInfo(JSONArray appArray) {
//		if (appArray != null) {
//			int appCount = appArray.length();
//			RecommendedApp recomApp = null;
//			ArrayList<RecommendedApp> recommendedAppList = new ArrayList<RecommendedApp>(
//					appCount);
//			for (int j = 0; j < appCount; j++) {
//				JSONObject appObject = (JSONObject) appArray.opt(j);
//				if (appObject != null) {
//					try {
//						recomApp = new RecommendedApp();
//
//						recomApp.mAppId = appObject.getString("appid");
//						recomApp.mTypeId = appObject.getString("typeid"); // 所属分类id
//						recomApp.mPackname = appObject.getString("packname"); // 应用包名
//						recomApp.mName = appObject.getString("name"); // 应用名称
//						recomApp.mIconUrl = appObject.getString("icon"); // 应用图标url
//						recomApp.mVersion = appObject.getString("version"); // 版本号
//						recomApp.mVersioncode = appObject
//								.getString("versioncode"); // 版本code
//						recomApp.mSize = appObject.getString("size"); // 安装包大小
//						recomApp.mSummary = appObject.getString("summary"); // 简介
//						recomApp.mDownloadtype = appObject
//								.getInt("downloadtype"); // 下载类型
//						recomApp.mDownloadurl = appObject
//								.getString("downloadurl"); // 下载地址
//						recomApp.mDetailtype = appObject.getInt("detailtype"); // 详情类型
//						recomApp.mDetailurl = appObject.getString("detailurl"); // 下载地址
//
//						recomApp.mUnusual = appObject.getInt("unusual"); // 是否特别推荐(备用)
//						recomApp.mGrade = appObject.getInt("grade"); // 应用的等级
//						recomApp.mPrice = appObject.getString("price"); // 应用的价格
//						recomApp.mDeveloper = appObject.getString("developer"); // 应用开发者
//						recomApp.mDevgrade = appObject.getInt("devgrade"); // 开发者等级
//						// recomApp.mGrade = 5;
//						// recomApp.mPrice = "免费";
//						// recomApp.mDeveloper = "test";
//						// recomApp.mDevgrade = 1;
//
//						recommendedAppList.add(recomApp);
//
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//			return recommendedAppList;
//		}
//		return null;
//	}*/
//
//	/**
//	 * 判断本地是否存在指定文件
//	 * 
//	 * @param localPath
//	 * @return
//	 *//*
//	private boolean hasLocalData(String localPath) {
//		File file = new File(localPath);
//		if (file != null && file.exists()) {
//			return true;
//		}
//		return false;
//	}
//
//	private void saveMark(String key, String mark) {
//		SharedPreferences sharedPreferences = mContext.getSharedPreferences(
//				LauncherEnv.APP_MANAGER_RECOMMEND, Context.MODE_PRIVATE);
//		SharedPreferences.Editor editor = sharedPreferences.edit();
//		editor.putString(key, mark);
//		editor.commit();
//	}
//
//	private String getMark(String key) {
//		SharedPreferences sharedPreferences = mContext.getSharedPreferences(
//				LauncherEnv.APP_MANAGER_RECOMMEND, Context.MODE_PRIVATE);
//		return sharedPreferences.getString(key, "");
//	}*/
// }
