/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gau.go.launcherex.R;
import com.go.util.AppUtils;
import com.jiubang.ggheart.apps.desks.diy.LockScreenHandler;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.statistics.FunctionalStatistic;
import com.jiubang.ggheart.launcher.GOLauncherApp;

public class Search extends LinearLayout
		implements
			OnClickListener,
			OnKeyListener,
			OnLongClickListener {

	// for bundle
	public static final String FIELD_INITIAL_QUERY = "initial_query";
	public static final String FIELD_SELECT_INITIAL_QUERY = "select_initial_query";
	public static final String FIELD_SEARCH_DATA = "search_data";
	public static final String FIELD_GLOBAL_SEARCH = "global_search";

	// Speed at which the widget slides up/down, in pixels/ms.
	private static final float ANIMATION_VELOCITY = 1.0f;

	/**
	 * The distance in dips between the optical top of the widget and the top if
	 * its bounds
	 */
	private static final float WIDGET_TOP_OFFSET = 9;

	private static final String TAG = "SearchWidget";

	// private Activity mActivity;

	private TextView mSearchText;
	private ImageButton mVoiceButton;

	/** The animation that morphs the search widget to the search dialog. */
	private Animation mMorphAnimation;

	/** The animation that morphs the search widget back to its normal position. */
	private Animation mUnmorphAnimation;

	// These four are passed to Launcher.startSearch() when the search widget
	// has finished morphing. They are instance variables to make it possible to
	// update
	// them while the widget is morphing.
	private String mInitialQuery;
	private boolean mSelectInitialQuery;
	private Bundle mAppSearchData;
	private boolean mGlobalSearch;

	// For voice searching
	private Intent mVoiceSearchIntent;

	private int mWidgetTopOffset;

	private ISearchEventListener mListener;

	/**
	 * Used to inflate the Workspace from XML.
	 * 
	 * @param context
	 *            The application's context.
	 * @param attrs
	 *            The attributes set containing the Workspace's customization
	 *            values.
	 */
	public Search(Context context, AttributeSet attrs) {
		super(context, attrs);

		final float scale = context.getResources().getDisplayMetrics().density;
		mWidgetTopOffset = Math.round(WIDGET_TOP_OFFSET * scale);

		Interpolator interpolator = new AccelerateDecelerateInterpolator();

		mMorphAnimation = new ToParentOriginAnimation();
		// no need to apply transformation before the animation starts,
		// since the gadget is already in its normal place.
		mMorphAnimation.setFillBefore(false);
		// stay in the top position after the animation finishes
		mMorphAnimation.setFillAfter(true);
		mMorphAnimation.setInterpolator(interpolator);
		mMorphAnimation.setAnimationListener(new Animation.AnimationListener() {
			// The amount of time before the animation ends to show the search
			// dialog.
			private static final long TIME_BEFORE_ANIMATION_END = 80;

			// The runnable which we'll pass to our handler to show the search
			// dialog.
			private final Runnable mShowSearchDialogRunnable = new Runnable() {
				@Override
				public void run() {
					showSearchDialog();
				}
			};

			@Override
			public void onAnimationEnd(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// Make the search dialog show up ideally *just* as the
				// animation reaches
				// the top, to aid the illusion that the widget becomes the
				// search dialog.
				// Otherwise, there is a short delay when the widget reaches the
				// top before
				// the search dialog shows. We do this roughly 80ms before the
				// animation ends.
				getHandler().postDelayed(mShowSearchDialogRunnable,
						Math.max(mMorphAnimation.getDuration() - TIME_BEFORE_ANIMATION_END, 0));
			}
		});

		mUnmorphAnimation = new FromParentOriginAnimation();
		// stay in the top position until the animation starts
		mUnmorphAnimation.setFillBefore(true);
		// no need to apply transformation after the animation finishes,
		// since the gadget is now back in its normal place.
		mUnmorphAnimation.setFillAfter(false);
		mUnmorphAnimation.setInterpolator(interpolator);
		mUnmorphAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationEnd(Animation animation) {
				clearAnimation();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}
		});

		mVoiceSearchIntent = new Intent(android.speech.RecognizerIntent.ACTION_WEB_SEARCH);
		mVoiceSearchIntent.putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				android.speech.RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
	}

	/**
	 * Implements OnClickListener.
	 */
	@Override
	public void onClick(View v) {
		if (v == mVoiceButton) {
			startVoiceSearch();
		} else {
			if (mListener != null) {
				mListener.showSearchDialog(null, false, null, true);

				// 统计
				FunctionalStatistic st = AppCore.getInstance().getFunctionalStatistic();
				if (null != st) {
					st.statistic(FunctionalStatistic.Rule.RULE_ADDVALUE, "widget_search_key", 1);
				}
			}
		}
	}

	private void startVoiceSearch() {
		AppUtils.safeStartActivity(getContext(), mVoiceSearchIntent);
	}

	/**
	 * Sets the query text. The query field is not editable, instead we forward
	 * the key events to the launcher, which keeps track of the text, calls
	 * setQuery() to show it, and gives it to the search dialog.
	 */
	public void setQuery(String query) {
		mSearchText.setText(query, TextView.BufferType.NORMAL);
	}

	/**
	 * Morph the search gadget to the search dialog. See
	 * {@link Activity#startSearch()} for the arguments.
	 */
	public void startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData,
			boolean globalSearch) {
		mInitialQuery = initialQuery;
		mSelectInitialQuery = selectInitialQuery;
		mAppSearchData = appSearchData;
		mGlobalSearch = globalSearch;

		showSearchDialog();

	}

	/**
	 * Shows the system search dialog immediately, without any animation.
	 */
	private void showSearchDialog() {
		// TODO 屏幕层通过消息发送给调度层
		if (mListener != null) {
			mListener.showSearchDialog(mInitialQuery, mSelectInitialQuery, mAppSearchData,
					mGlobalSearch);
		}
	}

	/**
	 * Restore the search gadget to its normal position.
	 * 
	 * @param animate
	 *            Whether to animate the movement of the gadget.
	 */
	public void stopSearch(boolean animate) {
		setQuery("");

		// Only restore if we are not already restored.
		if (getAnimation() == mMorphAnimation) {
			if (animate && !isAtTop()) {
				mUnmorphAnimation.setDuration(getAnimationDuration());
				startAnimation(mUnmorphAnimation);
			} else {
				clearAnimation();
			}
		}
	}

	private boolean isAtTop() {
		return getWidgetTop() == 0;
	}

	private int getAnimationDuration() {
		return (int) (getWidgetTop() / ANIMATION_VELOCITY);
	}

	/**
	 * Modify clearAnimation() to invalidate the parent. This works around an
	 * issue where the region where the end of the animation placed the view was
	 * not redrawn after clearing the animation.
	 */
	@Override
	public void clearAnimation() {
		Animation animation = getAnimation();
		if (animation != null) {
			super.clearAnimation();
			if (animation.hasEnded() && animation.getFillAfter() && animation.willChangeBounds()) {
				ViewParent parent = getParent();
				if (parent != null && parent instanceof View) {
					((View) parent).invalidate();
				}
			} else {
				invalidate();
			}
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (!event.isSystem() && (keyCode != KeyEvent.KEYCODE_DPAD_UP)
				&& (keyCode != KeyEvent.KEYCODE_DPAD_DOWN)
				&& (keyCode != KeyEvent.KEYCODE_DPAD_LEFT)
				&& (keyCode != KeyEvent.KEYCODE_DPAD_RIGHT)
				&& (keyCode != KeyEvent.KEYCODE_DPAD_CENTER) && (mListener != null)) {
			// Forward key events to Launcher, which will forward text
			// to search dialog
			// TODO 由屏幕层转发给调度层进行处理
			switch (event.getAction()) {
				case KeyEvent.ACTION_DOWN :
					return mListener.onSearchKeyDown(keyCode, event);
				case KeyEvent.ACTION_MULTIPLE :
					return mListener.onSearchKeyMultiple(keyCode, event.getRepeatCount(), event);
				case KeyEvent.ACTION_UP :
					return mListener.onSearchKeyUp(keyCode, event);
			}
		}
		return false;
	}

	/**
	 * Implements OnLongClickListener to pass long clicks on child views to the
	 * widget. This makes it possible to pick up the widget by long clicking on
	 * the text field or a button.
	 */
	@Override
	public boolean onLongClick(View v) {
		// 判断当前是否锁屏
		if (GOLauncherApp.getSettingControler().getScreenSettingInfo().mLockScreen) {
			LockScreenHandler.showLockScreenNotification(getContext());
			return true;
		}

		return performLongClick();
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mSearchText = (TextView) findViewById(R.id.search_src_text);
		mVoiceButton = (ImageButton) findViewById(R.id.search_voice_btn);

		// Set the placeholder text to be the Google logo within the search
		// widget.
		Drawable googlePlaceholder = null;
		// 这里有可能会出现爆内存
		try {
			googlePlaceholder = getContext().getResources().getDrawable(
					R.drawable.placeholder_google);
		} catch (OutOfMemoryError e) {
			Log.i(TAG, this.getClass().toString()+" onFinishInflate() out of memory");
		}

		if (mSearchText != null) {
			mSearchText.setOnKeyListener(this);
			mSearchText.setOnClickListener(this);
			mSearchText.setOnLongClickListener(this);
			mSearchText
					.setCompoundDrawablesWithIntrinsicBounds(googlePlaceholder, null, null, null);
		}

		if (mVoiceButton != null) {
			mVoiceButton.setOnClickListener(this);
			mVoiceButton.setOnLongClickListener(this);
		}

		setOnClickListener(this);
		configureVoiceSearchButton();
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	/**
	 * If appropriate & available, configure voice search
	 * 
	 * Note: Because the home screen search widget is always web search, we only
	 * check for getVoiceSearchLaunchWebSearch() modes. We don't support the
	 * alternate form of app-specific voice search.
	 */
	private void configureVoiceSearchButton() {
		// Enable the voice search button if there is an activity that can
		// handle it
		PackageManager pm = getContext().getPackageManager();
		ResolveInfo ri = pm.resolveActivity(mVoiceSearchIntent, PackageManager.MATCH_DEFAULT_ONLY);
		boolean voiceSearchVisible = ri != null;

		// finally, set visible state of voice search button, as appropriate
		mVoiceButton.setVisibility(voiceSearchVisible ? View.VISIBLE : View.GONE);
	}

	/**
	 * 设置搜索栏监听
	 * 
	 * @param listener
	 *            监听者
	 */
	public void setSearchEventListener(ISearchEventListener listener) {
		mListener = listener;
	}

	/**
	 * 搜索栏对外的监听回调接口
	 * 
	 * @author yuankai
	 * @version 1.0
	 */
	public interface ISearchEventListener {
		/**
		 * 搜索栏按下
		 * 
		 * @param keyCode
		 *            键值
		 * @param event
		 *            事件
		 * @return 是否处理
		 */
		public boolean onSearchKeyDown(int keyCode, KeyEvent event);

		/**
		 * 搜索栏放开
		 * 
		 * @param keyCode
		 *            键值
		 * @param event
		 *            事件
		 * @return 是否处理
		 */
		public boolean onSearchKeyUp(int keyCode, KeyEvent event);

		/**
		 * 搜索栏长按
		 * 
		 * @param keyCode
		 *            键值
		 * @param repeatCount
		 *            重复次数
		 * @param event
		 *            事件
		 * @return 是否处理
		 */
		public boolean onSearchKeyMultiple(int keyCode, int repeatCount, KeyEvent event);

		/**
		 * 要求显示搜索对话框
		 * 
		 * @param initialQuery
		 *            查询文字
		 * @param selectInitialQuery
		 * @param appSearchData
		 * @param globalSearch
		 * @return 是否处理
		 */
		public boolean showSearchDialog(String initialQuery, boolean selectInitialQuery,
				Bundle appSearchData, boolean globalSearch);
	};

	/**
	 * Moves the view to the top left corner of its parent.
	 */
	private class ToParentOriginAnimation extends Animation {
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			float dx = -getLeft() * interpolatedTime;
			float dy = -getWidgetTop() * interpolatedTime;
			t.getMatrix().setTranslate(dx, dy);
		}
	}

	/**
	 * Moves the view from the top left corner of its parent.
	 */
	private class FromParentOriginAnimation extends Animation {
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			float dx = -getLeft() * (1.0f - interpolatedTime);
			float dy = -getWidgetTop() * (1.0f - interpolatedTime);
			t.getMatrix().setTranslate(dx, dy);
		}
	}

	/**
	 * The widget is centered vertically within it's 4x1 slot. This is
	 * accomplished by nesting the actual widget inside another view. For
	 * animation purposes, we care about the top of the actual widget rather
	 * than it's container. This method return the top of the actual widget.
	 */
	private int getWidgetTop() {
		return getTop() + getChildAt(0).getTop() + mWidgetTopOffset;
	}

}
