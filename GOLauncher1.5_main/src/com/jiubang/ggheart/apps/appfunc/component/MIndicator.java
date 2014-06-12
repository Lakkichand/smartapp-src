package com.jiubang.ggheart.apps.appfunc.component;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.gau.go.launcherex.R;
import com.jiubang.core.mars.MImage;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.Indicator;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.IndicatorListner;
import com.jiubang.ggheart.apps.desks.diy.frames.screen.ScreenIndicator;
import com.jiubang.ggheart.data.theme.ImageExplorer;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * @author liushichuan
 * 
 */
public class MIndicator extends XComponent implements IMsgHandler {
	public static final byte MODE_V = 1; // 垂直模式
	public static final byte MODE_H = 2; // 水平模式

	public static final byte MAX_COUNT_NUM = 10;
	public static final byte MIN_SCROLL_LENGTH = 15;

	// 点之间的最大距离
	public static final byte DOT_MAX_DIS = 12;

	// 起始点的坐标
	private int mDotStartX;
	// 点之间的间隔
	private int mDotDis;
	// 末尾点的坐标
	private int mDotEndX;

	private byte mMode; // 显示模式

	private Drawable mScrollDraw;

	// 被选中时点亮的图片
	private MImage mChoice;
	private MImage mDark;

	// 总共的个数
	private int mTotalCount;
	// 当前的光标位置
	private int mCurCount;
	// 一屏可显示的数量
	private int mScreenNum;

	// 当前的光标实际X位置
	private int mScrollX;
	private int mScrollY;

	// 光标的高度
	private int mScrollHeight;

	// 单元格的高度
	private int mCellHeight;

	Context mContext;
	private AppFuncThemeController mThemeController;

	// 监听者
	private IndicatorListner mIndicatorListner;
	// 滑动的位置百分比
	private float mMovePercent;
	// 用于保存down时的位置信息
	protected Point mDownPoint = new Point(-1, -1);
	// 本组事件是否响应move
	protected int mMoveDirection = Indicator.MOVE_DIRECTION_NONE;

	/**
	 * @param tickCount
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public MIndicator(int tickCount, int x, int y, int width, int height, Context context) {
		super(tickCount, x, y, width, height);
		this.mContext = context;
		mThemeController = AppFuncFrame.getThemeController();
		// MScroller并非初始化功能表时就被构造，因此需要在被构造时加载主题资源
		loadResource();
		// 注册主题改变事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.THEME_CHANGE,
				this);
		// 注册加载主题资源事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.LOADTHEMERES,
				this);
		// 注册加载指示器主题资源事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(
				AppFuncConstants.LOADINDICATORTHEMERES, this);
	}

	/**
	 * 设置滚动条需要用的参数
	 * 
	 * @param displayMode
	 *            显示的模式，可以为水平和垂直两种模式
	 * @param curIndex
	 *            当前的光标位置
	 * @param totalCount
	 *            总共的数量
	 * @param screenNum
	 *            每屏显示的数量
	 */
	public void setParameter(byte displayMode, int totalCount, int curIndex, int screenNum) {
		setMode(displayMode);
		setCurIndex(curIndex, totalCount, screenNum);
	}

	/**
	 * 设置滚动条需要用的参数
	 * 
	 * @param displayMode
	 *            显示的模式，可以为水平和垂直两种模式
	 * @param curIndex
	 *            当前的光标位置
	 * @param totalCount
	 *            总共的数量
	 * @param screenNum
	 *            每屏显示的数量
	 */
	public void setParameter(byte displayMode, int totalCount, int curIndex, int screenNum,
			int cellHeight) {
		setMode(displayMode);
		setCurIndex(curIndex, totalCount, screenNum);

		mCellHeight = cellHeight;
	}

