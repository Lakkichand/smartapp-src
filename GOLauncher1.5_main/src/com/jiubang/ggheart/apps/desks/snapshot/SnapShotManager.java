package com.jiubang.ggheart.apps.desks.snapshot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.go.util.file.FileUtil;
import com.go.util.graphics.BitmapUtility;
import com.go.util.window.OrientationControl;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.INotificationId;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.launcher.ICustomAction;
import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 
 * @author dengdazhong
 *
 */
public class SnapShotManager implements SensorEventListener {
	/**
	 * 保存截屏到路径
	 */
	public static final String SNAPSHOT_PATH = LauncherEnv.Path.SNAPSHOT_PATH;
	/**
	 * 空闲时间达到一定的值就取消截屏
	 */
	public static final int TIME_TO_CANEL_CAPTURE = 120000;
	private long mLast; // 最后更新时间
	private long mLastToast; // 最后一次弹toast的时间
	private static final int CRITICAL_ACCELERATION = 7; // 临界速度
	private Context mContext;
	private float mOldAcceleration;

	private int mOritationTpye; // 记录进入截图时的横竖屏类型
	private int mOrientation; // 具体的横竖屏值
	private boolean mIsRootMode = false;
	private String mPath; // 保存截图的路径
	private Bitmap mBitmap; // 截图的bitmap
	public int status = STATUS_STOP; // 截屏状态
	public static final int STATUS_STOP = 0;
	public static final int STATUS_RUNNING = 1;
	public static final int STATUS_PAUSE = 2;
	public static final String NOTIFICATION_RETURN = "exit_capture";
	private static SnapShotManager sInstance;
	
	private int mSnapShotsCount = 0;
	private boolean mIsPreviewShow = false;
	private boolean mIsFirstRun = true;
	
	public SnapShotManager(Context context) {
		mContext = context;
	}

	public static SnapShotManager getInstance(Context context) {
		if (null == sInstance) {
			sInstance = new SnapShotManager(context);
		}
		return sInstance;
	}

	public void setRootMode(boolean isRoot) {
		mIsRootMode = isRoot;
	}
	
