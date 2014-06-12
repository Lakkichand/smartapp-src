package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.DeferredHandler;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.data.ClassificationExceptionRecord;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.TabController;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.appgame.base.utils.NetworkTipsTool;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 精品推荐主界面，用于应用中心/游戏中心的精品推荐页。数据由外部填充，该类只负责展示内容，逻辑处理交给FeatureController
 * 
 * @author xiedezhi
 * 
 */
public class FeaturedContainer extends FrameLayout implements IContainer, IModeChangeListener {
	// TODO:XIEDEZHI 有时会有重复应用，不知道是服务器数据问题还是客户端问题
	/**
	 * 数据列表对应的分类id
	 */
	private int mTypeId = -1;
	/**
	 * 专题数据总页数
	 */
	private int mPages = -1;
	/**
	 * 已经加载的页数
	 */
	private int mCurrentPage = -1;
	/**
	 * 是否正在加载下一页
	 */
	private volatile boolean mLoadingNextPage = false;
	/**
	 * 是否激活状态
	 */
	private boolean mIsActive = false;
	/**
	 * 是否在显示错误提示页面
	 */
	private boolean mShowingError = false;
	/**
	 * 标志位，表示当变成激活状态时需要重新刷界面
	 */
	private boolean mIsNeedToRefreshWhenActive = false;
	/**
	 * 精品推荐controller，负责逻辑处理
	 */
	private FeatureController mFeatureController = null;
	/**
	 * 精品推荐应用展示视图
	 */
	private ListView mFeatureListView = null;
	/**
	 * 加载下一页的进度条
	 */
	private CommonProgress mCommonProgress = null;
	/**
	 * CommonProgress的布局参数
	 */
	private FrameLayout.LayoutParams mCommonProgressLP = null;
	/**
	 * 精品推荐banner展示视图
	 */
	private FeatureHeaderView mFeatureHeaderView = null;
	/**
	 * 精品推荐数据adapter
	 */
	private FeatureTwoCellAdapter mFeatureAdapter = null;
	/**
	 * 错误提示页
	 */
	private NetworkTipsTool mNetworkTipsTool;
	/**
	 * 精品数据列表
	 */
	private List<BoutiqueApp> mFeatureApps = new ArrayList<BoutiqueApp>();
	/**
	 * 每页的应用数
	 */
	private double mNumPerPage = -1;
	/**
	 * DownloadManager里的下载任务列表，用于初始化某些进来中心前已经有下载状态的应用
	 */
	private ArrayList<DownloadTask> mTaskList;
	/**
	 * 列表滑动底部的toast提示
	 */
	Toast mToast = Toast.makeText(getContext(), R.string.appgame_list_end_tip, Toast.LENGTH_SHORT);
	/**
	 * 展示一句话简介的view
	 */
	private ContainerSummaryView mSummaryView = null;
	/**
	 * 该container的一句话简介
	 */
	private String mSummary = null;

