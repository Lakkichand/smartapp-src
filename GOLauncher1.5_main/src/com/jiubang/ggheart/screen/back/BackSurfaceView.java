package com.jiubang.ggheart.screen.back;

import java.lang.reflect.Method;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * 
 * @author jiangxuwen
 * 
 */
public class BackSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	private BackSurfaceThread mThread;
	private View mRenderer;

	public static final String METHOD_ON_SURFACE_CREATED = "surfaceCreated";
	public static final String METHOD_ON_SURFACE_CHANGED = "surfaceChanged";
	public static final String METHOD_ON_SURFACE_DESTROYED = "surfaceDestroyed";

	public BackSurfaceView(Context context) {
		super(context);
		init();
	}

	public BackSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BackSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		getHolder().addCallback(this);
		startThread();
		setFocusable(true);
		// 如果非系统绘制壁纸不需设置透明
		getHolder().setFormat(PixelFormat.TRANSLUCENT);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		onSurfaceChanged(width, height);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Rect rect = holder.getSurfaceFrame();
		if (rect != null) {
			final int width = rect.width();
			final int height = rect.height();
			onSurfaceCreated(width, height);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		onSurfaceDestroyed();
	}

	public void setRenderer(View renderer) {
		mRenderer = renderer;
	}

	public View getRenderer() {
		return mRenderer;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// drawBackground(canvas);
		final View renderer = mRenderer;
		if (renderer != null) {
			renderer.draw(canvas);
		}
	}

	public void onSurfaceDestroyed() {
		stopThread();
		doThemeViewMethod(METHOD_ON_SURFACE_DESTROYED, null);
	}

	public void onSurfaceChanged(int width, int height) {
		doThemeViewMethod(METHOD_ON_SURFACE_CHANGED, new Object[] { width, height }, Integer.TYPE,
				Integer.TYPE);
	}

	public void onSurfaceCreated(int width, int height) {
		doThemeViewMethod(METHOD_ON_SURFACE_CREATED, new Object[] { width, height }, Integer.TYPE,
				Integer.TYPE);
	}

	/**
	 * 反射回去rootView的方法onSurfaceCreated
	 * 
	 * @param methodName
	 * @return
	 */
	private void doThemeViewMethod(String methodName, Object[] value, Class... params) {
		Method method = null;
		if (mRenderer != null) {
			View themeView = mRenderer;
			try {
				Class tempClass = themeView.getClass();
				method = tempClass.getMethod(methodName, params);
				if (value != null) {
					method.invoke(themeView, value);
				} else {
					method.invoke(themeView);
				}
			} catch (Exception e) {
			}
		}
	} // end doThemeViewMethod

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (mRenderer != null) {
			return mRenderer.onTouchEvent(event);
		}
		return super.dispatchTouchEvent(event);
	}

	private void startThread() {
		if (mThread == null) {
			mThread = new BackSurfaceThread(getHolder(), this);
			mThread.setRunning(true);
			mThread.start();
		}
	}

	private void stopThread() {
		if (mThread != null) {
			mThread.setRunning(false);
			mThread.interrupt();
			mThread = null;
		}
	}

	public void onStateMethod(String state) {
		if (state.equals(BackWorkspace.STATE_ON_PAUSE) || state.equals(BackWorkspace.STATE_ON_STOP)
				|| state.equals(BackWorkspace.STATE_ON_DESTROY)) {
			stopThread();
		} else if (state.equals(BackWorkspace.STATE_ON_RESUME)) {
			startThread();
		}
	}

}
