package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.net.DownloadUtil;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.appgame.base.utils.NetworkTipsTool;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;

/**
 * 
 * <br>类描述:更多相关推荐View
 * <br>功能详细描述:
 * 
 * @author  zhengxiangcan
 * @date  [2012-12-18]
 */
public class MoreRecommendedAppsView extends RelativeLayout implements OnClickListener {
	private LayoutInflater mInflater = null;
	/**
	 * 没有数据时的view
	 */
	private RelativeLayout mNoDataView = null;
	/**
	 * view的控制器
	 */
	private MoreRecommendedAppsViewController mViewController = null;
	/**
	 * 每一页平均的应用数
	 */
	private int mNumPerPage = 0;
	/**
	 * 数据总页数
	 */
	private int mPages = -1;
	/**
	 * 已经加载的页数
	 */
	private int mCurrentPage = 0;
	/**
	 * 虚拟的typeId,写死302
	 */
	private int mTypeId = 302;
	/**
	 * 虚拟typeId的类型
	 */
	private int mItp = 2;
	private String mPkgName;
	/**
	 * 是否正在加载下一页
	 */
	private volatile boolean mLoadingNextPage = false;
	/**
	 * 外部的handler，用于回调
	 */
	private Handler mOutsideHandler = null;
	/**
	 * 出错信息的view 
	 */
	private NetworkTipsTool mNetworkTipsTool = null;
	/**
	 * 顶部的返回箭头
	 */
	private ImageButton mBackButton = null;
	/**	
	 * 顶部的title的文本
	 */
	private TextView mTitleTextView = null;
	/**
	 * listview
	 */
	private ListView mListView = null;
	/**
	 * 展示一句话简介的view
	 */
	private ContainerSummaryView mSummaryView = null;
	/**
	 * 加载下一页的进度条
	 */
	private CommonProgress mCommonProgress = null;
	/**
	 * CommonProgress的布局参数
	 */
	private RelativeLayout.LayoutParams mCommonProgressLP = null;

	private BaseAdapter mAdapter = null;
	/**
	 * 数据源
	 */
	private ArrayList<BoutiqueApp> mAppList = new ArrayList<BoutiqueApp>();
	/**
	 * 下载队列
	 */
	private ArrayList<DownloadTask> mDownloadTaskList = new ArrayList<DownloadTask>();

	public MoreRecommendedAppsView(Context context) {
		super(context);
	}

	public MoreRecommendedAppsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private void init() {
		initNetworkTipsView();
		mNetworkTipsTool.showProgress();
		mViewController = new MoreRecommendedAppsViewController(getContext(), mModeChangeListener);
		//重置图片管理器的列表控制信息
		AsyncImageManager.getInstance().restore();
	}

	@Override
	protected void onFinishInflate() {
		mInflater = LayoutInflater.from(getContext());
		init();
		mBackButton = (ImageButton) findViewById(R.id.more_apps_back);
		mBackButton.setOnClickListener(this);
		mTitleTextView = (TextView) findViewById(R.id.more_apps_title);
		mListView = (ListView) findViewById(R.id.more_apps_listview);
		mListView.setOnItemClickListener(mItemClickListener);
		mListView.setOnScrollListener(mScrollListener);
		mListView.setOnTouchListener(mListTouchListener);
		// 展示一句话简介的view
		mSummaryView = (ContainerSummaryView) mInflater.inflate(R.layout.appgame_container_summary,
				null);
		mListView.addHeaderView(mSummaryView, null, false);
		mSummaryView.viewGone();
		super.onFinishInflate();
	}
	
	public void setPkgName(String pkgName) {
		mPkgName = pkgName;
		loadNextPage(false);
//		String title = 
//		if (mTitleTextView != null) {
//			mTitleTextView.setText(title);
//		}
	}

	/**
	 * <br>功能简述:设置外部的handler
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param handler
	 */
	public void setOutsideHandler(Handler handler) {
		mOutsideHandler = handler;
	}

