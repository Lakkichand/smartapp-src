package com.jiubang.ggheart.apps.appfunc.component;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.gau.go.launcherex.R;
import com.go.util.graphics.ImageUtil;
import com.jiubang.core.mars.XALinear;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XMotion;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.AppFuncScreenItemInfo;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;

/**
 * 屏幕预览缩略图图标
 * 
 * @author huangshaotao
 * 
 */
public class AppFuncScreenIcon extends XComponent {

	/**
	 * 已经放大完成
	 */
	public static final int SCALEED = 2;
	/**
	 * 放大或缩小动画中
	 */
	public static final int SCALEING = 1;
	/**
	 * 正常状态
	 */
	public static final int SCALENORMAl = 0;

	/**
	 * 图标在x轴和y轴的放大最大增量
	 */
	public static final int MAXSCALE_X = 32;
	public static final int MAXSCALE_Y = 42;

	private AppFuncScreenItemInfo mInfo = null;
	private Drawable mBorad;
	private Drawable mBoradLight;
	private Drawable mBoradFull;

	private volatile int mCurrentState = SCALENORMAl;
	/**
	 * 当前缩放因子
	 */
	private volatile float mCurrentScaleFactory = 0;
	/**
	 * 图标默认宽度
	 */
	private int mDefaultWidth = 0;
	/**
	 * 图标默认高度
	 */
	private int mDefaultHeight = 0;
	/**
	 * 图标默认的x坐标
	 */
	private int mDefaultX = 0;
	/**
	 * 图标默认的Y坐标
	 */
	private int mDefaultY = 0;

	/**
	 * 图标默认padding
	 */
	private float mDefaultPadding = 0;

	/**
	 * 图标当前水平padding
	 */
	public float mCurrentPaddingH;

	/**
	 * 图标当前垂直padding
	 */
	public float mCurrentPaddingV;

	private AppFuncUtils mUtils = null;

	private Activity mActivity = null;
	/**
	 * xy轴放大最大值，这两个变量主要用来适应多分辨率
	 */
	private int mMaxScaleX = MAXSCALE_X;
	private int mMaxScaleY = MAXSCALE_Y;
	/**
	 * 是否重绘
	 */
	private boolean mIsRepaint = false;
	/**
	 * 缩放动画类
	 */
	private XMotion mScaleMotion = null;
	// private Paint mPaint = null;

	private boolean mLockScreen = false;

	public AppFuncScreenIcon(Activity activity, int tickCount, int x, int y, int width, int height) {
		super(tickCount, x, y, width, height);
		mActivity = activity;
		// mPaint = new Paint();
		// mPaint.setColor(Color.BLUE);
		mUtils = AppFuncUtils.getInstance(mActivity);
		mMaxScaleX = mUtils.getStandardSize(MAXSCALE_X);
		mMaxScaleY = mUtils.getStandardSize(MAXSCALE_Y);

		mDefaultX = x;
		mDefaultY = y;
		mDefaultWidth = width;
		mDefaultHeight = height;
		mDefaultPadding = (int) mContext.getResources().getDimension(
				R.dimen.app_screen_card_padding);
		mBorad = mContext.getResources().getDrawable(R.drawable.appdraw_screen_bg);
		mBoradLight = mContext.getResources().getDrawable(R.drawable.appdraw_screen_pressed_bg);
		mBoradFull = mContext.getResources().getDrawable(R.drawable.appdraw_screen_full_bg);
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		// canvas.drawRect(0,0,mWidth, mHeight, mPaint);
		if (mInfo != null) {
			if (mIsPressed && !mInfo.hasVancantCell()) {
				ImageUtil.drawImage(canvas, mBoradFull, 1, 0, 0, getWidth(), getHeight(), null);
			} else if (mIsPressed) {
				ImageUtil.drawImage(canvas, mBoradLight, 1, 0, 0, getWidth(), getHeight(), null);
			} else {
				ImageUtil.drawImage(canvas, mBorad, 1, 0, 0, getWidth(), getHeight(), null);
			}
			if (mInfo.getScreenPreviewBmp() != null) {
				// canvas.save();
				// float scaleFactorW = (getWidth() - padding *
				// 2)/mInfo.getScreenPreviewBmp().getWidth();
				// float scaleFactorH = (getHeight() - padding *
				// 2)/mInfo.getScreenPreviewBmp().getHeight();
				// canvas.scale(scaleFactorW, scaleFactorH);
				// canvas.drawBitmap(mInfo.getScreenPreviewBmp(), padding,
				// padding, null);
				// canvas.restore();

				try {
					mCurrentPaddingH = (getWidth() / mDefaultWidth) * mDefaultPadding;
					mCurrentPaddingV = (getHeight() / mDefaultHeight) * mDefaultPadding;
				} catch (Exception e) {
				}
				
				ImageUtil.drawImage(canvas, new BitmapDrawable(mInfo.getScreenPreviewBmp()), 1,
						(int) mCurrentPaddingH, (int) mCurrentPaddingV,
						(int) (getWidth() - mCurrentPaddingH),
						(int) (getHeight() - mCurrentPaddingV), null);
			}
			// ImageUtil.drawImage(canvas, new
			// BitmapDrawable(mInfo.getScreenPreviewBmp()), 1, padding, padding,
			// getWidth() - padding, getHeight() - padding, null);
		}
	}

