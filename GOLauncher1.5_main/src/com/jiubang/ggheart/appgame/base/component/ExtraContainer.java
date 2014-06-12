package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;

import com.gau.go.launcherex.R;
import com.gau.utils.net.IConnectListener;
import com.gau.utils.net.request.THttpRequest;
import com.gau.utils.net.response.IResponse;
import com.go.util.device.Machine;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.CategoriesDataBean;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.component.SpecialSubjectListView.onItemClickListener;
import com.jiubang.ggheart.appgame.base.data.AppJsonOperator;
import com.jiubang.ggheart.appgame.base.data.ClassificationDataDownload;
import com.jiubang.ggheart.appgame.base.data.ClassificationExceptionRecord;
import com.jiubang.ggheart.appgame.base.manage.TabController;
import com.jiubang.ggheart.appgame.base.menu.AppGameMenu;
import com.jiubang.ggheart.appgame.base.net.AppGameNetRecord;
import com.jiubang.ggheart.appgame.base.net.AppHttpAdapter;
import com.jiubang.ggheart.appgame.base.utils.NetworkTipsTool;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.config.ChannelConfig;
import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * Top Free,New Free,Must Have等专题推荐的视图。数据由外部填充，该类只负责展示内容（该排版样式已经不再使用）
 * 
 * @author xiedezhi
 * 
 */
@Deprecated
public class ExtraContainer extends FrameLayout implements IContainer {
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
	 * 标志是否需要重设listview的scroll距离，每次更新数据都需要设为true
	 */
	private boolean mIsNeedToResetScroll = false;
	/**
	 * 当前正在加载下一页的线程
	 */
	private NextPageRunnable mCurrentNextPageRunnable = null;

	private static final int LISTVIEW_MSG_LOADING = 2001;
	private static final int LISTVIEW_MSG_LOAD_END = 2002;
	private static final int LISTVIEW_MSG_LOAD_FINISH = 2005;

	private boolean mIsActive = false;
	private boolean mShowingError = false;

