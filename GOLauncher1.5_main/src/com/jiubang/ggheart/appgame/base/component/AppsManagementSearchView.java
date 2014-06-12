package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.HotSearchKeyword;
import com.jiubang.ggheart.appgame.base.bean.SearchHistoryBean;
import com.jiubang.ggheart.appgame.base.data.AppsSearchDownload;
import com.jiubang.ggheart.appgame.base.data.AppsSearchDownload.SearchDataHandler;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.net.InstallCallbackManager;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.appgame.base.utils.SearchKeywordUtil;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.StatisticsData;

/**
 * @author liuxinyang
 * 
 */
public class AppsManagementSearchView extends RelativeLayout
		implements
			OnClickListener,
			OnScrollListener,
			OnItemClickListener {
	// TODO:XIEDEZHI 滑到列表一半就加载下一页
	// TODO:XIEDEZHI 滑动列表底部加上提示
	private static final int MSG_SEARCH_FINISH = 2;
	private static final int MSG_SEARCH_ERROR = 3;
	private static final int MSG_LISTVIEW_REFRESH = 4;

	private EditText mEditText;
	/**
	 * 搜索结果页，包含搜索结果listview和底部一个进度条
	 */
	private FrameLayout mListViewFrame;
	private ListView mListView;
	private ImageButton mBackBtn;
	private Button mSearchBtn;
	private Button mClearBtn;
	/**
	 * 错误提示页
	 */
	private View mErrorTip;
	/**
	 * 进度圈圈view，用于显示了搜索页面，但还没加载出热词榜的时候显示
	 */
	private LinearLayout mProgressView = null;
	private Context mContext = null;
	private LayoutInflater mLayoutInflater = null;
	private String mSearchText = "";
	private int mAccess = -1;
	private AppsSearchDownload mSearchEngine = null;
	private ArrayList<BoutiqueApp> mSearchList = new ArrayList<BoutiqueApp>();
	/**
	 * 加载下一页的进度条
	 */
	private CommonProgress mCommonProgress = null;
	/**
	 * CommonProgress的布局参数
	 */
	private FrameLayout.LayoutParams mCommonProgressLP = null;
	private SearchResultAdapter mAdapter = null;
	private String mSearchId = "";
	// 输入区高度
	private float mSearchAreaH = DrawUtils.dip2px(48);

	private OnClickListener mBackListener = null;
	/**
	 * 清除历史记录的view，作为mHistoryListView的footerview
	 */
	private AppsManagementCleanHistoryView mHistoryFooterView = null;
	/**
	 * 展示搜索关键字的listview
	 */
	private ListView mHistoryListView = null;
	/**
	 * 搜索关键字数据adapter
	 */
	private AppSearchHistoryAdapter mHistoryAdapter = null;
	/**
	 * 展示热门搜索关键字的listview
	 */
	private ListView mHotKeywordListView = null;
	/**
	 * 没有搜索结果时的提示
	 */
	private ContainerSummaryView mSummaryView = null;
	/**
	 * 热门搜索关键字数据adapter
	 */
	private AppHotKeywordAdapter mHotKeywordAdapter = null;
	/**
	 * 历史关键字列表关键字源，用于避免重复请求关键字列表
	 */
	private String mHistoryKeywordSrc = "";
	/**
	 * 列表滑动底部的toast提示
	 */
	Toast mToast = Toast.makeText(getContext(), R.string.appgame_list_end_tip, Toast.LENGTH_SHORT);
	/*	*//**
			* 输入框焦点事件改变监听者
			*/
	private OnFocusChangeListener mFocusChangeListener = new OnFocusChangeListener() {

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				fillUpSearchKeyword();
			} else {
				mHistoryAdapter.update(null);
				mHistoryListView.setVisibility(View.GONE);
			}
		}
	};
	/**
	 * 输入框输入监听器
	 */
	private TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			mIsStopGetSearchKeyword = false;
			if (s == null || s.toString().equals("")) {
				if (mClearBtn != null) {
					mClearBtn.setVisibility(View.INVISIBLE);
				}
			} else {
				if (mClearBtn != null) {
					mClearBtn.setVisibility(View.VISIBLE);
				}
			}
			if (s != null) {
				// 当输入框文字发生改变后，清除当前搜索结果
				String text = s.toString().trim();
				if (!text.equals(mSearchText)) {
					removeCommonProgress();
					if (mSearchList != null) {
						mSearchList.clear();
						setSearchText("");
						mHandler.sendEmptyMessage(MSG_LISTVIEW_REFRESH);
					}
				}
			}