	/**
	 * <br>功能简述:初始化进度条的view
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void initNetworkTipsView() {
		ViewGroup tipsview = (ViewGroup) findViewById(R.id.more_apps_tips_view);
		mNetworkTipsTool = new NetworkTipsTool(tipsview);
	}

	/**
	 * <br>功能简述:假如没有数据，就会显示这个没有数据的view
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void showNoDataView() {
		if (mNoDataView == null) {
			if (mInflater == null) {
				mInflater = LayoutInflater.from(getContext());
			}
			mNoDataView = (RelativeLayout) mInflater.inflate(R.layout.themestore_nodata_tip_full,
					null);
		}
		mNoDataView.setGravity(Gravity.CENTER);
		addView(mNoDataView, new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT));
	}

	private IModeChangeListener mModeChangeListener = new IModeChangeListener() {
		@Override
		public void onModleChanged(int action, int state, Object value) {
			if (action == MoreRecommendedAppsViewController.sACTION_NEXT_PAGE_DATA) {
				mHandler.obtainMessage(mMSG_SHOW_CONTAINER_VIEW, value).sendToTarget();
			}
		}
	};

	private final int mMSG_SHOW_CONTAINER_VIEW = 222;

	private final int mMSG_TO_THE_END = 444;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case mMSG_TO_THE_END :
					removeCommonProgress();
					break;
				case mMSG_SHOW_CONTAINER_VIEW :
					mNetworkTipsTool.dismissProgress();
					removeCommonProgress();
					Object obj = msg.obj;
					if (obj == null || !(obj instanceof ClassificationDataBean)) {
						// 取回的数据不正确
						showNoDataView();
						return;
					}
					ClassificationDataBean bean = (ClassificationDataBean) obj;
					mLoadingNextPage = false;
					if (bean.featureList != null && bean.featureList.size() != 0
							&& bean.dataType == ClassificationDataBean.ONEPERLINE_SPECIALSUBJECT_TYPE) {
						// 一行一列样式
						mCurrentPage = bean.pageid;
						mPages = bean.pages;
						setListViewContent(bean);
						mNumPerPage = mAppList.size() / mCurrentPage;
					} else {
						showNoDataView();
					}
					break;
				default :
					break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		if (v == mBackButton) {
			if (mOutsideHandler != null) {
				mOutsideHandler.sendEmptyMessage(MoreRecommendedAppsActivity.sMSG_QUIT);
			}
		}
	}

	public void setDownloadTaskList(ArrayList<DownloadTask> list) {
		mDownloadTaskList = list;
	}

	public void notifyDownloadState(DownloadTask task) {
		if (task == null) {
			return;
		}
		// 这里判断下载中的应用是否在可视范围，在的话才调用notifyDataSetChanged
		int firstIndex = mListView.getFirstVisiblePosition();
		int lastIndex = mListView.getLastVisiblePosition();
		if (mAdapter != null) {
			int count = mAdapter.getCount();
			for (int i = 0; i < count; i++) {
				Object obj = mAdapter.getItem(i);
				if (obj != null && (obj instanceof BoutiqueApp)) {
					BoutiqueApp app = (BoutiqueApp) obj;
					if (app == null || app.info == null || app.info.appid == null) {
						continue;
					}
					if (app.info.appid.equals(task.getId() + "")) {
						app.downloadState.state = task.getState();
						app.downloadState.alreadyDownloadPercent = task.getAlreadyDownloadPercent();
						// 因为页面有一个headerview，所以位置要加上1
						if ((i + 1) >= firstIndex && (i + 1) <= lastIndex) {
							mAdapter.notifyDataSetChanged();
						}
						break;
					}
				}
			}
		}
	}

	/**
	 * <br>功能简述:设置listview的显示内容
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bean
	 */
	private void setListViewContent(ClassificationDataBean bean) {
		// 先使进度条不可见
		mNetworkTipsTool.removeProgress();
		// 设置listview可见
		mListView.setVisibility(View.VISIBLE);
		// 设置TopBar的title 
		if (bean.typename != null && !bean.typename.equals("")) {
			mTitleTextView.setText(bean.typename);
		}
		// 设置一句话简介
		if (bean.summary != null && !bean.summary.equals("")) {
			mSummaryView.viewVisible();
			mSummaryView.fillUp(bean.summary, true);
		}

		if (bean.dataType == ClassificationDataBean.ONEPERLINE_SPECIALSUBJECT_TYPE) {
			if (mAdapter == null) {
				NewExtraAdapter adapter = new NewExtraAdapter(getContext());
				adapter.onActiveChange(true);
				adapter.setDownloadTaskList(mDownloadTaskList);
				adapter.setDefaultIcon(getResources().getDrawable(R.drawable.default_icon));
				mAdapter = adapter;
				mListView.setAdapter(mAdapter);
			}
			if (bean.featureList != null) {
				for (BoutiqueApp app : bean.featureList) {
					mAppList.add(app);
				}
			}
			((NewExtraAdapter) mAdapter).updateList(mAppList);

		} 
	}

