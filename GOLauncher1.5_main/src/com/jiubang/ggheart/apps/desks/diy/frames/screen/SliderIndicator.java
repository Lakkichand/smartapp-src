package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;

/**
 * 可拖动的指示器
 *
 */
public class SliderIndicator extends Indicator {
	private Drawable mIndicator; // 指示器图片
	private Drawable mIndicatorBG; // 指示器背景

	private IndicatorBgView mBgImageView; // 指示器背景组件，为了事件传到这里才加控件

	/**
	 * 指示器背景View
	 *
	 */
	private class IndicatorBgView extends ImageView {

		public IndicatorBgView(Context context) {
			super(context);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			Drawable drawable = getDrawable();
			if (null != drawable) {
				drawable.draw(canvas);
			}
		}
	};

	public SliderIndicator(Context context) {
		super(context);

		mTotal = 1;
		mCurrent = 0;

		initBgImageView();
	}

	private void initBgImageView() {
		mBgImageView = new IndicatorBgView(getContext());
		mBgImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// do nothing;
			}
		});
		addView(mBgImageView);
	}

	public SliderIndicator(Context context, AttributeSet attr) {
		super(context, attr);
	}

	public SliderIndicator(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
	}

	public void setIndicator(Drawable indicator, Drawable indicatorBG) {
		mIndicator = indicator;
		mIndicatorBG = indicatorBG;
		mBgImageView.setImageDrawable(mIndicatorBG);
		requestLayout();
	}

	public void setIndicator(int indicator, int indicatorBG) {
		try {
			final Drawable drawableIndicator = getContext().getResources().getDrawable(indicator);
			final Drawable drawableIndicatorBG = getContext().getResources().getDrawable(
					indicatorBG);

			setIndicator(drawableIndicator, drawableIndicatorBG);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			setIndicator(null, null); // 设置为null,效果是不显示指示器
		}
	}

	@Override
	public void setTotal(int total) {
		if (mTotal != total) {
			mTotal = total;
			requestLayout();
		}
	}

	@Override
	public void setCurrent(int current) {
		if (current < 0) {
			return;
		}

		mCurrent = current;
		mOffset = getWidth() * mCurrent / mTotal;
		postInvalidate();
	}

	@Override
	public void setOffset(int offset) {
		if (mOffset != offset) {
			mOffset = offset;
			postInvalidate();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (null != mIndicator) {
			mIndicator.setBounds(0, 0, getWidth() / mTotal, mIndicator.getIntrinsicHeight());

			if (null != mBgImageView) {
				Rect bounds = new Rect(0, 0, getWidth(), mIndicatorBG.getIntrinsicHeight());
				mBgImageView.layout(bounds.left, bounds.top, bounds.right, bounds.bottom);
				Drawable drawable = mBgImageView.getDrawable();
				if (null != drawable) {
					drawable.setBounds(bounds);
				}
			}

		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (null != mIndicator) {
			canvas.translate(mOffset, 0);
			mIndicator.draw(canvas);
			canvas.translate(-mOffset, 0);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean ret = super.onInterceptTouchEvent(ev);

		if (null != mListner) {
			int action = ev.getAction();

			switch (action) {
				case MotionEvent.ACTION_DOWN : {
					mMovePercent = 0.0f;

					break;
				}

				case MotionEvent.ACTION_MOVE : {
					if (mMoveDirection != Indicator.MOVE_DIRECTION_NONE) {
						float x = ev.getRawX();
						if (0 <= x && x <= getWidth()) {
							mMovePercent = (x * 100) / getWidth();
							mListner.sliding(mMovePercent);
						}
					}

					break;
				}

				case MotionEvent.ACTION_CANCEL :
				case MotionEvent.ACTION_UP : {
					int x = (int) ev.getRawX();
					if (0 <= x && x <= getWidth()) {
						int index = (int) (((float) x / (float) getWidth()) * mTotal);
						mListner.clickIndicatorItem(index);
					}

					break;
				}

				default :
					break;
			}
		}

		return ret;
	}
}
