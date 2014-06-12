package com.jiubang.ggheart.apps.desks.diy.themescan;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.data.theme.OnlineThemeGetter;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;

/**
 * 主题详情Activity
 * 
 * @author yangbing
 * */
public class ThemeDetailActivity extends Activity {

	public static final int MSG_GET_DETAIL_FINISHED = 0X1;
	public static final int MSG_GET_DETAIL_FAILED = 0X2;
	public static final int TOAST_SHOW_TIME = 600;
	
	private ThemeDetailView mThemeDetailView;
	private String mThemePackageName;
	private String mThemeName = "";
	private ThemeInfoBean mThemeDetailBean;
	private BroadcastReceiver mThemeUninstallReceiver; // 监听主题被卸载
	private int mModel;
	private boolean mIsGetThemeBeanSuccessByNet = true;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		confirmOrientation();
		mThemeDetailView = (ThemeDetailView) LayoutInflater.from(ThemeDetailActivity.this).inflate(
				R.layout.theme_detail, null);
		setContentView(mThemeDetailView);
		Intent intent = getIntent();
		mModel = intent.getIntExtra(ThemeConstants.DETAIL_MODEL_EXTRA_KEY,
				ThemeConstants.DETAIL_MODEL_INSTALL_EXTRA_VALUE);
		mThemePackageName = intent.getStringExtra(ThemeConstants.PACKAGE_NAME_EXTRA_KEY);
		mThemeName = intent.getStringExtra(ThemeConstants.TITLE_EXTRA_KEY);
		if (mModel == ThemeConstants.DETAIL_MODEL_FEATURED_EXTRA_VALUE) {
			ThemeInfoBean infoBean = new ThemeInfoBean();
			infoBean.setPackageName(mThemePackageName);
			int id = intent.getIntExtra(ThemeConstants.DETAIL_ID_EXTRA_KEY, 0);
			infoBean.setFeaturedId(id);
			mThemeDetailBean = new OnlineThemeGetter(this).getFeatureThemeDetailInfo(infoBean,
					mHandler);
			if (mThemeDetailBean != null) {
				dismissProgress();
				mThemeDetailView.reLayoutView(mThemeDetailBean);
			} else {
				showProgress();
			}
		} else {
			dismissProgress();
			registerThemeUninstallReceiver();
			mThemeDetailBean = ThemeManager.getInstance(this).getThemeInfo(mThemePackageName);
			mThemeDetailBean.setBeanType(ThemeConstants.LAUNCHER_INSTALLED_THEME_ID);
			mThemeDetailView.reLayoutView(mThemeDetailBean);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		confirmOrientation();
//		mThemeDetailView.reLayoutView(mThemeDetailBean);
	}

	private void registerThemeUninstallReceiver() {

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addDataScheme("package");
		intentFilter.setPriority(Integer.MAX_VALUE);

		mThemeUninstallReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
					String packageName = intent.getDataString();
					if (packageName.contains(ThemeConstants.LAUNCHER_THEME_PREFIX)
							|| packageName.contains(ThemeConstants.LAUNCHER_BIG_THEME_PREFIX)) {
						ThemeDataManager.getInstance(context).clearup();
						ThemeDetailActivity.this.finish();
						ThemeManageActivity.sRefreshFlag = true;
					}
				} else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
					// 当在详情主题那里下载主题并且安装后，需要刷新界面
					String packageName = intent.getDataString();
					if (packageName.contains(ThemeConstants.LAUNCHER_THEME_PREFIX)
							|| packageName.contains(ThemeConstants.LAUNCHER_BIG_THEME_PREFIX)) {
						ThemeDataManager.getInstance(context).clearup();
						ThemeDetailActivity.this.finish();
						ThemeManageActivity.sRefreshFlag = true;
					}
				}

			}
		};

		try {

			registerReceiver(mThemeUninstallReceiver, intentFilter);
		} catch (Throwable e) {
			try {
				unregisterReceiver(mThemeUninstallReceiver);
				registerReceiver(mThemeUninstallReceiver, intentFilter);
			} catch (Throwable e1) {
				e1.printStackTrace();
			}
		}

	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onDestroy() {
		mThemeDetailView.cleanup();
		if (mThemeUninstallReceiver != null) {
			unregisterReceiver(mThemeUninstallReceiver);
		}
		super.onDestroy();
	};

	/**
	 * 判断是横屏还是竖屏
	 * */
	private void confirmOrientation() {
		DisplayMetrics mMetrics = getResources().getDisplayMetrics();
		if (mMetrics.widthPixels <= mMetrics.heightPixels) {
			if (!SpaceCalculator.sPortrait) {
				SpaceCalculator.setIsPortrait(true);
				SpaceCalculator.getInstance(this).calculateItemViewInfo();
			}
		} else {
			if (SpaceCalculator.sPortrait) {
				SpaceCalculator.setIsPortrait(false);
				SpaceCalculator.getInstance(this).calculateThemeListItemCount();
			}
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mThemeDetailView != null) {
				mThemeDetailView.goBack();
			}
		}
		return false;
	}

	public static void exit() {
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		confirmOrientation();
		if (!mIsGetThemeBeanSuccessByNet) {
			showProgress();
		}
		mThemeDetailView.setCurrentScreen(0);
		mThemeDetailView.reLayoutView(mThemeDetailBean);
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			// super.handleMessage(msg);
			switch (msg.what) {
				case MSG_GET_DETAIL_FINISHED :
					if (msg.obj != null && msg.obj instanceof ThemeInfoBean) {
						dismissProgress();
						mThemeDetailBean = (ThemeInfoBean) msg.obj;
						mThemeDetailView.reLayoutView(mThemeDetailBean);
					} else {
						Toast.makeText(ThemeDetailActivity.this, R.string.http_exception, TOAST_SHOW_TIME)
								.show();
						finish();
					}
					break;
				case MSG_GET_DETAIL_FAILED :
					Toast.makeText(ThemeDetailActivity.this, R.string.theme_get_detailinfo_failed,
							TOAST_SHOW_TIME).show();
					finish();
				default :
					break;
			}
		}
	};

	private void showProgress() {
		mIsGetThemeBeanSuccessByNet = false;
		mThemeDetailView.findViewById(R.id.theme_detail_loading).setVisibility(View.GONE);
		mThemeDetailView.findViewById(R.id.theme_detail).setVisibility(View.VISIBLE);
//		mThemeDetailView.findViewById(R.id.detail_buttons).setVisibility(View.GONE);
		mThemeDetailView.setImageBackground(mThemeName, mThemePackageName);
	}

	private void dismissProgress() {
		mIsGetThemeBeanSuccessByNet = true;
		mThemeDetailView.findViewById(R.id.theme_detail_loading).setVisibility(View.GONE);
		mThemeDetailView.findViewById(R.id.detail_buttons).setVisibility(View.VISIBLE);
		mThemeDetailView.findViewById(R.id.theme_detail).setVisibility(View.VISIBLE);
	}
}
