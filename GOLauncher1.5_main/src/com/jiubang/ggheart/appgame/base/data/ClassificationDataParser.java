package com.jiubang.ggheart.appgame.base.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.gau.utils.cache.utils.CacheUtil;
import com.jiubang.ggheart.appgame.appcenter.help.AppCacheManager;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.ConstValue;
import com.jiubang.ggheart.apps.desks.diy.messagecenter.Beans.MessageListBean;

/**
 * 分类项数据解析类
 * 
 * @author xiedezhi
 * 
 */
public class ClassificationDataParser {

	/**
	 * 数据类型,分类数据
	 */
	private static final int DATA_TYPE_FOR_CATEGORY = 1;
	/**
	 * 数据类型,应用或专题数据
	 */
	private static final int DATA_TYPE_FOR_APP_OR_THEME = 2;
	/**
	 * 展现类型,datatype=1时，列表展现分类; datatype=2时，双栏两列
	 */
	private static final int VIEW_TYPE_FOR_LIST = 1;
	/**
	 * 展现类型,datatype=1时，tab栏展现分类; datatype=2时，格子排版(精品推荐样式)
	 */
	private static final int VIEW_TYPE_FOR_APP_OR_THEME = 2;
	/**
	 * 展现类型,datatype=1时，图标+文字并排排列展现（UI2.0的顶级tab栏新样式）;datetype=2时，一栏一列展现应用
	 */
	private static final int VIEW_TYPE_FOR_ICONTAB_OR_APP = 3;
	/**
	 * datetype=1时，按钮型TAB ;展现类型,datetype=2时，编辑推荐排版展示应用
	 */
	private static final int VIEW_TYPE_FOR_EDITOR = 4;
	/**
	 * datetype=1时，九宫格分类展示 ;展现类型,datetype=2时, coverflow样式展现应用数据 
	 */
	private static final int VIEW_TYPE_FOR_COVERFLOW = 5;
	/**
	 * datetype=2时, 九宫格展示 
	 */
	private static final int VIEW_TYPE_FOR_GRID = 6;
	/**
	 * datetype=2时, 壁纸九宫格展示 
	 */
	private static final int VIEW_TYPE_FOR_WALLPAPER_GRID = 7;
	/**
	 * datatype=2时，价格变动列表样式
	 */
	private static final int VIEW_TYPE_FOR_PRICE_ALERT = 8;
	/**
	 * datatype=2时，广告推荐位banner
	 */
	private static final int VIEW_TYPE_FOR_AD_BANNER = 9;