	@Override
	protected synchronized boolean animate() {
		boolean ret = false;
		if (mScaleMotion != null) {
			if (mScaleMotion.isFinished()) {
				detachAnimator(mScaleMotion);
				mScaleMotion = null;
				// 判断当前缩放因子是否为0，若为0则把图标重置回正常状态，若不为零则把图标缩放到当前缩放因子对应的大小，同时状态设为已经放大
				if (mCurrentScaleFactory == 0) {

					int x = mDefaultX;
					int y = mDefaultY;
					int right = x + mDefaultWidth;
					int bottom = y + mDefaultHeight;
					layout(x, y, right, bottom);

					mCurrentState = SCALENORMAl;
				} else {
					int scaleX = 0;
					int scaleY = 0;
					int x = 0;
					int y = 0;
					if (mUtils.isVertical()) {
						scaleX = Math.round(mCurrentScaleFactory * mMaxScaleX);
						scaleY = Math.round(mCurrentScaleFactory * mMaxScaleY);

						x = mDefaultX - scaleX / 2;
						y = mDefaultY - scaleY;
					} else {
						scaleX = Math.round(mCurrentScaleFactory * mMaxScaleY);
						scaleY = Math.round(mCurrentScaleFactory * mMaxScaleX);

						x = mDefaultX - scaleX;
						y = mDefaultY - scaleY / 2;
					}

					int right = x + mDefaultWidth + scaleX;
					int bottom = y + mDefaultHeight + scaleY;
					layout(x, y, right, bottom);

					mCurrentState = SCALEED;
				}
			} else {
				// 改变图标的大小和位置
				int scaleX = 0;
				int scaleY = 0;
				int x = 0;
				int y = 0;
				if (mUtils.isVertical()) {
					scaleX = mScaleMotion.GetCurX();
					scaleY = mScaleMotion.GetCurY();
					x = mDefaultX - scaleX / 2;
					y = mDefaultY - scaleY;
				} else {
					scaleX = mScaleMotion.GetCurY();
					scaleY = mScaleMotion.GetCurX();
					x = mDefaultX - scaleX;
					y = mDefaultY - scaleY / 2;
				}

				int right = x + mDefaultWidth + scaleX;
				int bottom = y + mDefaultHeight + scaleY;
				layout(x, y, right, bottom);
			}
			ret = true;
		}
		if (mIsRepaint) {
			mIsRepaint = false;
			ret = true;
		}

		return ret;
	}

	public void setInfo(AppFuncScreenItemInfo info) {
		mInfo = info;
	}

	public AppFuncScreenItemInfo getInfo() {
		return mInfo;
	}

	/**
	 * 获得当前坐标x所对应的桌面屏幕的坐标位置
	 * 
	 * @param x为当前屏幕预览的相对坐标
	 * @return
	 */
	public int getScreenRelativeX(int x) {
		// int screenWidth = GoLauncher.getDisplayWidth();
		// int relativeX = x - getAbsX();
		// return relativeX * screenWidth / mWidth;
		return (int) ((x - mCurrentPaddingH) / getRelativeHScale());
	}

	/**
	 * 获得当前坐标y所对应的桌面屏幕的坐标位置
	 * 
	 * @param y为当前屏幕预览的相对坐标
	 * @return
	 */
	public int getScreenRelativeY(int y) {
		// int screenHeight = GoLauncher.getDisplayHeight();
		// int relativeY = y - getAbsY();
		// return relativeY * screenHeight / mHeight;
		return (int) ((y - mCurrentPaddingV) / getRelativeVScale());
	}

