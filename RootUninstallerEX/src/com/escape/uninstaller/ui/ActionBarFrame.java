package com.escape.uninstaller.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.escape.uninstaller.util.DrawUtil;

public class ActionBarFrame extends LinearLayout {

	private final int MAX_HEIGHT = DrawUtil.dip2px(getContext(), 48);
	// 时间间隔
	private final int INTERVAL = 7;
	// 距离间隔
	private int STEP = 7;
	// 目标高度
	private int mTarget = MAX_HEIGHT;

	private boolean mIsActive = true;

	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			removeCallbacks(mRunnable);
			removeCallbacks(this);
			if (!mIsActive) {
				return;
			}
			int height = getHeight();
			Log.e("", "ActionBarFrame height = " + height);
			if (height == mTarget) {
				return;
			}
			if (height < mTarget) {
				getLayoutParams().height = (height + STEP) > mTarget ? mTarget
						: (height + STEP);
			} else {
				getLayoutParams().height = (height - STEP) < mTarget ? mTarget
						: (height - STEP);
			}
			requestLayout();
			postDelayed(mRunnable, INTERVAL);
		}
	};

	public ActionBarFrame(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ActionBarFrame(Context context) {
		super(context);
		init();
	}

	private void init() {
		mIsActive = true;
	}

	public void up() {
		// 高度逐渐缩小
		post(new Runnable() {

			@Override
			public void run() {
				mTarget = 0;
				removeCallbacks(mRunnable);
				post(mRunnable);
			}
		});
	}

	public void down() {
		// 高度逐渐扩大
		post(new Runnable() {

			@Override
			public void run() {
				mTarget = MAX_HEIGHT;
				removeCallbacks(mRunnable);
				post(mRunnable);
			}
		});
	}

	public void stop() {
		mIsActive = false;
	}
}