	/**
	 * 启动截图
	 */
	public void startCapture() {
		if (FileUtil.isSDCardAvaiable()) {
			SensorManager sensorManager = (SensorManager) mContext.getSystemService(Activity.SENSOR_SERVICE);
			List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
			for (Sensor s : sensors) {
				sensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_GAME);
			}
			resetTimer();
			status = STATUS_RUNNING;
			Intent intent = new Intent(mContext, GoLauncher.class);
			intent.setAction(ICustomAction.ACTION_STOP_SNAPSHOT);
			intent.putExtra(NOTIFICATION_RETURN, true);
			String title = mContext.getString(R.string.snapshot_notification_title);
			String noteTitle = mContext.getString(R.string.snapshot_notification_message_title)
					.replace("%", String.valueOf(mSnapShotsCount));
			String noteText = mContext.getString(R.string.snapshot_notification_message);
			AppUtils.sendNotification(mContext, intent, R.drawable.snapshot_notification_icon,
					title, noteTitle, noteText, INotificationId.SNAPSHOT_NOTIFICATION, Notification.FLAG_NO_CLEAR | Notification.FLAG_AUTO_CANCEL);
			// 建立标志位，用于异常退出后重启
			PreferencesManager sharedPreferences = new PreferencesManager(mContext,
					IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
			sharedPreferences.putBoolean(IPreferencesIds.IS_SCREENSHOT_RUNNING_IN_ROOT_MODE, mIsRootMode);
			sharedPreferences.putBoolean(IPreferencesIds.IS_SCREENSHOT_RUNNING, true);
			sharedPreferences.commit();
		} else {
			Toast.makeText(mContext, R.string.snapshot_sdcard_error, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 取消截图
	 */
	public void cancelCapture() {
		status = STATUS_STOP;
		mOldAcceleration = 0;
		SensorManager sensorManager = (SensorManager) mContext.getSystemService(Activity.SENSOR_SERVICE);
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		for (Sensor s : sensors) {
			sensorManager.unregisterListener(this, s);
		}
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				AppUtils.cancelNotificaiton(GOLauncherApp.getContext(), INotificationId.SNAPSHOT_NOTIFICATION);
				String title = GOLauncherApp.getContext().getString(
						R.string.snapshot_notification_title_off);
				Intent intent = new Intent(GOLauncherApp.getContext(), GoLauncher.class);
				AppUtils.sendNotification(GOLauncherApp.getContext(), intent, R.drawable.snapshot_notification_icon, title, title, title, INotificationId.SNAPSHOT_NOTIFICATION_OFF, Notification.FLAG_AUTO_CANCEL);
				AppUtils.cancelNotificaiton(GOLauncherApp.getContext(), INotificationId.SNAPSHOT_NOTIFICATION_OFF);				
			}
			
		};
		Timer timer = new Timer();
		timer.schedule(task, 500);
//		// 清理
//		if (mBitmap != null) {
//			mBitmap.recycle();
//		}
		// 建立标志位，用于异常退出后重启
		PreferencesManager sharedPreferences = new PreferencesManager(mContext,
				IPreferencesIds.USERTUTORIALCONFIG, Context.MODE_PRIVATE);
		sharedPreferences.putBoolean(IPreferencesIds.IS_SCREENSHOT_RUNNING, false);
		sharedPreferences.putBoolean(IPreferencesIds.IS_SCREENSHOT_RUNNING_IN_ROOT_MODE, false);
		sharedPreferences.commit();
		mBitmap = null;
		mContext = null;
		sInstance = null;
	}
	
	/**
	 * 截屏
	 */
	public void pauseCapture() {
		if (mIsRootMode) {
			return;
		}
		if (status == STATUS_RUNNING) {
			status = STATUS_PAUSE;
			mIsFirstRun = true;
		}
	}

	public void resumeCapture() {
		Log.v("capture", "resumecapture");
		if (status != STATUS_STOP) {
			status = STATUS_RUNNING;
		}
	}

	/**
	 * 重新设置计时器
	 */
	public void resetTimer() {
		mLast = System.currentTimeMillis();
	}

	public void doCapture() {
		status = STATUS_PAUSE;
		mIsPreviewShow = true;
		
		resetTimer();
		File file = new File(SNAPSHOT_PATH);
		if (!file.exists()) {
			file.mkdir();
		}
		int mImgMaxWidth_1 = (int) mContext.getResources().getDimension(R.dimen.share_img_max_width_1);
		int screenW = GoLauncher.getScreenWidth();
		int width = mImgMaxWidth_1 < screenW ? mImgMaxWidth_1 : screenW;
		float scale = width * 1.0f / screenW;
//		if (mBitmap != null) {
//			mBitmap.recycle();
//		}
		try {
			mBitmap = null;
			if (mIsRootMode) {
				mBitmap = RootScreenshot.getScreenBitmap(mContext, scale);
				if (mBitmap == null) {
					Toast.makeText(mContext, R.string.snapshot_root_mode_faild, Toast.LENGTH_SHORT)
							.show();
					setRootMode(false);
					mBitmap = RootScreenshot.getScreenBitmapWithoutRoot(mContext, scale);
				}
			} else {
				setOritation();
				mBitmap = RootScreenshot.getScreenBitmapWithoutRoot(mContext, scale);
				reStoreOritation();
			}
		} catch (OutOfMemoryError e) {
			Toast.makeText(mContext, R.string.snapshot_create_image_error_oom, Toast.LENGTH_SHORT)
					.show();
		} catch (Exception e) {
			if (mIsRootMode) {
				Toast.makeText(mContext, R.string.snapshot_root_mode_faild, Toast.LENGTH_SHORT)
						.show();
				setRootMode(false);
				mBitmap = RootScreenshot.getScreenBitmapWithoutRoot(mContext, scale);
			}
			e.printStackTrace();
		}
		mPath = null;
		if (mBitmap != null) {
			openPreView();
			// 震动
			Vibrator sysVibrator = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);
			sysVibrator.vibrate(300);
			new SaveThread().start();
		} else {
			status = STATUS_RUNNING;
			mIsPreviewShow = false;
		}
		
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		float x = arg0.values[SensorManager.DATA_X];
		if (mIsFirstRun) {
			if (status == STATUS_RUNNING) {
				mIsFirstRun = false; // 防止点击截屏提示框确定按钮前翻转90度，然后立马截屏
				mOldAcceleration = x;
			}
		}
		float acceleration = Math.abs(x - mOldAcceleration);
		mOldAcceleration = x;
		if (status == STATUS_PAUSE && !mIsRootMode && !mIsPreviewShow
				&& acceleration > CRITICAL_ACCELERATION) {
			if ((System.currentTimeMillis() - mLastToast) > 3000) { // 最后一次弹次页面不支持截图和现在的时间大于3秒才触发，防止来回摇晃造成toast多次弹出
				Toast.makeText(mContext, R.string.snapshot_paused, Toast.LENGTH_SHORT).show();
				mLastToast = System.currentTimeMillis();
			}
			return;
		} else if (status != STATUS_RUNNING) {
			return;
		}
		if ((System.currentTimeMillis() - mLast) > TIME_TO_CANEL_CAPTURE) {
			cancelCapture();
			return;
		}

		
		if (acceleration > CRITICAL_ACCELERATION && status == STATUS_RUNNING) { // 加上status == STATUS_RUNNING是因为不停摇动时某些不是很快的机子会触发很多次docapture
			if ((System.currentTimeMillis() - mLast) > 3000) { // 距离上一次截屏时间大于3秒才会再次截屏,防止重复触发造成内存不足
				PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
				if (pm != null && !pm.isScreenOn()) {
					//当前处于暗屏状态
					return;
				}
				Log.v("capture", "capture");
				doCapture();
			}
		}
	}

