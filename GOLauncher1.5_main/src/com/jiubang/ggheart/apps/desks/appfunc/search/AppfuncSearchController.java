package com.jiubang.ggheart.apps.desks.appfunc.search;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.gau.go.launcherex.R;
import com.go.util.file.media.FileInfo;
import com.jiubang.ggheart.appgame.base.bean.HotSearchKeyword;
import com.jiubang.ggheart.appgame.base.utils.SearchKeywordUtil;
import com.jiubang.ggheart.appgame.base.utils.SearchKeywordUtil.DataHandler;
import com.jiubang.ggheart.apps.desks.appfunc.model.FuncSearchResultItem;
import com.jiubang.ggheart.apps.gowidget.gostore.views.BroadCaster.BroadCasterObserver;
/**
 * 
 * <br>类描述: 功能表搜索控制层
 * <br>功能详细描述:接收搜索请求与返回搜索结果
 * 
 * @author  dingzijian
 * @date  [2012-10-17]
 */
public class AppfuncSearchController implements BroadCasterObserver {

	public static final int BC_MSG_APPFUNC_FRAME_START = 0x500;
	public static final int BC_MSG_APPFUNC_FRAME_END = 0x501;
	public static final int BC_MSG_UPDATE_KEYWORDS = 0x502;
	public static final int BC_MSG_SEARCH_LOACL_RESOURCE = 0x503;
	public static final int BC_MSG_SEARCH_WEB_APPS = 0x504;
	public static final int BC_MSG_CHECK_HISTORY_LOCAL = 0x505;
	public static final int BC_MSG_CHECK_HISTORY_GOSOTRE = 0x506;
	public static final int BC_MSG_SAVE_HISTORY = 0X507;
	public static final int BC_MSG_CLEAR_HISTORY = 0X508;
	public static final int BC_MSG_LOAD_MORE_WEB_APPS = 0x509;
	public static final int BC_MSG_SEARCH_ENGINE_MEDIA_DATA_READY = 0x511;

	private Context mContext;
	private Handler mHandler;
	private AppfuncSearchObserver mSearchObserver;
	private static AppfuncSearchController sController = null;
	private KeyWordsHandler mKeyWordsHandler;
	private AppfuncSearchEngine mSearchEngine;
	private String mLastKeyWords = "";
	private String mLocalLastKeyWords = "";
	private int mGoStoreSearchCount;

	private AppfuncSearchController(Context context, Handler handler) {
		mContext = context;
		mHandler = handler;
		mKeyWordsHandler = new KeyWordsHandler();
		mSearchEngine = AppfuncSearchEngine.getInstance(mContext);
		mSearchObserver = new AppfuncSearchObserver(mContext, mHandler);
		ResponseHandler.register(mSearchObserver);
	}

	public static AppfuncSearchController getInstance(Context context, Handler handler) {
		if (sController == null) {
			sController = new AppfuncSearchController(context, handler);
		}
		return sController;

	}
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  dingzijian
	 * @date  [2012-10-17]
	 */
	private class AppfuncSearchObserver extends SearchObserver {

		public AppfuncSearchObserver(Context context, Handler handler) {
			super(context, handler);

		}

		@Override
		public void onSearchSupported(boolean supported) {
			if (!supported) {
				mHandler.sendEmptyMessage(AppFuncSearchView.MSG_SHOW_SEARCH_CONNECT_EXCEPTION);
			}
		}

		@Override
		public void onSearchStart(String searchKey, int type, int currentPage) {
			switch (type) {
				case AppfuncSearchEngine.SEARCH_TYPE_WEB :
					if (currentPage > 1) {
						mHandler.sendEmptyMessage(AppFuncSearchView.MSG_SHOW_GOSTORE_LOAD_MORE_PROGRESS);
					} else {
						mHandler.sendEmptyMessage(AppFuncSearchView.MSG_SHOW_GOSTORE_PROGRESS_BAR);
					}
					break;
				case AppfuncSearchEngine.SEARCH_TYPE_LOCAL :
					mHandler.sendEmptyMessage(AppFuncSearchView.MSG_SHOW_LOCAL_PROGRESS_BAR);
					break;
				default :
					break;
			}
		}

