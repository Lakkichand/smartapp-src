package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.go.util.scroller.ScreenScroller;
import com.go.util.scroller.ScreenScrollerListener;
import com.jiubang.ggheart.appgame.appcenter.component.AppsManagementActivity;
import com.jiubang.ggheart.appgame.appcenter.contorler.AppsManageViewController;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.bean.TabDataGroup;
import com.jiubang.ggheart.appgame.base.component.AppGameTabsBar.TabObserver;
import com.jiubang.ggheart.appgame.base.data.ClassificationExceptionRecord;
import com.jiubang.ggheart.appgame.base.data.TabDataManager;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.TabController;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.apps.gowidget.gostore.views.ScrollerViewGroup;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * tab显示基类，负责显示一个层级的tab栏数据，显示的内容由TabController控制
 * 
 * @author xiedezhi
 * 
 */
public abstract class TabManageView extends LinearLayout implements ScreenScrollerListener {
	/**
	 * 操作按钮，搜索
	 */
	protected static final int FUNC_BUTTON_FOR_SEARCH = 1;
	/**
	 * 操作按钮，应用排序
	 */
	protected static final int FUNC_BUTTON_FOR_APPSORT = 2;
	/**
	 * 操作按钮，批量删除
	 */
	protected static final int FUNC_BUTTON_FOR_UNINSTALL = 3;
	/**
	 * 入口值，1:从快捷方式进入2:从menu进入3:goStore更新按钮进入4:点击应用图标上面的更新提示进入5:点击一键装机中的按钮进入
	 * 入口类型定义见6:goStore应用中心按钮进入 {@link MainViewGroup}
	 */
	private int mEntranceId = -1;

	protected Context mContext = null;

	protected LayoutInflater mInflater = null;