//			if (s != null && !s.toString().equals("")) {
			fillUpSearchKeyword();
//			} else {
//				mListView.setVisibility(View.GONE);
//				mHistoryListView.setVisibility(View.GONE);
//				showHotSearchKeyword();
//			}
		}
	};
	/**
	 * 输入框点击事件
	 */
	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			fillUpSearchKeyword();
		}
	};
	/**
	 * 历史记录界面和热门搜索关键词界面touch监听器
	 */
	private OnTouchListener mOnTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			showIM(false);
			return false;
		}
	};
	/**
	 * 历史记录列表项和热门搜索关键词列表项点击事件
	 */
	private OnItemClickListener mItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Object object = view.getTag();
			if (object == null) {
				return;
			}
			String key = null;
			if (object instanceof SearchHistoryBean) {
				SearchHistoryBean bean = (SearchHistoryBean) object;
				key = bean.mKeyword;
			} else if (object instanceof HotSearchKeyword) {
				HotSearchKeyword word = (HotSearchKeyword) object;
				key = word.name;
			}
			final String text = key;
			if (text != null && (!text.trim().equals(""))) {
				mListViewFrame.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.VISIBLE);
				mHistoryListView.setVisibility(View.GONE);
				showIM(false);
				// 保存搜索关键字
				Thread thread = new Thread("saveSearchHistory") {
					@Override
					public void run() {
						try {
							SearchKeywordUtil.saveSearchHistory(getContext(), text.trim());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				thread.start();
				// 修改输入框的文字
				mEditText.removeTextChangedListener(mTextWatcher);
				mEditText.setText(text);
				mEditText.setSelection(text.length());
				if (text == null || text.toString().equals("")) {
					if (mClearBtn != null) {
						mClearBtn.setVisibility(View.INVISIBLE);
					}
				} else {
					if (mClearBtn != null) {
						mClearBtn.setVisibility(View.VISIBLE);
					}
				}
				mEditText.addTextChangedListener(mTextWatcher);
				searchApps(text.trim());
			}
		}
	};
	/**
	 * 是否停止搜索关键字联想
	 */
	private volatile boolean mIsStopGetSearchKeyword = false;

	public AppsManagementSearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	private void setSearchText(String text) {
		mSearchText = text;
		if (mAdapter != null) {
			mAdapter.setSearchText(text);
		}
	}

	private void setSearchId(String id) {
		mSearchId = id;
		if (mAdapter != null) {
			mAdapter.setSearchId(id);
		}
	}

	/**
	 * 根据输入框当前文字填充搜索关键字
	 */
	public void fillUpSearchKeyword() {
		String text = mEditText.getText().toString().trim();
		// 避免重复展示关键字填充
		if (text == null
				|| (text.equals(mHistoryKeywordSrc) && !text.equals("")
						&& mHistoryListView.getVisibility() == View.VISIBLE && mHistoryAdapter
						.getCount() > 0)) {
			return;
		}
		mHistoryKeywordSrc = text;
		List<String> list = SearchKeywordUtil.getSearchHistory(mContext, text);
		if (list == null || list.size() <= 0) {
				mHistoryAdapter.update(null);
				mHistoryListView.setVisibility(View.GONE);
		} else {
			List<SearchHistoryBean> beanList = new ArrayList<SearchHistoryBean>();
			for (String str : list) {
				SearchHistoryBean bean = new SearchHistoryBean(
						SearchHistoryBean.SEARCH_KEYWORD_TYPE_HISTORY, str);
				beanList.add(bean);
			}
			mHistoryListView.setVisibility(View.VISIBLE);
			mHistoryAdapter.update(beanList);
			mHistoryListView.setSelection(0);
			if (mHistoryKeywordSrc.equals("")) {
				// 清除历史记录的footerview可见
				mHistoryFooterView.viewVisible();
			}
		}
		if (text != null && (!text.equals(""))) {
			SearchDataHandler handler = new SearchDataHandler() {

				@Override
				public void handleData(final Object object) {
					if (object == null) {
						return;
					}
					post(new Runnable() {

						@Override
						public void run() {
							if (mIsStopGetSearchKeyword) {
								return;
							}
							try {
								Map<String, Object> map = (Map<String, Object>) object;
								String key = (String) map.get("key");
								int id = (Integer) map.get("keysearchid");
								List<String> keylist = (List<String>) map.get("keys");
								if (keylist != null && keylist.size() > 0) {
									if (key.equals(mEditText.getText().toString().trim())) {
										List<SearchHistoryBean> beanList = new ArrayList<SearchHistoryBean>();
										for (String str : keylist) {
											SearchHistoryBean bean = new SearchHistoryBean(
													SearchHistoryBean.SEARCH_KEYWORD_TYPE_NET, str);
											beanList.add(bean);
										}
										mHistoryListView.setVisibility(View.VISIBLE);
										mHistoryAdapter.append(beanList);
										mHistoryListView.setSelection(0);
									}
								}
								//  异步操作，需要实时判断输入框的字符是否为空串，才能判断是否显示清除历史记录
								if (mHistoryKeywordSrc.equals("")) {
									// 清除历史记录的footerview可见
									mHistoryFooterView.viewVisible();
								} else {
									mHistoryFooterView.viewGone();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
			};
			int ty = 1;
			SearchKeywordUtil.getSearchKeyword(getContext(), text, ty, 1, handler);
		} else if (list == null || list.size() <= 0) {
			mListView.setVisibility(View.GONE);
			mListViewFrame.setVisibility(View.GONE);
//			showHotSearchKeyword();
		}
	}

	/**
	 * 读取本地保存的热门搜索关键字展示出来，如果读取失败，则连网再取一次，用于下次展现
	 */
//	public void showHotSearchKeyword() {
//		List<HotSearchKeyword> list = SearchKeywordUtil.getHotSearchKeywords(mContext);
//		if (list == null || list.size() <= 0) {
//			int ty = 1;
////			mHotKeywordListView.setVisibility(View.VISIBLE);
//			SearchKeywordUtil.refreshHotSearchKeywords(getContext(), ty,
//					new DataHandler() {
//						@Override
//						public void handle(Object object) {
//							if (object instanceof JSONObject) {
//								JSONObject json = (JSONObject) object;
//								final List<HotSearchKeyword> tlist = SearchKeywordUtil
//										.parseHotSearchKeywords(json);
//								if (tlist != null && tlist.size() > 0) {
//									AppsManagementSearchView.this.post(new Runnable() {
//
//										@Override
//										public void run() {
//											mProgressView.setVisibility(View.GONE);
//											mHotKeywordListView.setVisibility(View.VISIBLE);
//											mHotKeywordAdapter.update(tlist);
//										}
//									});
//								}
//							}
//						}
//					});
//		} else {
//			mProgressView.setVisibility(View.GONE);
//			mHotKeywordListView.setVisibility(View.VISIBLE);
//			mHotKeywordAdapter.update(list);
//		}
//	}

	public void setAccess(int access) {
		mAccess = access;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mLayoutInflater = LayoutInflater.from(mContext);
		mEditText = (EditText) findViewById(R.id.apps_management_search_edt);
		mEditText.setOnFocusChangeListener(mFocusChangeListener);
		mEditText.addTextChangedListener(mTextWatcher);
		mEditText.setOnClickListener(mOnClickListener);
		mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					onClick(mSearchBtn);
		            return true;
		        }
		        return false;
		    }
		});
		// 搜索结果展示视图
		mListViewFrame = (FrameLayout) findViewById(R.id.apps_management_search_result_list_frame);
		mListView = (ListView) findViewById(R.id.apps_management_search_result_list);
		mBackBtn = (ImageButton) findViewById(R.id.apps_management_search_back_btn);
		mSearchBtn = (Button) findViewById(R.id.apps_management_search_result_btn);
		mClearBtn = (Button) findViewById(R.id.apps_management_search_clear_btn);
		// 历史搜索关键字视图
		mHistoryListView = (ListView) findViewById(R.id.apps_management_search_history);
		mHistoryListView.addFooterView(initHistoryFooterView(), null, false);
		mHistoryFooterView.viewGone();
		mHistoryAdapter = new AppSearchHistoryAdapter(getContext());
		mHistoryListView.setAdapter(mHistoryAdapter);
		mHistoryListView.setOnItemClickListener(mItemClickListener);
		mHistoryListView.setOnTouchListener(mOnTouchListener);
		// 热门搜索关键字视图
		mHotKeywordListView = (ListView) findViewById(R.id.apps_management_search_hotkeyword_list);
		mSummaryView = (ContainerSummaryView) mLayoutInflater.inflate(
				R.layout.appgame_container_summary, null);
		mHotKeywordListView.addHeaderView(mSummaryView, null, false);
		mSummaryView.viewGone();
		View header = mLayoutInflater.inflate(R.layout.appgame_hot_search_keyword_headerview, null);
		mHotKeywordListView.addHeaderView(header, null, false);
		mHotKeywordAdapter = new AppHotKeywordAdapter(getContext());
		mHotKeywordListView.setAdapter(mHotKeywordAdapter);
		mHotKeywordListView.setOnItemClickListener(mItemClickListener);
		mHotKeywordListView.setOnTouchListener(mOnTouchListener);
		// 返回按钮
		mBackBtn.setOnClickListener(this);
		mSearchBtn.setOnClickListener(this);
		mClearBtn.setOnClickListener(this);
		mListView.setOnItemClickListener(this);
		mListView.setOnScrollListener(this);
		// 进度圈圈view
		mProgressView = (LinearLayout) findViewById(R.id.apps_management_progress_view);
		ProgressBar progressBar = (ProgressBar) mProgressView.findViewById(R.id.apps_management_progress);
		progressBar.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
		Drawable drawable = getContext().getResources().getDrawable(R.drawable.go_progress_green);
		progressBar.setIndeterminateDrawable(drawable);
		// 屏蔽搜索热词，所以也屏蔽了进度条
		mProgressView.setVisibility(View.GONE);
		initView();

		// 如果网络不可用，展示错误提示页
		if (!Machine.isNetworkOK(getContext())) {
			showNoNetwordTip();
		}
	}
	
	private View initHistoryFooterView() {
		mHistoryFooterView = (AppsManagementCleanHistoryView) mLayoutInflater.inflate(
				R.layout.appcenter_clean_search_history_view, null);
		mHistoryFooterView.setOnButtonClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SearchKeywordUtil.cleanSearchHistory();
				fillUpSearchKeyword();
			}
		});
		return mHistoryFooterView;
	}

	private void initView() {
		mAdapter = new SearchResultAdapter(getContext());
		mListView.setAdapter(mAdapter);

		mListView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mHistoryListView.setVisibility(View.GONE);
				showIM(false);
				return false;
			}
		});
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
		
		RelativeLayout searchArea = (RelativeLayout) findViewById(R.id.apps_management_search_area); 
		mSearchAreaH = searchArea.getHeight(); 
	}

	/**
	 * 网络不可用时提示错误信息
	 */
	private void showNoNetwordTip() {
		// 如果网络没打开，发消息给MainViewGroup通知监听网络状态打开时自动刷新界面
		AppsManagementActivity.sendHandler("", IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
				IDiyMsgIds.REFRESH_WHEN_NETWORK_OK, -1, null, null);
		try {
			if (mErrorTip == null) {
				ViewStub viewStub = (ViewStub) this
						.findViewById(R.id.apps_management_search_errortip_viewstub);
				viewStub.inflate();
				mErrorTip = this.findViewById(R.id.appgame_error_root);
			}
			if (mErrorTip != null) {
				mErrorTip.setVisibility(View.VISIBLE);
				TextView textview = (TextView) mErrorTip.findViewById(R.id.appgame_error_title);
				textview.setVisibility(View.GONE);
				View retryAndFeedback = mErrorTip.findViewById(R.id.appgame_error_feedback);
				retryAndFeedback.setVisibility(View.GONE);
				Button retryBtn = (Button) mErrorTip.findViewById(R.id.retrybutton);
				retryBtn.setVisibility(View.VISIBLE);
				retryBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						try {
							getContext().startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				retryBtn.setText(R.string.appgame_menu_item_setting);
				View tip = mErrorTip.findViewById(R.id.appgame_error_nettip);
				tip.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		if (mBackBtn == v) {
			removeSelf();
		} else if (mClearBtn == v) {
			mEditText.setText("");
			showIM(true);
		} else if (mSearchBtn == v) {
			//TODO:XIEDEZHI 这里有bug
			mListViewFrame.setVisibility(View.VISIBLE);
			mListView.setVisibility(View.VISIBLE);
			mHistoryListView.setVisibility(View.GONE);
			showIM(false);
			// 保存搜索关键字
			Thread thread = new Thread("saveSearchHistory") {
				@Override
				public void run() {
					try {
						SearchKeywordUtil.saveSearchHistory(getContext(), mEditText
								.getText().toString().trim());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			thread.start();
			searchApps(mEditText.getText().toString().trim());
		}
	}

	private void searchApps(String searchText) {
		mIsStopGetSearchKeyword = true;
		if (searchText != null) {
			searchText = searchText.trim();
		}
		if (searchText == null || searchText.equals("")) {
			Toast.makeText(mContext, mContext.getString(R.string.apps_management_search_word_null),
					Toast.LENGTH_SHORT).show();
			mListViewFrame.setVisibility(View.GONE);
			mListView.setVisibility(View.GONE);
			return;
		}
		if (searchText.equals(mSearchText)) {
			if (mAdapter != null && mAdapter.getCount() > 0) {
				mListViewFrame.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.VISIBLE);
			} else {
				if (mSearchEngine.getIsLoadingNextPage() == false) {
					mListViewFrame.setVisibility(View.GONE);
					mListView.setVisibility(View.GONE);
				} else {
					// 假如关键字已经有请求搜索状态，则直接返回
					return;
				}
			}
			return;
		}
		mListViewFrame.setVisibility(View.VISIBLE);
		mListView.setVisibility(View.VISIBLE);
		setSearchText(searchText);
		StatisticsData.saveSearchKeywordStat(mContext, StatisticsData.SEARCH_ID_APPS,
				mSearchText, false);
		mSearchEngine = new AppsSearchDownload();
		mSearchList.clear();
		mSearchEngine.setIsLoadingNextPage(true);
		loadNextPage();
		if (mSummaryView != null) {
			mSummaryView.viewGone();
			mHotKeywordAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 功能简述:启动线程，获取下一页的数据 功能详细描述: 注意:
	 */
	private void loadNextPage() {
		if (TextUtils.isEmpty(mSearchText)) {
			return;
		}
		int searchType = AppsSearchDownload.SEARCH_TYPE_ALL;
		int priceType = AppsSearchDownload.PRICE_TYPE_FREE;
		int pageId = mSearchEngine.mCurPage;
		if (pageId == -1) {
			pageId = 0;
		}
		// 数据处理者
		AppsSearchDownload.SearchDataHandler handler = new AppsSearchDownload.SearchDataHandler() {

			@Override
			public void handleData(Object object) {
				ArrayList<BoutiqueApp> list = null;
				try {
					list = (ArrayList<BoutiqueApp>) object;
				} catch (Exception e) {
					e.printStackTrace();
				}
				setSearchId(String.valueOf(mSearchEngine.mSearchId));
				// -----------------统计START-----------------------//
				final ArrayList<BoutiqueApp> flist = list;
				new Thread("SearchSaveIssue") {
					public void run() {
						try {
							if (flist != null && flist.size() > 0) {
								List<String> packageNames = new ArrayList<String>();
								List<String> categoryids = new ArrayList<String>();
								List<Integer> indexs = new ArrayList<Integer>();
								for (int i = 0; i < flist.size(); i++) {
									BoutiqueApp app = flist.get(i);
									packageNames.add(app.info.packname);
									categoryids.add(String.valueOf(app.typeid));
									indexs.add(mAdapter.getCount() + i + 1);
								}
								AppRecommendedStatisticsUtil.getInstance().saveAppIssueDataList(
										getContext(), packageNames, categoryids, indexs);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					};
				}.start();
				// -----------------统计END-----------------------//
				Message msg = new Message();
				if (list != null) {
					msg.what = MSG_SEARCH_FINISH;
				} else {
					msg.what = MSG_SEARCH_ERROR;
				}
				msg.obj = list;
				mHandler.sendMessage(msg);
			}
		};
		//默认搜索clientId为1，代表应用游戏中心来源
		int clientId = 1;
		if (mAccess == MainViewGroup.ACCESS_FOR_WIDGET_SEARCH) {
			//如果是从widget搜索进入搜索页，clientId改为3
			clientId = 3;
		}
		// 第一次搜索，页数传1,clientId传1，代表应用游戏中心请求数据
		mSearchEngine.getSearchData(mContext, mSearchText,
				searchType, priceType, pageId + 1, handler, clientId);
		showCommonProgress();
	}
	
	/**
	 * 展示浮在列表底部的进度条
	 */
	private void showCommonProgress() {
		if (mCommonProgress == null) {
			mCommonProgress = (CommonProgress) mLayoutInflater.inflate(
					R.layout.appgame_common_progress, null);
		}
		if (mListView.getChildCount() <= 0 || mSearchList == null || mSearchList.size() <= 0) {
			mCommonProgressLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					DrawUtils.dip2px(40), Gravity.CENTER);
			mCommonProgress.setBackgroundDrawable(null);
		} else {
			mCommonProgressLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					DrawUtils.dip2px(40), Gravity.BOTTOM);
			mCommonProgress.setBackgroundResource(R.drawable.appgame_commonprogress_bg);
		}
		mListViewFrame.removeView(mCommonProgress);
		mListViewFrame.addView(mCommonProgress, mCommonProgressLP);
		mCommonProgress.setVisibility(View.VISIBLE);
		mCommonProgress.startAnimation(AppGameDrawUtils.getInstance().mCommonProgressAnimation);
	}

	/**
	 * 移除浮在列表底部的进度条
	 */
	private void removeCommonProgress() {
		if (mCommonProgress != null) {
			mCommonProgress.setVisibility(View.GONE);
		}
	}
	
	

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_SEARCH_ERROR :
					//TODO:XIEDEZHI 弹toast提示
					setSearchText("");
					mAdapter.update(mSearchList);
					removeCommonProgress();
					break;
				case MSG_SEARCH_FINISH :
					ArrayList<BoutiqueApp> list = (ArrayList<BoutiqueApp>) msg.obj;
					mSearchList.addAll(list);
					setSearchId(String.valueOf(mSearchEngine.mSearchId));
					// 假如搜索结果个数为零 或者 页数已经达到最大页数
					removeCommonProgress();
					mAdapter.update(mSearchList);
					if (mAdapter.getCount() <= 0) {
						mListViewFrame.setVisibility(View.GONE);
						mListView.setVisibility(View.GONE);
						if (mSummaryView != null && mHotKeywordAdapter != null
								&& mHotKeywordAdapter.getCount() > 0) {
							mSummaryView.viewVisible();
							mSummaryView.fillUp(
									getResources().getString(R.string.appgame_search_noresult),
									false);
							mHotKeywordAdapter.notifyDataSetChanged();
							mHotKeywordListView.setSelection(0);
						}
					} else {
						if (mSummaryView != null) {
							mSummaryView.viewGone();
							mHotKeywordAdapter.notifyDataSetChanged();
						}
					}
					break;
				case MSG_LISTVIEW_REFRESH :
					if (mAdapter != null) {
						mAdapter.update(mSearchList);
					}
					removeCommonProgress();
					break;
			}
		}
	};

	/**
	 * 功能简述:提供给外层的接口，用于控制adapter刷新 功能详细描述: 注意:
	 */
	public void doRefresh() {
		mHandler.sendEmptyMessage(MSG_LISTVIEW_REFRESH);
	}

	public void notifyDownloadState(DownloadTask downloadTask) {
		// 当前APP在可视范围才调notifyDataSetChanged
		if (mSearchList != null && downloadTask != null) {
			// 这里判断下载中的应用是否在可视范围，在的话才调用notifyDataSetChanged
			int firstIndex = mListView.getFirstVisiblePosition();
			int lastIndex = mListView.getLastVisiblePosition();
			Long id = downloadTask.getId();
			int count = mAdapter.getCount();
			for (int i = 0; i < count; i++) {
				BoutiqueApp app = (BoutiqueApp) mAdapter.getItem(i);
				long appid = 0;
				try {
					appid = Long.valueOf(app.info.appid);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				if (id == appid) {
					app.downloadState.alreadyDownloadPercent = downloadTask
							.getAlreadyDownloadPercent();
					app.downloadState.state = downloadTask.getState();
					// 因为页面有一个headerview，所以位置要加上1
					if (i >= firstIndex && i <= lastIndex) {
						mAdapter.notifyDataSetChanged();
					}
					break;
				}
			}
		}
	}

	/**
	 * 是否显示输入法
	 * 
	 * @param show
	 */
	public void showIM(boolean show) {
		InputMethodManager im = (InputMethodManager) getContext().getSystemService(
				Context.INPUT_METHOD_SERVICE);
		if (show) {
			if (!mEditText.isFocused()) {
				mEditText.requestFocus();
			}
			im.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
		} else {
			im.hideSoftInputFromWindow(getApplicationWindowToken(),
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public void setBackClickListener(OnClickListener listener) {
		mBackListener = listener;
	}

	/**
	 * 发送消息，移除自身的View
	 */
	public void removeSelf() {
		showIM(false);
		if (mBackListener != null) {
			mBackListener.onClick(mBackBtn);
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			onClick(mSearchBtn);
			showIM(false);
			return true;
		}
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			removeSelf();
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	public void onAppAction(String pkg, int action) {
		switch (action) {
			case MainViewGroup.FLAG_INSTALL :
			case MainViewGroup.FLAG_UNINSTALL :
				mHandler.sendEmptyMessage(MSG_LISTVIEW_REFRESH);
				break;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
		switch (scrollState) {
			// 如果是滑到底部，加载下一页
			case OnScrollListener.SCROLL_STATE_IDLE : {
				if (view.getLastVisiblePosition() >= (view.getCount() - 3)
						&& mSearchEngine.mCurPage < mSearchEngine.mPageNum) {
					if (mSearchEngine.getIsLoadingNextPage() == false) {
						mSearchEngine.setIsLoadingNextPage(true);
						loadNextPage();
					}
				}

				if (view.getLastVisiblePosition() >= (view.getCount() - 1)
						&& mSearchEngine.mCurPage >= mSearchEngine.mPageNum) {
					mToast.show();
				}
				//列表停止滚动时
				//找出列表可见的第一项和最后一项
				int start = view.getFirstVisiblePosition();
				int end = view.getLastVisiblePosition();
				//如果有添加HeaderView，要减去
				ListView lisView = null;
				if (view instanceof ListView) {
					lisView = (ListView) view;
				}
				if (lisView != null) {
					int headViewCount = lisView.getHeaderViewsCount();
					start -= headViewCount;
					end -= headViewCount;
				}
				if (end >= view.getCount()) {
					end = view.getCount() - 1;
				}
				//对图片控制器进行位置限制设置
				AsyncImageManager.getInstance().setLimitPosition(start, end);
				//然后解锁通知加载
				AsyncImageManager.getInstance().unlock();
			}
				break;
			case OnScrollListener.SCROLL_STATE_FLING : {
				//列表在滚动，图片控制器加锁
				AsyncImageManager.getInstance().lock();
			}
				break;
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL : {
				//列表在滚动，图片控制器加锁
				AsyncImageManager.getInstance().lock();
			}
				break;
			default :
				break;
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// 因为有headerview,所以位置自减1
		int index = position;
		if (index >= 0 && index < mAdapter.getCount()) {
			BoutiqueApp app = (BoutiqueApp) mAdapter.getItem(index);
			// 判断treatment的值
			if (app.info.treatment > 0) {
				InstallCallbackManager.saveTreatment(app.info.packname, app.info.treatment);
			}
			// 判断是否需要安装成功之后回调
			if (app.info.icbackurl != null && !app.info.icbackurl.equals("")) {
				InstallCallbackManager.saveCallbackUrl(app.info.packname, app.info.icbackurl);
			}
			StatisticsData.saveSearchKeywordStat(mContext, StatisticsData.SEARCH_ID_APPS,
					mSearchText, true);
			//默认详情来源为应用中心
			int startType = AppsDetail.START_TYPE_APPRECOMMENDED;
			if (mAccess == MainViewGroup.ACCESS_FOR_WIDGET_SEARCH) {
				//如果是从widget搜索进入搜索页，来源改为START_TYPE_GO_SEARCH_WIDGET
				startType = AppsDetail.START_TYPE_GO_SEARCH_WIDGET;
			}
			AppsDetail.jumpToDetail(mContext, app, startType, position, true);
		}
	}

	/**
	 * 判断搜索结果和搜索历史记录是否为空
	 * 
	 * @return
	 */
	public boolean isResultEmpty() {
		if ((mAdapter.getCount() <= 0 || mListView.getVisibility() != View.VISIBLE)
				&& (mHistoryAdapter.getCount() <= 0 || mHistoryListView.getVisibility() != View.VISIBLE)) {
			return true;
		}
		return false;
	}

	/**
	 * 功能简述:清空搜索结果，刷新搜索界面 功能详细描述: 注意:
	 */
	public void cleanSearchData() {
		removeCommonProgress();
		if (mSearchList == null) {
			return;
		}
		mSearchList.clear();
		mAdapter.update(mSearchList);
		mEditText.removeTextChangedListener(mTextWatcher);
		mEditText.setText("");
		if (mClearBtn != null) {
			mClearBtn.setVisibility(View.INVISIBLE);
		}
		setSearchText("");
		mEditText.addTextChangedListener(mTextWatcher);
		mHistoryAdapter.update(null);
		mHistoryListView.setVisibility(View.GONE);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		float evY = ev.getY();
		if (evY < mSearchAreaH) {
			return super.dispatchTouchEvent(ev);
		}
		if (mHistoryListView.getVisibility() == View.VISIBLE || mListView.getVisibility() == View.VISIBLE) {
			return super.dispatchTouchEvent(ev);
		}
		return false;
	}
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		// TODO Auto-generated method stub
//		super.onTouchEvent(event);
//		return true;
//	}
}