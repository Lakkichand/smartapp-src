package com.jiubang.ggheart.components.advert;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

/**
 * 
 * <br>类描述:15广告json解析类
 * <br>功能详细描述:
 * 
 * @author  licanhui
 * @date  [2012-12-28]
 */
public class AdvertJsonUntil {
	
	/**
	 * <br>功能简述:设置普通图标的json缓存
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param advertInfo
	 * @return
	 */
	public static JSONObject setAdvertAppJson(AdvertInfo advertInfo) {
		JSONObject jsonObject = new JSONObject();
		try {
			int[] xy = AdvertControl.getXY(advertInfo.mPos);
			jsonObject.put("isfile", advertInfo.mIsfile);
			jsonObject.put("cellX", xy[0]);
			jsonObject.put("cellY", xy[1]);
			jsonObject.put("pos", advertInfo.mPos);
			jsonObject.put("title", advertInfo.mTitle);
			jsonObject.put("screem", advertInfo.mScreen);
			jsonObject.put("packagename", advertInfo.mPackageName);
			JSONArray array = new JSONArray();
			jsonObject.put("filemsg", array);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	/**
	 * <br>功能简述:设置文件夹图标的json缓存
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param advertInfo
	 * @return
	 */
	public static JSONObject setAdvertFolderJson(AdvertInfo advertInfo, ArrayList<AdvertInfo> advertInfosList) {
		JSONObject jsonObject = new JSONObject();
		try {
			int[] xy = AdvertControl.getXY(advertInfo.mPos);
			jsonObject.put("isfile", advertInfo.mIsfile);
			jsonObject.put("cellX", xy[0]);
			jsonObject.put("cellY", xy[1]);
			jsonObject.put("pos", advertInfo.mPos);
			jsonObject.put("title", advertInfo.mTitle);
			jsonObject.put("screem", advertInfo.mScreen);
			jsonObject.put("packagename", advertInfo.mPackageName);
		
			JSONArray array = new JSONArray();
			if (advertInfosList != null) {
				int size = advertInfosList.size();
				for (int i = 0; i < size; i++) {
					JSONObject oneJsonObject = getFileItemJson(advertInfosList.get(i));
					if (oneJsonObject != null) {
						array.put(oneJsonObject);
					}
				}
			}
			jsonObject.put("filemsg", array);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	/**
	 * <br>功能简述:设置文件夹图标的每个子项缓存
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param advertInfo
	 * @return
	 */
	public static JSONObject getFileItemJson(AdvertInfo advertInfo) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("isfile", advertInfo.mIsfile);
			jsonObject.put("cellX", 0);
			jsonObject.put("cellY", 0);
			jsonObject.put("pos", advertInfo.mPos);
			jsonObject.put("title", advertInfo.mTitle);
			jsonObject.put("screem", advertInfo.mScreen);
			jsonObject.put("packageName", advertInfo.mPackageName);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	/**
	 * <br>功能简述:解析JSON缓存，返回广告对象数据
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @param msgsArray
	 * @param isFile
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
				advertInfo.mTitle = msgJsonObject.optString("title");
				advertInfo.mPackageName = msgJsonObject.optString("packagename");
				advertInfo.mCellX = msgJsonObject.optInt("cellX", -1);
				advertInfo.mCellY = msgJsonObject.optInt("cellY", -1);
				advertInfo.mScreen = msgJsonObject.optInt("screem", 0);
				advertInfo.mPos = msgJsonObject.optInt("pos", -1);
				advertInfo.mIsfile = msgJsonObject.optInt("isfile", 0);

				//判断是否文件夹
				if (advertInfo.mIsfile == 1 && isFile) {
					JSONArray fileArray = msgJsonObject.getJSONArray("filemsg");
					advertInfo.mFilemsg = getAdvrtArrary(context, fileArray, false);
				}
				advertInfosList.add(advertInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return advertInfosList;
	}
}
