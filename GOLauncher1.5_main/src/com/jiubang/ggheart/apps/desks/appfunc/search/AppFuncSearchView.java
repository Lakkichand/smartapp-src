package com.jiubang.ggheart.apps.desks.appfunc.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.core.framework.IFrameworkMsgId;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.component.AppsDetail;
import com.jiubang.ggheart.appgame.base.component.MainViewGroup;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager.AsyncImageLoadedCallBack;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.apps.appfunc.component.ApplicationIcon;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.desks.Preferences.DeskSettingConstants;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.XViewFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.FuncSearchResultItem;
import com.jiubang.ggheart.apps.desks.appfunc.search.SearchNavigationbarIcon.InitialListener;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.gowidget.gostore.views.Keywords;
import com.jiubang.ggheart.apps.gowidget.gostore.views.Keywords.Key;
import com.jiubang.ggheart.apps.gowidget.gostore.views.Keywords.KeyAnimationListener;
import com.jiubang.ggheart.data.statistics.AppRecommendedStatisticsUtil;
import com.jiubang.ggheart.data.statistics.StatisticsAppFuncSearch;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;
import com.jiubang.ggheart.plugin.mediamanagement.MediaPluginFactory;
import com.jiubang.ggheart.plugin.mediamanagement.inf.AppFuncContentTypes;
import com.jiubang.ggheart.plugin.mediamanagement.inf.OnSwitchMenuItemClickListener;
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2012-8-15]
 */