	/**
	 * 获得屏幕预览大小与桌面大小的水平缩放比例
	 * 
	 * @return
	 */
	public float getRelativeHScale() {
		float widthScale = 0.0f;
		int screenWidth = GoLauncher.getDisplayWidth();
		if (screenWidth > 0) {
			widthScale = 1.0f * (mWidth - 2 * mCurrentPaddingH) / screenWidth;
		}
		return widthScale;
	}

	/**
	 * 获得屏幕预览大小与桌面大小的垂直缩放比例
	 * 
	 * @return
	 */
	public float getRelativeVScale() {
		float heightScale = 0.0f;
		int screenHeight = GoLauncher.getDisplayHeight();
		if (screenHeight > 0) {
			heightScale = 1.0f * (mHeight - 2 * mCurrentPaddingV) / screenHeight;
		}
		return heightScale;
	}

	public int[] getCenterPoint() {
		int[] point = new int[2];
		point[0] = getAbsX() + mWidth / 2;
		point[1] = getAbsY() + mHeight / 2;
		return point;
	}

	@Override
	public boolean onTouch(MotionEvent event) {
		if (!mLockScreen) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN :
					mIsPressed = true;
					break;
				case MotionEvent.ACTION_CANCEL :
					mIsPressed = false;
					break;
				case MotionEvent.ACTION_UP :
					mIsPressed = false;

					if (XYInRange((int) event.getX(), (int) event.getY())) {
						DeliverMsgManager.getInstance().onChange(AppFuncConstants.APPFUNCFRAME,
								AppFuncConstants.EXITAPPFUNCFRAME, null);
						GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME,
								IDiyMsgIds.SCREEN_ENTER, mInfo.mIndex, 250, null);
					}
					break;
				default :
					break;
			}

			return super.onTouch(event);
		}
		return false;
	}

	/**
	 * 缩放图标的接口
	 * 
	 * @param scaleFactory
	 *            缩放因子，取值范围0-1,0表示将图标重置为正常状态，1表示将图标放大到最大
	 */
	public void scale(float scaleFactory) {
		if (mCurrentScaleFactory == scaleFactory) {
			return;
		}

		switch (mCurrentState) {
			case SCALEED : {
				if (scaleFactory == 0) {
					// 开始缩小动画
					startScaleAnimation(scaleFactory);
				} else {
					// 改变图标大小
					mCurrentScaleFactory = scaleFactory;

					int scaleX = 0;
					int scaleY = 0;
					int x = 0;
					int y = 0;
					if (mUtils.isVertical()) {
						scaleX = Math.round(mCurrentScaleFactory * mMaxScaleX);
						scaleY = Math.round(mCurrentScaleFactory * mMaxScaleY);

						x = mDefaultX - scaleX / 2;
						y = mDefaultY - scaleY;
					} else {
						scaleX = Math.round(mCurrentScaleFactory * mMaxScaleY);
						scaleY = Math.round(mCurrentScaleFactory * mMaxScaleX);

						x = mDefaultX - scaleX;
						y = mDefaultY - scaleY / 2;
					}

					int right = x + mDefaultWidth + scaleX;
					int bottom = y + mDefaultHeight + scaleY;
					layout(x, y, right, bottom);
				}
				break;
			}

			case SCALEING : {
				if (scaleFactory == 0) {
					// 开始缩小动画
					startScaleAnimation(scaleFactory);
				} else {
					// 不做任何事情
				}

				break;
			}

			case SCALENORMAl : {
				if (scaleFactory == 0) {
					// 不做任何事
				} else {
					// 开始放大动画
					startScaleAnimation(scaleFactory);
				}
				break;
			}
		}
	}

	private synchronized void startScaleAnimation(float scaleFactory) {
		if (mScaleMotion != null) {
			detachAnimator(mScaleMotion);
			mScaleMotion = null;
		}
		mCurrentState = SCALEING;
		int currentScaleX = Math.round(mCurrentScaleFactory * mMaxScaleX);
		int currentScaleY = Math.round(mCurrentScaleFactory * mMaxScaleY);

		int newScaleX = Math.round(scaleFactory * mMaxScaleX);
		int newScaleY = Math.round(scaleFactory * mMaxScaleY);

		mScaleMotion = new XALinear(1, XALinear.XALINEAR_ECSPEED, currentScaleX, currentScaleY,
				newScaleX, newScaleY, 5, 1, 1);
		attachAnimator(mScaleMotion);

		mCurrentScaleFactory = scaleFactory;
	}

	public void setPress(boolean press) {
		mIsPressed = press;
	}

	public void setLockScreen(boolean lock) {
		mLockScreen = lock;
	}
}
