package com.escape.uninstaller.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.FrameLayout;

public class MainViewGroup extends FrameLayout {

	private final BroadcastReceiver mAppInstallListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
					|| Intent.ACTION_PACKAGE_REMOVED.equals(action)
					|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				mTabManageView.onAppAction(packageName);
			}
		}
	};

	private TabManageView mTabManageView;

	public MainViewGroup(Context context) {
		super(context);
		init();
	}

	private void init() {
		mTabManageView = new TabManageView(getContext());
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		addView(mTabManageView, lp);
		// 注册广播接收器
		registerReceiver();
	}

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
		getContext().registerReceiver(mAppInstallListener, intentFilter);
	}

	/**
	 * 反注册广播接收器
	 */
	private void unRegisterReceiver() {
		// 反注册安装广播接收器
		getContext().unregisterReceiver(mAppInstallListener);
	}

	public void onDestroy() {
		unRegisterReceiver();
		mTabManageView.onDestroy();
	}

}
