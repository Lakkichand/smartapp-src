package com.jiubang.ggheart.apps.gowidget.gostore.cache;

import java.net.URISyntaxException;
import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.apps.gowidget.gostore.cache.parser.ICacheParser;
import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;
import com.jiubang.ggheart.apps.gowidget.gostore.net.MainDataHttpOperator;
import com.jiubang.ggheart.apps.gowidget.gostore.net.SimpleHttpAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.net.ThemeHttp;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.BaseBean;
import com.jiubang.ggheart.apps.gowidget.gostore.net.databean.UpdateCheckBean;
import com.jiubang.ggheart.data.statistics.StatisticsData;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * <br>类描述:旧的缓存管理，Go精品部分已经不用，请使用GoStoreCacheManger
 * <br>功能详细描述:
 * 
 * @author  huyong
 * @date  [2012-9-26]
 */
public class CacheManager {

	// 缓存文件路径
	private final static String MAINVIEW_CACHE_FILE_PATH = GoStorePublicDefine.GOSTORE_VIEW_CACHE_FILE_PATH
			+ "/MainViewCache.xml";
	private final static String THEMELISTVIEW_CACHE_FILE_PATH = GoStorePublicDefine.GOSTORE_VIEW_CACHE_FILE_PATH
			+ "/ThemeListViewCache.xml";
	private final static String SEARCHRESULTVIEW_CACHE_FILE_PATH = GoStorePublicDefine.GOSTORE_VIEW_CACHE_FILE_PATH
			+ "/SearchResultViewCache.xml";
	private final static String ITEMDETAIL_CACHE_FILE_PATH = GoStorePublicDefine.GOSTORE_VIEW_CACHE_FILE_PATH
			+ "/ItemDetailViewCache.xml";
	private final static String SEARCHKEYS_CACHE_FILE_PATH = GoStorePublicDefine.GOSTORE_VIEW_CACHE_FILE_PATH
			+ "/SearchKeysViewCache.xml";
	private final static String SORT_CACHE_FILE_PATH = GoStorePublicDefine.GOSTORE_VIEW_CACHE_FILE_PATH
			+ "/SortViewCache.xml";
	private final static String WALLPAPER_LIST_CACHE_FILE_PATH = GoStorePublicDefine.GOSTORE_VIEW_CACHE_FILE_PATH
			+ "/WallpaperListCache.xml";
	// 检查更新的状态
	public static final int STATE_NEW = 0;
	public static final int STATE_START = 1;
	public static final int STATE_CHECKING = 2;
	public static final int STATE_FINISH = 3;

	// Handler的what值
	private final static int UPDATE_CHECK_FINISH = 0;

	private final static String GOSTORE_UPATE_CHECEK_FILE_NAME = "gostore_upate_checek_file_name"; // 保存更新验证信息的文件名称
	private final static String TIMESTAMP_KEY = "timestamp_key"; // 时间戳键值
	private static CacheManager sSelf = null;
	private Context mContext = null;
	public static int sCheckState = STATE_NEW; // 进行更新检查的状态
	public static boolean sIsServerDataUpdate = true; // 服务器数据是否有更新的标识
	public static boolean sIsFirst = false; //若时间戳不存在则视为第一次进入
	private ICacheParser mCacheParser = null;
	private Handler mHandler = null;
	private ArrayList<ICacheCallBack> mCacheCallBacks = null;

	public synchronized static void build(Context context, ICacheParser cacheParser,
			ICacheCallBack cacheCallBack) {
		if (null == sSelf) {
			sSelf = new CacheManager(context, cacheParser, cacheCallBack);
		}
	}

	public static CacheManager getInstance() {
		return sSelf;
	}