	/**
	 * @param displayMode
	 *            显示的模式，可以为水平和垂直两种模式
	 */
	public void setMode(byte displayMode) {
		this.mMode = displayMode;
		if (displayMode == MODE_H) { // 如果是水平的方式
			mScrollDraw = AppFuncUtils.getInstance(mContext).getDrawable(R.drawable.scrollh);
		} else if (displayMode == MODE_V) {
			mScrollDraw = AppFuncUtils.getInstance(mContext).getDrawable(R.drawable.scrollv);
		}
	}

	/**
	 * @param curIndex
	 *            当前的光标位置
	 * @param totalCount
	 *            总共的数量
	 * @param screenNum
	 *            每屏显示的数量
	 */
	public void setCurIndex(int curIndex, int totalCount, int screenNum) {
		this.mTotalCount = totalCount;
		if (totalCount < 1) {
			mTotalCount = 1;
		}
		setScreenNum(screenNum);
		setCurIndex(curIndex);
		mDotEndX = mDotStartX + (mDotDis * mTotalCount);
	}

	/**
	 * @param num
	 *            每屏可以显示的数量
	 */
	public void setScreenNum(int num) {
		this.mScreenNum = num;
		int length = mWidth;
		if (mMode == MODE_V) {
			length = mHeight;
		} else if (mMode == MODE_H && !isNeedChange()) { // 如果是水平状态，且不超过10屏，则不做处理
			mDotDis = DOT_MAX_DIS;
			if (mChoice != null) {
				mDotDis += mChoice.getWidth();
			}
			mDotStartX = ((mTotalCount - 1) * mDotDis) >> 1;
			mDotStartX = (mWidth >> 1) - mDotStartX;
			return;
		}

		mScrollHeight = length * num / mTotalCount;
		if (mScrollHeight < MIN_SCROLL_LENGTH) {
			mScrollHeight = MIN_SCROLL_LENGTH;
		}
	}

	/**
	 * 当前的光标位置，竖屏时以像素做单位
	 * 
	 * @param curPosition
	 */
	public void setCurPosition(int curPosition) {
		mScrollX = 0;

		if (mMode == MODE_V) {
			final int exceedScroll = curPosition - (mTotalCount - mScreenNum) * mCellHeight;
			if (exceedScroll > 0) {
				mScrollY = mHeight - mScrollHeight + exceedScroll * mScrollHeight * 2 / getHeight();
			} else if (curPosition < 0) {
				mScrollY = mScrollHeight * curPosition * 2 / getHeight();
			} else {
				mScrollY = (mHeight - mScrollHeight) * curPosition
						/ ((mTotalCount - mScreenNum) * mCellHeight);
			}
			if (mScrollDraw != null) {
				mScrollDraw.setBounds(mScrollX, mScrollY, mWidth - mScrollX, mScrollY
						+ mScrollHeight);
			}
		} else {
			mScrollX = curPosition;
			if (null != mScrollDraw && mTotalCount != 0) {
				mScrollDraw.setBounds(mScrollX, 0, mScrollX + getWidth() / mTotalCount, mScrollY
						+ getHeight());
			}
		}
	}

	/**
	 * @param curIndex
	 *            当前的光标位置，竖屏时以行作单位
	 */
	public void setCurIndex(int curIndex) {
		setVisible(true);
		this.mCurCount = curIndex;
		if (mMode == MODE_V) {
			mScrollX = 0;
			if (curIndex > (mTotalCount - mScreenNum)) {
				mScrollY = mHeight - mScrollHeight;
			} else if (curIndex < 0) {
				mScrollY = 0;
			} else {
				mScrollY = (mHeight - mScrollHeight) * curIndex / (mTotalCount - mScreenNum);
			}
			if (mScrollDraw != null) {
				mScrollDraw.setBounds(mScrollX, mScrollY, mWidth - mScrollX, mScrollY
						+ mScrollHeight);
			}
		} else if (mMode == MODE_H) {
			mScrollY = 1;
			if (isNeedChange()) {
				if (curIndex > (mTotalCount - mScreenNum)) {
					mScrollX = mWidth - mScrollHeight;
				} else if (curIndex < 0) {
					mScrollX = 0;
				} else {
					mScrollX = (mWidth - mScrollHeight) * curIndex / (mTotalCount - mScreenNum);
				}
				if (mScrollDraw != null) {
					mScrollDraw.setBounds(mScrollX, mScrollY, mScrollX + mScrollHeight, mHeight
							- mScrollY);
				}
			} else {
				if (curIndex > mTotalCount) {
					mCurCount = mTotalCount - 1;
				}
			}
		}
	}

