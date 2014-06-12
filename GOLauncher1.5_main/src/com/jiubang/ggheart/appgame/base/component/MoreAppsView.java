/*
 * 文 件 名:  MoreAppsView.java
 * 版    权:  3G
 * 描    述:  <描述>
 * 修 改 人:  liuxinyang
 * 修改时间:  2012-11-6
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.DeferredHandler;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.appgame.base.bean.BoutiqueApp;
import com.jiubang.ggheart.appgame.base.bean.ClassificationDataBean;
import com.jiubang.ggheart.appgame.base.manage.AsyncImageManager;
import com.jiubang.ggheart.appgame.base.utils.AppGameDrawUtils;
import com.jiubang.ggheart.appgame.base.utils.NetworkTipsTool;
import com.jiubang.ggheart.appgame.download.DownloadTask;
import com.jiubang.ggheart.appgame.gostore.base.component.GridThreeCellAdapter;
import com.jiubang.ggheart.appgame.gostore.base.component.GridViewController;
import com.jiubang.ggheart.appgame.gostore.base.component.WallpaperGridController;
import com.jiubang.ggheart.appgame.gostore.base.component.WallpaperThreeCellAdapter;
import com.jiubang.ggheart.apps.gowidget.gostore.controller.IModeChangeListener;
import com.jiubang.ggheart.data.statistics.AppManagementStatisticsUtil;

/**
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  liuxinyang
 * @date  [2012-11-6]
 */
public class MoreAppsView extends RelativeLayout implements OnClickListener {

	private LayoutInflater mInflater = null;
	/**
	 * 没有数据时的view
	 */
	private RelativeLayout mNoDataView = null;
	/**
	 * view的控制器
	 */
	private MoreAppsViewController mViewController = null;
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
	 * 虚拟的typeId
	 */
	private int mTypeId = -1;
	/**
	 * 虚拟typeId的类型
	 */
	private int mItp = 2;
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

	public MoreAppsView(Context context) {
		super(context);
	}

	public MoreAppsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private void init() {
		initNetworkTipsView();
		mNetworkTipsTool.showProgress();
		mViewController = new MoreAppsViewController(getContext(), mModeChangeListener);
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
		mListView.setOnScrollListener(mScrollListener);
		// 展示一句话简介的view
		mSummaryView = (ContainerSummaryView) mInflater.inflate(R.layout.appgame_container_summary,
				null);
		mListView.addHeaderView(mSummaryView, null, false);
		mSummaryView.viewGone();
		super.onFinishInflate();
		
	}
	
