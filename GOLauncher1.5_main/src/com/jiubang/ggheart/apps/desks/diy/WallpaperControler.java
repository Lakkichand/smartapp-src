package com.jiubang.ggheart.apps.desks.diy;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.view.Window;
import com.go.util.device.Machine;
import com.go.util.graphics.BitmapUtility;
import com.go.util.log.LogConstants;
import com.go.util.window.WindowControl;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Workspace;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.info.ScreenSettingInfo;
import com.jiubang.ggheart.data.info.ThemeSettingInfo;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * 类描述:壁纸控制器 功能详细描述:
 */
// CHECKSTYLE:OFF
public class WallpaperControler {
	private static WallpaperControler sInstance;
	
	/**
	 * 单屏壁纸可能比屏幕宽度大的值
	 */
	private static final int SINGLE_WALLPAPER_SCREEN_SLOP = 10;
	
	/**
	 * int 当前总屏幕数
	 */
	public final static String FIELD_SCREEN_NUMS = "screen_nums";

	/**
	 * int 移动比率
	 */
	public final static String FIELD_SCROLL_RANGE = "range";

	/**
	 * int X方向位移
	 */
	public final static String FIELD_SCROLL_X = "scroll_x";

	/**
	 * 将事件传递给wallpapermanager所需的参数
	 */
	public final static String FIELD_COMMAND_X = "x";

	/**
	 * 将事件传递给wallpapermanager所需的参数
	 */
	public final static String FIELD_COMMAND_Y = "y";

	public final static String COMMAND = "command";
	public final static String COMMAND_TAP = "android.wallpaper.tap";
	public final static String COMMAND_DROP = "android.home.drop";

	private static final String TAG = "WallpaperControler";
	// 壁纸模式发生变更的action(对应主题2.0的壁纸发生变更)
	private static final String SUPERTHEME_WALLPAPER_CHANGE_ACTION = "com.gau.go.launcherex.supertheme.wallpaperchangeaction";
	// 模式变更的key
	private static final String CHANGE_MODE_KEY = "changemodekey";
	// 壁纸名字变更的key
	private static final String CHANGE_NAME_KEY = "changenamekey";
	// 壁纸模式发生变更的延迟时间
	private static final int SUPERTHEME_WALLPAPER_CHANGE_DELAY = 1400;
	private static final int SUPERTHEME_WALLPAPER_UPDATE_DELAY = 5000;
	private InputStream mInputStream = null;
	
	/**
	 * 
	 * 类描述:壁纸改变监听接口
	 * 
	 * @author jiangxuwen
	 * @date [2012-9-24]
	 */
	public interface IWallpaperChangeListener {
		// 相应壁纸更换事件
		void onWallpaperChange(Drawable wallpaperDrawable);
	}

	private final WallpaperManager mWallPaperManager;
	private Drawable mWallpaperDrawable = null;
	private WallpaperIntentReceiver mWallpaperReceiver;
	// 动态壁纸的标识
	private boolean mLwpSupport;
	// 交由系统绘制的标识
	private boolean mSysDrawSuport;
	// 支持透明通知栏的标识
	private boolean mTsbSupport;
	private WeakReference<Activity> mActivityReference;
	private IWallpaperChangeListener mListener;
	// 是否在启动了壁纸切换
	public static boolean sIsWallpaperSetting = false;
	// GoLauncher进入stop状态的时间,如果没有进入stop则记录的是pause的时间
	public static long sStopedTime;

	
	// 由GOLauncher进入stop状态开始，3000ms视为其他launcher进行壁纸修改的最小时间间隔
	// private static long CHAGNE_GAP = 3000;
	// 动态壁纸的包名
	private String mLwpPkgName;
	private int mLastWallPaperDrawableWidth;
	private Drawable mLastWallPaperDrawable;
	private static final int MSG_UPDATE_WALLPAPER = 1;
	private static final int MSG_CHANGE_WALLPAPER_ITEM = 2;
	private static final int MSG_UPDATE_WALLPAPER_TYPE = 3;
	private static final int MSG_SET_WALLPAPER_INPUTSTREAM = 4;
	private Runnable mUpdateWallpaperRunnable;
	private boolean mSettingSupperWallpaper;
	private long mLastSupperTime;
	private byte[] mSupperWallpaperLock = new byte[0];
	
