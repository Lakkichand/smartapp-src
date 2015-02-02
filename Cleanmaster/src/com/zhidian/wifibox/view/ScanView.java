package com.zhidian.wifibox.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.ta.TAApplication;
import com.zhidian.wifibox.util.DrawUtil;

/**
 * 扫描效果的view
 * 
 * @author xiedezhi
 * 
 */
public class ScanView extends View {

	private int mPosition = 0;

	private int mStep = DrawUtil.dip2px(TAApplication.getApplication(), 4);

	private int mLineWidth = DrawUtil.dip2px(TAApplication.getApplication(), 1);

	private boolean isIncreasing = true;

	private int mRectWidth = DrawUtil
			.dip2px(TAApplication.getApplication(), 45);

	private Paint mPaint = new Paint();

	private ProgressCallBack mCallBack;

	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			removeCallbacks(this);
			if (isIncreasing) {
				mPosition += mStep;
				if (mPosition >= getWidth()) {
					mPosition = getWidth();
					isIncreasing = false;
				}
			} else {
				mPosition -= mStep;
				if (mPosition <= 0) {
					mPosition = 0;
					isIncreasing = true;
				}
			}
			if (mCallBack != null) {
				int progress = (int) (mPosition * 1.0 / getWidth() * 100 + 0.5);
				if (progress < 0) {
					progress = 0;
				}
				if (progress > 100) {
					progress = 100;
				}
				mCallBack.progressUpdate(progress);
			}
			invalidate();
			if (getVisibility() == View.VISIBLE && getParent() != null) {
				postDelayed(this, 10);
			}
		}
	};

	public ScanView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public ScanView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ScanView(Context context) {
		super(context);
		init();
	}

	/**
	 * 设置当前位置
	 */
	public void setPosition(int position) {
		mPosition = position;
	}

	private void init() {
		setClickable(false);
		post(mRunnable);
	}

	/**
	 * 设置回调
	 */
	public void setCallBack(ProgressCallBack callback) {
		mCallBack = callback;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mPaint.reset();
		if (isIncreasing) {
			mPaint.setColor(0xFFFFFFFF);
			canvas.drawRect(mPosition - mLineWidth, 0, mPosition, getHeight(),
					mPaint);
			mPaint.reset();
			LinearGradient gradient = new LinearGradient(
					mPosition - mLineWidth, 0, mPosition - mLineWidth
							- mRectWidth, 0, 0x99FFFFFF, 0x00FFFFFF,
					Shader.TileMode.REPEAT);
			mPaint.setShader(gradient);
			canvas.drawRect(mPosition - mLineWidth - mRectWidth, 0, mPosition
					- mLineWidth, getHeight(), mPaint);
		} else {
			mPaint.setColor(0xFFFFFFFF);
			canvas.drawRect(mPosition, 0, mPosition + mLineWidth, getHeight(),
					mPaint);
			mPaint.reset();
			LinearGradient gradient = new LinearGradient(
					mPosition + mLineWidth, 0, mPosition + mLineWidth
							+ mRectWidth, 0, 0x99FFFFFF, 0x00FFFFFF,
					Shader.TileMode.REPEAT);
			mPaint.setShader(gradient);
			canvas.drawRect(mPosition + mLineWidth, 0, mPosition + mLineWidth
					+ mRectWidth, getHeight(), mPaint);
		}
	}

	/**
	 * scanview扫描进度回调
	 */
	public static interface ProgressCallBack {
		public void progressUpdate(int progress);
	}
}
