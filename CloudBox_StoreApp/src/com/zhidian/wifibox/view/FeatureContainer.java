package com.zhidian.wifibox.view;

import java.util.List;
import java.util.Map;

import za.co.immedia.pinnedheaderlistview.PinnedHeaderListView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.adapter.FeatureAdapter;
import com.zhidian.wifibox.controller.FeatureController;
import com.zhidian.wifibox.data.AppDataBean;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.DownloadTaskRecorder;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.Setting;
import com.zhidian.wifibox.view.BgPageView.onCallBackOnClickListener;

/**
 * 推荐列表页，数据由FeatureController加载
 * 
 * 加载下一页数据时，虽然url跟第一页不一样，但解析成PageDataBean时，bean.mUrl还是初始化为第一页url
 * 
 * 有分页
 * 
 * 
 * @author xiedezhi
 * 
 */
public class FeatureContainer extends LinearLayout implements IContainer {

	private PinnedHeaderListView mListView;
	/**
	 * 提示页面
	 */
	private BgPageView mTipsView;
	private ListViewSearchFooterView mFoot;
	private FeatureAdapter mAdapter;
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
	 * 幻灯片组件
	 */
	private BannerView mBannerView;

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
				mAdapter.update(mBean.mAppList);
				mTipsView.showContent();
				if (bean.mPageIndex >= bean.mTotalPage) {
					// 如果这是最后一页，改变footview
					mFoot.viewGone();
				}
				if (mBean.mAppList.size() <= 0) {
					mTipsView.showNoContentTip();
				}
				// 判断是否已经显示过提示页了
				Setting setting = new Setting(getContext());
				if (!setting.getBoolean(Setting.HAS_SHOW_CMODETIP, false)) {
					MainActivity.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
							IDiyMsgIds.SHOW_C_TIPS_PAGE, -1, null, null);
					// 保存已显示过提示页
					setting.putBoolean(Setting.HAS_SHOW_CMODETIP, true);
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
									// idurl,dataurl,要加载的页码,是否先加载本地数据
									String[] obj = { mBean.mUrl, mBean.mUrl,
											1 + "",
											mBean.mLoadLocalDataFirst + "" };
									TARequest request = new TARequest(
											FeatureController.NEXT_PAGE, obj);
									TAApplication
											.getApplication()
											.doCommand(
													getContext()
															.getString(
																	R.string.featurecontroller),
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
									false + "" };
							TARequest request = new TARequest(
									FeatureController.NEXT_PAGE, obj);
							TAApplication.getApplication().doCommand(
									getContext().getString(
											R.string.featurecontroller),
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
				showNextPage();

			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
	};

	public FeatureContainer(Context context) {
		super(context);
	}

	public FeatureContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		mListView = (PinnedHeaderListView) findViewById(R.id.feature_pinnedListView);
		LinearLayout tips = (LinearLayout) findViewById(R.id.tipsview);
		mTipsView = new BgPageView(getContext(), tips, mListView);
		mAdapter = new FeatureAdapter(getContext());
		// bannerview
		LayoutInflater inflater = LayoutInflater.from(getContext());
		mBannerView = (BannerView) inflater.inflate(R.layout.view_adv_roll,
				null);
		int bannerHeight = (int) ((new InfoUtil(getContext()).getWidth()) / 480.0 * 170.0 + 0.5);
		mBannerView.setLayoutParams(new AbsListView.LayoutParams(
				AbsListView.LayoutParams.MATCH_PARENT, bannerHeight));
		mListView.addHeaderView(mBannerView);
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
			mAdapter.update(mBean.mAppList);
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
			// idurl,dataurl,要加载的页码,是否先加载本地数据
			String[] obj = { bean.mUrl, bean.mUrl, 1 + "",
					bean.mLoadLocalDataFirst + "" };
			TARequest request = new TARequest(FeatureController.NEXT_PAGE, obj);
			// 如果正在加载下一页，则不需要再加载
			if (!mLoadingNexPage) {
				TAApplication.getApplication().doCommand(
						getContext().getString(R.string.featurecontroller),
						request, mRListener, true, false);
				mLoadingNexPage = true;
			}
			// 展示loading页
			mTipsView.showProgress();
			mLoadingNexPage = true;
		} else {
			// 展示数据
			// 这里应该要用下载任务列表初始化一下应用数据列表
			updateDownloadState(bean.mAppList);
			mAdapter.update(bean.mAppList);
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
		if (bean.mShowBanner) {
			mBannerView.setVisibility(View.VISIBLE);
			// 展示幻灯片，要处理先加载本地数据的情况
			mBannerView.init(bean.mBannerMark, bean.mLoadLocalDataFirst);
		} else {
			mBannerView.setVisibility(View.GONE);
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
			mAdapter.update(mBean.mAppList);
		}
	}

	/*****************
	 * 显示下一页
	 *****************/
	private void showNextPage() {
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
		String dataUrl = mBean.mUrl
				.replaceFirst("pageNo=1", "pageNo=" + pageNo);
		String[] obj = { mBean.mUrl, dataUrl, pageNo + "", false + "" };
		TARequest request = new TARequest(FeatureController.NEXT_PAGE, obj);
		TAApplication.getApplication().doCommand(
				getContext().getString(R.string.featurecontroller), request,
				mRListener, true, false);
		mLoadingNexPage = true;
		mFoot.showLoading();
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

	/****************************
	 * 幻灯片部分
	 ****************************/
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		float y = ev.getY();
		View v = mAdapter.getLayout();
		if (v == null) {
			return super.dispatchTouchEvent(ev);
		}
		float height = v.getMeasuredHeight();
		if (v.getParent() == null && y < height) {
			mAdapter.setTouch();
			v.dispatchTouchEvent(ev);
			return true;
		}
		mAdapter.setClick();
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onResume() {
		mAdapter.notifyDataSetChanged();
	}

}