	public void enterPreview() {
		mIsPreviewShow = true;
	}

	public void exitPreview() {
		mIsPreviewShow = false;
	}
	
	public String getSnapShotPath() {
		return mPath;
	}

	public Bitmap getSnapShotBitmap() {
		return mBitmap;
	}

	public int getSnapShotCount() {
		return mSnapShotsCount;
	}

	public boolean saveCapture() {
		if (null == mPath && mBitmap != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
			mPath = SNAPSHOT_PATH + "/" + dateFormat.format(new Date()).replace(":", "_") + ".jpg";
			try {
				RootScreenshot.saveScreenShot(mContext, mPath, mBitmap);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mSnapShotsCount++;
			Intent intent = new Intent(mContext, GoLauncher.class);
			intent.putExtra(NOTIFICATION_RETURN, true);
			intent.setAction(ICustomAction.ACTION_STOP_SNAPSHOT);
			String title = mContext.getString(R.string.snapshot_notification_title);
			String noteTitle = mContext.getString(R.string.snapshot_notification_message_title)
					.replace("%", String.valueOf(mSnapShotsCount));
			String noteText = mContext.getString(R.string.snapshot_notification_message);
			AppUtils.sendNotification(mContext, intent, R.drawable.snapshot_notification_icon,
					title, noteTitle, noteText, INotificationId.SNAPSHOT_NOTIFICATION,
					Notification.FLAG_NO_CLEAR);
			// 扫描SD卡更新截图目录图库缓存
			scanSDCard();
			
			return true;
		} else if (null != mPath) {
			return true; // 已经生成一遍
		}
		return false;
	}
	
	/**
	 * 扫描SD卡
	 */
	public void scanSDCard() {
		Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
		MediaStore.getMediaScannerUri();
		intent.setClassName("com.android.providers.media",
				"com.android.providers.media.MediaScannerReceiver");
		intent.setData(Uri.fromFile(new File(LauncherEnv.Path.SDCARD)));
		mContext.sendBroadcast(intent);
	}
	
	/***
	 * 设置当前状态不可旋转
	 */
	private void setOritation() {
		OrientationControl.keepCurrentOrientation(GoLauncher.getContext());
	}

	/***
	 * 恢复屏幕状态
	 */
	private void reStoreOritation() {
		OrientationControl.setOrientation(GoLauncher.getContext());
	}

	public void openPreView() {
		Intent intent = new Intent(mContext, SnapShotPreviewer.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		AppUtils.safeStartActivity(mContext, intent);
	}

	public static boolean isAccessGiven() {
		try {
			File file = new File("/dev/graphics/fb0");
			if (file.canRead()) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * check if the mobile has been rooted
	 * 
	 * @exception IOException
	 * @return the mobile has been rooted 
	 * @author TQS
	 */
	public static boolean isRoot() {
		boolean rooted = false;
		boolean hasSuFile = false;
		String command = "ls -l /%s/su";
		File su = new File("/system/bin/su");
		if (su.exists()) {
			hasSuFile = true;
			command = String.format(command, "system/bin");
		} else {
			su = new File("/system/xbin/su");
			if (su.exists()) {
				hasSuFile = true;
				command = String.format(command, "system/xbin");
			} else {
				su = new File("/data/bin/su");
				if (su.exists()) {
					hasSuFile = true;
					command = String.format(command, "data/bin");
				}
			}
		}

		if (hasSuFile == true) {
			Runtime runtime = Runtime.getRuntime();
			try {
				Process proc = runtime.exec(command);
				InputStream input = proc.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(input));
				String result = null;
				while ((result = br.readLine()) != null) {
					if (result.length() > 4) {
						String subString = result.substring(0, 4);
						if (subString.contains("s")) {
							rooted = true;
						}
					}

				}
				input.close();
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return rooted;
	}
	
	/**
	 * 保存图片线程
	 * @author dengdazhong
	 *
	 */
	class SaveThread extends Thread {

		@Override
		public void run() {
			BitmapUtility.saveBitmap(mBitmap, SNAPSHOT_PATH + "/tmp.png");
		}

	}
}