	public void checkViewDataUpdate() {
		if (mContext != null) {
			// 设置状态
			sCheckState = STATE_START;

			byte[] postData = ThemeHttp.getPostData(mContext, null,
					GoStorePublicDefine.FUNID_UPATE_CHECK);

			THttpRequest request;
			try {
				request = new THttpRequest(GoStorePublicDefine.URL_HOST3, postData,
						new IConnectListener() {

							@Override
							public void onStart(THttpRequest request) {
								// 设置状态
								sCheckState = STATE_CHECKING;
								sIsFirst = false;
							}

							@SuppressWarnings("unchecked")
							@Override
							public void onFinish(THttpRequest request, IResponse response) {
								// TODO Auto-generated method stub
								if (response.getResponseType() == IResponse.RESPONSE_TYPE_STREAM) {
									ArrayList<BaseBean> baseBeans = (ArrayList<BaseBean>) response
											.getResponse();
									if (baseBeans != null && baseBeans.size() > 0) {
										if (baseBeans.get(0) instanceof UpdateCheckBean) {
											UpdateCheckBean updateCheckBean = (UpdateCheckBean) baseBeans
													.get(0);
											if (mContext != null && updateCheckBean != null
													&& updateCheckBean.mLength > 0
													&& updateCheckBean.mUpdateTimestamp > 0) {
												PreferencesManager sharedPreferences = new PreferencesManager(
														mContext, GOSTORE_UPATE_CHECEK_FILE_NAME,
														Context.MODE_PRIVATE);
												if (sharedPreferences != null) {
													// 本地记录的时间戳
													Long timestamp = sharedPreferences.getLong(
															TIMESTAMP_KEY, -1);
													//保存统计用时间
													DownloadUtil.saveSerTime(mContext,
															updateCheckBean.mTimeStamp);
													if (timestamp > 0
															&& timestamp == updateCheckBean.mUpdateTimestamp) {
														// 如果有本地记录时间戳,并且跟服务器下发的时间戳相等
														// 则认为服务器没有更新数据，可使用缓存
														sIsServerDataUpdate = false;
													} else {
														// 如果没有本地时间戳或者本地时间戳与服务器下发的不相等
														// 则把服务器下发的时间戳保存到本地
														if (timestamp == -1) {
															sIsFirst = true;
														}
														sharedPreferences.putLong(TIMESTAMP_KEY,
																updateCheckBean.mUpdateTimestamp);
														sharedPreferences.commit();
													}
												}
											}
										}
									}
								}
								// 设置状态
								sCheckState = STATE_FINISH;

								if (mHandler != null) {
									mHandler.sendEmptyMessage(UPDATE_CHECK_FINISH);
								}
								if (sIsServerDataUpdate) {
									// 如果服务器有更新
									// 清空旧的缓存数据
									if (mCacheParser != null) {
										// TODO:LIGUOLIANG 现在就清除数据了,如果从网络取数据失败了呢??应该在网络取到数据后再清除之前的缓存数据才对吧??							
										mCacheParser.cleanAllCacheData(mContext);
									}
								}
							}

							@Override
							public void onException(THttpRequest request, int reason) {
								StatisticsData.saveHttpExceptionDate(mContext, request, reason);
								if (mHandler != null) {
									mHandler.sendEmptyMessage(UPDATE_CHECK_FINISH);
								}
							}
						});
				// 设置POST请求头
				request.addHeader("Content-Type", LauncherEnv.Url.POST_CONTENT_TYPE);

				MainDataHttpOperator operator = new MainDataHttpOperator();
				request.setOperator(operator);

				SimpleHttpAdapter.getInstance(mContext).addTask(request);

			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 保存页面请求缓存数据的方法
	 * 
	 * @param viewType
	 *            View类型
	 * @param request
	 *            缓存数据对应的THttpRequest
	 * @param object
	 *            要缓存的数据
	 */
	public void saveViewCacheData(int viewType, THttpRequest request, Object object) {
		if (mCacheParser != null) {
			String filePath = getFilePathByViewType(viewType);
			if (filePath != null && !"".equals(filePath.trim())) {
				mCacheParser.saveCacheData(mContext, filePath, request, object);
			}
		}
	}

	/**
	 * 获取页面请求缓存数据的方法
	 * 
	 * @param viewType
	 *            View类型
	 * @param request
	 *            缓存数据对应的THttpRequest
	 * @return
	 */
	public Object getViewCacheData(int viewType, THttpRequest request) {
		Object object = null;
		if (mCacheParser != null) {
			String filePath = getFilePathByViewType(viewType);
			if (filePath != null && !"".equals(filePath.trim())) {
				object = mCacheParser.getCacheData(mContext, filePath, request);
			}
		}
		return object;
	}

	/**
	 * 清空所有缓存文件的方法
	 */
	public void cleanAllViewCacheData() {
		if (mCacheParser != null) {
			mCacheParser.cleanAllCacheData(mContext);
		}
	}

	/**
	 * 通过View类型获取缓存数据保存路径的方法
	 * 
	 * @param viewType
	 * @return
	 */
	private String getFilePathByViewType(int viewType) {
		String filePath = null;
		switch (viewType) {
			case GoStorePublicDefine.VIEW_TYPE_MAIN : {
				filePath = MAINVIEW_CACHE_FILE_PATH;
			}
				break;
			case GoStorePublicDefine.VIEW_TYPE_TAB_LIST : {
				filePath = THEMELISTVIEW_CACHE_FILE_PATH;
			}
				break;
			case GoStorePublicDefine.VIEW_TYPE_SEARCH_RESULT : {
				filePath = SEARCHRESULTVIEW_CACHE_FILE_PATH;
			}
				break;
			case GoStorePublicDefine.VIEW_TYPE_ITEMDETAIL : {
				filePath = ITEMDETAIL_CACHE_FILE_PATH;
			}
				break;
			case GoStorePublicDefine.VIEW_TYPE_SEARCH_INPUT : {
				filePath = SEARCHKEYS_CACHE_FILE_PATH;
			}
				break;
			case GoStorePublicDefine.VIEW_TYPE_SORT : {
				filePath = SORT_CACHE_FILE_PATH;
			}
				break;
			case GoStorePublicDefine.VIEW_TYPE_WALLPAPER_DATA_LIST : {
				filePath = WALLPAPER_LIST_CACHE_FILE_PATH;
			}
				break;
			default :
				break;
		}
		return filePath;
	}

	/**
	 * 初始化Handler的方法
	 */
	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				if (msg != null) {
					int what = msg.what;
					switch (what) {
						case UPDATE_CHECK_FINISH : {
							// if(sIsServerDataUpdate){
							// Toast.makeText(mContext, "服务器有更新，清除缓存数据",
							// Toast.LENGTH_LONG).show();
							// }else {
							// Toast.makeText(mContext, "服务器没有更新",
							// Toast.LENGTH_LONG).show();
							// }
							if (mCacheCallBacks != null) {
								for (ICacheCallBack cacheCallBack : mCacheCallBacks) {
									if (cacheCallBack != null) {
										cacheCallBack.onUpdateCheckFinish();
									}
								}
							}
						}
							break;

						default :
							break;
					}
				}
			}
		};
	}

	/**
	 * 添加回调的方法
	 * 
	 * @param cacheCallBack
	 */
	public void addCallBack(ICacheCallBack cacheCallBack) {
		if (cacheCallBack != null) {
			if (null == mCacheCallBacks) {
				mCacheCallBacks = new ArrayList<ICacheCallBack>();
			}
			if (!mCacheCallBacks.contains(cacheCallBack)) {
				mCacheCallBacks.add(cacheCallBack);
			}
		}
	}

	private CacheManager(Context context, ICacheParser cacheParser, ICacheCallBack cacheCallBack) {
		mContext = context;
		mCacheParser = cacheParser;
		mCacheCallBacks = new ArrayList<ICacheCallBack>();
		if (cacheCallBack != null) {
			mCacheCallBacks.add(cacheCallBack);
		}
		initHandler();
	}

	private void cleanUp() {
		mContext = null;
		mCacheParser = null;
		if (mCacheCallBacks != null) {
			mCacheCallBacks.clear();
			mCacheCallBacks = null;
		}
		mHandler = null;
	}

	public static synchronized void destory() {
		if (sSelf != null) {
			sSelf.cleanUp();
			sSelf = null;
		}
	}
}