	/**
	 * @return 是否需要用条形的滚动条显示模式
	 */
	public boolean isNeedChange() {
		return mTotalCount > MAX_COUNT_NUM;
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		if (mMode == MODE_H) {
			if (isNeedChange() && mScrollDraw != null) {
				mScrollDraw.draw(canvas);
			} else {
				if (mChoice == null || mDark == null) {
					return;
				}
				int x = mDotStartX;

				int startIndex = 0;

				// 判断是否需要画search
				// 画search图标

				for (int i = startIndex; i < mTotalCount; i++) {
					if (i == mCurCount) {
						mChoice.draw(canvas, x - mChoice.getScaleWidth() * 0.5f, 0);
					} else {
						mDark.draw(canvas, x - mDark.getScaleWidth() * 0.5f, 0);
					}
					if (ScreenIndicator.sShowmode.equals(ScreenIndicator.SHOWMODE_NUMERIC)) {
						drawNum(i, x, mDark.getHeight() / 2, canvas);
					}
					x += mDotDis;
				}
			}
		} else if (mMode == MODE_V) {
			if (mScrollDraw != null) {
				mScrollDraw.draw(canvas);
			}
		}

	}

	@Override
	protected boolean animate() {
		return false;
	}

	@Override
	public void notify(int key, Object obj) {
		switch (key) {
			case AppFuncConstants.THEME_CHANGE : {
				mDark = null;
				mChoice = null;
			}
				break;
			case AppFuncConstants.LOADTHEMERES : {
				loadResource();
			}
				break;
			case AppFuncConstants.LOADINDICATORTHEMERES : {
				loadResource();
			}
		}

	}

