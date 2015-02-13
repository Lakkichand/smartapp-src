package com.zhidian.wifibox.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.zhidian.wifibox.R;
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
			JSONArray thumbs = obj.optJSONArray("thumbnailUrls");
			if (thumbs != null) {
				for (int i = 0; i < thumbs.length(); i++) {
					bean.thumbUrls.add(thumbs.getString(i));
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
			bean.type = obj.optInt("type", 1);
			bean.otherTarget = obj.optString("otherTarget", "");
			return bean;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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
			bean.AppSize = obj.optString("size", "");
			bean.amount = obj.optInt("amount", -1);
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
	 * 解析首页推荐
	 */
	public static void parseHomeFeatureList(Context context, PageDataBean bean,
			String json) {
		if (TextUtils.isEmpty(json)) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(json);
			bean.mStatuscode = obj.optInt("statusCode", -1);
			if (bean.mStatuscode == 0) {
				// 如果数据返回码为0并且数据是第一页数据，则保存数据到文件
				FileUtil.saveByteToFile(json.getBytes(), context.getCacheDir()
						.getAbsolutePath() + "/" + bean.mUrl.hashCode());
			}
			bean.mDataType = PageDataBean.HOME_FEATURE_DATATYPE;
			bean.mHomeFeatureDataBean = new HomeFeatureDataBean();
			JSONObject result = obj.optJSONObject("result");
			if (result != null) {
				JSONArray slides = result.optJSONArray("slides");
				for (int i = 0; i < slides.length(); i++) {
					BannerDataBean banner = parseBanner(slides.getString(i));
					if (banner != null) {
						bean.mHomeFeatureDataBean.mBannerList.add(banner);
					}
				}
				JSONArray tags = result.optJSONArray("tags");
				if (tags != null) {
					for (int i = 0; i < tags.length(); i++) {
						JSONObject tObj = tags.optJSONObject(i);
						if (tObj != null) {
							int type = tObj.optInt("type");
							String title = tObj.optString("title", "");
							String content = tObj.optString("content", "");
							String tag = tObj.optString("tag", "");
							int j = i % 4;
							int background = 0;
							if (j == 0) {
								background = R.drawable.navigation_blue_selector;
							} else if (j == 1) {
								background = R.drawable.navigation_yellow_selector;
							} else if (j == 2) {
								background = R.drawable.navigation_green_selector;
							} else {
								background = R.drawable.navigation_red_selector;
							}

							bean.mHomeFeatureDataBean.mIdList.add(content);
							bean.mHomeFeatureDataBean.mTypeList.add(type);
							bean.mHomeFeatureDataBean.mTitleList.add(title);
							bean.mHomeFeatureDataBean.mTagList.add(tag);
							bean.mHomeFeatureDataBean.mBackGroundList
									.add(background);
						}
					}
				}
				JSONArray softwares = result.optJSONArray("softwares");
				for (int i = 0; i < softwares.length(); i++) {
					AppDataBean app = parseAppData(softwares.getString(i));
					if (app != null) {
						bean.mHomeFeatureDataBean.mAppList.add(app);
					}
				}
				JSONArray games = result.optJSONArray("games");
				for (int i = 0; i < games.length(); i++) {
					AppDataBean game = parseAppData(games.getString(i));
					if (game != null) {
						bean.mHomeFeatureDataBean.mGameList.add(game);
					}
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
			String result = obj.optString("result");
			if (result != null) {
				JSONObject obj2 = new JSONObject(result);
				JSONArray apps = obj2.optJSONArray("apps");
				for (int i = 0; i < apps.length(); i++) {
					String str;
					str = apps.getString(i);
					bean.mAppList.add(parseAppData(str));
				}

				JSONObject specials = obj2.optJSONObject("specials");
				if (specials != null) {

					bean.mTopicDataList.add(parseSearchTopicData(specials));

				}

			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 解析搜索专题数据
	 */

	private static TopicDataBean parseSearchTopicData(JSONObject obj) {

		TopicDataBean bean = new TopicDataBean();
		bean.id = obj.optLong("id", 0);
		bean.title = obj.optString("title", "");
		bean.bannerUrl = obj.optString("iconUrl", "");
		bean.description = obj.optString("description", "");
		bean.message = obj.optString("amount", "");
		return bean;

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
					bean.mAutoSearchKeyList.add(parseAutoSearchKey(str));

				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析自动搜索列表
	 * 
	 * @param json
	 * @return
	 */
	private static AppDataBean parseAutoSearchKey(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}

		try {
			JSONObject obj = new JSONObject(json);
			AppDataBean bean = new AppDataBean();
			bean.id = obj.optLong("id", -1);
			bean.downloadUrl = obj.optString("downloadUrl", "");
			bean.name = obj.optString("name", "");
			bean.iconUrl = obj.optString("iconUrl", "");
			bean.packName = obj.optString("packageName", "");
			bean.size = obj.optInt("size", -1);
			bean.version = obj.optString("version", "");

			return bean;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
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
	 * 解析极速模式下最新推荐列表数据
	 */
	public static void parseXNewList(PageDataBean bean, String json) {
		if (TextUtils.isEmpty(json)) {
			return;
		}
		try {
			JSONObject obj = new JSONObject(json);
			bean.mDataType = PageDataBean.SPEEDINGDOWNLOAD_DATATYPE;
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
			bean.mDataType = PageDataBean.SPEEDINGDOWNLOAD_DATATYPE;
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

	/**
	 * 解析活动弹窗推荐
	 * 
	 * @param json
	 * @return
	 */
	public static PopupCommend parsePopupCommendData(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			JSONObject obj = new JSONObject(json);
			PopupCommend pop = new PopupCommend();
			int statusCode = obj.optInt("statusCode", -1);
			String message = obj.optString("message", "");
			Log.i("解析弹窗推荐", message);
			if (statusCode == 0) {
				JSONObject result = obj.optJSONObject("result");
				pop = parsePopupDetail(result);
				return pop;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static PopupCommend parsePopupDetail(JSONObject obj) {
		PopupCommend pop = new PopupCommend();
		pop.imageUrl = obj.optString("imageUrl", "");
		pop.type = obj.optInt("type", -1);
		pop.target = obj.optString("target", "");
		pop.title = obj.optString("title", "");
		return pop;
	}

	/**
	 * 解析门店广告数据
	 */
	public static AdvertisementBean parseAdvertisementData(String json) {
		if (TextUtils.isEmpty(json)) {
			return null;
		}
		try {
			JSONObject obj = new JSONObject(json);
			int statusCode = obj.optInt("statusCode", -1);
			String message = obj.optString("message", "");
			if (statusCode == 0) {
				JSONObject result = obj.optJSONObject("result");
				AdvertisementBean bean = new AdvertisementBean();
				bean.title = result.getString("title");
				bean.content = result.getString("content");
				bean.httpUrl = result.getString("httpUrl");
				return bean;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
