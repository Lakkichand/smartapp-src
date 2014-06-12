package com.jiubang.ggheart.appgame.base.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;

/**
 * 分类信息单元解析器，分类信息单元可以是分类推荐单元，也可以是tab栏单元
 * 
 * @author xiedezhi
 * 
 */
public class CategoriesDataParser {

	/**
	 * 解析分类信息数据
	 * 
	 * @param array
	 *            分类信息原始数据
	 * @return 分类信息列表
	 */
	public static List<CategoriesDataBean> parseCategoriesBean(JSONArray array) {
		List<CategoriesDataBean> ret = new ArrayList<CategoriesDataBean>();
		for (int i = 0; i < array.length(); i++) {
			try {
				JSONObject json = array.getJSONObject(i);
				CategoriesDataBean bean = new CategoriesDataBean();
				bean.typeId = json.optInt("typeid", Integer.MIN_VALUE);
				bean.name = json.optString("name", "").trim();
				bean.seq = json.optInt("seq", Integer.MIN_VALUE);
				bean.isHome = json.optInt("ishome", Integer.MIN_VALUE);
				bean.funButton = json.optString("funbutton", "");
				bean.feature = json.optInt("feature", Integer.MIN_VALUE);
				bean.icon = json.optString("icon", "");
				bean.cicon = json.optString("cicon", "");
				bean.accesshome = json.optString("accesshome", "");
				// 协议2.4新增字段
				bean.desc = json.optString("desc", "");
				// 协议2.7新增字段
				bean.pic = json.optString("pic", "");
				// Log.e("XIEDEZHI", "typid(" + bean.typeId + ")  name("
				// + bean.name + ")  isHome = " + bean.isHome);
				ret.add(bean);
			} catch (JSONException e) {
				ClassificationExceptionRecord.getInstance().record(e);
				e.printStackTrace();
			}
		}
		return ret;
	}
}
