/*
 * 文 件 名:  EditorFavoriteContainer.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-8-3
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.gau.go.launcherex.theme.cover.DrawUtils;
import com.go.util.DeferredHandler;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.data.ClassificationExceptionRecord;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.manage.TabController;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;
import com.jiubang.ggheart.appgame.base.net.InstallCallbackManager;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.appgame.base.utils.NetworkTipsTool;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 类描述: 功能详细描述:
 * 
 * @author liuxinyang
 * @date [2012-8-3]
 */
public class EditorFavoriteContainer extends FrameLayout implements IContainer {
	/**
	 * 当前数据列表对应的分类id
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
	 * 标志位，表示当变成激活状态时需要重新刷界面
	 */
	private boolean mIsNeedToRefreshWhenActive = false;
	/**
	 * 是否处于活动状态
	 */
	private boolean mIsActive = false;
	/**
	 * 是否在显示错误页面（网络错误，SD卡不在在）
	 */
	private boolean mShowingError = false;

	private LayoutInflater mInflater = null;
	/**
	 * 加载下一页的进度条
	 */
	private CommonProgress mCommonProgress = null;
	/**
	 * CommonProgress的布局参数
	 */
	private FrameLayout.LayoutParams mCommonProgressLP = null;
	/**
	 * 错误提示页面
	 */
	private NetworkTipsTool mNetworkTipsTool;
	/**
	 * 展示应用数据的listview
	 */
	private ListView mListView = null;
	/**
	 * 数据列表
	 */
	private ArrayList<BoutiqueApp> mAppList = new ArrayList<BoutiqueApp>();
	/**
	 * 每页的应用数
	 */
	private double mNumPerPage = -1;
	/**
	 * DownloadManager的下载任务列表
	 */
	private ArrayList<DownloadTask> mDownloadTaskList = new ArrayList<DownloadTask>();
	/**
	 * 编辑推荐的控制器，用于获取下一页的数据
	 */
	private EditorFavoriteController mController = null;
	/**
	 * container数据适配器
	 */
	private EditorFavoriteAdapter mAdapter = null;
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
	/**
	 * listview滑动监听器
	 */
	private OnScrollListener mScrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
				// 如果是滑到底部，加载下一页
				case OnScrollListener.SCROLL_STATE_IDLE : {
					if (view.getLastVisiblePosition() >= (view.getCount() - mNumPerPage / 2)) {
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
	private OnClickListener mRetryClickListener = new OnClickListener() {
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

	private IModeChangeListener modeChangeListener = new IModeChangeListener() {
		@Override
		public void onModleChanged(int action, int state, Object value) {
			if (action == NewExtraController.sACTION_RETURN_DATA) {
				Message message = new Message();
				message.what = LISTVIEW_MSG_LOAD_FINISH;
				message.obj = value;
				mHandler.sendMessage(message);
			}
		}
	};

	/**
	 * 后台是否在加载最新数据，如果是
	 */
	private boolean mIsPrevLoading = false;

	private static final int LISTVIEW_MSG_LOAD_END = 2002;

	private static final int LISTVIEW_MSG_LOAD_FINISH = 2005;

	public EditorFavoriteContainer(Context context) {
		super(context);
	}

	public EditorFavoriteContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EditorFavoriteContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		mInflater = LayoutInflater.from(getContext());
		mController = new EditorFavoriteController(getContext(), modeChangeListener);

		ViewGroup tipsview = (ViewGroup) findViewById(R.id.editor_favorite_tips_view);
		mNetworkTipsTool = new NetworkTipsTool(tipsview);
		mNetworkTipsTool.showNothing();

		mListView = (ListView) findViewById(R.id.editor_favorite_listview);
		// 展示一句话简介的view
		mSummaryView = (ContainerSummaryView) mInflater.inflate(R.layout.appgame_container_summary,
				null);
		mListView.addHeaderView(mSummaryView, null, false);
		mSummaryView.viewGone();

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Object tag = view.getTag(R.id.appgame);
				BoutiqueApp app = null;
				if (tag != null && tag instanceof BoutiqueApp) {
					app = (BoutiqueApp) tag;
				}
				if (app == null) {
					return;
				}
				// 判断treatment的值
				if (app.info.treatment > 0) {
					InstallCallbackManager.saveTreatment(app.info.packname, app.info.treatment);
				}
				// 判断是否需要安装成功之后回调
				if (app.info.icbackurl != null && !app.info.icbackurl.equals("")) {
					InstallCallbackManager.saveCallbackUrl(app.info.packname, app.info.icbackurl);
				}
				if (app.type == 1) {
					TabController.skipToTheNextTab(app.typeInfo.typeid, app.typeInfo.name, -1,
							true, -1, -1, null);
				} else if (app.type == 2) {
					AppsDetail.jumpToDetail(getContext(), app,
							AppsDetail.START_TYPE_APPRECOMMENDED, position, true);
				}
			}
		});
		mListView.setOnScrollListener(mScrollListener);
//		mListView.setOnTouchListener(mListTouchListener);
		// 创建adapter
		mAdapter = new EditorFavoriteAdapter(getContext());
		mListView.setAdapter(mAdapter);
		super.onFinishInflate();
	}

	private DeferredHandler mHandler = new DeferredHandler() {

		@Override
		public void handleIdleMessage(Message msg) {

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
							mAppList.add(app);
						}
						// 平均每页的应用数
						mNumPerPage = mAppList.size() * 1.0 / mCurrentPage;
						// 通知adapter更新界面
						mAdapter.updateList(mAppList);
						mAdapter.notifyDataSetChanged();
						mIsNeedToRefreshWhenActive = false;
					} else {
						Log.e("EditorFavoriteContainer", "mCurrentPage = " + mCurrentPage
								+ "  mbean.pageid = " + mbean.pageid);
					}
					break;
				default :
					break;
			}
		};
	};

	/**
	 * 取下一页的数据
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
		if (mController != null) {
			Bundle bundle = new Bundle();
			bundle.putInt("typeId", mTypeId);
			bundle.putInt("currentPage", mCurrentPage);
			bundle.putInt("startIndex", mAdapter.getCount() + 1);
			mLoadingNextPage = true;
			showCommonProgress();
			mController.sendAsyncRequest(EditorFavoriteController.ACTION_NEXT_PAGE, bundle);
		}
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
			EditorFavoriteContainer.this.addView(mCommonProgress, mCommonProgressLP);
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

	/**
	 * 刷新界面
	 * 
	 * @param isNeed 无论是否激活状态都刷新界面
	 */
	private void doRefresh() {
		if (mAppList == null || mAppList.size() <= 0) {
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
			mAdapter.onActiveChange(true);
			mAdapter.updateList(mAppList);
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

	/** {@inheritDoc} */

	@Override
	public void cleanup() {
		if (mAppList != null) {
			mAppList.clear();
		}
	}

	/** {@inheritDoc} */

	@Override
	public void sdCardTurnOff() {
		// do nothing
	}

	/** {@inheritDoc} */

	@Override
	public void sdCardTurnOn() {
		// do nothing
	}

	/** {@inheritDoc} */

	@Override
	public void onActiveChange(boolean isActive) {
		if (mIsActive == isActive) {
			return;
		}
		mIsActive = isActive;
		mAdapter.onActiveChange(isActive);
		if (isActive) {
			mAdapter.setDownloadTaskList(mDownloadTaskList);
			if (!mShowingError) {
				doRefresh();
			}
		}
	}

	/** {@inheritDoc} */

	@Override
	public void onResume() {
		if (mIsActive) {
			if (!mShowingError) {
				doRefresh();
			}
		}
	}

	/** {@inheritDoc} */

	@Override
	public void onStop() {
		mIsNeedToRefreshWhenActive = true;
	}

	/** {@inheritDoc} */

	@Override
	public void onAppAction(String packName, int appAction) {
		// 这里判断下载中的应用是否在可视范围，在的话才调用notifyDataSetChanged
		int firstIndex = mListView.getFirstVisiblePosition();
		int lastIndex = mListView.getLastVisiblePosition();
		boolean ret = false;
		for (int i = 0; i < mAdapter.getCount(); i++) {
			BoutiqueApp app = (BoutiqueApp) mAdapter.getItem(i);
			if (app == null || app.info == null) {
				continue;
			}
			if (packName.equals(app.info.packname)) {
				if ((i + 1) >= firstIndex && (i + 1) <= lastIndex) {
					ret = true;
					if (mIsActive) {
						mAdapter.notifyDataSetChanged();
					}
				}
				break;
			}
		}
		//如果列表可视范围的应用发生安装卸载事件，但该container已经被移除或者处于非激活状态，那么在激活状态时需要重新刷一次界面
		if (ret && (this.getParent() == null || !mIsActive)) {
			mIsNeedToRefreshWhenActive = true;
		}
	}

	/** {@inheritDoc} */

	@Override
	public void updateContent(ClassificationDataBean bean, boolean isPrevLoadRefresh) {
		if (bean == null || bean.dataType != ClassificationDataBean.EDITOR_RECOMM_TYPE) {
			Log.e("EditorFavoriteCOntainer",
					"updateContent  bean == null|| bean.dataType != ClassificationDataBean.EDITOR_RECOMM_TYPE");
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
				mNetworkTipsTool.showRetryErrorTip(mRetryClickListener, mFeedbackListener);
			} else {
				mNetworkTipsTool.showRetryErrorTip(mRetryClickListener, true);
			}
			return;
		}
		// 把load下一页的线程杀死
		if (mController != null) {
			mController.sendRequest(NewExtraController.sACTION_CANCEL_NEXT_PAGE, null);
		}
		//如果数据改变了，当激活状态时就要刷新界面
		mIsNeedToRefreshWhenActive = true;
		// 保存分类id
		mTypeId = bean.typeId;
		// 一句话简介
		mSummary = bean.summary;
		// 数据总页数
		mPages = bean.pages;
		// 已经加载的页
		mCurrentPage = bean.pageid;
		// 把数据保存下来，激活状态的时候刷新界面
		mAppList.clear();
		// 不要直接用updatecontent传过来的列表，因为这个对象是暴露出去的，有可能在子线程被修改，所以要copy一份保存
		if (bean.featureList != null) {
			for (BoutiqueApp app : bean.featureList) {
				mAppList.add(app);
			}
		}
		// 平均每页的应用数
		mNumPerPage = mAppList.size() * 1.0 / mCurrentPage;
		// 重置状态
		mLoadingNextPage = false;
		removeCommonProgress();
		// 如果不是预加载后刷新，显示进度条
		if (!isPrevLoadRefresh) {
			// 重置状态
			mIsActive = false;
			mShowingError = false;
			if (mIsNeedToRefreshWhenActive && mListView.getChildCount() <= 0) {
				// 显示进度条
				mListView.setVisibility(View.GONE);
				mNetworkTipsTool.showNothing();
				mNetworkTipsTool.showProgress();
			}
		} else if (mIsActive) {
			// 如果是预加载拿到更新数据，直接刷新界面
			mShowingError = false;
			doRefresh();
		}
	}

	/** {@inheritDoc} */

	@Override
	public void initEntrance(int access) {
		// do nothing
	}

	/** {@inheritDoc} */

	@Override
	public int getTypeId() {
		return mTypeId;
	}

	/** {@inheritDoc} */

	@Override
	public void onFinishAllUpdateContent() {

	}

	/** {@inheritDoc} */

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {
		if (downloadTask == null) {
			return;
		}
		// 这里判断下载中的应用是否在可视范围，在的话才调用notifyDataSetChanged
		int firstIndex = mListView.getFirstVisiblePosition();
		int lastIndex = mListView.getLastVisiblePosition();
		boolean ret = false;
		int count = mAdapter.getCount();
		for (int i = 0; i < count; i++) {
			Object obj = mAdapter.getItem(i);
			if (obj != null && (obj instanceof BoutiqueApp)) {
				BoutiqueApp app = (BoutiqueApp) obj;
				if (app == null || app.info == null || app.info.appid == null) {
					continue;
				}
				if (app.info.appid.equals(downloadTask.getId() + "")) {
					app.downloadState.state = downloadTask.getState();
					app.downloadState.alreadyDownloadPercent = downloadTask
							.getAlreadyDownloadPercent();
					// 因为页面有一个headerview，所以位置要加上1
					if ((i + 1) >= firstIndex && (i + 1) <= lastIndex) {
						ret = true;
						if (mIsActive) {
							mAdapter.notifyDataSetChanged();
						}
					}
					break;
				}
			}
		}
		//如果列表可视范围下载进度更新了，但该container已经被移除或者处于非激活状态，那么在激活状态时需要重新刷一次界面
		if (ret && (this.getParent() == null || !mIsActive)) {
			mIsNeedToRefreshWhenActive = true;
		}
	}

	/** {@inheritDoc} */

	@Override
	public void setDownloadTaskList(List<DownloadTask> taskList) {
		mDownloadTaskList = (ArrayList<DownloadTask>) taskList;
	}

	/** {@inheritDoc} */

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

	/** {@inheritDoc} */

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
