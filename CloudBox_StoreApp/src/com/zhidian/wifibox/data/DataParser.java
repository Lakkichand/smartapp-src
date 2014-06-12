package com.zhidian.wifibox.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.zhidian.wifibox.data.DetailDataBean.RelatedRecommendBean;
import com.zhidian.wifibox.util.FileUtil;

/**
 * 数据解析类，不同的数据格式走不同的解析流程，但都放在同一个DataBean中，不同的流程初始化为不同的datatype
 * 
 * @author xiedezhi
 * 
 */
public class DataParser {

	/**
	 * 解析应用列表对象
	 */
	public static AppDataBean parseAppData(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			JSONObject obj = new JSONObject(json);
			AppDataBean bean = new AppDataBean();
			bean.id = obj.optLong("id", 0);
			bean.name = obj.optString("name", "");
			bean.explain = obj.optString("explain", "");
			bean.iconUrl = obj.optString("iconUrl", "");
			bean.downloads = obj.optLong("downloads", 0);
			bean.downloadUrl = obj.optString("downloadUrl", "");
			bean.score = obj.optInt("score", 0);
			bean.size = obj.optInt("size", 0);
			bean.packName = obj.optString("packageName", "");
			bean.version = obj.optString("version", "");
			return bean;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解析相关推荐Bean
	 */
	public static DetailDataBean.RelatedRecommendBean parseRelatedRecommendBean(
			String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			JSONObject obj = new JSONObject(json);
			DetailDataBean.RelatedRecommendBean bean = new DetailDataBean.RelatedRecommendBean();
			bean.id = obj.optLong("id", 0);
			bean.name = obj.optString("name", "");
			bean.iconUrl = obj.optString("iconUrl", "");
			return bean;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static DetailDataBean parseDetail(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}

		try {
			JSONObject obj = new JSONObject(json);
			DetailDataBean bean = new DetailDataBean();
			int status = obj.optInt("statusCode", -1);
			String message = obj.optString("message", "");
			Log.i("parseDetail", message);
			String result = obj.optString("result");
			if (status == 0) {
				bean = parseDetailData(result);

			} else {
				bean = null;
			}
			return bean;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * 解析相关推荐应用对象
	 */
	public static List<RelatedRecommendBean> parseRelatedData(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}

		try {
			JSONObject obj = new JSONObject(json);
			List<RelatedRecommendBean> list = new ArrayList<RelatedRecommendBean>();
			int status = obj.optInt("statusCode", -1);
			String message = obj.optString("message", "");
			Log.i("解析相关推荐", message);
			JSONArray relatedApps = obj.optJSONArray("result");

			if (status == 0) {
				if (relatedApps != null) {
					for (int i = 0; i < relatedApps.length(); i++) {
						list.add(parseRelatedRecommendBean(relatedApps
								.getString(i)));
					}
				}
				return list;
			} else {
				return null;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * 解析应用详情对象
	 */
	public static DetailDataBean parseDetailData(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			JSONObject obj = new JSONObject(json);
			DetailDataBean bean = new DetailDataBean();
			bean.id = obj.optLong("id", 0);
			bean.name = obj.optString("name", "");
			bean.explain = obj.optString("explain", "");
			bean.description = obj.optString("description", "");
			bean.iconUrl = obj.optString("iconUrl", "");
			bean.downloads = obj.optLong("downloads", 0);
			bean.downloadUrl = obj.optString("downloadUrl", "");
			bean.rating = obj.optInt("rating", 0);
			bean.size = obj.optInt("size", 0);
			bean.version = obj.optString("version", "");
			bean.language = obj.optString("language", "");
			bean.updateTime = obj.optString("updateTime", "");
			bean.author = obj.optString("author", "");
			bean.packageName = obj.optString("packageName", "");
			JSONArray screenshots = obj.optJSONArray("screenshotUrls");
			if (screenshots != null) {
				for (int i = 0; i < screenshots.length(); i++) {
					bean.screenshotUrls.add(screenshots.getString(i));
				}
			}
			JSONArray relatedApps = obj.optJSONArray("relatedApps");
			if (relatedApps != null) {
				for (int i = 0; i < relatedApps.length(); i++) {
					bean.relatedApps.add(parseRelatedRecommendBean(relatedApps
							.getString(i)));
				}
			}
			return bean;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解析幻灯片数据
	 */
	public static BannerDataBean parseBanner(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			JSONObject obj = new JSONObject(json);
			BannerDataBean bean = new BannerDataBean();
			bean.title = obj.optString("title", "");
			bean.type = obj.optInt("type", 0);
			bean.imgUrl = obj.optString("imgUrl", "");
			bean.target = obj.optString("target", "");
			return bean;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解析专题数据
	 */
	public static TopicDataBean parseTopic(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			JSONObject obj = new JSONObject(json);
			TopicDataBean bean = new TopicDataBean();
			bean.id = obj.optLong("id", 0);
			bean.title = obj.optString("title", "");
			bean.iconUrl = obj.optString("iconUrl", "");
			bean.bannerUrl = obj.optString("bannerUrl", "");
			bean.description = obj.optString("description", "");
			bean.updateTime = obj.optString("updateTime", "");
			bean.message = obj.optString("message", "");
			bean.isNameVisited = obj.optInt("isNameVisited", 0);
			return bean;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解析应用分类数据单元
	 */
	public static CategoriesDataBean parseCategories(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			JSONObject obj = new JSONObject(json);
			CategoriesDataBean bean = new CategoriesDataBean();
			bean.id = obj.optLong("id", 0);
			bean.name = obj.optString("name", "");
			bean.iconUrl = obj.optString("iconUrl", "");
			bean.explain = obj.optString("explain", "");
			return bean;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解析幻灯片列表，没有分页
	 */
	public static void parseBannerList(Context context, PageDataBean bean,
			String json) {
		if (TextUtils.isEmpty(json)) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(json);
			bean.mDataType = PageDataBean.BANNER_DATATYPE;
			bean.mStatuscode = obj.optInt("statusCode", -1);
			if (bean.mStatuscode == 0) {
				// 保存数据到文件
				FileUtil.saveByteToFile(json.getBytes(), context.getCacheDir()
						.getAbsolutePath() + "/" + bean.mUrl.hashCode());
			}
			bean.mMessage = obj.optString("message", "");
			JSONArray array = obj.optJSONArray("result");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					String str = array.getString(i);
					bean.mBannerList.add(parseBanner(str));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析分类列表，没有分页
	 */
	public static void parseCategoryList(Context context, PageDataBean bean,
			String json) {
		if (TextUtils.isEmpty(json)) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(json);
			bean.mDataType = PageDataBean.CATEGORIES_DATATYPE;
			bean.mStatuscode = obj.optInt("statusCode", -1);
			if (bean.mStatuscode == 0) {
				// 保存数据到文件
				FileUtil.saveByteToFile(json.getBytes(), context.getCacheDir()
						.getAbsolutePath() + "/" + bean.mUrl.hashCode());
			}
			bean.mMessage = obj.optString("message", "");
			JSONArray array = obj.optJSONArray("result");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					String str = array.getString(i);
					bean.mCatList.add(parseCategories(str));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析专题列表，有分页
	 */
	public static void parseTopicList(Context context, PageDataBean bean,
			String json) {
		if (TextUtils.isEmpty(json)) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(json);
			bean.mDataType = PageDataBean.TOPIC_DATATYPE;
			bean.mStatuscode = obj.optInt("statusCode", -1);
			if (bean.mStatuscode == 0 && bean.mPageIndex == 1) {
				// 如果数据返回码为0并且数据是第一页数据，则保存数据到文件
				FileUtil.saveByteToFile(json.getBytes(), context.getCacheDir()
						.getAbsolutePath() + "/" + bean.mUrl.hashCode());
			}
			// 总页码
			bean.mTotalPage = obj.optInt("totalPages", -1);
			bean.mMessage = obj.optString("message", "");
			JSONArray array = obj.optJSONArray("result");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					String str = array.getString(i);
					bean.mTopicList.add(parseTopic(str));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析应用列表，有分页
	 */
	public static void parseAppList(Context context, PageDataBean bean,
			String json) {
		if (TextUtils.isEmpty(json)) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(json);
			bean.mStatuscode = obj.optInt("statusCode", -1);
			if (bean.mStatuscode == 0 && bean.mPageIndex == 1) {
				// 如果数据返回码为0并且数据是第一页数据，则保存数据到文件
				FileUtil.saveByteToFile(json.getBytes(), context.getCacheDir()
						.getAbsolutePath() + "/" + bean.mUrl.hashCode());
			}
			// 总页码
			bean.mTotalPage = obj.optInt("totalPages", -1);
			bean.mMessage = obj.optString("message", "");
			bean.sizeMessage = obj.optString("sizeMessage", "");
			bean.titleMessage = obj.optString("titleMessage", "");
			bean.detailMessage = obj.optString("detailMessage", "");
			bean.iconMessage = obj.optString("iconMessage", "");
			JSONArray array = obj.optJSONArray("result");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					String str = array.getString(i);
					bean.mAppList.add(parseAppData(str));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析搜索应用列表，有分页
	 */
	public static void parseSearchData(SearchDataBean bean, String json) {
		if (TextUtils.isEmpty(json)) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(json);
			bean.mStatuscode = obj.optInt("statusCode", -1);
			// 总页码
			bean.mTotalPage = obj.optInt("totalPages", -1);
			bean.mMessage = obj.optString("message", "");
			JSONArray array = obj.optJSONArray("result");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					String str;
					str = array.getString(i);
					bean.mAppList.add(parseAppData(str));

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析搜索关键词列表，没分页
	 */
	public static void parseSearchKeyData(SearchDataBean bean, String json) {
		if (TextUtils.isEmpty(json)) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(json);
			bean.mStatuscode = obj.optInt("statusCode", -1);
			bean.mMessage = obj.optString("message", "");
			JSONArray array = obj.optJSONArray("result");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					String str;
					str = array.getString(i);
					bean.mSearchKeyList.add(parseSearchKeyData(str));

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析搜索关键词对象列表
	 */
	private static String parseSearchKeyData(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			JSONObject obj = new JSONObject(json);
			return obj.optString("keyword", "");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * 解析搜索关键词推荐列表，没分页
	 */
	public static void parseSearchKeyRecommendData(SearchDataBean bean,
			String json) {
		if (TextUtils.isEmpty(json)) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(json);
			bean.mStatuscode = obj.optInt("statusCode", -1);
			bean.mMessage = obj.optString("message", "");
			JSONArray array = obj.optJSONArray("result");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					String str;
					str = array.getString(i);
					bean.mSearchKeyList.add(parseSearchKeyData(str));

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析可更新应用列表，无分页
	 */
	public static void parseUpdateAppList(Context context, PageDataBean bean,
			String json) {
		if (TextUtils.isEmpty(json)) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(json);
			bean.mStatuscode = obj.optInt("statusCode", -1);
			if (bean.mStatuscode == 0) {
				// 如果数据返回码为0，则保存数据到文件
				FileUtil.saveByteToFile(json.getBytes(), context.getCacheDir()
						.getAbsolutePath() + "/" + bean.mUrl.hashCode());
			}

			// 解析数据
			bean.mMessage = obj.optString("message", "");
			JSONArray array = obj.optJSONArray("result");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					String str = array.getString(i);
					bean.uAppList.add(parseUpdate(str));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析可更新应用
	 */
	public static UpdateAppBean parseUpdate(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			JSONObject obj = new JSONObject(json);
			UpdateAppBean bean = new UpdateAppBean();
			bean.id = obj.optLong("id", 0);
			bean.name = obj.optString("name", "");
			bean.iconUrl = obj.optString("iconUrl", "");
			bean.downloadUrl = obj.optString("downloadUrl", "");
			bean.size = obj.optInt("size", 0);
			bean.version = obj.optString("version", "");
			bean.packageName = obj.optString("packageName", "");
			return bean;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解析自动更新数据
	 */
	public static AutoUpdateBean parseAutoUpdate(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			JSONObject obj = new JSONObject(json);
			AutoUpdateBean bean = new AutoUpdateBean();
			bean.statusCode = obj.optInt("statusCode", -1);
			bean.message = obj.optString("message", "");
			JSONObject jObj = obj.optJSONObject("result");
			bean.isLatest = jObj.optBoolean("isLatest", true);
			bean.version = jObj.optString("version", "");
			bean.size = jObj.optInt("size", -1);
			bean.description = jObj.optString("description", "");
			bean.updateTime = jObj.optString("updateTime", "");
			bean.updateUrl = jObj.optString("updateUrl", "");
			bean.isMust = jObj.optBoolean("isMust", true);
			return bean;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解析错误信息
	 */
	public static ErrorDesc parseErrorDesc(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			JSONObject obj = new JSONObject(json);
			ErrorDesc ed = new ErrorDesc();
			ed.parameterName = obj.optString("parameterName", "");
			ed.errorType = obj.optInt("errorType", -1);
			return ed;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解析评论列表信息
	 */

	public static PageDataBean parseComment(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			JSONObject obj = new JSONObject(json);
			PageDataBean bean = new PageDataBean();

			bean.mStatuscode = obj.optInt("statusCode", -1);
			bean.mMessage = obj.optString("message", "");
			bean.mTotalPage = obj.optInt("totalPages", 1);

			JSONArray array = obj.optJSONArray("result");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					String str = array.getString(i);
					JSONObject jObj = new JSONObject(str);
					CommentBean b = new CommentBean();
					b.nickname = jObj.optString("nickname", "");
					b.score = jObj.optInt("score", -1);
					b.content = jObj.optString("content", "");
					b.createTime = jObj.optString("createTime", "");
					bean.commentList.add(b);
				}
			}
			return bean;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 解析发表评论信息
	 */
	public static int parsePublishData(String json) {
		int status = -2;
		if (TextUtils.isEmpty(json)) {
			return status;
		}
		try {
			JSONObject obj = new JSONObject(json);
			status = obj.optInt("statusCode", -1);
			String message = obj.optString("message", "");
			Log.i("解析发表评论信息", message);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return status;
	}

	/**
	 * 解析盒子编号数据
	 */
	public static String parseBoxIdData(String json) {
		String boxId = "";
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			JSONObject obj = new JSONObject(json);
			boxId = obj.optString("boxId", "");
			String message = obj.optString("message", "");
			Log.i("解析盒子编号数据", message);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return boxId;
	}

	/**
	 * 解析极速模式下所有应用列表数据
	 */
	public static void parseXAllList(PageDataBean bean, String json) {
		if (TextUtils.isEmpty(json)) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(json);
			bean.mDataType = PageDataBean.XALL_DATATYPE;
			bean.mStatuscode = obj.optInt("statusCode", -1);
			bean.mTotalPage = 1;
			bean.mMessage = obj.optString("errorMsg", "");
			JSONArray array = obj.optJSONArray("allApps");
			if (array != null) {
				for (int i = 0; i < array.length(); i++) {
					JSONObject app = array.getJSONObject(i);
					XAllDataBean abean = new XAllDataBean();
					abean.id = app.optInt("id", 0);
					abean.name = app.optString("name", "");
					abean.iconPath = app.optString("iconPath", "");
					abean.downPath = app.optString("downPath", "");
					abean.size = app.optInt("size", 0);
					abean.packName = app.optString("packageName", "");
					abean.version = app.optString("version", "");
					bean.mXAllList.add(abean);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析极速模式下最新推荐列表数据
	 */
	public static void parseXNewList(PageDataBean bean, String json) {
		if (TextUtils.isEmpty(json)) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(json);
			bean.mDataType = PageDataBean.XNEW_DATATYPE;
			bean.mStatuscode = obj.optInt("statusCode", 0);
			bean.mTotalPage = 1;
			bean.mMessage = obj.optString("errorMsg", "");
			JSONArray xarray = obj.optJSONArray("categorys");
			if (xarray != null) {
				JSONObject xxobj = null;
				for (int i = 0; i < xarray.length(); i++) {
					JSONObject _xxobj = xarray.optJSONObject(i);
					if (_xxobj.optBoolean("isRelatedApp")) {
						xxobj = _xxobj;
						break;
					}
				}
				if (xxobj != null) {
					JSONArray appArray = xxobj.optJSONArray("apps");
					if (appArray != null) {
						for (int i = 0; i < appArray.length(); i++) {
							JSONObject appJson = appArray.optJSONObject(i);
							if (appJson != null) {
								XAppDataBean abean = new XAppDataBean();
								abean.id = appJson.optInt("id", 0);
								abean.name = appJson.optString("name", "");
								abean.packageName = appJson.optString(
										"packageName", "");
								abean.version = appJson
										.optString("version", "");
								abean.size = appJson.optInt("size", 0);
								abean.iconPath = appJson.optString("iconPath",
										"");
								abean.downPath = appJson.optString("downPath",
										"");
								abean.score = appJson.optInt("score", 0);
								abean.downloads = appJson
										.optInt("downloads", 0);
								abean.type = appJson.optInt("type", 0);
								abean.source = appJson.optInt("source", 0);
								abean.explain = appJson
										.optString("explain", "");
								bean.mXAppList.add(abean);
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析极速模式下装机必备列表数据
	 */
	public static void parseXMustList(PageDataBean bean, String json) {
		if (TextUtils.isEmpty(json)) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(json);
			bean.mDataType = PageDataBean.XMUST_DATATYPE;
			bean.mStatuscode = obj.optInt("statusCode", 0);
			bean.mTotalPage = 1;
			bean.mMessage = obj.optString("errorMsg", "");
			JSONArray xarray = obj.optJSONArray("categorys");
			if (xarray != null) {
				JSONObject xobj = null;
				for (int i = 0; i < xarray.length(); i++) {
					JSONObject _xxobj = xarray.optJSONObject(i);
					if (!_xxobj.optBoolean("isRelatedApp")) {
						xobj = _xxobj;
						break;
					}
				}
				if (xobj != null) {
					JSONArray _xarray = xobj.optJSONArray("categories");
					for (int j = 0; j < _xarray.length(); j++) {
						JSONObject xxobj = _xarray.optJSONObject(j);
						if (xxobj != null) {
							XMustDataBean mbean = new XMustDataBean();
							mbean.name = xxobj.optString("name", "");
							if (j == 0) {
								mbean.status = 1;
							} else {
								mbean.status = 0;
							}
							JSONArray appArray = xxobj.optJSONArray("apps");
							if (appArray != null) {
								for (int i = 0; i < appArray.length(); i++) {
									JSONObject appJson = appArray
											.optJSONObject(i);
									if (appJson != null) {
										XAppDataBean abean = new XAppDataBean();
										abean.id = appJson.optInt("id", 0);
										abean.name = appJson.optString("name",
												"");
										abean.packageName = appJson.optString(
												"packageName", "");
										abean.version = appJson.optString(
												"version", "");
										abean.size = appJson.optInt("size", 0);
										abean.iconPath = appJson.optString(
												"iconPath", "");
										abean.downPath = appJson.optString(
												"downPath", "");
										abean.score = appJson
												.optInt("score", 0);
										abean.downloads = appJson.optInt(
												"downloads", 0);
										abean.type = appJson.optInt("type", 0);
										abean.source = appJson.optInt("source",
												0);
										abean.explain = appJson.optString(
												"explain", "");
										mbean.mAppList.add(abean);
									}
								}
							}
							bean.mXMustList.add(mbean);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
