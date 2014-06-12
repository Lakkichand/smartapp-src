package com.jiubang.ggheart.apps.desks.appfunc.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import com.gau.go.launcherex.R;
import com.gau.utils.net.request.THttpRequest;
import com.go.util.SortUtils;
import com.go.util.device.Machine;
import com.go.util.file.media.FileEngine;
import com.go.util.file.media.FileInfo;
import com.jb.util.pySearch.SearchResultItem;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.data.AppsSearchDownload;
import com.jiubang.ggheart.appgame.base.data.AppsSearchDownload.SearchDataHandler;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.FuncSearchUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.FuncSearchResultItem;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.AppDataEngine;
import com.jiubang.ggheart.data.info.AppItemInfo;
import com.jiubang.ggheart.data.statistics.StatisticsAppFuncSearch;

/**
 * 类描述:功能表搜索引擎 功能详细描述:处理功能表搜索网络的所有请求与处理回调的结果。
 * 
 * @author dingzijian
 * @date [2012-7-23]
 */
public class AppfuncSearchEngine {
	/**
	 * 网络搜素处理引擎
	 */
	private AppsSearchDownload mWebSearchEngine = null;
	/**
	 *应用中心搜索结果数据项列表
	 */
	private ArrayList<FuncSearchResultItem> mWebSearchResults = new ArrayList<FuncSearchResultItem>();
	/**
	 * 本地搜索结果数据项列表
	 */
	private ArrayList<FuncSearchResultItem> mLocalAllSearchResults = new ArrayList<FuncSearchResultItem>();
	/**
	 * 网络搜素的结果处理句柄
	 */
	private SearchDataHandler mWebResultHandler = null;
	private FuncSearchUtils mSearchUtil = null;
	private static final String SEARCH_FILE_NAME = "FuncSearchKeys";
	private static final String SEARCH_KEY_NAME = "GoStoreSearchKeys";
	private static final String SEARCH_SPLIT = "~!@#"; // 搜索过滤词
	private static final int SEARCH_HISTORY_LIMIT = 20; // 搜索记录条数
	public static final String[] SPACIL_SEARCH_KEY = { "0", "1", "2", "3", "4", "5", "6", "7", "8",
			"9" };

	private Context mContext;

	public static final int HISTORY_TYPE_LOCAL = 0x100;
	public static final int HISTORY_TYPE_WEB = 0x101;
	public static final int HISTORY_CLEAR = 0x102;
	public static final int SEARCH_TYPE_LOCAL = 0x200;
	public static final int SEARCH_TYPE_WEB = 0x201;
	public static final int SEARCH_TYPE_IMAGE = 0x202;
	public static final int SEARCH_TYPE_AUDIO = 0x203;
	public static final int SEARCH_TYPE_VIDEO = 0x204;
	public static final int SEARCH_ALL_RESOURCE = 0x205;
	public static final int LOCAL_SEARCH_FINISH = 0x206;

	private static AppfuncSearchEngine sEngine = null;

	private ConcurrentHashMap<Integer, List<FileInfo>> mMediaMap = new ConcurrentHashMap<Integer, List<FileInfo>>();

	public boolean mDataReady;

	private String mWebSearchKey = "";

	private Object mLock = new Object();

	private THttpRequest mRequest;
	private AppfuncSearchEngine(Context context) {
		mContext = context;
		mSearchUtil = FuncSearchUtils.getInstance(mContext);
		mWebSearchEngine = new AppsSearchDownload();
	}

	public synchronized static AppfuncSearchEngine getInstance(Context context) {
		if (sEngine == null) {
			sEngine = new AppfuncSearchEngine(context);
		}
		return sEngine;

	}

	public void searchWebApps(final String key) {
		resetSearchEngineData();
		mWebSearchKey = key;
		//		new SearchWebAppsTask().execute(key);
		searchWebAppsTask(key);
		StatisticsAppFuncSearch.countSearchStatistics(mContext,
				StatisticsAppFuncSearch.APPFUNC_SEARCH_WEB_TIMES);
	}