	/**
	 * container生成器
	 */
	protected ContainerBuiler mBuilder = null;
	/**
	 * 标题栏，在UI2.0中标题栏只用于二级和以下的tab，顶级tab的标题栏用GridTitleBar
	 */
	protected RelativeLayout mTitleBar = null;
	/**
	 * UI2.0顶级tab栏的标题栏，有5个选项区
	 */
	protected GridTitleBar mGridTitleBar = null;
	/**
	 * 顶级tab栏高度dp
	 */
	public static final float GRID_TITLE_BAR_HEIGHT = 52;
	/**
	 * tab头
	 */
	protected AppGameTabsBar mAppGameTabsBar;
	/**
	 * 子tab栏视图容器
	 */
	protected ScrollerViewGroup mScrollerViewGroup = null;
	/**
	 * tab跳转提示页
	 */
	protected TabTipsView mTipsView = null;
	/**
	 * 当前选中的页面位置
	 */
	private int mCurrentIndex = 0;
	/**
	 * 所有页面
	 */
	protected List<IContainer> mContainers = new ArrayList<IContainer>();
	/**
	 * 当前tab栏的数据列表
	 */
	protected TabDataGroup mTabDataGroup = null;
	/**
	 * 该层级标题
	 */
	protected TextView mTitleText = null;
	/**
	 * 该层级ICON
	 */
	//	protected ImageView mIcon = null;
	/**
	 * 返回按钮
	 */
	protected ImageView mBackButton = null;
	/**
	 * 搜索按钮、排序按钮、忽略更新按钮
	 */
	protected ImageView mOperatorButton;
	/**
	 * 子container的布局属性
	 */
	private LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
	/**
	 * 用于在主线程更新tab栏数据
	 */
	private Handler mHandler = null;
	/**
	 * 更新界面的Runnable
	 */
	private UpdateContentRunnable mUpdateContentRunnable = null;
	/**
	 * 重试按钮监听器
	 */
	private OnClickListener mRetryListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// 刷新当前tab栏
			TabController.refreshCurrentTab();
		}
	};
	/**
	 * 反馈按钮监听器
	 */
	private OnClickListener mFeedbackListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ClassificationExceptionRecord.sendFeedbackMail(mContext);
		}
	};
	/**	
	 * 网络设置点击监听器 
	 */
	private OnClickListener mNetworkSettingListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				getContext().startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	/**
	 * 是否正在更新界面，如果是的话onScrollFinish不处理
	 */
	private volatile boolean mIsUpdating = false;
	/**
	 * 是否第一次更新tab
	 */
	private boolean mIsFirstUpdateTab = true;
	/**
	 * 标识是否是UpdateContent所引起的屏幕滚动切换
	 */
	private boolean mIsUpdateContentChange = false;
	/**
	 * 进入应用中心/游戏中心时的DownloadManager里的所有DownloadTask
	 */
	private ArrayList<DownloadTask> mDownloadTaskList;
	/**
	 * 获取更新数据的状态
	 */
	private int mUpdateState = AppsManageViewController.MSG_ID_NOT_START;
	/**
	 * 可更新应用数据
	 */
	private Object mUpdateData;
	/**
	 * coverflow
	 */
	private GalleryView mCoverFlow;
	/**
	 * coverflow适配器
	 */
	private CoverFlowAdapter mCoverFlowAdapter;
	/**
	 * coverflow选中位置
	 */
	private int mCoverFlowSelection = Integer.MAX_VALUE / 2;
	/**
	 * coverflow选中监听器
	 */
	private OnItemSelectedListener mCoverFlowItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			mCoverFlowSelection = position;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};
	/**
	 * coverflow是否进行着点击事件，如果是则不处理onItemClick
	 */
	private boolean mIsItemClicking = false;
	/**
	 * 广告推荐位
	 */
	private AppGameADBanner mADBanner = null;
	/**
	 * 主线程
	 */
	private Thread mUiThread = null;
	/**
	 * 后台是否正在加载新数据
	 */
	private boolean mIsPrevLoading = false;
	/**
	 * 广告位adapter
	 */
	private AppGameADAdapter mADAdapter;
	/**
	 * 广告位选中位置
	 */
	private int mADSelection = Integer.MAX_VALUE / 2;
	/**
	 * 广告位选中监听器
	 */
	private OnItemSelectedListener mADSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			mADSelection = position;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

	public TabManageView(Context context, int entrance) {
		super(context);
		init(context, entrance);
	}

	public TabManageView(Context context, AttributeSet attrs, int entrance) {
		super(context, attrs);
		init(context, entrance);
	}

	/**
	 * 初始化
	 */
	private void init(Context context, int entranceId) {
		// 拿到主线程
		mUiThread = Thread.currentThread();
		// 初始化成员变量
		mEntranceId = entranceId;
		mContext = context;
		mHandler = new Handler();
		mInflater = LayoutInflater.from(context);
		mBuilder = new ContainerBuiler(context, mInflater);
		// 背景色
		setBackgroundColor(getResources().getColor(R.color.center_background));
		// 把view框架搭好
		initView();
	}

	/**
	 * 初始化view框架，包括标题栏，tab头，tab容器
	 */
	private void initView() {
		this.setOrientation(LinearLayout.VERTICAL);
		if (mContext == null || mContext.getResources() == null) {
			return;
		}

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				DrawUtils.dip2px(50.67f));
		params.weight = 0;

		// 初始化标题栏
		mTitleBar = (RelativeLayout) mInflater.inflate(R.layout.appgame_management_toptitle_layout,
				null);
		mOperatorButton = (ImageView) mTitleBar
				.findViewById(R.id.apps_management_title_sort_button);
		mTitleText = (TextView) mTitleBar.findViewById(R.id.apps_management_title_text);
		mBackButton = (ImageView) mTitleBar.findViewById(R.id.apps_management_title_back_iamge);
		mTitleBar.setBackgroundResource(R.drawable.appgame_titlebar_bg);
		addView(mTitleBar, params);

		// 初始化二级tab栏
		mAppGameTabsBar = new AppGameTabsBar(mContext, new TabObserver() {

			@Override
			public void handleChangeTab(int tabIndex) {
				// 点击tab 要切换scrollerViewGroup
				mScrollerViewGroup.gotoViewByIndexImmediately(tabIndex);
			}
		});
		mAppGameTabsBar.setBackgroundResource(R.drawable.appgame_subtab_bg);
		addView(mAppGameTabsBar);

		// 初始化tab容器
		mScrollerViewGroup = new ScrollerViewGroup(getContext(), this);
		mScrollerViewGroup.setIsNeedGap(true);
		mScrollerViewGroup.setGapColor(mContext.getResources().getColor(
				R.color.app_game_page_gap_color));
		mScrollerViewGroup.setBackgroundColor(getResources().getColor(R.color.center_background));
		params = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, 0);
		params.weight = 1.0f;
		addView(mScrollerViewGroup, params);

		// 初始化提示页
		mTipsView = new TabTipsView(mContext);
		params = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT, 0);
		params.weight = 1.0f;
		mTipsView.setVisibility(View.GONE);
		addView(mTipsView, params);
		mTipsView.setBackgroundColor(getResources().getColor(R.color.center_background));

		// 初始化UI2.0标题栏，用于顶级tab栏
		mGridTitleBar = new GridTitleBar(mContext);
		mGridTitleBar.setBackgroundResource(R.drawable.appgame_topbar_bg);
		mGridTitleBar.setVisibility(View.GONE);
        AppsManagementActivity.sendHandler(mContext,
		IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
		IDiyMsgIds.SHOW_SEARCH_BUTTON, 0,
		null, null);
		params = new LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
				DrawUtils.dip2px(GRID_TITLE_BAR_HEIGHT));
		params.weight = 0;
		addView(mGridTitleBar, params);
	}
	
	/**
	 * 更新当前屏幕选中页面的位置
	 */
	public synchronized void setCurrentIndex(int index) {
		mCurrentIndex = index;
	}

	/**
	 * 获取当前屏幕选中位置
	 */
	public synchronized int getCurrentIndex() {
		return mCurrentIndex;
	}

	/**
	 * 设置进入应用中心/游戏中心时的DownloadManager里的所有DownloadTask
	 */
	public void setmDownloadTasks(ArrayList<DownloadTask> downloadTaskList) {
		this.mDownloadTaskList = downloadTaskList;
		// 可能在拿到downloadTaksList前 Containers 已经初始化
		if (mContainers != null && mContainers.size() > 0) {
			int count = mContainers.size();
			for (int i = 0; i < count; i++) {
				mContainers.get(i).setDownloadTaskList(mDownloadTaskList);
			}
		}
	}

	/**
	 * 返回入口值，1:从快捷方式进入2:从menu进入3:goStore更新按钮进入4:点击应用图标上面的更新提示进入5:点击一键装机中的按钮进入
	 * 入口类型定义见6:goStore应用中心按钮进入{@link MainViewGroup}
	 */
	public int getEntrance() {
		return mEntranceId;
	}

	/**
	 * 设置当前tab栏标题
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		mGridTitleBar.setVisibility(View.GONE);
        AppsManagementActivity.sendHandler(mContext,
		IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
		IDiyMsgIds.SHOW_SEARCH_BUTTON, 0,
		null, null);
		mTitleBar.setVisibility(View.VISIBLE);
		mTitleText.setText(title);
	}

	/**
	 * 把标题栏设为不可见
	 */
	public void removeTitleBar() {
		if (mTitleBar != null) {
			mTitleBar.setVisibility(View.GONE);
		}
	}

	/**
	 * 当本地没有顶级tab栏数据时，为了不要一进去是空的，先显示一个本地写死的顶级标题栏，但不加入点击事件，根据UI2.0新增
	 */
	public void showFakeGridTitleBar() {
		Runnable run = new Runnable() {

			@Override
			public void run() {
				mGridTitleBar.setVisibility(View.VISIBLE);
				AppsManagementActivity.sendHandler(mContext,
						IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
						IDiyMsgIds.SHOW_SEARCH_BUTTON, 1, null, null);
				mTitleBar.setVisibility(View.GONE);
				mGridTitleBar.showDefaultBar();
			}
		};
		if (Thread.currentThread() == mUiThread) {
			run.run();
		} else {
			post(run);
		}
	}

	/**
	 * 清除gridTitleBar的点击事件
	 */
	public void disableGridTitleBar() {
		mGridTitleBar.clearClickListener();
	}

	/**
	 * 获取顶级tab栏
	 */
	public GridTitleBar getGridTitleBar() {
		return mGridTitleBar;
	}

	/**
	 * 
	 * @param show
	 *            true显示应用中心/游戏中心图标，false隐藏应用中心/游戏中心图标
	 */
	public void showIcon(boolean show) {
		//新版本没有了标题栏上的图标
	}

	/**
	 * 
	 * @param show
	 *            true显示应用中心/游戏中心返回按钮，false隐藏应用中心/游戏中心返回按钮
	 */
	public void showBackButton(boolean show) {
		if (show) {
			mBackButton.setVisibility(View.VISIBLE);
		} else {
			mBackButton.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 在列表下方展示一个进度条代表正在后台加载数据
	 */
	public void showPrevLoadProgress() {
		mIsPrevLoading = true;
		if (mContainers != null) {
			for (IContainer container : mContainers) {
				container.prevLoading();
			}
		}
	}
	
	/**
	 * 移除列表下发的进度条表示后台加载数据完毕
	 */
	public void hidePrevLoadProgress() {
		mIsPrevLoading = false;
		if (mContainers != null) {
			for (IContainer container : mContainers) {
				container.prevLoadFinish();
			}
		}
	}

	/**
	 * 如果服务器下发的数据有错，通过这个方法把错误的数据过滤掉
	 */
	private void fixDataGroup(TabDataGroup group) {
		//TODO:XIEDEZHI 把重复的typeId去掉
		if (group == null || group.data == null) {
			return;
		}
		List<ClassificationDataBean> data = new ArrayList<ClassificationDataBean>();
		for (int i = 0; i < group.data.size(); i++) {
			ClassificationDataBean bean = group.data.get(i);
			IContainer container = mBuilder.getContainer(bean);
			if (container != null) {
				data.add(bean);
			}
		}
		if (data.size() <= 0) {
			group.position = 0;
		} else {
			if (group.position > (data.size() - 1)) {
				group.position = data.size() - 1;
			}
		}
		group.data = data;
	}

	/**
	 ＊ 如果数据中有coverflow数据，展示coverflow，否则把coverflow移除
	 */
	private void showCoverFlow(TabDataGroup group) {
		if (group == null || group.coverFlowBean == null || group.coverFlowBean.featureList == null
				|| group.coverFlowBean.featureList.size() <= 0) {
			mAppGameTabsBar.big();
			if (mCoverFlow != null) {
				mCoverFlow.setVisibility(View.GONE);
				this.removeView(mCoverFlow);
			}
		} else {
			mAppGameTabsBar.small();
			ClassificationDataBean bean = group.coverFlowBean;
			if (mCoverFlow == null) {
				mCoverFlow = new GalleryView(getContext());
				mCoverFlow.setCallbackDuringFling(false);
				mCoverFlow.setOnItemSelectedListener(mCoverFlowItemSelectedListener);
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT, DrawUtils.dip2px(126.6666667f));
				this.addView(mCoverFlow, 0, lp);
				mCoverFlowAdapter = new CoverFlowAdapter(getContext());
				mCoverFlowAdapter.update(bean.featureList);
				mCoverFlow.setAdapter(mCoverFlowAdapter);
				mCoverFlow.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						final Object tag = view.getTag(R.id.appgame);
						if (tag != null && tag instanceof BoutiqueApp) {
							if (mIsItemClicking) {
								return;
							}
							mIsItemClicking = true;
							postDelayed(new Runnable() {

								@Override
								public void run() {
									BoutiqueApp app = (BoutiqueApp) tag;
									Context context = mContext;
									if (mCoverFlowAdapter != null) {
										mCoverFlowAdapter.onItemClick(context, app);
									}
									mIsItemClicking = false;
								}
							}, 350);
						}
					}
				});
			} else {
				mCoverFlowAdapter.update(bean.featureList);
				mCoverFlow.setAdapter(mCoverFlowAdapter);
				mCoverFlow.setVisibility(View.VISIBLE);
				if (mCoverFlow.getParent() == null) {
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.FILL_PARENT, DrawUtils.dip2px(126.6666667f));
					this.addView(mCoverFlow, 0, lp);
				}
			}
			mCoverFlow.setSelection(mCoverFlowSelection);
		}
	}
	
	/**
	 * 展示广告推荐位
	 */
	private void showADBanner(TabDataGroup group) {
		if (group == null || group.adBean == null || group.adBean.featureList == null
				|| group.adBean.featureList.size() <= 0) {
			if (mADBanner != null) {
				mADBanner.setVisibility(View.GONE);
				this.removeView(mADBanner);
			}
		} else {
			ClassificationDataBean bean = group.adBean;
			if (mADAdapter == null) {
				mADAdapter = new AppGameADAdapter(getContext());
			}
			if (mADBanner == null) {
				mADBanner = new AppGameADBanner(getContext());
				mADBanner.setOnItemSelectedListener(mADSelectedListener);
			}
			mADAdapter.update(bean.featureList);
			mADBanner.setAdapter(mADAdapter);
			mADBanner.setVisibility(View.VISIBLE);
			if (mADBanner.getParent() == null) {
				int index = TabManageView.this.indexOfChild(mAppGameTabsBar);
				// 广告位放在AppGameTabsBar下面
				if (index != -1) {
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.FILL_PARENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					this.addView(mADBanner, index + 1, lp);
				}
			}
			
			mADBanner.setSelection(mADSelection);
		}
	}
	
	/**
	 * 初始化container信息
	 * @param container
	 */
	private void initCotainerInfo(IContainer container, ClassificationDataBean bean) {
		if (container == null) {
			return;
		}
		// 初始化container入口值
		container.initEntrance(mEntranceId);
		// 把当前正的downloadtask设到每个container里
		container.setDownloadTaskList(mDownloadTaskList);
		// container填充数据，填充数据的时候只是把数据缓存起来，到转换成激活状态时才刷新界面
		container.updateContent(bean, false);
		//为每一个container设置可更新应用数据
		container.setUpdateData(mUpdateData, mUpdateState);
		if (mIsPrevLoading) {
			container.prevLoading();
		} else {
			container.prevLoadFinish();
		}
		// 设置ContainerBuilder，暂时只用在EmptyContainer
		container.setBuilder(mBuilder);
	}

	/**
	 * 根据数据更新tab头和scrollerViewGroup
	 * 
	 * @param group
	 *            tab栏数据
	 * @param singleActive
	 * 			  是否只激活当前屏幕
	 */
	private void updateView(TabDataGroup group, boolean singleActive) {
		if (group == null) {
			Log.e("TabManageView", "updateView group == null");
			return;
		}
		//分类页面切换，列表改变了，要重置图片管理器的列表控制信息
		AsyncImageManager.getInstance().restore();
		// 清理标题栏多余的东西
		clearOperatorButton();
		// tab头标题列表设为空
		mAppGameTabsBar.cleanData();
		// 移除所有mScrollerViewGroup的子view
		mScrollerViewGroup.removeAllViews();
		// 清空图标线程池的任务
		AsyncImageManager.getInstance().removeAllTask();
		//展示coverflow
		showCoverFlow(group);
		//展示广告推荐位
		showADBanner(group);
		// 把错误数据过滤
		fixDataGroup(group);
		if (group.data == null || group.data.size() == 0) {
			// 显示错误提示页
			mTipsView.setVisibility(View.VISIBLE);
			mAppGameTabsBar.setVisibility(View.GONE);
			mScrollerViewGroup.setVisibility(View.GONE);
			String title = group.title;
			if (mTitleBar.getVisibility() == View.VISIBLE) {
				title = null;
			}
			boolean networkOK = Machine.isNetworkOK(getContext());
			OnClickListener listener = mRetryListener;
			if (!networkOK) {
				listener = mNetworkSettingListener;
			}
			// 判断SD卡是否有网络错误日志，另外网络是否正常，是则显示反馈按钮
			if (FileUtil
					.isFileExist(LauncherEnv.Path.APP_MANAGER_CLASSIFICATION_EXCEPTION_RECORD_PATH)
					&& Machine.isNetworkOK(mContext)) {
				mTipsView.showRetryErrorTip(networkOK, listener, mFeedbackListener, title);
			} else {
				mTipsView.showRetryErrorTip(networkOK, listener, title);
			}
			if (mCoverFlow != null) {
				mCoverFlow.setVisibility(View.GONE);
			}
			if (mADBanner != null) {
				mADBanner.setVisibility(View.GONE);
			}
			if (!networkOK) {
				// 如果网络没打开，发消息给MainViewGroup通知监听网络状态打开时自动刷新界面
				AppsManagementActivity.sendHandler("",
						IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
						IDiyMsgIds.REFRESH_WHEN_NETWORK_OK, -1, null, null);
			}
			return;
		}
		if (group.position < 0 || group.position >= group.data.size()) {
			group.position = 0;
		}
		// 清除进度条，显示tab栏
		mTipsView.showNothing();
		mTipsView.setVisibility(View.GONE);
		mAppGameTabsBar.setVisibility(View.VISIBLE);
		mScrollerViewGroup.setVisibility(View.VISIBLE);
		// 填充tab头数据
		List<String> titles = new ArrayList<String>();
		for (ClassificationDataBean bean : group.data) {
			titles.add(bean.title);
		}
		mAppGameTabsBar.initTabsBar(titles);
		mAppGameTabsBar.setButtonSelected(group.position, false);
		// 填充各个子tab的数据
		final List<ClassificationDataBean> dataList = group.data;
		//根据分类数据列表生成container并添加到主界面
		for (ClassificationDataBean bean : dataList) {
			if (bean.dataType == ClassificationDataBean.BUTTON_TAB
					|| bean.dataType == ClassificationDataBean.TAB_TYPE) {
				//按钮tab栏展示的双层container
				IContainer container = mBuilder.getContainer(bean);
				//清空container
				container.removeContainers();
				//子container分类信息
				List<CategoriesDataBean> subCBeans = new ArrayList<CategoriesDataBean>();
				//子container列表
				List<IContainer> subIContainers = new ArrayList<IContainer>();
				if (bean.categoriesList != null) {
					for (CategoriesDataBean cbean : bean.categoriesList) {
						int typeId = cbean.typeId;
						ClassificationDataBean subBean = TabDataManager.getInstance().getTabData(typeId);
						if (subBean != null) {
							IContainer subContainer = mBuilder.getContainer(subBean);
							if (subContainer != null) {
								//初始化信息
								initCotainerInfo(subContainer, subBean);
								subCBeans.add(cbean);
								subIContainers.add(subContainer);
							}
						} else {
							Log.e("TabManageView", "MultiContainer subBean == null");
						}
					}
				}
				initCotainerInfo(container, bean);
				//填充container
				container.fillupMultiContainer(subCBeans, subIContainers);
				// 添加container
				addContainer(container);
			} else {
				//普通应用列表container
				IContainer container = mBuilder.getContainer(bean);
				initCotainerInfo(container, bean);
				// 添加container
				addContainer(container);
			}
		}
		mScrollerViewGroup.setScreenCount(mScrollerViewGroup.getChildCount());
		// 如果是单页数据，把scrollerviewgroup设为不可滑动
		ScreenScroller scroller = mScrollerViewGroup.getScreenScroller();
		if (group.data.size() == 1) {
			scroller.setPadding(0);
		} else {
			scroller.setPadding(0.5f);
		}
		// 跳转到首页
		final int position = group.position;
		// 标识是UpdateContent所引起的屏幕滚动切换
		// 如果是顶级TAB栏，并且不是现实第0页，那么会引起Scroll
		// 这是要把这个标志设置为True，防止在onScrollFinish里面再通知各Container激活一次
		// TODO:wangzhuobin 如果是非顶级TAB栏，怎么避免激活两次呢？用
		// mScrollerViewGroup.getScreenScroller().getCurrentScreen()判断
		mIsUpdateContentChange = TabDataManager.getInstance().isTopTab()
				&& (position != mScrollerViewGroup.getScreenScroller().getCurrentScreen());
		mScrollerViewGroup.gotoViewByIndexImmediately(position);
		// 调整标题栏的内容
		showOperatorButton(parseFunButton(group.data.get(position).funbutton));

		// 通知各子页面的激活状态
		int start = position - 1;
		int end = position + 1;
		if (mIsFirstUpdateTab || singleActive) {
			// 如果是第一次刷新界面，则只激活一屏
			start = position;
			end = position;
		}
		for (int i = start; i <= end; i++) {
			if (i >= 0 && i < mContainers.size()) {
				IContainer container = mContainers.get(i);
				container.onActiveChange(true);
			}
		}
		// 通知各子页面，所有的子页面已经初始化完毕
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				for (IContainer container : mContainers) {
					container.onFinishAllUpdateContent();
				}
			}
		});
		setCurrentIndex(group.position);
		
		TabController.mT = System.currentTimeMillis();
	}

	/**
	 * 更新侧面tab栏的界面，根据UI2.0新增
	 * 
	 * @param group
	 *            侧面tab栏的数据
	 * @param targetIndex
	 *            需要更新的侧面tab的下标
	 * @param targetSubIndex
	 *            指定需要跳转的子层级页面下标
	 */
	private void updateContentSide(TabDataGroup group, int targetIndex, int targetSubIndex) {
		if (group == null || mTabDataGroup == null) {
			// group不能为空
			Log.e("TabManageView", "updateContentSide group == null");
			return;
		}
		// 统计，切换侧面tab栏时如果不会发生scroll就把首页显示的页面统计一次点击
		int currentScreen = mScrollerViewGroup.getScreenScroller().getCurrentScreen();
		int position = group.position;
		if (currentScreen == position && group.data != null && group.data.size() > position) {
			final int typeId = group.data.get(position).typeId;
			if (typeId != -1) {
				// Log.e("XIEDEZHI", "typeid = " + typeId + "  name = "
				// + group.titles.get(currentScreen));
				// 统计点击数
				AppsManagementActivity.sendHandler(mContext,
						IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
						IDiyMsgIds.SAVE_TAB_CLICK, typeId, null, null);
			}
		}
		// 调整显示的首页面
		if (targetSubIndex >= 0 && group.data != null && targetSubIndex < group.data.size()) {
			group.position = targetSubIndex;
		}
		// 更新GridTitleBar选择状态
		mGridTitleBar.setSelection(targetIndex);
		// 更新数据
		mTabDataGroup.position = targetIndex;
		mTabDataGroup.subGroupList.set(targetIndex, group);
		mTabDataGroup.data = group.data;
		// 把GridTitleBar设为可见
		mGridTitleBar.setVisibility(View.VISIBLE);
        AppsManagementActivity.sendHandler(mContext,
		IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
		IDiyMsgIds.SHOW_SEARCH_BUTTON, 1,
		null, null);
		mTitleBar.setVisibility(View.GONE);
		// 更新界面
		updateView(group, true);
	}

	/**
	 * 同步到UI线程更新侧面tab栏的界面，根据UI2.0新增
	 * 
	 * @param group
	 *            侧面tab栏的数据
	 * @param targetIndex
	 *            需要更新的侧面tab的下标
	 * @param targetSubIndex
	 *            指定跳转到目标层级的哪个子层级页面，仅在跳转顶级双层tab栏时有效，如果不需要指定则传-1
	 */
	public void updateContentSideAsyn(TabDataGroup group, int targetIndex,
			int targetSubIndex) {
		if (mUpdateContentRunnable != null) {
			// 先把之前的runnable杀死
			mUpdateContentRunnable.kill();
		}
		if (mHandler != null) {
			mHandler.removeCallbacks(mUpdateContentRunnable);
			mUpdateContentRunnable = new UpdateContentRunnable(
					UpdateContentRunnable.TYPE_UPDATECONTENT_SIDE, group,
					targetIndex, targetSubIndex);
			// 如果是首次展示侧面tab栏，也就是首次进入，就直接run，加快进入速度
			if (Thread.currentThread() == mUiThread && mIsFirstUpdateTab) {
				mUpdateContentRunnable.run();
			} else {
				mHandler.post(mUpdateContentRunnable);
			}
		}
	}

	/**
	 * 更新tab栏数据
	 * 
	 * @param group
	 *            新层级tab栏数据
	 * @param targetIndex
	 *            指定跳转到目标层级的哪个页面，如果不需要指定则传-1
	 * @param targetSubIndex
	 *            指定跳转到目标层级的哪个子层级页面，仅在跳转顶级双层tab栏时有效，如果不需要指定则updateView传-1
	 */
	private void updateContent(TabDataGroup group, int targetIndex, int targetSubIndex) {
		if (group == null) {
			// group不能为空
			Log.e("TabManageView", "updateContent group == null");
			return;
		}
		// 保存当前tab栏数据
		mTabDataGroup = group;
		if (group.isIconTab) {
			// UI2.0,顶级分类用图标+文字并排排列展现
			if (group.categoryData == null || group.categoryData.size() == 0) {
				TabController.showErrorTab();
				return;
			}
			// 调整显示的首页面
			if (targetIndex >= 0 && group.categoryData != null
					&& targetIndex < group.categoryData.size()) {
				group.position = targetIndex;
			}
			// 展示GridTitleBar
			mTitleBar.setVisibility(View.GONE);
			mAppGameTabsBar.setVisibility(View.GONE);
			mGridTitleBar.setVisibility(View.VISIBLE);
	        AppsManagementActivity.sendHandler(mContext,
	        		IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
	        		IDiyMsgIds.SHOW_SEARCH_BUTTON, 1,
	        		null, null);
			mScrollerViewGroup.setVisibility(View.GONE);
			// 填充GridTitleBar
			mGridTitleBar.fillUp(group, targetSubIndex);
			if (mIsFirstUpdateTab) {
				if (TabDataManager.getInstance().getTabStackSize() == 1) {
					// 通知MainViewGroup顶层页面已经加载完毕
					AppsManagementActivity.sendHandler("",
							IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
							IDiyMsgIds.TOPTAB_VIEW_LOAD_FINISH, -1, null, null);
				}
				mIsFirstUpdateTab = false;
			}
			return;
		}
		// 把TitleBar设为可见
		mGridTitleBar.setVisibility(View.GONE);
        AppsManagementActivity.sendHandler(mContext,
		IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
		IDiyMsgIds.SHOW_SEARCH_BUTTON, 0,
		null, null);
		mTitleBar.setVisibility(View.VISIBLE);
		// 调整显示的首页面
		if (targetIndex >= 0 && group.data != null && targetIndex < group.data.size()) {
			group.position = targetIndex;
		}
		// 统计，跳转下一级tab栏时如果不会发生scroll就把首页显示的页面统计一次点击
		int currentScreen = mScrollerViewGroup.getScreenScroller().getCurrentScreen();
		int position = group.position;
		if (currentScreen == position && group.data != null && group.data.size() > position) {
			final int typeId = group.data.get(position).typeId;
			if (typeId != -1) {
				// Log.e("XIEDEZHI", "typeid = " + typeId + "  name = "
				// + group.titles.get(currentScreen));
				// 统计点击数
				AppsManagementActivity.sendHandler(mContext,
						IDiyFrameIds.APPS_MANAGEMENT_MAIN_VIEW_FRAME,
						IDiyMsgIds.SAVE_TAB_CLICK, typeId, null, null);
			}
		}
		// 更新界面
		updateView(group, false);
	}

	/**
	 * 同步到UI线程更新tab栏数据
	 * 
	 * @param group
	 *            新层级tab栏数据
	 * @param targetIndex
	 *            指定跳转到目标层级的哪个页面，如果不需要指定则传-1
	 * @param targetSubIndex
	 *            指定跳转到目标层级的哪个子层级页面，仅在跳转顶级双层tab栏时有效，如果不需要指定则传-1
	 */
	public void updateContentAsyn(TabDataGroup group, int targetIndex, int targetSubIndex) {
		if (mUpdateContentRunnable != null) {
			// 先把之前的runnable杀死
			mUpdateContentRunnable.kill();
		}
		if (mHandler != null) {
			mHandler.removeCallbacks(mUpdateContentRunnable);
			mUpdateContentRunnable = new UpdateContentRunnable(
					UpdateContentRunnable.TYPE_UPDATECONTENT, group, targetIndex, targetSubIndex);
			if (Thread.currentThread() == mUiThread) {
				mUpdateContentRunnable.run();
			} else {
				mHandler.post(mUpdateContentRunnable);
			}
		}
	}

	/**
	 * 准备跳转到下一层级tab或侧面的tab
	 * 
	 * @param 是否显示进度条
	 */
	public void prepareToUpdate(boolean showProgress, String title) {
		// 通知各子页面回收内存
		for (int i = 0; i < mContainers.size(); i++) {
			IContainer container = mContainers.get(i);
			container.onActiveChange(false);
			container.removeContainers();
		}
		// 清空container列表
		mContainers.clear();
		// 重置当前正在显示的下标
		setCurrentIndex(0);
		if (showProgress) {
			//移除coverflow
			if (mCoverFlow != null) {
				mCoverFlow.setVisibility(View.GONE);
			}
			if (mADBanner != null) {
				mADBanner.setVisibility(View.GONE);
			}
			// 清理标题栏多余的东西
			clearOperatorButton();
			mAppGameTabsBar.setVisibility(View.GONE);
			mScrollerViewGroup.setVisibility(View.GONE);
			// tab头标题列表设为空
			mAppGameTabsBar.cleanData();
			// 移除所有mScrollerViewGroup的子view
			mScrollerViewGroup.removeAllViews();
			// 显示进度条
			mTipsView.setVisibility(View.VISIBLE);
			mTipsView.showProgress(title);
		}
	}

	/**
	 * 增加一个子tab
	 * 
	 * @param view
	 * @param params
	 */
	private void addContainer(IContainer container) {
		if (container == null) {
			Log.e("TabManageView", "container==null");
			return;
		}
		if (container instanceof View) {
			mScrollerViewGroup.addView((View) container, mParams);
			mContainers.add(container);
		} else {
			Log.e("TabManageView", "! container instanceof View");
		}
	}

	/**
	 * 从当前tab正在显示container中获取分类id为指定参数的container
	 * 
	 * @param typeId
	 *            分类id
	 * @return 如果当前显示的container中有分类id为指定参数的container，则返回container。否则返回null
	 */
	public IContainer getContainerByID(int typeId) {
		if (mContainers != null) {
			for (IContainer container : mContainers) {
				List<IContainer> subContainers = container.getSubContainers();
				if (subContainers != null && subContainers.size() > 0) {
					for (IContainer subContainer : subContainers) {
						if (subContainer.getTypeId() == typeId) {
							return subContainer;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * 清除操作按钮
	 */
	private void clearOperatorButton() {
		mOperatorButton.setVisibility(View.GONE);
	}

	/**
	 * 解析按钮操作状态
	 */
	protected int[] parseFunButton(String str) {
		if (TextUtils.isEmpty(str)) {
			return null;
		}
		try {
			String[] array = str.split("#");
			int[] ret = new int[array.length];
			String funButtonId = null;
			for (int i = 0; i < array.length; i++) {
				funButtonId = array[i];
				if (funButtonId != null && !"".equals(funButtonId)) {
					ret[i] = Integer.parseInt(funButtonId);
				}
			}
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * SD卡事件的回调接口，TabManageView会把事件传递给每个container
	 * 
	 * @param turnon
	 *            是否开启SD卡
	 */
	public void onSDCardStateChange(boolean turnon) {
		// 通知GridTitleBar加载图标
		mGridTitleBar.onSDCardStateChange(turnon);
		for (IContainer container : mContainers) {
			if (turnon) {
				container.sdCardTurnOn();
			} else {
				container.sdCardTurnOff();
			}
		}
		for (IContainer container : mBuilder.getIdleContainers()) {
			if (turnon) {
				container.sdCardTurnOn();
			} else {
				container.sdCardTurnOff();
			}
		}
	}

	/**
	 * 当系统有安装，卸载，更新应用等操作时回调该接口，TabManageView会把事件传递给每个container
	 * 
	 * @param packName
	 *            安装/卸载/更新的包名
	 * 
	 * @param appAction
	 *            代表应用的操作码，详情看{@link MainViewGroup}
	 */
	public void onAppAction(String packName, int appAction) {
		for (IContainer container : mContainers) {
			container.onAppAction(packName, appAction);
		}
		for (IContainer container : mBuilder.getIdleContainers()) {
			container.onAppAction(packName, appAction);
		}
	}

	/**
	 * view所在activity发生onResume时调用，TabManageView会把事件传递给每个container
	 */
	public void onResume() {
		for (IContainer container : mContainers) {
			container.onResume();
		}
	}

	/**
	 * view所在activity发生onStop时的调用，TabManageView会把事件传递给每个container
	 */
	public void onStop() {
		for (IContainer container : mContainers) {
			container.onStop();
		}
	}

	/**
	 * 当应用下载进度有更新的时候，TabManageView会把消息发到每个container里
	 */
	public void notifyDownloadState(DownloadTask downloadTask) {
		// TODO:XIEDEZHI 更新TabDataManager缓存中的数据状态
		// 更新mDownloadTaskList
		if (mDownloadTaskList != null) {
			// 判断是否需要加入到mDownloadTaskList
			boolean needAddToList = true;
			for (int i = 0; i < mDownloadTaskList.size(); i++) {
				DownloadTask oldTask = mDownloadTaskList.get(i);
				if (oldTask.getId() == downloadTask.getId()) {
					needAddToList = false;
					mDownloadTaskList.set(i, downloadTask);
					break;
				}
			}
			// 说明mDownloadTaskList没有这个任务，需要添加
			if (needAddToList) {
				mDownloadTaskList.add(downloadTask);
			}
		}
		for (IContainer container : mContainers) {
			container.notifyDownloadState(downloadTask);
		}
		for (IContainer container : mBuilder.getIdleContainers()) {
			container.notifyDownloadState(downloadTask);
		}
	}

	/**
	 * 创建弹出菜单时被调用，TabManageView会把事件传递给当前正在显示的container
	 * 
	 * @param menu
	 *            可在此对象上增加子菜单项
	 * @return 处理后返回true，不处理返回false
	 */
	public boolean onPrepareOptionMenu(AppGameMenu menu) {
		if (mContainers.size() <= getCurrentIndex() || getCurrentIndex() < 0) {
			return true;
		}
		IContainer currentContainer = mContainers.get(getCurrentIndex());
		if (currentContainer != null) {
			return currentContainer.onPrepareOptionsMenu(menu);
		}
		return false;
	}

	/**
	 * 点击菜单时被调用，TabManageView会把事件传递给当前正在显示的container
	 * 
	 * @param id
	 * @return 处理后返回true，不处理返回false
	 */
	public boolean onOptionsItemSelected(int id) {
		if (mContainers.size() <= getCurrentIndex() || getCurrentIndex() < 0) {
			return true;
		}
		IContainer currentContainer = mContainers.get(getCurrentIndex());
		if (currentContainer != null) {
			return currentContainer.onOptionItemSelected(id);
		}
		return false;
	}

	/**
	 * 通知当前页面省流量模式发生改变
	 */
	public void onTrafficSavingModeChange() {
		if (mContainers != null && mContainers.size() > 0) {
			for (IContainer container : mContainers) {
				if (container != null) {
					container.onTrafficSavingModeChange();
				}
			}
		}
	}

	/**
	 * 当可更新应用数据发生改变时，通知当前界面
	 * 
	 * @param value 可更新数据或者异常信息
	 * @param state 状态：请看 {@link AppsManageViewController.MSG_ID_NOT_START,AppsManageViewController.MSG_ID_START,AppsManageViewController.MSG_ID_FINISH,AppsManageViewController.MSG_ID_EXCEPTION}
	 * @param noticeContainer 是否需要通知各子页面
	 */
	public void setUpdateData(Object value, int state, boolean noticeContainer) {
		mUpdateData = value;
		mUpdateState = state;
		if (noticeContainer) {
			for (IContainer container : mContainers) {
				container.setUpdateData(value, state);
			}
		}
	}

	// -----------------滚动器事件START-----------------------//
	@Override
	public ScreenScroller getScreenScroller() {
		return null;
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
		mAppGameTabsBar.setButtonSelected(newScreen, true);
		//页面滑动切换，列表改变了，要重置图片管理器的列表控制信息
		AsyncImageManager.getInstance().restore();
	}

	@Override
	public void onScrollFinish(int currentScreen) {
		try {
			//页面滑动切换，列表改变了，要重置图片管理器的列表控制信息
			AsyncImageManager.getInstance().restore();
			// 如果正在更新界面，则返回
			if (mIsUpdating) {
				Log.e("TabManageView", "onScrollFinish TabManageView is updating");
				return;
			}
			// 记录当前屏幕下标
			setCurrentIndex(currentScreen);
			changeCurrentTab(currentScreen);
			// 通知所有子屏幕加载内存或回收内存
			int count = mScrollerViewGroup.getChildCount();
			if (mIsUpdateContentChange) {
				// 如果是UpdateContent所引起的屏幕滚动切换
				// 不用通知各页面变换激活状态，因为在UpdateContent里面会通知
				mIsUpdateContentChange = false;
			} else {
				// 如果不是，就要通知各页面变换激活状态
				for (int i = 0; i < count; i++) {
					if (i == currentScreen - 1 || i == currentScreen || i == currentScreen + 1) {
						IContainer container = mContainers.get(i);
						container.onActiveChange(true);
					} else {
						IContainer container = mContainers.get(i);
						container.onActiveChange(false);
					}
				}
			}
			// 把当前位置记录在顶层tab栏数据里
			if (mTabDataGroup.isIconTab) {
				// 如果是双层tab，更新当前子tab数据的位置
				TabDataGroup subGroup = mTabDataGroup.subGroupList.get(mTabDataGroup.position);
				if (subGroup != null) {
					subGroup.position = currentScreen;
				}
			} else {
				// 如果是单层tab，直接更新位置
				mTabDataGroup.position = currentScreen;
			}
			if (mADBanner != null && mADBanner.getVisibility() == View.VISIBLE
					&& mADBanner.getParent() != null) {
				mADBanner.showNext();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// -----------------滚动器事件END-----------------------//

	/**
	 * 更新tab栏数据时，TabManageView会把当前的container的操作按钮状态传到该方法，
	 * 子类需要根据状态改变操作按钮并加入相应的监听事件。
	 * 
	 * @param 操作按钮列表
	 *            1:搜索2:应用排序3:批量删除
	 */
	protected abstract void showOperatorButton(int[] funButtons);

	/**
	 * 更改当前tab栏显示位置的回调接口，处理逻辑由子类实现
	 * 
	 * @param currScreen
	 */
	protected abstract void changeCurrentTab(int currScreen);

	/**
	 * 资源回收，处理逻辑由子类实现
	 */
	public abstract void recycle();

	/**
	 * 设置handler，用来在各层之间传递信息
	 * 
	 * @param handler
	 */
	public abstract void setHandler(Handler handler);

	/**
	 * 更新界面的Runnable，子线程拿到数据后会把数据放到Runnable，然后post到Handler去更新界面
	 * 
	 * @author xiedezhi
	 * 
	 */
	private class UpdateContentRunnable implements Runnable {

		// 左右切换tab栏的更新
		static final int TYPE_UPDATECONTENT_SIDE = 5471;
		// tab层级跳转的更新
		static final int TYPE_UPDATECONTENT = 5127;

		/**
		 * 更新界面的类型
		 */
		private int mType;
		/**
		 * 更新界面的数据
		 */
		private TabDataGroup mGroup;
		/**
		 * 更新侧面tab栏时，需要更新的侧面tab栏的下标
		 */
		private int mTargetIndex;
		/**
		 * 指定跳转到目标层级的哪个子层级页面，仅在跳转顶级双层tab栏时有效
		 */
		private int mTargetSubIndex;
		/**
		 * 同步锁
		 */
		private Object mLock = new Object();
		/**
		 * 线程是否已经被杀死，如果是，则不做处理
		 */
		private boolean mIsKilled = false;

		/**
		 * 标志该线程已被杀死，后台拿到数据后不做处理
		 */
		public void kill() {
			synchronized (mLock) {
				mIsKilled = true;
			}
		}

		/**
		 * 判断当前线程是否已被杀死
		 * 
		 */
		public boolean isKilled() {
			synchronized (mLock) {
				return mIsKilled;
			}
		}

		/**
		 * 构造函数
		 * 
		 * @param type
		 *            更新界面的类型
		 * @param group
		 *            更新界面的数据
		 * @param targetIndex
		 *            指定跳转到目标层级的哪个页面，如果不需要指定则传-1
		 * @param targetSubIndex
		 *            指定跳转到目标层级的哪个子层级页面，仅在跳转顶级双层tab栏时有效，如果不需要指定则传-1
		 */
		public UpdateContentRunnable(int type, TabDataGroup group, int targetIndex,
				int targetSubIndex) {
			mType = type;
			mGroup = group;
			mTargetIndex = targetIndex;
			mTargetSubIndex = targetSubIndex;
		}

		@Override
		public void run() {
			if (mType == TYPE_UPDATECONTENT_SIDE) {
				if (isKilled()) {
					return;
				}
				// 标示正在更新界面
				mIsUpdating = true;
				updateContentSide(mGroup, mTargetIndex, mTargetSubIndex);
				// 标示更新界面完毕
				mIsUpdating = false;
				kill();
				return;
			}
			if (mType == TYPE_UPDATECONTENT) {
				if (isKilled()) {
					return;
				}
				// 标示正在更新界面
				mIsUpdating = true;
				updateContent(mGroup, mTargetIndex, mTargetSubIndex);
				// 标示更新界面完毕
				mIsUpdating = false;
				kill();
				return;
			}
		}
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
		
		TabController.mT = System.currentTimeMillis();
	}
}
