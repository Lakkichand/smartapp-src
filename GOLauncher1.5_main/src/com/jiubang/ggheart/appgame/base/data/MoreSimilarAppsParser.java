package com.jiubang.ggheart.appgame.base.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;

/**
 * 
 * <br>类描述:更多相关推荐数据解析类
 * <br>功能详细描述:
 * 
 * @author  zhengxiangcan
 * @date  [2012-12-20]
 */
public class MoreSimilarAppsParser {
	/**
	 * 
	 * @param id
	 *            数据所属的分类id
	 * @param json
	 *            服务器下发或者本地读取出来的Json数据
	 * @return 分类id对应的数据
	 */
	public static ClassificationDataBean parseDataBean(int id, JSONObject json) {
		if (json == null) {
			return null;
		}
		ClassificationDataBean ret = new ClassificationDataBean();
		//返回的typeid字段才是真实的id
		if (json.has("typeid")) {
			int typeId = json.optInt("typeid", -1);
			ret.typeId = typeId;
		} else {
			ret.typeId = id;
		}
		String typename = json.optString("typename", "");
		ret.typename = typename;
		int pages = json.optInt("pages", Integer.MIN_VALUE);
		int pageid = json.optInt("pageid", Integer.MIN_VALUE);
		String summary = json.optString("summary", "");
		ret.summary = summary;
		ret.dataType = ClassificationDataBean.ONEPERLINE_SPECIALSUBJECT_TYPE;

		// 应用或专题数据
		try {
			JSONArray array = json.getJSONArray("apps");

//			ret.dataType = ClassificationDataBean.PRICE_ALERT;
			ret.featureList = parseSimilarApp(ret.typeId,
					array);
			ret.pages = pages;
			ret.pageid = pageid;
			return ret;
		} catch (JSONException e) {
			e.printStackTrace();
			ClassificationExceptionRecord.getInstance().record(e);
		}
		return null;
	}
	public static ArrayList<BoutiqueApp> parseSimilarApp(int typeid, JSONArray array) {
		if (array == null) {
			return null;
		}
		int count = array.length();
		ArrayList<BoutiqueApp> ret = new ArrayList<BoutiqueApp>();
		for (int i = 0; i < count; i++) {
			try {
				JSONObject json = (JSONObject) array.opt(i);
				BoutiqueApp app = new BoutiqueApp();
				app.typeid = typeid;
				app.rid = json.optInt("rid", Integer.MIN_VALUE);
				app.type = 2;
				app.cellsize = json.optInt("cellsize", Integer.MIN_VALUE);
				app.pic = json.optString("pic", "");
				app.name = json.optString("name", "");
				//				JSONObject info = json.optJSONObject("appinfo");
				FeatureDataParser.parseAppInfo(json, app.info);
				JSONObject typeInfo = json.optJSONObject("typeinfo");
				if (typeInfo != null) {
					app.typeInfo.typeid = typeInfo.optInt("typeid", -1);
					app.typeInfo.name = typeInfo.optString("name", "");
					app.typeInfo.summary = typeInfo.optString("summary", "");
//					app.typeInfo.grade = typeInfo.optInt("grade",
//							Integer.MIN_VALUE);
				}
				
				if (app.info.detailtype == BoutiqueApp.DETAIL_TYPE_FTP) {
					app.acttype = BoutiqueApp.FEATURE_ACTTYPE_FTP;
				} else if (app.info.detailtype == BoutiqueApp.DETAIL_TYPE_MARKET) {
					app.acttype = BoutiqueApp.FEATURE_ACTTYPE_MARKET;
				} else if (app.info.detailtype == BoutiqueApp.DETAIL_TYPE_WEB) {
					app.acttype = BoutiqueApp.FEATURE_ACTTYPE_BROWSER;
				}
				app.actvalue = app.info.detailurl;
				
				ret.add(app);
			} catch (Exception e) {
				ClassificationExceptionRecord.getInstance().record(e);
				e.printStackTrace();
			}
		}
		return ret;
	}
}
