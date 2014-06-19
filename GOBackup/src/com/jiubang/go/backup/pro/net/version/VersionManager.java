package com.jiubang.go.backup.pro.net.version;

import java.io.IOException;
import java.util.Locale;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.jiubang.go.backup.pro.net.UrlUtil;
import com.jiubang.go.backup.pro.statistics.StatisticsTool;

/**
 * 版本管理
 *
 * @author maiyongshen
 */
public class VersionManager {

	/**
	 * 版本升级监听
	 *
	 * @author maiyongshen
	 */
	public interface VersionUpdateListener {
		public void onVersionUpdate(Context context, VersionInfo versionInfo);

		public void onVersionForceUpdate(Context context, VersionInfo versionInfo);

		public void onNoNewVersion(Context context);

		public void onSystemPrompt(Context context, VersionInfo versionInfo);

		public void onError(Context context, int errCode, String errMsg, Object data);
	}

	public static final int TYPE_UPDATE_AUTOMATICALLY = 1;
	public static final int TYPE_UPDATE_MANUALLY = 2;

	private static final String CHECK_VERSION_URL = "http://imupdate.3g.cn:8888/versions/check?bn=";
	private static final String VERSION = "&v=";
	private static final String VERSION_CODE = "&vc=";
	private static final String TYPE = "&type=";
	private static final String CHANNEL = "&channel=";
	private static final String PARAM_PRODUCT_ID = "&p=";
	private static final String LANGUAGE = "&lang=";
	private static final int PACKAGE_ID = 244;
	private static final int PRODUCT_ID = 40;
	private int mType = TYPE_UPDATE_AUTOMATICALLY;

	public void checkVersion(final Context context, int type, final VersionUpdateListener listener) {
		mType = type;
		new Thread() {
			@Override
			public void run() {
				checkVersionInternal(context, listener);
			}
		} .start();
	}

	private void checkVersionInternal(Context context, VersionUpdateListener listener) {
		if (context == null) {
			throw new IllegalArgumentException("context is null!");
		}
		String versionName = null;
		int vesionCode = 0;
		Context appContext = context.getApplicationContext();
		final PackageManager pm = appContext.getPackageManager();
		if (pm != null) {
			try {
				PackageInfo pi = pm.getPackageInfo(appContext.getPackageName(), 0);
				if (pi != null) {
					versionName = pi.versionName;
					vesionCode = pi.versionCode;
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				return;
			}
		}

		String lan = "en";
		Locale locale = Locale.getDefault();
		String language = String.format("%s-%s", locale.getLanguage(), locale.getCountry());
		if (language != null && language.contains("zh-CN")) {
			lan = "zh-CN";
		}
		int channel = StatisticsTool.getProductChannelCode(appContext);

		String updateUrl = CHECK_VERSION_URL + PACKAGE_ID + VERSION + versionName + LANGUAGE + lan
				+ PARAM_PRODUCT_ID + PRODUCT_ID + CHANNEL + channel + TYPE + mType + VERSION_CODE + vesionCode;
		String result = null;
		try {
			result = UrlUtil.getUrlResult(updateUrl);
		} catch (IOException e) {
			e.printStackTrace();
			if (listener != null) {
				listener.onError(context, -1, null, null);
			}
			return;
		}
		postProcess(context, parseResult(result), listener);
	}

	private VersionInfo parseResult(String source) {
		if (source == null || source.length() <= 0) {
			return null;
		}
		VersionInfo versionInfo = null;
		String[] results = source.split("\\|\\|\\|");
		if (results != null && results.length > 0) {
			try {
				versionInfo = new VersionInfo();
				versionInfo.mAction = Integer.parseInt(results[0]);
				versionInfo.mTipInfo = results.length > 1 ? results[1] : null;
				versionInfo.mDownloadUrl = results.length > 2 ? results[2] : null;
				versionInfo.mLatestReleaseDate = results.length > 3 ? results[3] : null;
				versionInfo.mLatestVersionNumber = results.length > 4 ? results[4] : null;
				versionInfo.mLatestReleaseNote = results.length > 5 ? results[5] : null;
			} catch (Exception e) {
				versionInfo = null;
			}
		}
		return versionInfo;
	}

	private void postProcess(Context context, VersionInfo versionInfo,
			VersionUpdateListener listener) {
		if (listener == null) {
			return;
		}
		if (versionInfo == null) {
			listener.onError(context, -1, null, null);
			return;
		}

		switch (versionInfo.mAction) {
			case VersionInfo.ACTION_PROMPT_TO_UPDATE :
				listener.onVersionUpdate(context, versionInfo);
				break;
			case VersionInfo.ACTION_FORCE_UPDATE :
				listener.onVersionForceUpdate(context, versionInfo);
				break;
			case VersionInfo.ACTION_NORMAL_VERSION :
				listener.onNoNewVersion(context);
				break;
			case VersionInfo.ACTION_SYSTEM_PROMPT :
				listener.onSystemPrompt(context, versionInfo);
				break;
			case VersionInfo.ACTION_SOFT_MAINTENANCE :
			case VersionInfo.ACTION_USER_DEFINED :
			default :
				break;
		}
	}
}
