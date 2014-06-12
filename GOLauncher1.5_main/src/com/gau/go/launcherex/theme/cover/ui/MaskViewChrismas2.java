package com.gau.go.launcherex.theme.cover.ui;

import java.lang.reflect.Field;

import android.content.Context;
import android.content.Intent;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.gau.go.launcherex.theme.cover.CoverBean;
import com.gau.go.launcherex.theme.cover.CoverDataControl;
import com.gau.go.launcherex.theme.cover.DrawUtils;
import com.gau.go.launcherex.theme.cover.ViewControl;
import com.gau.go.launcherex.theme.cover.sensor.AccelerometerDataState;
import com.gau.go.launcherex.theme.cover.sensor.AccelerometerSensor;
import com.gau.go.launcherex.theme.cover.sensor.AudioDataState;
import com.gau.go.launcherex.theme.cover.sensor.BaseState;
import com.gau.go.launcherex.theme.cover.sensor.ISensorType;
import com.gau.go.launcherex.theme.cover.sensor.OnSensorDataChangeListener;
import com.gau.go.launcherex.theme.cover.utils.WakeUpGuard;

/**
 * 
 * 类描述:罩子层的容器View 功能详细描述:
 * 
 * @author guoyiqing
 * @date [2012-10-19]
 */
public class MaskViewChrismas2 extends SurfaceView implements
		SurfaceHolder.Callback, OnSensorDataChangeListener, ILauncherCallback {

	public static final float GRAVITY_MOVE_VALUE = 1.5f; // 开始响应重力感应的临界值
	public static final int MSG_CODE_ADD_FLOWER = 0;
	private static final String MASK_DRAW_THREAD = "mask_draw_thread";
	private static final int DRAW_THREAD_UPFATE_INTERVAL = 20;
	private static final String BROADCAST_REMOVE_MASKVIEW = "com.jiubang.gocover.remove";
	private static final String BROADCAST_HIDE_MASKVIEW = "com.jiubang.gocover.hide";
	private static final String BROADCAST_SHOW_MASKVIEW = "com.jiubang.gocover.show";
//	private AudioSensorer mAudioSensorer;
	private AudioDataState mAudioDataState;
	private AccelerometerSensor mAccelerometerSensor;
	private AccelerometerDataState mAccelerometerState;
	private DrawThread mDrawEnginer;
	private SpiritEngineThread mSpiritEngineThread;
	private volatile boolean mIsActive;
	private Camera mCamera = new Camera();
	private Matrix mMatrix = new Matrix();
	private WakeUpGuard mWakeUpGuard = new WakeUpGuard();
	SurfaceHolder mHolder;

	private CoverDataControl mDataControl; // 数据控制器，包括bean的解析和获取
	private CoverBean mCoverBean; // 整个罩子层的bean
	private ViewControl mViewControl; // Views的控制器

	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG
			| Paint.FILTER_BITMAP_FLAG);
	private Paint mDoDrawPaint = new Paint(Paint.ANTI_ALIAS_FLAG
			| Paint.FILTER_BITMAP_FLAG);
	private int mOffsetY = 0; // 竖直方向上的偏移，主要是长按图标的时候状态栏会收起，罩子层也相应发生偏移
	private boolean mFirstCreate = true;
	private boolean mSafe = false;
	private static final int DRAW_NORMAL = 0x1;
	private static final int DRAW_ENTER = 0x2;
	private static final int DRAW_LEAVE = 0x3;
	private int mDrawState = DRAW_NORMAL;
	private long mStartTime = 0;
	private float mBeginScale = 1.0f;
	private float mEndScale = 1.0f;
	private int mBeginAlpha = 255;
	private int mEndAlpha = 255;

	private static final int ANIMDURATION = 400;
	private boolean mNeedZoomOut = true; // 是否需要进行缩放动画的标识

	public MaskViewChrismas2(Context context) {
		super(context);
		init();
	}

	public MaskViewChrismas2(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MaskViewChrismas2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setFormat(PixelFormat.TRANSPARENT); // 设置为透明
		mPaint.setAntiAlias(true);
		mDoDrawPaint.setAntiAlias(true);
	}

	private void setLogDisable() {
		Class<? extends SurfaceView> surfaceClass = this.getClass();
		Field debug;
		try {
			debug = surfaceClass.getSuperclass().getDeclaredField("DEBUG");
			debug.setAccessible(true);
			boolean isEnable = debug.getBoolean(this);
			if (isEnable) {
				debug.setBoolean(this, false);
			}
			Field localLOGV = surfaceClass.getSuperclass()
					.getField("localLOGV");
			localLOGV.setAccessible(true);
			boolean localLOGVbool = localLOGV.getBoolean(this);
			if (localLOGVbool) {
				localLOGV.setBoolean(this, false);
			}
		} catch (Exception e) {
		}
	}

	private void initData(int width, int height) {
		boolean sendBroadCast = false;
		if (mFirstCreate) {
			DrawUtils.setScreenViewSize(width, height);
			// 设置绘画工具的值
			DrawUtils.resetDensity(getContext());
			// 初始化数据控制器
			// long current = System.currentTimeMillis();
			mDataControl = CoverDataControl.getInstance(getContext());
			mCoverBean = mDataControl.getCoverBean();
			mViewControl = new ViewControl(getContext(),
					DrawUtils.getScreenViewWidth(),
					DrawUtils.getScreenViewHeight());
			mViewControl.initMaps(mCoverBean);
			mFirstCreate = false;
			sendBroadCast = true;
			// startThread();
		} else {
			doConfigurationChange(width, height, false);
		}
		if (mViewControl != null) {
			mViewControl.onResume(mCoverBean);
		}
		if (sendBroadCast) {
			// 发广播通知启动罩子层动画
			sendBroadCast(BROADCAST_SHOW_MASKVIEW);
		}
	}

	private void doConfigurationChange(int width, int height,
			boolean backToDefault) {
		DrawUtils.resetDensity(getContext());
		DrawUtils.setScreenViewSize(width, height);
		if (mViewControl != null && mCoverBean != null) {
			mViewControl.setScreenSize(DrawUtils.getScreenViewWidth(),
					DrawUtils.getScreenViewHeight());
			mViewControl.onConfigurationChanged(mCoverBean, backToDefault);
		}
	}

	private void sendBroadCast(String action) {
		Intent intent = new Intent(action);
		getContext().sendBroadcast(intent);
	}

	private void startThread() {
		mIsActive = true;
		if (mSpiritEngineThread != null) {
			mSpiritEngineThread.cleanUp();
			mSpiritEngineThread = null;
		}
		if (mSpiritEngineThread == null) {
			mSpiritEngineThread = new SpiritEngineThread();
		}
		if (mViewControl != null) {
			mSpiritEngineThread.loadMovable(mViewControl);
		}
		mSpiritEngineThread.start();
		if (mDrawEnginer == null) {
			mDrawEnginer = new DrawThread(MASK_DRAW_THREAD);
			mDrawEnginer.start();
		}
		if (mAccelerometerSensor == null) {
			mAccelerometerSensor = new AccelerometerSensor(getContext());
			mAccelerometerSensor.registeObsever(this);
			mAccelerometerSensor.start();
		}
		// if (mAudioSensorer == null) {
		// mAudioSensorer = new AudioSensorer();
		// mAudioSensorer.registeObsever(this);
		// mAudioSensorer.start();
		// }
	}

	private void stopThread() {
		mIsActive = false;
//		if (mAudioSensorer != null) {
//			mAudioSensorer.unRegisteObsever(this);
//			mAudioSensorer.stop();
//			mAudioSensorer = null;
//		}
		if (mSpiritEngineThread != null) {
			mSpiritEngineThread.removeMovable(mViewControl);
			mSpiritEngineThread.onStop();
			mSpiritEngineThread = null;
		}
		if (mAccelerometerSensor != null) {
			mAccelerometerSensor.unRegisteObsever(this);
			mAccelerometerSensor.stop();
			mAccelerometerSensor = null;
		}
		if (mViewControl != null) {
			mViewControl.setBroken(true);
		}
		if (mDrawEnginer != null) {
			mDrawEnginer.interrupt();
			mDrawEnginer = null;
		}
	}

	/**
	 * 
	 * <br>
	 * 类描述:绘制线程 <br>
	 * 功能详细描述:
	 * 
	 * @author guoyiqing
	 * @date [2012-10-20]
	 */
	public class DrawThread extends Thread {

		public DrawThread(String threadName) {
			super(threadName);
		}

		@Override
		public void run() {
			try {
				while (!Thread.interrupted() && mIsActive) {
					Canvas canvas = null;
					try {
						canvas = mHolder.lockCanvas();
						synchronized (mHolder) {
							canvas.save();
							if (mOffsetY > 0) {
								canvas.translate(0, mOffsetY);
							}
							drawZoom(canvas); // 进入退出罩子层特效
							canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
							mViewControl.doDraw(mCamera, mMatrix, canvas,
									mPaint);
							canvas.restore();
						}
					} catch (Exception e) {
						// e.printStackTrace();
					} finally {
						if (canvas != null) {
							mHolder.unlockCanvasAndPost(canvas);
						}
					}
					Thread.sleep(DRAW_THREAD_UPFATE_INTERVAL);
				}
			} catch (InterruptedException e) {
			}
			super.run();
		}
	}

	private void drawZoom(Canvas canvas) {
		if (mDrawState != DRAW_NORMAL) {
			float t = Math.min(1, getDrawTime() / (float) ANIMDURATION);
			final float scale = DrawUtils.easeOut(mBeginScale, mEndScale, t);
			final int alpha = (int) DrawUtils
					.easeOut(mBeginAlpha, mEndAlpha, t);
			mPaint.setAlpha(alpha);
			canvas.scale(scale, scale, DrawUtils.getScreenViewWidth() / 2,
					DrawUtils.getScreenViewHeight() / 2);
			if (t >= 1) {
				if (mDrawState == DRAW_LEAVE) {
					sendBroadCast(BROADCAST_HIDE_MASKVIEW);
					stopThread();
				}
				mDrawState = DRAW_NORMAL;
				mStartTime = 0;
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = false;
		try {
			if (mViewControl != null) {
				ret = ret | mViewControl.onTouchEvent(event);
			}
		} catch (Exception e) {
		}
		return ret;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			Rect rect = holder.getSurfaceFrame();
			if (rect != null) {
				final int width = rect.width();
				final int height = rect.height();
				// DrawUtils.setScreenViewSize(width, height);
				// // 设置绘画工具的值
				// DrawUtils.resetDensity(getContext());
				// if (mSafe) {
				initData(width, height);
				// }
			}
			setLogDisable();
		} catch (Exception e) {
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		try {
			if (mFirstCreate) {
				DrawUtils.setScreenViewSize(width, height);
			} else if (width != DrawUtils.getScreenViewWidth()
					&& height != DrawUtils.getScreenViewHeight()) {
				// Log.v("xiaojun",
				// " doConfigurationChange in surfaceChanged ");
				doConfigurationChange(width, height, true);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		try {
			mIsActive = false;
			stopThread();
			cleanUp();
		} catch (Exception e) {
		}
	}

	@Override
	public void onDataChange(boolean isAsync, int sensor_type, BaseState state) {
		try {
			switch (sensor_type) {
			case ISensorType.Pysical.SENSOR_TYPE_AUDIO:
				if (state instanceof AudioDataState) {
					mAudioDataState = (AudioDataState) state;
				}
				break;
			case ISensorType.Pysical.SENSOR_TYPE_ACCELEROMETER:
				if (state instanceof AccelerometerDataState) {
					mAccelerometerState = (AccelerometerDataState) state;
					float speed = Math.abs(mAccelerometerState
							.getMaxChangeSpeed());

					if (mAccelerometerState.getNotifyType() == AccelerometerDataState.NOTIFY_TYPE_GRAVITY_CHANGE) {
						if (mViewControl != null) {
							mViewControl
									.handleGravityChange(mAccelerometerState);
						}
					} else {
						if (mViewControl != null) {
							mViewControl.handShake(speed);
						}
					}
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
		}

	}

	public void onBegin() {
	}

	public void onEnd() {
	}

	public boolean isActive() {
		return mIsActive;
	}

	/**
	 * 数据清理
	 */
	private void cleanUp() {
		try {
			if (mViewControl != null) {
				mViewControl.cleanUp();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onCreate() {

	}

	@Override
	public void onPause() {
		try {
			stopThread();
		} catch (Exception e) {
		}
	}

	@Override
	public void onStop() {
		try {
			if (mIsActive) {
				mDrawState = DRAW_LEAVE;
				prepareZoom();
			} else {
				mNeedZoomOut = true;
				sendBroadCast(BROADCAST_HIDE_MASKVIEW);
			}
		} catch (Exception e) {
		}
		// stopThread();
	}

	@Override
	public void onResume() {
		try {
			if (mSafe) {
				if (mNeedZoomOut) {
					mDrawState = DRAW_ENTER;
					prepareZoom();
				}
				startThread();
				if (mViewControl != null) {
					mViewControl.setBroken(false);
				}
			}
		} catch (Exception e) {
		}
		// startThread();
	}

	@Override
	public void onDestroy() {
		try {
			if (mViewControl != null) {
				mViewControl.destroy();
			}
			if (mDataControl != null) {
				mDataControl.cleanUp();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onStatusBarChange(int height) {
		mOffsetY = height;
	}

	@Override
	public void onWakeUp(Object o) {
		try {
			if (mWakeUpGuard != null) {
				if (mWakeUpGuard.isSafe(o)) {
					mSafe = true;
					mDrawState = DRAW_ENTER;
					prepareZoom();
					startThread();
					if (mViewControl != null) {
						mViewControl.setBroken(false);
					}
				}
			}
		} catch (Exception e) {
		}
	} // end onWakeUp

	/*
	 * 获取绘制的当前时间
	 */
	private long getDrawTime() {
		int currentTime = 0;
		if (mStartTime == 0) {
			mStartTime = SystemClock.uptimeMillis();
		} else {
			currentTime = (int) (SystemClock.uptimeMillis() - mStartTime);
		}
		return currentTime;
	}

	private void prepareZoom() {
		mStartTime = 0;
		if (mDrawState == DRAW_ENTER) {
			mBeginScale = 2.0f;
			mEndScale = 1.0f;
			mBeginAlpha = 0;
			mEndAlpha = 255;
			mNeedZoomOut = false;
		} else if (mDrawState == DRAW_LEAVE) {
			mBeginScale = 1.0f;
			mEndScale = 2.0f;
			mBeginAlpha = 255;
			mEndAlpha = 0;
			mNeedZoomOut = true;
		}
	} // end prepareZoom

	@Override
	public void onReadVersion(String version) {
		if (mWakeUpGuard != null) {
			mWakeUpGuard.getLauncherMaskVersion(version);
		}
	}

	@Override
	public void doDraw(Canvas canvas) {
		try {
			if (mOffsetY > 0) {
				canvas.translate(0, mOffsetY);
			}
			drawZoom(canvas); // 进入退出罩子层特效
			mViewControl.doDraw(mCamera, mMatrix, canvas, mDoDrawPaint);
		} catch (Exception e) {
		}
	} // end doDraw
}
