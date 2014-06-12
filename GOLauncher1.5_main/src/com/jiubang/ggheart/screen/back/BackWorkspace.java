package com.jiubang.ggheart.screen.back;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.go.util.LoadDexUtil;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.StatusBarHandler;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.ThemeInfoBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import com.jiubang.ggheart.screen.back.MiddleMonitor.IMiddleCallback;

/**
 * 屏幕背景的绘制类（包括壁纸和中间层）
 * 
 * @author jiangxuwen
 * 
 */
public class BackWorkspace extends FrameLayout implements IWallpaperDrawer, IMiddleCallback {

	private static final String MIDDLE_MATCH_CODE = "Hello_this_is_MiddleFrame_welcome_you";
	
	private static final String ON_UPDATE_BG_XY = "onUpdateBgXY";
	private static final String ON_UPDATE_OFFSET = "onUpdateOffset";
	private static final String ON_UPDATE_SCREEN = "onUpdateScreen";

	public static final String STATE_ON_CREATE = "onCreate";
	public static final String STATE_ON_RESUME = "onResume";
	public static final String STATE_ON_PAUSE = "onPause";
	public static final String STATE_ON_STOP = "onStop";
	public static final String STATE_ON_DESTROY = "onDestroyed";
	public static final String STATE_ON_WAKEUP = "onWakeUp";
	public static final String STATE_ON_READ_VERSION = "onReadVersion";
	public static final String STATE_ON_STATUSBAR_CHANGE = "onStatusBarChange";

	private View mMiddleView;
	private boolean mIsSurfaceView;
	protected Drawable mBackgroundDrawable;
	protected Bitmap mBitmap;
	protected Paint mPaint;
	private int mBgX;
	private int mBgY;
	private int mOffsetX;
	private int mOffsetY;
	// 以下的方法回调比较频繁，所以以静态方式保存，不需要多次反射来getMethod
	private static Method sMethodUpdateBgXY; // 更新壁纸裁剪位置的方法
	private static Method sMethodUpdateOffset; // 更新场景偏移量的方法
	private static Method sMethodUpdateScreen; // 更新当前屏幕的方法
	private static Method sMethodPause; //
	private static Method sMethodStop; //
	private static Method sMethodDestroy; //
	private static Method sMethodWakeUP; //
	private static Method sMethodResume; //
	private MiddleMonitor mMonitor;
    private CycloidDrawListener mCycloidListener;
    private boolean mUpdateBackground = false;
    private boolean mDrawCycloid = true;
    private int mMiddleScrollExtra;
    private LoadDexUtil mLoadDexUtil = null;
    
	public BackWorkspace(Context context) {
		super(context);
	}

