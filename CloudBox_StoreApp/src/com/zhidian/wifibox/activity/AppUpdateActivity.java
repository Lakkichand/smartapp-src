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
import com.umeng.analytics.MobclickAgent;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.UpdateAppAdapter;
import com.zhidian.wifibox.controller.UpdateController;
import com.zhidian.wifibox.data.CDataDownloader;
import com.zhidian.wifibox.data.PageDataBean;
import com.zhidian.wifibox.data.TabDataManager;
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
import com.zhidian.wifibox.view.BgPageView;
import com.zhidian.wifibox.view.BgPageView.onCallBackOnClickListener;

/**
 * 软件升级Activity
 * 
 * @author zhaoyl
 * 
 */
public class AppUpdateActivity extends Activity implements OnClickListener {

	private static final String TAG = AppUpdateActivity.class.getSimpleName();
	private TextView tvUpdateTotal; // 可升级应用总数
	private Button btnAllUpdate; // 全部升级
	private View tvNotUpdate; // 无更新时显示
	private LinearLayout updateLayout; // 有更新时显示
	private ListView listview;
	private Context mContext;
	private UpdateAppAdapter adapter;
	private BgPageView bgPageView;
	private LinearLayout home_liear_pro; // 加载数据进度条
	private LinearLayout home_liear_connent;// 加载成功后原界面要显示的内容
	/**
	 * 是否正在加载下一页
	 */
	private volatile boolean mLoadingNexPage = false;
	private PageDataBean mBean; // 该页面的数据

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_update);
		mContext = this;
		initUI();
		initData();
		initRegisterReceiver();// 注册广播事件
	}

	private void initRegisterReceiver() {
		// 注册下载广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(IDownloadInterface.DOWNLOAD_BROADCAST_ACTION);
		registerReceiver(mDownloadListener, intentFilter);

		// 注册应用安装卸载事件
		intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addDataScheme("package");
		registerReceiver(mAppInstallListener, intentFilter);
	}

	private void initUI() {
		TextView tvTitle = (TextView) findViewById(R.id.header_title_text);
		tvTitle.setText(R.string.app_update);
		ImageView btnBack = (ImageView) findViewById(R.id.header_title_back);
		btnBack.setOnClickListener(this);

		tvUpdateTotal = (TextView) findViewById(R.id.app_update_total);
		tvNotUpdate = findViewById(R.id.no_content);
		findViewById(R.id.jump).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
		updateLayout = (LinearLayout) findViewById(R.id.update_show_layout);

		listview = (ListView) findViewById(R.id.app_update_listview);
		btnAllUpdate = (Button) findViewById(R.id.app_update_all_btn);
		btnAllUpdate.setOnClickListener(this);

		home_liear_pro = (LinearLayout) findViewById(R.id.home_liear_pro);
		home_liear_connent = (LinearLayout) findViewById(R.id.home_liear_connent);
		bgPageView = new BgPageView(mContext, home_liear_pro,
				home_liear_connent);
		adapter = new UpdateAppAdapter(mContext);
		listview.setAdapter(adapter);

	}

	/***********************
	 * 初始化数据
	 **********************/
	private void initData() {
		// 获取缓存数据，如果有
		PageDataBean bean = TabDataManager.getInstance().getPageData(
				CDataDownloader.getUpdateAppUrl());
		updateContent(bean);

	}

	/********************
	 * 获取更新数据
	 *******************/
	private void getUpdateAppData() {
		if (mLoadingNexPage) {
			return;
		}
		mLoadingNexPage = true;
		TAApplication.getApplication().doCommand(
				getString(R.string.updatecontroller),
				new TARequest(UpdateController.GAIN_UPDATE_NETWORK,
						CDataDownloader.getUpdateAppUrl()),
				new TAIResponseListener() {

					@Override
					public void onSuccess(TAResponse response) {// 成功获取数据
						mLoadingNexPage = false;

						bgPageView.showContent();// 取消加载进度条
						PageDataBean bean = (PageDataBean) response.getData();
						mBean = bean;
						updateDownloadState(bean.uAppList);
						TabDataManager.getInstance().cachePageData(bean);
						if (bean.uAppList.size() > 0) {
							// 缓存数据
							tvNotUpdate.setVisibility(View.GONE);
							updateLayout.setVisibility(View.VISIBLE);
							tvUpdateTotal.setText(bean.uAppList.size()
									+ "");
							adapter.update(bean.uAppList);
						} else {
							tvNotUpdate.setVisibility(View.VISIBLE);
							updateLayout.setVisibility(View.GONE);
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
					public void onFailure(TAResponse response) {// 获取数据失败
						mLoadingNexPage = false;
						// bgPageView.calneProgress();//取消加载进度条
						bgPageView
								.showLoadException(new onCallBackOnClickListener() {

									@Override
									public void onClick() {
										bgPageView.showProgress();// 显示加载进度条
										getUpdateAppData();
									}
								});
					}
				}, true, false);

	}

	/**************************
	 * 接收到安装广播后
	 * 
	 * @param packName
	 ***************************/
	public void onAppAction(String packName) {
		if (mBean.uAppList == null) {
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
		adapter.update(mBean.uAppList);
		tvUpdateTotal.setText(mBean.uAppList.size() + "");
		MainActivity.sendHandler(null, IDiyFrameIds.NAVIGATIONBAR,
				IDiyMsgIds.SHOW_UPDATE_COUNT, mBean.uAppList == null ? 0
						: mBean.uAppList.size(), null, null);
		Setting setting = new Setting(TAApplication.getApplication());
		setting.putInt(Setting.UPDATE_COUNT, mBean.uAppList == null ? 0
				: mBean.uAppList.size());
	}

	private void updateContent(PageDataBean bean) {
		// 这里传进来的数据要不就是没有初始化的，要不就是已经拿到数据的，不存在服务器错误的情况，因为这种情况不会把数据缓存
		if (bean == null) {
			// 数据还没初始化
			bgPageView.showProgress();// 显示加载进度条
			getUpdateAppData();
		} else {
			mBean = bean;
			// 直接加载缓存的数据
			if (mBean.uAppList.size() > 0) {
				// 缓存数据
				tvNotUpdate.setVisibility(View.GONE);
				updateLayout.setVisibility(View.VISIBLE);
				updateDownloadState(mBean.uAppList);
				adapter.update(mBean.uAppList);
				tvUpdateTotal.setText(mBean.uAppList.size() + "");
			} else {
				tvNotUpdate.setVisibility(View.VISIBLE);
				updateLayout.setVisibility(View.GONE);
			}

		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.header_title_back:
			onBackPressed();
			break;
		case R.id.app_update_all_btn:
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
						TAApplication.getApplication().sendBroadcast(intent);
					}
				}
				adapter.notifyDataSetChanged();
			}
			break;

		default:
			break;
		}

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
				bean.alreadyDownloadPercent = task
						.alreadyDownloadPercent;
			} else {
				bean.downloadStatus = DownloadTask.NOT_START;
				bean.alreadyDownloadPercent = 0;
			}
		}
	}

	/**
	 * 应用安装卸载监听器
	 */
	private final BroadcastReceiver mAppInstallListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				onAppAction(packageName);
			}
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
	protected void onResume() {
		super.onResume();
		adapter.notifyDataSetChanged();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageStart("软件升级");
			MobclickAgent.onResume(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageEnd("软件升级");
			MobclickAgent.onPause(this);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mDownloadListener);
		unregisterReceiver(mAppInstallListener);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent i = new Intent(AppUpdateActivity.this, AppManageActivity.class);
		startActivity(i);
	}

}
