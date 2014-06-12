package com.jiubang.ggheart.appgame.gostore.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import com.jiubang.ggheart.appgame.base.component.CommonProgress;
import com.jiubang.ggheart.appgame.base.component.ContainerBuiler;
import com.jiubang.ggheart.appgame.base.component.IContainer;
import com.jiubang.ggheart.appgame.base.component.IMenuHandler;
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
 * 壁纸九宫格，数据由外部填充，该类只负责展示内容
 * 
 * @author zhouxuewen
 * 
 */
public class WallpaperGridContainer extends FrameLayout implements IContainer, IModeChangeListener {
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
	 * 加载下一页的进度条
	 */
	private CommonProgress mCommonProgress = null;
	/**
	 * CommonProgress的布局参数
	 */
	private FrameLayout.LayoutParams mCommonProgressLP = null;
	/**
	 * 标志位，表示当变成激活状态时需要重新刷界面
	 */
	private boolean mIsNeedToRefreshWhenActive = false;
	/**
	 * 精品推荐controller，负责逻辑处理
	 */
	private WallpaperGridController mWallController = null;
	/**
	 * 精品推荐应用展示视图
	 */
	private ListView mListView = null;
	/**
	 * 精品推荐数据adapter
	 */
	private WallpaperThreeCellAdapter mAdapter = null;
	/**
	 * 错误提示页
	 */
	private NetworkTipsTool mNetworkTipsTool;
	/**
	 * 精品数据列表
	 */
	private List<BoutiqueApp> mWallApps = new ArrayList<BoutiqueApp>();
	/**
	 * 每页的应用数
	 */
	private double mNumPerPage = -1;
	/**
	 * 列表滑动底部的toast提示
	 */
	Toast mToast = Toast.makeText(getContext(), R.string.appgame_list_end_tip, Toast.LENGTH_SHORT);

