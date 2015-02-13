package com.zhidian.wifibox.view;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.HomeFeatureadapter;
import com.zhidian.wifibox.adapter.HomeFeatureadapter.TransformationDataBean;
import com.zhidian.wifibox.controller.HomeFeatureController;
import com.zhidian.wifibox.data.AppDataBean;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.DownloadTaskRecorder;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.view.BgPageView.onCallBackOnClickListener;

/**
 * 首页推荐
 * 
 * @author xiedezhi
 * 
 */
public class HomeFeatureContainer extends LinearLayout implements IContainer {

	private PageDataBean mBean;

	private ListView mListView;
	private HomeFeatureadapter mAdapter;
	/**
	 * 提示页
	 */
	private BgPageView mTipsView;
	/**
	 * 幻灯片组件
	 */
	private BannerView mBannerView;
	/**
	 * 顶部导航栏，跳转到专题
	 */
	private HomeFeatureNavigation mNavigation;
	/**
	 * 是否正在加载下一页
	 */
	private volatile boolean mLoadingNexPage = false;

	/**
	 * 转化数据bean
	 */
	private TAIResponseListener mTransformationListener = new TAIResponseListener() {

		@Override
		public void onSuccess(TAResponse response) {
			List<TransformationDataBean> ret = (List<TransformationDataBean>) response
					.getData();
			mAdapter.update(ret, mBean == null ? "" : mBean.mStatisticsTitle);
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
				// 展示banner
				mBannerView.init(bean.mHomeFeatureDataBean.mBannerList);
				// 展示导航栏
				mNavigation.init(bean.mHomeFeatureDataBean.mTagList,
						bean.mHomeFeatureDataBean.mIdList,
						bean.mHomeFeatureDataBean.mBackGroundList,
						bean.mHomeFeatureDataBean.mTypeList,
						bean.mHomeFeatureDataBean.mTitleList);
				// 这里应该要用下载任务列表初始化一下刚刚拿到的数据
				updateDownloadState(bean.mHomeFeatureDataBean.mAppList);
				updateDownloadState(bean.mHomeFeatureDataBean.mGameList);
				TabDataManager.getInstance().cachePageData(bean);
				// 更新列表
				TARequest request = new TARequest(
						HomeFeatureController.TRANSFORMATION,
						mBean.mHomeFeatureDataBean);
				TAApplication.getApplication().doCommand(
						getContext().getString(R.string.homefeaturecontroller),
						request, mTransformationListener, true, false);
				mTipsView.showContent();
				if (bean.mHomeFeatureDataBean.mAppList.size() <= 0
						&& bean.mHomeFeatureDataBean.mGameList.size() <= 0) {
					mTipsView.showNoContentTip();
				}
			} else {
				// 加载失败
				mTipsView.showLoadException(new onCallBackOnClickListener() {

					@Override
					public void onClick() {
						// 如果正在加载下一页，则不需要再加载
						if (mLoadingNexPage) {
							return;
						}
						// 数据还没初始化
						// idurl,dataurl,要加载的页码,是否先加载本地数据
						String[] obj = { mBean.mUrl, mBean.mUrl,
								mBean.mLoadLocalDataFirst + "" };
						TARequest request = new TARequest(
								HomeFeatureController.NEXT_PAGE, obj);
						TAApplication.getApplication().doCommand(
								getContext().getString(
										R.string.homefeaturecontroller),
								request, mRListener, true, false);
						// 展示loading页
						mTipsView.showProgress();
						mLoadingNexPage = true;
					}
				});
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

	public HomeFeatureContainer(Context context) {
		super(context);
	}

	public HomeFeatureContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		mListView = (ListView) findViewById(R.id.listview);
		mTipsView = new BgPageView(getContext(),
				(LinearLayout) findViewById(R.id.tipsview), mListView);
		// bannerview
		LayoutInflater inflater = LayoutInflater.from(getContext());
		mBannerView = (BannerView) inflater.inflate(R.layout.view_adv_roll,
				null);
		int bannerHeight = (int) ((new InfoUtil(getContext()).getWidth()) / 480.0 * 170.0 + 0.5)
				+ DrawUtil.dip2px(getContext(), 4.5f);
		mBannerView.setLayoutParams(new AbsListView.LayoutParams(
				AbsListView.LayoutParams.MATCH_PARENT, bannerHeight));
		mListView.addHeaderView(mBannerView);
		// 导航栏
		mNavigation = new HomeFeatureNavigation(getContext());
		mNavigation.setLayoutParams(new AbsListView.LayoutParams(
				AbsListView.LayoutParams.MATCH_PARENT,
				AbsListView.LayoutParams.WRAP_CONTENT));
		mListView.addHeaderView(mNavigation);
		mAdapter = new HomeFeatureadapter(getContext());
		mListView.setAdapter(mAdapter);
	}

	@Override
	public void onAppAction(String packName) {
		// 更改列表应用安装状态
		boolean needToUpdate = false;
		if (mBean != null && mBean.mHomeFeatureDataBean != null) {
			for (AppDataBean bean : mBean.mHomeFeatureDataBean.mAppList) {
				if (bean.packName.equals(packName)) {
					needToUpdate = true;
					break;
				}
			}
			for (AppDataBean bean : mBean.mHomeFeatureDataBean.mGameList) {
				if (bean.packName.equals(packName)) {
					needToUpdate = true;
					break;
				}
			}
		}
		if (needToUpdate) {
			updateDownloadState(mBean.mHomeFeatureDataBean.mAppList);
			updateDownloadState(mBean.mHomeFeatureDataBean.mGameList);
			TARequest request = new TARequest(
					HomeFeatureController.TRANSFORMATION,
					mBean.mHomeFeatureDataBean);
			TAApplication.getApplication().doCommand(
					getContext().getString(R.string.homefeaturecontroller),
					request, mTransformationListener, true, false);
		}
	}

	@Override
	public String getDataUrl() {
		return mBean.mUrl;
	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {
		boolean needToUpdate = false;
		if (mBean != null && mBean.mHomeFeatureDataBean != null) {
			for (AppDataBean bean : mBean.mHomeFeatureDataBean.mAppList) {
				if (bean.downloadUrl.equals(downloadTask.url)) {
					needToUpdate = true;
					bean.downloadStatus = downloadTask.state;
					bean.alreadyDownloadPercent = downloadTask.alreadyDownloadPercent;
				}
			}
			for (AppDataBean bean : mBean.mHomeFeatureDataBean.mGameList) {
				if (bean.downloadUrl.equals(downloadTask.url)) {
					needToUpdate = true;
					bean.downloadStatus = downloadTask.state;
					bean.alreadyDownloadPercent = downloadTask.alreadyDownloadPercent;
				}
			}
		}
		if (needToUpdate) {
			TARequest request = new TARequest(
					HomeFeatureController.TRANSFORMATION,
					mBean.mHomeFeatureDataBean);
			TAApplication.getApplication().doCommand(
					getContext().getString(R.string.homefeaturecontroller),
					request, mTransformationListener, true, false);
		}
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
			// idurl,dataurl,是否先加载本地数据
			String[] obj = { bean.mUrl, bean.mUrl,
					bean.mLoadLocalDataFirst + "" };
			TARequest request = new TARequest(HomeFeatureController.NEXT_PAGE,
					obj);
			// 如果正在加载下一页，则不需要再加载
			if (!mLoadingNexPage) {
				TAApplication.getApplication().doCommand(
						getContext().getString(R.string.homefeaturecontroller),
						request, mRListener, true, false);
				mLoadingNexPage = true;
			}
			// 展示loading页
			mTipsView.showProgress();
			mLoadingNexPage = true;
		} else {
			// 展示banner
			mBannerView.init(bean.mHomeFeatureDataBean.mBannerList);
			// 展示导航栏
			mNavigation.init(bean.mHomeFeatureDataBean.mTagList,
					bean.mHomeFeatureDataBean.mIdList,
					bean.mHomeFeatureDataBean.mBackGroundList,
					bean.mHomeFeatureDataBean.mTypeList,
					bean.mHomeFeatureDataBean.mTitleList);
			// 展示数据
			// 这里应该要用下载任务列表初始化一下应用数据列表
			updateDownloadState(bean.mHomeFeatureDataBean.mAppList);
			updateDownloadState(bean.mHomeFeatureDataBean.mGameList);
			TARequest request = new TARequest(
					HomeFeatureController.TRANSFORMATION,
					mBean.mHomeFeatureDataBean);
			TAApplication.getApplication().doCommand(
					getContext().getString(R.string.homefeaturecontroller),
					request, mTransformationListener, true, false);
			if (bean.mHomeFeatureDataBean.mAppList.size() <= 0
					&& bean.mHomeFeatureDataBean.mGameList.size() <= 0) {
				mTipsView.showNoContentTip();
			} else {
				mTipsView.showContent();
			}
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

	/**
	 * 获取标签栏
	 */
	public HomeFeatureNavigation getNavigation() {
		return mNavigation;
	}

}