	/**
	 * 设置虚拟分类id，并开始加载数据
	 * 
	 * @param typeId
	 */
	public void startLoadData(int typeId) {
		mTypeId = typeId;
		if (mViewController != null) {
			loadNextPage(false);
		}
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
			if (action == MoreAppsViewController.sACTION_NEXT_PAGE_DATA) {
				mHandler.sendMessage(mMSG_SHOW_CONTAINER_VIEW, -1, -1, value);
			}
		}
	};

	private final int mMSG_SHOW_CONTAINER_VIEW = 222;

	private final int mMSG_TO_THE_END = 444;

	private DeferredHandler mHandler = new DeferredHandler() {
		@Override
		public void handleIdleMessage(Message msg) {
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
					//拿到第一页数据后统计tab点击一次
					if (bean != null && bean.pageid == 1) {
						AppManagementStatisticsUtil.getInstance();
						AppManagementStatisticsUtil
								.saveTabClickData(getContext(), bean.typeId, null);
					}
					mLoadingNextPage = false;
					if (bean.featureList != null
							&& bean.featureList.size() > 0
							&& bean.dataType == ClassificationDataBean.ONEPERLINE_SPECIALSUBJECT_TYPE) {
						// 一行一列样式
						mListView.setOnItemClickListener(mItemClickListener);
						mCurrentPage = bean.pageid;
						mPages = bean.pages;
						setListViewContent(bean);
						mNumPerPage = (int) (mAppList.size() / 1.0 / mCurrentPage);
					} else if (bean.featureList != null && bean.featureList.size() > 0
							&& bean.dataType == ClassificationDataBean.EDITOR_RECOMM_TYPE) {
						// 编辑推荐样式
						mListView.setOnItemClickListener(mItemClickListener);
						mCurrentPage = bean.pageid;
						mPages = bean.pages;
						setListViewContent(bean);
						mNumPerPage = (int) (mAppList.size() / 1.0 / mCurrentPage);
					} else if (bean.featureList != null && bean.featureList.size() > 0
							&& bean.dataType == ClassificationDataBean.FEATURE_TYPE) {
						// 精品推荐
						mListView.setOnItemClickListener(null);
						mCurrentPage = bean.pageid;
						mPages = bean.pages;
						setListViewContent(bean);
						mNumPerPage = (int) (mAppList.size() / 2.0 / mCurrentPage + 0.5);
					} else if (bean.featureList != null && bean.featureList.size() > 0
							&& bean.dataType == ClassificationDataBean.GRID_TYPE) {
						// 主题九宫格
						mListView.setOnItemClickListener(null);
						mCurrentPage = bean.pageid;
						mPages = bean.pages;
						setListViewContent(bean);
						mNumPerPage = (int) (mAppList.size() / 3.0 / mCurrentPage + 0.5);
					}  else if (bean.featureList != null && bean.featureList.size() > 0
							&& bean.dataType == ClassificationDataBean.WALLPAPER_GRID) {
						// 壁纸九宫格
						mListView.setOnItemClickListener(null);
						mCurrentPage = bean.pageid;
						mPages = bean.pages;
						setListViewContent(bean);
						mNumPerPage = (int) (mAppList.size() / 3.0 / mCurrentPage + 0.5);
					} else {
						showNoDataView();
					}
					//如果是最后一页数据，就把loading条取消
					if (mCurrentPage >= mPages && mCurrentPage > 0 && mPages > 0) {
						mLoadingNextPage = false;
						mHandler.sendEmptyMessage(mMSG_TO_THE_END);
						return;
					}
					break;
				default :
					break;
			}
		}
	};

	@Override
	public void onClick(View v) {
		if (v == mBackButton) {
			if (mOutsideHandler != null) {
				mOutsideHandler.sendEmptyMessage(MoreAppsActivity.sMSG_QUIT);
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
		if (bean.summary != null && !bean.summary.equals("") && !bean.summary.equalsIgnoreCase("null")) {
			mSummaryView.viewVisible();
			mSummaryView.fillUp(bean.summary, true);
		} else {
			mSummaryView.viewGone();
		}

		// 判断数据类型以便采用不同的adapter
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
			mAdapter.notifyDataSetChanged();
		} else if (bean.dataType == ClassificationDataBean.EDITOR_RECOMM_TYPE) {
			if (mAdapter == null) {
				EditorFavoriteAdapter adapter = new EditorFavoriteAdapter(getContext());
				adapter.onActiveChange(true);
				adapter.setDownloadTaskList(mDownloadTaskList);
				mAdapter = adapter;
				mListView.setAdapter(mAdapter);
			}
			if (bean.featureList != null) {
				for (BoutiqueApp app : bean.featureList) {
					mAppList.add(app);
				}
			}
			((EditorFavoriteAdapter) mAdapter).updateList(mAppList);
			mAdapter.notifyDataSetChanged();
		} else if (bean.dataType == ClassificationDataBean.FEATURE_TYPE) {
			// 精品推荐
			if (mAdapter == null) {
				FeatureTwoCellAdapter adapter = new FeatureTwoCellAdapter(getContext());
				FeatureController controller = new FeatureController(getContext(), null);
				adapter.setFeatureController(controller);
				adapter.onActiveChange(true);
				adapter.setDownloadTaskList(mDownloadTaskList);
				adapter.setDefaultIcon(getResources().getDrawable(R.drawable.default_icon));
				mAdapter = adapter;
				mListView.setAdapter(mAdapter);
			}
			if (bean.featureList != null) {
				for (BoutiqueApp app : bean.featureList) {
					if (app.cellsize == 6) {
						mAppList.add(app);
					}
				}
			}
			((FeatureTwoCellAdapter) mAdapter).update(mAppList);
			mAdapter.notifyDataSetChanged();
		} else if (bean.dataType == ClassificationDataBean.GRID_TYPE) {
			// 主题九宫格
			if (mAdapter == null) {
				GridThreeCellAdapter adapter = new GridThreeCellAdapter(getContext());
				int id = R.drawable.appcenter_feature_default_banner;
				adapter.setDefaultIcon(getResources().getDrawable(id));
				GridViewController controller = new GridViewController(getContext(), null);
				adapter.setGridController(controller);
				adapter.onActiveChange(true);
				mAdapter = adapter;
				mListView.setAdapter(mAdapter);
			}
			if (bean.featureList != null) {
				for (BoutiqueApp app : bean.featureList) {
					mAppList.add(app);
				}
			}
			((GridThreeCellAdapter) mAdapter).update(mAppList);
			mAdapter.notifyDataSetChanged();
		} else if (bean.dataType == ClassificationDataBean.WALLPAPER_GRID) {
			//  壁纸九宫格
			if (mAdapter == null) {
				WallpaperThreeCellAdapter adapter = new WallpaperThreeCellAdapter(getContext());
				int id = R.drawable.appcenter_feature_default_banner;
				adapter.setDefaultIcon(getResources().getDrawable(id));
				WallpaperGridController controller = new WallpaperGridController(getContext(), null);
				adapter.setWallController(controller);
				adapter.onActiveChange(true);
				mAdapter = adapter;
				mListView.setAdapter(mAdapter);
			}
			if (bean.featureList != null) {
				for (BoutiqueApp app : bean.featureList) {
					mAppList.add(app);
				}
			}
			((WallpaperThreeCellAdapter) mAdapter).update(mAppList);
			mAdapter.notifyDataSetChanged();
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

					if (view.getLastVisiblePosition() >= (view.getCount() - 1)
							&& mCurrentPage >= mPages) {
						Toast.makeText(getContext(), R.string.appgame_list_end_tip,
								Toast.LENGTH_SHORT).show();
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

	// ListView的点击监听
	private OnItemClickListener mItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			try {
				Object tag = view.getTag(R.id.appgame);
				BoutiqueApp app = null;
				if (tag != null && tag instanceof BoutiqueApp) {
					app = (BoutiqueApp) tag;
				}
				if (app == null) {
					return;
				}
				AppsDetail.jumpToDetail(getContext(), app, AppsDetail.START_TYPE_APPRECOMMENDED,
						position, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * 取下一页的数据
	 */
	private void loadNextPage(boolean showLoading) {
		if (mCurrentPage >= mPages && mCurrentPage > 0 && mPages > 0) {
			mLoadingNextPage = false;
			mHandler.sendEmptyMessage(mMSG_TO_THE_END);
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
			if (mAdapter != null) {
				bundle.putInt("startIndex", mAdapter.getCount() + 1);
			} else {
				bundle.putInt("startIndex", 1);
			}
			mLoadingNextPage = true;
			mViewController.sendRequest(MoreAppsViewController.sACTION_NEXT_PAGE, bundle);
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
			MoreAppsView.this.addView(mCommonProgress, mCommonProgressLP);
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
