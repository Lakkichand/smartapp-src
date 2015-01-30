package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.adapter.TopicAdapter;
import com.zhidian.wifibox.controller.TopicController;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.data.TopicDataBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.view.BgPageView.onCallBackOnClickListener;

/**
 * 专题列表页，数据由TopicController加载
 * 
 * 有分页
 * 
 * @author xiedezhi
 * 
 */
public class TopicContainer extends FrameLayout implements IContainer {

	private ListView mListView;
	/**
	 * 提示页面
	 */
	private BgPageView mTipsView;
	private ListViewSearchFooterView mFoot;
	private TopicAdapter mAdapter;
	private ImageView mGotoTop;
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
				TabDataManager.getInstance().cachePageData(bean);
				// 更新列表
				mAdapter.update(mBean.mTopicList);
				mTipsView.showContent();
				mGotoTop.setVisibility(View.VISIBLE);
				if (bean.mPageIndex >= bean.mTotalPage) {
					// 如果这是最后一页，改变footview
					mFoot.viewGone();
				}
				if (mBean.mTopicList.size() <= 0) {
					mTipsView.showNoContentTip();
					mGotoTop.setVisibility(View.GONE);
				}
			} else {
				// 加载失败
				if (bean.mPageIndex == 1) {
					// 如果是第一页，展示错误提示页
					mGotoTop.setVisibility(View.GONE);
					mTipsView
							.showLoadException(new onCallBackOnClickListener() {

								@Override
								public void onClick() {
									// 如果正在加载下一页，则不需要再加载
									if (mLoadingNexPage) {
										return;
									}
									String[] obj = { mBean.mUrl, mBean.mUrl,
											1 + "" };
									TARequest request = new TARequest(
											TopicController.NEXT_PAGE, obj);
									TAApplication.getApplication().doCommand(
											getContext().getString(
													R.string.topiccontroller),
											request, mRListener, true, false);
									// 展示loading页
									mGotoTop.setVisibility(View.GONE);
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
							String[] obj = { mBean.mUrl, dataUrl, pageNo + "" };
							TARequest request = new TARequest(
									TopicController.NEXT_PAGE, obj);
							TAApplication.getApplication().doCommand(
									getContext().getString(
											R.string.topiccontroller), request,
									mRListener, true, false);
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
				String[] obj = { mBean.mUrl, dataUrl, pageNo + "" };
				TARequest request = new TARequest(TopicController.NEXT_PAGE,
						obj);
				TAApplication.getApplication().doCommand(
						getContext().getString(R.string.topiccontroller),
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
	/**
	 * 列表点击监听
	 */
	private OnItemClickListener mItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// 跳转到专题内容
			TopicDataBean bean = (TopicDataBean) view.getTag(R.string.app_name);
			List<Object> list = new ArrayList<Object>();
			list.add(bean);
			// 通知TabManageView跳转下一层级，把TopicDataBean带过去
			TAApplication.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
					IDiyMsgIds.ENTER_NEXT_LEVEL, -1,
					CDataDownloader.getTopicContentUrl(bean.id, 1), list);
		}
	};

	public TopicContainer(Context context) {
		super(context);
	}

	public TopicContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TopicContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		mListView = (ListView) findViewById(R.id.listview);
		LinearLayout tips = (LinearLayout) findViewById(R.id.tipsview);
		mTipsView = new BgPageView(getContext(), tips, mListView);
		mAdapter = new TopicAdapter(getContext());
		mListView.setOnScrollListener(mScrollListener);
		mListView.setOnItemClickListener(mItemClickListener);
		// 添加footview
		mFoot = (ListViewSearchFooterView) LayoutInflater.from(getContext())
				.inflate(R.layout.view_listview_footer, null);
		mFoot.showLoading();
		mListView.addFooterView(mFoot);
		mListView.setAdapter(mAdapter);
		mGotoTop = (ImageView) findViewById(R.id.gototop);
		mGotoTop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mListView.setSelection(0);
			}
		});
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
			String[] obj = { bean.mUrl, bean.mUrl, 1 + "" };
			TARequest request = new TARequest(TopicController.NEXT_PAGE, obj);
			if (!mLoadingNexPage) {
				TAApplication.getApplication().doCommand(
						getContext().getString(R.string.topiccontroller),
						request, mRListener, true, false);
				mLoadingNexPage = true;
			}
			// 展示loading页
			mTipsView.showProgress();
			mGotoTop.setVisibility(View.GONE);
			mLoadingNexPage = true;
		} else {
			// 展示数据
			mAdapter.update(bean.mTopicList);
			if (bean.mPageIndex >= bean.mTotalPage) {
				// 如果这是最后一页，改变footview
				mFoot.viewGone();
			}
			if (bean.mTopicList.size() <= 0) {
				mTipsView.showNoContentTip();
				mGotoTop.setVisibility(View.GONE);
			} else {
				mTipsView.showContent();
				mGotoTop.setVisibility(View.VISIBLE);
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
