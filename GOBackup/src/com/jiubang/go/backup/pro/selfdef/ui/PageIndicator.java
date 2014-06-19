package com.jiubang.go.backup.pro.selfdef.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.jiubang.go.backup.ex.R;

/**
 * 页面指示器
 * 
 * @author maiyongshen
 * @version 1.0
 */
public class PageIndicator extends View {

	private Drawable mNormalDrawable;
	private Drawable mHighlightDrawable;
	private int mCount;
	private int mCurrentIndex = -1;
	private int mIndicatorSpace;

	public PageIndicator(Context context) {
		this(context, null);
	}

	public PageIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PageIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		final int m5 = 5;
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PageIndicator);
		mNormalDrawable = a.getDrawable(R.styleable.PageIndicator_normal_drawable);
		mHighlightDrawable = a.getDrawable(R.styleable.PageIndicator_high_light_drawable);
		mIndicatorSpace = a.getDimensionPixelSize(R.styleable.PageIndicator_indicator_spacing, m5);
		configureBound();
		a.recycle();
	}

	private void configureBound() {
		if (mNormalDrawable != null) {
			mNormalDrawable.setBounds(0, 0, mNormalDrawable.getIntrinsicWidth(),
					mNormalDrawable.getIntrinsicHeight());
		}
		if (mHighlightDrawable != null) {
			mHighlightDrawable.setBounds(0, 0, mHighlightDrawable.getIntrinsicWidth(),
					mHighlightDrawable.getIntrinsicHeight());
		}
	}

	public void setStyleDrawable(int normalDrawableId, int highLightDrawableId) {
		if (normalDrawableId != 0) {
			mNormalDrawable = getResources().getDrawable(normalDrawableId);
		}
		if (highLightDrawableId != 0) {
			mHighlightDrawable = getResources().getDrawable(highLightDrawableId);
		}
		configureBound();
		invalidate();
	}

	public void setStyleDrawable(Drawable normalDrawable, Drawable highLightDrawable) {
		mNormalDrawable = normalDrawable;
		mHighlightDrawable = highLightDrawable;
		configureBound();
		invalidate();
	}

	public void setStyleDrawableBitmap(Bitmap normalBitmap, Bitmap highLightBitmap) {
		if (normalBitmap != null) {
			mNormalDrawable = new BitmapDrawable(normalBitmap);
		}
		if (highLightBitmap != null) {
			mHighlightDrawable = new BitmapDrawable(highLightBitmap);
		}
		configureBound();
		invalidate();
	}

	public void update(int currentIndex, int totalCount) {
		mCurrentIndex = currentIndex;
		mCount = totalCount;
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int heightSpecSize = Math.max(mNormalDrawable.getIntrinsicHeight(),
				mHighlightDrawable.getIntrinsicHeight());
		int widthSpecSize = Math.max(mNormalDrawable.getIntrinsicWidth(),
				mHighlightDrawable.getIntrinsicWidth())
				* mCount + mIndicatorSpace * (mCount - 1);
		setMeasuredDimension(widthSpecSize, heightSpecSize);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		final int count = mCount;
		int offset = 0;
		Drawable currentDrawable = null;
		final int saveCount = canvas.save();
		for (int i = 0; i < count; i++) {
			if (mCurrentIndex == i) {
				currentDrawable = mHighlightDrawable;
			} else {
				currentDrawable = mNormalDrawable;
			}
			offset = i == 0 ? 0 : currentDrawable.getIntrinsicWidth() + mIndicatorSpace;
			canvas.translate(offset, 0);
			currentDrawable.draw(canvas);
		}
		canvas.restoreToCount(saveCount);
	}
}
