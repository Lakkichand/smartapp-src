/*
 * 文 件 名:  InstallCallback.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-12-19
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.net;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-12-19]
 */
public class InstallCallbackManager {

	public static final String INSTALL_CALLBACK_PREFERENCES_NAME = "install_callback_preferences";

	public static final String TREATMENT = "treatment";

	public static final String TREATMENT_TIME = "treatment_time";

	public static final String ICBACKURL = "icbackurl";

	public static final String ICBACKURL_TIME = "icbackurl_time";
	
	public static final long TIME_STEP = 1000 * 60 * 60 * 3;

	/**
	 * <br>功能简述:保存treat的值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param packagename
	 * @param treatment
	 */
	public static void saveTreatment(String packagename, int treatment) {
		try {
			PreferencesManager sp = new PreferencesManager(GOLauncherApp.getContext(),
					INSTALL_CALLBACK_PREFERENCES_NAME, Context.MODE_PRIVATE);
			// 从sharePreferences取出存储的string,并转成JSON数据
			String jsonString = sp.getString(packagename, "");
			JSONObject jsonObject = null;
			if (!jsonString.equals("") && !jsonString.equals("{}")) {
				jsonObject = new JSONObject(jsonString);
			} else {
				jsonObject = new JSONObject();
			}
			// 更新JSON的字段
			jsonObject.put(TREATMENT, treatment);
			jsonObject.put(TREATMENT_TIME, System.currentTimeMillis());
			// 保存修改
			sp.putString(packagename, jsonObject.toString());
			sp.commit();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <br>功能简述:保存安装成功的回调url地址
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param packagename
	 * @param icbackurl
	 */
	public static void saveCallbackUrl(String packagename, String icbackurl) {
		try {
			PreferencesManager sp = new PreferencesManager(GOLauncherApp.getContext(),
					INSTALL_CALLBACK_PREFERENCES_NAME, Context.MODE_PRIVATE);
			// 从sharePreferences取出存储的string,并转成JSON数据
			String jsonString = sp.getString(packagename, "");
			JSONObject jsonObject = null;
			if (!jsonString.equals("") && !jsonString.equals("{}")) {
				jsonObject = new JSONObject(jsonString);
			} else {
				jsonObject = new JSONObject();
			}
			// 更新JSON的字段
			jsonObject.put(ICBACKURL, icbackurl);
			jsonObject.put(ICBACKURL_TIME, System.currentTimeMillis());
			// 保存修改
			sp.putString(packagename, jsonObject.toString());
			sp.commit();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <br>功能简述:返回treatment的值
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param packagename
	 * @return 返回-1表示没有该值
	 */
	public static int getTreatment(String packagename) {
		int treatment = -1;
		try {
			PreferencesManager sp = new PreferencesManager(GOLauncherApp.getContext(),
					INSTALL_CALLBACK_PREFERENCES_NAME, Context.MODE_PRIVATE);
			// 从sharePreferences取出存储的string
			String jsonString = sp.getString(packagename, "");
			// JsonString为默认值，说明sharePreferences没有存储该packageName的数据
			if (jsonString.equals("") || jsonString.equals("{}")) {
				return treatment;
			}
			// 取值并更新JSON的字段
			JSONObject jsonObject = new JSONObject(jsonString);
			treatment = (Integer) jsonObject.optInt(TREATMENT, -1);
			jsonObject.remove(TREATMENT);
			jsonObject.remove(TREATMENT_TIME);
			// 保存修改
			jsonString = jsonObject.toString();
			// 假如json是空的，说明已经没有存储数据，将packageName的键值对删除
			if (jsonString.equals("") || jsonString.equals("{}")) {
				sp.remove(packagename);
			} else {
				sp.putString(packagename, jsonString);
			}
			sp.commit();
			return treatment;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return treatment;
	}
	
	/**
	 * <br>功能简述:安装成功的回调url地址
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param packagename
	 * @return 返回null表示没有回调地址
	 */
	public static String getIcbackurl(String packagename) {
		String icbackurl = null;
		try {
			PreferencesManager sp = new PreferencesManager(GOLauncherApp.getContext(),
					INSTALL_CALLBACK_PREFERENCES_NAME, Context.MODE_PRIVATE);
			// 从sharePreferences取出存储的string
			String jsonString = sp.getString(packagename, "");
			// JsonString为默认值，说明sharePreferences没有存储该packageName的数据
			if (jsonString.equals("") || jsonString.equals("{}")) {
				return icbackurl;
			}
			// 更新JSON的字段
			JSONObject jsonObject = new JSONObject(jsonString);
			icbackurl = jsonObject.optString(ICBACKURL, "");
//			jsonObject.remove(ICBACKURL);
//			jsonObject.remove(ICBACKURL_TIME);
			// 保存修改
			// 假如json是空的，说明已经没有存储数据，将packageName的键值对删除
			jsonString = jsonObject.toString();
			if (jsonString.equals("") || jsonString.equals("{}")) {
				sp.remove(packagename);
			} else {
				sp.putString(packagename, jsonString);
			}
			sp.commit();
			if (icbackurl.equals("")) {
				icbackurl = null;
			}
			return icbackurl;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return icbackurl;
	}
	
	public static void cleanSharePreferencesData() {
		try {
//			SharedPreferences mPreferences = GOLauncherApp.getContext().getSharedPreferences(
//					INSTALL_CALLBACK_PREFERENCES_NAME, Context.MODE_PRIVATE);
			PreferencesManager sp = new PreferencesManager(GOLauncherApp.getContext(),
					INSTALL_CALLBACK_PREFERENCES_NAME, Context.MODE_PRIVATE);
			Map<String, String> map = (Map<String, String>) sp.getAll();
			Iterator<Entry<String, String>> iter = map.entrySet().iterator();
			// 当前系统时间，判断存储的值是否需要清除
			long currentTime = System.currentTimeMillis();
			while (iter.hasNext()) {
				Entry<String, String> entry = iter.next();
				String jsonString = entry.getValue();
				JSONObject jsonObject = new JSONObject(jsonString);
				long treatTime = jsonObject.optLong(TREATMENT_TIME, 0);
				long icbackUrlTime = jsonObject.optLong(ICBACKURL_TIME, 0);
				// treatment的存储时间判断
				if (currentTime - treatTime >= TIME_STEP) {
					jsonObject.remove(TREATMENT_TIME);
					jsonObject.remove(TREATMENT);
				}
				// 回调地址的存储时间判断
				if (currentTime - icbackUrlTime >= TIME_STEP) {
					jsonObject.remove(ICBACKURL_TIME);
					jsonObject.remove(ICBACKURL);
				}
				// 根据结果，决定删除还是更新数据
				jsonString = jsonObject.toString();
				if (jsonString.equals("") || jsonString.equals("{}")) {
					iter.remove();
				} else {
					entry.setValue(jsonString);
				}
			}
			// 将所有结果一次性更新到sharePreferences
			saveSharePreferencesData(sp, map);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * <br>功能简述:批量存储
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param sp
	 * @param map
	 */
	private static void saveSharePreferencesData(PreferencesManager sp, Map<String, String> map) {
		if (sp == null) {
			return ;
		}
		sp.clear();
		Iterator<Entry<String, String>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			sp.putString(entry.getKey(), entry.getValue());
		}
		sp.commit();
	}
}
