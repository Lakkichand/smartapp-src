package com.zhidian.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.zhidian.bean.DownloadBean;

/**
 * 解析工具类
 * 
 * @author zhaoyl
 * 
 */
public class DataParser {

	/**
	 * 解析全部下载包路径信息
	 * 
	 * @param json
	 * @return
	 */
	public static List<DownloadBean> parserDownload(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}

		List<DownloadBean> list = new ArrayList<DownloadBean>();

		try {
			JSONObject obj = new JSONObject(json);

			int status = obj.optInt("statusCode", -1);
			JSONArray array = obj.optJSONArray("result");
			if (array != null && status == 0) {
				for (int i = 0; i < array.length(); i++) {
					String str = array.getString(i);
					DownloadBean bean = new DownloadBean();
					bean = parserDownloadDetail(str);
					list.add(bean);
				}
			}

			

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return list;

	}

	private static DownloadBean parserDownloadDetail(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}

		try {
			JSONObject obj = new JSONObject(json);
			DownloadBean b = new DownloadBean();
			b.boxNum = obj.optString("boxNum", "");
			b.code = obj.optString("code", "");
			b.versionCode = obj.optInt("versionCode", -1);
			b.downUrl = obj.optString("downloadUrl", "");
			return b;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;

		}

	}

}
