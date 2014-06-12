package com.jiubang.ggheart.appgame.gostore.base.component;

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
public class ThemesFeatureTag extends ImageView {
	/**
	 * 是否显示标签
	 * 
	 */
	private boolean mIsShining = false;
	/**
	 * 动画帧集合
	 */
	private List<Bitmap> mShiningFrame = null;
	/**
	 * 当前正在播放的帧的下标
	 */
	private int mCurrentFrame = 0;
	/**
	 * 刷新动画的时间间隔，单位毫秒
	 */
	private static final int ANIMATION_INTERVAL = 150;
	/**
	 * Bitmap缩放矩阵
	 */
	private Matrix mMatrix = new Matrix();

	private Runnable mAnimationRunnable = new Runnable() {

		@Override
		public void run() {
			if (mIsShining && mShiningFrame != null && mShiningFrame.size() > 0) {
				invalidate();
				mCurrentFrame++;
				if (mShiningFrame != null && mCurrentFrame >= mShiningFrame.size()) {
					mCurrentFrame = 0;
				}
				removeCallbacks(mAnimationRunnable);
				postDelayed(mAnimationRunnable, ANIMATION_INTERVAL);
			} else {
				setShining(false);
			}
		}
	};

	public ThemesFeatureTag(Context context) {
		super(context);
	}

	public ThemesFeatureTag(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ThemesFeatureTag(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 设置是否显示火焰动画
	 */
	public void setShining(boolean shining) {
		// if (mIsBurning == burning) {
		// return;
		// }
		mIsShining = shining;
		mCurrentFrame = 0;
		if (!shining) {
			// 清除动画runnable
			removeCallbacks(mAnimationRunnable);
			// 把动画帧清空
			if (mShiningFrame != null) {
				mShiningFrame.clear();
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
	public void setShiningFrame(List<Bitmap> frames) {
		if (frames == null || frames.size() == 0) {
			// 如果动画帧为空，停止动画
			this.mShiningFrame = null;
			setShining(false);
			return;
		}
		if (this.mShiningFrame == null) {
			this.mShiningFrame = new ArrayList<Bitmap>();
		} else {
			this.mShiningFrame.clear();	 //add by  zzf 
		}
		for (Bitmap bm : frames) {
			this.mShiningFrame.add(bm);
		}
		if (frames.get(0) != null) {
			
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mIsShining && mShiningFrame != null) {
			if (mCurrentFrame >= 0 && mCurrentFrame < mShiningFrame.size()) {
				Bitmap bm = mShiningFrame.get(mCurrentFrame);
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