	/**
	 * 预加载，解析旧顶级tab栏的每个子tab栏的新数据
	 * 
	 * @return 返回分类id列表中有更新的分类数据，没有更新的不返回
	 */
	public static Map<Integer, ClassificationDataBean> parseNewSubTabData(Context context,
			int[] ids, JSONObject json, int pageid) {
		if (json == null) {
			return null;
		}
		List<Integer> originalId = new ArrayList<Integer>();
		for (int id : ids) {
			originalId.add(id);
		}
		Map<Integer, ClassificationDataBean> ret = new HashMap<Integer, ClassificationDataBean>();
		try {
			JSONObject result = json.getJSONObject(MessageListBean.TAG_RESULT);

			// 保存网络收集开关状态
			int netlog = result.getInt(MessageListBean.TAG_NETLOG);
			DownloadUtil.saveNetLog(context, AppsDetailDownload.KEY_NETLOG_MARK,
					String.valueOf(netlog));

			// 是否允许使用长连接
			int keepalive = result.optInt(MessageListBean.TAG_KEEPALIVE, -1);
			if (keepalive == 3) {
				AppHttpAdapter.getInstance(context).setAliveEnable(true);
			} else if (keepalive == 4) {
				AppHttpAdapter.getInstance(context).setAliveEnable(false);
			}
			
			

			// 保存获取数据时，服务器的时间和本地时间
			long severtime = result.optLong(MessageListBean.TAG_SEVERTIME, -1l);
			DownloadUtil.saveSerTime(context, severtime);

			int status = result.getInt(MessageListBean.TAG_STATUS);
			if (status == ConstValue.STATTUS_OK) {
				JSONObject types = json.getJSONObject(MessageListBean.TAG_TPPES);
				Iterator<String> iterator = types.keys();
				while (iterator.hasNext()) {
					String id = iterator.next();
					// Log.e("XIEDEZHI", "parseNewSubTabData id = " + id);
					JSONObject jsonBean = types.optJSONObject(id);
					int hasNew = jsonBean.optInt("hasnew", 0);
					// Log.e("XIEDEZHI", "typeid = " + id + "  hasNew = " +
					// hasNew);
					if (hasNew == 1) {
						String mark = jsonBean.optString("mark", "");
						DownloadUtil.saveMark(context,
								ClassificationDataDownload.getMarkKey() + id, mark);
						// Log.e("XIEDEZHI", "typeid = " + id + "  mark = " +
						// mark);

						// TODO:LIGUOLIANG 修改缓存管理方式
						ClassificationDataBean dataBean = parseDataBean(
								Integer.parseInt(id),
								jsonBean,
								ClassificationDataDownload.buildClassificationKey(
										Integer.parseInt(id), pageid));
						// 只返回传进来的id列表中有更新的id数据
						if (dataBean != null && originalId.contains(Integer.parseInt(id))) {
							ret.put(Integer.parseInt(id), dataBean);
						}
					}
				}
				return ret;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * 解析服务器返回的分类数据列表
	 * 
	 * @param context
	 * @param json
	 *            服务器返回的数据
	 * @param pageid
	 *            页码
	 * @return 返回的服务器下发的所有分类数据
	 */
	public static Map<Integer, ClassificationDataBean> parseData(Context context, JSONObject json, int pageid) {
		if (json == null) {
			return null;
		}
		Map<Integer, ClassificationDataBean> ret = new HashMap<Integer, ClassificationDataBean>();
		try {
			JSONObject result = json.getJSONObject(MessageListBean.TAG_RESULT);

			// 保存网络收集开关状态
			int netlog = result.getInt(MessageListBean.TAG_NETLOG);
			DownloadUtil.saveNetLog(context, AppsDetailDownload.KEY_NETLOG_MARK,
					String.valueOf(netlog));

			// 是否允许使用长连接
			int keepalive = result.optInt(MessageListBean.TAG_KEEPALIVE, -1);
			if (keepalive == 3) {
				AppHttpAdapter.getInstance(context).setAliveEnable(true);
			} else if (keepalive == 4) {
				AppHttpAdapter.getInstance(context).setAliveEnable(false);
			}

			// 保存获取数据时，服务器的时间和本地时间
			long severtime = result.optLong(MessageListBean.TAG_SEVERTIME, -1l);
			DownloadUtil.saveSerTime(context, severtime);

			int status = result.getInt(MessageListBean.TAG_STATUS);
			if (status == ConstValue.STATTUS_OK) {
				JSONObject types = json.getJSONObject(MessageListBean.TAG_TPPES);
				Iterator<String> iterator = types.keys();
				while (iterator.hasNext()) {
					String id = iterator.next();
					// Log.e("XIEDEZHI", "parseData id = " + id);
					JSONObject jsonBean = types.optJSONObject(id);
					ClassificationDataBean dataBean = parseItem(context, Integer.parseInt(id),
							jsonBean, pageid);
					if (dataBean != null) {
						ret.put(Integer.parseInt(id), dataBean);
					}
				}
				return ret;
			} else {
				Log.e("ClassificationDataParser", "result = " + result.toString());
				// 记录错误信息
				ClassificationExceptionRecord.getInstance().record("服务器数据异常：" + result.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			ClassificationExceptionRecord.getInstance().record(e);
		}
		return null;
	}

	/**
	 * 解析分类id对应的数据，如果服务器没有新数据（hasnew为0），则读本地数据
	 * 
	 * @param context
	 * @param id
	 *            分类id
	 * @param json
	 *            服务器返回的json数据
	 * @param pageid
	 *            页码
	 * @return 分类id对应的数据
	 */
	private static ClassificationDataBean parseItem(Context context, int id, JSONObject json, int pageid) {
		if (json == null) {
			return null;
		}
		// TODO:LIGUOLIANG 修改缓存管理方式
		try {
			String key = ClassificationDataDownload.buildClassificationKey(id, pageid);
			int hasNew = json.getInt("hasnew");
			// Log.e("XIEDEZHI", "typeid(" + id + ") hasNew = " + hasNew);

			if (hasNew == 1) {
				// 服务器有更新数据
				DownloadUtil.saveMark(context, ClassificationDataDownload.getMarkKey() + id,
						json.optString("mark", ""));
				return parseDataBean(id, json, key);
			} else {

				byte[] cacheData = AppCacheManager.getInstance().loadCache(key);
				if (cacheData == null) {
					return null;
				}
				JSONObject obj = CacheUtil.byteArrayToJson(cacheData);
				if (obj != null) {
					return parseDataBean(id, obj, null);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			ClassificationExceptionRecord.getInstance().record(e);
			return null;
		}
		return null;
	}

	/**
	 * 
	 * @param id
	 *            数据所属的分类id
	 * @param json
	 *            服务器下发或者本地读取出来的Json数据
	 * @param saveFilePath
	 *            缓存数据的key值，如果不需要保存则传null
	 * @return 分类id对应的数据
	 */
	public static ClassificationDataBean parseDataBean(int id, JSONObject json, String key) {
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
		int datatype = json.optInt("datatype", Integer.MIN_VALUE);
		int viewtype = json.optInt("viewtype", Integer.MIN_VALUE);
		int feature = json.optInt("feature", Integer.MIN_VALUE);
		String module = ClassificationDataDownload.buildClassficationCacheModule(id);
		String extra = ClassificationDataDownload.buildClassificationCacheExtra(id, pageid);
		// 标题栏展现的位置，1：靠顶部2：靠底部（仅在datatype为1，viewtype为3时有效），根据UI2.0新增
		int viewlocal = json.optInt("viewlocal", 1);
		String summary = json.optString("summary", "");
		ret.summary = summary;
		ret.showlist = json.optInt("showlist", 0);
		ret.filter = json.optInt("filter", Integer.MIN_VALUE);
		ret.versize = json.optInt("versize", Integer.MIN_VALUE);
		AppCacheManager acm = AppCacheManager.getInstance();
		if (key != null) {
			// 如果是分页数据(双栏两列专题，一栏一列专题，编辑推荐，精品推荐)并且第一页有更新，删除该分类id所有的本地数据
			if (datatype == DATA_TYPE_FOR_APP_OR_THEME
					&& (viewtype == VIEW_TYPE_FOR_LIST || viewtype == VIEW_TYPE_FOR_APP_OR_THEME
							|| viewtype == VIEW_TYPE_FOR_ICONTAB_OR_APP
							|| viewtype == VIEW_TYPE_FOR_EDITOR || viewtype == VIEW_TYPE_FOR_GRID
							|| viewtype == VIEW_TYPE_FOR_WALLPAPER_GRID || viewtype == VIEW_TYPE_FOR_PRICE_ALERT)) {
				if (pageid == 1) {
					// 专题数据，有分页					
					// TODO:LIGUOLIANG 修改缓存管理方式
					if (acm.isCacheExist(key)) {
						// 本地有旧数据,先删除,需要清除所有页的数据						
						// 获取分页数据的key值
						List<String> keyList = acm.getModuleKeyList(module);
						// 清除分页数据缓存值
						acm.clearCache(keyList);
						// 清除键值
						acm.clearModuleKeyList(module);
					}
				}
				// 分页数据需要缓存Key值
				acm.saveModuleKey(module, extra);
			} else {
				// 不是专题，默认没有分页，只读取第一页的内容
				pageid = 1;
			}
			// 先清除旧的缓存
			if (acm.isCacheExist(key)) {
				acm.clearCache(key);
			}
			// 缓存新的数据
			acm.saveCache(key, json.toString().getBytes());
		}
		// TODO:XIEDEZHI feature字段已经移到分类信息单元，分类项单元的feature字段已经没意义
		switch (feature) {
			case 0 :
				// 默认
				break;
			case CategoriesDataBean.FEATURE_FOR_YJZWJ :
				// 一键装机
				ret.dataType = ClassificationDataBean.YJZJ_TYPE;
				return ret;
			case CategoriesDataBean.FEATURE_FOR_GAME_AND_APP :
				// 我的应用
				ret.dataType = ClassificationDataBean.MY_APP_TYPE;
				return ret;
			case CategoriesDataBean.FEATURE_FOR_APP_UPDATE :
				// 应用更新
				ret.dataType = ClassificationDataBean.UPDATE_APP_TYPE;
				return ret;
			case CategoriesDataBean.FEATURE_FOR_SEARCH :
				// 搜索
				ret.dataType = ClassificationDataBean.SEARCH_TYPE;
				return ret;
			case CategoriesDataBean.FEATURE_FOR_MANAGEMENT :
				// 本地管理
				ret.dataType = ClassificationDataBean.MANAGEMENT_TYPE;
				return ret;
			default :
				Log.e("ClassificationDataParser", "parseDataBean bad feature = " + feature);
				return null;
		}

		if (datatype == DATA_TYPE_FOR_CATEGORY) {
			// 分类数据
			try {
				JSONArray array = json.getJSONArray("typedata");
				if (viewtype == VIEW_TYPE_FOR_LIST) {
					// 列表展现分类
					ret.dataType = ClassificationDataBean.CATEGORIES_TYPE;
					ret.categoriesList = CategoriesDataParser.parseCategoriesBean(array);
					return ret;
				} else if (viewtype == VIEW_TYPE_FOR_APP_OR_THEME) {
					// tab栏展现分类
					ret.dataType = ClassificationDataBean.TAB_TYPE;
					ret.categoriesList = CategoriesDataParser.parseCategoriesBean(array);
					return ret;
				} else if (viewtype == VIEW_TYPE_FOR_ICONTAB_OR_APP) {
					// 标题栏用图标加文字展现（根据UI2.0新增）
					ret.dataType = ClassificationDataBean.ICON_TAB_TYPE;
					ret.viewlocal = viewlocal;
					ret.categoriesList = CategoriesDataParser.parseCategoriesBean(array);
					return ret;
				} else if (viewtype == VIEW_TYPE_FOR_EDITOR) {
					// 按钮型TAB
					ret.dataType = ClassificationDataBean.BUTTON_TAB;
					ret.viewlocal = viewlocal;
					ret.categoriesList = CategoriesDataParser.parseCategoriesBean(array);
					return ret;
				} else if (viewtype == VIEW_TYPE_FOR_COVERFLOW) {
					// 九宫格类别显示
					ret.dataType = ClassificationDataBean.GRID_SORT;
					ret.viewlocal = viewlocal;
					ret.categoriesList = CategoriesDataParser.parseCategoriesBean(array);
					return ret;
				} else {
					Log.e("ClassificationDataParser", "parseDataBean bad viewtype = " + viewtype);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				ClassificationExceptionRecord.getInstance().record(e);
			}
		} else if (datatype == DATA_TYPE_FOR_APP_OR_THEME) {
			// 应用或专题数据
			try {
				JSONArray array = json.getJSONArray("appdata");
				if (viewtype == VIEW_TYPE_FOR_LIST) {
					// 双栏两列展示应用
					ret.dataType = ClassificationDataBean.SPECIALSUBJECT_TYPE;
					ret.featureList = FeatureDataParser.parseFeatureApp(ret.typeId, array);
					ret.pages = pages;
					ret.pageid = pageid;
					return ret;
				} else if (viewtype == VIEW_TYPE_FOR_APP_OR_THEME) {
					// 精品推荐数据
					ret.dataType = ClassificationDataBean.FEATURE_TYPE;
					ret.featureList = FeatureDataParser.parseFeatureApp(ret.typeId, array);
					ret.pages = pages;
					ret.pageid = pageid;
					return ret;
				} else if (viewtype == VIEW_TYPE_FOR_ICONTAB_OR_APP) {
					// 一栏一列展示应用
					ret.dataType = ClassificationDataBean.ONEPERLINE_SPECIALSUBJECT_TYPE;
					ret.featureList = FeatureDataParser.parseFeatureApp(ret.typeId, array);
					ret.pages = pages;
					ret.pageid = pageid;
					return ret;
				} else if (viewtype == VIEW_TYPE_FOR_EDITOR) {
					// 编辑推荐样式展示应用
					ret.dataType = ClassificationDataBean.EDITOR_RECOMM_TYPE;
					ret.featureList = FeatureDataParser.parseFeatureApp(ret.typeId, array);
					ret.pages = pages;
					ret.pageid = pageid;
					return ret;
				} else if (viewtype == VIEW_TYPE_FOR_COVERFLOW) {
					// coverflow样式展现应用数据
					ret.dataType = ClassificationDataBean.COVER_FLOW;
					ret.featureList = FeatureDataParser.parseFeatureApp(ret.typeId, array);
					return ret;
				} else if (viewtype == VIEW_TYPE_FOR_PRICE_ALERT) {
					// 价格变动列表
					ret.dataType = ClassificationDataBean.PRICE_ALERT;
					ret.featureList = FeatureDataParser.parseFeatureApp(ret.typeId, array);
					ret.pages = pages;
					ret.pageid = pageid;
					return ret;
				} else if (viewtype == VIEW_TYPE_FOR_GRID) {
					// 九宫格列表
					ret.dataType = ClassificationDataBean.GRID_TYPE;
					ret.featureList = FeatureDataParser.parseFeatureApp(ret.typeId, array);
					ret.pages = pages;
					ret.pageid = pageid;
					return ret;
				} else if (viewtype == VIEW_TYPE_FOR_WALLPAPER_GRID) {
					// 壁纸九宫格列表
					ret.dataType = ClassificationDataBean.WALLPAPER_GRID;
					ret.featureList = FeatureDataParser.parseFeatureApp(ret.typeId, array);
					ret.pages = pages;
					ret.pageid = pageid;
					return ret;
				} else if (viewtype == VIEW_TYPE_FOR_AD_BANNER) {
					// 广告推荐位展现样式
					ret.dataType = ClassificationDataBean.AD_BANNER;
					ret.featureList = FeatureDataParser.parseFeatureApp(ret.typeId, array);
					return ret;
				} else {
					Log.e("ClassificationDataParser", "parseDataBean bad viewtype = " + viewtype);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				ClassificationExceptionRecord.getInstance().record(e);
			}
		} else {
			Log.e("ClassificationDataParser", "parseDataBean bad datatype = " + datatype);
		}
		return null;
	}
	
	/**
	 * 从字符串中获取数据的数据类型，如果获取失败返回Integer.MIN_VALUE
	 */
	public static int getDataTypeFromString(String src) {
		// TODO：XIEDEZHI 这里的读取采用非常规的方法，如果服务器下发的JSON结构有改动，这个方法可能需要修改
		try {
			if (src == null) {
				return Integer.MIN_VALUE;
			}
			// 先把应用专题数据（appdata）和分类数据（typedata）删掉，避免出现多个“datatype”和"viewtype"
			while (true) {
				int index1 = src.lastIndexOf("[");
				int index2 = src.indexOf("]");
				if (index1 == -1 || index2 == -1 || index2 <= index1) {
					break;
				}
				String head = src.substring(0, index1);
				String end = src.substring(index2 + 1);
				src = head + end;
			}
			int t = src.indexOf("\"datatype\"");
			int g = src.lastIndexOf("\"datatype\"");
			if (t != g || t == -1 || g == -1) {
				return Integer.MIN_VALUE;
			}
			int x = src.indexOf(":", t);
			int y = src.indexOf(",", t);
			String datatypeStr = src.substring(x + 1, y);
			int datatype = Integer.parseInt(datatypeStr);

			t = src.indexOf("\"viewtype\"");
			g = src.lastIndexOf("\"viewtype\"");
			if (t != g || t == -1 || g == -1) {
				return Integer.MIN_VALUE;
			}
			x = src.indexOf(":", t);
			y = src.indexOf(",", t);
			String viewtypeStr = src.substring(x + 1, y);
			int viewtype = Integer.parseInt(viewtypeStr);

			if (datatype == DATA_TYPE_FOR_CATEGORY) {
				// 分类数据
				if (viewtype == VIEW_TYPE_FOR_LIST) {
					// 列表展现分类
					return ClassificationDataBean.CATEGORIES_TYPE;
				} else if (viewtype == VIEW_TYPE_FOR_APP_OR_THEME) {
					// tab栏展现分类
					return ClassificationDataBean.TAB_TYPE;
				} else if (viewtype == VIEW_TYPE_FOR_ICONTAB_OR_APP) {
					// 标题栏用图标加文字展现（根据UI2.0新增）
					return ClassificationDataBean.ICON_TAB_TYPE;
				} else if (viewtype == VIEW_TYPE_FOR_EDITOR) {
					// 按钮型TAB
					return ClassificationDataBean.BUTTON_TAB;
				} else if (viewtype == VIEW_TYPE_FOR_COVERFLOW) {
					// 九宫格类别显示
					return ClassificationDataBean.GRID_SORT;
				}
			} else if (datatype == DATA_TYPE_FOR_APP_OR_THEME) {
				// 应用或专题数据
				if (viewtype == VIEW_TYPE_FOR_LIST) {
					// 双栏两列展示应用
					return ClassificationDataBean.SPECIALSUBJECT_TYPE;
				} else if (viewtype == VIEW_TYPE_FOR_APP_OR_THEME) {
					// 精品推荐数据
					return ClassificationDataBean.FEATURE_TYPE;
				} else if (viewtype == VIEW_TYPE_FOR_ICONTAB_OR_APP) {
					// 一栏一列展示应用
					return ClassificationDataBean.ONEPERLINE_SPECIALSUBJECT_TYPE;
				} else if (viewtype == VIEW_TYPE_FOR_EDITOR) {
					// 编辑推荐样式展示应用
					return ClassificationDataBean.EDITOR_RECOMM_TYPE;
				} else if (viewtype == VIEW_TYPE_FOR_COVERFLOW) {
					// coverflow样式展现应用数据
					return ClassificationDataBean.COVER_FLOW;
				} else if (viewtype == VIEW_TYPE_FOR_PRICE_ALERT) {
					// 价格变动列表
					return ClassificationDataBean.PRICE_ALERT;
				} else if (viewtype == VIEW_TYPE_FOR_GRID) {
					// 九宫格列表
					return ClassificationDataBean.GRID_TYPE;
				} else if (viewtype == VIEW_TYPE_FOR_WALLPAPER_GRID) {
					// 壁纸九宫格列表
					return ClassificationDataBean.WALLPAPER_GRID;
				} else if (viewtype == VIEW_TYPE_FOR_AD_BANNER) {
					// 广告推荐位展现样式
					return ClassificationDataBean.AD_BANNER;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Integer.MIN_VALUE;
	}
	
	/**
	 * 获取分类id对应本地数据中所有的子分类id
	 */
	public static List<LocalJSON> getLocalSubTypeidList(int typeId) {
//		long t1 = System.currentTimeMillis();
		LocalJSON localJson = new LocalJSON();
		localJson.mTypeId = typeId;
		List<LocalJSON> typeIds = new ArrayList<LocalJSON>();
		typeIds.add(localJson);
		// 本地数据的key值，页码为1
		String key = ClassificationDataDownload.buildClassificationKey(typeId, 1);
		AppCacheManager acm = AppCacheManager.getInstance();
		if (acm.isCacheExist(key)) {
			byte[] cacheData = AppCacheManager.getInstance().loadCache(key);
			if (cacheData != null) {
				JSONObject obj = CacheUtil.byteArrayToJson(cacheData);
				if (obj != null) {
					localJson.mJson = obj;
					int datatype = obj.optInt("datatype", Integer.MIN_VALUE);
					int viewtype = obj.optInt("viewtype", Integer.MIN_VALUE);
					//如果分类id下面还有子分类id，把子分类id也读出来
					if (datatype == DATA_TYPE_FOR_CATEGORY
							&& (viewtype == VIEW_TYPE_FOR_APP_OR_THEME
									|| viewtype == VIEW_TYPE_FOR_ICONTAB_OR_APP || viewtype == VIEW_TYPE_FOR_EDITOR)) {
						try {
							JSONArray array = obj.getJSONArray("typedata");
							for (int i = 0; i < array.length(); i++) {
								JSONObject json = array.getJSONObject(i);
								int id = json.optInt("typeid", Integer.MIN_VALUE);
								List<LocalJSON> list = getLocalSubTypeidList(id);
								if (list != null) {
									typeIds.addAll(list);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
//		long t2 = System.currentTimeMillis();
//		Log.e("XIEDEZHI", "getLocalSubTypeidList 耗时：" + (t2 - t1) + "ms");
		return typeIds;
	}
	
	/**
	 * 
	 * 本地分类id对应的JSON数据
	 * 
	 * @author  xiedezhi
	 * @date  [2013-1-21]
	 */
	public static class LocalJSON {
		/**
		 * 分类id
		 */
		public int mTypeId;
		/**
		 * 本地JSON数据
		 */
		public JSONObject mJson;
	}
}
