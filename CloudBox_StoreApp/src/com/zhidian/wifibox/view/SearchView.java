package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.adapter.SearchAdapter;
import com.zhidian.wifibox.controller.SearchController;
import com.zhidian.wifibox.data.AppDataBean;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.SearchDataBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.DownloadTaskRecorder;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.pulldownlistview.PullDownRefreshView;
import com.zhidian.wifibox.pulldownlistview.PullDownRefreshView.onLoadMoreListener;
import com.zhidian.wifibox.util.InfoUtil;

/**
 * 搜索界面
 * 
 * @author xiedezhi
 * 
 */
public class SearchView extends FrameLayout implements IContainer,
		OnClickListener {
	private TextView searchHintTv;
	private EditText searchKeyEt;
	private boolean isLoading = false;
	private boolean isSearching = false;// 是否执行搜索
	private int pageNow = 1;
	private GridView mGridView;
	private ImageView backIv;
	private ListView mListView;
	private ListViewSearchFooterView mFoot;
	private ArrayAdapter<String> keywordAdapter;
	private ProgressBar loadingBar;
	private LinearLayout noFound;
	private Button deleteBtn;
	public static final int DEFAULT_PAGENO = 1;
	public static final String TAG = "SearcherView";
	public static boolean TYPE_WIFI = false; // 是否处于WIFI状态

	/**
	 * searchKeyEt监听器
	 */
	private TextWatcher mTextChangedListener = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			if (s.length() == 0) {
				searchHintTv.setVisibility(View.VISIBLE);
				// keywordAdapter.clear();
				keywordListView.setVisibility(View.GONE);
				noFound.setVisibility(View.GONE);
				loadingBar.setVisibility(View.GONE);
				mGridView.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.GONE);
				deleteBtn.setVisibility(View.GONE);
			} else {
				searchHintTv.setVisibility(View.GONE);
				keywordListView.setVisibility(View.GONE);
				noFound.setVisibility(View.GONE);
				deleteBtn.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.GONE);
				// 搜索关键词自动完成
				// 判断是否处于WIFI状态
				if (InfoUtil.hasWifiConnection(SearchView.this.getContext())
						&& !isSearching) {
					searchKeyAutoComplete(searchKeyEt.getText().toString()
							.trim());
				} else {
					mGridView.setVisibility(View.GONE);
				}
			}
		}

		/**
		 * 搜索关键词自动完成
		 * 
		 * @param keyword
		 */
		private void searchKeyAutoComplete(String keyword) {
			loadingBar.setVisibility(View.VISIBLE);
			mGridView.setVisibility(View.GONE);
			TAApplication.getApplication().doCommand(
					getContext().getString(R.string.searchviewcontroller),
					new TARequest(SearchController.SEARCH_KEY_AUTOCOMPLETE,
							keyword), new TAIResponseListener() {

						@Override
						public void onSuccess(TAResponse response) {

							SearchDataBean dataGroup = (SearchDataBean) response
									.getData();
							if (dataGroup.mStatuscode != 0) {
								// TODO 显示出错页面
								loadingBar.setVisibility(View.GONE);
								return;
							}
							loadingBar.setVisibility(View.GONE);
							// 更新关键词ListView
							if (dataGroup.mKeyword.equals(searchKeyEt.getText()
									.toString().trim())) {
								updateSearchKeyListView(dataGroup);
							}
						}

						@Override
						public void onStart() {

						}

						@Override
						public void onRuning(TAResponse response) {

						}

						@Override
						public void onFailure(TAResponse response) {

						}

						@Override
						public void onFinish() {

						}
					}, true, false);
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

	};
	private Button searchButton;
	private SearchDataBean mSearchDateBean;
	private PullDownRefreshView refreshView;
	private ListView keywordListView;
	private SearchAdapter mAdapter;

	/**
	 * 列表滑动底部的toast提示
	 */
	Toast mToast = Toast.makeText(getContext(), R.string.lastpage,
			Toast.LENGTH_SHORT);

	/************************
	 * 
	 * 监听推荐关键词点击事件
	 * 
	 ************************/
	private OnItemClickListener mGridViewItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int arg2,
				long arg3) {
			TextView keywordTv = (TextView) view.findViewById(R.id.keyword_tv);
			isSearching = true;
			searchKeyEt.setText(keywordTv.getText());
			searchKeyEt.setSelection(keywordTv.getText().length());
			doSearch();
		}

	};

	/************************
	 * 
	 * 监听关键词ListView点击事件
	 * 
	 ************************/
	private OnItemClickListener mKeywordItemClickListener = new android.widget.AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View view, int arg2,
				long arg3) {
			TextView appName = (TextView) view;
			isSearching = true;
			searchKeyEt.setText(appName.getText());
			doSearch();

		}
	};

	public SearchView(Context context) {
		super(context);
	}

	public SearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SearchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onAppAction(String packName) {
		// 更改列表应用安装状态
		boolean needToUpdate = false;
		if (mSearchDateBean != null && mSearchDateBean.mAppList != null) {
			for (AppDataBean bean : mSearchDateBean.mAppList) {
				if (bean.packName.equals(packName)) {
					needToUpdate = true;
					break;
				}
			}
		}
		if (needToUpdate) {
			updateDownloadState(mSearchDateBean.mAppList);
			mAdapter.update(mSearchDateBean.mAppList);
		}
	}

	@Override
	public String getDataUrl() {
		return "";
	}

	@Override
	public void updateContent(PageDataBean bean) {
	}

	@Override
	protected void onFinishInflate() {
		searchHintTv = (TextView) findViewById(R.id.search_hint_tv);
		noFound = (LinearLayout) findViewById(R.id.no_found_ll);
		backIv = (ImageView) findViewById(R.id.backBtn);
		searchKeyEt = (EditText) findViewById(R.id.searchEdit);
		searchButton = (Button) findViewById(R.id.searchBtn);
		deleteBtn = (Button) findViewById(R.id.deleteBtn);
		searchKeyEt.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					searchButton.performClick();
					return true;
				}
				return false;
			}
		});
		loadingBar = (ProgressBar) findViewById(R.id.loading_pb);
		keywordListView = (ListView) findViewById(R.id.keyword_list);
		refreshView = (PullDownRefreshView) findViewById(R.id.apps_list_refreshview);
		mListView = (ListView) refreshView.getChildAt(1);
		// 添加footview
		mFoot = (ListViewSearchFooterView) LayoutInflater.from(getContext())
				.inflate(R.layout.view_listview_footer, null);
		mListView.addFooterView(mFoot);
		mFoot.showLoading();
		mAdapter = new SearchAdapter(getContext());
		mListView.setAdapter(mAdapter);
		searchKeyEt.addTextChangedListener(mTextChangedListener);
		mGridView = (GridView) findViewById(R.id.appsNameGrid);
		deleteBtn.setOnClickListener(this);
		searchButton.setOnClickListener(this);
		backIv.setOnClickListener(this);
		getSearchKeyRecommend();
		requestFocus();
		InputMethodManager imm = (InputMethodManager) getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

		refreshView.setOnLoadMoreListener(new onLoadMoreListener() {

			@Override
			public void onLoadMore() {// 底部加载更多
				refreshView.setOnLoadState(false, false);
				if (!isLoading) {
					Log.e("加载", "加载更多");
					pageNow++;
					getNextPageData();
				}
			}
		});

	}

	/*********************
	 * 获取下一页数据
	 *********************/
	private void getNextPageData() {

		if (pageNow > mSearchDateBean.mTotalPage) {
			// 已经是最后一页，弹toast提示
			mToast.show();
			mFoot.viewGone();
			return;
		} else {
			mFoot.showLoading();
			Map<String, String> data = new HashMap<String, String>();
			data.put("keyword", searchKeyEt.getText().toString().trim());
			data.put("pageNo", pageNow + "");
			TARequest request = new TARequest(SearchController.START_SEARCH,
					data);
			TAApplication.getApplication().doCommand(
					getContext().getString(R.string.searchviewcontroller),
					request, new TAIResponseListener() {

						@Override
						public void onSuccess(TAResponse response) {
							SearchDataBean bean = (SearchDataBean) response
									.getData();
							if (mSearchDateBean == null) {
								mSearchDateBean = bean;
							} else {
								mSearchDateBean.mAppList.addAll(bean.mAppList);
							}
							mAdapter.update(mSearchDateBean.mAppList);
							if (bean.mTotalPage <= bean.mPageIndex) {
								mFoot.viewGone();
							}
						}

						@Override
						public void onStart() {
						}

						@Override
						public void onRuning(TAResponse response) {
						}

						@Override
						public void onFailure(TAResponse response) {
						}

						@Override
						public void onFinish() {

						}
					}, true, false);

		}
	}

	/********************
	 * 获取搜索关键词推荐
	 *********************/
	private void getSearchKeyRecommend() {
		TARequest request = new TARequest(
				SearchController.SEARCH_KEY_RECOMMEND, null);

		TAApplication.getApplication().doCommand(
				getContext().getString(R.string.searchviewcontroller), request,
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						if (TextUtils.isEmpty(searchKeyEt.getText().toString()
								.trim())) {

							SearchDataBean bean = (SearchDataBean) response
									.getData();
							Log.e(TAG, " 搜索关键词推荐  Statuscode >>>>   "
									+ bean.mMessage);
							List<Map<String, String>> list = new ArrayList<Map<String, String>>();
							for (int i = 0; i < bean.mSearchKeyList.size(); i++) {
								HashMap<String, String> map = new HashMap<String, String>();
								map.put("keyword", bean.mSearchKeyList.get(i));
								list.add(map);
							}
							SimpleAdapter adapter = new SimpleAdapter(
									getContext(), list,
									R.layout.gridview_item_keyword,
									new String[] { "keyword" },
									new int[] { R.id.keyword_tv });
							mGridView.setAdapter(adapter);
							mGridView.setVisibility(View.VISIBLE);
							mGridView
									.setOnItemClickListener(mGridViewItemClickListener);
						}

					}

					@Override
					public void onStart() {
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFailure(TAResponse response) {
					}

					@Override
					public void onFinish() {

					}
				}, true, false);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.searchBtn:
			doSearch();
			break;

		case R.id.backBtn:
			removeSearchView();
			InputMethodManager imm = (InputMethodManager) getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mListView.getWindowToken(), 0);
			break;

		case R.id.deleteBtn:
			searchKeyEt.setText("");
			break;

		default:
			break;
		}
	}

	private void removeSearchView() {
		MainActivity.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
				IDiyMsgIds.REMOVE_SEARCHVIEW, -1, null, null);
	}

	/**
	 * 实现搜索功能;
	 */
	private void doSearch() {
		if (TextUtils.isEmpty(searchKeyEt.getText().toString().trim())) {
			Toast.makeText(getContext(), "请输入搜索关键词", Toast.LENGTH_SHORT).show();
			return;
		}
		keywordListView.setVisibility(View.GONE);
		mListView.setVisibility(View.GONE);
		mGridView.setVisibility(View.GONE);
		noFound.setVisibility(View.GONE);
		loadingBar.setVisibility(View.VISIBLE);
		pageNow = 1;
		// 通知Searchviewcontroller发送搜索请求
		Map<String, String> data = new HashMap<String, String>();
		data.put("keyword", searchKeyEt.getText().toString().trim());
		data.put("pageNo", String.valueOf(DEFAULT_PAGENO));
		TARequest request = new TARequest(SearchController.START_SEARCH, data);
		mFoot.showLoading();
		TAApplication.getApplication().doCommand(
				getContext().getString(R.string.searchviewcontroller), request,
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {
						isSearching = false; // 搜索完成
						final SearchDataBean bean = (SearchDataBean) response
								.getData();
						if (bean.mTotalPage <= bean.mPageIndex) {
							mFoot.viewGone();
						}
						showListView(bean);
					}

					@Override
					public void onStart() {
					}

					@Override
					public void onRuning(TAResponse response) {
					}

					@Override
					public void onFailure(TAResponse response) {
					}

					@Override
					public void onFinish() {
					}
				}, true, false);

	}

	/**
	 * 显示应用列表
	 * 
	 * @param bean
	 */
	private void showListView(SearchDataBean bean) {
		loadingBar.setVisibility(View.GONE);
		// 隐藏软键盘
		InputMethodManager imm = (InputMethodManager) getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mListView.getWindowToken(), 0);
		updateAppListView(bean);
	}

	/**
	 * 更新搜索应用列表
	 * 
	 * @param bean
	 */
	protected void updateAppListView(SearchDataBean bean) {

		if (!TextUtils.isEmpty(searchKeyEt.getText().toString().trim())) {

			if (bean.mStatuscode != 0) {
				Toast.makeText(getContext(), "搜索出错", Toast.LENGTH_SHORT).show();
			}
			Log.e("mytest", "搜索成功 更新搜索应用列表");
			mSearchDateBean = bean;
			if (null == mSearchDateBean.mAppList
					|| mSearchDateBean.mAppList.size() <= 0) {
				mListView.setVisibility(View.GONE);
				noFound.setVisibility(View.VISIBLE);
				return;
			}
			updateDownloadState(bean.mAppList);
			noFound.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
			mAdapter.update(mSearchDateBean.mAppList);
			mListView.setSelection(0);
		}

	}

	/**
	 * 用下载任务列表更新应用列表的下载状态
	 */
	private void updateDownloadState(List<AppDataBean> list) {
		if (list == null || list.size() <= 0) {
			return;
		}
		Map<String, DownloadTask> map = DownloadTaskRecorder.getInstance()
				.getDownloadTaskList();
		for (AppDataBean bean : list) {
			if (map.containsKey(bean.downloadUrl)) {
				DownloadTask task = map.get(bean.downloadUrl);
				bean.downloadStatus = task.state;
				bean.alreadyDownloadPercent = task.alreadyDownloadPercent;
			} else {
				bean.downloadStatus = DownloadTask.NOT_START;
				bean.alreadyDownloadPercent = 0;
			}
		}
	}

	/**
	 * 更新关键词ListView
	 * 
	 * @param bean
	 */
	private void updateSearchKeyListView(SearchDataBean bean) {
		mGridView.setVisibility(View.GONE);
		mSearchDateBean = bean;
		if (mSearchDateBean.mSearchKeyList.size() <= 0) {
			return;
		}

		String[] keyList = mSearchDateBean.mSearchKeyList
				.toArray(new String[(mSearchDateBean.mSearchKeyList.size())]);
		keywordAdapter = new ArrayAdapter<String>(this.getContext(),
				android.R.layout.simple_list_item_1, keyList);
		keywordListView.setAdapter(keywordAdapter);
		keywordListView.setOnItemClickListener(mKeywordItemClickListener);
		keywordListView.setVisibility(View.VISIBLE);

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			removeSearchView();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {
		boolean needToUpdate = false;
		if (mSearchDateBean != null && mSearchDateBean.mAppList != null) {
			for (AppDataBean bean : mSearchDateBean.mAppList) {
				if (bean.downloadUrl.equals(downloadTask.url)) {
					needToUpdate = true;
					bean.downloadStatus = downloadTask.state;
					bean.alreadyDownloadPercent = downloadTask.alreadyDownloadPercent;
				}
			}
		}
		if (needToUpdate) {
			mAdapter.update(mSearchDateBean.mAppList);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		super.dispatchTouchEvent(ev);
		return true;
	}

	@Override
	public void onResume() {
		mAdapter.notifyDataSetChanged();
	}

}
