package com.zhidian.wifibox.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
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
import com.zhidian.wifibox.adapter.TopicContentAdapter;
import com.zhidian.wifibox.adapter.TopicContentAdapter.allDownloadClickListener;
import com.zhidian.wifibox.controller.TopicContentController;
import com.zhidian.wifibox.data.AppDataBean;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataGroup;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.DownloadTaskRecorder;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.util.PathConstant;
import com.zhidian.wifibox.view.BgPageView.onCallBackOnClickListener;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager;
import com.zhidian3g.wifibox.imagemanager.AsyncImageManager.AsyncImageLoadedCallBack;

/**
 * 专题内容列表，有全部下载功能，数据由TopicContentController提供
 * 
 * @author xiedezhi
 * 
 */
public class TopicContentContainer extends FrameLayout implements IContainer {
	// TODO 加载出标题后显示标题
	private ListView mListView;
	/**
	 * 提示页面
	 */
	private BgPageView mTipsView;
	private ListViewSearchFooterView mFoot;
	private TopicContentAdapter mAdapter;
	private ImageView ivPhoto; // 图片
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
				if (!TextUtils.isEmpty(bean.titleMessage)) {
					mBean.mTitle = bean.titleMessage;
				}
				// 展示专题信息
				showTopicInfo(mBean);
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
				// 更改标题
				List<Object> objlist = new ArrayList<Object>();
				objlist.add(TopicContentContainer.this);
				TAApplication.sendHandler(null, IDiyFrameIds.TABMANAGEVIEW,
						IDiyMsgIds.CHANGE_TITLE, -1, mBean.mTitle, objlist);
				List<TabDataGroup> glist = TabDataManager.getInstance()
						.getAllTabDataGroup();
				for (TabDataGroup group : glist) {
					if (group.mPageList.size() == 1
							&& group.mPageList.get(0) == mBean) {
						group.title = mBean.mTitle;
					}
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
									String[] obj = { mBean.mUrl, mBean.mUrl,
											1 + "" };
									TARequest request = new TARequest(
											TopicContentController.NEXT_PAGE,
											obj);
									TAApplication
											.getApplication()
											.doCommand(
													getContext()
															.getString(
																	R.string.topiccontentcontroller),
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
							String[] obj = { mBean.mUrl, dataUrl, pageNo + "" };
							TARequest request = new TARequest(
									TopicContentController.NEXT_PAGE, obj);
							TAApplication.getApplication().doCommand(
									getContext().getString(
											R.string.topiccontentcontroller),
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
				String[] obj = { mBean.mUrl, dataUrl, pageNo + "" };
				TARequest request = new TARequest(
						TopicContentController.NEXT_PAGE, obj);
				TAApplication.getApplication()
						.doCommand(
								getContext().getString(
										R.string.topiccontentcontroller),
								request, mRListener, true, false);
				mLoadingNexPage = true;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}
	};

	public TopicContentContainer(Context context) {
		super(context);
	}

	public TopicContentContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TopicContentContainer(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		mListView = (ListView) findViewById(R.id.topicc_pinnedListView);
		LinearLayout tips = (LinearLayout) findViewById(R.id.tipsview);
		mTipsView = new BgPageView(getContext(), tips, mListView);

		LayoutInflater inflater = LayoutInflater.from(getContext());
		View headerView = (View) inflater.inflate(
				R.layout.view_topiccontent_header, null);
		ivPhoto = (ImageView) headerView.findViewById(R.id.logo_iv);
		int bannerHeight = (int) ((new InfoUtil(getContext()).getWidth()) / 480.0 * 139.0 + 0.5);
		ivPhoto.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, bannerHeight));
		mListView.addHeaderView(headerView);

		mAdapter = new TopicContentAdapter(getContext());
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

	/**
	 * 展示专题列表信息
	 */
	private void showTopicInfo(PageDataBean bean) {
		mAdapter.setPageDataBena(bean);
		mAdapter.setDownloadClick(new allDownloadClickListener() {

			@Override
			public void onClick() {
				setonclick();
			}
		});
		ivPhoto.setTag(bean.iconMessage);
		Bitmap bm = AsyncImageManager.getInstance().loadImage(
				PathConstant.ICON_ROOT_PATH, bean.iconMessage + "",
				bean.iconMessage, true, false, new AsyncImageLoadedCallBack() {

					@Override
					public void imageLoaded(Bitmap imageBitmap, String imgUrl) {
						if (imageBitmap == null) {
							return;
						}
						if (ivPhoto.getTag().equals(imgUrl)) {
							ivPhoto.setImageBitmap(imageBitmap);
						}
					}
				});
		if (bm != null) {
			ivPhoto.setImageBitmap(bm);
		} else {
			ivPhoto.setImageBitmap(DrawUtil.sTopicDefaultBannerBig);
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
			// idurl,dataurl,要加载的页码
			String[] obj = { bean.mUrl, bean.mUrl, 1 + "" };
			TARequest request = new TARequest(TopicContentController.NEXT_PAGE,
					obj);
			if (!mLoadingNexPage) {
				TAApplication.getApplication()
						.doCommand(
								getContext().getString(
										R.string.topiccontentcontroller),
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
		showTopicInfo(bean);
	}

	/**
	 * 下载全部app
	 */
	private void setonclick() {
		if (mBean.mAppList != null) {
			for (AppDataBean bean : mBean.mAppList) {
				boolean isInstall = InstallingValidator.getInstance()
						.isAppExist(getContext(), bean.packName);
				if (isInstall) {
					continue;
				}
				String apkFileName = DownloadUtil
						.getCApkFileFromUrl(bean.downloadUrl);
				if (FileUtil.isFileExist(apkFileName)) {
					continue;
				}
				if (bean.downloadStatus != DownloadTask.DOWNLOADING
						|| bean.downloadStatus != DownloadTask.WAITING) {
					bean.downloadStatus = DownloadTask.WAITING;
					Intent intent = new Intent(
							IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
					intent.putExtra("command",
							IDownloadInterface.REQUEST_COMMAND_ADD);
					intent.putExtra("url", bean.downloadUrl);
					intent.putExtra("iconUrl", bean.iconUrl);
					intent.putExtra("name", bean.name);
					intent.putExtra("size", bean.size);
					intent.putExtra("packName", bean.packName);
					intent.putExtra("appId", bean.id);
					intent.putExtra("version", bean.version);
					intent.putExtra("page", mBean.mStatisticsTitle);
					TAApplication.getApplication().sendBroadcast(intent);
				}
			}
			mAdapter.notifyDataSetChanged();
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