		@Override
		public void onSearchFinsh(String searchKey, List<?> list, int type, int resultCount,
				int currentPage) {

			@SuppressWarnings("unchecked")
			ArrayList<FuncSearchResultItem> resultItems = (ArrayList<FuncSearchResultItem>) list;
			Message msg = null;
			switch (type) {
				case AppfuncSearchEngine.SEARCH_TYPE_LOCAL :

					break;
				case AppfuncSearchEngine.SEARCH_TYPE_IMAGE :

					break;
				case AppfuncSearchEngine.SEARCH_TYPE_AUDIO :

					break;
				case AppfuncSearchEngine.SEARCH_TYPE_VIDEO :

					break;
				case AppfuncSearchEngine.SEARCH_TYPE_WEB :
					mGoStoreSearchCount = resultCount;
					if (currentPage > 1) {
						msg = mHandler
								.obtainMessage(AppFuncSearchView.MSG_DATACHANGE_GOSTORE_ADAPTER);
					} else {
						msg = mHandler
								.obtainMessage(AppFuncSearchView.MSG_SET_GOSTORE_RESULT_ADAPTER);
					}
					msg.arg2 = mSearchEngine.getSearchId();
					break;
				case AppfuncSearchEngine.LOCAL_SEARCH_FINISH :
					msg = mHandler.obtainMessage(AppFuncSearchView.MSG_SET_LOCAL_RESULT_ADAPTER);
					break;
				default :
					break;
			}
			if (msg != null) {
				msg.arg1 = resultCount;
				ArrayList<FuncSearchResultItem> uiResultItems = new ArrayList<FuncSearchResultItem>(
						resultItems);
				msg.obj = uiResultItems;
				mHandler.sendMessage(msg);
			}
		}

		@Override
		public void onSearchWithoutData(String searchKey, int type) {
			Message msg = null;
			switch (type) {
				case AppfuncSearchEngine.SEARCH_ALL_RESOURCE :
					msg = mHandler.obtainMessage(AppFuncSearchView.MSG_SHOW_LOCAL_NODATA_VIEW);
					msg.obj = mContext.getString(R.string.appfunc_search_tip_no_match_data);
					break;
				case AppfuncSearchEngine.SEARCH_TYPE_WEB :
					msg = mHandler.obtainMessage(AppFuncSearchView.MSG_SHOW_GOSTORE_NODATA_VIEW);
					msg.obj = mContext.getString(R.string.appfunc_search_tip_no_match_data_web);
					break;
				default :
					break;
			}
			if (msg != null) {
				mHandler.sendMessage(msg);
			}
		}

		@Override
		public void onSearchException(String searchKey, Object obj, int type) {
			switch (type) {
				case AppfuncSearchEngine.SEARCH_TYPE_WEB :
					Message msg = mHandler.obtainMessage(AppFuncSearchView.MSG_SHOW_GOSTORE_NODATA_VIEW);
					msg.obj = mContext.getString(R.string.appfunc_search_tip_no_match_data_web);
					mHandler.sendMessage(msg);
					mHandler.sendEmptyMessage(AppFuncSearchView.MSG_SHOW_SEARCH_CONNECT_EXCEPTION);
					break;
				default :
					break;
			}
		}

