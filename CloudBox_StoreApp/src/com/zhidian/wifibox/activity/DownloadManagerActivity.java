package com.zhidian.wifibox.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ta.TAApplication;
import com.umeng.analytics.MobclickAgent;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.download.DownloadTask;
import com.zhidian.wifibox.download.IDownloadInterface;
import com.zhidian.wifibox.util.InstallingValidator;
import com.zhidian.wifibox.view.DownloadManagerContainer;

/**
 * 下载管理Activity
 */
public class DownloadManagerActivity extends Activity {

	/**
	 * 应用安装卸载监听器
	 */
	private final BroadcastReceiver mAppInstallListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
					|| Intent.ACTION_PACKAGE_REMOVED.equals(action)
					|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				// 通知安装验证器
				InstallingValidator.getInstance().onAppAction(
						DownloadManagerActivity.this, packageName);
				// 通知container
				mContainer.onAppAction(packageName);
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
			// 通知container
			mContainer.notifyDownloadState(task);
		}
	};
	/**
	 * 下载管理页面
	 */
	private DownloadManagerContainer mContainer;

	/**
	 * 注册广播接收器
	 */
	private void registerReceiver() {
		// 注册应用安装卸载事件
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addDataScheme("package");
		registerReceiver(mAppInstallListener, intentFilter);
		// 注册下载广播事件
		intentFilter = new IntentFilter();
		intentFilter.addAction(IDownloadInterface.DOWNLOAD_BROADCAST_ACTION);
		registerReceiver(mDownloadListener, intentFilter);
	}

	/**
	 * 反注册广播接收器
	 */
	private void unRegisterReceiver() {
		// 反注册安装广播接收器
		unregisterReceiver(mAppInstallListener);
		// 反注册下载广播接收器
		unregisterReceiver(mDownloadListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater inflater = LayoutInflater
				.from(DownloadManagerActivity.this);
		mContainer = (DownloadManagerContainer) inflater.inflate(
				R.layout.downloadcontainer, null);
		setContentView(R.layout.downloadmanager);
		FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
		frame.addView(mContainer, new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT));
		mContainer.updateContent(null);
		registerReceiver();

		findViewById(R.id.header_title_back).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						onBackPressed();
					}
				});
		TextView title = (TextView) findViewById(R.id.header_title_text);
		title.setText(R.string.downloadmanager_title);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageStart("下载管理");
			MobclickAgent.onResume(this);
		}
		mContainer.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!TAApplication.DEBUG) {
			MobclickAgent.onPageEnd("下载管理");
			MobclickAgent.onPause(this);
		}
	}

	@Override
	protected void onDestroy() {
		unRegisterReceiver();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(DownloadManagerActivity.this,
				MainActivity.class);
		startActivity(intent);
	}

}
