package com.zhidian.wifibox.view;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slidingmenu.lib.app.SlidingActivity;
import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.ta.util.http.AsyncHttpClient;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.adapter.XAllAdapter;
import com.zhidian.wifibox.controller.CategoriesController;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.controller.XAllController;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.data.XAllDataBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.DownloadTaskRecorder;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.view.BgPageView.onCallBackOnClickListener;
import com.zhidian.wifibox.view.dialog.ConfirmDialog;
import com.zhidian.wifibox.view.dialog.LoadingDialog;
import com.zhidian.wifibox.view.dialog.XAllDialog;

/**
 * 极速模式全部应用页面
 */
public class XAllContainer extends LinearLayout implements IContainer {

	private GridView gridView;
	private XAllAdapter adapter;
	/**
	 * 提示页面
	 */
	private BgPageView mTipsView;
	/**
	 * 该页面的数据
	 */
	private PageDataBean mBean;
	/**
	 * 是否正在加载下一页
	 */
	private volatile boolean mLoadingNexPage = false;
	/**
	 * 当前下载速度
	 */
	private TextView mSpeed;
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
				// 这里应该要用下载任务列表初始化一下刚刚拿到的数据
				updateDownloadState(bean.mXAllList);
				TabDataManager.getInstance().cachePageData(bean);
				// 更新列表
				adapter.update(mBean.mXAllList);
				// 去除进度条
				mTipsView.showContent();
				if (mBean.mXAllList.size() <= 0) {
					mTipsView.showNoContentTip();
				}
			} else if (bean.mStatuscode == 9) {
				// 提示数据正在更新
				mTipsView.showUpdating(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 如果正在加载下一页，则不需要再加载
						if (mLoadingNexPage) {
							return;
						}
						String[] obj = { mBean.mUrl, mBean.mUrl };
						TARequest request = new TARequest(
								XAllController.NEXT_PAGE, obj);
						TAApplication.getApplication()
								.doCommand(
										getContext().getString(
												R.string.xallcontroller),
										request, mRListener, true, false);
						// 展示loading页
						mTipsView.showProgress();
						mLoadingNexPage = true;
					}
				});
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
								XAllController.NEXT_PAGE, obj);
						TAApplication.getApplication()
								.doCommand(
										getContext().getString(
												R.string.xallcontroller),
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

	public XAllContainer(Context context) {
		super(context);
	}

	public XAllContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * 有正在下载的任务时显示总下载速度，其他不显示
	 */
	private void updateSpeed() {
		boolean show = false;
		long speed = 0;
		if (mBean != null && mBean.mXAllList != null) {
			for (XAllDataBean bean : mBean.mXAllList) {
				if (bean.downloadStatus == DownloadTask.DOWNLOADING) {
					show = true;
					speed += bean.speed;
				}
			}
		}
		if (show) {
			mSpeed.setVisibility(View.VISIBLE);
			mSpeed.setText(speed + "KB/S");
		} else {
			mSpeed.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	protected void onFinishInflate() {
		gridView = (GridView) findViewById(R.id.speed_all_gridview);
		mSpeed = (TextView) findViewById(R.id.speed);
		adapter = new XAllAdapter(getContext());
		gridView.setAdapter(adapter);
		LinearLayout tips = (LinearLayout) findViewById(R.id.tipsview);
		LinearLayout content = (LinearLayout) findViewById(R.id.content);
		mTipsView = new BgPageView(getContext(), tips, content);
		findViewById(R.id.speed_rbtn_sp).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 检查是否能连接到外网
						final LoadingDialog dialog = new LoadingDialog(
								getContext(), "正在切换到普通模式");
						dialog.setCancelable(false);
						dialog.show();
						AsyncHttpClient client = new AsyncHttpClient();
						client.setTimeout(5000);
						client.get(CDataDownloader.getExtranetUrl(),
								new AsyncHttpResponseHandler() {
									@Override
									public void onSuccess(String content) {
										try {
											JSONObject json = new JSONObject(
													content);
											// 返回时json格式就代表能连接外网
											dialog.dismiss();
											ModeManager
													.getInstance()
													.setRapidly(
															!ModeManager
																	.getInstance()
																	.isRapidly());
											MainActivity.sendHandler(null,
													IDiyFrameIds.MAINVIEWGROUP,
													IDiyMsgIds.SWITCH_MODE, -1,
													null, null);
											return;
										} catch (JSONException e) {
											e.printStackTrace();
										}
										dialog.dismiss();
										// 提示
										ConfirmDialog cDialog = new ConfirmDialog(
												getContext(), "网络提示",
												"请确认米宝盒子能连接外网，再尝试切换网络模式");
										cDialog.show();
									}

									@Override
									public void onStart() {
									}

									@Override
									public void onFailure(Throwable error) {
										dialog.dismiss();
										// 提示
										ConfirmDialog cDialog = new ConfirmDialog(
												getContext(), "网络提示",
												"请确认米宝盒子能连接外网，再尝试切换网络模式");
										cDialog.show();
									}

									@Override
									public void onFinish() {
									}

								});
					}
				});
		findViewById(R.id.mapSearchBtn).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						((SlidingActivity) getContext()).getSlidingMenu()
								.toggle();
					}
				});
		findViewById(R.id.question).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 弹框
				XAllDialog dialog = new XAllDialog(getContext());
				dialog.show();
			}
		});
	}

	@Override
	public void onAppAction(String packName) {
		// 更改列表应用安装状态
		boolean needToUpdate = false;
		if (mBean != null && mBean.mXAllList != null) {
			for (XAllDataBean bean : mBean.mXAllList) {
				if (bean.packName.equals(packName)) {
					needToUpdate = true;
					break;
				}
			}
		}
		if (needToUpdate) {
			updateDownloadState(mBean.mXAllList);
			adapter.update(mBean.mXAllList);
		}
	}

	@Override
	public String getDataUrl() {
		return mBean.mUrl;
	}

	@Override
	public void notifyDownloadState(DownloadTask downloadTask) {
		boolean needToUpdate = false;
		if (mBean != null && mBean.mXAllList != null) {
			for (XAllDataBean bean : mBean.mXAllList) {
				if (bean.downPath.equals(downloadTask.url)) {
					needToUpdate = true;
					bean.downloadStatus = downloadTask.state;
					bean.alreadyDownloadPercent = downloadTask.alreadyDownloadPercent;
					bean.speed = downloadTask.speed;
				}
			}
		}
		if (needToUpdate) {
			adapter.update(mBean.mXAllList);
		}
		updateSpeed();
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
			TARequest request = new TARequest(XAllController.NEXT_PAGE, obj);
			if (!mLoadingNexPage) {
				TAApplication.getApplication().doCommand(
						getContext().getString(R.string.xallcontroller),
						request, mRListener, true, false);
				mLoadingNexPage = true;
			}
			// 展示loading页
			mTipsView.showProgress();
		} else {
			// 展示数据
			// 这里应该要用下载任务列表初始化一下应用数据列表
			updateDownloadState(bean.mXAllList);
			adapter.update(bean.mXAllList);
			if (bean.mXAllList.size() <= 0) {
				mTipsView.showNoContentTip();
			} else {
				mTipsView.showContent();
			}
		}
	}

	/**
	 * 用下载任务列表更新应用列表的下载状态
	 */
	private void updateDownloadState(List<XAllDataBean> list) {
		if (list == null || list.size() <= 0) {
			return;
		}
		Map<String, DownloadTask> map = DownloadTaskRecorder.getInstance()
				.getDownloadTaskList();
		for (XAllDataBean bean : list) {
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
		updateSpeed();
	}

	@Override
	public void onResume() {
		adapter.notifyDataSetChanged();
	}

}