	public void loadMoreWebApps() {
		if (mWebSearchKey == null || "".equals(mWebSearchKey)
				|| mWebSearchEngine.mCurPage == mWebSearchEngine.mPageNum) {
			return;
		}
		searchWebAppsTask(mWebSearchKey);
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public void searchLocalResource(String key, boolean isSearchInitial) {
		new Thread(new SearchLocalResource(key, isSearchInitial)).start();
		StatisticsAppFuncSearch.countSearchStatistics(mContext,
				StatisticsAppFuncSearch.APPFUNC_SEARCH_LOCAL_TIMES);
	}

	/**
	 * <br>功能简述:
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	private ArrayList<FuncSearchResultItem> getLocalSearchHistory() {
		ArrayList<String> history = getSearchKeysFromSharedPreference(HISTORY_TYPE_LOCAL);
		if (history == null || history.size() == 0) {
			return null;
		}
		FunControler controler = AppFuncFrame.getFunControler();
		int size = history.size() + 1; // 清理历史记录项
		ArrayList<FuncSearchResultItem> localHistoryResults = new ArrayList<FuncSearchResultItem>(
				size);
		FuncSearchResultItem searchResultHeader = new FuncSearchResultItem();
		searchResultHeader.mType = FuncSearchResultItem.ITEM_TYPE_RESULT_HEADER;
		searchResultHeader.mTitle = mContext.getResources().getString(
				R.string.appfunc_search_history);
		localHistoryResults.add(searchResultHeader);
		FuncSearchResultItem historyItem = null;
		for (String key : history) {
			try {
				AppItemInfo info = AppDataEngine.getInstance(mContext).getAppItem(
						Intent.getIntent(key));
				if (info != null) {
					if (info.mIntent != null && !controler.isHiddeApp(info.mIntent)) {
						historyItem = new FuncSearchResultItem();
						historyItem.mType = FuncSearchResultItem.ITEM_TYPE_LOCAL_HISTORY_APPS;
						historyItem.mTitle = info.mTitle;
						historyItem.mIcon = info.mIcon;
						historyItem.mIntent = info.mIntent;
						localHistoryResults.add(historyItem);
					}
				} else {
					if (mDataReady) {
						ArrayList<FileInfo> fileInfos = (ArrayList<FileInfo>) mMediaMap
								.get(FileEngine.TYPE_IMAGE);
						searchMediaHistory(localHistoryResults, key, fileInfos);
						fileInfos = (ArrayList<FileInfo>) mMediaMap.get(FileEngine.TYPE_AUDIO);
						searchMediaHistory(localHistoryResults, key, fileInfos);
						fileInfos = (ArrayList<FileInfo>) mMediaMap.get(FileEngine.TYPE_VIDEO);
						searchMediaHistory(localHistoryResults, key, fileInfos);
					}

				}
			} catch (Exception e) {
				deleteSearchKeyFromSharedPreference(key);
			}

		}

		if (localHistoryResults.size() <= 1) {
			return null;
		}
		return localHistoryResults;
	}

	private void searchMediaHistory(ArrayList<FuncSearchResultItem> localHistoryResults,
			String key, ArrayList<FileInfo> fileInfos) {
		FuncSearchResultItem historyItem;
		for (FileInfo fileInfo : fileInfos) {
			if (fileInfo.fileName.equals(key)) {
				historyItem = new FuncSearchResultItem();
				historyItem.mType = FuncSearchResultItem.ITEM_TYPE_LOCAL_HISTORY_MEDIA;
				historyItem.mTitle = fileInfo.fileName;
				historyItem.fileInfo = fileInfo;
				localHistoryResults.add(historyItem);
			}
		}
	}

	/**
	 * 把搜索关键字保存到SharedPreferences里面
	 * 
	 * @param searchKey
	 */
	public void putSearchKeyToSharedPreference(String searchKey, int type) {
		if (searchKey == null || "".equals(searchKey)) {
			return;
		}
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		String searchKeysString = null;
		StringBuilder resultString = null;
		switch (type) {
			case HISTORY_TYPE_LOCAL :
				searchKeysString = sharedPreferences.getString(SEARCH_FILE_NAME, "");
				resultString = splitKeyString(searchKey, searchKeysString);
				sharedPreferences.putString(SEARCH_FILE_NAME, resultString.toString());
				sharedPreferences.commit();
				break;
			case HISTORY_TYPE_WEB :
				searchKeysString = sharedPreferences.getString(SEARCH_KEY_NAME, "");
				resultString = splitKeyString(searchKey, searchKeysString);
				sharedPreferences.putString(SEARCH_KEY_NAME, resultString.toString());
				sharedPreferences.commit();
				break;
			default :
				break;
		}

	}

	private StringBuilder splitKeyString(String searchKey, String searchKeysString) {
		String[] keys = searchKeysString.split(SEARCH_SPLIT);
		for (int i = 0; i < keys.length; i++) {
			if (keys[i].equals(searchKey)) {
				keys[i] = null;
			}
		}
		StringBuilder resultString = new StringBuilder(searchKey);
		resultString.append(SEARCH_SPLIT);
		int count = 1;
		for (int i = 0; i < keys.length && count < SEARCH_HISTORY_LIMIT; i++) {
			if (keys[i] != null && !"".equals(keys[i])) {
				resultString.append(keys[i]);
				resultString.append(SEARCH_SPLIT);
				count++;
			}
		}
		return resultString;
	}

	/**
	 * 到SharedPreferences中取出搜索关键字
	 * 
	 * @return
	 */
	private ArrayList<String> getSearchKeysFromSharedPreference(int type) {
		ArrayList<String> result = new ArrayList<String>();
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		String searchKeysString = null;
		switch (type) {
			case HISTORY_TYPE_LOCAL :
				searchKeysString = sharedPreferences.getString(SEARCH_FILE_NAME, "");
				break;
			case HISTORY_TYPE_WEB :
				searchKeysString = sharedPreferences.getString(SEARCH_KEY_NAME, "");
				break;
			default :
				break;
		}
		if (!"".equals(searchKeysString)) {
			String[] keys = searchKeysString.split(SEARCH_SPLIT);
			for (int i = 0; i < keys.length; i++) {
				if (keys[i] != null && !"".equals(keys[i])) {
					result.add(keys[i]);
				}
			}
		}
		return result;
	}

	private void deleteSearchKeyFromSharedPreference(String key) {
		if (key == null || "".equals(key)) {
			return;
		}
		ArrayList<String> result = getSearchKeysFromSharedPreference(HISTORY_TYPE_LOCAL);
		if (result != null) {
			Iterator<String> it = result.iterator();
			StringBuilder resultString = new StringBuilder();
			while (it.hasNext()) {
				String temp = it.next();
				if (key.equals(temp)) {
					it.remove();
				} else {
					resultString.append(temp + SEARCH_SPLIT);
				}
			}
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
			sharedPreferences.putString(SEARCH_FILE_NAME, resultString.toString());
			sharedPreferences.commit();
		}
	}

	/**
	 * 清理历史记录
	 * 
	 * @author dingzijian
	 */
	public void clearSearchKeysSharedPreference(int type, boolean isNotifyHandler) {
		PreferencesManager sharedPreferences = null;
		switch (type) {
			case HISTORY_TYPE_LOCAL :
				sharedPreferences = new PreferencesManager(mContext,
						IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
				sharedPreferences.clear();
				if (isNotifyHandler) {
					ResponseHandler.historyResponseRecevied(null, HISTORY_TYPE_LOCAL, false, false);
				}
				break;
			case HISTORY_TYPE_WEB :
				sharedPreferences = new PreferencesManager(mContext,
						IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
				sharedPreferences.clear();
				if (isNotifyHandler) {
					ResponseHandler.historyResponseRecevied(null, HISTORY_TYPE_WEB, false, false);
				}
				break;
			default :
				break;
		}
	}

	public void checkSearchHistroy(int historyType, boolean isNotifly) {
		switch (historyType) {
			case HISTORY_TYPE_LOCAL :
				ArrayList<FuncSearchResultItem> items = getLocalSearchHistory();
				if (items != null) {
					ResponseHandler.historyResponseRecevied(items, historyType, true, false);
				} else {
					ResponseHandler.historyResponseRecevied(null, historyType, false, false);
				}
				break;
			case HISTORY_TYPE_WEB :
				mWebSearchEngine.cancelTask(mContext, mRequest);
				ArrayList<String> historyRestults = getSearchKeysFromSharedPreference(historyType);
				if (null == historyRestults || historyRestults.isEmpty()) {
					ResponseHandler.historyResponseRecevied(null, historyType, false, isNotifly);
				} else {

					ArrayList<FuncSearchResultItem> webHistoryRestults = new ArrayList<FuncSearchResultItem>(
							historyRestults.size());
					FuncSearchResultItem searchResultHeader = new FuncSearchResultItem();
					searchResultHeader.mType = FuncSearchResultItem.ITEM_TYPE_RESULT_HEADER;
					searchResultHeader.mTitle = mContext.getResources().getString(
							R.string.appfunc_search_history);
					searchResultHeader.mIcon = mContext.getResources().getDrawable(
							R.drawable.appfunc_search_history_icon);
					FuncSearchResultItem keyWordItem = new FuncSearchResultItem();
					keyWordItem.mType = FuncSearchResultItem.ITEM_TYPE_WEB_KEY_WORDS;
					keyWordItem.mTitle = mContext.getResources().getString(
							R.string.appfunc_search_key_words_sorts);
					keyWordItem.mIsLastItem = true;
					keyWordItem.mIcon = mContext.getResources().getDrawable(
							R.drawable.appgame_toptab_hot);
					webHistoryRestults.add(keyWordItem);
					webHistoryRestults.add(searchResultHeader);
					for (String historyRestult : historyRestults) {
						FuncSearchResultItem item = new FuncSearchResultItem();
						item.mTitle = historyRestult;
						item.mType = FuncSearchResultItem.ITEM_TYPE_WEB_HISTORY;
						item.mIcon = mContext.getResources().getDrawable(
								R.drawable.appfunc_search_history_icon);
						webHistoryRestults.add(item);
					}
					ResponseHandler.historyResponseRecevied(webHistoryRestults, historyType, true,
							isNotifly);
				}
				break;
			default :
				break;
		}
	}

	public static void recyle() {
		sEngine = null;
	}

	public void cancleTask() {
		if (mWebSearchEngine != null) {
			mWebSearchEngine.cancelTask(mContext, mRequest);
		}
	}
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  dingzijian
	 * @date  [2012-8-15]
	 */
	//	class SearchWebAppsTask extends AsyncTask<String, Void, Void> {
	//		@Override
	protected void searchWebAppsTask(String key) {
		//			String key = params[0];
		if (key == null || "".equals(key) || "".equals(key.trim())) {
			return;
		}
		putSearchKeyToSharedPreference(key, HISTORY_TYPE_WEB);
		if (!Machine.isNetworkOK(mContext)) {
			ResponseHandler.checkSearchSupportedResponse(false);
			return;
		}
		if (mWebResultHandler == null) {
			mWebResultHandler = new SearchDataHandler() {
				@Override
				public void handleData(Object object) {
					if (!AppFuncSearchFrame.sIsSearchVisable) {
						return;
					}
					final ArrayList<BoutiqueApp> list = (ArrayList<BoutiqueApp>) object;
					new Thread(new Runnable() {

						@Override
						public void run() {
							handleWebDataResult(list, mWebSearchKey);

						}
					}).start();
				}
			};
		}
		int pageId = mWebSearchEngine.mCurPage;
		if (pageId == -1) {
			pageId = 0;
		}
		mWebSearchEngine.setIsLoadingNextPage(true);
		ResponseHandler.startResponseReceived(null, SEARCH_TYPE_WEB, pageId + 1);
		//clientId传2代表功能表搜索请求数据 add by xiedezhi 2012-12-19
		mRequest = mWebSearchEngine.getSearchData(mContext,
				key,
				AppsSearchDownload.SEARCH_TYPE_ALL,
				AppsSearchDownload.PRICE_TYPE_ALL, pageId + 1,
				mWebResultHandler, 2);
		return;
	}
	private void handleWebDataResult(ArrayList<BoutiqueApp> searchResultList, String key) {
		if (searchResultList == null) {
			ResponseHandler.exceptionResponseReceived(null, null, SEARCH_TYPE_WEB);
			return;
		}
		if (!searchResultList.isEmpty()) {
			ArrayList<FuncSearchResultItem> tmpWebSearchResults = new ArrayList<FuncSearchResultItem>(
					searchResultList.size());
			for (BoutiqueApp element : searchResultList) {
				FuncSearchResultItem resultItem = new FuncSearchResultItem();
				resultItem.mType = FuncSearchResultItem.ITEM_TYPE_APP_CENTER_APPS;
				resultItem.appInfo = element;
				SearchResultItem item = mSearchUtil.match(key, element.info.name);
				resultItem.setTitle(element.info.name, item);
				resultItem.recApp = element;
				tmpWebSearchResults.add(resultItem);
			}
			if (mWebSearchResults.isEmpty()) {
				int resultCount = tmpWebSearchResults.size();
				tmpWebSearchResults.get(resultCount - 1).mIsLastItem = true;
				FuncSearchResultItem searchResultHeader = new FuncSearchResultItem();
				searchResultHeader.mType = FuncSearchResultItem.ITEM_TYPE_RESULT_HEADER;
				searchResultHeader.mTitle = mContext.getResources().getString(
						R.string.appfunc_search_result)
						+ ":";
				mWebSearchResults.add(searchResultHeader);
			}
			mWebSearchResults.addAll(tmpWebSearchResults);
			//			 按搜索结果相关性进行排序
			SortUtils.sortByInt(tmpWebSearchResults, "getMatchIndex", null, null, "ASC");
			ResponseHandler.finishResponseReceived(null, mWebSearchResults, SEARCH_TYPE_WEB,
					mWebSearchEngine.mTotalNum, mWebSearchEngine.mCurPage);
		} else {
			ResponseHandler.noDataResponseReceived(null, SEARCH_TYPE_WEB);
		}
	}
	//	}
	protected void setMediaData(int mediaType, List<FileInfo> mediaList) {
		mMediaMap.put(mediaType, mediaList);
		switch (mediaType) {
			case FileEngine.TYPE_VIDEO :
				AppFuncSearchFrame.broadCast(
						AppfuncSearchController.BC_MSG_SEARCH_ENGINE_MEDIA_DATA_READY,
						SEARCH_ALL_RESOURCE, null, null);
				mDataReady = true;
				break;
			default :
				break;
		}
	}
	/**
	 * 
	 * <br>类描述: 搜索媒体资源的runnable
	 * <br>功能详细描述:
	 * 
	 * @author  dingzijian
	 * @date  [2012-10-17]
	 */
	class SearchLocalResource implements Runnable {
		final String mKey;
		String mType;
		byte mItemType;
		boolean mIsSearchInitial;
		private ArrayList<FuncSearchResultItem> mLocalSearchResults = new ArrayList<FuncSearchResultItem>();

		public SearchLocalResource(String key, boolean isSearchInitial) {
			mKey = key;
			mIsSearchInitial = isSearchInitial;
		}
		@Override
		public void run() {

			//开始搜索
			ResponseHandler.startResponseReceived(mKey, SEARCH_TYPE_LOCAL, 0);

			searchLocalData(SEARCH_TYPE_LOCAL, AppDataEngine.getInstance(mContext)
					.getAllAppItemInfos());
			searchLocalData(SEARCH_TYPE_IMAGE, mMediaMap.get(FileEngine.TYPE_IMAGE));
			searchLocalData(SEARCH_TYPE_AUDIO, mMediaMap.get(FileEngine.TYPE_AUDIO));
			searchLocalData(SEARCH_TYPE_VIDEO, mMediaMap.get(FileEngine.TYPE_VIDEO));

			synchronized (mLock) {
				AppfuncSearchEngine.this.mLocalAllSearchResults = mLocalSearchResults;
			}

			//搜索结束
			if (mLocalSearchResults.isEmpty()) {
				ResponseHandler.noDataResponseReceived(mKey, SEARCH_ALL_RESOURCE);
			} else {
				ResponseHandler.finishResponseReceived(mKey, mLocalSearchResults,
						LOCAL_SEARCH_FINISH, -1, -1);
			}

		}

		private void searchLocalData(int searchType, List<?> mediaList) {
			switch (searchType) {
				case SEARCH_TYPE_IMAGE :
					mType = mContext.getString(R.string.appfunc_search_type_image);
					mItemType = FuncSearchResultItem.ITEM_TYPE_LOCAL_IMAGE;
					break;
				case SEARCH_TYPE_AUDIO :
					mType = mContext.getString(R.string.appfunc_search_type_music);
					mItemType = FuncSearchResultItem.ITEM_TYPE_LOCAL_AUDIO;
					break;
				case SEARCH_TYPE_VIDEO :
					mType = mContext.getString(R.string.appfunc_search_type_video);
					mItemType = FuncSearchResultItem.ITEM_TYPE_LOCAL_VIDEO;
					break;
				case SEARCH_TYPE_LOCAL :
					mType = mContext.getString(R.string.appfunc_search_type_apps);
					mItemType = FuncSearchResultItem.ITEM_TYPE_LOCAL_APPS;
					break;
				default :
					break;
			}
			if (mediaList == null || mediaList.size() == 0) {
				ResponseHandler.exceptionResponseReceived(mKey, null, searchType);
				return;
			}
			ArrayList<FuncSearchResultItem> resultItems = new ArrayList<FuncSearchResultItem>();
			if (searchType == SEARCH_TYPE_LOCAL) {
				List<AppItemInfo> appData = (List<AppItemInfo>) mediaList;
				// 过滤掉隐藏程序,其实就是把隐藏程序从列表中删除
				Iterator<AppItemInfo> it = appData.iterator();
				while (it.hasNext()) {
					AppItemInfo item = it.next();
					if (AppFuncFrame.getAppConfigControler().isHideApp(item.mIntent)) {
						it.remove();
					}
				}
				if (mKey.equals("#")) {
					for (String key : SPACIL_SEARCH_KEY) {
						mIsSearchInitial = true;
						searchLocalApps(resultItems, key, appData);
					}
				} else {
					searchLocalApps(resultItems, mKey, appData);
				}
			} else {
				List<FileInfo> mediaData = (List<FileInfo>) mediaList;
				if (mKey.equals("#")) {
					for (String key : SPACIL_SEARCH_KEY) {
						mIsSearchInitial = true;
						searchLocalMedia(resultItems, key, mediaData);
					}
				} else {
					searchLocalMedia(resultItems, mKey, mediaData);
				}
			}
			if (!resultItems.isEmpty()) {
				try {
					// 按搜索结果相关性进行排序
					SortUtils.sortByInt(resultItems, "getMatchIndex", null, null, "ASC");
					int resultCount = resultItems.size();
//					resultItems.get(resultCount - 1).mIsLastItem = true;
					FuncSearchResultItem searchResultHeader = new FuncSearchResultItem();
					searchResultHeader.mType = FuncSearchResultItem.ITEM_TYPE_RESULT_HEADER;
					searchResultHeader.mTitle = mType + "(" + resultCount + ")";		
					mLocalSearchResults.add(searchResultHeader);
					mLocalSearchResults.addAll(resultItems);
					if (mItemType == FuncSearchResultItem.ITEM_TYPE_LOCAL_APPS) {
						FuncSearchResultItem hotKeyItem = new FuncSearchResultItem();
						hotKeyItem.mType = FuncSearchResultItem.ITEM_TYPE_SEARCH_WEB;
						hotKeyItem.mTitle = mContext
								.getString(R.string.appfunc_search_get_more_apps);
						hotKeyItem.mIsLastItem = true;
						mLocalSearchResults.add(hotKeyItem);
					}
					
					//						ResponseHandler.finishResponseReceived(mKey, mLocalSearchResults,
					//								mSearchType, resultItems.size() - 1, -1);
				} finally {

				}
			}
		}

		private void searchLocalMedia(ArrayList<FuncSearchResultItem> resultItems, String key,
				List<FileInfo> mediaData) {
			if (resultItems == null) {
				return;
			}
			FuncSearchResultItem resultItem;
			for (FileInfo itemInfo : mediaData) {
				if (itemInfo.fileName != null) {
					String searchKey = itemInfo.fileName;
					if (mIsSearchInitial) {
						if (searchKey == null || searchKey.length() < 1) {
							continue;
						}
						searchKey = searchKey.substring(0, 1);
					}
					SearchResultItem item = mSearchUtil.match(key, searchKey);
					if (item != null && item.mMatchValue > 0) {
						resultItem = new FuncSearchResultItem();
						resultItem.mType = mItemType;
						resultItem.fileInfo = itemInfo;
						resultItem.setTitle(itemInfo.fileName, item);
						resultItems.add(resultItem);
					}
				}
			}
//			resultItems.get(resultItems.size() - 1).mIsLastItem = true;
		}

		private void searchLocalApps(ArrayList<FuncSearchResultItem> resultItems, String key,
				List<AppItemInfo> appData) {
			Drawable tmpDrawable = null;
			BitmapDrawable tmpBitmapDrawable = null;
			DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
			FuncSearchResultItem resultItem;
			for (AppItemInfo itemInfo : appData) {
				if (itemInfo.mTitle != null) {
					String searchKey = itemInfo.mTitle;
					if (mIsSearchInitial) {
						if (searchKey == null || searchKey.length() < 1) {
							continue;
						}
						searchKey = searchKey.substring(0, 1);
					}
					SearchResultItem item = mSearchUtil.match(key, searchKey);
					if (item != null && item.mMatchValue > 0) {
						resultItem = new FuncSearchResultItem();
						resultItem.mType = FuncSearchResultItem.ITEM_TYPE_LOCAL_APPS;
						resultItem.mIcon = itemInfo.mIcon;
						tmpDrawable = resultItem.mIcon;
						if (tmpDrawable instanceof BitmapDrawable) {
							// 需要设置icon的tensity
							tmpBitmapDrawable = (BitmapDrawable) tmpDrawable;
							tmpBitmapDrawable.setTargetDensity(displayMetrics);
						}
						resultItem.mIntent = itemInfo.mIntent;
						resultItem.setTitle(itemInfo.mTitle, item);
						resultItems.add(resultItem);
					}
				}
			}
		};
	}
	private void resetSearchEngineData() {
		if (mWebSearchEngine != null && mWebSearchResults != null) {
			mWebSearchEngine.mCurPage = -1; // 当前分页
			mWebSearchEngine.mTotalNum = 0; // 总结果数量
			mWebSearchEngine.mPageNum = -1; // 总页数
			mWebSearchEngine.mSearchId = -1; // 搜索分类id
			mWebSearchResults.clear();
		}
	}
	public int getSearchId() {
		if (mWebSearchEngine == null) {
			return -1;
		}
		return mWebSearchEngine.mSearchId;
	}
	public void resetData(int which) {
		switch (which) {
			case AppFuncSearchView.GOSTORE_ADAPTER_TAG :
				if (mWebSearchResults != null && !mWebSearchResults.isEmpty()) {
					ResponseHandler.finishResponseReceived(mWebSearchKey, mWebSearchResults,
							SEARCH_TYPE_WEB, mWebSearchResults.size(), -1);
				} else {
					ResponseHandler.noDataResponseReceived(null, SEARCH_TYPE_WEB);
				}
				break;
			case AppFuncSearchView.GOSTORE_HISTORY_ADAPTER_TAG :
				checkSearchHistroy(HISTORY_TYPE_WEB, false);
				break;
			case AppFuncSearchView.LOCAL_ADAPTER_TAG :
				if (mLocalAllSearchResults != null && !mLocalAllSearchResults.isEmpty()) {
					ResponseHandler.finishResponseReceived(mWebSearchKey, mLocalAllSearchResults,
							LOCAL_SEARCH_FINISH, -1, -1);
				} else {
					ResponseHandler.noDataResponseReceived(null, SEARCH_ALL_RESOURCE);
				}
				break;
			case AppFuncSearchView.LOCAL_HISTORY_ADAPTER_TAG :
				checkSearchHistroy(HISTORY_TYPE_LOCAL, false);
				break;
			case AppFuncSearchView.KEY_WORDS_ADAPTER_TAG :
				AppFuncSearchFrame.broadCast(AppfuncSearchController.BC_MSG_UPDATE_KEYWORDS, -1,
						null, null);
				break;	
			default :
				break;
		}

	}
}
