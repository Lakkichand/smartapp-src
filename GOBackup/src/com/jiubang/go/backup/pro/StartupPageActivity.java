package com.jiubang.go.backup.pro;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.analytics.tracking.android.EasyTracker;
import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.model.BackupManager;
import com.jiubang.go.backup.pro.schedules.ContactCheckerSchedule;
import com.jiubang.go.backup.pro.statistics.StatisticsDataManager;

/**
 * 启动页
 * @author maiyongshen
 *
 */
public class StartupPageActivity extends Activity {
	//启动页的展示时间
	private static final int SHOW_DELAY = 1200;
	private Handler mHandler = new Handler();
	// 启动时间，目前是指onResume方法的执行时间
	private long mStartupTime = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
//		LogUtil.d("startup page activity onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startup_page);		
	}
	
	private void startMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mStartupTime = System.currentTimeMillis();
		new Thread(new Runnable() {
			@Override
			public void run() {
				//初始化
				init();
				//跳转到主界面
				long now = System.currentTimeMillis();
				final long delta = now - mStartupTime;
//				LogUtil.d("delta = " + delta);
				if (delta >= SHOW_DELAY) {
//					LogUtil.d("start main activity immediately");
					startMainActivity();
				} else {
//					LogUtil.d("start main activity delay");
					mHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							startMainActivity();
						}
					}, SHOW_DELAY - delta);
				}
			}
		}).start();
	}
	/**
	 * <br>功能简述:初始化的方法
	 * <br>功能详细描述:
	 * <br>注意:
	 */
	private void init() {
//		long t = System.currentTimeMillis();
		StatisticsDataManager sdm = StatisticsDataManager.getInstance();
		// 统计用户首次启动时间
		if (sdm.getFirstLaunchTime(this) < 0) {
			sdm.setFirstLaunchTime(this, System.currentTimeMillis());
		}
		
		// 扫描备份包
		final BackupManager bm = BackupManager.getInstance();
		bm.init(StartupPageActivity.this);
		
		// 设置联系人修改通知推送
		setupContactsChangePush();
		
//		t = System.currentTimeMillis() - t;
//		LogUtil.d("StartupPage init time = " + t);
	}
	
	private void setupContactsChangePush() {
		PreferenceManager pm = PreferenceManager.getInstance();
		if (pm.getLong(getApplicationContext(), PreferenceManager.KEY_NEXT_CONTACT_CHAGE_CHECK_TIME, 0) == 0) {
			// 设置程序安装 首次运行的联系人变更检查推送
			GoBackupApplication.postRunnable(new Runnable() {
				@Override
				public void run() {
					ContactCheckerSchedule.getInstance(getApplicationContext())
							.reflashContactToPreference();
					ContactCheckerSchedule.getInstance(getApplicationContext()).scheduleNextCheck();
				}
			});
		}
	}
	
	/**
	 * 初始化谷歌统计
	 */
	private void initGA() {	
		Intent intent = this.getIntent();
		Uri uri = intent.getData();					
		EasyTracker.getInstance().setContext(this);
		if (uri != null) {
			if (uri.getQueryParameter("utm_source") != null) {
				Log.i("ABEN", "uri.getPath() = " + uri.getPath());
				EasyTracker.getTracker().setCampaign(uri.getPath());
			} else if (uri.getQueryParameter("referrer") != null) {
				EasyTracker.getTracker().setReferrer(uri.getQueryParameter("referrer"));
				Log.i("ABEN", "uri.getQueryParameter() = " + uri.getQueryParameter("referrer"));
			}
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		EasyTracker.getInstance().activityStart(this);
		
		initGA();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		EasyTracker.getInstance().activityStop(this);
	}
}
