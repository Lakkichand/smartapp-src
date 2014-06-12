package com.jiubang.ggheart.appgame.appcenter.component;

import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.gau.go.launcherex.R;
import com.go.util.device.Machine;
import com.go.util.graphics.DrawUtils;
import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.apps.gowidget.gostore.views.ScrollerViewGroup;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  
 * @date  [2012-10-10]
 */
public abstract class PagerActionBar extends View {
	/**
	 * Indicates that the pager is in an idle, settled state. The current page
	 * is fully in view and no animation is in progress.
	 */
	public static final int SCROLL_STATE_IDLE = 0;

	/**
	 * Indicates that the pager is currently being dragged by the user.
	 */
	public static final int SCROLL_STATE_DRAGGING = 1;

	/**
	 * Indicates that the pager is in the process of settling to a final
	 * position.
	 */
	public static final int SCROLL_STATE_SETTLING = 2;

	private String mTag = "PagerActionBar";
	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	/**
	 * 绘制更新数字的画笔
	 */
	private Paint mUpdatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private int mTitleTextColor;
	private int mSelectedItemTextColor;
	// private int mFooterSeparatorColor;
	private int mSelectorColor;
	private float mFooterSeparatorLineWeight; // 底部分隔线线条宽度
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

	// 左右tab超过左右边界的距离
	private final static int PADDING = 0;

	private final static int ICON_OFFSET_X = DrawUtils.dip2px(2);
	private final static int ICON_OFFSET_Y = DrawUtils.dip2px(13);

	// private ViewPager mViewPager;
	// private PageTitleProvider mPageTitleProvider;

	/**
	 * tab栏下面主页面的内容
	 */
	private ScrollerViewGroup mScrollerViewGroup;

	/**
	 * tab栏显示的tab名称集合
	 */
	private List<String> mTitleList;

	/**
	 * 应用更新tab上显示的更新个数
	 */
	private int mUpdateCount = 0;

	/**
	 * tab栏点击的监听类
	 */
	private TabOnClickListener mTabClickListener;

	/**
	 * tab的遮盖层，为了左右tab显示模糊效果
	 */
	private Bitmap mBgBit = null;

	/**
	 * 横竖屏状态
	 */
	private boolean mIsPortait = true;

	/**
	 * 应用更新tab名称
	 */
	private String mAppsUpdateTabStr = null;

	/**
	 * 是否需要横竖屏切换，默认是竖屏;横竖屏切换主要用来画遮罩层，横竖屏的遮罩层不一样
	 */
	private boolean mIsNeedLandscape = false;

	public List<String> getmTitleList() {
		return mTitleList;
	}

	public void setmTitleList(List<String> mTitleList) {
		this.mTitleList = mTitleList;
	}

	public void setmTabClickListener(TabOnClickListener mTabClickListener) {
		this.mTabClickListener = mTabClickListener;
	}

	public PagerActionBar(Context context) {
		this(context, null);
	}

