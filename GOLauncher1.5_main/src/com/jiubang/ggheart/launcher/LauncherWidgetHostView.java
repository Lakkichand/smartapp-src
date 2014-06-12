/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jiubang.ggheart.launcher;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;

/**
 * {@inheritDoc}
 */
public class LauncherWidgetHostView extends AppWidgetHostView {

	private static final long WIDGET_LONG_CLICK_TIMEOUT = 700;

	// private static final String TAG = "LauncherAppWidgetHostView";

	private boolean mHasPerformedLongPress;

	private CheckForLongPress mPendingCheckForLongPress;

	private LayoutInflater mInflater;

	private int mTouchSlop;

	public LauncherWidgetHostView(Context context) {
		super(context);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
	}

	@Override
	protected View getErrorView() {
		try {
			return mInflater.inflate(R.layout.widget_error, this, false);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void addView(View child) {
		if (child == null) {
			return;
		}
		super.addView(child);
	}

	@Override
	public void addView(View child, int index) {
		if (child == null) {
			return;
		}
		super.addView(child, index);
	}

	@Override
	public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
		if (child == null) {
			return;
		}
		super.addView(child, index, params);
	}

	@Override
	public void addView(View child, int width, int height) {
		if (child == null) {
			return;
		}
		super.addView(child, width, height);
	}

	@Override
	public void addView(View child, android.view.ViewGroup.LayoutParams params) {
		if (child == null) {
			return;
		}
		super.addView(child, params);
	}

	@Override
	protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
		try {
			super.dispatchRestoreInstanceState(container);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private float startY;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// Consume any touch events for ourselves after longpress is triggered
		if (mHasPerformedLongPress) {
			mHasPerformedLongPress = false;
			return true;
		}

		// Watch for longpress events at this level to make sure
		// users can always pick up this widget
		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN :
				startY = ev.getY();
				postCheckForLongClick();
				break;

			case MotionEvent.ACTION_MOVE :
				if (Math.abs(ev.getY() - startY) < mTouchSlop) {
					return false;
				}

			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :
				mHasPerformedLongPress = false;
				if (mPendingCheckForLongPress != null) {
					removeCallbacks(mPendingCheckForLongPress);
				}
				break;

			default :
				break;
		}

		// Otherwise continue letting touch events fall through to children
		return false;
	}

	class CheckForLongPress implements Runnable {
		private int mOriginalWindowAttachCount;

		@Override
		public void run() {
			if ((getParent() != null) && hasWindowFocus()
					&& mOriginalWindowAttachCount == getWindowAttachCount()
					&& !mHasPerformedLongPress) {
				performLongClick();
			}
		}

		public void rememberWindowAttachCount() {
			mOriginalWindowAttachCount = getWindowAttachCount();
		}
	}

	private void postCheckForLongClick() {
		mHasPerformedLongPress = false;

		if (mPendingCheckForLongPress == null) {
			mPendingCheckForLongPress = new CheckForLongPress();
		}
		mPendingCheckForLongPress.rememberWindowAttachCount();
		postDelayed(mPendingCheckForLongPress, WIDGET_LONG_CLICK_TIMEOUT);
	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();

		mHasPerformedLongPress = false;
		if (mPendingCheckForLongPress != null) {
			removeCallbacks(mPendingCheckForLongPress);
		}
	}

	@Override
	protected void prepareView(View view) {
		if (view == null) {
			return;
		}
		super.prepareView(view);
	}

	@Override
	public boolean performLongClick() {
		if (!mHasPerformedLongPress) {
			mHasPerformedLongPress = true;
			return super.performLongClick();
		}
		return true;
	}
}
