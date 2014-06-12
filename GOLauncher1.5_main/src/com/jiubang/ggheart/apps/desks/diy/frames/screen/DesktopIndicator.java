package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.IndicatorBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.IndicatorItem;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.IndicatorShowMode;

/**
 * 桌面指示器
 * 
 * @author luopeihuan
 * 
 */
public class DesktopIndicator extends ViewGroup implements AnimationListener {
	private Indicator mIndicator;
	public static final int UPDATE_SCREEN_NUM = 0;
	public static final int UPDATE_SLIDER_INDICATOR = 1;
	public static final int UPDATE_DOTS_INDICATOR = 2;

	// for Bundle
	public static final String OFFSET = "offset";
	public final static String CURRENT = "current";
	public final static String TOTAL = "total";

	public static final int INDICATOR_TYPE_PAGER = 1;
	public static final int INDICATOR_TYPE_SLIDER_TOP = 2;
	public static final int INDICATOR_TYPE_SLIDER_BOTTOM = 3;

	// 自动隐藏的显示时间
	public static final int VISIABLE_DURATION = 300;

	private int mIndicatorType = 1;
	private int mItems = 0;
	private int mCurrent = 0;
	private Animation mAnimation;
	private Handler mHandler = new Handler();
	private int mDotIndicatorHeight;
	private int mSliderIndicatorHeight;
	private int mDefaultDotsIndicatorNormalResID = R.drawable.normalbar;
	private int mDefaultDotsIndicatorLightResID = R.drawable.lightbar;

	private int mDotsMaxNum = 10;

	private SliderIndicator mSliderIndicator;
	private ScreenIndicator mDotsIndicator;

	private boolean mAutoHide = false;
	private boolean mVisible = true;
	// 指示器展现与否的标识，减少频繁地设置指示器的visible
	private boolean mShow = true;
	private IndicatorShowMode mDisplayMode = IndicatorShowMode.None;

	/**
	 * 
	 * @param context
	 *            上下文
	 */
	public DesktopIndicator(Context context) {
		super(context);
		initIndicator(context);
	}

