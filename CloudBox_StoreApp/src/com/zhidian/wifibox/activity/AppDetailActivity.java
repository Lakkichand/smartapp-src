package com.zhidian.wifibox.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ta.TAApplication;
import com.umeng.analytics.MobclickAgent;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.AppDetailPagerAdapter;
import com.zhidian.wifibox.data.AppDataBean;
import com.zhidian.wifibox.data.DetailDataBean;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.DownloadTaskRecorder;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.listener.AppdetailListener;
import com.zhidian.wifibox.util.DownloadUtil;
import com.zhidian.wifibox.util.FileUtil;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.view.AppDescribe;
import com.zhidian.wifibox.view.MyViewPager;
import com.zhidian.wifibox.view.TextProgressBar;
import com.zhidian.wifibox.view.UserEvaluate;
import com.zhidian.wifibox.view.dialog.DownloadHintDialog;
import com.zhidian.wifibox.view.dialog.DownloadHintDialog.CancleCallBackListener;
import com.zhidian.wifibox.view.dialog.DownloadHintDialog.GoonCallBackListener;

/**
 * App应用详情
 * 
 * @author Zhaoyl
 * 
 */
public class AppDetailActivity extends Activity implements
		OnCheckedChangeListener, OnClickListener {

	private MyViewPager viewPager; // 页卡内容
	private TextView tvTitle; // app名称
	private List<View> listViews; // Tab页面列表
	private ImageView ibtnBack; // 返回
	private RadioButton rbtnDescribe; // 应用描述
	private RadioButton rbtnEvaluate; // 用户评价
	private LinearLayout btnDownload; // 下载
	private ImageView ivStatusBtn;
	private TextView tvStatusBtn;
	private long appId; // 应用Id
	private AppDataBean bean; // 应用bean
	private AppDescribe appDescribe; // 应用描述
	private UserEvaluate userEvaluate; // 用户评价
	private TextProgressBar textProgressBar; // 下载进度条
	private LinearLayout showDowloadBtn; // 显示下载按钮
	private RelativeLayout progressShow;
	public static Map<String, Bitmap> showGameinfoCache = new HashMap<String, Bitmap>();
	private DetailDataBean reBean;
	private ImageView ivStatus; // 下载状态logo
	private TextView tvStatus; // 下载状态Text
	private DownloadHintDialog hintDialog; // 下载提示对话框

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_appdetail);
		initIntent();
		initUI();
		initViewPager();

		initAppDescribe();
		// 初始化用户评价界面
		if (userEvaluate == null) {
			userEvaluate = new UserEvaluate(this, listViews.get(1), appId);
		}
		initRegisterReceiver();// 注册广播事件

	}

	/**
	 * 初始化应用描述界面
	 */
	private void initAppDescribe() {
		if (appDescribe == null) {
			appDescribe = new AppDescribe(this, listViews.get(0), appId, bean,
					TAApplication.getApplication(), new AppdetailListener() {

						@Override
						public void onCancle() {
							showDowloadBtn.setVisibility(View.GONE);
						}

						@Override
						public void onShow() {
							showDowloadBtn.setVisibility(View.VISIBLE);

						}

						@Override
						public void getData(DetailDataBean bean) {
							tvTitle.setText(bean.name);
							reBean = bean;
							gainDownloadStatus();
						}

						@Override
						public void getAppData(String title) {
							// TODO Auto-generated method stub
							tvTitle.setText(title);
						}

					});
		}

	}

	/**
	 * 注册下载广播事件
	 */
	private void initRegisterReceiver() {
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

	// 获得当前应用下载状态
	protected void gainDownloadStatus() {
		if (reBean == null) {
			return;
		}
		Map<String, DownloadTask> map = DownloadTaskRecorder.getInstance()
				.getDownloadTaskList();
		if (map.containsKey(reBean.downloadUrl)) {// 当前应用正在下载中
			btnDownload.setVisibility(View.GONE);
			progressShow.setVisibility(View.VISIBLE);
			DownloadTask task = map.get(reBean.downloadUrl);
			int downloadStatus = task.state;
			int alreadyDownloadPercent = task.alreadyDownloadPercent;
			getDownloadStatus(downloadStatus, alreadyDownloadPercent);
		} else {
			boolean isInstall = InstallingValidator.getInstance().isAppExist(
					this, reBean.packageName);
			if (isInstall) {// 已安装
				tvStatusBtn.setText("打　开");
				ivStatusBtn.setImageResource(R.drawable.v2_btn_open_icon);
				btnDownload
						.setBackgroundResource(R.drawable.btn_download_open_selector);
				btnDownload.setOnClickListener(mOpenAppClickListener);
			} else {
				btnDownload.setOnClickListener(mDownloadClickListener);
				btnDownload.setVisibility(View.VISIBLE);
				progressShow.setVisibility(View.GONE);
			}

		}
	}

	/**
	 * 下载状态
	 * 
	 * @param downloadStatus
	 *            状态
	 * @param alreadyDownloadPercent
	 *            下载百分比
	 */
	private void getDownloadStatus(int downloadStatus,
			int alreadyDownloadPercent) {
		String packName = reBean.packageName;
		String apkFileName = DownloadUtil
				.getCApkFileFromUrl(reBean.downloadUrl);
		boolean isInstall = InstallingValidator.getInstance().isAppExist(
				getApplicationContext(), packName);
		if (isInstall) {
			btnDownload.setVisibility(View.VISIBLE);
			progressShow.setVisibility(View.GONE);
			tvStatusBtn.setText("打　开");
			ivStatusBtn.setImageResource(R.drawable.v2_btn_open_icon);
			btnDownload.setBackgroundResource(R.drawable.btn_download_open_selector);
			btnDownload.setOnClickListener(mOpenAppClickListener);
		} else if (downloadStatus == DownloadTask.INSTALLING) {
			btnDownload.setVisibility(View.VISIBLE);
			progressShow.setVisibility(View.GONE);
			tvStatusBtn.setText("安装中");
			btnDownload.setTag(null);
			ivStatusBtn.setImageResource(R.drawable.v2_footer_icon_03);
			btnDownload
			.setBackgroundResource(R.drawable.btn_download_install_selector);
			btnDownload.setOnClickListener(null);
		} else if (FileUtil.isFileExist(apkFileName)) {
			btnDownload.setVisibility(View.VISIBLE);
			progressShow.setVisibility(View.GONE);
			tvStatusBtn.setText("安　装");
			btnDownload.setTag(reBean.downloadUrl);
			ivStatusBtn.setImageResource(R.drawable.v2_footer_icon_03);
			btnDownload
			.setBackgroundResource(R.drawable.btn_download_install_selector);
			btnDownload.setOnClickListener(mInstallApkClickListener);
		} else if (downloadStatus == DownloadTask.DOWNLOADING) {
			btnDownload.setVisibility(View.GONE);
			progressShow.setVisibility(View.VISIBLE);
			textProgressBar.setOnClickListener(mPauseClickListener);
			tvStatus.setText(R.string.btn_pause);
			ivStatus.setImageResource(R.drawable.v2_footer_icon_02);
			textProgressBar.setProgress(alreadyDownloadPercent);
		} else if (downloadStatus == DownloadTask.WAITING) {
			// 等待中
			btnDownload.setVisibility(View.VISIBLE);
			progressShow.setVisibility(View.GONE);
			textProgressBar.setOnClickListener(null);
			tvStatusBtn.setText(R.string.btn_waiting);
			btnDownload
			.setBackgroundResource(R.drawable.btn_copy_selector);
			ivStatusBtn.setImageResource(R.drawable.v2_footer_icon_08);
			textProgressBar.setProgress(alreadyDownloadPercent);
		} else if (downloadStatus == DownloadTask.PAUSING) {
			btnDownload.setVisibility(View.VISIBLE);
			progressShow.setVisibility(View.GONE);
			btnDownload.setOnClickListener(mContinueClickListener);
			tvStatusBtn.setText(R.string.btn_goon);
			//TODO
			btnDownload
			.setBackgroundResource(R.drawable.btn_download_goon_selector);
			ivStatusBtn.setImageResource(R.drawable.v2_jixu_icon);
			textProgressBar.setProgress(alreadyDownloadPercent);
		} else {
			btnDownload.setVisibility(View.GONE);
			progressShow.setVisibility(View.VISIBLE);
			textProgressBar.setProgress(alreadyDownloadPercent);
		}
	}

	private void initUI() {
		hintDialog = new DownloadHintDialog(this);
		rbtnDescribe = (RadioButton) findViewById(R.id.tab_detail_radiobt_describe);
		rbtnDescribe.setOnCheckedChangeListener(this);
		rbtnDescribe.setChecked(true);
		rbtnEvaluate = (RadioButton) findViewById(R.id.tab_detail_radiobt_evaluate);
		rbtnEvaluate.setOnCheckedChangeListener(this);
		viewPager = (MyViewPager) findViewById(R.id.detail_vPager);

		ibtnBack = (ImageView) findViewById(R.id.detail_back);
		ibtnBack.setOnClickListener(this);
		btnDownload = (LinearLayout) findViewById(R.id.detail_download_btn);
		showDowloadBtn = (LinearLayout) findViewById(R.id.detail_show_dowbtn);
		tvTitle = (TextView) findViewById(R.id.detail_app_name);
		progressShow = (RelativeLayout) findViewById(R.id.detail_app_linear);
		tvStatus = (TextView) findViewById(R.id.detail_status);
		ivStatus = (ImageView) findViewById(R.id.detail_sign);
		
		ivStatusBtn = (ImageView) findViewById(R.id.detail_btn_img);
		tvStatusBtn = (TextView) findViewById(R.id.detail_btn_text);
		textProgressBar = (TextProgressBar) findViewById(R.id.detail_app_down_progress);
		textProgressBar.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				// TODO
				ToStopDownload();
				hintDialog.show();
				return false;
			}
		});

		hintDialog.setGoonCallBackListener(new GoonCallBackListener() {// 继续下载

					@Override
					public void onClick() {
						Intent intent = new Intent(
								IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
						intent.putExtra("command",
								IDownloadInterface.REQUEST_COMMAND_CONTINUE);
						intent.putExtra("url", reBean.downloadUrl);
						sendBroadcast(intent);
					}
				});
		hintDialog.setCancleCallBackListener(new CancleCallBackListener() {// 取消下载

					@Override
					public void onClick() {
						Intent intent = new Intent(
								IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
						intent.putExtra("command",
								IDownloadInterface.REQUEST_COMMAND_DELETE);
						intent.putExtra("url", reBean.downloadUrl);
						TAApplication.getApplication().sendBroadcast(intent);

						btnDownload.setVisibility(View.VISIBLE);
						btnDownload.setOnClickListener(mDownloadClickListener);
						progressShow.setVisibility(View.GONE);
					}
				});

	}

	/*********************
	 * 初始化ViewPager
	 ********************/
	private void initViewPager() {

		listViews = new ArrayList<View>();
		LayoutInflater mInflater = LayoutInflater.from(this);
		listViews.add(mInflater.inflate(R.layout.view_detail_describe, null));
		listViews.add(mInflater.inflate(R.layout.view_detail_evaluate, null));
		viewPager.setAdapter(new AppDetailPagerAdapter(listViews));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener(this));
	}

	/**
	 * 页卡切换监听
	 * 
	 */

	public class MyOnPageChangeListener implements OnPageChangeListener {
		private Context context = null;

		public MyOnPageChangeListener(Context context) {
			this.context = context;
		}

		@Override
		public void onPageSelected(final int arg0) {
			switch (arg0) {
			case 0:
				rbtnDescribe.setChecked(true);

				break;

			case 1:
				// 初始化用户评价界面
				// if (userEvaluate == null) {
				// userEvaluate = new UserEvaluate(context, listViews.get(1),
				// appId);
				// }
				rbtnEvaluate.setChecked(true);
				break;
			}

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	private void initIntent() {
		appId = getIntent().getLongExtra("appId", -1);
		bean = (AppDataBean) getIntent().getSerializableExtra("bean");
	}

	/*********************
	 * 暂停下载
	 *********************/
	private void ToStopDownload() {
		Intent intent = new Intent(IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
		intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_PAUSE);
		intent.putExtra("url", reBean.downloadUrl);
		sendBroadcast(intent);
	}

	/**
	 * 暂停点击事件
	 */
	private OnClickListener mPauseClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ToStopDownload();
		}
	};
	/**
	 * 继续点击事件
	 */
	private OnClickListener mContinueClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command",
					IDownloadInterface.REQUEST_COMMAND_CONTINUE);
			intent.putExtra("url", reBean.downloadUrl);
			sendBroadcast(intent);
