package com.jiubang.ggheart.apps.desks.diy.frames.screen;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.gau.go.launcherex.R;
import com.jiubang.ggheart.apps.desks.diy.OutOfMemoryHandler;
import com.jiubang.ggheart.data.AppCore;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.theme.DeskThemeControler;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.IndicatorBean;
import com.jiubang.ggheart.data.theme.bean.DeskThemeBean.IndicatorItem;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 屏幕指示器
 * 
 * @author luopeihuan
 * 
 */
public class ScreenIndicator extends Indicator {
	public static String sShowmode; // 显示模式
	public static final String SHOWMODE_NORMAL = ThemeManager.DEFAULT_THEME_PACKAGE; // 正常模式
	public static final String SHOWMODE_NUMERIC = "Numeric Style"; // 数字模式

	public final static String INDICRATOR_ON_TOP = "top";
	public final static String INDICRATOR_ON_BOTTOM = "bottom";

	public static final int LAYOUT_MODE_NORMAL = 1; // 正常模式，按照指定尺寸，增加边距
	public static final int LAYOUT_MODE_ADJUST_PICSIZE = 2; // 适应图片尺寸模式，不加边距
	private int mLayoutMode = LAYOUT_MODE_NORMAL; // 排版模式

	private int mDrawMode = ScreenIndicatorItem.DRAW_MODE_GENERAL;

	private int mDefaultDotsIndicatorNormalResID = R.drawable.normalbar;
	private int mDefaultDotsIndicatorLightResID = R.drawable.lightbar;

	private Drawable mFocus;
	private Drawable mUnfocus;
	private int mCellSize; // 每个点的宽度

	private int mIndicatorL;
	private int mIndicatorR;

	/**
	 * 
	 * @param context
	 *            context
	 */
	public ScreenIndicator(Context context) {
		this(context, null);
	}

	/**
	 * 
	 * @param context
	 *            context
	 * @param att
	 *            属性集
	 */
	public ScreenIndicator(Context context, AttributeSet att) {
		this(context, att, 0);
	}

	/**
	 * @param context
	 *            context
	 * @param att
	 *            属性集
	 * @param defStyle
	 *            默认风格
	 */
	public ScreenIndicator(Context context, AttributeSet att, int defStyle) {
		super(context, att, defStyle);
		TypedArray a = context.obtainStyledAttributes(att, R.styleable.ScreenIndicator);

		mCellSize = a.getDimensionPixelSize(R.styleable.ScreenIndicator_dotWidth, 32);
		a.recycle();
		sShowmode = GoSettingControler.getInstance(context).getScreenStyleSettingInfo()
				.getIndicatorStyle();
		applyTheme();

	}

	private void setDotsImage(Drawable selected, Drawable unSelected) {
		mFocus = selected;
		mUnfocus = unSelected;

		if (mFocus != null) {
			mFocus.setBounds(0, 0, mFocus.getIntrinsicWidth(), mFocus.getIntrinsicHeight());
		}

		if (mUnfocus != null) {
			mUnfocus.setBounds(0, 0, mUnfocus.getIntrinsicWidth(), mUnfocus.getIntrinsicHeight());
		}
		updateContent();
		initPadding();
	}

	public void setDotsImage(int selected, int unSelected) {
		try {
			final Drawable focusDrawable = getDrawable(selected);
			final Drawable unFocusDrawable = getDrawable(unSelected);
			setDotsImage(focusDrawable, unFocusDrawable);
		} catch (OutOfMemoryError e) {
			OutOfMemoryHandler.handle();
			setDotsImage(null, null);
		}
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
	}

	private void updateContent() {
		int childcount = getChildCount();
		for (int i = 0; i < childcount; i++) {
			ScreenIndicatorItem view = (ScreenIndicatorItem) getChildAt(i);
			if (i != mCurrent) {
				view.setImageDrawable(mUnfocus);
			} else {
				view.setImageDrawable(mFocus);
			}
		}
	}

	/**
	 * 设置每个点宽度
	 * 
	 * @param width
	 *            宽度
	 */
	public void setDotWidth(int width) {
		mCellSize = width;
	}

