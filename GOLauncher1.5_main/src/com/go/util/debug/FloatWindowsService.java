package com.go.util.debug;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 悬浮窗口，展现当前内存值的后台service。
 * 该service每隔1s通知前台重绘一次当前状态值。
 * @author zengyongping
 * 
 */
public class FloatWindowsService extends Service {
	public static final String TAG = "FloatWindowsService";
	TextView mTextView = null;
	WindowManager mWM = null;
	Context mContext = null;

	private Timer mTimer = null;
	private TimerTask mTimerTask = null;

	private ServiceHandler mServiceHandler = null;
	private Looper mServiceLooper = null;
	private String mMemory = "";
	RectF mRect = null;
	Paint mPaint = null;
	private Method mMethod = null;
	private int[] mPid = null;
	ActivityManager mActivityManager = null;

	private int mOtherSD = 0;
	private int mOtherPD = 0;
	private int mOtherPss = 0;

	private static int TIMER = 1000;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		initTime();
	}

	private void initTime() {
		if (mTimer == null) {
			mTimer = new Timer();
			mTimerTask = new TimerTask() {
				@Override
				public void run() {
					Message msg = mServiceHandler.obtainMessage();
					mServiceHandler.sendMessage(msg);
				}
			};
			mTimer.schedule(mTimerTask, TIMER, TIMER);
		}
	}

	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			int serviceId = msg.arg1;
			switch (serviceId) {
				default :
					break;
			}
			Debug.MemoryInfo info = getMem();
			if (info != null) {
				int pss = info.otherPss;
				int shareDirty = info.otherSharedDirty;
				int privateDirty = info.otherPrivateDirty;
				if (pss != mOtherPss || shareDirty != mOtherSD || privateDirty != mOtherPD) {
					int totle = pss + shareDirty + privateDirty;
					// mMemory = "T" + pss + ",S" + shareDirty + ",P" +
					// privateDirty + ",All" + totle;
					StringBuffer sb = new StringBuffer();
					sb.append("otherPss=");
					sb.append(pss);
					sb.append(",otherSD=");
					sb.append(shareDirty);
					sb.append(",otherPD=");
					sb.append(privateDirty);
					mMemory = sb.toString();
					mTextView.post(new Runnable() {
						@Override
						public void run() {

							mTextView.setText(mMemory);
						}
					});
					mOtherPss = pss;
					mOtherSD = shareDirty;
					mOtherPD = privateDirty;
				}
			}
		}
	}

	private void initMemory() {
		try {
			Context context = GOLauncherApp.getContext();
			mActivityManager = (ActivityManager) mContext
					.getSystemService(Context.ACTIVITY_SERVICE);
			mPid = new int[] { android.os.Process.myPid() };

			Class<?> activityClass = Class.forName("android.app.ActivityManager");
			mMethod = activityClass.getDeclaredMethod("getProcessMemoryInfo", int[].class);

		} catch (Exception e) {
		}
	}

	private Debug.MemoryInfo getMem() {
		Debug.MemoryInfo mem = null;
		if (mMethod != null) {
			try {
				Debug.MemoryInfo[] info = (Debug.MemoryInfo[]) mMethod.invoke(mActivityManager,
						mPid);
				mem = info[0];
			} catch (Exception e) {

			}
		}
		return mem;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = GOLauncherApp.getContext();
		initView();
		initMemory();

		HandlerThread thread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		mRect = new RectF();
		mPaint = new Paint();
		mPaint.setColor(0xffff0000);

	}

	private void initView() {
		mWM = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
		mTextView = new TextView(mContext) {
			@Override
			protected void onDraw(android.graphics.Canvas canvas) {
				int t = getTop();
				int l = getLeft();
				int b = getBottom();
				int r = getRight();
				mRect.top = t;
				mRect.left = t;
				mRect.right = r;
				mRect.bottom = b;
				canvas.drawRect(mRect, mPaint);
				super.onDraw(canvas);
			};
		};

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
		lp.type = 2002;
		// wmParams.format=1;
		lp.flags |= 8;

		lp.gravity = Gravity.LEFT | Gravity.TOP; // 调整悬浮窗口至左上角
		// 以屏幕左上角为原点，设置x、y初始值
		lp.x = 0;
		lp.y = 0;

		// 设置悬浮窗口长宽数据
		lp.width = 300;
		lp.height = 25;
		mTextView.setText("");
		mWM.addView(mTextView, lp);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		mServiceLooper.quit();
		mTimerTask.cancel();
		mTimer.cancel();

		mWM.removeView(mTextView);
	}

	/**
	 * 启动服务
	 */
	public static void beginService(/* Context context */) {
		if (true) {
			Log.v("TestSpeed", "FloatWindowsService beginService 1");
			Context context = GOLauncherApp.getContext();
			Intent i = new Intent(context, FloatWindowsService.class);
			context.startService(i);
		}
	}

	/**
	 * 终止服务
	 */
	public static void stopService(/* Context context */) {
		if (true) {
			Context context = GOLauncherApp.getContext();
			Intent i = new Intent(context, FloatWindowsService.class);
			context.stopService(i);
		}
	}
}