//			btnDownload.setVisibility(View.GONE);
//			progressShow.setVisibility(View.VISIBLE);
		}
	};
	/**
	 * 下载点击事件
	 */
	private OnClickListener mDownloadClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(
					IDownloadInterface.DOWNLOAD_REQUEST_ACTION);
			intent.putExtra("command", IDownloadInterface.REQUEST_COMMAND_ADD);
			intent.putExtra("url", reBean.downloadUrl);
			intent.putExtra("iconUrl", reBean.iconUrl);
			intent.putExtra("name", reBean.name);
			intent.putExtra("size", reBean.size);
			intent.putExtra("packName", reBean.packageName);
			intent.putExtra("appId", reBean.id);
			intent.putExtra("version", reBean.version);
			sendBroadcast(intent);

//			btnDownload.setVisibility(View.GONE);
//			progressShow.setVisibility(View.VISIBLE);
		}
	};

	/**
	 * 安装APK的点击监听
	 */
	private OnClickListener mInstallApkClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				String downloadUrl = (String) v.getTag();
				String apkFileName = DownloadUtil
						.getCApkFileFromUrl(downloadUrl);
				File file = new File(apkFileName);
				Intent intent = new Intent();
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file),
						"application/vnd.android.package-archive");
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * 打开应用的点击监听
	 */
	private OnClickListener mOpenAppClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String packName = reBean.packageName;
			try {
				PackageManager packageManager = getPackageManager();
				Intent intent = packageManager
						.getLaunchIntentForPackage(packName);
				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.detail_back:
			this.finish();
			break;
		}

	}

	/**
	 * 应用安装卸载监听器
	 */
	private final BroadcastReceiver mAppInstallListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (reBean == null) {
				return;
			}
			String action = intent.getAction();
			if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
					|| Intent.ACTION_PACKAGE_REMOVED.equals(action)
					|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				// 通知安装验证器
				InstallingValidator.getInstance().onAppAction(
						AppDetailActivity.this, packageName);
				// 通知SlidingMenu
				// 通知MainViewGroup
				String name = reBean.packageName;
				if (name.equals(packageName)) {
					boolean isInstall = InstallingValidator.getInstance()
							.isAppExist(AppDetailActivity.this,
									reBean.packageName);
					if (isInstall) {// 已安装
						tvStatusBtn.setText("打　开");
						ivStatusBtn.setImageResource(R.drawable.v2_btn_open_icon);
						btnDownload
								.setBackgroundResource(R.drawable.btn_download_open_selector);
						btnDownload.setOnClickListener(mOpenAppClickListener);
					} else {
						btnDownload.setOnClickListener(mDownloadClickListener);
						btnDownload.setVisibility(View.VISIBLE);
						progressShow.setVisibility(View.GONE);
					}
				}
			}
		}
	};

	/**
	 * 下载广播接收器
	 */
	private final BroadcastReceiver mDownloadListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (reBean == null) {
				return;
			}
			DownloadTask task = intent.getParcelableExtra("task");
			if (task.url.equals(reBean.downloadUrl)) {
				int alreadyDownloadPercent = task.alreadyDownloadPercent;
				int downloadStatus = task.state;
				getDownloadStatus(downloadStatus, alreadyDownloadPercent);

			}
		}
	};

	public void onResume() {
		super.onResume();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageStart("应用详情");
			MobclickAgent.onResume(this);
		}
	}

	@Override
	protected void onPause() {
		// 释放资源
		if (showGameinfoCache != null) {
			showGameinfoCache.clear();
		}
		super.onPause();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageEnd("应用详情");
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
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			switch (buttonView.getId()) {
			case R.id.tab_detail_radiobt_describe:
				viewPager.setCurrentItem(0);
				break;

			case R.id.tab_detail_radiobt_evaluate:
				viewPager.setCurrentItem(1);
				break;
			}
		}

	}

}