	public BackWorkspace(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BackWorkspace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private void init() {
		mPaint = new Paint();
	}

	private void registerMiddleCallback() {
		if (mMonitor != null) {
			mMonitor.registerCoverCallback(this);
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		init();
	}

	public void setMiddleView(View middleView, boolean isSurfaceview) {
		if (middleView == null) {
			return;
		}
		// 每添加一个中间层时，先把上一个移除
		removeMiddleView();

		mIsSurfaceView = isSurfaceview;
		if (isSurfaceview) {
			BackSurfaceView surfaceView = new BackSurfaceView(getContext());
			surfaceView.setRenderer(middleView);
			mMiddleView = surfaceView;
		} else {
			mMiddleView = middleView;
			// 主题通过此方法进行数据初始化
			doMiddleViewMethod(STATE_ON_WAKEUP, MIDDLE_MATCH_CODE);
		}
		addView(mMiddleView);
		if (mMonitor == null) {
			mMonitor = new MiddleMonitor(getContext());
			registerMiddleCallback();
		}
	}

	public void removeMiddleView() {
		if (mMiddleView != null) {
			onStateMethod(STATE_ON_DESTROY);
			removeView(mMiddleView);
			mMiddleView = null;
		}
		mIsSurfaceView = false;
		if (mMonitor != null) {
			mMonitor.cleanup();
			mMonitor = null;
		}
		cleanUpStaticMethod();
	}

	private void cleanUpStaticMethod() {
		sMethodPause = null;
		sMethodUpdateBgXY = null; 
		sMethodUpdateOffset = null; 
		sMethodUpdateScreen = null; 
		sMethodPause = null; 
		sMethodStop = null; 
		sMethodDestroy = null; 
		sMethodWakeUP = null; 
		sMethodResume = null; 
	}
	/**
	 * 由于surfaceView在桌面控制，所以传进来的是一个view，需要isSurfaceView来确定是否组装surfaceView
	 * 
	 * @param pkgName
	 * @param isSurfaceView
	 */
	public void setMiddleView(String pkgName, boolean isSurfaceView) {
		try {
			View mainView = null;
			Context remoteContext = getContext().createPackageContext(pkgName,
					Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
			final ThemeInfoBean themeInfoBean = ThemeManager.getInstance(getContext()).getCurThemeInfoBean();
			final String[] classDexNames = themeInfoBean.getClassDexNames();
			// 收费主题的处理
			if (Machine.IS_ICS && classDexNames != null && classDexNames.length > 0) {
				Resources resources = remoteContext.getResources();
				final int length = classDexNames.length;
				int[] dexIds = new int[length];
				for (int i = 0; i < length; i++) {
					dexIds[i] = resources.getIdentifier(classDexNames[i], "raw", pkgName);
				}
				final int versionCode = themeInfoBean.getVerId();
				final String viewPath = themeInfoBean.getMiddleViewPath();
				mLoadDexUtil = LoadDexUtil.getInstance(GOLauncherApp.getContext());
				if (mLoadDexUtil != null) {
					mainView = mLoadDexUtil.createDexAppView(pkgName, dexIds, versionCode, viewPath);
				}
			} 
			if (mainView == null) {
				mainView = createAppView(pkgName);
			}
			setMiddleView(mainView, isSurfaceView);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	protected void dispatchDraw(Canvas canvas) {
		if (!mIsSurfaceView) {
			drawBackground(canvas);
			drawCycloidScreen(canvas);
//			canvas.translate(0, -mBgY);
			super.dispatchDraw(canvas);
//			canvas.translate(0, mBgY);
		}
	}

	public void drawBackground(Canvas canvas) {
		if (mBitmap != null && mBitmap.isRecycled()) {
			// 如果背景壁纸被其他应用更改了，图片会失效
			mBitmap = null;
			mBackgroundDrawable = null;
			return;
		}
		if (mBitmap != null) {
			canvas.translate(-mOffsetX, 0);
			canvas.drawBitmap(mBitmap, mBgX, -mBgY, mPaint);
			canvas.translate(mOffsetX, 0);
		} else if (mBackgroundDrawable != null) {
			// mBackgroundDrawable.setAlpha(mPaint.getAlpha());
			canvas.translate(-mOffsetX + mBgX, -mBgY);
			mBackgroundDrawable.draw(canvas);
			canvas.translate(mOffsetX - mBgX, mBgY);
		}
		// updateWallpaperOffset();
	}

	private void drawCycloidScreen(Canvas canvas) {
		if (mCycloidListener == null || !mDrawCycloid) {
			return;
		}
		final int alpha = mCycloidListener.getCurrentAlpha();
		if (alpha != 0) {
			mPaint.setAlpha(alpha);
			int tempX = mBgX;
			int scroll = mCycloidListener.getCycloidScroll();
			mBgX = mCycloidListener.getBackgroundX(scroll);
			drawBackground(canvas);
			mBgX = tempX;
			mPaint.setAlpha(255);
		}
	}
	
	/**
	 * 设置背景内容
	 * 
	 * @param drawable
	 * @param bitmap
	 */
	public void setBackground(Drawable drawable, Bitmap bitmap) {
		if (!mIsSurfaceView) {
			mBackgroundDrawable = drawable;
			mBitmap = bitmap;
			if (Machine.IS_HONEYCOMB) {
				invalidate();
			}
		}
	}

	@Override
	public void updateXY(int x, int y) {
		if (!mIsSurfaceView) {
			mBgX = x;
			// mBgY = y;
		}
		if (mMiddleView != null) {
			doMiddleViewMethod(ON_UPDATE_BG_XY, x, y);
		}
	}

	private View createAppView(String packName) {
		try {
			Context remoteContext = GOLauncherApp.getContext().createPackageContext(packName,
					Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
			LayoutInflater inflater = (LayoutInflater) remoteContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			Resources resources = remoteContext.getResources();
			int resourceId = resources.getIdentifier("middle_root_view", "layout", packName);
			View mainView = inflater.inflate(resourceId, null);
			if (mainView != null /* && mainView instanceof SurfaceView */) {
				// 如果为GLSurfaceView，则为3d主题
				if (mainView instanceof SurfaceView) {
					// ((SurfaceView) mainView).setZOrderOnTop(true);
				}
				return mainView;
			}
		} catch (OutOfMemoryError e) {
			// e.printStackTrace();
			return null;
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}
		return null;
	}

	/**
	 * 桌面状态发生变更，告之中间层进行调整
	 * 
	 * @param stateId
	 *            状态的Id
	 */
	public void onStateMethod(String state) {
		// surfaceView需要额外处理一些线程的机制
		if (mIsSurfaceView) {
			((BackSurfaceView) mMiddleView).onStateMethod(state);
		}
		doMiddleViewMethod(state);
	}

	private void doMiddleViewMethod(String methodName, Object... params) {
		if (mMiddleView == null) {
			return;
		}
		View tempView = mMiddleView;
		if (mIsSurfaceView) {
			tempView = ((BackSurfaceView) mMiddleView).getRenderer();
		}
		try {
			Class tempClass = mMiddleView.getClass();
			Method tempMethod = null;
			if (methodName.equals(ON_UPDATE_BG_XY)) {
				if (sMethodUpdateBgXY == null) {
					sMethodUpdateBgXY = tempClass.getMethod(methodName, Integer.TYPE, Integer.TYPE);
				}
				tempMethod = sMethodUpdateBgXY;
			} else if (methodName.equals(ON_UPDATE_OFFSET)) {
				if (sMethodUpdateOffset == null) {
					sMethodUpdateOffset = tempClass.getMethod(methodName, Integer.TYPE);
				}
				tempMethod = sMethodUpdateOffset;
			} else if (methodName.equals(ON_UPDATE_SCREEN)) {
				if (sMethodUpdateScreen == null) {
					sMethodUpdateScreen = tempClass.getMethod(methodName, Integer.TYPE,
							Integer.TYPE);
				}
				tempMethod = sMethodUpdateScreen;
			} else if (methodName.equals(STATE_ON_PAUSE)) {
				if (sMethodPause == null) {
					sMethodPause = tempClass.getMethod(methodName);
				}
				tempMethod = sMethodPause;
			} else if (methodName.equals(STATE_ON_STOP)) {
				if (sMethodStop == null) {
					sMethodStop = tempClass.getMethod(methodName);
				}
				tempMethod = sMethodStop;
			} else if (methodName.equals(STATE_ON_RESUME)) {
				if (sMethodResume == null) {
					sMethodResume = tempClass.getMethod(methodName);
				}
				tempMethod = sMethodResume;
			} else if (methodName.equals(STATE_ON_DESTROY)) {
				if (sMethodDestroy == null) {
					sMethodDestroy = tempClass.getMethod(methodName);
				}
				tempMethod = sMethodDestroy;
			} else if (methodName.equals(STATE_ON_WAKEUP)) {
				if (sMethodWakeUP == null) {
					sMethodWakeUP = tempClass.getMethod(methodName, Object.class);
				}
				tempMethod = sMethodWakeUP;
			}
			if (tempMethod != null) {
				tempMethod.invoke(tempView, params);
			}
		} catch (Exception e) {
			Log.i("BackWorkspace", "doMiddleViewMethod() has exception = " + e.getMessage());
		}
	} // end doMiddleViewMethod

	@Override
	public void setAlpha(int alpha) {
		if (!mIsSurfaceView) {
			mPaint.setAlpha(alpha);
		}
	}

	@Override
	public Drawable getBackgroundDrawable() {
		return mBackgroundDrawable;
	}

	@Override
	public Bitmap getBackgroundBitmap() {
		return mBitmap;
	}

	@Override
	public void updateOffsetX(int offsetX, boolean drawCycloid) {
		mDrawCycloid = drawCycloid;
		if (!mIsSurfaceView) {
			mOffsetX = offsetX;
			if (Machine.IS_HONEYCOMB) {
				invalidate();
			}
		}
		if (mMiddleView != null) {
			final int offset = mMiddleScrollExtra != 0 ? -mMiddleScrollExtra : -offsetX + mBgX;
			doMiddleViewMethod(ON_UPDATE_OFFSET, offset);
		}
	}

	@Override
	public void updateOffsetY(int offsetY, boolean drawCycloid) {
		if (!mIsSurfaceView) {
			final int backWorkspaceOffsetY = offsetY == 0 ? StatusBarHandler.getStatusbarHeight()
					: 0;
			mOffsetY = backWorkspaceOffsetY;
			mBgY = offsetY;
		}
		if (Machine.IS_HONEYCOMB) {
			invalidate();
		}
	}

	@Override
	public void updateScreen(int newScreen, int oldScreen) {
		if (mMiddleView != null) {
			doMiddleViewMethod(ON_UPDATE_SCREEN, newScreen, oldScreen);
		}
	}

	@Override
	public void doDraw(Canvas canvas, int bgX, int bgY) {
		try {
			if (mBitmap != null) {
				canvas.drawBitmap(mBitmap, bgX, bgY, mPaint);
			} else if (mBackgroundDrawable != null) {
				canvas.translate(bgX, bgY);
				mBackgroundDrawable.draw(canvas);
				canvas.translate(-bgX, -bgY);
			}
			if (!mIsSurfaceView) {
				canvas.translate(0, -mBgY);
				super.dispatchDraw(canvas);
				canvas.translate(0, mBgY);
			}
		} catch (Exception e) {

		}
	} // end doDraw

	public void hideMiddleView() {
		if (mMiddleView != null) {
			mMiddleView.setVisibility(View.INVISIBLE);
		}
	}

	public void showMiddleView() {
		if (mMiddleView != null) {
			mMiddleView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void handleRemoveMiddleView() {
		removeMiddleView();
	}

	@Override
	public void handleHideMiddleView() {
		hideMiddleView();
	}

	@Override
	public void handleShowMiddleView() {
		showMiddleView();
	}

	@Override
	public void setCycloidDrawListener(CycloidDrawListener listener) {
		mCycloidListener = listener;
	}

	@Override
	public void setUpdateBackground(boolean bool) {
		if (mUpdateBackground != bool) {
			mUpdateBackground = bool;
		}
	} // end setDrawBackColor

	@Override
	public boolean needUpdateBackground() {
		return mUpdateBackground;
	}

	@Override
	public void setMiddleScrollEnabled(boolean enable) {
		if (GoLauncher.isPortait() && !enable) {
			mMiddleScrollExtra = DrawUtils.sWidthPixels >> 1;
		} else {
			mMiddleScrollExtra = 0;
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		// TODO Auto-generated method stub
		try {
			super.onRestoreInstanceState(state);
		} catch (Exception e) {
			// TODO: handle exception
			Log.i("BackWorkspace", "onRestoreInstanceState has exception " + e.getMessage());
		}
	}
	
}
