package com.zhidian.wifibox.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.ta.TAApplication;
import com.ta.mvc.command.TACommand;
import com.ta.mvc.common.TARequest;
import com.zhidian.wifibox.util.InfoUtil;
import com.zhidian.wifibox.util.Setting;

/**
 * 管理控制器
 * 
 * @author xiedezhi
 * 
 */
public class ManagerController extends TACommand {
	/**
	 * 计算咪表当前值
	 */
	public static final String CALCULATE = "MANAGERCONTROLLER_CALCULATE";
	/**
	 * 一键清理
	 */
	public static final String CLEAN = "MANAGERCONTROLLER_CLEAN";

	private Handler mHandler = new Handler(Looper.getMainLooper());

	@Override
	protected void executeCommand() {
		TARequest request = getRequest();
		String command = (String) request.getTag();
		if (command.equals(CALCULATE)) {
			ActivityManager acm = (ActivityManager) TAApplication
					.getApplication()
					.getSystemService(Context.ACTIVITY_SERVICE);
			MemoryInfo outInfo = new MemoryInfo();
			acm.getMemoryInfo(outInfo);
			long avi = (long) (outInfo.availMem / 1024.0 / 1024.0 + 0.5);
			long total = (long) (InfoUtil.getTotalRAM() / 1024.0 + 0.5);
			long used = total - avi;
			int process = (int) (used * 1.0 / total * 100 + 0.5);
			CalculateRunnable runnable = new CalculateRunnable(process);
			mHandler.post(runnable);
		} else if (command.equals(CLEAN)) {
			// 一键清理
			Setting setting = new Setting(TAApplication.getApplication());
			String json = setting.getString(Setting.PROTECT_APP);
			JSONArray array = null;
			try {
				array = new JSONArray(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if (array == null) {
				array = new JSONArray();
			}
			Set<String> protect = new HashSet<String>();
			for (int i = 0; i < array.length(); i++) {
				try {
					String packname = array.getString(i);
					protect.add(packname);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			int current = (Integer) request.getData();
			CleanRunnable runnable = new CleanRunnable(current);
			mHandler.post(runnable);
			ActivityManager activityManager = (ActivityManager) TAApplication
					.getApplication()
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager
					.getRunningAppProcesses();
			for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
				String[] pkgs = runningAppProcessInfo.pkgList;
				for (String pkg : pkgs) {
					if (pkg.equals(TAApplication.getApplication()
							.getPackageName())) {
						continue;
					}
					if (protect.contains(pkg)) {
						continue;
					}
					activityManager.restartPackage(pkg);
					activityManager.killBackgroundProcesses(pkg);
				}
			}
			MemoryInfo outInfo = new MemoryInfo();
			activityManager.getMemoryInfo(outInfo);
			long avi = (long) (outInfo.availMem / 1024.0 / 1024.0 + 0.5);
			long total = (long) (InfoUtil.getTotalRAM() / 1024.0 + 0.5);
			long used = total - avi;
			int process = (int) (used * 1.0 / total * 100 + 0.5);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			runnable.stop(process);
		}
	}

	class CleanRunnable implements Runnable {

		private boolean isIncreasing = false;

		private int mCurrentProgress = 0;

		private int mFinal = -1;

		public CleanRunnable(int start) {
			mCurrentProgress = start;
			// 先递减
			isIncreasing = false;
			mFinal = -1;
		}

		@Override
		public void run() {
			mHandler.removeCallbacks(this);
			if (isIncreasing) {
				mCurrentProgress++;
				mCurrentProgress++;
				if (mCurrentProgress > 100) {
					mCurrentProgress = 100;
					isIncreasing = false;
				}
			} else {
				mCurrentProgress--;
				mCurrentProgress--;
				if (mCurrentProgress < 0) {
					mCurrentProgress = 0;
					isIncreasing = true;
				}
			}
			if (mFinal > 0) {
				if (Math.abs(mCurrentProgress - mFinal) <= 1) {
					sendSuccessMessage(mFinal);
					return;
				}
			}
			sendRuntingMessage(mCurrentProgress);
			mHandler.postDelayed(this, 10);

		}

		public void stop(int progress) {
			mFinal = progress;
		}
	}

	class CalculateRunnable implements Runnable {

		private int mProgress;

		private int mCurrentProgress;

		public CalculateRunnable(int progress) {
			mProgress = progress;
			mCurrentProgress = 0;
		}

		@Override
		public void run() {
			mHandler.removeCallbacks(this);
			if (mCurrentProgress >= mProgress) {
				sendSuccessMessage(mProgress);
			} else {
				sendRuntingMessage(mCurrentProgress);
				mCurrentProgress++;
				mCurrentProgress++;
				mHandler.postDelayed(this, 10);
			}
		}
	}

}