	private LayoutInflater mInflater = null;
	/**
	 * 精品推荐banner图的点击事件
	 */
	private OnClickListener mHeaderViewClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getTag() != null && (v.getTag() instanceof BoutiqueApp)) {
				BoutiqueApp app = (BoutiqueApp) v.getTag();
				mFeatureController.onItemClick(getContext(), app);
			}
		}
	};
	/**
	 * listview滑动监听器
	 */
	private OnScrollListener mScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			// 如果是滑到底部，加载下一页
				case OnScrollListener.SCROLL_STATE_IDLE : {
					if (view.getLastVisiblePosition() >= (view.getCount() - mNumPerPage / 4)) {
						loadNextPage();
					}

					if (view.getLastVisiblePosition() >= (view.getCount() - 1)
							&& mCurrentPage >= mPages) {
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
	};
	/**
	 * 数据列表为空时的重试点击监听器，刷新当前tab
	 */
	private OnClickListener mRetryListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			TabController.refreshCurrentTab();
		}
	};
	/**
	 * 反馈按钮监听器
	 */
	private OnClickListener mFeedbackListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ClassificationExceptionRecord.sendFeedbackMail(getContext());
		}
	};
	/**
	 * 列表在非激活状态时显示的默认图标
	 */
	private Drawable mDefaultIcon = null;
	/**
	 * 后台是否在加载最新数据，如果是
	 */
	private boolean mIsPrevLoading = false;

	// listview已经加载到最后一页了
	private static final int LISTVIEW_MSG_LOAD_END = 2002;
	// listview加载下一页完成
	private static final int LISTVIEW_MSG_LOAD_FINISH = 2005;
	/**
	 * 消息处理器
	 */
	private DeferredHandler mHandler = new DeferredHandler() {

		@Override
		public void handleIdleMessage(Message msg) {

			switch (msg.what) {
				case LISTVIEW_MSG_LOAD_END :
					removeCommonProgress();
					break;
				case LISTVIEW_MSG_LOAD_FINISH :// TODO 是不是这里导致了重复应用
					mLoadingNextPage = false;
					removeCommonProgress();
					Object mobj = msg.obj;
					if (mobj == null || !(mobj instanceof ClassificationDataBean)) {
						// 加载失败
						//TODO:XIEDEZHI 弹出toast提示
						return;
					}
					ClassificationDataBean mbean = (ClassificationDataBean) mobj;
					if (mbean.featureList == null || mbean.featureList.size() <= 0) {
						// 加载失败
						//TODO:XIEDEZHI 弹出toast提示
						return;
					}
					if (mTypeId == mbean.typeId && mCurrentPage + 1 == mbean.pageid) {
						// 加载下一页成功
						// 记录总页数
						mPages = mbean.pages;
						// 记录当前页码
						mCurrentPage = mbean.pageid;
						for (BoutiqueApp app : mbean.featureList) {
							mFeatureApps.add(app);
						}
						// 过滤新来的数据,更新container的数据源
						mFeatureApps = mFeatureController.fixedFeatureData(mFeatureApps);
						// 平均每页的应用数
						mNumPerPage = mFeatureApps.size() * 1.0 / mCurrentPage;
						// 通知adapter更新界面
						// 把格子数为5的数据刷上
						mFeatureHeaderView.fillUp(mFeatureController.getBannerApp(mFeatureApps));
						// 把格子数为6的应用抽出来更新adapter数据
						List<BoutiqueApp> appList = mFeatureController
								.getApplicationApp(mFeatureApps);
						mFeatureAdapter.update(appList);
						mFeatureAdapter.notifyDataSetChanged();
						mIsNeedToRefreshWhenActive = false;
					} else {
						Log.e("FeaturedContainer", "mCurrentPage = " + mCurrentPage
								+ "  mbean.pageid = " + mbean.pageid);
					}
					break;
				default :
					break;
			}

		};
	};

	public FeaturedContainer(Context context) {
		super(context);
	}

	public FeaturedContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FeaturedContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		mInflater = LayoutInflater.from(getContext());
		mDefaultIcon = getResources().getDrawable(R.drawable.default_icon);

		ViewGroup tipsView = (ViewGroup) findViewById(R.id.featrue_tips_view);
		mNetworkTipsTool = new NetworkTipsTool(tipsView);
		mNetworkTipsTool.showNothing();
		// listview
		mFeatureListView = (ListView) findViewById(R.id.feature_listview);
		mFeatureListView.setOnScrollListener(mScrollListener);
		//		mFeatureListView.setOnTouchListener(mListTouchListener);
		// 一行双列的情况下，点击事件处理在adapter
		// 展示一句话简介的view
		mSummaryView = (ContainerSummaryView) mInflater.inflate(R.layout.appgame_container_summary,
				null);
		mFeatureListView.addHeaderView(mSummaryView, null, false);
		mSummaryView.viewGone();
		// HeaderView，用于展示banner图
		mFeatureHeaderView = new FeatureHeaderView(getContext());
		mFeatureHeaderView.setItemClickListener(mHeaderViewClickListener);
		mFeatureListView.addHeaderView(mFeatureHeaderView, null, false);
		// 初始化adapter
		mFeatureAdapter = new FeatureTwoCellAdapter(getContext());
		mFeatureAdapter.setDefaultIcon(mDefaultIcon);
		mFeatureListView.setAdapter(mFeatureAdapter);
		// 初始化controller
		mFeatureController = new FeatureController(getContext(), this);
		// 一行双列：为adapter设置controller
		mFeatureAdapter.setFeatureController(mFeatureController);
	}

	/**
	 * 加载下一页
	 */
	private void loadNextPage() {
		if (mCurrentPage >= mPages) {
			mLoadingNextPage = false;
			mHandler.sendEmptyMessage(LISTVIEW_MSG_LOAD_END);
			return;
		}
		if (mLoadingNextPage) {
			return;
		}
		// 加载下一页
		mLoadingNextPage = true;
		showCommonProgress();
		// 把加载下一页的任务交给controller处理
		Bundle bundle = new Bundle();
		bundle.putInt("typeId", mTypeId);
		bundle.putInt("pageId", mCurrentPage + 1);
		bundle.putInt("startIndex", mFeatureApps == null ? 0 : mFeatureApps.size() + 1);
		mFeatureController.sendRequest(FeatureController.ACTION_NEXT_PAGE, bundle);
	}

	/**
	 * 展示浮在列表底部的进度条
	 */
	private void showCommonProgress() {
		boolean needAnimation = false;
		if (mCommonProgress == null) {
			mCommonProgress = (CommonProgress) mInflater.inflate(R.layout.appgame_common_progress,
					null);
			mCommonProgressLP = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					DrawUtils.dip2px(40), Gravity.BOTTOM);
			FeaturedContainer.this.addView(mCommonProgress, mCommonProgressLP);
			needAnimation = true;
		}
		if (!needAnimation && mCommonProgress.getVisibility() != View.VISIBLE) {
			needAnimation = true;
		}
		mCommonProgress.setVisibility(View.VISIBLE);
		if (needAnimation) {
			mCommonProgress.startAnimation(AppGameDrawUtils.getInstance().mCommonProgressAnimation);
		}
	}

	/**
	 * 移除浮在列表底部的进度条
	 */
	private void removeCommonProgress() {
		if (mCommonProgress != null) {
			mCommonProgress.setVisibility(View.GONE);
		}
	}

	@Override
	public void onModleChanged(int action, int state, Object value) {
		if (action == FeatureController.ACTION_NEXT_PAGE_DATA) {
			Message message = new Message();
			message.what = LISTVIEW_MSG_LOAD_FINISH;
			message.obj = value;
			mHandler.sendMessage(message);
		}
	}

	/**
	 * 刷新界面，展示精品数据
	 */
	private void doRefresh() {
		if (mFeatureApps == null || mFeatureApps.size() <= 0) {
			return;
		}
		if (mIsActive && mIsNeedToRefreshWhenActive) {
			mIsNeedToRefreshWhenActive = false;
			mNetworkTipsTool.showNothing();
			if (mSummary == null || mSummary.trim().equals("")
					|| mSummary.trim().equalsIgnoreCase("null")) {
				mSummaryView.viewGone();
			} else {
				mSummaryView.viewVisible();
				mSummaryView.fillUp(mSummary, true);
			}
			// 把格子数为6的应用抽出来更新adapter数据
			mFeatureAdapter.onActiveChange(true);
			mFeatureAdapter.update(mFeatureController.getApplicationApp(mFeatureApps));
			mFeatureAdapter.notifyDataSetChanged();
			mFeatureListView.setVisibility(View.VISIBLE);
			if (mCurrentPage >= mPages) {
				mLoadingNextPage = false;
				mHandler.sendEmptyMessage(LISTVIEW_MSG_LOAD_END);
			}
			// 先把格子数为5的应用抽出来，用于生成headerview
			mFeatureHeaderView.fillUp(mFeatureController.getBannerApp(mFeatureApps));
		}
		post(new Runnable() {

			@Override
			public void run() {
				// 如果列表已经在底部，加载下一页
				if (mFeatureListView.getLastVisiblePosition() >= (mFeatureListView.getCount() - 2)) {
					loadNextPage();
				}
			}
		});
		mShowingError = false;
		if (mIsActive && mIsPrevLoading) {
			showCommonProgress();
		}
	}

	@Override
	public void cleanup() {
		// 清除资源
		if (mFeatureApps != null) {
			mFeatureApps.clear();
		}
	}

	@Override
	public void sdCardTurnOff() {
		// do nothing
	}

	@Override
	public void sdCardTurnOn() {
		// do nothing
	}

	@Override
	public void onActiveChange(boolean isActive) {
		if (isActive && isActive == mIsActive) {
			return;
		}
		mIsActive = isActive;
		// 通知adapter更新激活状态，用于getview时判断是否要加载图标
		mFeatureAdapter.onActiveChange(isActive);
		if (isActive) {
			mFeatureAdapter.setDownloadTaskList(mTaskList);
			if (!mShowingError) {
				doRefresh();
			}
		}
	}
	@Override
	public void onResume() {
		if (mIsActive) {
			if (!mShowingError) {
				doRefresh();
			}
		}
	}

	@Override
	public void onStop() {
		mIsNeedToRefreshWhenActive = true;
	}

	@Override
	public void onAppAction(String packName, int appAction) {
		// TODO:XIEDEZHI 其他两个container没有实现这个东西
		// 这里判断下载中的应用是否在可视范围，在的话才调用notifyDataSetChanged
		int firstIndex = mFeatureListView.getFirstVisiblePosition();
		int lastIndex = mFeatureListView.getLastVisiblePosition();
		if (mFeatureAdapter != null) {
			boolean ret = mFeatureAdapter
					.onAppAction(firstIndex, lastIndex, 2, packName, appAction);
			//如果列表可视范围的应用发生安装卸载事件，但该container已经被移除或者处于非激活状态，那么在激活状态时需要重新刷一次界面
			if (ret && (this.getParent() == null || !mIsActive)) {
				mIsNeedToRefreshWhenActive = true;
			}
		}
	}

	@Override
	public void updateContent(ClassificationDataBean bean, boolean isPrevLoadRefresh) {
		if (bean == null || bean.dataType != ClassificationDataBean.FEATURE_TYPE) {
			Log.e("FeaturedContainer",
					"updateContent  bean == null|| bean.dataType != ClassificationDataBean.FEATURE_TYPE");
			return;
		}
		if (isPrevLoadRefresh && (bean.featureList == null || bean.featureList.size() == 0)) {
			return;
		}
		if (bean.featureList == null || bean.featureList.size() == 0) {
			// 显示重试页面，点击重试刷新整个tab栏
			mFeatureListView.setVisibility(View.GONE);
			mNetworkTipsTool.showNothing();
			// 判断SD卡是否有网络错误日志，另外网络是否正常，是则显示反馈按钮
			if (FileUtil
					.isFileExist(LauncherEnv.Path.APP_MANAGER_CLASSIFICATION_EXCEPTION_RECORD_PATH)
					&& Machine.isNetworkOK(getContext())) {
				mNetworkTipsTool.showRetryErrorTip(mRetryListener, mFeedbackListener);
			} else {
				mNetworkTipsTool.showRetryErrorTip(mRetryListener, true);
			}
			mShowingError = true;
			return;
		}
		// 把load下一页的进程杀死
		mFeatureController.sendRequest(FeatureController.ACTION_CANCLE_NEXT_PAGE, null);
		mIsNeedToRefreshWhenActive = true;
		// 保存分类id
		mTypeId = bean.typeId;
		// 一句话简介
		mSummary = bean.summary;
		// 数据总页数
		mPages = bean.pages;
		// 已经加载的页
		mCurrentPage = bean.pageid;
		// 把服务器下发的数据中不支持的样式去除（只支持格子数为5和6的样式）
		List<BoutiqueApp> list = mFeatureController.fixedFeatureData(bean.featureList);
		mFeatureApps = list;
		// 平均每页的应用数
		mNumPerPage = mFeatureApps.size() * 1.0 / mCurrentPage;
		// 重置状态
		mLoadingNextPage = false;
		removeCommonProgress();
		if (!isPrevLoadRefresh) {
			// 如果不是预加载后刷新，显示进度条
			// 重置状态
			mShowingError = false;
			mIsActive = false;
			if (mIsNeedToRefreshWhenActive && mFeatureListView.getChildCount() <= 0) {
				// 显示进度条
				mFeatureListView.setVisibility(View.GONE);
				mNetworkTipsTool.showNothing();
				mNetworkTipsTool.showProgress();
			}
		} else if (mIsActive) {
			//如果是预加载拿到更新数据，直接刷新界面
			mShowingError = false;
			doRefresh();
		}
	}

	@Override
	public void initEntrance(int access) {
		// do nothing
	}

	@Override
	public int getTypeId() {
		return mTypeId;
	}

	@Override
	public void onFinishAllUpdateContent() {
		// do nothing
	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {
		if (downloadTask == null) {
			return;
		}
		// 这里判断下载中的应用是否在可视范围，在的话才调用notifyDataSetChanged
		int firstIndex = mFeatureListView.getFirstVisiblePosition();
		int lastIndex = mFeatureListView.getLastVisiblePosition();
		boolean ret = mFeatureAdapter.updateDownloadTask(firstIndex, lastIndex, 2, downloadTask);
		//如果列表可视范围下载进度更新了，但该container已经被移除或者处于非激活状态，那么在激活状态时需要重新刷一次界面
		if (ret && (this.getParent() == null || !mIsActive)) {
			mIsNeedToRefreshWhenActive = true;
		}
	}

	@Override
	public void setDownloadTaskList(List<DownloadTask> taskList) {
		// TODO:XIEDEZHI 如果是激活状态，刷新界面
		mTaskList = (ArrayList<DownloadTask>) taskList;
	}

	@Override
	public boolean onPrepareOptionsMenu(AppGameMenu menu) {
		ChannelConfig channelConfig = GOLauncherApp.getChannelConfig();
		boolean isNeedDownloadManager = true;
		if (channelConfig != null) {
			isNeedDownloadManager = channelConfig.isNeedDownloadManager();
		}
		int resId[] = null;
		if (isNeedDownloadManager) {
			resId = new int[] { IMenuHandler.MENU_ITEM_FRESH,
					IMenuHandler.MENU_ITEM_DOWNLOAD_MANAGER, IMenuHandler.MENU_ITEM_SETTING,
					IMenuHandler.MENU_ITEM_FEEDBACK };
		} else {
			resId = new int[] { IMenuHandler.MENU_ITEM_FRESH, IMenuHandler.MENU_ITEM_SETTING,
					IMenuHandler.MENU_ITEM_FEEDBACK };
		}
		menu.setResourceId(resId);
		menu.show(this);
		return true;
	}

	@Override
	public boolean onOptionItemSelected(int id) {
		switch (id) {
			case IMenuHandler.MENU_ITEM_FRESH :
				// 整个tab栏刷新
				TabController.refreshCurrentTab();
				return true;
		}
		return false;
	}

	@Override
	public void onTrafficSavingModeChange() {
		if (!mIsActive) {
			mIsNeedToRefreshWhenActive = true;
			return;
		}
		if (mFeatureAdapter != null) {
			mFeatureAdapter.onActiveChange(mIsActive);
			mFeatureAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void setUpdateData(Object value, int state) {
		// do nothing
	}

	@Override
	public void fillupMultiContainer(List<CategoriesDataBean> cBeans, List<IContainer> containers) {
		// do nothing
	}

	@Override
	public void removeContainers() {
		// do nothing
	}

	@Override
	public List<IContainer> getSubContainers() {
		List<IContainer> ret = new ArrayList<IContainer>();
		ret.add(this);
		return ret;
	}

	@Override
	public void onMultiVisiableChange(boolean visiable) {
		if (!visiable) {
			//TODO 回收图片
			mIsNeedToRefreshWhenActive = true;
		}
	}

	@Override
	public void prevLoading() {
		mIsPrevLoading = true;
		if (mIsActive && !mShowingError) {
			showCommonProgress();
		}
	}

	@Override
	public void prevLoadFinish() {
		if (mIsPrevLoading) {
			mIsPrevLoading = false;
			if (!mLoadingNextPage) {
				removeCommonProgress();
			}
		}
	}
	
	@Override
	public void setBuilder(ContainerBuiler builder) {
		// do nothing
	}
}
