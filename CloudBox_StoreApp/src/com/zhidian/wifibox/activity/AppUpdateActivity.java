package com.zhidian.wifibox.activity;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.stat.StatService;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.UpdateAppAdapter;
import com.zhidian.wifibox.controller.TabController;
import com.zhidian.wifibox.controller.UpdateController;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.UpdateAppBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.DownloadTaskRecorder;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.message.IDiyFrameIds;
import com.zhidian.wifibox.message.IDiyMsgIds;
import com.zhidian.wifibox.util.AppUtils;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.Setting;

/**
 * 软件升级Activity
 * 
 * @author zhaoyl
 * 
 */
public class AppUpdateActivity extends Activity {

	private TextView tvUpdateTotal; // 可升级应用总数
	private TextView tvUpdateSize;// 全部升级需要的流量
	private ListView listview;
	private Context mContext;
	private UpdateAppAdapter adapter;
	private LinearLayout mContent;// 加载成功后原界面要显示的内容
	private View mLoading;
	private View mNoContent;
	private View mFail;
	private PageDataBean mBean; // 该页面的数据
	/**
	 * 点击
	 */
	private OnClickListener mClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.header_title_back:
				onBackPressed();
				break;
			case R.id.update_all:
				// 全部升级
				if (mBean.uAppList != null) {
					for (UpdateAppBean bean : mBean.uAppList) {
						// 判读是否已安装已下载
						if (FileUtil.isFileExist(DownloadUtil
								.getCApkFileFromUrl(bean.downloadUrl))) {
							continue;
						}
						if (bean.downloadStatus != DownloadTask.DOWNLOADING
								&& bean.downloadStatus != DownloadTask.WAITING) {
							bean.downloadStatus = DownloadTask.WAITING;
							Intent intent = new Intent(
									IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
							intent.putExtra("command",
									IDownloadInterface.REQUEST_COMMAND_ADD);
							intent.putExtra("url", bean.downloadUrl);
							intent.putExtra("iconUrl", bean.iconUrl);
							intent.putExtra("name", bean.name);
							intent.putExtra("size", bean.size);
							intent.putExtra("packName", bean.packageName);
							intent.putExtra("appId", bean.id + 0l);
							intent.putExtra("version", bean.version);
							intent.putExtra("page", "应用更新");
							TAApplication.getApplication()
									.sendBroadcast(intent);
						}
					}
					adapter.notifyDataSetChanged();
				}
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 应用安装卸载监听器
	 */
	private final BroadcastReceiver mAppInstallListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String packageName = intent.getData().getSchemeSpecificPart();
			onAppAction(packageName);
		}
	};

	/**
	 * 下载广播接收器
	 */
	private final BroadcastReceiver mDownloadListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			DownloadTask task = intent.getParcelableExtra("task");
			if (task != null) {
				notifyDownloadState(task);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_update);
		mContext = this;
		initUI();
		initRegisterReceiver();// 注册广播事件
		updateContent(mBean);
	}

	private void initUI() {
		TextView title = (TextView) findViewById(R.id.header_title_text);
		title.setText("应用升级");
		ImageView btnBack = (ImageView) findViewById(R.id.header_title_back);
		btnBack.setOnClickListener(mClickListener);

		tvUpdateTotal = (TextView) findViewById(R.id.update_total_count);
		tvUpdateSize = (TextView) findViewById(R.id.update_total_size);

		listview = (ListView) findViewById(R.id.app_update_listview);

		mNoContent = findViewById(R.id.no_content);
		Button nBtn = (Button) mNoContent.findViewById(R.id.jump);
		nBtn.setText("返回首页");
		nBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
				TAApplication.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
						IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
						TabController.NAVIGATIONFEATURE, null);
				v.postDelayed(new Runnable() {

					@Override
					public void run() {
						TAApplication.sendHandler(null, IDiyFrameIds.ACTIONBAR,
								IDiyMsgIds.JUMP_TITLE, 0, null, null);
					}
				}, 50);
			}
		});
		TextView text = (TextView) mNoContent.findViewById(R.id.text);
		text.setText("亲的应用都是最新版本的");
		mLoading = findViewById(R.id.loading_frame);
		mContent = (LinearLayout) findViewById(R.id.home_liear_connent);
		mFail = findViewById(R.id.fail);
		Button bx = (Button) mFail.findViewById(R.id.jump);
		bx.setText("返回首页");
		bx.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
				TAApplication.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
						IDiyMsgIds.NAV_SWITCH_NAVIGATION, -1,
						TabController.NAVIGATIONFEATURE, null);
				v.postDelayed(new Runnable() {

					@Override
					public void run() {
						TAApplication.sendHandler(null, IDiyFrameIds.ACTIONBAR,
								IDiyMsgIds.JUMP_TITLE, 0, null, null);
					}
				}, 50);
			}
		});
		TextView tx = (TextView) mFail.findViewById(R.id.text);
		tx.setText("Sorry，加载失败~");
		mNoContent.setVisibility(View.GONE);
		mContent.setVisibility(View.GONE);
		mLoading.setVisibility(View.VISIBLE);
		mFail.setVisibility(View.GONE);

		adapter = new UpdateAppAdapter(mContext);
		listview.setAdapter(adapter);
		findViewById(R.id.update_all).setOnClickListener(mClickListener);
	}

	private void initRegisterReceiver() {
		// 注册下载广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(IDownloadInterface.DOWNLOAD_BROADCAST_ACTION);
		registerReceiver(mDownloadListener, intentFilter);

		// 注册应用安装卸载事件
		intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");
		registerReceiver(mAppInstallListener, intentFilter);
	}

	private void updateContent(PageDataBean bean) {
		if (bean == null) {
			// 数据还没初始化
			mFail.setVisibility(View.GONE);
			mLoading.setVisibility(View.VISIBLE);
			mNoContent.setVisibility(View.GONE);
			mContent.setVisibility(View.GONE);
			getUpdateAppData();
		} else {
			mBean = bean;
			// 直接加载缓存的数据
			if (mBean.uAppList.size() > 0) {
				// 缓存数据
				mFail.setVisibility(View.GONE);
				mLoading.setVisibility(View.GONE);
				mNoContent.setVisibility(View.GONE);
				mContent.setVisibility(View.VISIBLE);
				updateDownloadState(mBean.uAppList);
				adapter.update(mBean.uAppList);
				tvUpdateTotal.setText(mBean.uAppList.size() + "");
			} else {
				mFail.setVisibility(View.GONE);
				mLoading.setVisibility(View.GONE);
				mNoContent.setVisibility(View.VISIBLE);
				mContent.setVisibility(View.GONE);
			}
		}
	}

	/********************
	 * 获取更新数据
	 *******************/
	private void getUpdateAppData() {
		TAApplication.getApplication().doCommand(
				getString(R.string.updatecontroller),
				new TARequest(UpdateController.GAIN_UPDATE_NETWORK,
						CDataDownloader.getUpdateAppUrl()),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {// 成功获取数据
						mFail.setVisibility(View.GONE);
						mLoading.setVisibility(View.GONE);
						mNoContent.setVisibility(View.GONE);
						mContent.setVisibility(View.VISIBLE);
						PageDataBean bean = (PageDataBean) response.getData();
						mBean = bean;
						updateDownloadState(bean.uAppList);
						if (bean.uAppList.size() > 0) {
							// 缓存数据
							mFail.setVisibility(View.GONE);
							mLoading.setVisibility(View.GONE);
							mNoContent.setVisibility(View.GONE);
							mContent.setVisibility(View.VISIBLE);
							tvUpdateTotal.setText(bean.uAppList.size() + "");
							long size = 0;
							for (UpdateAppBean ubean : bean.uAppList) {
								size += ubean.size;
							}
							tvUpdateSize.setText(Formatter.formatFileSize(
									mContext, size * 1024L));
							adapter.update(bean.uAppList);
						} else {
							mFail.setVisibility(View.GONE);
							mLoading.setVisibility(View.GONE);
							mNoContent.setVisibility(View.VISIBLE);
							mContent.setVisibility(View.GONE);
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
						mFail.setVisibility(View.VISIBLE);
						mLoading.setVisibility(View.GONE);
						mNoContent.setVisibility(View.GONE);
						mContent.setVisibility(View.GONE);
					}
				}, true, false);
	}

	/**************************
	 * 接收到安装广播后
	 * 
	 * @param packName
	 ***************************/
	private void onAppAction(String packName) {
		if (mBean == null || mBean.uAppList == null) {
			return;
		}
		// 如果应用的版本号与服务器的版本号一致，则移除该应用
		String version = AppUtils.getVersion(this, packName);
		Iterator<UpdateAppBean> iterator = mBean.uAppList.iterator();
		while (iterator.hasNext()) {
			UpdateAppBean bean = iterator.next();
			if (bean.packageName.equals(packName)
					&& bean.version.equals(version)) {
				iterator.remove();
			}
		}
		// 更新个数总大小
		tvUpdateTotal.setText(mBean.uAppList.size() + "");
		long size = 0;
		for (UpdateAppBean ubean : mBean.uAppList) {
			size += ubean.size;
		}
		tvUpdateSize.setText(Formatter.formatFileSize(mContext, size * 1024L));
		updateContent(mBean);
		TAApplication.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
				IDiyMsgIds.SHOW_UPDATE_COUNT, mBean.uAppList == null ? 0
						: mBean.uAppList.size(), null, null);
		Setting setting = new Setting(TAApplication.getApplication());
		setting.putInt(Setting.UPDATE_COUNT, mBean.uAppList == null ? 0
				: mBean.uAppList.size());
	}

	public void notifyDownloadState(DownloadTask downloadTask) {
		boolean needToUpdate = false;
		if (mBean != null && mBean.uAppList != null
				&& mBean.uAppList.size() > 0) {
			for (UpdateAppBean bean : mBean.uAppList) {
				if (bean.downloadUrl.equals(downloadTask.url)) {
					needToUpdate = true;
					bean.downloadStatus = downloadTask.state;
					bean.alreadyDownloadPercent = downloadTask.alreadyDownloadPercent;
				}
			}
		}
		if (needToUpdate) {
			adapter.update(mBean.uAppList);
		}
	}

	/**
	 * 用下载任务列表更新应用列表的下载状态
	 */
	private void updateDownloadState(List<UpdateAppBean> list) {
		if (list == null || list.size() <= 0) {
			return;
		}
		Map<String, DownloadTask> map = DownloadTaskRecorder.getInstance()
				.getDownloadTaskList();
		for (UpdateAppBean bean : list) {
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
	protected void onResume() {
		super.onResume();
		adapter.notifyDataSetChanged();
		// 页面统计
		StatService.trackBeginPage(this, "软件升级");
		XGPushClickedResult click = XGPushManager.onActivityStarted(this);
		if (click != null) {
			// TODO
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 页面统计
		StatService.trackEndPage(this, "软件升级");
		XGPushManager.onActivityStoped(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		// 必须要调用这句
		setIntent(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mDownloadListener);
		unregisterReceiver(mAppInstallListener);
	}

}