public class AppFuncSearchView extends RelativeLayout
		implements
			ScreenScrollerListener,
			OnClickListener,
			OnItemClickListener,
			OnScrollListener,
			OnFocusChangeListener,
			KeyAnimationListener,
			OnSwitchMenuItemClickListener,
			InitialListener {

	public static final int LOCAL_ADAPTER_TAG = 0x300;
	public static final int LOCAL_HISTORY_ADAPTER_TAG = 0x301;
	public static final int GOSTORE_ADAPTER_TAG = 0x302;
	public static final int GOSTORE_HISTORY_ADAPTER_TAG = 0x303;
	public static final int KEY_WORDS_ADAPTER_TAG = 0X304;

	public static final int MSG_UPDATE_KEYWORDS = 0x400;
	public static final int MSG_SET_LOCAL_RESULT_ADAPTER = 0x401;
	public static final int MSG_SHOW_GOSTORE_HISTORY = 0x402;
	public static final int MSG_SHOW_GOSTORE_NODATA_VIEW = 0x404;
	public static final int MSG_SHOW_LOCAL_NODATA_VIEW = 0x405;
	public static final int MSG_SET_GOSTORE_RESULT_ADAPTER = 0x406;
	public static final int MSG_DATACHANGE_GOSTORE_ADAPTER = 0x407;
	public static final int MSG_SHOW_GOSTORE_PROGRESS_BAR = 0x408;
	public static final int MSG_CHANGE_IM_STATE = 0x409;
	public static final int MSG_SHOW_LOCAL_HISTORY = 0x410;
	public static final int MSG_SHOW_GOSTORE_LOAD_MORE_PROGRESS = 0x411;
	public static final int MSG_SHOW_INIT_LOAD_MORE_PROGRESS = 0x416;
	public static final int MSG_SHOW_SEARCH_CONNECT_EXCEPTION = 0x417;
	public static final int MSG_MEDIA_DATA_READY = 0x418;
	public static final int MSG_SHOW_LOCAL_PROGRESS_BAR = 0x422;
	public static final int MSG_SHOW_KEY_WORDS = 0x424;
	public static final int MSG_DELETE_SEARCH_HISTORY = 0x425;

	private static final int LOCAL_LIST = 0;
	private static final int GOSTROE_LIST = 1;
	/**
	 * 搜索热词
	 */
	//	private Keywords mKeywordsView;
	/**
	 * 获取更多搜索热词
	 */
	//	private TextView mLuckBtn;
	/**
	 * 本地tab文字
	 */
	private TextView mLocalTitle;
	/**
	 * 网络tab文字
	 */
	private TextView mGoStroeTitle;
	/**
	 * 选项卡按钮
	 */
	private Button mSwitchBtn;
	/**
	 * 主页按钮
	 */
	private Button mHomeBtn;
	/**
	 * 菜单按钮
	 */
	private Button mMenuBtn;
	/**
	 * 本地Tab下划线
	 */
	private ImageView mLocalBaseLine;
	/**
	 * 网络Tab下划线
	 */
	private ImageView mWebBaseLine;
	/**
	 * 本地没有数据的view
	 */
	private TextView mLocalListNoDataView;
	/**
	 * 网络没有数据的view
	 */
	private TextView mGoStoreListNoDataView;
	/**
	 * 控制appfuncsearchview刷新的handler
	 */
	private UIHandler mHandler;
	/**
	 * 包在scroller外层的layout
	 */
	private RelativeLayout mResultScrollerLayout;
	/**
	 * 功能表搜索左右滑动的Scroller
	 */
	private AppfuncsearchScroller mResultScroller;
	/**
	 * 本地ListVIew的外层layout，直接加载布局文件，add到scroller。
	 */
	private RelativeLayout mLocalListLayout;
	/**
	 * 网络ListVIew的外层layout，直接加载布局文件，add到scroller。
	 */
	private RelativeLayout mGoStroeListLayout;
	/**
	 * 本地ListVIew;
	 */
	private ListView mLocalResultList;
	/**
	 * 网络ListVIew;
	 */
	private ListView mGoStroeResultList;
	/**
	 * 网络ListVIew顶部的加载框
	 */
	private LinearLayout mProgressLinearLayout;
	/**
	 * 网络ListVIew底部的更多加载框
	 */
	private LinearLayout mLoadMoreProgressbar;
	/**
	 * 本地ListVIew顶部的加载框
	 */
	private LinearLayout mLocalProgressLayout;
	/**
	 * 输入框文字清楚按钮
	 */
	private Button mClearBtn;
	/**
	 * 系统输入法管理器
	 */
	private InputMethodManager mMethodManager;
	/**
	 * 输入法文字监听
	 */
	private SearchTextWatcher mTextWatcher;
	/**
	 * 本地搜索数据adapter
	 */
	private LocalResultListAdapter mLocalResultListAdapter;
	/**
	 * 本地历史搜索数据adapter
	 */
	private LocalHistoryListAdapter mLocalHistoryListAdapter;
	/**
	 * 网络搜索数据adapter
	 */
	private GoStoreResultListAdapter mGoStoreResultListAdapter;
	/**
	 * 网络搜索历史记录adapter
	 */
	private GoStoreHistoryListAdapter mGoStoreHistoryListAdapter;

	private WebSearchHotKeyListAdapter mHotKeywordAdapter;
	/**
	 * 搜字母搜索导航栏
	 */
	private SearchNavigationbar mNavigationbar;
	/**
	 * 本地Tab
	 */
	private RelativeLayout mLocalTab;
	/**
	 * 网络Tab
	 */
	private RelativeLayout mWebTab;
	/**
	 * 搜索热词
	 */
	private List<FuncSearchResultItem> mKeys;
	//	/**
	//	 * 搜索输入框外层layout，主要控制进入功能表不弹出输入法
	//	 */
	//	private RelativeLayout mEditorLayout;
	/**
	 * 搜索层
	 */
	private AppFuncSearchFrame mSearchFrame;
	/**
	 * 查询关键字，由输入框传进来，用于执行查询操作
	 */
	private String mSearchKey = "";
	private Activity mActivity;
	/**
	 * 搜索输入框
	 */
	private EditText mResultEditor;
	/**
	 * 网络Listview最后的可视项索引
	 */
	private int mVisibleLastIndex = 0;
	/**
	 * 主题控制器
	 */
	private AppFuncThemeController mThemeController;
	/**
	 * 网络图片异步
	 */
	private AsyncImageManager mImgManager = null;

	/**
	 * 选项卡正常图标
	 */
	private Drawable mSwitchBtnIcon = null;
	/**
	 * 选项卡高亮图标
	 */
	private Drawable mSwitchBtnIconLight = null;
	/**
	 * 主页按钮正常图标
	 */
	private Drawable mHomeBtnIcon = null;
	/**
	 * 主页按钮高亮图标
	 */
	private Drawable mHomeBtnIconLight = null;
	/**
	 * 菜单按钮正常图标
	 */
	private Drawable mMenuBtnIcon = null;
	/**
	 * 菜单按钮高亮图标
	 */
	private Drawable mMenuBtnIconLight = null;
	/**
	 * 功能表搜索菜单
	 */
	private AppFuncSearchMenu mSearchMenu = null;

	private LayoutInflater mInflater;

	private Bitmap mDefaultBitmap;

	private RelativeLayout mGetAppBtn;

	public AppFuncSearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mThemeController = AppFuncFrame.getThemeController();
		//		mImageExplorer = AppCore.getInstance().getImageExplorer();
		mActivity = (Activity) context;
		mHandler = new UIHandler();
		mImgManager = AsyncImageManager.getInstance();
		mInflater = LayoutInflater.from(mActivity);
	}

	@Override
	protected void onFinishInflate() {
		initViews();
		if (!XViewFrame.getInstance().isDrawMergeBg()) {
			setBackgroundResource(R.drawable.guide_black_bg);
		}
		super.onFinishInflate();
	}
	/**
	 * <br>功能简述: 初始化所有view
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initViews() {
		mResultScrollerLayout = (RelativeLayout) findViewById(R.id.appfunc_search_result_list_scroller);
		mResultEditor = (EditText) findViewById(R.id.appfunc_search_result_editor);
		mLocalTitle = (TextView) findViewById(R.id.appfunc_search_title_local);
		mGoStroeTitle = (TextView) findViewById(R.id.appfunc_search_title_network);
		mSwitchBtn = (Button) findViewById(R.id.appfunc_search_switch_btn);
		mHomeBtn = (Button) findViewById(R.id.appfunc_search_home_btn);
		mMenuBtn = (Button) findViewById(R.id.appfunc_search_menu_btn);
		mLocalBaseLine = (ImageView) findViewById(R.id.appfunc_search_local_base_line);
		mWebBaseLine = (ImageView) findViewById(R.id.appfunc_search_network_base_line);
		mClearBtn = (Button) findViewById(R.id.appfunc_search_clear_btn);
		mLocalTab = (RelativeLayout) findViewById(R.id.appfunc_search_title_local_layout);
		mWebTab = (RelativeLayout) findViewById(R.id.appfunc_search_title_network_layout);
		//		mEditorLayout = (RelativeLayout) findViewById(R.id.appfunc_search_result_title_layout);
		// add by zhoujun 2012-8-2 cn的某些渠道，不应该显示应用中心，应该替换为go精品
		ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
		if (channelConfig != null) {
			boolean needAppCenter = channelConfig.isNeedAppCenter();
			if (!needAppCenter) {
				mGoStroeTitle.setText(R.string.appfunc_search_tab_title_web_gostore);
			}
		}
		// add by zhoujun 2012-8-2 end

		mLocalTab.setOnClickListener(this);
		mWebTab.setOnClickListener(this);
		mMenuBtn.setOnClickListener(this);
		mSwitchBtn.setOnClickListener(this);
		mHomeBtn.setOnClickListener(this);
		mClearBtn.setOnClickListener(this);

		mResultEditor.setOnFocusChangeListener(this);
		mTextWatcher = new SearchTextWatcher();
		mResultEditor.addTextChangedListener(mTextWatcher);
		mResultEditor.setOnClickListener(this);

		mResultScroller = new AppfuncsearchScroller(getContext(), this);
		mResultScroller.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT));
		mLocalListLayout = (RelativeLayout) mInflater.inflate(R.layout.appfunc_search_result_list,
				null);
		mLocalResultList = (ListView) mLocalListLayout
				.findViewById(R.id.appfunc_search_result_listView);

		SearchNavigationbarIcon navigationbarIcon = (SearchNavigationbarIcon) mLocalListLayout
				.findViewById(R.id.appfunc_search_navigation_bar_icon);
		mNavigationbar = (SearchNavigationbar) mLocalListLayout
				.findViewById(R.id.appfunc_search_navigation_bar);
		mNavigationbar.setInitialListener(navigationbarIcon);
		navigationbarIcon.setInitialListener(this);

		mResultScroller.setSearchNavigationbarIcon(navigationbarIcon);
		mLocalResultList.setOnItemClickListener(this);
		mLocalResultList.setOnScrollListener(this);

		mGoStroeListLayout = (RelativeLayout) mInflater.inflate(
				R.layout.appfunc_search_web_result_list, null);
		mGoStroeResultList = (ListView) mGoStroeListLayout
				.findViewById(R.id.appfunc_search_result_listView);

		//		mKeywordsView = (Keywords) mGoStroeListLayout.findViewById(R.id.appfunc_search_keywords);
		//		mKeywordsView.setDuration(800l);
		//		mKeywordsView.setKeyAnimationListener(this); // 动画监听
		//		mKeywordsView.rubKeywords();

		//		mLuckBtn = (TextView) mGoStroeListLayout.findViewById(R.id.appfunc_search_lucky_btn);
		//		mLuckBtn.setOnClickListener(this);

		mGoStroeResultList.setOnItemClickListener(this);

		mLocalProgressLayout = (LinearLayout) mLocalListLayout
				.findViewById(R.id.appfunc_search_progressbar);
		mLocalProgressLayout.setBackgroundResource(android.R.color.transparent);

		mProgressLinearLayout = (LinearLayout) mGoStroeListLayout
				.findViewById(R.id.appfunc_search_progressbar);
		mProgressLinearLayout.setBackgroundResource(android.R.color.transparent);

		mLocalListNoDataView = (TextView) mLocalListLayout
				.findViewById(R.id.appfunc_search_no_data_view);
		mGoStoreListNoDataView = (TextView) mGoStroeListLayout
				.findViewById(R.id.appfunc_search_no_data_view);
		mGoStoreListNoDataView.setText(R.string.appfunc_search_tip_no_match_data_web);

		mGoStroeResultList.setOnScrollListener(this);
		mResultScrollerLayout.addView(mResultScroller);
		mResultScroller.addScreenView(mLocalListLayout);
		mResultScroller.addScreenView(mGoStroeListLayout);
		mResultScroller.setScreenCount(2);
		mResultScroller.getScreenScroller().setCurrentScreen(1);
		setSearchTab(GOSTROE_LIST);
		mResultScroller.getScreenScroller().setDuration(80);
		mLoadMoreProgressbar = (LinearLayout) mGoStroeListLayout
				.findViewById(R.id.appfunc_search_load_more_progressbar);
		mLoadMoreProgressbar.setBackgroundResource(android.R.color.transparent);
		// mResultScroller.getScreenScroller().setInterpolator(new
		// AccelerateInterpolator(1.5f));
		mMethodManager = (InputMethodManager) mActivity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		//		if (AppFuncUtils.getInstance(mActivity).checkNetwork() == 4) {
		//			mKeywordsView.setVisibility(View.GONE);
		//			mLuckBtn.setVisibility(View.GONE);
		//		} else if (!mLocal.contains("zh") & !mLocal.contains("en")) {
		//			mKeywordsView.setVisibility(View.GONE);
		//			mLuckBtn.setVisibility(View.GONE);
		//			showResultLayout();
		//		}

		mLocalResultListAdapter = new LocalResultListAdapter(null);
		mLocalHistoryListAdapter = new LocalHistoryListAdapter(null);
		mGoStoreResultListAdapter = new GoStoreResultListAdapter(null);
		mGoStoreHistoryListAdapter = new GoStoreHistoryListAdapter(null);

		loadResource(GOLauncherApp.getSettingControler().getFunAppSetting().getTabHomeBgSetting());
		// 加载选项卡图标
		mSwitchBtn
				.setBackgroundDrawable(newSelector(mActivity, mSwitchBtnIcon, mSwitchBtnIconLight));
		// 加载home键图标
		mHomeBtn.setBackgroundDrawable(newSelector(mActivity, mHomeBtnIcon, mHomeBtnIconLight));
		// 加载菜单键图标
		mMenuBtn.setBackgroundDrawable(newSelector(mActivity, mMenuBtnIcon, mMenuBtnIconLight));

		mDefaultBitmap = ((BitmapDrawable) mActivity.getResources().getDrawable(
				R.drawable.default_icon)).getBitmap();

		mGetAppBtn = new RelativeLayout(mActivity);
		TextView textView = new TextView(mActivity);
		android.widget.AbsListView.LayoutParams relativeParams = new android.widget.AbsListView.LayoutParams(
				LayoutParams.MATCH_PARENT, DrawUtils.dip2px(60));
		LayoutParams params = new LayoutParams(DrawUtils.dip2px(200), DrawUtils.dip2px(40));
		params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		mGetAppBtn.setLayoutParams(relativeParams);
		textView.setBackgroundDrawable(mActivity.getResources().getDrawable(
				R.drawable.appfunc_search_luckey_btn_selector));
		textView.setGravity(Gravity.CENTER);
		textView.setText(R.string.appfunc_search_get_more_apps);
		textView.setTextColor(0xff7AA300);
		textView.setLayoutParams(params);
		mGetAppBtn.addView(textView);
		mGetAppBtn.setAddStatesFromChildren(true);
		mGetAppBtn.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
		textView.setDuplicateParentStateEnabled(false);
		textView.setClickable(true);
		textView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mResultScroller != null) {
					AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mActivity,
							AppRecommendedStatisticsUtil.ENTRY_TYPE_APPFUNC_SEARCH);
					AppsManagementActivity.startAppCenter(mActivity,
							MainViewGroup.ACCESS_FOR_APPFUNC_SEARCH, true);
				}
			}
		});
	}
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  dingzijian
	 * @date  [2012-8-15]
	 */
	private class LocalResultListAdapter extends SearchListBaseAdapter {
		LayoutInflater mInflater = mActivity.getLayoutInflater();
		LocalListViewHolder mHolder;
		public LocalResultListAdapter(List<FuncSearchResultItem> dataSource) {
			super(dataSource);

		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (mDataSource == null || mDataSource.isEmpty()) {
				return convertView;
			}
			final FuncSearchResultItem itemInfo = (FuncSearchResultItem) mDataSource.get(position);
			if (itemInfo.mType == FuncSearchResultItem.ITEM_TYPE_RESULT_HEADER) {
				View headerView = mInflater.inflate(R.layout.appfunc_search_headerview, null);
				TextView textView = (TextView) headerView
						.findViewById(R.id.appfunc_search_header_text);
				textView.setText(itemInfo.mTitle);
				DeskSettingConstants.setTextViewTypeFace(textView);
				return headerView;
			}
			if (convertView == null
					|| convertView.getId() != R.id.appfunc_search_result_item_layout) {
				convertView = mInflater.inflate(R.layout.appfunc_search_result_list_item, parent,
						false);
				mHolder = new LocalListViewHolder();
				//				RelativeLayout layout = (RelativeLayout) convertView
				//						.findViewById(R.id.appfunc_search_result_item_layout);
				//				layout.setTag(LOCAL_ADAPTER_TAG);
				AppFuncSearchImageView iconView = (AppFuncSearchImageView) convertView
						.findViewById(R.id.appfunc_search_result_list_item_icon);
				TextView name = (TextView) convertView
						.findViewById(R.id.appfunc_search_result_list_item_name);
				Button findOutBtn = (Button) convertView
						.findViewById(R.id.appfunc_search_result_list_item_findout);
				ImageView baseLIne = (ImageView) convertView
						.findViewById(R.id.appfunc_search_result_list_item_line);
				mHolder.mIcon = iconView;
				mHolder.mText = name;
				mHolder.mImageView = baseLIne;
				mHolder.mBtn = findOutBtn;
				DeskSettingConstants.setTextViewTypeFace(mHolder.mBtn);
				DeskSettingConstants.setTextViewTypeFace(mHolder.mText);
				convertView.setTag(mHolder);
			} else {
				DeskSettingConstants.setTextViewTypeFace(mHolder.mBtn);
				DeskSettingConstants.setTextViewTypeFace(mHolder.mText);
				mHolder = (LocalListViewHolder) convertView.getTag();
			}
			//			convertView = mInflater(mActivity, R.layout.appfunc_search_result_list_item, null);
			//			RelativeLayout layout = (RelativeLayout) convertView
			//					.findViewById(R.id.appfunc_search_result_item_layout);
			//			layout.setTag(LOCAL_ADAPTER_TAG);
			//			AppFuncSearchImageView iconView = (AppFuncSearchImageView) convertView
			//					.findViewById(R.id.appfunc_search_result_list_item_icon);
			//			TextView name = (TextView) convertView
			//					.findViewById(R.id.appfunc_search_result_list_item_name);
			//			Button findOutBtn = (Button) convertView
			//					.findViewById(R.id.appfunc_search_result_list_item_findout);
			//			ImageView baseLIne = (ImageView) convertView
			//					.findViewById(R.id.appfunc_search_result_list_item_line);
			if (itemInfo.mType == FuncSearchResultItem.ITEM_TYPE_LOCAL_APPS) {
				mHolder.mIcon.setDrawable(itemInfo.mIcon);
				mHolder.mBtn.setVisibility(View.VISIBLE);
				mHolder.mBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						AppFuncSearchFrame.broadCast(AppfuncSearchController.BC_MSG_SAVE_HISTORY,
								AppfuncSearchEngine.HISTORY_TYPE_LOCAL,
								itemInfo.mIntent.toUri(Intent.URI_INTENT_SCHEME), null);
						StatisticsAppFuncSearch.countSearchStatistics(mActivity,
								StatisticsAppFuncSearch.APPFUNC_SEARCH_FIND_OUT_TIMES);
						DeliverMsgManager.getInstance().onChange(
								AppFuncConstants.APP_FUNC_MAIN_VIEW,
								AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
								new Object[] { AppFuncContentTypes.APP });
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IDiyMsgIds.SHOW_APP_DRAWER, -1, null, null);
						DeliverMsgManager.getInstance().onChange(AppFuncConstants.ALLAPPS_GRID,
								AppFuncConstants.APP_GRID_LOCATE_ITEM, itemInfo.mIntent);
						showIM(false, 1);

					}
				});
			} else if (itemInfo.mType == FuncSearchResultItem.ITEM_TYPE_SEARCH_WEB) {
				return mGetAppBtn;
			} else {
				if (MediaPluginFactory.isMediaPluginExist(mActivity)) {
					mHolder.mBtn.setVisibility(View.VISIBLE);
					mHolder.mBtn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							AppFuncSearchFrame.broadCast(
									AppfuncSearchController.BC_MSG_SAVE_HISTORY,
									AppfuncSearchEngine.HISTORY_TYPE_LOCAL, itemInfo.mTitle, null);
							GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									IDiyMsgIds.SHOW_APP_DRAWER, -1, null, null);
							Object[] objs = new Object[2];
							objs[0] = itemInfo.fileInfo;
							objs[1] = Boolean.valueOf(true);
							DeliverMsgManager.getInstance().onChange(
									AppFuncConstants.APP_FUNC_MAIN_VIEW,
									AppFuncConstants.LOCATE_MEDIA_ITEM, objs);
							showIM(false, 0);
						}
					});
				} else {
					mHolder.mBtn.setVisibility(View.GONE);
				}
				mHolder.mIcon.setFileItemInfo(itemInfo.fileInfo);
			}
			if (itemInfo.mIsLastItem) {
				mHolder.mImageView.setVisibility(View.GONE);
			}
			SpannableString string = null;
			if (itemInfo.mMatchIndex > -1 && itemInfo.mMatchWords > 0) {
				string = new SpannableString(itemInfo.mTitle);
				string.setSpan(new ForegroundColorSpan(0xFF3399FF), itemInfo.getMatchIndex(),
						itemInfo.mMatchIndex + itemInfo.mMatchWords,
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				mHolder.mText.setText(string);
			} else {
				mHolder.mText.setText(itemInfo.mTitle);
			}

			return convertView;
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
	private class LocalHistoryListAdapter extends SearchListBaseAdapter {

		public LocalHistoryListAdapter(List<FuncSearchResultItem> dataSource) {
			super(dataSource);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// if (convertView == null || convertView.getId() !=
			// R.id.appfunc_search_result_item_layout) {
			// convertView = mInflater(mActivity,
			// R.layout.appfunc_search_result_list_item, null);
			// }
			if (mDataSource == null || mDataSource.isEmpty()) {
				return convertView;
			}
			final FuncSearchResultItem itemInfo = (FuncSearchResultItem) mDataSource.get(position);
			if (itemInfo.mType == FuncSearchResultItem.ITEM_TYPE_RESULT_HEADER) {
				View headerView = mInflater.inflate(R.layout.appfunc_search_headerview, null);
				TextView textView = (TextView) headerView
						.findViewById(R.id.appfunc_search_header_text);
				textView.setText(itemInfo.mTitle);
				DeskSettingConstants.setTextViewTypeFace(textView);
				return headerView;
			}
			convertView = mInflater.inflate(R.layout.appfunc_search_result_list_item, null);
			RelativeLayout layout = (RelativeLayout) convertView
					.findViewById(R.id.appfunc_search_result_item_layout);
			layout.setTag(LOCAL_HISTORY_ADAPTER_TAG);
			AppFuncSearchImageView iconView = (AppFuncSearchImageView) convertView
					.findViewById(R.id.appfunc_search_result_list_item_icon);
			TextView mName = (TextView) convertView
					.findViewById(R.id.appfunc_search_result_list_item_name);
			Button findOutBtn = (Button) convertView
					.findViewById(R.id.appfunc_search_result_list_item_findout);
			DeskSettingConstants.setTextViewTypeFace(mName);
			DeskSettingConstants.setTextViewTypeFace(findOutBtn);
			if (itemInfo.mType == FuncSearchResultItem.ITEM_TYPE_LOCAL_HISTORY_APPS) {
				iconView.setDrawable(itemInfo.mIcon);
				findOutBtn.setVisibility(View.VISIBLE);
				findOutBtn.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						StatisticsAppFuncSearch.countSearchStatistics(mActivity,
								StatisticsAppFuncSearch.APPFUNC_SEARCH_FIND_OUT_TIMES);
						DeliverMsgManager.getInstance().onChange(
								AppFuncConstants.APP_FUNC_MAIN_VIEW,
								AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
								new Object[] { AppFuncContentTypes.APP });
						GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IDiyMsgIds.SHOW_APP_DRAWER, -1, null, null);
						DeliverMsgManager.getInstance().onChange(AppFuncConstants.ALLAPPS_GRID,
								AppFuncConstants.APP_GRID_LOCATE_ITEM, itemInfo.mIntent);
						showIM(false, 1);
					}
				});
			} else {
				if (MediaPluginFactory.isMediaPluginExist(mActivity)) {
					findOutBtn.setVisibility(View.VISIBLE);
					findOutBtn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
									IDiyMsgIds.SHOW_APP_DRAWER, -1, null, null);
							Object[] objs = new Object[2];
							objs[0] = itemInfo.fileInfo;
							objs[1] = Boolean.valueOf(true);
							DeliverMsgManager.getInstance().onChange(
									AppFuncConstants.APP_FUNC_MAIN_VIEW,
									AppFuncConstants.LOCATE_MEDIA_ITEM, objs);
							showIM(false, 0);
						}
					});
				} else {
					findOutBtn.setVisibility(View.GONE);
				}
				iconView.setFileItemInfo(itemInfo.fileInfo);
			}
			mName.setText(itemInfo.mTitle);
			return convertView;
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
	private class GoStoreResultListAdapter extends SearchListBaseAdapter {

		public String mSearchId;

		public GoStoreResultListAdapter(List<FuncSearchResultItem> dataSource) {
			super(dataSource);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Viewholder viewholder = null;
			if (mDataSource == null || mDataSource.isEmpty()) {
				return convertView;
			}
			//			if (position == getCount() - 1) {
			//				return mFooterViewLayout;
			//			}
			final FuncSearchResultItem itemInfo = (FuncSearchResultItem) mDataSource.get(position);
			if (itemInfo.mType == FuncSearchResultItem.ITEM_TYPE_RESULT_HEADER) {
				View headerView = mInflater.inflate(R.layout.appfunc_search_headerview, null);
				TextView textView = (TextView) headerView
						.findViewById(R.id.appfunc_search_header_text);
				textView.setText(itemInfo.mTitle);
				DeskSettingConstants.setTextViewTypeFace(textView);
				return headerView;
			}
			if (convertView == null
					|| convertView.getId() != R.id.appfunc_search_result_item_layout) {
				convertView = mInflater.inflate(R.layout.appfunc_search_result_web_list_item, null);
				viewholder = new Viewholder();
				viewholder.mImageSwitcher = (ImageSwitcher) convertView
						.findViewById(R.id.search_switcher);
				viewholder.mAppName = (TextView) convertView.findViewById(R.id.search_app_name);
				viewholder.mAppSize = (TextView) convertView.findViewById(R.id.search_app_size);
				viewholder.mRatingBar = (RatingBar) convertView.findViewById(R.id.search_rating);
				viewholder.mVersion = (TextView) convertView.findViewById(R.id.search_version);
				viewholder.mTypeInfo = (TextView) convertView.findViewById(R.id.search_typeinfo);
				convertView.setTag(viewholder);
			} else {
				viewholder = (Viewholder) convertView.getTag();
			}
			final BoutiqueApp appinfo = mDataSource.get(position).recApp;
			if (appinfo != null) {
				viewholder.mAppName.setText(appinfo.info.name);
				viewholder.mAppSize.setText(appinfo.info.size);
				viewholder.mTypeInfo.setText(appinfo.info.typeinfo);
				// 版本号显示
				if (appinfo.info.version == null || appinfo.info.version.equals("")) {
					viewholder.mVersion.setText(" ");
				} else {
					viewholder.mVersion.setText(mActivity.getResources().getString(
							R.string.appgame_version)
							+ appinfo.info.version);
				}
				float grade = appinfo.info.grade / 2.0f;
//				if (grade != viewholder.mRatingBar.getRating()) {
				if (Math.abs(grade - viewholder.mRatingBar.getRating()) > 0.00001) {
					// 星级显示
					viewholder.mRatingBar.setRating(grade);
				}
				// 设置图标
				if (!TextUtils.isEmpty(appinfo.info.icon)) {
					setIcon(position, viewholder.mImageSwitcher, appinfo.info.icon,
							LauncherEnv.Path.APP_MANAGER_ICON_PATH,
							String.valueOf(appinfo.info.icon.hashCode()));
				}
				//				convertView.setId(Integer.valueOf(appinfo.mAppId));
			}
			return convertView;
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
	private class GoStoreHistoryListAdapter extends SearchListBaseAdapter {

		public GoStoreHistoryListAdapter(List<FuncSearchResultItem> dataSource) {
			super(dataSource);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (mDataSource == null || mDataSource.isEmpty()) {
				return convertView;
			}
			final FuncSearchResultItem itemInfo = (FuncSearchResultItem) mDataSource.get(position);
			if (itemInfo.mType == FuncSearchResultItem.ITEM_TYPE_RESULT_HEADER) {
				View headerView = mInflater.inflate(R.layout.appfunc_search_headerview, null);
				TextView textView = (TextView) headerView
						.findViewById(R.id.appfunc_search_header_text);
				textView.setText(itemInfo.mTitle);
				DeskSettingConstants.setTextViewTypeFace(textView);
				return headerView;
			}
			convertView = mInflater.inflate(R.layout.appfunc_search_web_history_list_item, null);
			RelativeLayout layout = (RelativeLayout) convertView
					.findViewById(R.id.appfunc_search_result_item_layout);
			layout.setTag(GOSTORE_HISTORY_ADAPTER_TAG);
			ImageView mIconView = (ImageView) convertView
					.findViewById(R.id.appfunc_search_result_list_item_icon);
			mIconView.setImageDrawable(itemInfo.mIcon);
			TextView mName = (TextView) convertView
					.findViewById(R.id.appfunc_search_result_list_item_name);
			Button findOutBtn = (Button) convertView
					.findViewById(R.id.appfunc_search_result_list_item_findout);
			ImageView baseLIne = (ImageView) convertView
					.findViewById(R.id.appfunc_search_result_list_item_line);
			DeskSettingConstants.setTextViewTypeFace(mName);
			DeskSettingConstants.setTextViewTypeFace(findOutBtn);
			findOutBtn.setVisibility(View.GONE);
			if (itemInfo.mIsLastItem) {
				baseLIne.setVisibility(View.GONE);
			}
			mName.setText(itemInfo.mTitle);
			return convertView;
		}
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2012-12-19]
 */
	private class WebSearchHotKeyListAdapter extends SearchListBaseAdapter {

		public WebSearchHotKeyListAdapter(List<FuncSearchResultItem> dataSource) {
			super(dataSource);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (position < 0 || position >= mDataSource.size()) {
				return convertView;
			}
			FuncSearchResultItem word = mDataSource.get(position);
			if (word.mType == FuncSearchResultItem.ITEM_TYPE_RESULT_HEADER) {
				View headerView = mInflater.inflate(R.layout.appfunc_search_headerview, null);
				TextView textView = (TextView) headerView
						.findViewById(R.id.appfunc_search_header_text);
				textView.setText(word.mTitle);
				DeskSettingConstants.setTextViewTypeFace(textView);
				return headerView;
			}
			convertView = mInflater.inflate(R.layout.appgame_hot_search_keyword_item, null);
			ImageView imgLine = (ImageView) convertView
					.findViewById(R.id.appgame_hot_search_divider);
			imgLine.setVisibility(View.VISIBLE);
			TextView num = (TextView) convertView.findViewById(R.id.appgame_hot_search_keyword_num);
			TextView text = (TextView) convertView
					.findViewById(R.id.appgame_hot_search_keyword_text);
			ImageView img = (ImageView) convertView
					.findViewById(R.id.appgame_hot_search_keyword_img);
			num.setText(position + "");
			if (position >= 0 && position <= 3) {
				num.setBackgroundResource(R.drawable.appgame_hot_keyword_num_hit);
				num.setTextColor(0xFFFFFFFF);
			} else {
				num.setBackgroundResource(R.drawable.appgame_hot_keyword_num_nor);
				num.setTextColor(0xFF343434);
			}
			text.setText(word.mTitle);
			if (word.state == 1) {
				// 新增
				img.setImageResource(R.drawable.appgame_hot_keyword_new);
			} else if (word.state == 2) {
				// 上升
				img.setImageResource(R.drawable.appgame_hot_keyword_up);
			} else if (word.state == 3) {
				// 平稳
				img.setImageResource(R.drawable.appgame_hot_keyword_smooth);
			} else if (word.state == 4) {
				// 下降
				img.setImageResource(R.drawable.appgame_hot_keyword_down);
			} else {
				if (!TextUtils.isEmpty(word.sicon)) {
					// 其他
					setIcon(img, word.sicon, LauncherEnv.Path.APP_MANAGER_ICON_PATH,
							String.valueOf(word.sicon.hashCode()), false);
				}
			}
			convertView.setTag(KEY_WORDS_ADAPTER_TAG);
			return convertView;
		}

	}
	public boolean showIM(boolean show, int needRemove) {
		boolean isHIde = false;
		if (show) {
			mResultEditor.requestFocus();
			isHIde = mMethodManager.showSoftInput(mResultEditor, InputMethodManager.SHOW_IMPLICIT);
		} else {
			isHIde = mMethodManager.hideSoftInputFromWindow(mResultEditor.getWindowToken(), 0);
			if (needRemove == 1) {
				GoLauncher
						.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
								IFrameworkMsgId.REMOVE_FRAME, IDiyFrameIds.APPFUNC_SEARCH_FRAME,
								null, null);
			}
		}
		return isHIde;
	}
	/**
	 * 
	 * <br>类描述:处理所有更新AppfuncsearchVIew的信息
	 * <br>功能详细描述:
	 * 
	 * @author  dingzijian
	 * @date  [2012-8-15]
	 */
	private class UIHandler extends Handler {
		public UIHandler() {
			super(Looper.getMainLooper());
		}

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);
			switch (msg.what) {
				case MSG_UPDATE_KEYWORDS :
					mKeys = (ArrayList<FuncSearchResultItem>) msg.obj;
					if (mHotKeywordAdapter == null) {
						mHotKeywordAdapter = new WebSearchHotKeyListAdapter(mKeys);
					} else {
						mHotKeywordAdapter.updateDataSource(mKeys);
					}
					mGoStroeResultList.setAdapter(mHotKeywordAdapter);
					break;
				case MSG_DATACHANGE_GOSTORE_ADAPTER :
					hideView(mLoadMoreProgressbar, mGoStoreListNoDataView, mProgressLinearLayout);
					mGoStoreResultListAdapter.mSearchId = String.valueOf(msg.arg2);
					mGoStoreResultListAdapter
							.updateDataSource((ArrayList<FuncSearchResultItem>) msg.obj);
					break;
				case MSG_SET_GOSTORE_RESULT_ADAPTER :
					hideKeyWords();
					hideView(mLoadMoreProgressbar, mGoStoreListNoDataView, mProgressLinearLayout);
					mGoStoreResultListAdapter.mSearchId = String.valueOf(msg.arg2);
					mGoStoreResultListAdapter
							.updateDataSource((ArrayList<FuncSearchResultItem>) msg.obj);
					mGoStroeResultList.setAdapter(mGoStoreResultListAdapter);
					break;
				case MSG_SHOW_GOSTORE_HISTORY :
					hideKeyWords();
					hideView(mLoadMoreProgressbar, mGoStoreListNoDataView, mProgressLinearLayout);
					mGoStoreHistoryListAdapter
							.updateDataSource((ArrayList<FuncSearchResultItem>) msg.obj);
					mGoStroeResultList.setAdapter(mGoStoreHistoryListAdapter);
					break;
				case MSG_SHOW_GOSTORE_PROGRESS_BAR :
					hideKeyWords();
					//隐藏掉没有数据与底部加载进度
					hideView(mGoStoreListNoDataView, mLoadMoreProgressbar);
					//开始新的搜索把所有数据都清空。
					mGoStoreResultListAdapter.updateDataSource(null);
					mGoStroeResultList.setAdapter(null);

					showView(mProgressLinearLayout);
					break;
				case MSG_SHOW_GOSTORE_NODATA_VIEW :
					hideKeyWords();
					hideView(mLoadMoreProgressbar, mProgressLinearLayout);
					String text = "";
					if (msg.obj instanceof String) {
						text = (String) msg.obj;
						mGoStoreListNoDataView.setText(text);
					} else {
						break;
					}
					if (text.equals(mActivity.getString(R.string.appfunc_search_no_history))) {
						mGoStoreHistoryListAdapter.updateDataSource(null);
						mGoStroeResultList.setAdapter(mGoStoreHistoryListAdapter);
					} else {
						mGoStoreResultListAdapter.updateDataSource(null);
						mGoStroeResultList.setAdapter(null);
					}
					showView(mGoStoreListNoDataView);
					break;
				case MSG_SHOW_LOCAL_NODATA_VIEW :
					hideView(mLocalProgressLayout);
					String noDataText = "";
					if (msg.obj instanceof String) {
						noDataText = (String) msg.obj;
						mLocalListNoDataView.setText(noDataText);
					} else {
						break;
					}
					if (noDataText.equals(mActivity.getString(R.string.appfunc_search_no_history))) {
						mLocalHistoryListAdapter.updateDataSource(null);
						mLocalResultList.setAdapter(mLocalHistoryListAdapter);
					} else {
						mLocalResultListAdapter.updateDataSource(null);
						mLocalResultList.setAdapter(null);
					}
					showView(mLocalListNoDataView);
					break;
				case MSG_SET_LOCAL_RESULT_ADAPTER :
					hideView(mLocalProgressLayout, mLocalListNoDataView);
					mLocalResultListAdapter
							.updateDataSource((ArrayList<FuncSearchResultItem>) msg.obj);
					mLocalResultList.setAdapter(mLocalResultListAdapter);
					break;
				case MSG_SHOW_LOCAL_HISTORY :
					hideView(mLocalProgressLayout, mLocalListNoDataView);
					mLocalHistoryListAdapter
							.updateDataSource((ArrayList<FuncSearchResultItem>) msg.obj);
					mLocalResultList.setAdapter(mLocalHistoryListAdapter);
					break;
				case MSG_CHANGE_IM_STATE :
					showIM((Boolean) msg.obj, msg.arg1);
					break;
				case MSG_SHOW_GOSTORE_LOAD_MORE_PROGRESS :
					showView(mLoadMoreProgressbar);
					break;
				case MSG_SHOW_SEARCH_CONNECT_EXCEPTION :
					Toast.makeText(mActivity, R.string.http_exception, Toast.LENGTH_SHORT).show();
					break;
				case MSG_MEDIA_DATA_READY :
					String key = mResultEditor.getText().toString();
					if (key != null && !"".equals(key)) {
						//搜索本地媒体资源
						boolean isInital = isSearchKeyWithInitial(key);
						if (isInital) {
							key = removeInitialString(key);
						}
						searchLocal(key, isInital);
					} else {
						checkLocalHistory();
					}
					break;
				case MSG_SHOW_LOCAL_PROGRESS_BAR :
					mLocalResultListAdapter.updateDataSource(null);
					mLocalResultList.setAdapter(null);
					showView(mLocalProgressLayout);
					break;
				case MSG_SHOW_KEY_WORDS :
					showKeyWords();
					break;
				case MSG_DELETE_SEARCH_HISTORY :
					if (mGoStroeResultList.getAdapter() instanceof GoStoreHistoryListAdapter) {
						deleteHistory(AppfuncSearchEngine.HISTORY_TYPE_WEB, true);
					} else {
						deleteHistory(AppfuncSearchEngine.HISTORY_TYPE_WEB, false);
					}
					if (mLocalResultList.getAdapter() instanceof LocalHistoryListAdapter) {
						deleteHistory(AppfuncSearchEngine.HISTORY_TYPE_LOCAL, true);
					} else {
						deleteHistory(AppfuncSearchEngine.HISTORY_TYPE_LOCAL, false);
					}
					break;
				default :
					break;
			}

		}

		private void deleteHistory(int type, boolean isNotifly) {
			AppFuncSearchFrame.broadCast(AppfuncSearchController.BC_MSG_CLEAR_HISTORY, type, null,
					isNotifly);
		}

	}
	private String removeInitialString(String key) {
		String initial = mActivity.getString(R.string.initial);
		if (key.length() == initial.length() + 1) {
			key = key.substring(key.length() - 1, key.length());
		}
		return key;
	}

	private void searchLocal(String key, boolean isInital) {
		//		hideView(mLocalProgressLayout, mLocalListNoDataView);
		AppFuncSearchFrame.broadCast(AppfuncSearchController.BC_MSG_SEARCH_LOACL_RESOURCE,
				AppfuncSearchEngine.SEARCH_ALL_RESOURCE, key.toString(), isInital);
	}

	private boolean isSearchKeyWithInitial(String key) {
		if (key != null && key.startsWith(mActivity.getString(R.string.initial))) {
			return true;
		}
		return false;

	}
	private void searchWeb(String key) {
		hideView(mLoadMoreProgressbar, mGoStoreListNoDataView, mProgressLinearLayout);
		AppFuncSearchFrame.broadCast(AppfuncSearchController.BC_MSG_SEARCH_WEB_APPS, -1, key,
				mGoStoreResultListAdapter.mDataSource);
	}

	private void checkLocalHistory() {
		AppFuncSearchFrame.broadCast(AppfuncSearchController.BC_MSG_CHECK_HISTORY_LOCAL, -1, null,
				null);
	}

	private void checkWebSearchHIstory(boolean isNotifly) {
		hideKeyWords();
		AppFuncSearchFrame.broadCast(AppfuncSearchController.BC_MSG_CHECK_HISTORY_GOSOTRE, -1,
				null, isNotifly);
	}

	private void hideView(View... viewList) {
		for (View view : viewList) {
			view.setVisibility(View.GONE);
		}
	}

	private void showView(View... viewList) {
		for (View view : viewList) {
			view.setVisibility(View.VISIBLE);
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
	private class SearchTextWatcher implements TextWatcher {
		@Override
		public void afterTextChanged(Editable s) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			mSearchKey = s.toString();
			//隐藏清除按钮
			if (mSearchKey.equals("")) {
				mClearBtn.setVisibility(View.GONE);
			} else {
				mClearBtn.setVisibility(View.VISIBLE);
			}
			//如果关键字为“”，则显示历史记录。
			if (!mSearchKey.equals("") && mResultScroller.getCurrentViewIndex() == 0) {
				//搜索本地程序
				searchLocal(mSearchKey, false);
			} else if (mResultScroller.getCurrentViewIndex() == 0) {
				checkLocalHistory();
			} else if (mResultScroller.getCurrentViewIndex() == 1) {

			}
			mNavigationbar.resetTextColor();
		}
	}

	@Override
	public ScreenScroller getScreenScroller() {

		return mResultScroller.getScreenScroller();
	}

	@Override
	public void setScreenScroller(ScreenScroller scroller) {

	}

	@Override
	public void onFlingIntercepted() {

	}

	@Override
	public void onScrollStart() {

	}

	@Override
	public void onFlingStart() {

	}

	@Override
	public void onScrollChanged(int newScroll, int oldScroll) {

	}

	@Override
	public void onScreenChanged(int newScreen, int oldScreen) {
		if (newScreen == GOSTROE_LIST && oldScreen == LOCAL_LIST) {
			if (null == mSearchKey || "".equals(mSearchKey)) {
				mHandler.sendEmptyMessage(MSG_SHOW_KEY_WORDS);
			} else {
				//				mProgressLinearLayout.setVisibility(View.GONE);
				if (isSearchKeyWithInitial(mSearchKey)) {
					mSearchKey = removeInitialString(mSearchKey);
				}
				setSearchKey(mSearchKey);
				searchWeb(mSearchKey);
			}
		}
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		setSearchTab(currentScreen);
		if (currentScreen == LOCAL_LIST) {
			if (null != mSearchKey && !mSearchKey.equals("")) {
				mResultEditor.setText(mSearchKey);
			} else {
				checkLocalHistory();
			}
		}
		Editable editable = mResultEditor.getEditableText();
		if (editable != null) {
			mResultEditor.setSelection(editable.length());
		}
	}

	public void setSearchTab(int tab) {
		switch (tab) {
			case LOCAL_LIST :
				mLocalTitle.setTextColor(0xFF9FD300);
				mGoStroeTitle.setTextColor(0xFFACACAC);
				mWebBaseLine.setImageResource(R.drawable.appfunc_search_bael_line);
				mLocalBaseLine.setImageResource(R.drawable.appfunc_search_base_line_light);
				break;
			case GOSTROE_LIST :
				mLocalTitle.setTextColor(0xFFACACAC);
				mGoStroeTitle.setTextColor(0xFF9FD300);
				mLocalBaseLine.setImageResource(R.drawable.appfunc_search_bael_line);
				mWebBaseLine.setImageResource(R.drawable.appfunc_search_base_line_light);
				break;
			default :
				break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.appfunc_search_switch_btn :
				if (MediaPluginFactory.isMediaPluginExist(GOLauncherApp.getContext())) {
					MediaPluginFactory.getSwitchMenuControler().popupSearchMenu(this);
				} else {

					mSearchFrame.startAnimation(AppFuncSearchFrame.STATE_LEFT);
				}
				break;
			case R.id.appfunc_search_home_btn :
				finishSearchFrame(false);
				break;
			case R.id.appfunc_search_clear_btn :
				if (mResultScroller.getScreenScroller().getCurrentScreen() == GOSTROE_LIST) {
					setSearchKey("");
					checkWebSearchHIstory(false);
				} else {
					mResultEditor.setText("");
				}
				break;
			case R.id.appfunc_search_menu_btn :
				if (mSearchMenu == null) {
					mSearchMenu = new AppFuncSearchMenu(mActivity, mHandler);
				}
				mSearchMenu.show(this);
				break;
			case R.id.appfunc_search_title_local_layout :
				mResultScroller.gotoViewByIndex(0);
				break;
			case R.id.appfunc_search_title_network_layout :
				mResultScroller.gotoViewByIndex(1);
				break;

			case R.id.appfunc_search_result_editor :
				if (mResultScroller.getCurrentViewIndex() == GOSTROE_LIST) {
					checkWebSearchHIstory(true);
				}
				break;
			default :
				break;
		}
	}

	private void hideKeyWords() {
		//		if (mKeywordsView != null && mKeywordsView.isShown()) {
		//			mKeywordsView.setVisibility(View.GONE);
		//		}
		//		if (mLuckBtn != null && mLuckBtn.isShown()) {
		//			mLuckBtn.setVisibility(View.GONE);
		//		}
	}

	private void showKeyWords() {
		mGoStoreHistoryListAdapter.updateDataSource(null);
		//		mGoStroeResultList.setAdapter(mGoStoreHistoryListAdapter);
		mGoStoreListNoDataView.setVisibility(View.GONE);
		mProgressLinearLayout.setVisibility(View.GONE);
		//		mKeywordsView.setVisibility(View.VISIBLE);
		//		mLuckBtn.setVisibility(View.VISIBLE);
		AppFuncSearchFrame
				.broadCast(AppfuncSearchController.BC_MSG_UPDATE_KEYWORDS, -1, null, null);
		showIM(false, 0);
	}
	@Override
	public void onItemClick(AdapterView<?> item, View view, int positon, long arg3) {
		AppFuncFrame.sVisible = true;
		Adapter adapter = item.getAdapter();
		int viewType = adapter.getItemViewType(positon);
		if (view == null || view.getTag() == null) {
			return;
		}
		if (view.getTag() instanceof LocalListViewHolder) {
			FuncSearchResultItem localitem = (FuncSearchResultItem) adapter.getItem(positon);
			switch (viewType) {
				case FuncSearchResultItem.ITEM_TYPE_LOCAL_APPS :
					ApplicationIcon.sIsStartApp = true;
					AppFuncSearchFrame.broadCast(AppfuncSearchController.BC_MSG_SAVE_HISTORY,
							AppfuncSearchEngine.HISTORY_TYPE_LOCAL,
							localitem.mIntent.toUri(Intent.URI_INTENT_SCHEME), null);
					showIM(false, 0);
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.START_ACTIVITY, -1, localitem.mIntent, null);
					//					DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
					//							AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
					//							new Object[] { MediamanagementTabBasicContent.CONTENT_TYPE_APP });
					break;
				case FuncSearchResultItem.ITEM_TYPE_SEARCH_WEB :
					mResultScroller.gotoViewByIndex(1);
					break;
				default :
					AppFuncSearchFrame.broadCast(AppfuncSearchController.BC_MSG_SAVE_HISTORY,
							AppfuncSearchEngine.HISTORY_TYPE_LOCAL, localitem.mTitle, null);
					GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
							IDiyMsgIds.OPEN_MEIDA_FILE, FunControler.MEDIA_FILE_OPEN_BY_SEARCH,
							localitem.fileInfo, null);
					break;
			}
		} else if (view.getTag() instanceof Viewholder) {
			ApplicationIcon.sIsStartApp = true;
			FuncSearchResultItem localitem = (FuncSearchResultItem) adapter.getItem(positon);
			AppRecommendedStatisticsUtil.getInstance().saveCurrentEnter(mActivity,
					AppRecommendedStatisticsUtil.ENTRY_TYPE_APPFUNC_SEARCH);
			AppsDetail.jumpToDetail(mActivity, localitem.appInfo,
					/*mGoStoreResultListAdapter.mSearchId,*/ AppsDetail.START_TYPE_APPFUNC_SEARCH,
					positon, true);
		} else if ((Integer) view.getTag() == LOCAL_HISTORY_ADAPTER_TAG) {
			FuncSearchResultItem localitem = (FuncSearchResultItem) adapter.getItem(positon);
			switch (viewType) {
				case FuncSearchResultItem.ITEM_TYPE_LOCAL_HISTORY_APPS :
					ApplicationIcon.sIsStartApp = true;
					showIM(false, 0);
					GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME,
							IDiyMsgIds.START_ACTIVITY, -1, localitem.mIntent, null);
					//					DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
					//							AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
					//							new Object[] { MediamanagementTabBasicContent.CONTENT_TYPE_APP });
					break;
				default :
					GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME,
							IDiyMsgIds.OPEN_MEIDA_FILE, FunControler.MEDIA_FILE_OPEN_BY_SEARCH,
							localitem.fileInfo, null);
					break;
			}
		} else if ((Integer) view.getTag() == GOSTORE_HISTORY_ADAPTER_TAG) {
			FuncSearchResultItem resultItem = (FuncSearchResultItem) adapter.getItem(positon);
			switch (viewType) {
				case FuncSearchResultItem.ITEM_TYPE_WEB_HISTORY :
					mSearchKey = resultItem.mTitle;
					mResultEditor.setText(mSearchKey);
					searchWeb(mSearchKey);
					break;
				case FuncSearchResultItem.ITEM_TYPE_WEB_KEY_WORDS :
					mHandler.sendEmptyMessage(AppFuncSearchView.MSG_SHOW_KEY_WORDS);
					break;
			}
		} else if ((Integer) view.getTag() == KEY_WORDS_ADAPTER_TAG) {
			FuncSearchResultItem hotKeyItem = (FuncSearchResultItem) adapter.getItem(positon);
			switch (viewType) {
				case FuncSearchResultItem.ITEM_TYPE_WEB_KEY_WORDS :
					//					mGoStroeResultList.setAdapter(null);
					mResultEditor.setText(hotKeyItem.mTitle);
					searchWeb(hotKeyItem.mTitle);
					break;
			}
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
			searchWeb(mSearchKey);
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		mVisibleLastIndex = firstVisibleItem + visibleItemCount - 1;

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (view == mGoStroeResultList) {
			Adapter adapter = view.getAdapter();
			if (adapter == null) {
				return;
			}
			// 数据集最后一项的索引
			int lastIndex = adapter.getCount() - 1;
			if (mVisibleLastIndex == lastIndex && adapter instanceof GoStoreResultListAdapter) {
				// 如果是自动加载,可以在这里放置异步加载数据的代码
				AppFuncSearchFrame.broadCast(AppfuncSearchController.BC_MSG_LOAD_MORE_WEB_APPS, -1,
						null, null);
			}
		}
		if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL && mMethodManager != null
				&& mMethodManager.isActive()) {
			showIM(false, 0);
		}
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		switch (v.getId()) {
			case R.id.appfunc_search_result_editor :
				if (hasFocus) {

					//					mHandler.postDelayed(new Runnable() {
					//						
					//						@Override
					//						public void run() {
					//							showIM(false, 0);
					//							
					//						}
					//					}, 500);
					if (mResultScroller.getCurrentViewIndex() == 0 && mSearchKey.equals("")) {
						checkLocalHistory();
					}
					if (mResultScroller.getCurrentViewIndex() == GOSTROE_LIST) {
						checkWebSearchHIstory(true);
					}
				}
				break;
			default :
				break;
		}
	}

	public UIHandler getUIHandler() {
		if (mHandler == null) {
			mHandler = new UIHandler();
		}
		return mHandler;
	}

	@Override
	public void isFinish(boolean ok) {

	}

	@Override
	public void onTouch(Key key) {
		setSearchTab(GOSTROE_LIST);
		mResultScroller.getScreenScroller().setCurrentScreen(1);
		mResultEditor.setText(key.key);
		mResultEditor.requestFocus();
		searchWeb(key.key);
	}

	private synchronized void feedKeywords(Keywords keywords, List<Key> list) {
		if (list == null || list.isEmpty() || keywords == null) {
			return;
		}
		// 打乱List里的内容
		Collections.shuffle(list);
		int count = (list.size() > Keywords.MAX) ? Keywords.MAX : list.size();
		keywords.rubKeywords();
		for (int i = 0; i < count; i++) {
			Key tmp = list.get(i);
			keywords.feedKeyword(tmp);
		}
		//		mKeywordsView.go2Show(Keywords.ANIMATION_OUT);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		resetKeywords();
	}

	public void resetKeywords() {
		//		if (mKeywordsView == null) {
		//			return;
		//		}
		//		mKeywordsView.rubAllViews();
		//		mHandler.postDelayed(new Runnable() {
		//
		//			@Override
		//			public void run() {
		//				onClick(mLuckBtn);
		//			}
		//		}, 500);
	}

	public void recyle() {
		//		mKeywordsView.getViewTreeObserver().removeGlobalOnLayoutListener(mKeywordsView);
		mResultScroller.recyle(this);
		mLocalTitle.setOnClickListener(null);
		mGoStroeTitle.setOnClickListener(null);
		mMenuBtn.setOnClickListener(null);
		mSwitchBtn.setOnClickListener(null);
		mHomeBtn.setOnClickListener(null);
		mClearBtn.setOnClickListener(null);
		//		mLuckBtn.setOnClickListener(null);
		mResultEditor.setOnFocusChangeListener(null);
		mResultEditor.removeTextChangedListener(mTextWatcher);
		mTextWatcher = null;
		mGoStroeResultList.setOnItemClickListener(null);
		mGoStroeResultList.setAdapter(null);
		mGoStroeResultList.setOnScrollListener(null);
		mLocalResultList.setAdapter(null);
		mLocalResultList.setOnItemClickListener(null);
		mLocalResultList.setOnScrollListener(null);
		//		mKeywordsView.rubKeywords();
		//		mKeywordsView.removeKeyAnimationListener(this);
		//		mKeywordsView.removeAllViews();
		mNavigationbar.setInitialListener(null);
		removeAllViews();
		//		mKeywordsView = null;
		mSwitchBtnIcon = null;
		mSwitchBtnIconLight = null;
		mHomeBtnIcon = null;
		mHomeBtnIconLight = null;
		mMenuBtnIcon = null;
		mMenuBtnIconLight = null;
	}

	@Override
	public void preMenuItemClick(int actionId) {
		GoLauncher.sendMessage(this, IDiyFrameIds.APPFUNC_FRAME, IDiyMsgIds.DESTROY_FILE_ENGINE,
				actionId, null, null);
	}

	@Override
	public void postMenuItemClick(int actionId) {
		// 退出搜索层
		if (mSearchFrame != null) {
			mSearchFrame.startAnimation(AppFuncSearchFrame.STATE_LEFT);
		}
	}

	/**  
	 * 设置Selector
	 * */
	public static StateListDrawable newSelector(Context context, Drawable stateNormal,
			Drawable statePressed) {
		StateListDrawable bg = new StateListDrawable();
		// View.PRESSED_ENABLED_STATE_SET
		bg.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled },
				statePressed);
		// View.EMPTY_STATE_SET
		bg.addState(new int[] {}, stateNormal);
		return bg;
	}

	/**
	 * 
	 * 加载资源
	 * @param packageName
	 */
	public void loadResource(String packageName) {
		Drawable dra = mThemeController.getDrawable(
				mThemeController.getThemeBean().mHomeBean.mHomeUnSelected, packageName);
		if (dra != null) {
			mHomeBtnIcon = dra;
		}
		dra = mThemeController.getDrawable(mThemeController.getThemeBean().mHomeBean.mHomeSelected,
				packageName);
		if (dra != null) {
			mHomeBtnIconLight = dra;
		}

		dra = mThemeController.getDrawable(
				mThemeController.getThemeBean().mAllAppDockBean.mHomeMenu, packageName);
		if (dra != null) {
			mMenuBtnIcon = dra;
		}
		dra = mThemeController.getDrawable(
				mThemeController.getThemeBean().mAllAppDockBean.mHomeMenuSelected, packageName);
		if (dra != null) {
			mMenuBtnIconLight = dra;
		}

		if (MediaPluginFactory.isMediaPluginExist(GOLauncherApp.getContext())) {
			dra = mThemeController
					.getDrawable(mThemeController.getThemeBean().mSwitchButtonBean.mSearchIcon);
		} else {
			dra = mThemeController
					.getDrawable(mThemeController.getThemeBean().mSwitchButtonBean.mAppIcon);
		}
		if (dra != null) {
			mSwitchBtnIcon = dra;
		}
		if (MediaPluginFactory.isMediaPluginExist(GOLauncherApp.getContext())) {
			dra = mThemeController
					.getDrawable(mThemeController.getThemeBean().mSwitchButtonBean.mSearchIconLight);
		} else {
			dra = mThemeController
					.getDrawable(mThemeController.getThemeBean().mSwitchButtonBean.mAppIconLight);
		}
		if (dra != null) {
			mSwitchBtnIconLight = dra;
		}
	}

	public void exitOrShowHistory() {
		if (mResultScroller.getScreenScroller().getCurrentScreen() == LOCAL_LIST) {
			if (mLocalResultList.getAdapter() instanceof LocalHistoryListAdapter) {
				finishSearchFrame(false);
			} else {
				mResultEditor.setText("");
			}
		} else {
			Adapter adapter = mGoStroeResultList.getAdapter();
			if (adapter instanceof GoStoreHistoryListAdapter) {
				//				if (mKeywordsView.isShown()) {
				//					finishSearchFrame(false);
				//				} else {
				mHandler.sendEmptyMessage(MSG_SHOW_KEY_WORDS);
				//				}
			} else if (adapter instanceof WebSearchHotKeyListAdapter) {
				finishSearchFrame(false);
			} else {
				setSearchKey("");
				mHandler.sendEmptyMessage(MSG_SHOW_KEY_WORDS);
			}
		}

	}

	@Override
	public boolean dispatchKeyEventPreIme(KeyEvent event) {
		if (mSearchFrame != null && !mSearchFrame.isTopFrame()) {
			return super.dispatchKeyEventPreIme(event);
		}
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			KeyEvent.DispatcherState state = getKeyDispatcherState();
			if (state != null) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
					state.startTracking(event, this);
					return true;
				} else if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()
						&& state.isTracking(event)) {
					InputMethodManager imm = (InputMethodManager) getContext().getSystemService(
							Context.INPUT_METHOD_SERVICE);
					if (imm != null) {
						if (imm.hideSoftInputFromWindow(getWindowToken(), 0)) {
							if (mGoStroeResultList.getAdapter() instanceof GoStoreHistoryListAdapter
									&& mGoStoreListNoDataView.isShown()) {
								mHandler.sendEmptyMessage(MSG_SHOW_KEY_WORDS);
							}
							return true;
						}
					}
					exitOrShowHistory();
					return true;
				}
			}
		}
		return super.dispatchKeyEventPreIme(event);
	}

	protected void finishSearchFrame(boolean isNeedAnimation) {
		if (isNeedAnimation) {
			mSearchFrame.startAnimation(AppFuncSearchFrame.STATE_LEFT);
		} else {
			GoLauncher.sendMessage(this, IDiyFrameIds.SCHEDULE_FRAME, IDiyMsgIds.REMOVE_FRAME,
					IDiyFrameIds.APPFUNC_SEARCH_FRAME, null, null);
		}
		// 销毁FileEndgine和清空缩略图缓存
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APP_FUNC_MAIN_VIEW,
				AppFuncConstants.ALL_APP_SWITCH_CONTENT_TYPE,
				new Object[] { AppFuncContentTypes.APP });
		// 退出功能表，清空功能表数据
		DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
				AppFuncConstants.EXIT_APPFUNC_FRAME_WITHOUT_ANIMATION, null);

	}
	public String getSearchKey() {
		return mSearchKey;
	}

	public void setSearchKey(String key) {
		mResultEditor.removeTextChangedListener(mTextWatcher);
		mResultEditor.setText(key);
		mResultEditor.addTextChangedListener(mTextWatcher);
		if ("".equals(key)) {
			mClearBtn.setVisibility(View.GONE);
		} else {
			mClearBtn.setVisibility(View.VISIBLE);
		}
		mSearchKey = key;
	}

	@Override
	public void onNavigationUp(String initial) {

		if (initial.equals(SearchNavigationbar.HISTORY)) {
			mResultEditor.setText("");
		} else {
			//搜索本地搜有资源
			setSearchKey(mActivity.getString(R.string.initial) + initial);
//			Editable editable = mResultEditor.getEditableText();
//			if (editable != null) {
//				Selection.setSelection(editable, 0, editable.length());
//			}
			searchLocal(mSearchKey, true);
		}
		StatisticsAppFuncSearch.countSearchStatistics(mActivity,
				StatisticsAppFuncSearch.APPFUNC_SEARCH_NAVIGATION_USED_TIMES);
	}

	@Override
	public void onNavigationDown(String initial) {
		showIM(false, 0);
	}

	public int whichAdapter(int currentTab) {
		switch (currentTab) {
			case GOSTROE_LIST :
				Adapter adapter = mGoStroeResultList.getAdapter();
				if (adapter instanceof GoStoreHistoryListAdapter) {
					return GOSTORE_HISTORY_ADAPTER_TAG;
				} else if (adapter instanceof GoStoreResultListAdapter) {
					return GOSTORE_ADAPTER_TAG;
				} else if (adapter instanceof WebSearchHotKeyListAdapter) {
					return KEY_WORDS_ADAPTER_TAG;
				}
			case LOCAL_LIST :
				if (mLocalResultList.getAdapter() instanceof LocalHistoryListAdapter) {
					return LOCAL_HISTORY_ADAPTER_TAG;
				} else {
					return LOCAL_ADAPTER_TAG;
				}
		}
		return currentTab;

	}

	/**
	 * 清除菜单
	 */
	public void clearMenu() {
		if (mSearchMenu != null) {
			mSearchMenu.dismiss();
			mSearchMenu = null;
		}
		//清除字体
		DeskSettingConstants.selfDestruct(this);
	}
	/**
	 * 
	 * <br>类描述:
	 * <br>功能详细描述:
	 * 
	 * @author  dingzijian
	 * @date  [2012-10-31]
	 */
	static class LocalListViewHolder {
		TextView mText;
		AppFuncSearchImageView mIcon;
		Button mBtn;
		ImageView mImageView;
	}
