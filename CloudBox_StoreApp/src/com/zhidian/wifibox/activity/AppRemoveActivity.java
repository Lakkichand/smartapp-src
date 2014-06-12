package com.zhidian.wifibox.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ta.TAApplication;
import com.ta.mvc.common.TAIResponseListener;
import com.ta.mvc.common.TARequest;
import com.ta.mvc.common.TAResponse;
import com.umeng.analytics.MobclickAgent;
import com.zhidian.wifibox.R;
import com.zhidian.wifibox.adapter.AppDetailPagerAdapter;
import com.zhidian.wifibox.controller.InstalledAppController;
import com.zhidian.wifibox.data.AppInfo;
import com.zhidian.wifibox.listener.AppremoveCallBackListener;
import com.zhidian.wifibox.util.AppInfoProvider;
import com.zhidian.wifibox.util.DrawUtil;
import com.zhidian.wifibox.view.PageProgressBitmapDrawable;
import com.zhidian.wifibox.view.PersonApp;
import com.zhidian.wifibox.view.SystemAppRemove;

/**
 * 软件卸载Activity
 * 
 * @author zhaoyl
 * 
 */
public class AppRemoveActivity extends Activity implements
		OnCheckedChangeListener, OnClickListener {

	private static final String TAG = AppRemoveActivity.class.getSimpleName();
	protected static final int GET_SYSTEM_APP_FINISH = 80;
	protected static final int GET_USER_APP_FINISH = 81;
	private ViewPager viewPager;
	private RadioButton rbtnPerson; // 个人应用
	private RadioButton rbtnSystem; // 系统预装
	private List<View> listViews; // Tab页面列表
	private PersonApp personApp;
	private SystemAppRemove systemAppRemove;

	private AppInfoProvider provider;
	private List<AppInfo> appinfos, userappInfo, systemappInfo;
	private Context mContext;
	private LinearLayout progress_layout; // 加载loading
	private ImageView mProgress; // 加载进度图
	private LinearLayout contentLayout; // 内容
	private TextView mProgressText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remove_app);
		mContext = this;
		provider = new AppInfoProvider(this);
		initUI();		
		initViewPager();
		showProgress();
		initData();
		initRegisterReceiver();// 注册广播事件

	}

	private void initRegisterReceiver() {
		// 注册应用安装卸载事件
		IntentFilter intentFilter = new IntentFilter();
		intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addDataScheme("package");
		registerReceiver(mAppInstallListener, intentFilter);
	}

	/**
	 * 应用卸载监听器
	 */
	private final BroadcastReceiver mAppInstallListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				personApp.setUpdate(packageName,
						new AppremoveCallBackListener() {

							@Override
							public void callback(int size) {
								rbtnPerson.setText("个人应用（" + size + "）");
							}
						});

				systemAppRemove.setUpdate(packageName,
						new AppremoveCallBackListener() {

							@Override
							public void callback(int size) {
								rbtnSystem.setText("系统预装（" + size + "）");
							}
						});
				// Toast.makeText(mContext, "正在刷新列表...", Toast.LENGTH_SHORT)
				// .show();
			}
		}
	};

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GET_SYSTEM_APP_FINISH:
				break;
			case GET_USER_APP_FINISH:
				if (personApp == null) {

					rbtnPerson.setText("个人应用（" + userappInfo.size() + "）");
					if (systemappInfo != null) {
						rbtnSystem
								.setText("系统预装（" + systemappInfo.size() + "）");
					}

					personApp = new PersonApp(mContext, listViews.get(0),
							userappInfo);
					showContent();
				} else {
					personApp.setUpdate(userappInfo);
					rbtnPerson.setText("个人应用（" + userappInfo.size() + "）");
					rbtnSystem.setText("系统预装（" + systemappInfo.size() + "）");
				}

				if (systemAppRemove == null) {
					systemAppRemove = new SystemAppRemove(mContext,
							listViews.get(1), systemappInfo,
							new AppremoveCallBackListener() {

								@Override
								public void callback(int size) {
									rbtnSystem.setText("系统预装（" + size + "）");

								}
							});
				} else {
					systemAppRemove.setUpdate(systemappInfo);
				}
				break;
			}
		}

	};

	private void setUIData() {
		if (personApp == null) {

			rbtnPerson.setText("个人应用（" + userappInfo.size() + "）");
			if (systemappInfo != null) {
				rbtnSystem.setText("系统预装（" + systemappInfo.size() + "）");
			}

			personApp = new PersonApp(mContext, listViews.get(0), userappInfo);
			showContent();
		} else {
			personApp.setUpdate(userappInfo);
			rbtnPerson.setText("个人应用（" + userappInfo.size() + "）");
			rbtnSystem.setText("系统预装（" + systemappInfo.size() + "）");
		}

		if (systemAppRemove == null) {
			systemAppRemove = new SystemAppRemove(mContext, listViews.get(1),
					systemappInfo, new AppremoveCallBackListener() {

						@Override
						public void callback(int size) {
							rbtnSystem.setText("系统预装（" + size + "）");

						}
					});
		} else {
			systemAppRemove.setUpdate(systemappInfo);
		}
	}

	private void initData() {
		TAApplication.getApplication().doCommand(
				getString(R.string.installedappcontroller),
				new TARequest(InstalledAppController.INSTALLED_APP, null),
				new TAIResponseListener() {

					@SuppressWarnings("unchecked")
					@Override
					public void onSuccess(TAResponse response) {
						// TODO Auto-generated method stub
						HashMap<String, List<AppInfo>> map = (HashMap<String, List<AppInfo>>) response
								.getData();
						userappInfo = map.get("user");
						systemappInfo = map.get("system");
						setUIData();
					}

					@Override
					public void onStart() {
						// TODO Auto-generated method stub

					}

					@Override
					public void onRuning(TAResponse response) {
						Object obj = response.getData();
						if (obj != null && obj instanceof Integer) {
							int progress = (Integer) obj;
							Drawable drawable = new PageProgressBitmapDrawable(
									getResources(),
									DrawUtil.sPageProgressBitmap, progress);
							mProgress.setImageDrawable(drawable);
							mProgressText.setText(progress + "%");
						}

					}

					@Override
					public void onFinish() {
						// TODO Auto-generated method stub

					}

					@Override
					public void onFailure(TAResponse response) {
						// TODO Auto-generated method stub

					}
				}, true, false);

		// new Thread() {
		//
		// @Override
		// public void run() {
		// super.run();
		//
		// appinfos = provider.getAllApps();
		// getUserApps(appinfos);
		// handler.sendEmptyMessage(GET_USER_APP_FINISH);
		// }
		//
		// }.start();

	}

	/**
	 * 分类
	 * 
	 * @param appinfos
	 * @return
	 */
	private void getUserApps(List<AppInfo> appinfos) {
		systemappInfo = new ArrayList<AppInfo>();
		userappInfo = new ArrayList<AppInfo>();
		for (AppInfo appinfo : appinfos) {
			if (appinfo.isSystemApp()) {
				if (provider.filterOpen(appinfo.getPackname())) {
					systemappInfo.add(appinfo);
				}

			} else {
				userappInfo.add(appinfo);
			}
		}
	}

	private void initUI() {
		TextView tvTitle = (TextView) findViewById(R.id.header_title_text);
		tvTitle.setText(R.string.app_remove);
		ImageButton btnBack = (ImageButton) findViewById(R.id.header_title_back);
		btnBack.setOnClickListener(this);

		viewPager = (ViewPager) findViewById(R.id.removeapp_vPager);
		rbtnPerson = (RadioButton) findViewById(R.id.removeapp_radiobt_person);
		rbtnPerson.setOnCheckedChangeListener(this);
		rbtnPerson.setChecked(true);
		rbtnSystem = (RadioButton) findViewById(R.id.removeapp_radiobt_system);
		rbtnSystem.setOnCheckedChangeListener(this);

		progress_layout = (LinearLayout) findViewById(R.id.person_app_probar);
		contentLayout = (LinearLayout) findViewById(R.id.person_app_home);
		mProgressText = (TextView) findViewById(R.id.progress_text);
		mProgress = (ImageView) findViewById(R.id.progress);
		Drawable drawable = new PageProgressBitmapDrawable(getResources(),
				DrawUtil.sPageProgressBitmap, 0);
		mProgress.setImageDrawable(drawable);

	}

	private void showProgress() {
		contentLayout.setVisibility(View.GONE);
		progress_layout.setVisibility(View.VISIBLE);
	}

	private void showContent() {
		contentLayout.setVisibility(View.VISIBLE);
		progress_layout.setVisibility(View.GONE);
	}

	/*********************
	 * 初始化ViewPager
	 ********************/
	private void initViewPager() {

		listViews = new ArrayList<View>();
		LayoutInflater mInflater = LayoutInflater.from(this);
		listViews.add(mInflater.inflate(R.layout.view_person_app, null));
		listViews.add(mInflater.inflate(R.layout.view_person_app, null));
		viewPager.setAdapter(new AppDetailPagerAdapter(listViews));
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener(this));
	}

	/**
	 * 页卡切换监听
	 * 
	 */

	public class MyOnPageChangeListener implements OnPageChangeListener {

		public MyOnPageChangeListener(Context context) {

		}

		@Override
		public void onPageSelected(final int arg0) {
			switch (arg0) {
			case 0:
				rbtnPerson.setChecked(true);
				break;

			case 1:

				rbtnSystem.setChecked(true);
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

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			switch (buttonView.getId()) {
			case R.id.removeapp_radiobt_person:
				viewPager.setCurrentItem(0);
				break;

			case R.id.removeapp_radiobt_system:
				viewPager.setCurrentItem(1);
				break;
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onPageStart("软件卸载");
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPageEnd("软件卸载");
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mAppInstallListener);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.header_title_back:
			this.finish();
			break;

		default:
			break;
		}
	}

}