	/**
	 * 设置指示器圆点总数
	 * 
	 * @param total
	 *            圆点总数
	 */
	@Override
	public void setTotal(int total) {
		if (total < 0) {
			return;
		}
		mTotal = total;

		int childcound = getChildCount();
		int dis = total - childcound;
		if (dis == 0) {
			return;
		}
		// dis > 0
		for (; dis > 0; dis--) {
			ScreenIndicatorItem imageView = new ScreenIndicatorItem(getContext());
			Drawable drawable = (mCurrent == getChildCount()) ? mFocus : mUnfocus;
			imageView.setImageDrawable(drawable);
			imageView.mIndex = getChildCount();
			// 这里加点击监听者原因是：如果没有onClickListner或onTouchListner，只会接收到MotionEvent.ACTION_DOWN事件
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (null != mListner) {
						// do nothing
					}
				}
			});

			imageView.setmDrawMode(mDrawMode);

			addView(imageView);
		}
		initPadding();
		// dis < 0
		for (; dis < 0; dis++) {
			removeViewAt(getChildCount() - 1);
		}
	}

	private void initPadding() {
		Drawable drawable = (mCurrent == getChildCount()) ? mFocus : mUnfocus;
		int w = (null == drawable) ? 0 : drawable.getIntrinsicWidth();

		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			int paddingL = (mCellSize - w) >> 1;
			int paddingR = paddingL;
			int paddingT = (mCellSize - w) >> 1;
			int paddingB = paddingT;
			View child = getChildAt(i);
			child.setPadding(paddingL, paddingT, paddingR, paddingB);
		}
	}

	/**
	 * 设置当前圆点
	 * 
	 * @param current
	 *            当前圆点索引
	 */
	@Override
	public void setCurrent(int current) {
		int childcount = getChildCount();
		if (0 <= mCurrent && mCurrent < childcount) {
			((ImageView) getChildAt(mCurrent)).setImageDrawable(mUnfocus);
		}
		if (0 <= current && current < childcount) {
			((ImageView) getChildAt(current)).setImageDrawable(mFocus);
			mCurrent = current;
		}
	}

	/**
	 * 
	 * @param current
	 *            当前屏幕
	 * @param total
	 *            屏幕总数
	 */
	public void setScreen(int current, int total) {
		if (current >= total || total <= 0) {
			return;
		}

		setTotal(total);
		setCurrent(current);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		// 只有一个点的时候不用绘制
		if (mFocus == null || mUnfocus == null) {
			return;
		}
		if (mTotal <= 1) {
			removeAllViews();
			return;
		}

		if (mLayoutMode == LAYOUT_MODE_NORMAL) {
			onLayoutNormal(changed, left, top, right, bottom);
		} else if (mLayoutMode == LAYOUT_MODE_ADJUST_PICSIZE) {
			onLayoutAdjustPicSize(changed, left, top, right, bottom);
		}
	}

	private void onLayoutNormal(boolean changed, int left, int top, int right, int bottom) {
		final int width = right - left;
		final int realWidth = mCellSize * mTotal;
		int offset = (width - realWidth) / 2;
		// 居中补差
		int unFocusW = mUnfocus.getIntrinsicWidth();
		int dis = (mCellSize - unFocusW) >> 1;
		dis = dis < 0 ? 0 : dis;
		offset += dis;

		int childcount = getChildCount();
		for (int i = 0; i < childcount; i++) {
			View view = getChildAt(i);
			view.layout(offset, 0, offset + mCellSize, bottom - top);
			offset += mCellSize;

			if (sShowmode.equals(SHOWMODE_NUMERIC)) {
				((ScreenIndicatorItem) view).updateTextBound();
			}
		}
	}

	private void onLayoutAdjustPicSize(boolean changed, int left, int top, int right, int bottom) {
		final int width = right - left;
		int unFocusW = mUnfocus.getIntrinsicWidth();
		final int realWidth = unFocusW * mTotal;
		int offset = (width - realWidth) / 2;

		int childcount = getChildCount();
		for (int i = 0; i < childcount; i++) {
			View view = getChildAt(i);
			view.setPadding(0, 0, 0, 0);
			view.layout(offset, 0, offset + unFocusW, bottom);
			offset += unFocusW;

			if (sShowmode.equals(SHOWMODE_NUMERIC)) {
				((ScreenIndicatorItem) view).updateTextBound();
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mFocus != null) {
			mFocus.setBounds(0, 0, mFocus.getIntrinsicWidth(), mFocus.getIntrinsicHeight());
		}

		if (mUnfocus != null) {
			mUnfocus.setBounds(0, 0, mUnfocus.getIntrinsicWidth(), mUnfocus.getIntrinsicHeight());
		}
	}

	public void applyTheme() {
		AppCore appCore = AppCore.getInstance();
		if (null == appCore) {
			// "我的主题"，不同进程，访问不了appcore
			return;
		}

		IndicatorBean indicatorBean = null;
		DeskThemeControler themeControler = appCore.getDeskThemeControler();
		String modeString = GOLauncherApp.getSettingControler().getScreenStyleSettingInfo()
				.getIndicatorStyle();
		sShowmode = modeString;
		boolean applyTheme = true;
		if (modeString.equals(ScreenIndicator.SHOWMODE_NORMAL)
				|| modeString.equals(ScreenIndicator.SHOWMODE_NUMERIC)) {
			applyTheme = false;
		}
		if (themeControler != null && applyTheme) {
			DeskThemeBean themeBean = themeControler.getDeskThemeBean();
			if (themeBean != null) {
				indicatorBean = themeBean.mIndicator;
			}
		}

		if (indicatorBean != null && indicatorBean.mDots != null) {
			setDotIndicator(indicatorBean.mDots, themeControler);
		} else {
			setDotIndicator(null, null);
		}
	}

	public void setDotIndicator(IndicatorItem item, DeskThemeControler controler) {
		if (item != null && controler != null) {
			Drawable focusDrawable = null;
			Drawable unFocusDrawable = null;
			if (!sShowmode.equals(ScreenIndicator.SHOWMODE_NUMERIC)) {
				String packageName = controler.getDeskThemeBean().mIndicator.getPackageName();
				if (null == packageName) {

					packageName = GOLauncherApp.getThemeManager().getCurThemePackage();
				}
				if (item.mSelectedBitmap != null) {

					focusDrawable = controler.getDrawable(item.mSelectedBitmap.mResName,
							mDefaultDotsIndicatorLightResID, packageName);
				} else {
					getDrawable(mDefaultDotsIndicatorLightResID);
				}

				if (item.mUnSelectedBitmap != null) {
					unFocusDrawable = controler.getDrawable(item.mUnSelectedBitmap.mResName,
							mDefaultDotsIndicatorNormalResID, packageName);
				} else {
					getDrawable(mDefaultDotsIndicatorNormalResID);
				}
			} else if (sShowmode.equals(ScreenIndicator.SHOWMODE_NUMERIC)) {
				focusDrawable = getDrawable(R.drawable.focus_indicator_numeric);
				unFocusDrawable = getDrawable(R.drawable.unfocus_indicator_numeric);
			}

			setDotsImage(focusDrawable, unFocusDrawable);
			if (/* item.mWidth > 0 && */item.mWidth >= mCellSize) {
				setDotWidth(item.mWidth);
			} else {
				setDotWidth(getResources().getDimensionPixelSize(R.dimen.dots_indicator_width));
			}
		} else {
			mCellSize = getResources().getDimensionPixelSize(R.dimen.dots_indicator_width);
			// if
			// (sShowmode.equals(ScreenIndicator.SHOWMODE_NORMAL)||sShowmode.equals(ThemeManager.DEFAULT_THEME_PACKAGE_3))
			// {
			// //3.0主题暂时使用默认主题的指示器，add by yangbing 2012-05-08
			// setDotsImage(mDefaultDotsIndicatorLightResID,
			// mDefaultDotsIndicatorNormalResID);
			// }else
			if (sShowmode.equals(ScreenIndicator.SHOWMODE_NUMERIC)) {
				setDotsImage(R.drawable.focus_indicator_numeric,
						R.drawable.unfocus_indicator_numeric);
			} else {
				setDotsImage(mDefaultDotsIndicatorLightResID, mDefaultDotsIndicatorNormalResID);
			}
		}
		requestLayout();
	}

	@Override
	public void doWithShowModeChanged() {
		applyTheme();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		boolean ret = super.onInterceptTouchEvent(ev);

		if (null != mListner) {
			int action = ev.getAction();
			switch (action) {
				case MotionEvent.ACTION_DOWN : {
					mMovePercent = 0.0f;
					mIndicatorL = 0;
					mIndicatorR = 0;

					int childcount = getChildCount();
					if (childcount > 0) {
						mIndicatorL = getChildAt(0).getLeft();
						mIndicatorR = getChildAt(childcount - 1).getRight();
					}
					break;
				}

				case MotionEvent.ACTION_MOVE : {
					if (mMoveDirection != Indicator.MOVE_DIRECTION_NONE) {
						float x = ev.getRawX();

						int childcount = getChildCount();
						if (childcount > 0) {
							int left = getChildAt(0).getLeft();
							int right = getChildAt(childcount - 1).getRight();
							int width = right - left;
							if (left < x && x < right) {
								mMovePercent = ((x - left) * 100) / width;
								mListner.sliding(mMovePercent);
							}
						}
					}
					break;
				}

				case MotionEvent.ACTION_CANCEL :
				case MotionEvent.ACTION_UP : {
					int childcount = getChildCount();
					if (childcount > 0) {
						int x = (int) ev.getRawX();
						if (x <= mIndicatorL) {
							mListner.clickIndicatorItem(0);
						} else if (mIndicatorL < x && x < mIndicatorR) {
							int width = mIndicatorR - mIndicatorL;
							int index = (int) ((((float) (x - mIndicatorL)) / ((float) width)) * childcount);
							mListner.clickIndicatorItem(index);
						} else if (mIndicatorR <= x) {
							mListner.clickIndicatorItem(childcount - 1);
						}
					}
					break;
				}

				default :
					break;
			}
		}

		return ret;
	}

	/**
	 * @param mLayoutMode
	 *            the mLayoutMode to set
	 */
	public void setmLayoutMode(int mLayoutMode) {
		this.mLayoutMode = mLayoutMode;
	}

	public void setDrawMode(int mode) {
		mDrawMode = mode;
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			ScreenIndicatorItem child = (ScreenIndicatorItem) getChildAt(i);
			child.setmDrawMode(mode);
		}
	}

	private Drawable getDrawable(int id) {
		Drawable drawable = null;
		if (ImageExplorer.getInstance(getContext()) != null) {
			drawable = ImageExplorer.getInstance(getContext()).getDefaultDrawable(id);
		}
		if (null == drawable) {
			drawable = getResources().getDrawable(id);
		}
		return drawable;
	}
}