/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  dingzijian
 * @date  [2012-12-19]
 */
	private class Viewholder {
		public ImageSwitcher mImageSwitcher;
		public TextView mAppName;
		public TextView mAppSize;
		public TextView mTypeInfo;
		public RatingBar mRatingBar = null;
		public TextView mVersion;
	}

	public void setSearchFrame(AppFuncSearchFrame funcSearchFrame) {
		mSearchFrame = funcSearchFrame;
	}

	/**
	 * 多媒体插件安装或卸载
	 * @param object
	 */
	public void mediaPluginChange(Object object) {
		Drawable dra = null;
		if (MediaPluginFactory.isMediaPluginExist(GOLauncherApp.getContext())) {
			dra = mThemeController
					.getDrawable(mThemeController.getThemeBean().mSwitchButtonBean.mSearchIcon);
		} else {
			dra = mThemeController
					.getDrawable(mThemeController.getThemeBean().mSwitchButtonBean.mAppIcon);
		}
		if (dra != null) {
			mSwitchBtnIcon = dra;
		}
		if (MediaPluginFactory.isMediaPluginExist(GOLauncherApp.getContext())) {
			dra = mThemeController
					.getDrawable(mThemeController.getThemeBean().mSwitchButtonBean.mSearchIconLight);
		} else {
			dra = mThemeController
					.getDrawable(mThemeController.getThemeBean().mSwitchButtonBean.mAppIconLight);
		}
		if (dra != null) {
			mSwitchBtnIconLight = dra;
		}
		// 重新加载选项卡图标
		mSwitchBtn
				.setBackgroundDrawable(newSelector(mActivity, mSwitchBtnIcon, mSwitchBtnIconLight));
	}
	/**
	 * 读取图标，然后设到imageview里
	 */
	private void setIcon(final ImageView imageView, String imgUrl, String imgPath, String imgName,
			boolean setDefaultIcon) {
		imageView.setTag(imgUrl);
		Bitmap bm = mImgManager.loadImage(imgPath, imgName, imgUrl, true, false, null,
				new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageView.getTag().equals(imgUrl)) {
							imageView.setImageBitmap(imageBitmap);
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		if (bm != null) {
			imageView.setImageBitmap(bm);
		} else {
			if (setDefaultIcon) {
				imageView.setImageResource(android.R.drawable.sym_def_app_icon);
			} else {
				imageView.setImageDrawable(null);
			}
		}
	}

	private void setIcon(final int position, final ImageSwitcher switcher, String imgUrl,
			String imgPath, String imgName) {

		if (switcher.getTag() != null && switcher.getTag().equals(imgUrl)) {
			return;
		}
		if (switcher.getTag() != null && switcher.getTag().equals(imgUrl)) {
			ImageView image = (ImageView) switcher.getCurrentView();
			Drawable drawable = image.getDrawable();
			if (drawable != null && drawable instanceof BitmapDrawable) {
				BitmapDrawable bDrawable = (BitmapDrawable) drawable;
				if (bDrawable.getBitmap() != null && bDrawable.getBitmap() != mDefaultBitmap) {
					return;
				}
			}
		}
		switcher.setTag(imgUrl);
		switcher.getCurrentView().clearAnimation();
		switcher.getNextView().clearAnimation();
		Bitmap bm = mImgManager.loadImageForList(position, imgPath, imgName, imgUrl, true, false,
				AppGameDrawUtils.getInstance().mMaskIconOperator, new AsyncImageLoadedCallBack() {
					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (switcher.getTag().equals(imgUrl)) {
							Drawable drawable = ((ImageView) switcher.getCurrentView())
									.getDrawable();
							if (drawable instanceof BitmapDrawable) {
								Bitmap bm = ((BitmapDrawable) drawable).getBitmap();
								if (bm == mDefaultBitmap) {
									switcher.setImageDrawable(new BitmapDrawable(imageBitmap));
								}
							}
						} else {
							imageBitmap = null;
							imgUrl = null;
						}
					}
				});
		ImageView imageView = (ImageView) switcher.getCurrentView();
		if (bm != null) {
			imageView.setImageBitmap(bm);
		} else {
			imageView.setImageBitmap(mDefaultBitmap);
		}
	}
}
