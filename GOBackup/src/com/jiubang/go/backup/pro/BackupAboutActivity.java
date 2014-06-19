package com.jiubang.go.backup.pro;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import com.jiubang.go.backup.ex.R;
import com.jiubang.go.backup.pro.net.version.VersionChecker;
import com.jiubang.go.backup.pro.net.version.VersionInfo;
import com.jiubang.go.backup.pro.statistics.StatisticsDataManager;
import com.jiubang.go.backup.pro.statistics.StatisticsKey;

/**
 * @author jiangpeihe
 *关于
 */
public class BackupAboutActivity extends PreferenceActivity {
	private static final String URL_3G_CN = "http://3g.cn";
	// 版本信息
	private Preference mVersionInfo;
	// 软件评分
	private Preference mRateSoft;
	// 版权信息
	private Preference mCopyrightInfo;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initPreference();
	}

	private void initPreference() {
		addPreferencesFromResource(R.xml.go_backup_about_preference);
		mVersionInfo = findPreference(getString(R.string.key_version_info));
		if (mVersionInfo != null) {
			try {
				PackageManager mPreferenceManager = getPackageManager();
				final String versionName = mPreferenceManager.getPackageInfo(getPackageName(), 0).versionName;
				mVersionInfo.setSummary(versionName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mVersionInfo.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					VersionChecker versionChecker = VersionChecker.getInstance();
					versionChecker.checkUpdate(BackupAboutActivity.this, false);
					VersionChecker.planToCheckUpdateNextTime(getApplicationContext());
					StatisticsDataManager.getInstance().updateStatisticBoolean(
							getApplicationContext(), StatisticsKey.HAS_CHECK_UPDATE, true);
					return true;
				}
			});
		}

		mRateSoft = findPreference(getString(R.string.key_soft_rate));
		if (mRateSoft != null) {
			mRateSoft.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					VersionChecker.gotoMarket(BackupAboutActivity.this, null);
					return true;
				}
			});
		}

		mCopyrightInfo = findPreference(getString(R.string.key_copyright_info));
		if (mCopyrightInfo != null) {
			mCopyrightInfo.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL_3G_CN));
					try {
						startActivity(intent);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return true;
				}
			});
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		registerVersionUpdateEventRecevier();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		unregisterVersionUpdateEventReceiver();
	}
	
	public void registerVersionUpdateEventRecevier() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(VersionChecker.ACTION_NEW_UPDATE);
		filter.addAction(VersionChecker.ACTION_FORCE_UPDATE);
		filter.addAction(VersionChecker.ACTION_SHOW_UPDATE_TIP);
		registerReceiver(mVersionUpdateReceiver, filter);
	}

	public void unregisterVersionUpdateEventReceiver() {
		try {
			unregisterReceiver(mVersionUpdateReceiver);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private BroadcastReceiver mVersionUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			VersionInfo versionInfo = intent.getParcelableExtra(VersionChecker.EXTRA_VERSION_INFO);
			String message = intent.getStringExtra(VersionChecker.EXTRA_MESSAGE);
			if (VersionChecker.ACTION_NEW_UPDATE.equals(action)) {
				VersionChecker.showUpdateInfoDialog(BackupAboutActivity.this, versionInfo);
			} else if (VersionChecker.ACTION_FORCE_UPDATE.equals(action)) {
				VersionChecker.showForceUpdateDialog(BackupAboutActivity.this, versionInfo);
			} else if (VersionChecker.ACTION_SHOW_UPDATE_TIP.equals(action)) {
				VersionChecker.showTipDialog(BackupAboutActivity.this, message);
			}
		}
	};

}
