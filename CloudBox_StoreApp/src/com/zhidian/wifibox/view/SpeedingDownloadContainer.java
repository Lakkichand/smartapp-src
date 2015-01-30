package com.zhidian.wifibox.view;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.SpeedingHotAdapter;
import com.zhidian.wifibox.adapter.SpeedingMandatoryAdapter;
import com.zhidian.wifibox.adapter.SpeedingMandatoryAdapter.SpeedingMandatoryDataBean;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.controller.SpeedingDownloadController;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.data.XAppDataBean;
import com.zhidian.wifibox.data.XDataDownload;
import com.zhidian.wifibox.data.XMustDataBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.DownloadTaskRecorder;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.view.BgPageView.onCallBackOnClickListener;

/**
 * 超速下载页面，有两个界面
 * 
 * @author xiedezhi
 * 
 */
public class SpeedingDownloadContainer extends LinearLayout implements
		IContainer {

	private Button mMandatory;
	private Button mHot;

	private FrameLayout mMandatoryFrame;
	private ListView mMandatoryList;
	private LinearLayout mMandatoryTipsFrame;
	private BgPageView mMandatoryTipsView;

	private FrameLayout mHotFrame;
	private ListView mHotList;
	private LinearLayout mHotTipsFrame;
	private BgPageView mHotTipsView;

	private SpeedingMandatoryAdapter mMandatoryAdapter;
	private SpeedingHotAdapter mHotAdapter;

	private PageDataBean mBean;

	private LinearLayout mContent;
	private LinearLayout mNonet;

	/**
	 * 是否正在加载装机必备
	 */
	private volatile boolean mLoadingMandatory = false;
	/**
	 * 是否正在加载热门推荐
	 */
	private volatile boolean mLoadingHot = false;

	private TAIResponseListener mTransformationListener = new TAIResponseListener() {

		@Override
		public void onSuccess(TAResponse response) {
			List<SpeedingMandatoryDataBean> ret = (List<SpeedingMandatoryDataBean>) response
					.getData();
			mMandatoryAdapter.update(ret);
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

	private TAIResponseListener mHotListener = new TAIResponseListener() {

		@Override
		public void onSuccess(TAResponse response) {
			mLoadingHot = false;
			// 这是下一页的数据
			PageDataBean bean = (PageDataBean) response.getData();
			// 如果不是当前列表的下一页数据，抛弃
			if (bean.mStatuscode == 0) {
				// 加载成功，展示列表
				// 先缓存列表
				// 这里应该要用下载任务列表初始化一下刚刚拿到的数据
				updateDownloadState(bean.mXAppList);
				TabDataManager.getInstance().cachePageData(bean);
				// 更新列表
				mHotAdapter.update(mBean.mXAppList);
				// 去除进度条
				mHotTipsView.showContent();
				if (mBean.mXAppList.size() <= 0) {
					mHotTipsView.showNoContentTip();
				}
			} else if (bean.mStatuscode == 9) {
				// 提示数据正在更新
				mHotTipsView.showUpdating(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 如果正在加载下一页，则不需要再加载
						if (mLoadingHot) {
							return;
						}
						String[] obj = { mBean.mUrl,
								XDataDownload.getXNewDataUrl() };
						TARequest request = new TARequest(
								SpeedingDownloadController.LOAD_HOT, obj);
						TAApplication.getApplication().doCommand(
								getContext().getString(
										R.string.speedingdownloadcontroller),
								request, mHotListener, true, false);
						// 展示loading页
						mHotTipsView.showProgress();
						mLoadingHot = true;
					}
				});
			} else {
				// 加载失败
				// 展示错误提示页
				mHotTipsView.showLoadException(new onCallBackOnClickListener() {

					@Override
					public void onClick() {
						// 如果正在加载下一页，则不需要再加载
						if (mLoadingHot) {
							return;
						}
						String[] obj = { mBean.mUrl,
								XDataDownload.getXNewDataUrl() };
						TARequest request = new TARequest(
								SpeedingDownloadController.LOAD_HOT, obj);
						TAApplication.getApplication().doCommand(
								getContext().getString(
										R.string.speedingdownloadcontroller),
								request, mHotListener, true, false);
						// 展示loading页
						mHotTipsView.showProgress();
						mLoadingHot = true;
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

	private TAIResponseListener mMandatoryListener = new TAIResponseListener() {

		@Override
		public void onSuccess(TAResponse response) {
			mLoadingMandatory = false;
			// 这是下一页的数据
			PageDataBean bean = (PageDataBean) response.getData();
			// 如果不是当前列表的下一页数据，抛弃
			if (bean.mStatuscode == 0) {
				// 加载成功，展示列表
				// 先缓存列表
				// 这里应该要用下载任务列表初始化一下刚刚拿到的数据
				for (XMustDataBean mustBean : bean.mXMustList) {
					if (mustBean != null && mustBean.mAppList != null) {
						updateDownloadState(mustBean.mAppList);
					}
				}
				TabDataManager.getInstance().cachePageData(bean);
				// 展示列表
				TARequest request = new TARequest(
						SpeedingDownloadController.TRANSFORMATION,
						mBean.mXMustList);
				TAApplication.getApplication().doCommand(
						getContext().getString(
								R.string.speedingdownloadcontroller), request,
						mTransformationListener, true, false);
				// 去除进度条
				mMandatoryTipsView.showContent();
				if (mBean.mXMustList.size() <= 0) {
					mMandatoryTipsView.showNoContentTip();
				}
			} else if (bean.mStatuscode == 9) {
				// 提示数据正在更新
				mMandatoryTipsView.showUpdating(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 如果正在加载下一页，则不需要再加载
						if (mLoadingMandatory) {
							return;
						}
						String[] obj = { mBean.mUrl,
								XDataDownload.getXMustDataUrl() };
						TARequest request = new TARequest(
								SpeedingDownloadController.LOAD_MANDATORY, obj);
						TAApplication.getApplication().doCommand(
								getContext().getString(
										R.string.speedingdownloadcontroller),
								request, mMandatoryListener, true, false);
						// 展示loading页
						mMandatoryTipsView.showProgress();
						mLoadingMandatory = true;
					}
				});
			} else {
				// 加载失败
				// 展示错误提示页
				mMandatoryTipsView
						.showLoadException(new onCallBackOnClickListener() {

							@Override
							public void onClick() {
								// 如果正在加载下一页，则不需要再加载
								if (mLoadingMandatory) {
									return;
								}
								String[] obj = { mBean.mUrl,
										XDataDownload.getXMustDataUrl() };
								TARequest request = new TARequest(
										SpeedingDownloadController.LOAD_MANDATORY,
										obj);
								TAApplication
										.getApplication()
										.doCommand(
												getContext()
														.getString(
																R.string.speedingdownloadcontroller),
												request, mMandatoryListener,
												true, false);
								// 展示loading页
								mMandatoryTipsView.showProgress();
								mLoadingMandatory = true;
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

	public SpeedingDownloadContainer(Context context) {
		super(context);
	}

	public SpeedingDownloadContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mMandatory = (Button) findViewById(R.id.mandatory);
		mMandatory.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mMandatoryFrame.setVisibility(View.VISIBLE);
				mHotFrame.setVisibility(View.GONE);
				mBean.mIndex = 0;
				mMandatory.setTextColor(0xFFFF7000);
				mMandatory.setBackgroundColor(0xFFfff5de);
				mHot.setBackgroundColor(0xFFf8f8f8);
				mHot.setTextColor(0xFF333333);
			}
		});
		mHot = (Button) findViewById(R.id.hot);
		mHot.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mMandatoryFrame.setVisibility(View.GONE);
				mHotFrame.setVisibility(View.VISIBLE);
				mBean.mIndex = 1;
				mMandatory.setTextColor(0xFF333333);
				mMandatory.setBackgroundColor(0xFFf8f8f8);
				mHot.setBackgroundColor(0xFFfff5de);
				mHot.setTextColor(0xFFFF7000);
			}
		});
		mMandatoryFrame = (FrameLayout) findViewById(R.id.mandatory_frame);
		mMandatoryList = (ListView) findViewById(R.id.mandatory_listview);
		mMandatoryTipsFrame = (LinearLayout) findViewById(R.id.mandatory_tipsview);
		mMandatoryTipsView = new BgPageView(getContext(), mMandatoryTipsFrame,
				mMandatoryList);
		mHotFrame = (FrameLayout) findViewById(R.id.hot_frame);
		mHotList = (ListView) findViewById(R.id.hot_listview);
		mHotTipsFrame = (LinearLayout) findViewById(R.id.hot_tipsview);
		mHotTipsView = new BgPageView(getContext(), mHotTipsFrame, mHotList);
		mMandatoryAdapter = new SpeedingMandatoryAdapter(getContext());
		mMandatoryList.setAdapter(mMandatoryAdapter);
		mHotAdapter = new SpeedingHotAdapter(getContext());
		mHotList.setAdapter(mHotAdapter);

		mContent = (LinearLayout) findViewById(R.id.content);
		mNonet = (LinearLayout) findViewById(R.id.nonet);
		findViewById(R.id.jump).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 跳转到首页推荐
				TAApplication.sendHandler(null, IDiyFrameIds.ACTIONBAR,
						IDiyMsgIds.JUMP_TITLE, 0, null, null);
			}
		});
		findViewById(R.id.net).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 设置网络
				getContext().startActivity(
						new Intent(Settings.ACTION_WIFI_SETTINGS));
			}
		});
	}

	@Override
	public void onAppAction(String packName) {
		// 更改列表应用安装状态
		boolean needToUpdate = false;
		if (mBean != null && mBean.mXAppList != null) {
			for (XAppDataBean bean : mBean.mXAppList) {
				if (bean.packageName.equals(packName)) {
					needToUpdate = true;
					break;
				}
			}
		}
		if (needToUpdate) {
			updateDownloadState(mBean.mXAppList);
			mHotAdapter.update(mBean.mXAppList);
		}
		needToUpdate = false;
		if (mBean != null) {
			for (XMustDataBean mbean : mBean.mXMustList) {
				List<XAppDataBean> list = mbean.mAppList;
				for (XAppDataBean bean : list) {
					if (bean.packageName.equals(packName)) {
						needToUpdate = true;
						break;
					}
				}
				if (needToUpdate) {
					break;
				}
			}
		}
		if (needToUpdate) {
			for (XMustDataBean mustBean : mBean.mXMustList) {
				if (mustBean != null && mustBean.mAppList != null) {
					updateDownloadState(mustBean.mAppList);
				}
			}
			// 展示列表
			TARequest request = new TARequest(
					SpeedingDownloadController.TRANSFORMATION, mBean.mXMustList);
			TAApplication.getApplication()
					.doCommand(
							getContext().getString(
									R.string.speedingdownloadcontroller),
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
		if (mBean != null && mBean.mXAppList != null) {
			for (XAppDataBean bean : mBean.mXAppList) {
				if (bean.downPath.equals(downloadTask.url)) {
					needToUpdate = true;
					bean.downloadStatus = downloadTask.state;
					bean.alreadyDownloadPercent = downloadTask.alreadyDownloadPercent;
					bean.speed = downloadTask.speed;
				}
			}
		}
		if (needToUpdate) {
			mHotAdapter.update(mBean.mXAppList);
		}
		needToUpdate = false;
		if (mBean != null) {
			for (XMustDataBean mbean : mBean.mXMustList) {
				List<XAppDataBean> list = mbean.mAppList;
				for (XAppDataBean bean : list) {
					if (bean.downPath.equals(downloadTask.url)) {
						needToUpdate = true;
						bean.downloadStatus = downloadTask.state;
						bean.alreadyDownloadPercent = downloadTask.alreadyDownloadPercent;
						bean.speed = downloadTask.speed;
					}
				}
			}
		}
		if (needToUpdate) {
			// 展示列表
			TARequest request = new TARequest(
					SpeedingDownloadController.TRANSFORMATION, mBean.mXMustList);
			TAApplication.getApplication()
					.doCommand(
							getContext().getString(
									R.string.speedingdownloadcontroller),
							request, mTransformationListener, true, false);
		}
	}

	@Override
	public void updateContent(PageDataBean bean) {
		if (bean == null) {
			return;
		}
		mBean = bean;
		if (!ModeManager.checkRapidly()) {
			// 提示需要连上盒子
			mNonet.setVisibility(View.VISIBLE);
			mContent.setVisibility(View.GONE);
			return;
		}
		mNonet.setVisibility(View.GONE);
		mContent.setVisibility(View.VISIBLE);
		if (bean.mIndex == 0) {
			mMandatoryFrame.setVisibility(View.VISIBLE);
			mHotFrame.setVisibility(View.GONE);
			mMandatory.setTextColor(0xFFFF7000);
			mMandatory.setBackgroundColor(0xFFfff5de);
			mHot.setBackgroundColor(0xFFf8f8f8);
			mHot.setTextColor(0xFF333333);
		} else {
			mMandatoryFrame.setVisibility(View.GONE);
			mHotFrame.setVisibility(View.VISIBLE);
			mMandatory.setTextColor(0xFF333333);
			mMandatory.setBackgroundColor(0xFFf8f8f8);
			mHot.setBackgroundColor(0xFFfff5de);
			mHot.setTextColor(0xFFFF7000);
		}
		if (bean.mXAppList != null && bean.mXAppList.size() > 0) {
			updateDownloadState(bean.mXAppList);
			// 展示列表
			mHotAdapter.update(bean.mXAppList);
		} else {
			// 加载热门推荐
			// idurl,dataurl
			String[] obj = { bean.mUrl, XDataDownload.getXNewDataUrl() };
			TARequest request = new TARequest(
					SpeedingDownloadController.LOAD_HOT, obj);
			if (!mLoadingHot) {
				TAApplication.getApplication().doCommand(
						getContext().getString(
								R.string.speedingdownloadcontroller), request,
						mHotListener, true, false);
				mLoadingHot = true;
			}
			// 展示loading页
			mHotTipsView.showProgress();
		}
		if (bean.mXMustList != null && bean.mXMustList.size() > 0) {
			for (XMustDataBean mustBean : bean.mXMustList) {
				if (mustBean != null && mustBean.mAppList != null) {
					updateDownloadState(mustBean.mAppList);
				}
			}
			// 展示列表
			TARequest request = new TARequest(
					SpeedingDownloadController.TRANSFORMATION, bean.mXMustList);
			TAApplication.getApplication()
					.doCommand(
							getContext().getString(
									R.string.speedingdownloadcontroller),
							request, mTransformationListener, true, false);
		} else {
			// 加载装机必备
			// idurl,dataurl
			String[] obj = { bean.mUrl, XDataDownload.getXMustDataUrl() };
			TARequest request = new TARequest(
					SpeedingDownloadController.LOAD_MANDATORY, obj);
			if (!mLoadingMandatory) {
				TAApplication.getApplication().doCommand(
						getContext().getString(
								R.string.speedingdownloadcontroller), request,
						mMandatoryListener, true, false);
				mLoadingMandatory = true;
			}
			// 展示loading页
			mMandatoryTipsView.showProgress();
		}
	}

	/**
	 * 用下载任务列表更新应用列表的下载状态
	 */
	private void updateDownloadState(List<XAppDataBean> list) {
		if (list == null || list.size() <= 0) {
			return;
		}
		Map<String, DownloadTask> map = DownloadTaskRecorder.getInstance()
				.getDownloadTaskList();
		for (XAppDataBean bean : list) {
			if (map.containsKey(bean.downPath)) {
				DownloadTask task = map.get(bean.downPath);
				bean.downloadStatus = task.state;
				bean.alreadyDownloadPercent = task.alreadyDownloadPercent;
				bean.speed = task.speed;
			} else {
				bean.downloadStatus = DownloadTask.NOT_START;
				bean.alreadyDownloadPercent = 0;
			}
		}
	}

	@Override
	public void onResume() {
		mMandatoryAdapter.notifyDataSetChanged();
		mHotAdapter.notifyDataSetChanged();
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