	private void loadResource() {
		String packageName = GOLauncherApp.getSettingControler().getFunAppSetting()
				.getIndicatorSetting();
		if (null == packageName
				|| GOLauncherApp.getThemeManager().getCurThemePackage().equals(packageName)) {
			if (mChoice == null) {
				Drawable drawable = mThemeController
						.getDrawable(mThemeController.getThemeBean().mIndicatorBean.indicatorCurrentHor, false);
				if (drawable != null && drawable instanceof BitmapDrawable) {
					mChoice = new MImage((BitmapDrawable) drawable);
				}
			} else {
				Drawable drawable = mThemeController
						.getDrawable(mThemeController.getThemeBean().mIndicatorBean.indicatorCurrentHor, false);
				if (drawable != null && drawable instanceof BitmapDrawable) {
					mChoice.setDrawable((BitmapDrawable) drawable);
				}
			}
			if (mDark == null) {
				Drawable drawable = mThemeController
						.getDrawable(mThemeController.getThemeBean().mIndicatorBean.indicatorHor, false);
				if (drawable != null && drawable instanceof BitmapDrawable) {
					mDark = new MImage((BitmapDrawable) drawable);
				}
			} else {
				Drawable drawable = mThemeController
						.getDrawable(mThemeController.getThemeBean().mIndicatorBean.indicatorHor, false);
				if (drawable != null && drawable instanceof BitmapDrawable) {
					mDark.setDrawable((BitmapDrawable) drawable);
				}
			}
		} else if (packageName.equals(ScreenIndicator.SHOWMODE_NORMAL)
				|| packageName.equals(ScreenIndicator.SHOWMODE_NUMERIC)) {
			if (mChoice == null) {
				Drawable drawable = null;
				if (packageName.equals(ScreenIndicator.SHOWMODE_NORMAL)) {
					drawable = mThemeController
							.getDrawable(mThemeController.getThemeBean().mIndicatorBean.indicatorCurrentHor, false);
				} else if (packageName.equals(ScreenIndicator.SHOWMODE_NUMERIC)) {
					drawable = getDrawable(R.drawable.focus_indicator_numeric);
				}

				if (drawable != null && drawable instanceof BitmapDrawable) {
					mChoice = new MImage((BitmapDrawable) drawable);
				}
			} else {
				Drawable drawable = null;
				if (packageName.equals(ScreenIndicator.SHOWMODE_NORMAL)) {
					drawable = mThemeController
							.getDrawable(mThemeController.getThemeBean().mIndicatorBean.indicatorCurrentHor, false);
				} else if (packageName.equals(ScreenIndicator.SHOWMODE_NUMERIC)) {
					drawable = getDrawable(R.drawable.focus_indicator_numeric);
				}
				if (drawable != null && drawable instanceof BitmapDrawable) {
					mChoice.setDrawable((BitmapDrawable) drawable);
				}
			}
			if (mDark == null) {
				Drawable drawable = null;
				if (packageName.equals(ScreenIndicator.SHOWMODE_NORMAL)) {
					drawable = mThemeController
							.getDrawable(mThemeController.getThemeBean().mIndicatorBean.indicatorHor, false);
				} else if (packageName.equals(ScreenIndicator.SHOWMODE_NUMERIC)) {
					drawable = getDrawable(R.drawable.unfocus_indicator_numeric);
				}

				if (drawable != null && drawable instanceof BitmapDrawable) {
					mDark = new MImage((BitmapDrawable) drawable);
				}
			} else {
				Drawable drawable = null;
				if (packageName.equals(ScreenIndicator.SHOWMODE_NORMAL)) {
					drawable = mThemeController
							.getDrawable(mThemeController.getThemeBean().mIndicatorBean.indicatorHor, false);
				} else if (packageName.equals(ScreenIndicator.SHOWMODE_NUMERIC)) {
					drawable = getDrawable(R.drawable.unfocus_indicator_numeric);
				}

				if (drawable != null && drawable instanceof BitmapDrawable) {
					mDark.setDrawable((BitmapDrawable) drawable);
				}
			}
		} else {
			if (mChoice == null) {
				Drawable drawable = mThemeController.getDrawable(
						mThemeController.getThemeBean().mIndicatorBean.indicatorCurrentHor,
						packageName, false);
				if (drawable != null && drawable instanceof BitmapDrawable) {
					mChoice = new MImage((BitmapDrawable) drawable);
				}
			} else {
				Drawable drawable = mThemeController.getDrawable(
						mThemeController.getThemeBean().mIndicatorBean.indicatorCurrentHor,
						packageName, false);
				if (drawable != null && drawable instanceof BitmapDrawable) {
					mChoice.setDrawable((BitmapDrawable) drawable);
				}
			}
			if (mDark == null) {
				Drawable drawable = mThemeController.getDrawable(
						mThemeController.getThemeBean().mIndicatorBean.indicatorHor, packageName, false);

				if (drawable != null && drawable instanceof BitmapDrawable) {
					mDark = new MImage((BitmapDrawable) drawable);
				}
			} else {
				Drawable drawable = mThemeController.getDrawable(
						mThemeController.getThemeBean().mIndicatorBean.indicatorHor, packageName, false);

				if (drawable != null && drawable instanceof BitmapDrawable) {
					mDark.setDrawable((BitmapDrawable) drawable);
				}
			}
		}
	}

	private Drawable getDrawable(int id) {
		Drawable drawable = null;
		if (ImageExplorer.getInstance(mContext) != null) {
			drawable = ImageExplorer.getInstance(mContext).getDefaultDrawable(id);
		}
		if (null == drawable) {
			drawable = mContext.getResources().getDrawable(id);
		}
		return drawable;
	}