		@Override
		public void onHistoryChange(List<?> list, int historyType, boolean hasHistory,
				boolean isNotifly) {
			Message msg = null;
			switch (historyType) {
				case AppfuncSearchEngine.HISTORY_TYPE_LOCAL :
					if (hasHistory) {
						msg = mHandler.obtainMessage(AppFuncSearchView.MSG_SHOW_LOCAL_HISTORY);
						@SuppressWarnings("unchecked")
						ArrayList<FuncSearchResultItem> resultItems = (ArrayList<FuncSearchResultItem>) list;
						msg.obj = resultItems;
					} else {
						msg = mHandler.obtainMessage(AppFuncSearchView.MSG_SHOW_LOCAL_NODATA_VIEW);
						msg.obj = mContext.getString(R.string.appfunc_search_no_history);
					}
					break;
				case AppfuncSearchEngine.HISTORY_TYPE_WEB :
					if (hasHistory) {
						@SuppressWarnings("unchecked")
						ArrayList<String> resultItems = (ArrayList<String>) list;
						msg = mHandler.obtainMessage(AppFuncSearchView.MSG_SHOW_GOSTORE_HISTORY);
						msg.obj = resultItems;
					} else {
						if (isNotifly) {
							msg = mHandler
									.obtainMessage(AppFuncSearchView.MSG_SHOW_GOSTORE_NODATA_VIEW);
							msg.obj = mContext.getString(R.string.appfunc_search_no_history);
						} else {
							msg = mHandler.obtainMessage(AppFuncSearchView.MSG_SHOW_KEY_WORDS);
						}

					}
					break;
				default :
					break;
			}
			mHandler.sendMessage(msg);
		}

	}
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  dingzijian
	 * @date  [2012-10-17]
	 */
	private class KeyWordsHandler {
//		private boolean mHasCacheData = false;
//		private HttpAdapter mHttpAdapter;
//		private ArrayList<HotSearchKeyword> mKeywordList;
//		private CacheManager mCacheManager;
//		ArrayList<NameValuePair> mNameValuePairs = new ArrayList<NameValuePair>();
//		THttpRequest mRequest = createHttpRequest(GoStorePublicDefine.URL_HOST3, mNameValuePairs,
//				GoStorePublicDefine.FUNID_SEARCH_KEYS, 0);

//		public KeyWordsHandler() {
//			mHttpAdapter = SimpleHttpAdapter.getHttpAdapter(mContext);
//			CacheManager.build(mContext, new XmlCacheParser(), null);
//			mCacheManager = CacheManager.getInstance();
//		}

//		private void getKeyworksData() {
//
//			if (mRequest != null) {
//				if (AppFuncUtils.isSDCardAccess()) {
//					getCacheDataFromLocal(mRequest, 0);
//				}
//				if (!mHasCacheData) {
//					// 如果没有缓存数据 或服务器有更新
//					getKeyworksDataFromNet(mRequest);
//				} else {
//					if (!getCacheDataFromLocal(mRequest, 0)) {
//						getKeyworksDataFromNet(mRequest);
//					}
//				}
//			}
//		}

//		private void getKeyworksDataFromNet(THttpRequest request) {
//			if (Machine.isNetworkOK(mContext)) {
//				if (request != null && mHttpAdapter != null) {
//					mHttpAdapter.addTask(request);
//				}
//			}
//		}

//		private boolean getCacheDataFromLocal(THttpRequest request, int action) {
//			boolean hasCacheData = false;
//			if (request != null) {
//				// CacheManager cacheManager = CacheManager.getInstance();
//				if (mCacheManager != null) {
//					Object object = mCacheManager.getViewCacheData(
//							GoStorePublicDefine.VIEW_TYPE_SEARCH_INPUT, request);
//					if (object != null) {
//						hasCacheData = true;
//						if (mHandler != null) {
//							if (object instanceof ArrayList<?>) {
//								@SuppressWarnings("unchecked")
//								ArrayList<BaseBean> listBeans = (ArrayList<BaseBean>) object;
//								handleDataBeans(listBeans, null, null);
//							}
//						}
//					}
//				}
//			}
//			return hasCacheData;
//		}

//		private THttpRequest createHttpRequest(String url,
//				ArrayList<NameValuePair> mNameValuePairs, int funid, int i) {
//			THttpRequest request = null;
//			try {
//				// 获取POST请求数据
//				byte[] postData = ThemeHttp.getPostData(mContext, mNameValuePairs, funid);
//				request = new THttpRequest(url, postData, new IConnectListener() {
//
//					@Override
//					public void onStart(THttpRequest request) {
//
//					}
//
//					@Override
//					public void onFinish(THttpRequest request, IResponse response) {
//						if (response != null
//								&& response.getResponseType() == IResponse.RESPONSE_TYPE_STREAM) {
//							if (mHandler != null && !mHasCacheData) {
//								Object obj = response.getResponse();
//								if (obj instanceof ArrayList<?>) {
//									@SuppressWarnings("unchecked")
//									ArrayList<BaseBean> listBeans = (ArrayList<BaseBean>) obj;
//									// 保存缓存数据
//									handleDataBeans(listBeans, request, response);
//								}
//							}
//
//						}
//					}
//
//					@Override
//					public void onException(THttpRequest request, int reason) {
//						StatisticsData.saveHttpExceptionDate(mContext, request, reason);
//					}
//				});
//				// 设置POST请求头
//				request.addHeader("Content-Type", LauncherEnv.Url.POST_CONTENT_TYPE);
//				MainDataHttpOperator operator = new MainDataHttpOperator();
//				request.setOperator(operator);
//			} catch (IllegalArgumentException e) {
//				e.printStackTrace();
//			} catch (URISyntaxException e) {
//				e.printStackTrace();
//			}
//			return request;
//		}

//		private void handleDataBeans(ArrayList<BaseBean> listBeans, THttpRequest request,
//				IResponse response) {
//
//			if (listBeans != null && listBeans.size() > 0) {
//				int count = listBeans.size();
//				for (int i = 0; i < count; i++) {
//					BaseBean baseBean = listBeans.get(i);
//					if (baseBean instanceof SearchKeysBean) {
//						if (mKeywordList == null) {
//							mKeywordList = new ArrayList<Key>();
//						} else {
//							mKeywordList.clear();
//						}
//						SearchKeysBean keysBean = (SearchKeysBean) baseBean;
//						ArrayList<KeyElement> keyElements = keysBean.mKeyElements;
//
//						if (keyElements.size() > 0) {
//							for (KeyElement element : keyElements) {
//								Key key = new Key();
//								key.type = 0;
//								key.key = element.mKeyword;
//								mKeywordList.add(key);
//							}
//							Message msg = mHandler.obtainMessage();
//							msg.obj = mKeywordList;
//							msg.what = AppFuncSearchView.MSG_UPDATE_KEYWORDS;
//							mHandler.sendMessage(msg);
//							if (mCacheManager != null && response != null && request != null) {
//								mCacheManager.saveViewCacheData(
//										GoStorePublicDefine.VIEW_TYPE_SEARCH_INPUT, request,
//										response.getResponse());
//								mHasCacheData = true;
//							}
//						} else {
//							mHasCacheData = false;
//						}
//					}
//				}
//			}
//		}
		/**
		 * 读取本地保存的热门搜索关键字展示出来，如果读取失败，则连网再取一次，用于下次展现
		 */
		public void getHotSearchKeyword() {
			List<HotSearchKeyword> list = SearchKeywordUtil.getHotSearchKeywords(mContext);
			if (list == null || list.size() <= 0) {
				SearchKeywordUtil.refreshHotSearchKeywords(mContext, 1,
						new DataHandler() {
							@Override
							public void handle(Object object) {
								if (object instanceof JSONObject) {
									JSONObject json = (JSONObject) object;
									final List<HotSearchKeyword> tlist = SearchKeywordUtil
											.parseHotSearchKeywords(json);
									if (tlist != null && tlist.size() > 0) {
										sendKeyWords(tlist);
									}
								}
							}
						});
			} else {
				sendKeyWords(list);
			}
		}
		private void sendKeyWords(final List<HotSearchKeyword> tlist) {
			if (tlist == null) {
				return;
			}
			List<FuncSearchResultItem> items = new ArrayList<FuncSearchResultItem>(tlist.size());
			FuncSearchResultItem hotKeyHeader = new FuncSearchResultItem();
			hotKeyHeader.mType = FuncSearchResultItem.ITEM_TYPE_RESULT_HEADER;
			hotKeyHeader.mTitle = mContext.getString(R.string.appfunc_search_key_words_sorts);
			items.add(hotKeyHeader);
			for (HotSearchKeyword element : tlist) {
				FuncSearchResultItem key = new FuncSearchResultItem();
				key.mType = FuncSearchResultItem.ITEM_TYPE_WEB_KEY_WORDS;
				key.mTitle = element.name;
				key.sicon = element.sicon;
				key.state = element.state;
				items.add(key);
			}
			Message msg = mHandler.obtainMessage();
			msg.obj = items;
			msg.what = AppFuncSearchView.MSG_UPDATE_KEYWORDS;
			mHandler.sendMessage(msg);
		}
	}