	private LayoutInflater mInflater = null;
	/**
	 * listview滑动监听器
	 */
	private OnScrollListener mScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
				// 如果是滑到底部，加载下一页
				case OnScrollListener.SCROLL_STATE_IDLE : {
					if (view.getLastVisiblePosition() >= (view.getCount() - mNumPerPage / 6)) {
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
	 * 加载下一页失败时的重试点击监听器
	 */
	private OnClickListener mListRetryListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// 重新加载下一页
			loadNextPage();
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
		public void handleIdleMessage(android.os.Message msg) {
			switch (msg.what) {
				case LISTVIEW_MSG_LOAD_END :
					removeCommonProgress();
					break;
				case LISTVIEW_MSG_LOAD_FINISH :
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
							mWallApps.add(app);
						}
						// 平均每页的应用数
						mNumPerPage = mWallApps.size() * 1.0 / mCurrentPage;
						//通知adapter更新界面
						mAdapter.update(mWallApps);
						mAdapter.notifyDataSetChanged();
						mIsNeedToRefreshWhenActive = false;
					} else {
						Log.e("WallpaperGridContainer", "mCurrentPage = " + mCurrentPage
								+ "  mbean.pageid = " + mbean.pageid);
					}
					break;
				default :
					break;
			}
		}
	};

	public WallpaperGridContainer(Context context) {
		super(context);
	}

	public WallpaperGridContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WallpaperGridContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		mInflater = LayoutInflater.from(getContext());

		int id = R.drawable.appcenter_feature_default_banner;
		mDefaultIcon = getResources().getDrawable(id);

		ViewGroup tipsView = (ViewGroup) findViewById(R.id.featrue_tips_view);
		mNetworkTipsTool = new NetworkTipsTool(tipsView);
		mNetworkTipsTool.showNothing();
		// listview
		mListView = (ListView) findViewById(R.id.feature_listview);
		mListView.setOnScrollListener(mScrollListener);
		// 初始化adapter
		mAdapter = new WallpaperThreeCellAdapter(getContext());
		mAdapter.setDefaultIcon(mDefaultIcon);
		mListView.setAdapter(mAdapter);
		// 初始化controller
		mWallController = new WallpaperGridController(getContext(), this);
		// 为adapter设置controller
		mAdapter.setWallController(mWallController);
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
		bundle.putInt("startIndex", mWallApps == null ? 0 : mWallApps.size() + 1);
		mWallController.sendRequest(GridViewController.ACTION_NEXT_PAGE, bundle);
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
			WallpaperGridContainer.this.addView(mCommonProgress, mCommonProgressLP);
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
		if (action == GridViewController.ACTION_NEXT_PAGE_DATA) {
			mHandler.sendMessage(LISTVIEW_MSG_LOAD_FINISH, -1, -1, value);
		}
	}

	/**
	 * 刷新界面，展示精品数据
	 */
	private void doRefresh() {
		if (mWallApps == null || mWallApps.size() <= 0) {
			return;
		}
		if (mIsActive && mIsNeedToRefreshWhenActive) {
			mIsNeedToRefreshWhenActive = false;
			mNetworkTipsTool.showNothing();
			mAdapter.onActiveChange(true);
			mAdapter.update(mWallApps);
			mAdapter.notifyDataSetChanged();
			mListView.setVisibility(View.VISIBLE);
			if (mCurrentPage >= mPages) {
				mLoadingNextPage = false;
				mHandler.sendEmptyMessage(LISTVIEW_MSG_LOAD_END);
			}
		}
		post(new Runnable() {
			
			@Override
			public void run() {
				// 如果列表已经在底部，加载下一页
				if (mListView.getLastVisiblePosition() >= (mListView.getCount() - 2)) {
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
		if (mWallApps != null) {
			mWallApps.clear();
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
		mAdapter.onActiveChange(isActive);
		if (isActive) {
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
		// 壁纸不会显示已安装标识
	}

	@Override
	public void updateContent(ClassificationDataBean bean, boolean isPrevLoadRefresh) {
		if (bean == null || bean.dataType != ClassificationDataBean.WALLPAPER_GRID) {
			Log.e("WallpaperGridContainer",
					"updateContent  bean == null|| bean.dataType != ClassificationDataBean.WALLPAPER_GRID");
			return;
		}
		if (isPrevLoadRefresh && (bean.featureList == null || bean.featureList.size() == 0)) {
			return;
		}
		if (bean.featureList == null || bean.featureList.size() == 0) {
			// 显示重试页面，点击重试刷新整个tab栏
			mListView.setVisibility(View.GONE);
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
		mWallController.sendRequest(GridViewController.ACTION_CANCLE_NEXT_PAGE, null);
		mIsNeedToRefreshWhenActive = true;
		// 保存分类id
		mTypeId = bean.typeId;
		// 数据总页数
		mPages = bean.pages;
		// 已经加载的页
		mCurrentPage = bean.pageid;
		// 把数据保存下来，激活状态的时候刷新界面
		mWallApps.clear();
		// 不要直接用updatecontent传过来的列表，因为这个对象是暴露出去的，有可能在子线程被修改，所以要copy一份保存
		if (bean.featureList != null) {
			for (BoutiqueApp app : bean.featureList) {
				mWallApps.add(app);
			}
		}
		// 平均每页的应用数
		mNumPerPage = mWallApps.size() * 1.0 / mCurrentPage;
		// 重置状态
		mLoadingNextPage = false;
		removeCommonProgress();
		if (!isPrevLoadRefresh) {
			// 如果不是预加载后刷新，显示进度条
			// 重置状态
			mShowingError = false;
			mIsActive = false;
			if (mIsNeedToRefreshWhenActive && mListView.getChildCount() <= 0) {
				// 显示进度条
				mListView.setVisibility(View.GONE);
				mNetworkTipsTool.showNothing();
				mNetworkTipsTool.showProgress();
			} else if (mIsActive) {
				//如果是预加载拿到更新数据，直接刷新界面
				mShowingError = false;
				doRefresh();
			}
		}
	}

	@Override
	public void initEntrance(int access) {
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
		// 壁纸不会显示下载进度
	}

	@Override
	public void setDownloadTaskList(List<DownloadTask> taskList) {
		// 壁纸不会显示下载进度
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
			resId = new int[] { IMenuHandler.MENU_ITEM_FRESH,
					IMenuHandler.MENU_ITEM_SETTING,
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
		if (mAdapter != null) {
			mAdapter.onActiveChange(mIsActive);
			mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void setUpdateData(Object value, int state) {
		// do nothing
	}

	@Override
	public void fillupMultiContainer(List<CategoriesDataBean> cBeans, List<IContainer> containers) {
		//do nothing			
	}

	@Override
	public void removeContainers() {
		//do nothing			
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