	// ListView的滑动监听
	private OnScrollListener mScrollListener = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState) {
				// 如果是滑到底部，加载下一页
				case OnScrollListener.SCROLL_STATE_IDLE : {
					if (view.getLastVisiblePosition() >= (view.getCount() - mNumPerPage / 2)) {
						loadNextPage(true);
					}
//
//					if (view.getLastVisiblePosition() >= (view.getCount() - 1)
//							&& mCurrentPage >= mPages) {
//						Toast.makeText(getContext(), R.string.appgame_list_end_tip,
//								Toast.LENGTH_SHORT).show();
//					}
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

	// ListView的点击监听
	private OnItemClickListener mItemClickListener = new OnItemClickListener() {
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
			if (app.info.effect == 1) {
				DownloadUtil.saveViewedEffectApp(getContext(), app.info.packname);
			}
			AppsDetail.jumpToDetail(getContext(), app, AppsDetail.START_TYPE_APPRECOMMENDED,
					position, true);
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

	/**
	 * 取下一页的数据
	 */
	private void loadNextPage(boolean showLoading) {
		if (mCurrentPage >= mPages && mCurrentPage > 0 && mPages > 0 || mCurrentPage == Integer.MIN_VALUE) {
			mLoadingNextPage = false;
			mHandler.obtainMessage(mMSG_TO_THE_END).sendToTarget();
			return;
		}
		if (mLoadingNextPage) {
			return;
		}
		if (mViewController != null) {
			Bundle bundle = new Bundle();
			bundle.putInt("typeId", mTypeId);
			bundle.putInt("itp", mItp);
			bundle.putInt("pageId", mCurrentPage + 1);
			bundle.putString("pkgName", mPkgName);
			if (mAdapter != null) {
				bundle.putInt("startIndex", mAdapter.getCount() + 1);
			} else {
				bundle.putInt("startIndex", 1);
			}
			mLoadingNextPage = true;
			mViewController.sendRequest(MoreRecommendedAppsViewController.sACTION_NEXT_PAGE, bundle);
			if (showLoading) {
				showCommonProgress();
			} else {
				removeCommonProgress();
			}
		}
	}
	
	/**
	 * 展示浮在列表底部的进度条
	 */
	private void showCommonProgress() {
		if (mCommonProgress == null) {
			mCommonProgress = (CommonProgress) mInflater.inflate(R.layout.appgame_common_progress,
					null);
			mCommonProgressLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
					DrawUtils.dip2px(40));
			mCommonProgressLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
			MoreRecommendedAppsView.this.addView(mCommonProgress, mCommonProgressLP);
		}
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
}
