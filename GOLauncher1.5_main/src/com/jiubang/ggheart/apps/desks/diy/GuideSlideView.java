package com.jiubang.ggheart.apps.desks.diy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;

import com.gau.go.launcherex.R;

public class GuideSlideView extends View {
	private Bitmap mMask;
	private Drawable mBg;
	private Bitmap mTopBmp;
	private Animation mAiAnimation;
	private Context mContext;
	private PorterDuffXfermode pdf = null;
	private Paint mPaint;
	private Transformation mTransformation = new Transformation();
	private int mWidth;
	private int mHight;

	public GuideSlideView(Context context) {
		this(context, null);
	}

	public GuideSlideView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
		init();
	}

	private void init() {
		int w = mWidth = (int) mContext.getResources().getDimension(R.dimen.guide_slide_width);
		int h = mHight = (int) mContext.getResources().getDimension(R.dimen.guide_slide_height);
		mMask = ((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.guide_slide_mask))
				.getBitmap();
		mBg = mContext.getResources().getDrawable(R.drawable.guide_slide_bg);
		mTopBmp = ((BitmapDrawable) mContext.getResources().getDrawable(R.drawable.guide_slide_top))
				.getBitmap().copy(Config.ARGB_8888, true);
		mBg.setBounds(0, 0, mBg.getIntrinsicWidth(), mBg.getIntrinsicHeight());
		// mMask.setBounds(0,
		// 0,mMask.getIntrinsicWidth(),mMask.getIntrinsicHeight());
		mAiAnimation = new TranslateAnimation(w, -w, 0, 0);
		mAiAnimation.initialize(w, h, w, h);
		mAiAnimation.setDuration(1000);
		mAiAnimation.setRepeatCount(-1);
		mAiAnimation.startNow();
		pdf = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
		mPaint = new Paint();
		mPaint.setXfermode(pdf);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int sc = canvas.saveLayer(0, 0, mWidth, mHight, null, Canvas.MATRIX_SAVE_FLAG
				| Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
				| Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);
		if (mBg != null) {
			mBg.draw(canvas);
			int id = canvas.save();
			if (mAiAnimation != null) {
				mAiAnimation.getTransformation(AnimationUtils.currentAnimationTimeMillis(),
						mTransformation);
				canvas.concat(mTransformation.getMatrix());
			}
			canvas.drawBitmap(mTopBmp, 0, 0, null);
			canvas.restoreToCount(id);
		}
		canvas.drawBitmap(mMask, 0, 0, mPaint);
		canvas.restoreToCount(sc);
		invalidate();
	}

}