	private WallpaperControler(Activity activity, IWallpaperChangeListener listener) {
		mWallPaperManager = WallpaperManager.getInstance(activity);
		mActivityReference = new WeakReference<Activity>(activity);
		mUpdateWallpaperRunnable = new UpdateWallpaperRunnable();
		mListener = listener;
	}
	
	public static WallpaperControler buildInstance(Activity activity,
			IWallpaperChangeListener listener) {
		sInstance = new WallpaperControler(activity, listener);
		return sInstance;
	}

	public static WallpaperControler getInstance() {
		return sInstance;
	}

	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_WALLPAPER :
				invokeWallPaperChange(); // 通知
				mSettingSupperWallpaper = false;
				break;
				
			case MSG_CHANGE_WALLPAPER_ITEM : 
				// 通知添加模块添加壁纸设置选项发生变更
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
						IDiyMsgIds.SCREEN_EDIT_UPDATE_WALLPAPER_ITEMS, 0, null, null);
				break;
				
			case MSG_UPDATE_WALLPAPER_TYPE :
				setWindowBackground(mSysDrawSuport); // 更新壁纸
				break;
			
			case MSG_SET_WALLPAPER_INPUTSTREAM :
				if (mInputStream == null) {
					return ;
				}
				try {
					try {
						mWallPaperManager.setStream(mInputStream);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						mInputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mInputStream = null;
				}
				break;
				
			default:
				break;
			}
		}
	};
	
	public void registWallpaperReceiver() {
		Activity activity = mActivityReference.get();
		if (activity != null) {
			// 注册壁纸更新事件
			mWallpaperReceiver = new WallpaperIntentReceiver(this);
			IntentFilter wallpaperfilter = new IntentFilter();
			wallpaperfilter.addAction(Intent.ACTION_WALLPAPER_CHANGED);
			wallpaperfilter.addAction(SUPERTHEME_WALLPAPER_CHANGE_ACTION);
			activity.registerReceiver(mWallpaperReceiver, wallpaperfilter);
		}
	}

	public static Bundle createWallpaperOffsetBundle(int count, int scrollX, int range) {
		Bundle dataBundle = new Bundle();
		dataBundle.putInt(WallpaperControler.FIELD_SCREEN_NUMS, count);
		dataBundle.putInt(WallpaperControler.FIELD_SCROLL_X, scrollX);
		dataBundle.putInt(WallpaperControler.FIELD_SCROLL_RANGE, range);
		return dataBundle;
	}

	public static Bundle createWallpaperCommandBundle(int x, int y) {
		Bundle bundle = new Bundle();
		bundle.putString(WallpaperControler.COMMAND, WallpaperControler.COMMAND_TAP);
		bundle.putInt(WallpaperControler.FIELD_COMMAND_X, x);
		bundle.putInt(WallpaperControler.FIELD_COMMAND_Y, y);
		return bundle;
	}

	public void unRegistWallpaperReceiver() {
		Activity activity = mActivityReference.get();
		if (activity != null) {
			try {
				activity.unregisterReceiver(mWallpaperReceiver);
			} catch (Exception e) {
				e.printStackTrace();
				Log.i(LogConstants.HEART_TAG, "unregist wallpaper receiver fail!!");
			}
			activity = null;
		}
	}

	public void setWallpaperInBackground(Context context, Resources resources, int resId) {
		if (mSettingSupperWallpaper) {
			if (System.currentTimeMillis() - mLastSupperTime < SUPERTHEME_WALLPAPER_CHANGE_DELAY) {
				return;
			}
		}
		mSettingSupperWallpaper = true;
		Thread thread = new Thread(new SetWallpaperRunnable(context, resources, resId));
		thread.setName("SUPERTHEME_SET_WALLPAPER");
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
		mLastSupperTime = System.currentTimeMillis();
	}
	
	private class SetWallpaperRunnable implements Runnable {
		private Context mContext;
		private Resources mResources;
		private int mId;

		public SetWallpaperRunnable(Context context, Resources resources,
				int resId) {
			mContext = context;
			mResources = resources;
			mId = resId;
		}
		@Override
		public void run() {
			setWallpaper(mContext, mResources, mId);
		}
	}
	
	private class UpdateWallpaperRunnable implements Runnable {
		@Override
		public void run() {
			boolean needPrepare = false;
			if (mTsbSupport) {
				mWallpaperDrawable = null; // 动态壁纸
				mSysDrawSuport = true;
			} else {
				synchronized (mSupperWallpaperLock) {
					try {
						mWallpaperDrawable = null;
						mWallpaperDrawable = mWallPaperManager.getDrawable();
						Resources resources = GOLauncherApp.getContext()
								.getResources();
						int screenWidth = resources.getDisplayMetrics().widthPixels;
						int screenHeight = resources.getDisplayMetrics().heightPixels;
						boolean needReturen = preventAmplifyWallpaper(
								resources, screenWidth);
						if (needReturen) {
							mSettingSupperWallpaper = false;
							return;
						}
						if (mWallpaperDrawable != null) {
							int bmpWidth = mWallpaperDrawable
									.getIntrinsicWidth();
							int bmpHeight = mWallpaperDrawable
									.getIntrinsicHeight();
							if (bmpHeight < screenHeight
									|| bmpWidth < screenWidth) {
								mWallpaperDrawable = preventBlackScreen(mWallpaperDrawable);
							}
						}
						needPrepare = true;
					} catch (OutOfMemoryError e) {
						Log.e(TAG, "outOfMemory in updateWallpaper(boolean)"
								+ e.getMessage());
						OutOfMemoryHandler.handle();
						resetData(); // 交由系统去处理
					} catch (Exception e) {
						mSettingSupperWallpaper = false;
						Log.e(TAG, "updateWallPaper()" + e.getMessage());
					}
					mSysDrawSuport = false;
					if (null == mWallpaperDrawable) { // 当获取壁纸失败时，交由系统自己处理
						mSysDrawSuport = true;
					}
					mHandler.sendEmptyMessage(MSG_UPDATE_WALLPAPER_TYPE);

					if (needPrepare && null != mWallpaperDrawable) { // 逻辑预处理
						mWallpaperDrawable = prepareOrientationChange(mWallpaperDrawable);
					}
					mHandler.sendEmptyMessage(MSG_UPDATE_WALLPAPER);
				} // end synchronized
			} // end else
		} // end run
	}
	
	// 异步获取壁纸，再同步到UI线程刷新
	public void updateWallpaperInBackground() {
		Thread thread = new Thread(mUpdateWallpaperRunnable);
		thread.setName("SUPERTHEME_UPDATE_WALLPAPER");
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	public void updateWallpaper(boolean fromIntentReceiver) {
		boolean needPrepare = false;
		boolean hasException = false;
		Object wallpaperInfo = null;
		try {
			Method getWallpaperInfo = mWallPaperManager.getClass().getMethod("getWallpaperInfo");
			wallpaperInfo = getWallpaperInfo.invoke(mWallPaperManager);
		} catch (Throwable e) {
			hasException = true;
		}
		final boolean isLiveWallpaper = !hasException && wallpaperInfo != null ? true : false;
		boolean livecChange = false;
		if (isLiveWallpaper && wallpaperInfo instanceof WallpaperInfo) {
			String newPkgName = ((WallpaperInfo) wallpaperInfo).getPackageName();
			if (!newPkgName.equals(mLwpPkgName)) {
				mLwpPkgName = newPkgName;
				livecChange = true;
			}
		}
		if (isLiveWallpaper != mLwpSupport || livecChange) { // 壁纸动/静切换或者是动态壁纸切换动态壁纸，均需要通知更新
			mLwpSupport = isLiveWallpaper;
			if (Workspace.getLayoutScale() < 1.0f) { // 如果壁纸发生（动/静）变更并且桌面是添加状态
				// 通知添加模块添加壁纸设置选项发生变更
				GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_EDIT_BOX_FRAME,
						IDiyMsgIds.SCREEN_EDIT_UPDATE_WALLPAPER_ITEMS, 0, null, null);
			}
		}
		synchronized (mSupperWallpaperLock) {
			if (mTsbSupport || isLiveWallpaper) {
				mWallpaperDrawable = null; // 动态壁纸
				mSysDrawSuport = true;
			} else {
				if (fromIntentReceiver || mWallpaperDrawable == null) {
					try {
						mWallpaperDrawable = null;
						mWallpaperDrawable = mWallPaperManager.getDrawable();
						Resources resources = GOLauncherApp.getContext().getResources();
						int screenWidth = resources.getDisplayMetrics().widthPixels;
						int screenHeight = resources.getDisplayMetrics().heightPixels;
						boolean needReturen = preventAmplifyWallpaper(resources, screenWidth);
						if (needReturen) {
							return;
						}
						if (mWallpaperDrawable != null) {
							int bmpWidth = mWallpaperDrawable.getIntrinsicWidth();
							int bmpHeight = mWallpaperDrawable.getIntrinsicHeight();
							if (bmpHeight < screenHeight || bmpWidth < screenWidth) {
								mWallpaperDrawable = preventBlackScreen(mWallpaperDrawable);
							}
						}
						needPrepare = true;
					} catch (OutOfMemoryError e) {
						Log.e(TAG, "outOfMemory in updateWallpaper(boolean)" + e.getMessage());
						OutOfMemoryHandler.handle();
						resetData(); // 交由系统去处理
					} catch (Exception e) {
						Log.e(TAG, "updateWallPaper()" + e.getMessage());
					}
				}
				// mLwpSupport = false;
				mSysDrawSuport = false;
				if (null == mWallpaperDrawable) { // 当获取壁纸失败时，交由系统自己处理
					// mLwpSupport = true;
					mSysDrawSuport = true;
				}
			}
			setWindowBackground(mSysDrawSuport); // 更新壁纸
			if (needPrepare && null != mWallpaperDrawable) { // 逻辑预处理
				mWallpaperDrawable = prepareOrientationChange(mWallpaperDrawable);
			}
			invokeWallPaperChange(); // 通知
			if (needPrepare && mWallpaperDrawable != null && sIsWallpaperSetting) { // 调整壁纸宽度
				sIsWallpaperSetting = false;
				adjustWallpaperDimension(mWallPaperManager);
			}
			
		} // end synchronized
	}

	private boolean isRecycled() {
		if (mLastWallPaperDrawable != null && mLastWallPaperDrawable instanceof BitmapDrawable
				&& ((BitmapDrawable) mLastWallPaperDrawable).getBitmap().isRecycled()) {
			return true;
		}
		return false;
	}
	
	/**
	 * 防止外界app对壁纸进行了更改
	 * 
	 * @param resources
	 * @param screenWidth
	 * @return
	 */
	private boolean preventAmplifyWallpaper(Resources resources, int screenWidth) {
		if (mWallpaperDrawable != null) {
			mSysDrawSuport = false;
			if (mWallpaperDrawable.getIntrinsicWidth() >= screenWidth) {
				if (mLastWallPaperDrawableWidth == 0) {
					mLastWallPaperDrawableWidth = mWallpaperDrawable.getIntrinsicWidth();
				} else if (!GOLauncherApp.getSettingControler().getScreenSettingInfo().mWallpaperScroll
						&& mWallpaperDrawable.getIntrinsicWidth() != mLastWallPaperDrawableWidth
						&& (mWallpaperDrawable.getIntrinsicWidth() > mLastWallPaperDrawableWidth || mWallpaperDrawable
								.getIntrinsicWidth() > screenWidth + SINGLE_WALLPAPER_SCREEN_SLOP)
						&& mLastWallPaperDrawable != null && !isRecycled()) {
					int currentWallPaperWidth = mWallpaperDrawable.getIntrinsicWidth();
					mWallpaperDrawable = mLastWallPaperDrawable;
					if (resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
						mWallpaperDrawable = prepareOrientationChange(mWallpaperDrawable);
					}
					setWindowBackground(mSysDrawSuport);
					invokeWallPaperChange();
					mLastWallPaperDrawableWidth = currentWallPaperWidth;
					return true;
				}
				mLastWallPaperDrawable = mWallpaperDrawable;
				mLastWallPaperDrawableWidth = mWallpaperDrawable.getIntrinsicWidth();
			}
		} else {
			mSysDrawSuport = true;
			setWindowBackground(mSysDrawSuport);
			resetData();
		}
		return false;
	}

	private void resetData() {
		mLastWallPaperDrawableWidth = 0;
		mLastWallPaperDrawable = null;
		mWallpaperDrawable = null;
		mSettingSupperWallpaper = false;
	}

	private void invokeWallPaperChange() {
		if (mListener != null) {
			mListener.onWallpaperChange(mWallpaperDrawable);
		}
	}

	/**
	 * 避免壁纸很小出现黑屏的情况发生
	 * 
	 * @param wallPaper
	 * @return
	 */
	private BitmapDrawable preventBlackScreen(Drawable wallPaper) {
		Resources resources = GOLauncherApp.getContext().getResources();
		int screenW = resources.getDisplayMetrics().widthPixels;
		int screenH = resources.getDisplayMetrics().heightPixels;
		if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			if (screenW > screenH) {
				int temp = screenW;
				screenW = screenH;
				screenH = temp;
			}
		} else {
			if (screenW < screenH) {
				int temp = screenW;
				screenW = screenH;
				screenH = temp;
			}
		}
		boolean isWallpaperScroll = GOLauncherApp.getSettingControler().getScreenSettingInfo().mWallpaperScroll;
		final int screenSpan = isWallpaperScroll ? WallpaperDensityUtil.WALLPAPER_SCREENS_SPAN_WIDTH_2
				: WallpaperDensityUtil.WALLPAPER_SCREENS_SPAN_WIDTH_1;
		BitmapDrawable newWallDrawable = WindowControl.prepareWallpaper(
				(BitmapDrawable) wallPaper, screenW * screenSpan, screenH, resources);
		return newWallDrawable;

	}

	/**
	 * 预防竖屏切横屏时壁纸不全问题
	 * 
	 * @param walllPaper
	 * @return
	 */
	private Drawable prepareOrientationChange(Drawable walllPaper) {
		Drawable ret = walllPaper;
		Activity activity = mActivityReference.get();
		if (null != activity
				&& activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			int width = walllPaper.getIntrinsicWidth();
			int height = walllPaper.getIntrinsicHeight();
			int screenW = GoLauncher.getScreenWidth();
			int screenH = GoLauncher.getScreenHeight();
			if (screenW < screenH) {
				int temp = screenW;
				screenW = screenH;
				screenH = temp;
			}
			if (width >= screenW && height >= screenH) {
				return ret;
			}
			float wScale = (float) screenW / (float) width;
			float hScale = (float) screenH / (float) height;
			float scale = wScale > hScale ? wScale : hScale;
			int w = (int) (width * scale);
			int h = (int) (height * scale);
			try {
				Drawable newWallpaper = BitmapUtility.zoomDrawable(activity, walllPaper, w, h);
				ret = newWallpaper;
			} catch (OutOfMemoryError e) {
				Log.e(TAG,
						"发生了内存溢出 in prepareOrientationChange(Drawable),壁纸交给系统处理" + e.getMessage());
			} catch (Exception e) {
				Log.e(TAG, "prepareOrientationChange" + e.getMessage());
			}
		}
		return ret;
	}

	private void setWindowBackground(boolean lwp) {
		Activity activity = mActivityReference.get();
		if (activity != null) {
			boolean hqDrawing = false;
			ThemeSettingInfo info = GOLauncherApp.getSettingControler().getThemeSettingInfo();
			if (info != null) {
				hqDrawing = info.mHighQualityDrawing;
				info = null;
			}

			final Window window = activity.getWindow();
			if (!lwp) {
				window.setBackgroundDrawable(null);
				if (hqDrawing) {
					window.setFormat(PixelFormat.RGBA_8888);
				} else {
					// window.setFormat(PixelFormat.RGBX_8888);//24位，不带Alpha通道
					window.setFormat(PixelFormat.RGB_565);
				}
			} else {
				window.setBackgroundDrawable(new ColorDrawable(0));
				window.setFormat(PixelFormat.TRANSPARENT);
			}
			activity = null;
		}
	}

	public void sendWallpaperCommand(View window, Bundle bundle) {
		if (window == null || bundle == null) {
			return;
		}

		final int x = bundle.getInt(FIELD_COMMAND_X);
		final int y = bundle.getInt(FIELD_COMMAND_Y);
		final String command = bundle.getString(COMMAND);
		sendWallpaperCommand(window, command, x, y, 0, null);
	}

	public void sendWallpaperCommand(View window, String action, int x, int y, int z, Bundle extras) {
		if (mSysDrawSuport) {
			if (window != null) {
				try {
					Method sendWallpaperCommand = mWallPaperManager.getClass().getMethod(
							"sendWallpaperCommand", IBinder.class, String.class, int.class,
							int.class, int.class, Bundle.class);
					sendWallpaperCommand.invoke(mWallPaperManager, window.getWindowToken(), action,
							x, y, z, extras);
				} catch (Exception e) {
					Log.i(LogConstants.HEART_TAG, "cannot send wall paper command");
				}
			}
		}
	}

	public void updateWallpaperOffset(View window, Bundle bundle) {
		if (window == null || bundle == null) {
			return;
		}

		try {
			int nums = bundle.getInt(FIELD_SCREEN_NUMS);
			int range = bundle.getInt(FIELD_SCROLL_RANGE);
			int scrollX = bundle.getInt(FIELD_SCROLL_X);
			if (nums > 0 && mSysDrawSuport) {
				updateWallpaperOffset(window, nums, scrollX, range);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateWallpaperOffset(View window, int nums, int scrollX, int range) {
		if (!mSysDrawSuport || window == null) {
			return;
		}

		try {
			Method setWallpaperOffsetSteps = mWallPaperManager.getClass().getMethod(
					"setWallpaperOffsetSteps", float.class, float.class);
			setWallpaperOffsetSteps.invoke(mWallPaperManager, 1.0f / (nums - 1), 0);
			mWallPaperManager.setWallpaperOffsets(window.getWindowToken(), scrollX
					/ (float) range, 0);
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, e.toString());
		}
	}

	/**
	 * 
	 * 类描述:壁纸更改接受广播 功能详细描述:
	 * 
	 */
	private static class WallpaperIntentReceiver extends BroadcastReceiver {
		private WeakReference<WallpaperControler> mControlReference;

		WallpaperIntentReceiver(WallpaperControler controler) {
			mControlReference = new WeakReference<WallpaperControler>(controler);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mControlReference != null) {
				final WallpaperControler controler = mControlReference.get();
				if (controler != null) {
					String action = intent.getAction();
					// 壁纸变更消息
					if (Intent.ACTION_WALLPAPER_CHANGED.equals(action)) {
						controler.mHandler.postDelayed(new Runnable() { // post到UI线程中去做，receive中不能做耗时操作
							@Override
							public void run() {
								if (controler.mSettingSupperWallpaper && 
										System.currentTimeMillis() - controler.mLastSupperTime < SUPERTHEME_WALLPAPER_UPDATE_DELAY) {
									controler.updateWallpaperInBackground();
								} else {
									controler.mSettingSupperWallpaper = false;
									controler.updateWallpaper(true);
								}
							}
						}, 500);
					} // end ACTION_WALLPAPER_CHANGED
					// 超级主题的壁纸变更消息
					else if (SUPERTHEME_WALLPAPER_CHANGE_ACTION.equals(action)) {
						final String name = intent.getStringExtra(CHANGE_NAME_KEY);
						if (name != null) {
							final Context contextTemp = context;
							controler.mHandler.postDelayed(new Runnable() {
								@Override
								public void run() {
									controler.setSuperThemeWallpaper(contextTemp, name);
								}
							}, /*SUPERTHEME_WALLPAPER_CHANGE_DELAY*/20);
						} // end if name
					} // end SUPERTHEME_WALLPAPER_CHANGE_ACTION
				}
			}
		}
	}

	// 设置超级主题的壁纸
	private void setSuperThemeWallpaper(Context context, String resName) {
		try {
			ThemeInfoBean infoBean = ThemeManager.getInstance(context).getCurThemeInfoBean();
			if (infoBean != null) {
				final String pgkName = infoBean.getPackageName();
				Resources resources = GOLauncherApp.getContext().getPackageManager()
						.getResourcesForApplication(pgkName);
				final int resId = resources.getIdentifier(resName, "drawable", pgkName);
				setWallpaperInBackground(context, resources, resId);
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
	} // end setSuperThemeWallpaper
    
	/**
	 * 设置是否支持透明通知栏
	 */
	public void setTransparentStatusbarSupport(boolean bool) {
		mTsbSupport = bool;
		updateWallpaper(true);
	}

	/**
	 * 调整壁纸尺寸的方法，只要是针对壁纸放大的问题（用户当前是可滚，但是选择壁纸时却是单屏，这时候设回为单屏）
	 * 
	 * @param wallpaperManager
	 * @return
	 */
	public static boolean adjustWallpaperDimension(WallpaperManager wallpaperManager) {
		// 是否进行调整的结果
		boolean result = false;
		if (wallpaperManager == null) {
			wallpaperManager = WallpaperManager.getInstance(GOLauncherApp.getContext());
		}
		try {
			Method method = wallpaperManager.getClass().getMethod("getIWallpaperManager");
			Object iWallpaperManager = method.invoke(wallpaperManager);
			// Class IWallpaperManager = iWallpaperManager.getClass();
			Class iWallpaperManagerClass = Class.forName("android.app.IWallpaperManager");

			Field field = wallpaperManager.getClass().getDeclaredField("sGlobals");
			field.setAccessible(true);
			Object globals = field.get(wallpaperManager);

			Class[] arrayOfClass = new Class[] {
					Class.forName("android.app.IWallpaperManagerCallback"), Bundle.class };
			method = iWallpaperManagerClass.getDeclaredMethod("getWallpaper", arrayOfClass);
			method.setAccessible(true);

			Bundle params = new Bundle();
			ParcelFileDescriptor fd = (ParcelFileDescriptor) method.invoke(iWallpaperManager,
					new Object[] { globals, params });

			if (fd != null) {
				final int width = params.getInt("width", 0);
				final int height = params.getInt("height", 0);
				try {
					BitmapFactory.Options options = new BitmapFactory.Options();
					Bitmap bm = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null,
							options);
					final int bitWidth = bm.getWidth();
					final int bitHeight = bm.getHeight();
					GoSettingControler settingControler = GOLauncherApp.getSettingControler();
					ScreenSettingInfo screenSettingInfo = GOLauncherApp.getSettingControler()
							.getScreenSettingInfo();

					if (bitWidth == width / 2 && screenSettingInfo.mWallpaperScroll) {
						screenSettingInfo.mWallpaperScroll = false;
						result = true;
					}
					// else if(bitWidth == width * 2 &&
					// !screenSettingInfo.mWallpaperScroll){
					// screenSettingInfo.mWallpaperScroll = true;
					// result = true;
					// }
					if (result) {
						// 把屏幕是否可滚写进数据库
						settingControler.updateScreenSettingInfo(screenSettingInfo, false);
						WallpaperDensityUtil.setWallpaperDimension(GoLauncher.getContext());
					}
				} catch (OutOfMemoryError e) {
					result = false;
				} finally {
					try {
						fd.close();
					} catch (IOException e) {
						result = false;
					}
				}
			}
		} catch (Throwable e) {
			Log.v("System.out.print", e.toString());
			result = false;
		}
		return result;
	} // end adjustWallpaperDimension

	public static void setWallpaperSetting(boolean value) {
		sIsWallpaperSetting = value;
	}
	
	/**
	 * 设置壁纸
	 * 
	 * @param context
	 *            context
	 * @param resources
	 *            如果是主题包，必须用主题包的resource
	 * @param resId
	 *            图片资源id
	 */
	private void setWallpaper(Context context, Resources resources, int resId) {
		OutOfMemoryHandler.handle();

		if (context == null || resources == null || resId < 0) {
			return;
		}

		boolean bSetOk = false;

		WallpaperManager wpm = null;
		Drawable drb = null;
		BitmapDrawable bdrb = null;
//		InputStream in = null;
		ByteArrayOutputStream out = null;
		try {
			// 获取宽高
			// 竖屏状态
			int screenW = 0;
			int screenH = 0;
			if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
				screenW = resources.getDisplayMetrics().widthPixels;
				screenH = resources.getDisplayMetrics().heightPixels;
			} else {
				screenW = resources.getDisplayMetrics().heightPixels;
				screenH = resources.getDisplayMetrics().widthPixels;
			}

			wpm = (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
			drb = resources.getDrawable(resId);
			{
				// 图片处理
				// 对Lephone的特殊处理
				if (Machine.isLephone()) {
					bdrb = WindowControl.prepareWallpaper((BitmapDrawable) drb, screenH, screenH, resources);
				} else {
					bdrb = WindowControl.prepareWallpaper((BitmapDrawable) drb, screenW * WindowControl.WALLPAPER_SCREENS_SPAN,
							screenH, resources);
				}
				out = new ByteArrayOutputStream();
				boolean b = bdrb.getBitmap().compress(CompressFormat.JPEG, 100, out);
				if (b) {
					mInputStream = new BufferedInputStream(new ByteArrayInputStream(out.toByteArray()));
					out.close();
					out = null;
					
				} else {
					mInputStream = resources.openRawResource(resId);
				}
				bSetOk = true;
				mHandler.sendEmptyMessage(MSG_SET_WALLPAPER_INPUTSTREAM);
			}
		} catch (OutOfMemoryError e) {
			// 内存爆掉，不进行图片处理
			OutOfMemoryHandler.handle();

			try {
				if (wpm != null) {
					mInputStream = resources.openRawResource(resId);
					mHandler.sendEmptyMessage(MSG_SET_WALLPAPER_INPUTSTREAM);
				}
				bSetOk = true;
			} catch (Throwable e2) {
				Log.i(LogConstants.HEART_TAG, "fail to re-change wallpaper " + e2);
			}
		} catch (IOException e) {
			Log.i(LogConstants.HEART_TAG, "fail to change wallpaper " + e);
		} catch (Exception e) {
			Log.i(LogConstants.HEART_TAG, "fail to change wallpaper " + e);
		} finally {
			if (null != out) {
				try {
					out.close();
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}

	}
	
	public boolean isLiveWallpaper(){
		boolean result = false;
		boolean hasException = false;
		Object wallpaperInfo = null;
		try {
			Method getWallpaperInfo = mWallPaperManager.getClass().getMethod("getWallpaperInfo");
			wallpaperInfo = getWallpaperInfo.invoke(mWallPaperManager);
		} catch (Throwable e) {
			hasException = true;
		}

		if (!hasException && wallpaperInfo != null) {
			// 动态壁纸
			mWallpaperDrawable = null;
			result = true;
		} else {
			if (mWallpaperDrawable == null) {
				try {
					mWallpaperDrawable = null;
					mWallpaperDrawable = mWallPaperManager.getDrawable();
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					// 交由系统去处理
					mWallpaperDrawable = null;
				} catch (Exception e) {
					e.fillInStackTrace();
				}
			}
			result = false;
			// 当获取壁纸失败时，交由系统自己处理
			if (null == mWallpaperDrawable) {
				result = true;
			}
		}
		return result;
	}
}