	public void drawNum(int index, float mText_X, float mText_Y, Canvas canvas) {
		int value = (index == 0) ? 2 : (index + 1);
		String string = new String(Integer.toString(value));
		Rect bounds = new Rect();

		int id = R.dimen.indicator_numeric_textsize;
		Resources resources = mContext.getResources();
		Paint mPaint = new Paint();
		mPaint.setTextSize(resources.getDimensionPixelSize(id));
		mPaint.setColor(0xb3000000);
		mPaint.setAntiAlias(true);
		mPaint.getTextBounds(string, 0, string.length(), bounds);
		mText_X = mText_X - (bounds.right - bounds.left) / 2;
		mText_Y = mText_Y + (bounds.bottom - bounds.top) / 2;

		canvas.drawText(Integer.toString(index + 1), mText_X, mText_Y, mPaint);
	}

	public void setIndicatorListner(IndicatorListner listner) {
		mIndicatorListner = listner;
	}

	public byte getMode() {
		return mMode;
	}

	/**
	 * 显示内容的X起点
	 */
	public int getContentStartX() {
		if (mMode == MODE_H) {
			if (isNeedChange()) {
				return 0;
			} else {
				return mDotStartX - mDotDis / 2;
			}
		} else {
			return getWidth() - mWidth;
		}
	}

	/**
	 * 显示内容的X终点
	 * 
	 * @return
	 */
	public int getContentEndX() {
		if (mMode == MODE_H) {
			if (isNeedChange()) {
				return getWidth();
			} else {
				return mDotEndX - mDotDis / 2;
			}
		} else {
			return getWidth();
		}
	}

	@Override
	public boolean onTouch(MotionEvent event) {
		if (null == mIndicatorListner || mMode != MODE_H) {
			// 竖向滑动模式不处理
			return false;
		}

		int action = event.getAction();
		float x = event.getRawX();
		float y = event.getRawY();
		int startX = getAbsX() + getContentStartX();
		int endX = getAbsX() + getContentEndX();
		switch (action) {
			case MotionEvent.ACTION_DOWN : {
				mDownPoint.x = (int) x;
				mDownPoint.y = (int) y;
				mMoveDirection = Indicator.MOVE_DIRECTION_NONE;
				break;
			}

			case MotionEvent.ACTION_MOVE :
				if (mMoveDirection == Indicator.MOVE_DIRECTION_NONE) {
					if (Indicator.CLICK_LIMEN <= (x - mDownPoint.x)) {
						// 当move超过阈值Indicator.CLICK_LIMEN时才响应move
						mMoveDirection = Indicator.MOVE_DIRECTION_RIGHT;
					} else if (Indicator.CLICK_LIMEN <= (mDownPoint.x - x)) {
						mMoveDirection = Indicator.MOVE_DIRECTION_LEFT;
					} else {
						break;
					}
				}

				if (!isNeedChange()) {
					// 点状
					if (startX <= x && x <= endX) {
						mMovePercent = ((x - startX) * 100) / (endX - startX);
						mIndicatorListner.sliding(mMovePercent);
					}
				} else {
					// 条状
					if (0 <= x && x <= getWidth()) {
						float movePercent = (x * 100) / getWidth();
						mIndicatorListner.sliding(movePercent);
					}
				}
				break;

			case MotionEvent.ACTION_CANCEL :
			case MotionEvent.ACTION_UP : {
				if (!isNeedChange()) {
					// 点状
					if (x <= startX) {
						mIndicatorListner.clickIndicatorItem(0);
					} else if (startX < x && x < endX) {
						int index = (int) ((((x - startX)) / ((endX - startX))) * mTotalCount);
						mIndicatorListner.clickIndicatorItem(index);
					} else if (endX <= x) {
						mIndicatorListner.clickIndicatorItem(mTotalCount - 1);
					}
				} else {
					// 条状
					if (getAbsX() <= x && x <= getAbsX() + getWidth()) {
						int index = (int) (((x - getAbsX()) / getWidth()) * mTotalCount);
						mIndicatorListner.clickIndicatorItem(index);
					}
				}

				break;
			}

			default :
				break;
		}

		return true;
	}

	public void setScrollDraw(Drawable draw) {
		mScrollDraw = draw;
	}
}