	public PagerActionBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PagerActionBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PagerActionBar);
		mTitleTextSize = a.getDimensionPixelSize(R.styleable.PagerActionBar_title_text_size, 15);
		mTitleTextColor = a.getColor(R.styleable.PagerActionBar_title_text_color, Color.WHITE);
		mSelectedItemTextColor = a.getColor(R.styleable.PagerActionBar_selected_title_text_color,
				Color.WHITE);
		mSelectorColor = a.getColor(R.styleable.PagerActionBar_selector_color, 0x00000000);
		// mSelector = new ColorDrawable(mSelectorColor);
		// mFooterSeparatorColor = a.getColor(
		// R.styleable.PagerActionBar_footer_separator_color, Color.WHITE);
		mFooterSeparatorLineWeight = a.getDimensionPixelSize(
				R.styleable.PagerActionBar_footer_separator_line_weight, 5);
		mHeaderPadding = a.getDimensionPixelSize(R.styleable.PagerActionBar_header_padding, 10);
		mFooterPadding = a.getDimensionPixelSize(R.styleable.PagerActionBar_footer_padding, 10);

		mAppsUpdateTabStr = context.getResources().getString(R.string.apps_update);

		mUpdatePaint.setColor(Color.WHITE);
		mUpdatePaint.setTextSize(getContext().getResources().getDimension(
				R.dimen.recomm_apps_management_update_count_text_size));

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

		if (mTitleList == null || mTitleList.size() == 0) {
			return;
		}
		if (mScrollerViewGroup == null || mScrollerViewGroup.getChildCount() == 0) {
			return;
		}

		drawTitleText(canvas);
		drawSeletor(canvas);
		drawBg(canvas);
	}

	/**
	 * 计算指定位置tab要绘制的区域
	 * 
	 * @param position
	 * @return
	 */
	protected RectF getTitleBound(int position) {
		RectF result = new RectF();
		result.setEmpty();
		// final int titleCount = (mViewPager != null && mViewPager.getAdapter()
		// != null) ? mViewPager.getAdapter().getCount() : 0;
		final int titleCount = (mScrollerViewGroup != null)
				? mScrollerViewGroup.getChildCount()
				: 0;
		if (position >= 0 && position < titleCount) {
			// String title = mPageTitleProvider.getPageTitle(position);
			String title = mTitleList.get(position);
			// Log.d(mTag, "position:"+position+" , title : "+title);
			mPaint.setTextSize(mTitleTextSize);
			final float textWidth = mPaint.measureText(title);
			final float textHeight = mPaint.descent() - mPaint.ascent();
			final int screenWidth = getResources().getDisplayMetrics().widthPixels;
			float left = 0;

			final float leftEdge = getPaddingLeft();
			final float rightEdge = getWidth() - getPaddingRight();
			final float leftOfCenterItem = (getWidth() - textWidth) / 2.0f;
			final float rightOfCenterItem = leftOfCenterItem + textWidth;
			// Log.d(mTag, "leftEdge : " + leftEdge + " rightEdge: " + rightEdge
			// + ", leftOfCenterItem :" + leftOfCenterItem
			// + ",rightOfCenterItem:" + rightOfCenterItem + ",textWidth:"
			// + textWidth);
			if (!mScrolling) {
				if (position == mCurrentPage) {
					left = leftOfCenterItem;
				} else if (position == mCurrentPage - 1) {
					left = leftEdge - PADDING;
				} else if (position == mCurrentPage + 1) {
					left = rightEdge - textWidth + PADDING;
				} else {
					left = -Integer.MAX_VALUE;
				}
			} else if (mPageScrollTo != -2) {
				float offsetPercent = 0;
				float scrollDistance = 0;
				if (mScrollToLeft) {
					// offsetPercent = (float) (0 - mCurrentOffsetPixels) /
					// (float) screenWidth;
					offsetPercent = -mCurrentOffsetPercent;
					if (position < mPageScrollTo) {
						if (position == mPageScrollTo - 1) {
							left = leftOfCenterItem;
							scrollDistance = Math.abs(leftOfCenterItem + PADDING - leftEdge);
						} else {
							left = leftEdge - PADDING;
							scrollDistance = screenWidth;
						}
					} else if (position == mPageScrollTo) {
						left = rightEdge - textWidth + PADDING;
						scrollDistance = Math.abs(left - leftOfCenterItem);

						// Log.d(mTag, "position : " + position
						// + " mCurrentPage: " + mCurrentPage
						// + " mPageScrollTo: " + mPageScrollTo
						// + " left: " + left + ", scrollDistance :"
						// + scrollDistance + ",offsetPercent:"
						// + offsetPercent);
					} else {
						left = rightEdge + screenWidth / 2.0f;
						scrollDistance = textWidth - PADDING + screenWidth / 2.0f;
					}
				} else {
					// offsetPercent = (float) (screenWidth -
					// mCurrentOffsetPixels) / (float) screenWidth;
					// offsetPercent = 1.0f - mCurrentOffsetPercent;
					offsetPercent = mCurrentOffsetPercent;
					if (position > mPageScrollTo) {
						if (position == mPageScrollTo + 1) {
							left = leftOfCenterItem;
							scrollDistance = Math.abs(rightEdge + PADDING - rightOfCenterItem);
						} else {
							left = rightEdge - textWidth + PADDING;
							scrollDistance = screenWidth;
						}
					} else if (position == mPageScrollTo) {
						left = leftEdge - PADDING;
						scrollDistance = Math.abs(leftEdge - leftOfCenterItem - PADDING);
					} else {
						left = leftEdge - textWidth - screenWidth / 2.0f;
						scrollDistance = textWidth - PADDING + screenWidth / 2.0f;
					}
				}
				left += scrollDistance * offsetPercent;
			} else {
				// Log.d(mTag, " ----------- this is runing -----------");
				left = leftEdge - PADDING;
			}

			final float right = left + textWidth;
			final float top = getPaddingTop() + mHeaderPadding;
			final float bottom = top + textHeight;

			result.set(left, top, right, bottom);
		}
		return result;
	}

	/**
	 * 画tab栏显示的文字
	 * 
	 * @param canvas
	 */
	private void drawTitleText(Canvas canvas) {
		// Log.d(mTag, "drawTitleText is running ");
		// final int titleCount = mViewPager.getAdapter().getCount();

		mPaint.setTextSize(mTitleTextSize);
		mPaint.setColor(mTitleTextColor);
		
		//获取桌面字体。修改字体显示
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		if (controler != null) {
			FontBean fontBean = controler.getUsedFontBean();
			if (fontBean != null) {
				mPaint.setTypeface(fontBean.mFontTypeface);
			}
		}

		// 画中间的tab
		drawText(canvas, mCurrentPage);
		// 画左边的tab
		RectF leftBound = drawText(canvas, mCurrentPage - 1);
		// 画右边的tab
		RectF rightBound = drawText(canvas, mCurrentPage + 1);

		// 判断在边缘时，是否要加载第4个tab
		if (mScrolling) {
			RectF fourBound = null;
			if (mScrollToLeft) {
				if (mCurrentPage == 0 || (leftBound != null && leftBound.left <= 0)) {
					final int titleCount = mScrollerViewGroup.getChildCount();
					// 加载第4屏
					final String right2Title = mCurrentPage + 2 >= titleCount ? null : mTitleList
							.get(mCurrentPage + 2);
					if (right2Title != null) {
						fourBound = getTitleBound(mCurrentPage + 2);
						final float baseLineY = fourBound.bottom - mPaint.descent();
						canvas.drawText(right2Title, fourBound.left, baseLineY, mPaint);
					}
				}

			} else {
				if (rightBound == null || (rightBound != null && rightBound.right >= getWidth())) {
					// 加载第4屏
					final String right2Title = mCurrentPage - 2 < 0 ? null : mTitleList
							.get(mCurrentPage - 2);
					if (right2Title != null) {
						fourBound = getTitleBound(mCurrentPage - 2);
						final float baseLineY = fourBound.bottom - mPaint.descent();
						canvas.drawText(right2Title, fourBound.left, baseLineY, mPaint);
					}
				}
			}
		}
		drawSelectedTitleText(canvas);
	}

	/**
	 * 绘制tab指定位置的tab内容
	 * 
	 * @param canvas
	 * @param position
	 * @return
	 */
	private RectF drawText(Canvas canvas, int position) {
		RectF rect = null;
		String right2Title = null;
		if (position >= 0 && position < mTitleList.size()) {
			rect = getTitleBound(position);
			right2Title = mTitleList.get(position);
			if (mAppsUpdateTabStr != null && mAppsUpdateTabStr.equals(right2Title)
					&& mUpdateCount > 0) {

				float contentWidth = rect.width();
				float contentHeight = rect.height();

				int mIconLeft = (int) (rect.left + contentWidth) - ICON_OFFSET_X;
				int mIconTop = (int) (contentHeight / 2) - ICON_OFFSET_Y + 8;

				NinePatchDrawable bgNine = (NinePatchDrawable) getContext().getResources()
						.getDrawable(
								R.drawable.stat_notify);
				int size = (int) getContext().getResources().getDimension(
						R.dimen.recomm_message_notify_size);
				bgNine.setBounds(new Rect(0, 0, size, size));

				Bitmap bgIcon = Bitmap.createBitmap(bgNine.getBounds().width(), bgNine.getBounds()
						.height(), Config.ARGB_8888);
				Canvas iconCanvas = new Canvas(bgIcon);
				bgNine.draw(iconCanvas);
				String updateCountStr = String.valueOf(mUpdateCount);
				float w = mUpdatePaint.measureText(updateCountStr);
				iconCanvas.drawText(updateCountStr, (bgIcon.getWidth() - w) / 2,
						bgIcon.getHeight() * 2 / 3, mUpdatePaint);
				canvas.drawBitmap(bgIcon, mIconLeft, mIconTop, mUpdatePaint);
			}
			final float baseLineY = rect.bottom - mPaint.descent();
			canvas.drawText(right2Title, rect.left, baseLineY, mPaint);

		}
		return rect;
	}

	private void drawSelectedTitleText(Canvas canvas) {
		mPaint.setTextSize(mTitleTextSize);
		mPaint.setColor(mSelectedItemTextColor);
		// final int titleCount = mViewPager.getAdapter().getCount();
		final int titleCount = mScrollerViewGroup.getChildCount();
		// final String curPageTitle = mCurrentPage >=0 && mCurrentPage <
		// titleCount ? mPageTitleProvider.getPageTitle(mCurrentPage) : null;
		final String curPageTitle = mCurrentPage >= 0 && mCurrentPage < titleCount ? mTitleList
				.get(mCurrentPage) : null;
		float offsetPercent = mScrolling ? (mScrollToLeft
				? mCurrentOffsetPercent
				: 1.0f - mCurrentOffsetPercent) : 0;
		// Log.d("GoNamecard", "mScrollToLeft = " + mScrollToLeft);
		// Log.d("GoNamecard", "offsetPercent = " + offsetPercent);
		if (curPageTitle != null) {
			int alpha = Math.max(
					(int) ((mSelectedItemTextColor >>> 24) * (1.0f - offsetPercent * 2.0f)), 0);
			mPaint.setAlpha(alpha);
			RectF curPageBound = getTitleBound(mCurrentPage);

			final float baseLineY = curPageBound.bottom - mPaint.descent();
			canvas.drawText(curPageTitle, curPageBound.left, baseLineY, mPaint);
		}

		if (mScrolling && Math.abs(offsetPercent) > 0.5f) {
			// final String pageScrollToTitle = mPageScrollTo >=0 &&
			// mPageScrollTo < titleCount ?
			// mPageTitleProvider.getPageTitle(mPageScrollTo) : null;
			final String pageScrollToTitle = mPageScrollTo >= 0 && mPageScrollTo < titleCount
					? mTitleList.get(mPageScrollTo)
					: null;
			if (pageScrollToTitle != null) {
				RectF pageScrollToBound = getTitleBound(mPageScrollTo);
				int alpha = Math.max(
						(int) ((mSelectedItemTextColor >>> 24) * ((offsetPercent - 0.5f) * 2)), 0);
				// Log.d("GoNamecard", "pageScrollTo = " + mPageScrollTo);
				// Log.d("GoNamecard", "pageScrollTo Alpha = " + alpha);
				mPaint.setAlpha(alpha);
				final float baseLineY = pageScrollToBound.bottom - mPaint.descent();
				canvas.drawText(pageScrollToTitle, pageScrollToBound.left, baseLineY, mPaint);
			}
		}
	}

	// private void drawFooterSeparator(Canvas canvas) {
	// // mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
	// mPaint.setColor(mFooterSeparatorColor);
	// mPaint.setStrokeWidth(mFooterSeparatorLineWeight);
	// final int width = getWidth();
	// final int height = getHeight();
	// canvas.drawLine(0, height - mFooterSeparatorLineWeight / 2.0f, width,
	// height - mFooterSeparatorLineWeight / 2.0f, mPaint);
	// }

	private void drawSeletor(Canvas canvas) {
		if (shouldShowSelector() && mSelectorRect != null && !mSelectorRect.isEmpty()) {
			Style oldStyle = mPaint.getStyle();
			mPaint.setStyle(Style.FILL);
			mPaint.setColor(mSelectorColor);
			canvas.drawRect(mSelectorRect, mPaint);
			mPaint.setStyle(oldStyle);
		}
	}

	// /**
	// * 画tab栏遮罩层的图片
	// *
	// * @param canvas
	// */
	// private void drawBg(Canvas canvas) {
	// if (bgBit == null || isPortait != GoLauncher.isPortait()) {
	// createBitmap();
	// isPortait = GoLauncher.isPortait();
	// }
	// if (bgBit != null) {
	// canvas.drawBitmap(bgBit, 0, 0, null);
	// }
	// }

	/**
	 * 画tab栏遮罩层的图片
	 * 
	 * @param canvas
	 */
	private void drawBg(Canvas canvas) {

		if (mBgBit == null) {
			createBitmap();
		} else {
			if (mIsNeedLandscape) {
				boolean isPortait = isPortait(getContext());
				if (mIsPortait != isPortait) {
					createBitmap();
					mIsPortait = isPortait;
				}
			}
		}

		if (mBgBit != null) {
			canvas.drawBitmap(mBgBit, 0, 0, null);
		}
	}

	/**
	 * 生成tab栏遮罩层的bitmap
	 */
	private void createBitmap() {
		NinePatchDrawable bgNine = (NinePatchDrawable) getContext().getResources().getDrawable(
				R.drawable.recomm_apps_management_tab_alpha_bg);
		if (bgNine != null) {
			bgNine.setBounds(new Rect(0, 0, getWidth(), getHeight()));
			int bitWidth = bgNine.getBounds().width();
			int bitHeight = bgNine.getBounds().height();
			if (bitWidth > 0 && bitHeight > 0) {
				mBgBit = Bitmap.createBitmap(bitWidth, bitHeight, Config.ARGB_8888);
				Canvas iconCanvas = new Canvas(mBgBit);
				bgNine.draw(iconCanvas);
			}

		}
	}

	/**
	 * 获取tab区域内被点击的子tab
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private int getTitleIndexPressed(float x, float y) {
		// if (mViewPager != null && mViewPager.getAdapter() != null) {
		// final int count = mViewPager.getAdapter().getCount();
		if (mScrollerViewGroup != null) {
			final int count = mScrollerViewGroup.getChildCount();
			for (int i = 0; i < count; i++) {
				final RectF bound = getTitleBound(i);
				bound.top = 0;
				bound.bottom = getHeight();
				if (bound != null && bound.contains(x, y)) {
					return i;
				}
			}
		}
		return -2;
	}

	protected void positionSelector(int titleIndex) {
		RectF bound = getTitleBound(titleIndex);
		if (bound != null && !bound.isEmpty()) {
			mSelectorRect.set((int) bound.left, 0, (int) bound.right, getHeight());
			refreshDrawableState();
		}
	}

	protected abstract void drawFooterIndicator(Canvas canvas);

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
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
			result = mPaint.descent() - mPaint.ascent() + mHeaderPadding + mFooterPadding;
		}
		return (int) result;
	}

	/**
	 * 绑定tab栏下面的内容页面
	 */
	public void attachToViewPager(ScrollerViewGroup scrollerView) {
		mScrollerViewGroup = scrollerView;
		// if (scrollerViewGroup != null) {
		// // 这里没有设置监听，怎么传递事件呢。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。
		// // mViewPager.setOnPageChangeListener(this);
		//
		// // final PagerAdapter adapter = mViewPager.getAdapter();
		// // if (!(adapter instanceof PageTitleProvider)) {
		// // throw new
		// //
		// IllegalArgumentException("ViewPager adapter must implement PageTitleProvider to provide title of each page");
		// // }
		// // mPageTitleProvider = (PageTitleProvider) adapter;
		// }
		invalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// if (mViewPager == null || mViewPager.getAdapter().getCount() == 0) {
		// return false;
		// }
		if (mScrollerViewGroup == null || mScrollerViewGroup.getChildCount() == 0) {
			return false;
		}
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();

		// 先把事件传递过去
		mScrollerViewGroup.onTouchEvent(event);

		switch (action) {
			case MotionEvent.ACTION_DOWN :
				mIsPressed = true;
				final int seletectedItemIndex = getTitleIndexPressed(x, y);
				if (seletectedItemIndex != -2) {
					// mDrawSeletorInPressedState = true;
					positionSelector(seletectedItemIndex);
				}
				mLastMotionX = event.getX();
				break;
			case MotionEvent.ACTION_MOVE :
				final float deltaX = x - mLastMotionX;
				if (!mIsDragging) {
					// final int touchSlop =
					// ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(getContext()));
					final int touchSlop = 5;
					if (Math.abs(deltaX) > touchSlop) {
						mIsDragging = true;
					}
				}
				if (mIsDragging) {
					// 这个地方 要重点看下啊
					// .........................................................
					// if (!mViewPager.isFakeDragging()) {
					// mViewPager.beginFakeDrag();
					// }
					// mViewPager.fakeDragBy(deltaX);
					// fakeDragBy(deltaX);
					onPageScrollStateChanged(SCROLL_STATE_DRAGGING);
					mLastMotionX = x;
					// mDrawSeletorInPressedState = false;
				}
				break;
			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :
				if (mIsPressed) {
					mIsPressed = false;
					if (!mIsDragging) {
						int selectedItemIndex = getTitleIndexPressed(x, y);
						if (selectedItemIndex != -2) {
							// mViewPager.setCurrentItem(selectedItemIndex,
							// true);
							mScrollerViewGroup.gotoViewByIndex(selectedItemIndex);
							if (mTabClickListener != null) {
								mTabClickListener.onClick(selectedItemIndex);
							}
						}
						onPageScrollStateChanged(SCROLL_STATE_IDLE);
						// mDrawSeletorInPressedState = false;
						mIsDragging = false;
					}

					// if (mViewPager.isFakeDragging()) {
					// mViewPager.endFakeDrag();
					// }

				}
				// onPageScrollStateChanged(SCROLL_STATE_IDLE);
				// mDrawSeletorInPressedState = false;
				// mIsDragging = false;
				break;
			default :
				break;
		}
		invalidate();
		// refreshDrawableState();
		return true;
	}

	// @Override
	// public void onPageScrolled(int position, float positionOffset, int
	// positionOffsetPixels) {
	// if (!mScrolling && positionOffsetPixels != 0) {
	// mScrolling = true;
	// final int halfScreenWidth =
	// getResources().getDisplayMetrics().widthPixels / 2;
	// mScrollToLeft = Math.abs(positionOffsetPixels) < halfScreenWidth ? true :
	// false;
	// mPageScrollTo = mScrollToLeft ? position + 1 : position;
	//
	// if (mScrolling && positionOffsetPixels == 0) {
	// mPageScrollTo = -1;
	// mScrolling = false;
	// }
	// }
	// if (mScrollState != SCROLL_STATE_IDLE) {
	// if (positionOffsetPixels != 0) {
	// if (mScrollState == SCROLL_STATE_DRAGGING) {
	// mScrollToLeft = (position == mCurrentPage) ? true : false;
	// mPageScrollTo = mScrollToLeft ? position + 1 : position;
	// } else if (!mScrolling && mScrollState == SCROLL_STATE_SETTLING) {
	// mPageScrollTo = mCurrentPage;
	// mScrollToLeft = (position == mPageScrollTo) ? false : true;
	// }
	// mScrolling = true;
	// } else {
	// if (mScrolling) {
	// mPageScrollTo = -1;
	// mScrolling = false;
	// }
	// }
	// }
	//
	// Log.d(mTag, "mScrolling  = " + mScrolling + ", mScrollState = " +
	// mScrollState + ", mScrollToLeft = " +
	// mScrollToLeft+", mPageScrollTo = "+mPageScrollTo);
	// mCurrentOffsetPercent = positionOffset;
	// mCurrentOffsetPixels = positionOffsetPixels;
	//
	// invalidate();
	// }

	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

		if (!mScrolling && positionOffsetPixels != 0) {
			mScrolling = true;
			mScrollState = SCROLL_STATE_DRAGGING;
			// inEnd = false;
			// final int halfScreenWidth =
			// getResources().getDisplayMetrics().widthPixels / 2;
			// mScrollToLeft = Math.abs(positionOffsetPixels) < halfScreenWidth
			// ? true
			// : false;
			// mPageScrollTo = mScrollToLeft ? position + 1 : position;

			if (mScrolling && positionOffsetPixels == 0) {
				mPageScrollTo = -2;
				mScrolling = false;
				mIsDragging = false;
			}
		}
		if (mScrollState != SCROLL_STATE_IDLE) {
			if (positionOffsetPixels != 0) {
				if (mScrollState == SCROLL_STATE_DRAGGING) {
					mScrollToLeft = (position == mCurrentPage) ? true : false;
					mPageScrollTo = mScrollToLeft ? position + 1 : position;
				} else if (!mScrolling && mScrollState == SCROLL_STATE_SETTLING) {
					mPageScrollTo = mCurrentPage;
					mScrollToLeft = (position == mPageScrollTo) ? false : true;
				}
				mScrolling = true;
			} else {
				if (mScrolling) {
					mPageScrollTo = -2;
					mScrolling = false;
					mIsDragging = false;
					return;
				}
			}
		}

		// Log.d(mTag, "mScrolling  = " + mScrolling + ", mScrollState = "
		// + mScrollState + ", mScrollToLeft = " + mScrollToLeft
		// + ", mPageScrollTo = " + mPageScrollTo);
		mCurrentOffsetPercent = positionOffset;
		mCurrentOffsetPixels = positionOffsetPixels;

		invalidate();
	}

	public void setStatus() {
		if (mIsDragging) {
			mIsDragging = false;
		}
	}

	// boolean inEnd = false;

	// @Override
	public void onPageSelected(int position) {
		// Log.d("GoNamecard", "onPageSelected position = " + position);
		mCurrentPage = position;
		// inEnd = true;
		invalidate();

	}

	// @Override
	public void onPageScrollStateChanged(int state) {
		// Log.d("GoNamecard", "onPageScrollStateChanged state = " + state);
		mScrollState = state;
		if (mScrolling && mScrollState == SCROLL_STATE_IDLE) {
			mPageScrollTo = -2;
			mScrolling = false;
			mIsDragging = false;
			// invalidate();
		}
	}

	public int getmCurrentPage() {
		return mCurrentPage;
	}

	public void setmCurrentPage(int mCurrentPage) {
		this.mCurrentPage = mCurrentPage;
	}

	/**
	 * tab点击的监听类
	 * 
	 * @author zhoujun
	 * 
	 */
	public interface TabOnClickListener {
		public void onClick(int position);
	}

	public void setmUpdateCount(int mUpdateCount) {
		this.mUpdateCount = mUpdateCount;
		invalidate();
	}

	private boolean shouldShowSelector() {
		return mDrawSeletorInPressedState;
	}

	public void setmIsNeedLandscape(boolean mIsNeedLandscape) {
		this.mIsNeedLandscape = mIsNeedLandscape;
	}

	private boolean isPortait(Context context) {
		if (getScreenHeight(context) > getScreenWidth(context)) {
			return true;
		}
		return false;
	}

	/**
	 * 屏幕高度(px)
	 * 
	 * @return
	 */
	private int getScreenHeight(Context context) {
		if (Machine.isTablet(context)) {
			return DrawUtils.getTabletScreenHeight(context);
		}
		return DrawUtils.sHeightPixels;
	}

	/**
	 * 屏幕宽度(px)
	 * 
	 * @return
	 */
	private int getScreenWidth(Context context) {
		if (Machine.isTablet(context)) {
			return DrawUtils.getTabletScreenWidth(context);
		}
		return DrawUtils.sWidthPixels;
	}

}
