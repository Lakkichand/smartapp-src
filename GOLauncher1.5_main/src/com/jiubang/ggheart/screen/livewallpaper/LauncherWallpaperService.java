/**
 * 
 */
package com.jiubang.ggheart.screen.livewallpaper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;

/**
 * @author liguoliang
 *
 */
public class LauncherWallpaperService extends WallpaperService {
	private int mScreenWidth = 480;
	private int mScreenHeight = 800;
	
	/**
	 * 绘制时间间隔
	 */
	private static final long DRAW_TIME_INTERVAL = 40;
	
	/**
	 * 是否横屏 
	 */
	private boolean mIsLandscape = false;
	
	private Context mContext;
	
	@Override
	public Engine onCreateEngine() {
		mContext = getApplicationContext();
		resetScreen();
		return new LauncherEngine();
	}
	
	private void resetScreen() {
		DisplayMetrics dm = new DisplayMetrics();
		dm = getApplicationContext().getResources().getDisplayMetrics();
		mScreenWidth = dm.widthPixels;
		mScreenHeight = dm.heightPixels;
		if (mScreenWidth < mScreenHeight) {			
			mIsLandscape = false;
		} else {
			mIsLandscape = true;
		}
	}

	/**
	 * @author liguoliang
	 *
	 */
	private class LauncherEngine extends Engine {		
		private Paint mPaint = new Paint();
		
		private Matrix mWallpaperMatrix = new Matrix();
		private Bitmap mWallpaperBitmap;
		private int mWallpaperBmpWidth;
		private int mWallpaperBmpHeight;
		
		private final SurfaceHolder mHolder = getSurfaceHolder();
		
		private long mPrevTime = 0;
		
		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);
		}
		
		private void resetWallpaper() {
			try {
				OutOfMemoryHandler.gcIfAllocateOutOfHeapSize();
				BitmapDrawable drawable = (BitmapDrawable) mContext.getResources().getDrawable(
						R.drawable.default_wallpaper);
				mWallpaperBitmap = drawable.getBitmap();				
			} catch (Exception e) {
				mWallpaperBitmap = null;
				return;
			}
			mWallpaperBmpWidth = mWallpaperBitmap.getWidth();
			mWallpaperBmpHeight = mWallpaperBitmap.getHeight();
						
			// 将图片按高度拉伸
			if (mIsLandscape) {
				// 横屏时
				float scale = (float) mScreenWidth / (float) mWallpaperBmpHeight;
				mWallpaperBitmap = Bitmap.createScaledBitmap(mWallpaperBitmap,
						(int) (mWallpaperBmpWidth * scale), mScreenHeight, true);
				mWallpaperBmpWidth = mWallpaperBitmap.getWidth();
				mWallpaperBmpHeight = mWallpaperBitmap.getHeight();
			} else {
				// 竖屏时
				float scale = (float) mScreenHeight / (float) mWallpaperBmpHeight;
				mWallpaperBitmap = Bitmap.createScaledBitmap(mWallpaperBitmap,
						(int) (mWallpaperBmpWidth * scale), mScreenHeight, true);
				mWallpaperBmpWidth = mWallpaperBitmap.getWidth();
				mWallpaperBmpHeight = mWallpaperBitmap.getHeight();
			}			
		}

		@Override
		public void onDestroy() {
			super.onDestroy();
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep,
				float yOffsetStep, int xPixelOffset, int yPixelOffset) {
			super.onOffsetsChanged(xOffset, yOffset, xOffsetStep, yOffsetStep, xPixelOffset, yPixelOffset);
			
			if (isPreview()) {
				mWallpaperMatrix.reset();			
			} else {				
				// 计算屏幕数
//				int pageSize = (int) Math.floor(1 / xOffsetStep + 0.5f + 1);		
//				// 计算所有屏幕总尺寸
//				int screenSize = Math.abs(mScreenWidth * pageSize);
				// 当前屏幕索引
				mWallpaperMatrix.reset();
				mWallpaperMatrix.postTranslate(-(mWallpaperBmpWidth - mScreenWidth) * xOffset, 0.0f);				
			}
			long t = System.currentTimeMillis();
			if (t - mPrevTime > DRAW_TIME_INTERVAL) {
				drawFrame();
				mPrevTime = System.currentTimeMillis();
			}			
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
			resetScreen();
			resetWallpaper();
			drawFrame();
		}

		@Override
		public void onSurfaceCreated(SurfaceHolder holder) {
			super.onSurfaceCreated(holder);
			resetWallpaper();
			drawFrame();
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
		}
		
		private void drawFrame() {
			Canvas canvas = null;
			try {
				canvas = mHolder.lockCanvas();

				// 清屏
				synchronized (mHolder) {
					canvas.save();
					canvas.drawColor(Color.BLACK);
					// 绘制底图
					canvas.drawBitmap(mWallpaperBitmap, mWallpaperMatrix, mPaint);
					canvas.restore();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (canvas != null) {
					mHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
		
	}
}
