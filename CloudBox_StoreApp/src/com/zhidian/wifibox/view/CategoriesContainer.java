package com.zhidian.wifibox.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.CategoriesAdapter;
import com.zhidian.wifibox.controller.CategoriesController;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.view.BgPageView.onCallBackOnClickListener;

/**
 * 应用分类列表，数据由CategoriesController加载
 * 
 * 没有分页
 * 
 * @author xiedezhi
 * 
 */
public class CategoriesContainer extends FrameLayout implements IContainer {

	private GridView mGridView;
	/**
	 * 提示页面
	 */
	private BgPageView mTipsView;
	private CategoriesAdapter mAdapter;
	/**
	 * 该页面的数据
	 */
	private PageDataBean mBean;
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
			if (bean.mStatuscode == 0) {
				// 加载成功，展示列表
				// 先缓存列表
				TabDataManager.getInstance().cachePageData(bean);
				// 更新列表
				mAdapter.update(mBean.mCatList);
				// 去除进度条
				mTipsView.showContent();
				if (mBean.mCatList.size() <= 0) {
					mTipsView.showNoContentTip();
				}
			} else {
				// 加载失败
				// 展示错误提示页
				mTipsView.showLoadException(new onCallBackOnClickListener() {

					@Override
					public void onClick() {
						// 如果正在加载下一页，则不需要再加载
						if (mLoadingNexPage) {
							return;
						}
						String[] obj = { mBean.mUrl, mBean.mUrl };
						TARequest request = new TARequest(
								CategoriesController.NEXT_PAGE, obj);
						TAApplication.getApplication().doCommand(
								getContext().getString(
										R.string.categoriescontroller),
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

	public CategoriesContainer(Context context) {
		super(context);
	}

	public CategoriesContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CategoriesContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		mGridView = (GridView) findViewById(R.id.girdview);
		LinearLayout tips = (LinearLayout) findViewById(R.id.tipsview);
		mTipsView = new BgPageView(getContext(), tips, mGridView);
		mAdapter = new CategoriesAdapter(getContext());
		mGridView.setAdapter(mAdapter);
	}

	@Override
	public void onAppAction(String packName) {
		// do nothing
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
			String[] obj = { bean.mUrl, bean.mUrl };
			TARequest request = new TARequest(CategoriesController.NEXT_PAGE,
					obj);
			if (!mLoadingNexPage) {
				TAApplication.getApplication().doCommand(
						getContext().getString(R.string.categoriescontroller),
						request, mRListener, true, false);
				mLoadingNexPage = true;
			}
			// 展示loading页
			mTipsView.showProgress();
		} else {
			// 展示数据
			mAdapter.update(bean.mCatList);
			if (bean.mCatList.size() <= 0) {
				mTipsView.showNoContentTip();
			} else {
				mTipsView.showContent();
			}
		}
	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {
		// do nothing
	}

	@Override
	public void onResume() {
		// do nothing
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
