package com.zhidian.wifibox.view;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.RankingAdapter;
import com.zhidian.wifibox.controller.RankController;
import com.zhidian.wifibox.data.AppDataBean;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.DownloadTaskRecorder;
import com.zhidian.wifibox.view.BgPageView.onCallBackOnClickListener;

/**
 * 排行榜列表，数据由RankingController提供
 * 
 * @author xiedezhi
 * 
 */
public class RankingContainer extends FrameLayout implements IContainer {
	private ListView mListView;
	/**
	 * 提示页面
	 */
	private BgPageView mTipsView;
	private ListViewSearchFooterView mFoot;
	private RankingAdapter mAdapter;
	/**
	 * 该页面的数据
	 */
	private PageDataBean mBean;
	/**
	 * 列表滑动底部的toast提示
	 */
	Toast mToast = Toast.makeText(getContext(), R.string.lastpage,
			Toast.LENGTH_SHORT);
	/**
	 * 是否正在加载下一页
	 */
	private volatile boolean mLoadingNexPage = false;
	/**
	 * controller数据回调接口
	 */
	private TAIResponseListener mRListener = new TAIResponseListener() {

		@Override
		public void onSuccess(TAResponse response) {
			// 这是下一页的数据
			mLoadingNexPage = false;
			PageDataBean bean = (PageDataBean) response.getData();
			// 如果不是当前列表的下一页数据，抛弃
			if (bean.mPageIndex != mBean.mPageIndex + 1) {
				return;
			}
			if (bean.mStatuscode == 0) {
				// 加载成功，展示列表
				// 先缓存列表
				// 这里应该要用下载任务列表初始化一下刚刚拿到的数据
				updateDownloadState(bean.mAppList);
				TabDataManager.getInstance().cachePageData(bean);
				// 更新列表

				mAdapter.update(mBean.mAppList, mBean.mStatisticsTitle);
				mTipsView.showContent();
				if (bean.mPageIndex >= bean.mTotalPage) {
					// 如果这是最后一页，改变footview
					mFoot.viewGone();
				}
				if (mBean.mAppList.size() <= 0) {
					mTipsView.showNoContentTip();
				}
			} else {
				// 加载失败
				if (bean.mPageIndex == 1) {
					// 如果是第一页，展示错误提示页
					mTipsView
							.showLoadException(new onCallBackOnClickListener() {

								@Override
								public void onClick() {
									// 如果正在加载下一页，则不需要再加载
									if (mLoadingNexPage) {
										return;
									}
									// 数据还没初始化
									// idurl,dataurl,要加载的页码
									String[] obj = { mBean.mUrl, mBean.mUrl,
											1 + "",
											mBean.mLoadLocalDataFirst + "" };
									TARequest request = new TARequest(
											RankController.NEXT_PAGE, obj);
									TAApplication
											.getApplication()
											.doCommand(
													getContext()
															.getString(
																	R.string.rankingcontroller),
													request, mRListener, true,
													false);
									// 展示loading页
									mTipsView.showProgress();
									mLoadingNexPage = true;
								}
							});

				} else {
					// 如果不是第一页，展示重试footview
					mFoot.showRetry(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// 如果正在加载下一页，则不需要再加载
							if (mLoadingNexPage) {
								return;
							}
							int pageNo = mBean.mPageIndex + 1;
							// 下一页的URL
							String dataUrl = mBean.mUrl.replaceFirst(
									"pageNo=1", "pageNo=" + pageNo);
							String[] obj = { mBean.mUrl, dataUrl, pageNo + "",
									mBean.mLoadLocalDataFirst + "" };
							TARequest request = new TARequest(
									RankController.NEXT_PAGE, obj);
							TAApplication.getApplication().doCommand(
									getContext().getString(
											R.string.rankingcontroller),
									request, mRListener, true, false);
							mLoadingNexPage = true;
							mFoot.showLoading();
						}
					});
				}
			}
		}

		@Override
		public void onStart() {
		}

		@Override
		public void onRuning(TAResponse response) {
		}

		@Override
		public void onFinish() {
		}

		@Override
		public void onFailure(TAResponse response) {
		}
	};
	/**
	 * 滑动监听
	 */
	private OnScrollListener mScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// 如果是滑到底部，加载下一页
			if (view.getLastVisiblePosition() >= (view.getCount() - 1)) {
				if (mBean.mPageIndex >= mBean.mTotalPage) {
					// 如果是最后一页，弹toast提示
					mToast.show();
					return;
				}
				// 如果正在加载下一页，则不需要再加载
				if (mLoadingNexPage) {
					return;
				}
				int pageNo = mBean.mPageIndex + 1;
				// 下一页的URL
				String dataUrl = mBean.mUrl.replaceFirst("pageNo=1", "pageNo="
						+ pageNo);
				String[] obj = { mBean.mUrl, dataUrl, pageNo + "",
						mBean.mLoadLocalDataFirst + "" };
				TARequest request = new TARequest(RankController.NEXT_PAGE, obj);
				TAApplication.getApplication().doCommand(
						getContext().getString(R.string.rankingcontroller),
						request, mRListener, true, false);
				mLoadingNexPage = true;
				mFoot.showLoading();
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
	};

	public RankingContainer(Context context) {
		super(context);
	}

	public RankingContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RankingContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		mListView = (ListView) findViewById(R.id.listview);
		LinearLayout tips = (LinearLayout) findViewById(R.id.tipsview);
		mTipsView = new BgPageView(getContext(), tips, mListView);
		mAdapter = new RankingAdapter(getContext());
		mListView.setOnScrollListener(mScrollListener);
		// 添加footview
		mFoot = (ListViewSearchFooterView) LayoutInflater.from(getContext())
				.inflate(R.layout.view_listview_footer, null);
		mFoot.showLoading();
		mListView.addFooterView(mFoot);
		mListView.setAdapter(mAdapter);
	}

	@Override
	public void onAppAction(String packName) {
		// 更改列表应用安装状态
		boolean needToUpdate = false;
		if (mBean != null && mBean.mAppList != null) {
			for (AppDataBean bean : mBean.mAppList) {
				if (bean.packName.equals(packName)) {
					needToUpdate = true;
					break;
				}
			}
		}
		if (needToUpdate) {
			updateDownloadState(mBean.mAppList);
			mAdapter.update(mBean.mAppList, mBean.mStatisticsTitle);
		}
	}

	@Override
	public String getDataUrl() {
		return mBean.mUrl;
	}

	@Override
	public void updateContent(PageDataBean bean) {
		if (bean == null) {
			return;
		}
		mBean = bean;
		// 这里传进来的数据要不就是没有初始化的，要不就是已经拿到数据的，不存在服务器错误的情况，因为这种情况不会把数据缓存
		if (bean.mStatuscode == -1) {
			// 数据还没初始化
			// idurl,dataurl,要加载的页码
			String[] obj = { bean.mUrl, bean.mUrl, 1 + "",
					mBean.mLoadLocalDataFirst + "" };
			TARequest request = new TARequest(RankController.NEXT_PAGE, obj);
			if (!mLoadingNexPage) {
				TAApplication.getApplication().doCommand(
						getContext().getString(R.string.rankingcontroller),
						request, mRListener, true, false);
				mLoadingNexPage = true;
			}
			// 展示loading页
			mTipsView.showProgress();
			mLoadingNexPage = true;
		} else {
			// 这里应该要用下载任务列表初始化一下应用数据列表
			updateDownloadState(bean.mAppList);
			// 展示数据
			mAdapter.update(bean.mAppList, bean.mStatisticsTitle);
			if (bean.mPageIndex >= bean.mTotalPage) {
				// 如果这是最后一页，改变footview
				mFoot.viewGone();
			}
			if (bean.mAppList.size() <= 0) {
				mTipsView.showNoContentTip();
			} else {
				mTipsView.showContent();
			}
		}
	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {
		boolean needToUpdate = false;
		if (mBean != null && mBean.mAppList != null) {
			for (AppDataBean bean : mBean.mAppList) {
				if (bean.downloadUrl.equals(downloadTask.url)) {
					needToUpdate = true;
					bean.downloadStatus = downloadTask.state;
					bean.alreadyDownloadPercent = downloadTask.alreadyDownloadPercent;
				}
			}
		}
		if (needToUpdate) {
			mAdapter.update(mBean.mAppList, mBean.mStatisticsTitle);
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

	@Override
	public void onResume() {
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void beginPage() {
		if (mBean == null) {
			return;
		}
		StatService.trackBeginPage(getContext(), "" + mBean.mStatisticsTitle);
	}

	@Override
	public void endPage() {
		if (mBean == null) {
			return;
		}
		StatService.trackEndPage(getContext(), "" + mBean.mStatisticsTitle);
	}

}