	@Override
	public void onBCChange(int msgId, int param, Object searchKey, Object object2) {

		switch (msgId) {
			case BC_MSG_APPFUNC_FRAME_START :

				break;
			case BC_MSG_APPFUNC_FRAME_END :
				sController = null;
				AppfuncSearchEngine.getInstance(mContext).cancleTask();
				AppfuncSearchEngine.recyle();
				break;
			case BC_MSG_UPDATE_KEYWORDS :
				mKeyWordsHandler.getHotSearchKeyword();
				break;
			case BC_MSG_SEARCH_LOACL_RESOURCE :
				String key = (String) searchKey;
				if (key == null || "".equals(key)) {
					Message message = mHandler
							.obtainMessage(AppFuncSearchView.MSG_SHOW_LOCAL_NODATA_VIEW);
					message.obj = mContext.getString(R.string.appfunc_search_tip_no_match_data);
					break;
				} else if (mLocalLastKeyWords.equalsIgnoreCase((String) searchKey)
						&& object2 != null) {
					break;
				}
				mLocalLastKeyWords = key;
				String initial = mContext.getString(R.string.initial);
				if (mSearchEngine != null) {
					if (key.startsWith(initial)) {
						if (key.length() == initial.length() + 1) {
							key = key.substring(key.length() - 1, key.length());
						}
					}
					mSearchEngine.searchLocalResource(key, (Boolean) object2);
				}
				break;
			case BC_MSG_SEARCH_WEB_APPS :
				hideImp(0);
				if (searchKey == null || "".equals(searchKey)) {
					Message message = mHandler
							.obtainMessage(AppFuncSearchView.MSG_SHOW_GOSTORE_NODATA_VIEW);
					message.obj = mContext.getString(R.string.appfunc_search_tip_no_match_data_web);
					break;
				}
				// 相同的搜索关键词,不重复搜索。
				else if (mLastKeyWords.equalsIgnoreCase((String) searchKey) && object2 != null) {
					Message msg = mHandler
							.obtainMessage(AppFuncSearchView.MSG_SET_GOSTORE_RESULT_ADAPTER);
					msg.obj = object2;
					msg.arg1 = mGoStoreSearchCount;
					mHandler.sendMessage(msg);
					mLastKeyWords = (String) searchKey;
					break;
				}
				mLastKeyWords = (String) searchKey;
				if (mSearchEngine != null) {
					mSearchEngine.searchWebApps((String) searchKey);
				}
				break;
			case BC_MSG_CHECK_HISTORY_GOSOTRE :
				if (mSearchEngine != null) {
					mSearchEngine.checkSearchHistroy(AppfuncSearchEngine.HISTORY_TYPE_WEB,
							(Boolean) object2);
				}
				break;
			case BC_MSG_CHECK_HISTORY_LOCAL :
				if (mSearchEngine != null) {
					mLocalLastKeyWords = "";
					mSearchEngine.checkSearchHistroy(AppfuncSearchEngine.HISTORY_TYPE_LOCAL, false);
				}
				break;
			case BC_MSG_SAVE_HISTORY :
				if (mSearchEngine != null) {
					mSearchEngine.putSearchKeyToSharedPreference((String) searchKey, param);
				}
				break;
			case BC_MSG_CLEAR_HISTORY :
				if (mSearchEngine != null) {
					mSearchEngine.clearSearchKeysSharedPreference(param, (Boolean) object2);
				}
				break;
			case BC_MSG_LOAD_MORE_WEB_APPS :
				if (mSearchEngine != null) {
					mSearchEngine.loadMoreWebApps();
				}
				break;
			case BC_MSG_SEARCH_ENGINE_MEDIA_DATA_READY :
				Message msg = mHandler.obtainMessage(AppFuncSearchView.MSG_MEDIA_DATA_READY);
				mHandler.sendMessage(msg);
				break;
			default :
				break;
		}
	}

	private void hideImp(int needRemove) {
		Message message = mHandler.obtainMessage(AppFuncSearchView.MSG_CHANGE_IM_STATE);
		message.obj = false;
		message.arg1 = needRemove;
		mHandler.sendMessage(message);
	}

	protected void setMediaSourceData(int mediaType, List<FileInfo> mediaList) {
		mSearchEngine.setMediaData(mediaType, mediaList);
	}

	public void setUIHandler(Handler handler) {
		mHandler = handler;
	}

	public void getEngineData(int i) {
		mSearchEngine.resetData(i);
	}

}