	/**
	 * 
	 * @param context
	 *            上下文
	 * @param attrs
	 *            xml属性
	 */
	public DesktopIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
		initIndicator(context);
	}

	/**
	 * 
	 * @param context
	 *            上下文
	 * @param attrs
	 *            xml属性
	 * @param defStyle
	 *            默认风格
	 */
	public DesktopIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initIndicator(context);
	}

	private void initIndicator(Context context) {
		mDotIndicatorHeight = getResources().getDimensionPixelSize(R.dimen.dots_indicator_height);
		mSliderIndicatorHeight = getResources().getDimensionPixelSize(
				R.dimen.slider_indicator_height);

		mDotsIndicator = new ScreenIndicator(context);
		mDotsIndicator.setDotsImage(mDefaultDotsIndicatorLightResID,
				mDefaultDotsIndicatorNormalResID);
		mDotsIndicator.setScreen(mItems, mCurrent);

		mSliderIndicator = new SliderIndicator(context);
		mSliderIndicator.setIndicator(R.drawable.indicator, R.drawable.indicator_bg);

		// 加载主题
		applyTheme();

		if (showSliderIndicator()) {
			addView(mSliderIndicator);
		} else {
			addView(mDotsIndicator);
		}
	}

	public void setIndicatorHeight(int height) {
		mDotIndicatorHeight = height;
	}

	/**
	 * 设置默认点状页面指示器图片，如果不设置，使用screen里默认的图片
	 * 
	 * @param selected
	 * @param unSelected
	 */
	public void setDefaultDotsIndicatorImage(int selected, int unSelected) {
		mDefaultDotsIndicatorNormalResID = unSelected;
		mDefaultDotsIndicatorLightResID = selected;
		if (null != mDotsIndicator) {
			mDotsIndicator.setDefaultDotsIndicatorImage(mDefaultDotsIndicatorLightResID,
					mDefaultDotsIndicatorNormalResID);
			mDotsIndicator.setDotsImage(mDefaultDotsIndicatorLightResID,
					mDefaultDotsIndicatorNormalResID);
		}
	}

	@Override
	public void addView(View child) {
		if (null == child) {
			return;
		} else if (child instanceof Indicator) {
			mIndicator = (Indicator) child;
		}

		removeAllViews();
		super.addView(child);
	}

	/**
	 * 设置当前屏幕
	 * 
	 * @param items
	 *            屏幕总数
	 */
	public void setTotal(int items) {
		mItems = items;

		if (mItems > mDotsMaxNum) {
			addView(mSliderIndicator);
		} else {
			addView(mDotsIndicator);
		}
		if (null != mIndicator) {
			mIndicator.setTotal(items);
		}
	}

	/**
	 * 更新指示器
	 * 
	 * @param type
	 * @param bundle
	 */
	public void updateIndicator(int type, Bundle bundle) {
		if (type == DesktopIndicator.UPDATE_SCREEN_NUM) {
			final int num = bundle.getInt(DesktopIndicator.TOTAL);
			setTotal(num);
		} else if (type == DesktopIndicator.UPDATE_SLIDER_INDICATOR) {
			final int offset = bundle.getInt(DesktopIndicator.OFFSET);
			setOffset(offset);
		} else if (type == DesktopIndicator.UPDATE_DOTS_INDICATOR) {
			final int current = bundle.getInt(DesktopIndicator.CURRENT);
			setCurrent(current);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int realHeight = 0;
		if (showSliderIndicator()) {
			realHeight = mSliderIndicatorHeight;
		} else {
			realHeight = mDotIndicatorHeight;
		}
		mIndicator.measure(getWidth(), realHeight);

		int realHeightMeasurespec = MeasureSpec.makeMeasureSpec(realHeight, MeasureSpec.EXACTLY);
		super.onMeasure(widthMeasureSpec, realHeightMeasurespec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		LinearLayout.LayoutParams params;
		if (showSliderIndicator()) {
			params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			mIndicator.measure(getWidth(), mSliderIndicatorHeight);
			mIndicator.setLayoutParams(params);
			mIndicator.layout(0, 0, getWidth(), mSliderIndicatorHeight);
		} else {
			params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER;
			mIndicator.measure(getWidth(), mDotIndicatorHeight);
			mIndicator.setLayoutParams(params);
			mIndicator.layout(0, 0, getWidth(), mDotIndicatorHeight);
		}
	}

	public void setDotIndicatorLayoutMode(int mode) {
		if (null != mDotsIndicator) {
			mDotsIndicator.setmLayoutMode(mode);
		}
	}

	public void setDotIndicatorDrawMode(int mode) {
		if (null != mDotsIndicator) {
			mDotsIndicator.setDrawMode(mode);
		}
	}

	/**
	 * 设置百分比（条形指示器）
	 */
	public void setOffset(int offset) {
		if (null != mIndicator && offset != mIndicator.mOffset) {
			// setVisibility(View.VISIBLE);
			mIndicator.setOffset(offset);
			mIndicator.postInvalidate();

			mHandler.removeCallbacks(mAutoHideRunnable);
			if (mAutoHide && mShow) {
				setVisibility(View.VISIBLE);
				mHandler.postDelayed(mAutoHideRunnable, VISIABLE_DURATION);
			}
		}
	}

	/**
	 * 
	 * @param position
	 *            position
	 */
	public void setCurrent(int position) {
		// setVisibility(View.VISIBLE);
		if (null != mIndicator && mItems <= mDotsMaxNum && position >= 0) {
			mIndicator.setCurrent(position);
		}

		mHandler.removeCallbacks(mAutoHideRunnable);
		if (mAutoHide && mShow) {
			setVisibility(View.VISIBLE);
			mHandler.postDelayed(mAutoHideRunnable, VISIABLE_DURATION);
		}
		if (position >= 0) {
			mCurrent = position;
			postInvalidate();
		}
	}

	public int getCurrent() {
		return mCurrent;
	}

	/**
	 * 设置指示器类型：点状或条状
	 * 
	 * @param type
	 *            类型
	 */
	public void setType(int type) {
		if (type != mIndicatorType) {
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(getLayoutParams());
			if (type == INDICATOR_TYPE_SLIDER_BOTTOM) {
				lp.gravity = Gravity.BOTTOM;
			} else {
				lp.gravity = Gravity.TOP;
			}
			setLayoutParams(lp);
			mIndicatorType = type;
			removeAllViews();
			initIndicator(getContext());
		}
	}

	/**
	 * 设置自动隐藏
	 * 
	 * @param autohide
	 *            true自动隐藏
	 */
	public void setAutoHide(boolean autohide) {
		mAutoHide = autohide;
		if (mAutoHide) {
			setVisibility(INVISIBLE);
		} else {
			setVisibility(VISIBLE);
		}
	}

	private Runnable mAutoHideRunnable = new Runnable() {
		@Override
		public void run() {
			if (mAnimation == null) {
				mAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out_fast);
				mAnimation.setAnimationListener(DesktopIndicator.this);
			} else {
				try {
					// This little thing seems to be making some androids piss
					// off
					if (!mAnimation.hasEnded()) {
						mAnimation.reset();
					}
				} catch (NoSuchMethodError e) {
				}
			}
			startAnimation(mAnimation);
		}
	};

	@Override
	public void onAnimationEnd(Animation animation) {
		setVisibility(View.INVISIBLE);
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}

	/**
	 * 隐藏指示器
	 */
	public void hide() {
		mShow = false;
		setVisibility(View.INVISIBLE);
	}

	/**
	 * 显示指示器
	 */
	public void show() {
		mShow = true;
		if (mVisible) {
			if (!mAutoHide) {
				setVisibility(View.VISIBLE);
			}
			mHandler.removeCallbacks(mAutoHideRunnable);
			// if (mAutoHide)
			// {
			// mHandler.postDelayed(mAutoHideRunnable, VISIABLE_DURATION);
			// }
		}
	}

	/**
	 * 设置可见性
	 * 
	 * @param visible
	 *            true 时为可见
	 */
	public void setVisible(boolean visible) {
		if (mVisible != visible) {
			mVisible = visible;
			invalidate();
		}
	}

	public boolean getVisible() {
		return mVisible;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		int visibility = getVisibility();
		if (mVisible && visibility == VISIBLE && null != mIndicator) {
			mIndicator.draw(canvas);
		}
	}

	public void setDisplayMode(IndicatorShowMode mode) {
		if (mDisplayMode != mode) {
			mDisplayMode = mode;
			requestLayout();
		}
	}

	private boolean showSliderIndicator() {
		if (mItems > mDotsMaxNum) {
			return true;
		} else {
			return false;
		}
	}

	public void applyTheme() {
		AppCore appCore = AppCore.getInstance();
		if (null == appCore) {
			// "我的主题"，不同进程，访问不了appcore
			return;
		}
		mDotsIndicator.applyTheme();
		float themeVersion = 0;
		IndicatorBean indicatorBean = null;
		DeskThemeControler themeControler = appCore.getDeskThemeControler();
		String indicatorMode = GoSettingControler.getInstance(GoLauncher.getContext())
				.getScreenStyleSettingInfo().getIndicatorStyle();
		if (themeControler != null && !indicatorMode.equals(ScreenIndicator.SHOWMODE_NORMAL)
				&& !indicatorMode.equals(ScreenIndicator.SHOWMODE_NUMERIC)) {
			DeskThemeBean themeBean = themeControler.getDeskThemeBean();
			if (themeBean != null) {
				themeVersion = themeBean.mDeskVersion;
				// themeVersion = themeBean.getVerId();
				indicatorBean = themeBean.mIndicator;
			}
		}

		// 由于以前的条状指示器主题配置错误
		// 1以下版本不支持
		float version = 1.0f;
		if (indicatorBean != null && themeVersion > version) {
			// mDotsMaxNum = indicatorBean.mWhenScreenCount;
			// setDisplayMode(indicatorBean.mIndicatorShowMode);
			if (indicatorBean.mSlide != null) {
				setSlideIndicator(indicatorBean.mSlide, themeControler);
			}
		} else // default theme
		{
			mDotsMaxNum = 10;
			setDisplayMode(IndicatorShowMode.None);
			setSlideIndicator(null, null);
		}
	}

	private void setSlideIndicator(IndicatorItem item, DeskThemeControler controler) {
		// IndicatorItem.mSelectedBitmap-->Indicator
		// IndicatorItem.mUnSelectedBitmap-->IndicatorBG
		if (item != null && controler != null) {
			Drawable indicator = null;
			if (null != item.mSelectedBitmap) {
				indicator = controler.getThemeResDrawable(item.mSelectedBitmap.mResName);

				if (null == indicator) {
					indicator = getResources().getDrawable(R.drawable.indicator);
				}
			}
			if (null != indicator) {
				Drawable indicatorBG = null;
				if (null != item.mUnSelectedBitmap) {
					indicatorBG = controler.getThemeResDrawable(item.mUnSelectedBitmap.mResName);

					if (null == indicatorBG) {
						indicatorBG = getResources().getDrawable(R.drawable.indicator_bg);
					}
				}
				mSliderIndicator.setIndicator(indicator, indicatorBG);
				return;
			}
		}
		// else
		{
			mSliderIndicator.setIndicator(R.drawable.indicator, R.drawable.indicator_bg);
		}
	}

	public void setIndicatorListner(IndicatorListner listner) {
		if (null != mDotsIndicator) {
			mDotsIndicator.setListner(listner);
		}
		if (null != mSliderIndicator) {
			mSliderIndicator.setListner(listner);
		}
	}

	public void doWithShowModeChanged() {
		if (null != mDotsIndicator) {
			mDotsIndicator.doWithShowModeChanged();
		}
	}

	/***
	 * 设置指示器是否响应触摸事件
	 * @param touch
	 */
	public void setTouchable(boolean touch) {
		if (mIndicator != null) {
			mIndicator.mIsCanTouch = touch;
		}
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		// TODO Auto-generated method stub
		try {
			super.onRestoreInstanceState(state);
		} catch (Exception e) {
			// TODO: handle exception
			Log.i("DesktopIndicator", "onRestoreInstanceState has exception " + e.getMessage());
		}
	}

}
