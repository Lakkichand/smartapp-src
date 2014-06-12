package com.escape.cachecleaner;

import java.util.Collections;
import java.util.List;

import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.escape.cachecleaner.CacheManager.OnActionListener;
import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;

/**
 * 缓存清理控制器
 * 
 * @author xiedezhi
 * 
 */
public class CacheCleanerController extends TACommand {

	/**
	 * 检测APP的缓存
	 */
	public static final String CACHE_APP = "CACHECLEANERCONTROLLER_CACHE_APP";
	/**
	 * 一键清理缓存
	 */
	public static final String A_KEY_CLEAN_UP = "CACHECLEANERCONTROLLER_A_KEY_CLEAN_UP";
	/**
	 * 检查单个应用的缓存
	 */
	public static final String CHECK_SINGLE_APP = "CHECK_SINGLE_APP_CACHECLEANERCONTROLLER";

	private Handler mHandler = new Handler(Looper.getMainLooper());

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		Log.e("", "executeCommand " + command);
		if (command.equals(CACHE_APP)) {
			final CacheManager manager = new CacheManager(TAApplication
					.getApplication().getPackageManager());
			OnActionListener listener = new OnActionListener() {

				@Override
				public void onScanStarted(int appsCount) {
					sendStartMessage();
				}

				@Override
				public void onScanProgressUpdated(int current, int max) {
					sendRuntingMessage(new int[] { current, max });
				}

				@Override
				public void onScanCompleted(List<CacheDataBean> apps) {
					Collections.sort(apps);
					sendSuccessMessage(apps);
				}

				@Override
				public void onCleanStarted() {
				}

				@Override
				public void onCleanCompleted(long cacheSize) {
				}
			};
			manager.setOnActionListener(listener);
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					manager.scanCache();
				}
			});
		} else if (command.equals(A_KEY_CLEAN_UP)) {
			final long cacheSize = (Long) request.getData();
			// 一键清理
			final CacheManager manager = new CacheManager(TAApplication
					.getApplication().getPackageManager());
			OnActionListener listener = new OnActionListener() {

				@Override
				public void onScanStarted(int appsCount) {
				}

				@Override
				public void onScanProgressUpdated(int current, int max) {
				}

				@Override
				public void onScanCompleted(List<CacheDataBean> apps) {
				}

				@Override
				public void onCleanStarted() {
				}

				@Override
				public void onCleanCompleted(long cacheSize) {
					sendSuccessMessage(null);
				}
			};
			manager.setOnActionListener(listener);
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					manager.cleanCache(cacheSize);
				}
			});
		} else if (command.equals(CHECK_SINGLE_APP)) {
			CacheDataBean bean = (CacheDataBean) request.getData();
			// 获取该应用的缓存
			PackageManager packageManager = TAApplication.getApplication()
					.getPackageManager();
			AppUtils.invokeMethod(packageManager, "getPackageSizeInfo",
					bean.mInfo.packageName, new IPackageStatsObserver.Stub() {

						@Override
						public void onGetStatsCompleted(PackageStats pStats,
								boolean succeeded) throws RemoteException {
							if (succeeded) {
								sendSuccessMessage(pStats.cacheSize);
							}
						}
					});

		}
	}

}
