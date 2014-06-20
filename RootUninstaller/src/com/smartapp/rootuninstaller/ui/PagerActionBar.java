package com.smartapp.rootuninstaller.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.smartapp.rootuninstaller.R;

public abstract class PagerActionBar extends View implements
		ViewPager.OnPageChangeListener {
	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private int mTitleTextColor;
	private int mSelectedItemTextColor;
	private int mFooterSeparatorColor;
	private int mSelectorColor;
	private float mFooterSeparatorLineWeight; // 底部分隔线线条宽�?
	private float mTitleTextSize;
	private float mHeaderPadding;
	private float mFooterPadding;
	// private Drawable mSelector;
	protected Rect mSelectorRect = new Rect();

	protected int mScrollState;
	protected int mCurrentPage;
	protected int mPageScrollTo;
	protected float mCurrentOffsetPercent;
	protected int mCurrentOffsetPixels;

	protected boolean mScrolling = false;
	protected boolean mScrollToLeft = false;

	private float mLastMotionX;
	private boolean mIsPressed;
	private boolean mIsDragging;
	private boolean mDrawSeletorInPressedState;

	private ViewPager mViewPager;

	public PagerActionBar(Context context) {
		this(context, null);
	}

	public PagerActionBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PagerActionBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.PagerActionBar);
		mTitleTextSize = a.getDimensionPixelSize(
				R.styleable.PagerActionBar_title_text_size, 15);
		mTitleTextColor = a.getColor(
				R.styleable.PagerActionBar_title_text_color, Color.WHITE);
		mSelectedItemTextColor = a.getColor(
				R.styleable.PagerActionBar_selected_title_text_color,
				Color.WHITE);
		mSelectorColor = a.getColor(R.styleable.PagerActionBar_selector_color,
				0x00000000);
		// mSelector = new ColorDrawable(mSelectorColor);
		mFooterSeparatorColor = a.getColor(
				R.styleable.PagerActionBar_footer_separator_color, Color.WHITE);
		mFooterSeparatorLineWeight = a.getDimensionPixelSize(
				R.styleable.PagerActionBar_footer_separator_line_weight, 5);
		mHeaderPadding = a.getDimensionPixelSize(
				R.styleable.PagerActionBar_header_padding, 10);
		mFooterPadding = a.getDimensionPixelSize(
				R.styleable.PagerActionBar_footer_padding, 10);
	}

	public void setTitleTextSize(float textSize) {
		mTitleTextSize = textSize;
		invalidate();
	}

	public float getTitleTextSize() {
		return mTitleTextSize;
	}

	public void setSeparatorLineWeight(float lineWeight) {
		mFooterSeparatorLineWeight = lineWeight;
		invalidate();
	}

	public float getFooterSeparaotrLineWeight() {
		return mFooterSeparatorLineWeight;
	}

	public void setSelectorColor(int color) {
		mSelectorColor = color;
		invalidate();
	}

	public int getSelectorColor() {
		return mSelectorColor;
	}

	public void setSelectedItemTextColor(int color) {
		mSelectedItemTextColor = color;
		invalidate();
	}

	public int getSelectedItemTextColor() {
		return mSelectedItemTextColor;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mViewPager == null) {
			return;
		}
		if (mViewPager.getAdapter() == null
				|| mViewPager.getAdapter().getCount() == 0) {
			return;
		}
		drawTitleText(canvas);
		drawSeletor(canvas);
		drawFooterSeparator(canvas);
		drawFooterIndicator(canvas);
	}

	protected RectF getTitleBound(int position) {
		RectF result = new RectF();
		result.setEmpty();
		final int titleCount = (mViewPager != null && mViewPager.getAdapter() != null) ? mViewPager
				.getAdapter().getCount() : 0;
		if (position >= 0 && position < titleCount) {
			String title = mViewPager.getAdapter().getPageTitle(position)
					.toString();
			mPaint.setTextSize(mTitleTextSize);
			final float textWidth = mPaint.measureText(title);
			final float textHeight = mPaint.descent() - mPaint.ascent();
			final int screenWidth = getResources().getDisplayMetrics().widthPixels;
			float left = 0;

			final float leftEdge = getPaddingLeft();
			final float rightEdge = getWidth() - getPaddingRight();
			final float leftOfCenterItem = (getWidth() - textWidth) / 2.0f;
			final float rightOfCenterItem = leftOfCenterItem + textWidth;
			if (!mScrolling) {
				if (position == mCurrentPage) {
					left = leftOfCenterItem;
				} else if (position == mCurrentPage - 1) {
					left = leftEdge;
				} else if (position == mCurrentPage + 1) {
					left = rightEdge - textWidth;
				} else {
					left = -Integer.MAX_VALUE;
				}
			} else if (mPageScrollTo != -1) {
				float offsetPercent = 0;
				float scrollDistance = 0;
				if (mScrollToLeft) {
					// offsetPercent = (float) (0 - mCurrentOffsetPixels) /
					// (float) screenWidth;
					offsetPercent = -mCurrentOffsetPercent;
					if (position < mPageScrollTo) {
						if (position == mPageScrollTo - 1) {
							left = leftOfCenterItem;
							scrollDistance = Math.abs(leftOfCenterItem
									- leftEdge);
						} else {
							left = leftEdge;
							scrollDistance = screenWidth;
						}
					} else if (position == mPageScrollTo) {
						left = rightEdge - textWidth;
						scrollDistance = Math.abs(left - leftOfCenterItem);
					} else {
						left = rightEdge + screenWidth / 2.0f;
						scrollDistance = textWidth + screenWidth / 2.0f;
					}
				} else {
					// offsetPercent = (float) (screenWidth -
					// mCurrentOffsetPixels) / (float) screenWidth;
					offsetPercent = 1.0f - mCurrentOffsetPercent;
					if (position > mPageScrollTo) {
						if (position == mPageScrollTo + 1) {
							left = leftOfCenterItem;
							scrollDistance = Math.abs(rightEdge
									- rightOfCenterItem);
						} else {
							left = rightEdge - textWidth;
							scrollDistance = screenWidth;
						}
					} else if (position == mPageScrollTo) {
						left = leftEdge;
						scrollDistance = Math.abs(leftEdge - leftOfCenterItem);
					} else {
						left = leftEdge - textWidth - screenWidth / 2.0f;
						scrollDistance = textWidth + screenWidth / 2.0f;
					}
				}
				left += (scrollDistance * offsetPercent);
			}

			final float right = left + textWidth;
			final float top = getPaddingTop() + mHeaderPadding;
			final float bottom = top + textHeight;
			result.set(left, top, right, bottom);
		}
		return result;
	}

	private void drawTitleText(Canvas canvas) {
		final int titleCount = mViewPager.getAdapter().getCount();
		mPaint.setTextSize(mTitleTextSize);
		mPaint.setColor(mTitleTextColor);
		final String centerTitle = mCurrentPage >= 0
				&& mCurrentPage < titleCount ? mViewPager.getAdapter()
				.getPageTitle(mCurrentPage).toString() : null;
		if (centerTitle != null) {
			RectF centerBound = getTitleBound(mCurrentPage);
			final float baseLineY = centerBound.bottom - mPaint.descent();
			canvas.drawText(centerTitle, centerBound.left, baseLineY, mPaint);
		}

		RectF otherBound = null;
		final String leftTitle = mCurrentPage - 1 < 0 ? null : mViewPager
				.getAdapter().getPageTitle(mCurrentPage - 1).toString();
		if (leftTitle != null) {
			otherBound = getTitleBound(mCurrentPage - 1);
			final float baseLineY = otherBound.bottom - mPaint.descent();
			canvas.drawText(leftTitle, otherBound.left, baseLineY, mPaint);
		}

		final String rightTitle = mCurrentPage + 1 >= titleCount ? null
				: mViewPager.getAdapter().getPageTitle(mCurrentPage + 1)
						.toString();
		if (rightTitle != null) {
			otherBound = getTitleBound(mCurrentPage + 1);
			final float baseLineY = otherBound.bottom - mPaint.descent();
			canvas.drawText(rightTitle, otherBound.left, baseLineY, mPaint);
		}

		drawSelectedTitleText(canvas);
	}

	private void drawSelectedTitleText(Canvas canvas) {
		mPaint.setTextSize(mTitleTextSize);
		mPaint.setColor(mSelectedItemTextColor);
		final int titleCount = mViewPager.getAdapter().getCount();
		final String curPageTitle = mCurrentPage >= 0
				&& mCurrentPage < titleCount ? mViewPager.getAdapter()
				.getPageTitle(mCurrentPage).toString() : null;
		float offsetPercent = mScrolling ? (mScrollToLeft ? mCurrentOffsetPercent
				: 1.0f - mCurrentOffsetPercent)
				: 0;
		// Log.d("GoNamecard", "mScrollToLeft = " + mScrollToLeft);
		// Log.d("GoNamecard", "offsetPercent = " + offsetPercent);
		if (curPageTitle != null) {
			int alpha = Math
					.max((int) ((mSelectedItemTextColor >>> 24) * (1.0f - offsetPercent * 2.0f)),
							0);
			mPaint.setAlpha(alpha);
			RectF curPageBound = getTitleBound(mCurrentPage);
			final float baseLineY = curPageBound.bottom - mPaint.descent();
			canvas.drawText(curPageTitle, curPageBound.left, baseLineY, mPaint);
		}

		if (mScrolling && Math.abs(offsetPercent) > 0.5f) {
			final String pageScrollToTitle = mPageScrollTo >= 0
					&& mPageScrollTo < titleCount ? mViewPager.getAdapter()
					.getPageTitle(mPageScrollTo).toString() : null;
			if (pageScrollToTitle != null) {
				RectF pageScrollToBound = getTitleBound(mPageScrollTo);
				int alpha = Math
						.max((int) ((mSelectedItemTextColor >>> 24) * ((offsetPercent - 0.5f) * 2)),
								0);
				// Log.d("GoNamecard", "pageScrollTo = " + mPageScrollTo);
				// Log.d("GoNamecard", "pageScrollTo Alpha = " + alpha);
				mPaint.setAlpha(alpha);
				final float baseLineY = pageScrollToBound.bottom
						- mPaint.descent();
				canvas.drawText(pageScrollToTitle, pageScrollToBound.left,
						baseLineY, mPaint);
			}
		}
	}

	private void drawFooterSeparator(Canvas canvas) {
		// mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setColor(mFooterSeparatorColor);
		mPaint.setStrokeWidth(mFooterSeparatorLineWeight);
		final int width = getWidth();
		final int height = getHeight();
		canvas.drawLine(0, height - mFooterSeparatorLineWeight / 2.0f, width,
				height - mFooterSeparatorLineWeight / 2.0f, mPaint);
	}

	private void drawSeletor(Canvas canvas) {
		if (shouldShowSelector() && mSelectorRect != null
				&& !mSelectorRect.isEmpty()) {
			Style oldStyle = mPaint.getStyle();
			mPaint.setStyle(Style.FILL);
			mPaint.setColor(mSelectorColor);
			canvas.drawRect(mSelectorRect, mPaint);
			mPaint.setStyle(oldStyle);
		}
	}

	private boolean shouldShowSelector() {
		return mDrawSeletorInPressedState;
	}

	private int getTitleIndexPressed(float x, float y) {
		if (mViewPager != null && mViewPager.getAdapter() != null) {
			final int count = mViewPager.getAdapter().getCount();
			for (int i = 0; i < count; i++) {
				final RectF bound = getTitleBound(i);
				bound.top = 0;
				bound.bottom = getHeight();
				if (bound != null && bound.contains(x, y)) {
					return i;
				}
			}
		}
		return -1;
	}

	protected void positionSelector(int titleIndex) {
		RectF bound = getTitleBound(titleIndex);
		if (bound != null && !bound.isEmpty()) {
			mSelectorRect.set((int) bound.left, 0, (int) bound.right,
					getHeight());
			refreshDrawableState();
		}
	}

	protected abstract void drawFooterIndicator(Canvas canvas);

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec),
				measureHeight(heightMeasureSpec));
	}

	private int measureWidth(int widthMeasureSpec) {
		int specMode = MeasureSpec.getMode(widthMeasureSpec);
		int specSize = MeasureSpec.getSize(widthMeasureSpec);
		int result = 0;
		if (specMode == MeasureSpec.UNSPECIFIED) {
			throw new IllegalStateException(
					"getClass().getSimpleName() measureWidth cannot be UNSPECIFIED");
		}
		result = specSize;
		return result;
	}

	private int measureHeight(int heightMeasureSpec) {
		int specMode = MeasureSpec.getMode(heightMeasureSpec);
		int specSize = MeasureSpec.getSize(heightMeasureSpec);
		float result = 0;
		if (specMode == MeasureSpec.EXACTLY) {
			result = specSize;
		} else {
			mPaint.setTextSize(mTitleTextSize);
			result = mPaint.descent() - mPaint.ascent() + mHeaderPadding
					+ mFooterPadding;
		}
		return (int) result;
	}

	public void attachToViewPager(ViewPager viewPager) {
		mViewPager = viewPager;
		if (mViewPager != null) {
			mViewPager.setOnPageChangeListener(this);
		}
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mViewPager == null || mViewPager.getAdapter().getCount() == 0) {
			return false;
		}
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mIsPressed = true;
			final int seletectedItemIndex = getTitleIndexPressed(x, y);
			if (seletectedItemIndex != -1) {
				mDrawSeletorInPressedState = true;
				positionSelector(seletectedItemIndex);
			}
			mLastMotionX = event.getX();
			break;
		case MotionEvent.ACTION_MOVE:
			final float deltaX = x - mLastMotionX;
			if (!mIsDragging) {
				final int touchSlop = ViewConfigurationCompat
						.getScaledPagingTouchSlop(ViewConfiguration
								.get(getContext()));
				if (Math.abs(deltaX) > touchSlop) {
					mIsDragging = true;
				}
			}
			if (mIsDragging) {
				if (!mViewPager.isFakeDragging()) {
					mViewPager.beginFakeDrag();
				}
				mViewPager.fakeDragBy(deltaX);
				mLastMotionX = x;
				mDrawSeletorInPressedState = false;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (mIsPressed) {
				mIsPressed = false;
				if (!mIsDragging) {
					int selectedItemIndex = getTitleIndexPressed(x, y);
					if (selectedItemIndex != -1) {
						mViewPager.setCurrentItem(selectedItemIndex, true);
					}
				}
				if (mViewPager.isFakeDragging()) {
					mViewPager.endFakeDrag();
				}
			}
			mDrawSeletorInPressedState = false;
			mIsDragging = false;
			break;
		default:
			break;
		}
		invalidate();
		// refreshDrawableState();
		return true;
	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {
		/*
		 * if (!mScrolling && positionOffsetPixels != 0) { mScrolling = true;
		 * final int halfScreenWidth =
		 * getResources().getDisplayMetrics().widthPixels / 2; mScrollToLeft =
		 * Math.abs(positionOffsetPixels) < halfScreenWidth ? true : false;
		 * mPageScrollTo = mScrollToLeft ? position + 1 : position;
		 * 
		 * if (mScrolling && positionOffsetPixels == 0) { mPageScrollTo = -1;
		 * mScrolling = false; } }
		 */
		if (mScrollState != ViewPager.SCROLL_STATE_IDLE) {
			if (positionOffsetPixels != 0) {
				if (mScrollState == ViewPager.SCROLL_STATE_DRAGGING) {
					mScrollToLeft = (position == mCurrentPage) ? true : false;
					mPageScrollTo = mScrollToLeft ? position + 1 : position;
				} else if (!mScrolling
						&& mScrollState == ViewPager.SCROLL_STATE_SETTLING) {
					mPageScrollTo = mCurrentPage;
					mScrollToLeft = (position == mPageScrollTo) ? false : true;
				}
				mScrolling = true;
			} else {
				if (mScrolling) {
					mPageScrollTo = -1;
					mScrolling = false;
				}
			}
		}

		// Log.d("GoNamecard", "onPageScrolled position = " + position +
		// ", offset = " + positionOffsetPixels + ", state = " + mScrollState);
		mCurrentOffsetPercent = positionOffset;
		mCurrentOffsetPixels = positionOffsetPixels;

		invalidate();
	}

	@Override
	public void onPageSelected(int position) {
		// Log.d("GoNamecard", "onPageSelected position = " + position);
		mCurrentPage = position;
		invalidate();
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		// Log.d("GoNamecard", "onPageScrollStateChanged state = " + state);
		mScrollState = state;
		if (mScrolling && mScrollState == ViewPager.SCROLL_STATE_IDLE) {
			mPageScrollTo = -1;
			mScrolling = false;
			invalidate();
		}
	}

}
