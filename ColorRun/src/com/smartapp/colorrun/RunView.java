package com.smartapp.colorrun;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

public class RunView extends View implements OnGestureListener {

	private final int mGap = Util.dip2px(getContext(), 0.5f);

	private float mItemHeight;

	private float mStep;

	private float mOffset;

	private int[] mArray = new int[5];

	private Handler mHandler = new Handler(Looper.getMainLooper());

	private Paint mPaint = new Paint();

	private int mScreenWidth;

	/**
	 * 手势滑动处理类
	 */
	private GestureDetector mDetector = new GestureDetector(getContext(), this);

	private Runnable mRolling = new Runnable() {

		@Override
		public void run() {
			mHandler.removeCallbacks(this);
			if (RunView.this.getParent() != null) {
				mOffset += mStep;
				if (mOffset > 0) {
					mOffset = -mItemHeight;
					int[] array = new int[mArray.length];
					array[0] = Math.abs(Util.sRandom.nextInt()) % 4;
					for (int i = 0; i < mArray.length - 1; i++) {
						array[i + 1] = mArray[i];
					}
					mArray = array;
				}
				invalidate();
				mHandler.postDelayed(this, 10);
			}
		}
	};

	public RunView(Context context, int screenWidth, int screenHeight) {
		super(context);
		mScreenWidth = screenWidth;
		mItemHeight = screenHeight / 4.0f;
		mStep = mItemHeight / 50.0f;
		mOffset = -mItemHeight;
		for (int i = 0; i < mArray.length; i++) {
			mArray[i] = Math.abs(Util.sRandom.nextInt()) % 4;
		}
		mHandler.postDelayed(mRolling, 1000);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// 画5行
		// 第一行
		drawItem(canvas, mOffset, mArray[0], true);
		// 第二行
		drawItem(canvas, mOffset + mItemHeight, mArray[1], true);
		// 第三行
		drawItem(canvas, mOffset + mItemHeight * 2.0f, mArray[2], true);
		// 第四行
		drawItem(canvas, mOffset + mItemHeight * 3.0f, mArray[3], true);
		// 第五行，最后一行不用画底部分隔线
		drawItem(canvas, mOffset + mItemHeight * 4.0f, mArray[4], false);
	}

	private void drawItem(Canvas canvas, float offset, int index,
			boolean bottomgap) {
		mPaint.setColor(Color.WHITE);
		canvas.drawRect(0, offset, mScreenWidth, offset + mItemHeight, mPaint);
		float width = (mScreenWidth - 3 * mGap) / 4.0f;
		// 竖分割线
		mPaint.setColor(Color.BLACK);
		canvas.drawRect(width, offset, width + mGap, offset + mItemHeight,
				mPaint);
		canvas.drawRect(width * 2 + mGap, offset, width * 2 + mGap * 2, offset
				+ mItemHeight, mPaint);
		canvas.drawRect(width * 3 + mGap * 2, offset, width * 3 + mGap * 3,
				offset + mItemHeight, mPaint);
		// 黑格
		width = mScreenWidth / 4.0f;
		canvas.drawRect(index * width, offset, (index + 1) * width, offset
				+ mItemHeight, mPaint);
		// 底线
		mPaint.setColor(Color.BLACK);
		if (bottomgap) {
			canvas.drawRect(0, offset + mItemHeight - mGap, mScreenWidth,
					offset + mItemHeight, mPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		mDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		Log.e("", "onDown x = " + e.getX() + "  y = " + e.getY());
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}
}