	private NetworkTipsTool mNetworkTipsTool;
	private SpecialSubjectListView mListView;
	/**
	 * 数据列表
	 */
	private List<BoutiqueApp> mAppList = new ArrayList<BoutiqueApp>();
	/**
	 * 每页的应用数
	 */
	private double mNumPerPage = -1;
	/**
	 * listview加载下一页失败时的重试点击监听器
	 */
	private final OnClickListener mListViewRetryListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			loadNextPage();
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
	/**
	 * listview滑动监听事件
	 */
	private OnScrollListener mScrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
			case OnScrollListener.SCROLL_STATE_IDLE:
				if (view.getLastVisiblePosition() >= (view.getCount() - mNumPerPage / 2)) {
					loadNextPage();
				}
				break;
			default:
				break;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
				int totalItemCount) {

		}
	};
	/**
	 * listview的触摸监听事件，android低版本的机型里，listview有时候滑动底部收不到相应的回调消息，需要手动加入回调
	 * http://code.google.com/p/android/issues/detail?id=5086
	 */
	private OnTouchListener mListTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent event) {
			final int action = event.getAction();
			boolean mFingerUp = action == MotionEvent.ACTION_UP
					|| action == MotionEvent.ACTION_CANCEL;
			if (mFingerUp) {
				mScrollListener.onScrollStateChanged((AbsListView) view,
						OnScrollListener.SCROLL_STATE_FLING);
				mScrollListener.onScrollStateChanged((AbsListView) view,
						OnScrollListener.SCROLL_STATE_IDLE);
			}
			return false;
		}
	};

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LISTVIEW_MSG_LOAD_END:
				mListView.removeMoreFooter();
				break;
			case LISTVIEW_MSG_LOADING:
				mListView.showLoadingAnotherPage();
				break;
			case LISTVIEW_MSG_LOAD_FINISH:
				mLoadingNextPage = false;
				Object mobj = msg.obj;
				if (mobj == null || !(mobj instanceof ClassificationDataBean)) {
					// 加载失败
					mListView.showLoadRetry(mListViewRetryListener);
					return;
				}
				ClassificationDataBean mbean = (ClassificationDataBean) mobj;
				if (mbean.featureList == null || mbean.featureList.size() <= 0) {
					// 加载失败
					mListView.showLoadRetry(mListViewRetryListener);
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
					// 如果在激活状态，通知adapter更新界面
					if (mIsActive) {
						mListView.updateList(mAppList, true);
					}
				} else {
					Log.e("ExtraContainer", "mCurrentPage = " + mCurrentPage + "  mbean.pageid = "
							+ mbean.pageid);
				}
				if (mCurrentPage >= mPages) {
					mListView.removeMoreFooter();
				}
				break;
			default:
				break;
			}
		};
	};

	public ExtraContainer(Context context) {
		super(context);
	}

	public ExtraContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ExtraContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		ViewGroup tipsView = (ViewGroup) findViewById(R.id.topfree_tips_view);
		mNetworkTipsTool = new NetworkTipsTool(tipsView);
		mNetworkTipsTool.showNothing();

		mListView = (SpecialSubjectListView) findViewById(R.id.topfree_listview);
		mListView.setItemClickListener(new onItemClickListener() {

			@Override
			public void onItemClick(BoutiqueApp app, int index) {
				if (app != null) {
					AppsDetail.jumpToDetail(getContext(), app,
							AppsDetail.START_TYPE_APPRECOMMENDED, index, true);
				}
			}
		});
		mListView.setOnScrollListener(mScrollListener);
		mListView.setOnTouchListener(mListTouchListener);
	}

	/**
	 * 刷新界面
	 */
	private void doRefresh() {
		if (!mShowingError) {
			if (!FileUtil.isSDCardAvaiable()) {
				mListView.setVisibility(View.GONE);
				mNetworkTipsTool.showNothing();
				mNetworkTipsTool.showRetryErrorTip(null, false);
				mShowingError = true;
				return;
			}
		}
		if (mAppList == null || mAppList.size() <= 0) {
			return;
		}
		if (mIsActive) {
			mNetworkTipsTool.showNothing();
			mListView.updateList(mAppList, true);
			mListView.setVisibility(View.VISIBLE);
			if (mCurrentPage >= mPages) {
				mLoadingNextPage = false;
				mHandler.obtainMessage(LISTVIEW_MSG_LOAD_END).sendToTarget();
			}
			// 重置scroll距离
			if (mIsNeedToResetScroll) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						mListView.setSelection(0);
					}
				});
				mIsNeedToResetScroll = false;
			}
		}
		mShowingError = false;
	}

	/**
	 * 加载下一页的线程，可以杀死
	 * 
	 * @author xiedezhi
	 * 
	 */
	private class NextPageRunnable implements Runnable {
		/**
		 * 该线程是否已被杀死
		 */
		private boolean mIsKilled = false;
		private Object mIsKilledLock = new Object();

		/**
		 * 标志该线程已被杀死，后台拿到数据后不做处理
		 */
		public void kill() {
			synchronized (mIsKilledLock) {
				mIsKilled = true;
			}
		}

		/**
		 * 判断当前线程是否已被杀死
		 * 
		 */
		public boolean isKilled() {
			synchronized (mIsKilledLock) {
				return mIsKilled;
			}
		}

		@Override
		public void run() {
			mLoadingNextPage = true;
			mHandler.obtainMessage(LISTVIEW_MSG_LOADING).sendToTarget();

			String url = ClassificationDataDownload.getUrl(getContext());
			final int[] typeIds = new int[] { mTypeId };
			final JSONObject postdata = ClassificationDataDownload.getPostJson(getContext(), typeIds, -1, mCurrentPage + 1, 0);
			THttpRequest request = null;
			try {
				request = new THttpRequest(url, postdata.toString().getBytes(),
						new IConnectListener() {

							@Override
							public void onStart(THttpRequest arg0) {
							}

							@Override
							public void onFinish(THttpRequest request, IResponse response) {
								if (response != null && response.getResponse() != null
										&& (response.getResponse() instanceof JSONObject)) {
									try {
										JSONObject json = (JSONObject) response.getResponse();
										List<ClassificationDataBean> beans = ClassificationDataDownload.getClassificationData(
												json, postdata, getContext(), typeIds,
												mCurrentPage + 1, mAppList.size() + 1, true);
										if (beans == null || beans.size() <= 0) {
											notifyError();
											return;
										}
										ClassificationDataBean bean = beans.get(0);
										if (isKilled()) {
											return;
										}
										mHandler.obtainMessage(LISTVIEW_MSG_LOAD_FINISH, bean)
												.sendToTarget();
										kill();
										return;
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
								notifyError();
							}

							@Override
							public void onException(THttpRequest arg0, int arg1) {
								notifyError();
							}
						});
			} catch (Exception e) {
				notifyError();
				return;
			}
			if (request != null) {
				// 设置备选url
				try {
					request.addAlternateUrl(ClassificationDataDownload.getAlternativeUrl(
							getContext()));
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 设置线程优先级，读取数据的线程优先级应该最高（比拿取图片的线程优先级高）
				request.setRequestPriority(Thread.MAX_PRIORITY);
				request.setOperator(new AppJsonOperator());
				request.setNetRecord(new AppGameNetRecord(getContext(), false));
				AppHttpAdapter httpAdapter = AppHttpAdapter.getInstance(getContext());
				httpAdapter.addTask(request, true);
			}
		}

		private void notifyError() {
			if (isKilled()) {
				return;
			}
			mHandler.obtainMessage(LISTVIEW_MSG_LOAD_FINISH, null).sendToTarget();
			kill();
		}
	}

	/**
	 * 取下一页的数据
	 */
	private void loadNextPage() {
		if (mCurrentPage >= mPages) {
			mLoadingNextPage = false;
			mHandler.obtainMessage(LISTVIEW_MSG_LOAD_END).sendToTarget();
			return;
		}
		if (mLoadingNextPage) {
			return;
		}
		mCurrentNextPageRunnable = new NextPageRunnable();
		mCurrentNextPageRunnable.run();
	}

	@Override
	public void onActiveChange(boolean isActive) {
		if (mIsActive == isActive) {
			return;
		}
		mIsActive = isActive;
		if (isActive) {
			if (!mShowingError) {
				doRefresh();
			}
		} else {
			/*
				// 如果更新数据为空，直接返回
				if (mAppList == null || mAppList.size() <= 0) {
				return;
				}
				mListView.updateList(mAppList, false);
				*/
		}
	}

	@Override
	public void cleanup() {
		mListView.updateList(null, false);
		if (mAppList != null) {
			mAppList.clear();
		}
	}

	/**
	 * sd卡已经关闭
	 */
	@Override
	public void sdCardTurnOff() {
		mListView.setVisibility(View.GONE);
		if (mNetworkTipsTool != null) {
			mNetworkTipsTool.showRetryErrorTip(null, false);
		}
		mShowingError = true;
	}

	/**
	 * sd卡已经开启
	 */
	@Override
	public void sdCardTurnOn() {
		doRefresh();
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
		if (mIsActive) {
			mListView.updateList(mAppList, false);
		}
	}

	@Override
	public void onAppAction(String packName, int appAction) {
		// 应用安装卸载事件不用处理
	}

	@Override
	public void updateContent(ClassificationDataBean bean, boolean isPrevLoadRefresh) {
		if (bean == null || bean.dataType != ClassificationDataBean.SPECIALSUBJECT_TYPE) {
			Log.e("ExtraContainer",
					"updateContent  bean == null|| bean.dataType != ClassificationDataBean.SPECIALSUBJECT_TYPE");
			return;
		}
		if (isPrevLoadRefresh && bean.featureList == null) {
			return;
		}
		if (bean.featureList == null) {
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
		if (mCurrentNextPageRunnable != null) {
			mCurrentNextPageRunnable.kill();
		}
		// 保存分类id
		mTypeId = bean.typeId;
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
		// TODO:XIEDEZHI 预加载刷新需要把加载下一页的线程杀死吗?
		mLoadingNextPage = false;
		mIsNeedToResetScroll = false;
		// 重置footview
		mListView.showLoadingAnotherPage();
		if (!isPrevLoadRefresh) {
			// 如果不是预加载后刷新，显示进度条
			// 重置状态
			mIsActive = false;
			mShowingError = false;
			// 标志需要重置scroll距离
			mIsNeedToResetScroll = true;
			// 显示进度条
			mListView.setVisibility(View.GONE);
			mNetworkTipsTool.showNothing();
			mNetworkTipsTool.showProgress();
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

	}

	@Override
	public void notifyDownloadState(DownloadTask downlaodTask) {

	}

	@Override
	public void setDownloadTaskList(List<DownloadTask> taskList) {

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
		case IMenuHandler.MENU_ITEM_FRESH:
			// 整个tab栏刷新
			TabController.refreshCurrentTab();
			return true;
		}
		return false;
	}

	@Override
	public void onTrafficSavingModeChange() {
		// do nothing
	}

	@Override
	public void setUpdateData(Object value, int state) {
		//do nothing
	}
	
	@Override
	public void prevLoading() {
		//do nothing
	}

	@Override
	public void prevLoadFinish() {
		//do nothing
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
		//do nothing			
	}
	
	@Override
	public void setBuilder(ContainerBuiler builder) {
		// do nothing
	}

}
