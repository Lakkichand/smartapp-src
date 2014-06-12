package com.jiubang.ggheart.apps.desks.diy.frames.preview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.gau.go.launcherex.R;
import com.go.util.graphics.BitmapUtility;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;

/**
 * @author  
 */

public class ScreenEditLightView extends View {
	private Context mContext;
	private Paint mPaint;
	private BitmapDrawable mHomeRound;
	private BitmapDrawable mPlusRound;
	private BitmapDrawable mScreenMask;
	private Bitmap mBgBitmap;
	private int mRadius;
	private static int sHomeX;
	private static int sHomeY;
	private static int sPlusX;
	private static int sPlusY;
	private boolean mIs9Screen;

	public ScreenEditLightView(Context context) {
		this(context, null);
	}

	public ScreenEditLightView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}

	private void init() {
		mPaint = new Paint();
		int width = GoLauncher.getDisplayWidth();
		int height = GoLauncher.getDisplayHeight();

		Resources res = mContext.getResources();
		mScreenMask = (BitmapDrawable) res.getDrawable(R.drawable.guide_for_screenfolder_mask);
		mHomeRound = (BitmapDrawable) res.getDrawable(R.drawable.guide_for_screenedit_circle_light);
		mPlusRound = (BitmapDrawable) res.getDrawable(R.drawable.guide_for_screenedit_circle_light);
		mBgBitmap = BitmapUtility.createScaledBitmap(mScreenMask.getBitmap(), width, height);
		mRadius = mHomeRound.getIntrinsicWidth() / 2;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBgBitmap == null) {
			return;
		}
		mPaint.setFilterBitmap(false);
		int sc = canvas.saveLayer(0, 0, GoLauncher.getDisplayWidth(),
				GoLauncher.getDisplayHeight(), null, Canvas.MATRIX_SAVE_FLAG
						| Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
						| Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
		canvas.drawBitmap(mBgBitmap, 0, 0, mPaint);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));
		// 画home的圆
		canvas.drawBitmap(mHomeRound.getBitmap(), sHomeX - mRadius, sHomeY, mPaint);
		if (!mIs9Screen) {
			// 如果小于9屏,画“+”的圆
			canvas.drawBitmap(mPlusRound.getBitmap(), sPlusX - mRadius, sPlusY, mPaint);
		}
		mPaint.setXfermode(null);
		canvas.restoreToCount(sc);
		super.onDraw(canvas);
	}

	public static void setHomeXY(int x, int y) {
		sHomeX = x;
		sHomeY = y;
	}

	public static void setPlusXY(int x, int y) {
		sPlusX = x;
		sPlusY = y;
	}

	public void is9Screen(boolean bool) {
		mIs9Screen = bool;
	}
	
	/**
	 * 提示层相关资源回收
	 * */
	public void recycle() {
//		if (mHomeRound.getBitmap() != null && !mHomeRound.getBitmap().isRecycled()) {
//			mHomeRound.getBitmap().recycle();
//		}
//		if (mPlusRound.getBitmap() != null && !mPlusRound.getBitmap().isRecycled()) {
//			mPlusRound.getBitmap().recycle();
//		}
//		if (mBgBitmap != null && !mBgBitmap.isRecycled()) {
//			mBgBitmap.recycle();
//		}
//		if (mScreenMask.getBitmap() != null && !mScreenMask.getBitmap().isRecycled()) {
//			mScreenMask.getBitmap().recycle();
//		}
		mScreenMask.setCallback(null);
		mHomeRound.setCallback(null);
		mPlusRound.setCallback(null);
		mScreenMask = null;
		mHomeRound = null;
		mPlusRound = null;
		mBgBitmap = null;
	}
}
