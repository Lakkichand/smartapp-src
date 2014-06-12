package com.jiubang.ggheart.appgame.base.component;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 
 * 类描述:有火焰动画的图标
 * 
 * @author xiedezhi
 * @date [2012-8-16]
 */
public class BurningIcon extends ImageView {
	/**
	 * 是否显示火焰动画
	 */
	private boolean mIsBurning = false;
	/**
	 * 动画帧集合
	 */
	private List<Bitmap> mBurningFrame = null;
	/**
	 * 当前正在播放的帧的下标
	 */
	private int mCurrentFrame = 0;
	/**
	 * 刷新动画的时间间隔，单位毫秒
	 */
	private static final int ANIMATION_INTERVAL = 100;
	/**
	 * Bitmap缩放矩阵
	 */
	private Matrix mMatrix = new Matrix();

	private Runnable mAnimationRunnable = new Runnable() {

		@Override
		public void run() {
			if (mIsBurning && mBurningFrame != null && mBurningFrame.size() > 0) {
				invalidate();
				mCurrentFrame++;
				if (mBurningFrame != null && mCurrentFrame >= mBurningFrame.size()) {
					mCurrentFrame = 0;
				}
				removeCallbacks(mAnimationRunnable);
				postDelayed(mAnimationRunnable, ANIMATION_INTERVAL);
			} else {
				setBurning(false);
			}
		}
	};

	public BurningIcon(Context context) {
		super(context);
	}

	public BurningIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BurningIcon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 设置是否显示火焰动画
	 */
	public void setBurning(boolean burning) {
		// if (mIsBurning == burning) {
		// return;
		// }
		mIsBurning = burning;
		mCurrentFrame = 0;
		if (!burning) {
			// 清除动画runnable
			removeCallbacks(mAnimationRunnable);
			// 把动画帧清空
			if (mBurningFrame != null) {
				mBurningFrame.clear();
			}
		} else {
			removeCallbacks(mAnimationRunnable);
			post(mAnimationRunnable);
		}
	}

	/**
	 * 设置需要播放的动画帧集合，要在setBurning前调用
	 * 
	 * @param frames
	 *            需要播放的动画帧集合
	 */
	public void setBurningFrame(List<Bitmap> frames) {
		if (frames == null || frames.size() == 0) {
			// 如果动画帧为空，停止动画
			this.mBurningFrame = null;
			setBurning(false);
			return;
		}
		if (this.mBurningFrame == null) {
			this.mBurningFrame = new ArrayList<Bitmap>();
		}
		for (Bitmap bm : frames) {
			this.mBurningFrame.add(bm);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mIsBurning && mBurningFrame != null) {
			if (mCurrentFrame >= 0 && mCurrentFrame < mBurningFrame.size()) {
				Bitmap bm = mBurningFrame.get(mCurrentFrame);
				if (bm != null && (!bm.isRecycled())) {
					// TODO:XIEDEZHI 不用每次算scale
					float sx = (float) getWidth() / (float) bm.getWidth();
					float sy = (float) getHeight() / (float) bm.getHeight();
					// Log.e("XIEDEZHI", "sx = " + sx + "  sy = " + sy);
					// 在canvas上画出当前的动画帧
					mMatrix.reset();
					mMatrix.setScale(sx, sy);
					canvas.drawBitmap(bm, mMatrix, null);
				}
			}
		}
	}
}
