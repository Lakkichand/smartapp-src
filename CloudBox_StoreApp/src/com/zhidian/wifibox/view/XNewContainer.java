package com.zhidian.wifibox.view;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.slidingmenu.lib.app.SlidingActivity;
import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.ta.util.http.AsyncHttpClient;
import com.ta.util.http.AsyncHttpResponseHandler;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.activity.MainActivity;
import com.zhidian.wifibox.adapter.XNewAdapter;
import com.zhidian.wifibox.controller.ModeManager;
import com.zhidian.wifibox.controller.XNewController;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataManager;
import com.zhidian.wifibox.data.XAppDataBean;
import com.zhidian.wifibox.data.XDataDownload;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.DownloadTaskRecorder;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.util.Setting;
import com.zhidian.wifibox.view.BgPageView.onCallBackOnClickListener;
import com.zhidian.wifibox.view.dialog.ConfirmDialog;
import com.zhidian.wifibox.view.dialog.LoadingDialog;

/**
 * 极速模式最新推荐页面
 */
public class XNewContainer extends LinearLayout implements IContainer,
		OnClickListener {

	private ListView listView;
	private XNewAdapter adapter;
	/**
	 * 提示页面
	 */
	private BgPageView mTipsView;
	/**
	 * 该页面的数据
	 */
	private PageDataBean mBean;
	/**
	 * 全选
	 */
	private Button btnCheckAll;
	/**
	 * 一键安装
	 */
	private Button btnYiInstall;
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
			mLoadingNexPage = false;
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
				adapter.update(mBean.mXAppList);
				// 去除进度条
				mTipsView.showContent();
				if (mBean.mXAppList.size() <= 0) {
					mTipsView.showNoContentTip();
				}
				calculateAppSize();
				// 判断是否已经显示过提示页了
				Setting setting = new Setting(getContext());
				if (!setting.getBoolean(Setting.HAS_SHOW_XMODETIP, false)) {
					MainActivity.sendHandler(null, IDiyFrameIds.MAINVIEWGROUP,
							IDiyMsgIds.SHOW_X_TIPS_PAGE, -1, null, null);
					// 保存已显示过提示页
					setting.putBoolean(Setting.HAS_SHOW_XMODETIP, true);
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
						String[] obj = { mBean.mUrl,
								XDataDownload.getXNewDataUrl() };
						TARequest request = new TARequest(
								XNewController.NEXT_PAGE, obj);
						TAApplication.getApplication()
								.doCommand(
										getContext().getString(
												R.string.xnewcontroller),
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
						String[] obj = { mBean.mUrl,
								XDataDownload.getXNewDataUrl() };
						TARequest request = new TARequest(
								XNewController.NEXT_PAGE, obj);
						TAApplication.getApplication()
								.doCommand(
										getContext().getString(
												R.string.xnewcontroller),
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

	public XNewContainer(Context context) {
		super(context);
	}

	public XNewContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		btnCheckAll = (Button) findViewById(R.id.xnew_all_btn);
		btnCheckAll.setOnClickListener(this);
		btnYiInstall = (Button) findViewById(R.id.xnew_yi_btn);
		btnYiInstall.setOnClickListener(this);
		listView = (ListView) findViewById(R.id.speed_new_gridview);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				XAppDataBean bean = (XAppDataBean) view.getTag(R.string.app);
				if (bean.isSelect) {
					bean.isSelect = false;
				} else {
					String packName = bean.packageName;
					String apkFileName = DownloadUtil
							.getXApkFileFromUrl(bean.downPath);
					boolean isInstall = InstallingValidator.getInstance()
							.isAppExist(TAApplication.getApplication(),
									packName);
					if (isInstall) {
						bean.isSelect = false;
						// 打开应用
						try {
							PackageManager packageManager = TAApplication
									.getApplication().getPackageManager();
							Intent intent = packageManager
									.getLaunchIntentForPackage(packName);
							TAApplication.getApplication()
									.startActivity(intent);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (FileUtil.isFileExist(apkFileName)) {
						bean.isSelect = false;
						// 安装应用
						try {
							File file = new File(apkFileName);
							Intent intent = new Intent();
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							intent.setAction(android.content.Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.fromFile(file),
									"application/vnd.android.package-archive");
							TAApplication.getApplication()
									.startActivity(intent);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (bean.downloadStatus == DownloadTask.DOWNLOADING) {
						bean.isSelect = false;
						// 暂停下载
						bean.downloadStatus = DownloadTask.PAUSING;
						Intent intent = new Intent(
								IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
						intent.putExtra("command",
								IDownloadInterface.REQUEST_COMMAND_PAUSE);
						intent.putExtra("url", bean.downPath);
						TAApplication.getApplication().sendBroadcast(intent);
					} else {
						bean.isSelect = true;
					}
				}
				adapter.notifyDataSetChanged();
				calculateAppSize();
			}
		});
		adapter = new XNewAdapter(getContext());
		listView.setAdapter(adapter);
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
			adapter.update(mBean.mXAppList);
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
			adapter.update(mBean.mXAppList);
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
			String[] obj = { bean.mUrl, XDataDownload.getXNewDataUrl() };
			TARequest request = new TARequest(XNewController.NEXT_PAGE, obj);
			if (!mLoadingNexPage) {
				TAApplication.getApplication().doCommand(
						getContext().getString(R.string.xnewcontroller),
						request, mRListener, true, false);
				mLoadingNexPage = true;
			}
			// 展示loading页
			mTipsView.showProgress();
		} else {
			// 展示数据
			// 这里应该要用下载任务列表初始化一下应用数据列表
			updateDownloadState(bean.mXAppList);
			for (XAppDataBean xbean : bean.mXAppList) {
				String packName = xbean.packageName;
				String apkFileName = DownloadUtil
						.getXApkFileFromUrl(xbean.downPath);
				boolean isInstall = InstallingValidator.getInstance()
						.isAppExist(TAApplication.getApplication(), packName);
				if (isInstall || FileUtil.isFileExist(apkFileName)
						|| xbean.downloadStatus == DownloadTask.DOWNLOADING) {
					if (xbean.isSelect) {
						xbean.isSelect = false;
					}
				}
			}
			adapter.update(bean.mXAppList);
			if (bean.mXAppList.size() <= 0) {
				mTipsView.showNoContentTip();
			} else {
				mTipsView.showContent();
			}
			calculateAppSize();
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.xnew_yi_btn:
			if (mBean != null && mBean.mXAppList != null) {
				boolean select = false;
				List<XAppDataBean> list = mBean.mXAppList;
				for (XAppDataBean bean : list) {
					if (bean.isSelect) {
						select = true;
						break;
					}
				}
				if (select) {
					for (XAppDataBean bean : list) {
						String packName = bean.packageName;
						String apkFileName = DownloadUtil
								.getXApkFileFromUrl(bean.downPath);
						boolean isInstall = InstallingValidator.getInstance()
								.isAppExist(TAApplication.getApplication(),
										packName);
						if (!bean.isSelect
								|| isInstall
								|| FileUtil.isFileExist(apkFileName)
								|| bean.downloadStatus == DownloadTask.DOWNLOADING) {
							// do nothing
						} else if (bean.downloadStatus == DownloadTask.PAUSING) {
							bean.downloadStatus = DownloadTask.DOWNLOADING;
							Intent intent = new Intent(
									IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
							intent.putExtra("command",
									IDownloadInterface.REQUEST_COMMAND_CONTINUE);
							intent.putExtra("url", bean.downPath);
							TAApplication.getApplication()
									.sendBroadcast(intent);
							bean.isSelect = false;
						} else {
							bean.downloadStatus = DownloadTask.DOWNLOADING;
							Intent intent = new Intent(
									IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
							intent.putExtra("command",
									IDownloadInterface.REQUEST_COMMAND_ADD);
							intent.putExtra("url", bean.downPath);
							intent.putExtra("iconUrl", bean.iconPath);
							intent.putExtra("name", bean.name);
							intent.putExtra("size", bean.size);
							intent.putExtra("packName", bean.packageName);
							intent.putExtra("appId", bean.id + 0l);
							intent.putExtra("version", bean.version);
							TAApplication.getApplication()
									.sendBroadcast(intent);
							bean.isSelect = false;
						}
					}
					adapter.notifyDataSetChanged();
					calculateAppSize();
				} else {
					// 如果没有选中的选项，则提示用户选选择
					Toast.makeText(getContext(), R.string.selecttip,
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		case R.id.xnew_all_btn:
			if (mBean != null && mBean.mXAppList != null) {
				boolean allselect = true;
				List<XAppDataBean> list = mBean.mXAppList;
				for (XAppDataBean bean : list) {
					String packName = bean.packageName;
					String apkFileName = DownloadUtil
							.getXApkFileFromUrl(bean.downPath);
					boolean isInstall = InstallingValidator.getInstance()
							.isAppExist(TAApplication.getApplication(),
									packName);
					if (isInstall || FileUtil.isFileExist(apkFileName)
							|| bean.downloadStatus == DownloadTask.DOWNLOADING) {
					} else {
						if (!bean.isSelect) {
							allselect = false;
							break;
						}
					}
				}
				if (allselect) {
					for (XAppDataBean bean : list) {
						bean.isSelect = false;
					}
				} else {
					for (XAppDataBean bean : list) {
						String packName = bean.packageName;
						String apkFileName = DownloadUtil
								.getXApkFileFromUrl(bean.downPath);
						boolean isInstall = InstallingValidator.getInstance()
								.isAppExist(TAApplication.getApplication(),
										packName);
						if (isInstall
								|| FileUtil.isFileExist(apkFileName)
								|| bean.downloadStatus == DownloadTask.DOWNLOADING) {
							bean.isSelect = false;
						} else {
							bean.isSelect = true;
						}
					}
				}
				adapter.update(mBean.mXAppList);
				calculateAppSize();
			}
			break;
		}
	}

	/**
	 * 根据已选的应用计算下载大小和下载时间
	 */
	private void calculateAppSize() {
		if (mBean != null && mBean.mXAppList != null) {
			// 个数
			int count = 0;
			// 大小
			int size = 0;
			// 时间
			int time = 0;
			List<XAppDataBean> list = mBean.mXAppList;
			for (XAppDataBean bean : list) {
				if (bean.isSelect) {
					count++;
					size += bean.size;
					time += (bean.size / DownloadUtil.sXDownloadSpeed);
				}
			}
			TextView countT = (TextView) findViewById(R.id.xnew_t);
			countT.setText(count + "");

			TextView sizeAndCount = (TextView) findViewById(R.id.sizeandtime);
			String one = getContext().getString(R.string.speed_text_yigong);
			String two = FileUtil.convertFileSize(size);
			String three = getContext().getString(R.string.speed_text_yi_time);
			String four = FileUtil.convertTime(time);
			SpannableString multiWord = new SpannableString(one + two + three
					+ four);
			multiWord.setSpan(new ForegroundColorSpan(0xFF35AC1F),
					one.length(), (one + two).length(),
					Spanned.SPAN_INCLUSIVE_INCLUSIVE);
			multiWord.setSpan(new ForegroundColorSpan(0xFF35AC1F),
					(one + two + three).length(),
					(one + two + three + four).length(),
					Spanned.SPAN_INCLUSIVE_INCLUSIVE);
			sizeAndCount.setText(multiWord);
		}
	}

	@Override
	public void onResume() {
		adapter.notifyDataSetChanged();
	}
}
