package com.jiubang.ggheart.appgame.base.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp.BoutiqueAppInfo;

/**
 * 精品推荐数据解析器
 * 
 * @author xiedezhi
 * 
 */
public class FeatureDataParser {

	/**
	 * 解析精品应用信息、专题推荐应用信息、编辑推荐应用信息，并初始化一些应用信息
	 * 
	 * @param typeid
	 *            数据列表所属的分类id
	 * @param array
	 *            服务器返回的原始数据
	 * @param dataType
	 *            数据类型，表示要解析的数据是属于精品推荐数据、专题推荐数据还是编辑推荐数据
	 * @return 解析得到的数据列表
	 */
	public static ArrayList<BoutiqueApp> parseFeatureApp(int typeid, JSONArray array) {
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
				app.type = json.optInt("type", Integer.MIN_VALUE);
				app.acttype = json.optInt("acttype", Integer.MIN_VALUE);
				app.actvalue = json.optString("actvalue", "");
				app.cellsize = json.optInt("cellsize", Integer.MIN_VALUE);
				app.pic = json.optString("pic", "");
				app.name = json.optString("name", "").trim();
				JSONObject info = json.optJSONObject("appinfo");
				parseAppInfo(info, app.info);
				JSONObject typeInfo = json.optJSONObject("typeinfo");
				if (typeInfo != null) {
					app.typeInfo.typeid = typeInfo.optInt("typeid", -1);
					app.typeInfo.name = typeInfo.optString("name", "");
					app.typeInfo.summary = typeInfo.optString("summary", "");
//					app.typeInfo.grade = typeInfo.optInt("grade", Integer.MIN_VALUE);
				}
				ret.add(app);
			} catch (Exception e) {
				ClassificationExceptionRecord.getInstance().record(e);
				e.printStackTrace();
			}
		}
		return ret;
	}

	/**
	 * 解析应用信息单元，并把值赋于BoutiqueAppInfo
	 */
	public static void parseAppInfo(JSONObject json, BoutiqueAppInfo info) {
		if (json == null || info == null) {
			return;
		}
		info.appid = json.optString("appid", "");
		info.packname = json.optString("packname", "");
		info.name = json.optString("name", "").trim();
		info.icon = json.optString("icon", "");
		info.version = json.optString("version", "");
		info.versioncode = json.optString("versioncode", "");
		info.size = json.optString("size", "");
		info.summary = json.optString("summary", "");
		info.grade = json.optInt("grade", Integer.MIN_VALUE);
		info.isfree = json.optInt("isfree", Integer.MIN_VALUE);
		info.oldprice = json.optString("oldprice", "");
		info.price = json.optString("price", "");
		info.developer = json.optString("developer", "");
//		info.devgrade = json.optInt("devgrade", Integer.MIN_VALUE);
//		info.feature = json.optInt("feature", Integer.MIN_VALUE);
		info.ficon = json.optString("ficon", "");
		info.downloadtype = json.optInt("downloadtype", Integer.MIN_VALUE);
		info.downloadurl = json.optString("downloadurl", "");
		info.cback = json.optInt("cback", 0);
		info.cbacktype = json.optInt("cbacktype", 0);
		info.cbackurl = json.optString("cbackurl", "");
//		info.remdtype = json.optInt("remdtype", Integer.MIN_VALUE);
//		info.remdmsg = json.optString("remdmsg", "");
		info.effect = json.optInt("effect", 0);
		info.treatment = json.optInt("treatment", 0);
		info.typeinfo = json.optString("typeinfo", "");
		//2.7 新增字段
		info.detailtype = json.optInt("detailtype", Integer.MIN_VALUE);
		info.detailurl = json.optString("detailurl", "");
//		info.unusual = json.optString("unusual", "");
//		info.apptype = json.optInt("apptype", 1);
		//2.8 新增字段
//		info.paytype = json.optString("paytype", "");
//		info.payid = json.optString("payid", "");
		// 2.9 新增
//		info.downloadcount = json.optInt("downloadcount", 0);
		info.dlcs = json.optString("dlcs", "");
		info.pics = json.optString("pics", "");
//		if (strPicUrls != null && !strPicUrls.trim().equals("")) {
//			// 解析图片url字符串:每张图片之间用@@分隔,小图大图之间用##分隔
//			info.smallPics = new ArrayList<String>();
//			info.bigPics = new ArrayList<String>();
//			String[] picUrls = strPicUrls.split("@@");
//			for (String picUrl : picUrls) {
//				int index = picUrl.indexOf("##");
//				if (index != -1) {
//					String smallPicUrl = picUrl.substring(0, index);
//					String largePicUrl = picUrl.substring(index + 2, picUrl.length());
//					info.smallPics.add(smallPicUrl);
//					info.bigPics.add(largePicUrl);
//				}
//			}
//		}
		info.changetime = json.optString("changetime", "");
		info.commentsnum = json.optString("commentsnum", "");
		info.detailstyle = json.optInt("detailstyle", 0);
		//2.9 新增字段
//		info.resourceurl = json.optString("resourceurl", "");
		//3.0 新增字段
		info.icbackurl = json.optString("icbackurl", "");
		// TODO 木瓜sdk
		//3.２ 新增字段
		info.iAfCbackurl = json.optString("mgcbackurl", "");
		info.tag = json.optInt("tag", 0);

	}

}
