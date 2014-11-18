package com.escape.uninstaller.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.smartapp.rootuninstaller.R;

public class TitlePagerActionBar extends PagerActionBar {
	private float mFooterUnderlineWeight;
	private float mFooterUnderlinePadding;
	private int mFooterUnderlineColor;
	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	public TitlePagerActionBar(Context context) {
		this(context, null);
	}
	
	public TitlePagerActionBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public TitlePagerActionBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TitlePagerActionBar);
		mFooterUnderlineWeight = a.getDimensionPixelSize(R.styleable.TitlePagerActionBar_footer_underline_weight, 0);
		mFooterUnderlinePadding = a.getDimensionPixelSize(R.styleable.TitlePagerActionBar_footer_underline_padding, 0);
		mFooterUnderlineColor = a.getColor(R.styleable.TitlePagerActionBar_footer_underline_color, 0xffffffff);
		a.recycle();
	}
	
	public void setFooterIndicatorLineWeight(float lineWeight) {
		mFooterUnderlineWeight = lineWeight;
		invalidate();
	}
	
	public float getFooterIndicatorLineWeight() {
		return mFooterUnderlineWeight;
	}
	
	public void setFooterUnderlineColor(int color) {
		mFooterUnderlineColor = color;
		invalidate();
	}
	
	public int getFooterUnderlineColor() {
		return mFooterUnderlineColor;
	}
	
	public void setFooterUnderlinePadding(float padding) {
		mFooterUnderlinePadding = padding;
		invalidate();
	}
	
	public float getFooterUnderlinePadding() {
		return mFooterUnderlinePadding;
	}
	
	@Override
	protected void positionSelector(int titleIndex) {
		RectF bound = getTitleBound(titleIndex);
		if (bound != null && !bound.isEmpty()) {
			mSelectorRect.set((int) (bound.left - mFooterUnderlinePadding), 0, (int) (bound.right + mFooterUnderlinePadding), getHeight());
		}
	}

	@Override
	protected void drawFooterIndicator(Canvas canvas) {
		mPaint.setColor(mFooterUnderlineColor);
//		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setStrokeWidth(mFooterUnderlineWeight);
		float offsetPercent = mScrolling ? (mScrollToLeft ? mCurrentOffsetPercent : 1.0f - mCurrentOffsetPercent) : 0;
		int alpha = Math.max((int) ((mFooterUnderlineColor >>> 24) * (1.0f - offsetPercent * 2.0f)), 0);
		mPaint.setAlpha(alpha);
		RectF curPageBound = getTitleBound(mCurrentPage);
		final float y = getHeight() - getPaddingBottom() - mFooterUnderlineWeight / 2.0f;
		canvas.drawLine(curPageBound.left - mFooterUnderlinePadding, y,
				curPageBound.right + mFooterUnderlinePadding, y, mPaint);		
		
		if (mScrolling && Math.abs(offsetPercent) > 0.5f) {
			RectF pageScrollToBound = getTitleBound(mPageScrollTo);
			alpha = Math.max((int) ((mFooterUnderlineColor >>> 24) * ((offsetPercent - 0.5f) * 2)), 0);
			mPaint.setAlpha(alpha);
			canvas.drawLine(pageScrollToBound.left - mFooterUnderlinePadding, y,
					pageScrollToBound.right + mFooterUnderlinePadding, y, mPaint);
		}
		
	}	

}
