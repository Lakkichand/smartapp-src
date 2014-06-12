package com.jiubang.ggheart.apps.appfunc.component;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.gau.go.launcherex.R;
import com.go.util.Utilities;
import com.go.util.graphics.DrawUtils;
import com.go.util.log.LogUnit;
import com.go.util.window.OrientationControl;
import com.jiubang.core.mars.EventType;
import com.jiubang.core.mars.IAnimateListener;
import com.jiubang.core.mars.IComponentEventListener;
import com.jiubang.core.mars.MImage;
import com.jiubang.core.mars.XALinear;
import com.jiubang.core.mars.XComponent;
import com.jiubang.core.mars.XMElastic;
import com.jiubang.core.mars.XMotion;
import com.jiubang.core.mars.XPanel;
import com.jiubang.ggheart.apps.appfunc.controler.FunControler;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.OrientationInvoker;
import com.jiubang.ggheart.apps.desks.appfunc.XBaseGrid;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager;
import com.jiubang.ggheart.apps.desks.appfunc.animation.AnimationManager.AnimationInfo;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IPreferencesIds;
import com.jiubang.ggheart.apps.desks.diy.PreferencesManager;
import com.jiubang.ggheart.data.info.AppSettingDefault;

/**
 * 功能表模块图标基类
 * @author yangguanxiang
 *
 */
public class BaseAppIcon extends XPanel
		implements
			IComponentEventListener,
			IMsgHandler,
			Cloneable,
			OrientationInvoker {
	/*
	 * 图标显示的style
	 */
	public static final int APP_ICON_ONLY = 0; // 只显示应用程序图标
	public static final int APP_EDIT_BOTH = 1; // 显示应用程序图标和编辑图标
	public static final int APP_EDIT_BOTH_TOP = 2; // 显示编辑图标和“New”图标
	public static final int APP_ICON_STORENUM = 3; // 显示GOSTORE数字和图标
	public static final int APP_ICON_SHOWUPDATE = 4; // 显示可更新图标
	public static final int APP_ICON_NOTIFICATIONNUM = 5; // 显示通讯统计书记和图标

	protected Paint mCountPaint;
	/**
	 * 应用程序图标
	 */
	protected MImage mAppPic;
	/**
	 * 程序名称
	 */
	protected String mTitle;
	/**
	 * 应用程序图标控件
	 */
	protected IconImage mIconImage;
	/**
	 * 图标控件高度
	 */
	protected int mIconHeight;
	/**
	 * 图标控件宽度
	 */
	protected int mIconWidth;
	/**
	 * 应用程序名称控件
	 */
	protected AppText mAppText;
	/**
	 * 锁定图片
	 */
	protected MImage mLockPic;
	/**
	 * 编辑图片
	 */
	protected MImage mEditPic;

	/**
	 * 编辑图片的高亮图
	 */
	protected MImage mEditLightPic;
	/**
	 * 是否为编辑模式
	 */
	protected boolean mEditMode;
	/**
	 * 背景图到控件上下边界的距离：
	 */
	protected int mMargin_v;
	/**
	 * 背景图到控件左右边界的距离：
	 */
	protected int mMargin_h;
	// /**
	// * 图标上边界到背景上边界的距离：
	// */
	// protected int mIconToBgTop_v;
	// /**
	// * 文字下边界到背景下边界的距离：
	// */
	// protected int mTextToBgBottom;
	// /**
	// * 图标左右边界到背景左右边界的距离：p
	// */
	// protected int mIconToBg_h;
	/**
	 * 图标与文字控件之间的间隔
	 */
	protected int mIconTextDst;
	/**
	 * 文字高度
	 */
	protected int mTextHeight;
	/**
	 * 名字控件高度
	 */
	public int mTextCtrlHeight;
	/**
	 * 画背景的画笔
	 */
	protected Paint mBgPaint;
	/**
	 * 背景图的范围
	 */
	protected RectF mBgRect;
	private final Rect mCounterRect = new Rect();
	/**
	 * 是否显示应用程序名称
	 */
	protected boolean mIsDrawText;
	/**
	 * Tick Count
	 */
	protected int mTickCount;
	protected Activity mActivity;
	/**
	 * 是否显示编辑图标
	 */
	protected boolean mShowEditPic;

	// 编辑图标是否正被点击中
	protected boolean mIsEditPress;

	/**
	 * 是否已经收到Down事件
	 */
	protected boolean mTouchDown;

	protected int mDelayfactor = 0;
	protected XMElastic mElastic = null;
	/**
	 * 图标显示的style
	 */
	protected int mIconStyle = APP_ICON_ONLY;
	/**
	 * 主题控制器
	 */
	protected AppFuncThemeController mThemeController;
	/**
	 * 新安装程序图片
	 */
	protected MImage mNewAppPic;

	/**
	 * 程序有更新图片
	 */
	protected MImage mUpdatePic;

	/**
	 * 通讯统计有未读图片
	 */
	protected Drawable mNotificationNumPic;
	// /**
	// * 程序有更新图片高亮
	// */
	// protected MImage mUpdatePicLight;

	protected Drawable mGOStoreNumPic;
	/**
	 * 图标名颜色
	 */
	public static int sTxtColor = AppFuncConstants.ICON_TEXT_COLOR;
	/**
	 * 聚焦时的背景颜色
	 */
	public static int sFocusedBgColor = AppFuncConstants.ICON_BG_FOCUSED;

	protected int mAlpha = 255;

	protected int mIntrinSize;

	/**
	 * 是否显示更新图标
	 */
	protected boolean mShowUpdate = false;

	private int mCounterPadding;
	private int mFontSize;
	private String mBeanListCount;
	// ////////////////////////add by yangbing/////////////////////////////////
	/**
	 * 通讯统计未读数
	 */
	protected String mUnreadCount;
	/**
	 * 是否在屏幕边界上
	 */
	protected boolean mIsInEdge;

	protected AppFuncUtils mUtils;

	protected boolean mShowTwoLines;
	protected XMotion mMoveMotion;

	protected AnimationManager mAnimManager;

	protected BaseAppIcon(Activity activity, int tickCount, int x, int y, int width, int height,
			BitmapDrawable appPic, BitmapDrawable lockPic, BitmapDrawable editPic,
			BitmapDrawable editLightPic, String title, boolean isDrawText) {
		super(tickCount, x, y, width, height);
		mActivity = activity;
		mTickCount = tickCount;
		mUtils = AppFuncUtils.getInstance(mActivity);
		mThemeController = AppFuncFrame.getThemeController();
		if (lockPic != null) {
			mLockPic = new MImage(lockPic);
		}
		if (editPic != null) {
			mEditPic = new MImage(editPic);
		}

		if (editLightPic != null) {
			mEditLightPic = new MImage(editLightPic);
		}
		initIntrinSize();
		mIsEditPress = false;

		mEditMode = false;
		mShowEditPic = false;
		mIsDrawText = isDrawText;
		if (appPic != null) {
			mAppPic = new MImage(appPic);
		}
		mTitle = title;
		resetTitleColorValue();
		sFocusedBgColor = mThemeController.getThemeBean().mAppIconBean.mIconBgColor;
		if (mAppPic != null) {
			calculateParams();
			constructAppIcon();
			constructAppText();

			mBgPaint = new Paint();
			mBgPaint.setStyle(Style.FILL);
			mBgPaint.setAntiAlias(true);
			constructBgRect();

			mCountPaint = new Paint();
			mCountPaint.setStyle(Style.FILL_AND_STROKE);
			mCountPaint.setAntiAlias(true); // 抗锯齿
			mCountPaint.setColor(android.graphics.Color.WHITE);
			mCountPaint.setTypeface(Typeface.DEFAULT_BOLD);
			mCountPaint.setTextAlign(Paint.Align.CENTER);

			if (mIconImage != null) {
				addComponent(mIconImage);
			}

			if (mIsDrawText) {
				if (mAppText != null) {
					addComponent(mAppText);
					mAppText.setDrawingCacheEnabled(true); // 绘图缓冲还是统一由外部容器来控制吧
				}
			}
		}
		mAnimManager = AnimationManager.getInstance(activity);
	}

	protected void initIntrinSize() {
		mIntrinSize = Utilities.getStandardIconSize(mActivity);
	}

	private void resetTitleColorValue() {
		if (GoLauncher.getCustomTitleColor()) {
			int color = GoLauncher.getAppTitleColor();
			if (color != 0) {
				sTxtColor = color;
			} else {
				sTxtColor = AppFuncConstants.ICON_TEXT_COLOR;
			}
		} else {
			sTxtColor = mThemeController.getThemeBean().mAppIconBean.mTextColor;
		}
	}

	public void setNameVisible(boolean isVisible) {
		if (mIsDrawText != isVisible) {
			mIsDrawText = isVisible;
			setIsDrawText();
			setAppTextDrawingCacheEnable(mIsDrawText);
		}

	}

	public void setEditMode(boolean editModeEnabled) {

	}

	public boolean getEditMode() {
		return mEditMode;
	}

	/**
	 * 是否是文件夹
	 * 
	 * @return
	 */
	public boolean isFolder() {
		return false;
	}

	/**
	 * 是否显示程序名称
	 * 
	 * @param drawText
	 */
	protected void setIsDrawText() {
		// mIsDrawText = drawText;
		// 重新排版
		if (mAppPic == null) {
			return;
		}
		calculateParams();
		if (mIsDrawText == false) {
			removeComponent(mAppText);
			constructAppIcon();
			constructBgRect();
		} else {
			if (indexOfComponent(mAppText) < 0) {
				addComponent(mAppText);
				constructAppIcon();
				constructAppText();
				constructBgRect();
			}
		}
		resetTitleColorValue();
		mAppText.setPaintColor(sTxtColor);
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		if (mIsFocused && mBgPaint != null) {
			// 绘制聚焦时的背景
			mBgPaint.setColor(sFocusedBgColor);
			canvas.drawRoundRect(mBgRect, 5, 5, mBgPaint);
		}
	}

	@Override
	protected boolean animate() {
		return false;
	}

	/**
	 * 计算坐标
	 */
	protected void calculateParams() {
		// 以下参数都是与屏幕像素无关，换算成实际长度和宽度需要乘以DPI
		setIconTextDst();
		initIconSize();
		// mIconToBgTop_v = 0;
		// mTextToBgBottom = 0;
		// mIconToBg_h = 0;
		if (mIsDrawText == true) {
			setFontSize();
		} else {
			mTextHeight = 0;
		}

		mTextCtrlHeight = mTextHeight + mUtils.getStandardSize(4);
		// if (AppFuncFrame.getDataHandler()!= null){
		// int standard = AppFuncFrame.getDataHandler().getStandard();
		// if (standard != FunAppSetting.LINECOLUMNNUMXY_THICK &&
		// standard != FunAppSetting.LINECOLUMNNUMXY_MIDDLE_2){
		// mTextCtrlHeight = mTextHeight * 2 + mUtils.getStandardSize(4);
		// }
		// }
		if (mShowTwoLines) {
			mTextCtrlHeight = mTextHeight * 2 + mUtils.getStandardSize(4);
		}

		mMargin_v = (mHeight - mIconTextDst - mTextCtrlHeight - mIconHeight) / 2;
		if (mMargin_v < 0) {
			mMargin_v = 0;
		}
		// mMargin_v=0;
		mMargin_h = (mWidth - mIconWidth) / 2;
		if (mMargin_h < 0) {
			mMargin_h = 0;
		}
	}

	protected void initIconSize() {
		if (mAppPic != null) {
			mIconHeight = (int) ((mAppPic.getHeight() * (1 + 0.194)));
			mIconWidth = (int) ((mAppPic.getWidth() * (1 + 2 * 0.194)));
		} else {
			mIconHeight = (int) (mIntrinSize * (1 + 0.194));
			mIconWidth = (int) (mIntrinSize * (1 + 2 * 0.194));
		}
	}

	protected void setIconTextDst() {
		mIconTextDst = mUtils.getDimensionPixelSize(R.dimen.appfunc_icon_text_dst);
	}

	/**
	 * 调整坐标
	 */
	public void resetLayoutParams(int x, int y, int width, int height) {
		mX = x;
		mY = y;
		setSize(width, height);
		if (mAppPic == null) {
			return;
		}
		calculateParams();
		constructAppIcon();
		constructAppText();
		constructBgRect();
		resetTitleColorValue();
		mAppText.setPaintColor(sTxtColor);
	}

	/**
	 * 初始化应用程序图标控件
	 * 
	 * @param tickCount
	 */
	protected void constructAppIcon() {
		int startX = (mWidth - mIconWidth) / 2;
		int startY = mMargin_v;
		if (mIconImage == null) {
			LogUnit.d("BaseIcon new 347");
			mIconImage = new IconImage(mTickCount, startX, startY, mIconWidth, mIconHeight);
		} else {
			mIconImage.setXY(startX, startY);
			mIconImage.setSize(mIconWidth, mIconHeight);
		}
	}

	/**
	 * 设置编辑图片X和Y坐标对于程序图标宽度的百分比
	 * 
	 * @param X
	 * @param Y
	 */
	public void setEditIconRatio(float x, float y) {
		if (mIconImage != null) {
			mIconImage.setEditPicRatio(x, y);
		}
	}

	/**
	 * 初始化应用程序名称控件
	 * 
	 * @param tickCount
	 */
	protected void constructAppText() {
		int textWidth = mWidth; // (mIconWidth > mWidth) ? (mWidth - 4) :
								// mIconWidth;
		int textX = 0; // (mWidth - textWidth) / 2;
		int textY = getTextTop();
		if (mAppText == null) {
			// 默认大屏手机分两行显示名称，小屏手机用一行
			LogUnit.d("BaseIcon new 379");
			// boolean drawTwoLines = false;
			// if(AppFuncUtils.getInstance(mActivity).getSmallerBound() > 320){
			// drawTwoLines = true;
			// }
			mAppText = new AppText(mActivity, mTickCount, textX, textY, textWidth, mTextCtrlHeight,
					mTextHeight, mTitle, mShowTwoLines);
		} else {
			mAppText.setXY(textX, textY);
			mAppText.setSize(textWidth, mTextCtrlHeight);
			mAppText.setTextSize(mTextHeight);
			mAppText.setShowTwoLines(mShowTwoLines);
		}
	}

	protected int getTextTop() {
		return mMargin_v + mIconHeight + mIconTextDst;
	}

	/**
	 * 设置背景图片区域
	 */
	protected void constructBgRect() {
		int left = mMargin_h;
		int top = (int) ((mIconHeight + mTextCtrlHeight + mIconTextDst) * 0.1 + mMargin_v); // 默认情况下背景图下移（图表控件+文字控件）10%
		int right = mWidth - mMargin_h;
		int bottom = (mIconHeight + mTextCtrlHeight + mIconTextDst) + mMargin_v;
		if (mMargin_h == 0) {
			left = 0;
			right = mWidth;
		}
		// if (mMargin_v == 0) {
		// top = (int) (mHeight * 0.1); // 图片控件+文字控件 > 图标控件时，按图标控件大小作背景图下移10%
		// bottom = (int) (mHeight * 1.1);
		// }
		if (mBgRect == null) {
			mBgRect = new RectF(left, top, right, bottom);
		} else {
			mBgRect.set(left, top, right, bottom);
		}
	}

	@Override
	public boolean onEventFired(XComponent component, byte eventType, Object event, int arg,
			Object object) {
		if (component instanceof XBaseGrid) {
			if (eventType == EventType.CLICKEVENT) {
				return onClickEvent(event, arg, object);
			} else if (eventType == EventType.MOTIONEVENT) {
				return onUpDownEvent(event, arg, (Integer) object);
			} else if (eventType == EventType.LONGCLICKEVENT) {
				return onLongClickEvent(event, arg, (Integer) object);
			} else if (eventType == EventType.FOCUSEVENT) {
				if (event.equals(Boolean.TRUE)) {
					mIsFocused = true;
				} else {
					mIsFocused = false;
				}
			}
		}
		return false;
	}

	/**
	 * 移动动画
	 * 
	 * @param dx
	 * @param dy
	 */
	public void moveMotion(int dx, int dy, IAnimateListener listener,
			OrientationInvoker orientationInvoker) {
		LogUnit.d("BaseIcon new 446");
		XMotion motion = new XALinear(1, XALinear.XALINEAR_EBACCEL, mX, mY, dx, dy, 9,
				(int) (2.1 * (dx - mX) / 9), (int) (2.1 * (dy - mY) / 9));
		// setMotionFilter(motion);
		// motion.setAnimateListener(listener);
		AnimationInfo info = new AnimationInfo(AnimationInfo.TYPE_CHANGE_POSITION, this, motion,
				listener);
		mAnimManager.attachAnimation(info, orientationInvoker);
	}

	/**
	 * 处理Click事件
	 * 
	 * @return
	 */
	protected boolean onClickEvent(Object event, int arg, Object object) {
		return false;
	}

	/**
	 * 处理Up&Down事件
	 */
	protected boolean onUpDownEvent(Object event, int offsetX, int offsetY) {
		return false;
	}

	protected boolean onLongClickEvent(Object event, int offsetX, int offsetY) {
		return false;
	}

	public void setAlpha(int alpha) {
		if (mAlpha == alpha) {
			return;
		}
		mAlpha = alpha;
		if (mAppPic != null) {
			mAppPic.setAlpha(alpha);
		}
		if (mEditPic != null) {
			mEditPic.setAlpha(alpha);
		}
		if (mEditLightPic != null) {
			mEditLightPic.setAlpha(alpha);
		}
		if (mLockPic != null) {
			mLockPic.setAlpha(alpha);
		}
		if (mAppText != null) {
			if (isChildrenDrawnWithCacheEnabled() && mAppText.isDrawingCacheEnabled()) {
				mAppText.setPaintAlpha(255);
				mAppText.setDrawingCacheAlpha(alpha);
			} else {
				mAppText.setPaintAlpha(alpha);
			}
		}

	}

	public int getAlpha() {
		int alpha = 255;
		if (mAppPic != null) {
			alpha = mAppPic.getAlpha();
		} else if (mEditPic != null) {
			alpha = mEditPic.getAlpha();
		} else if (mAppText != null) {
			if (isChildrenDrawnWithCacheEnabled() && mAppText.isDrawingCacheEnabled()) {
				alpha = mAppText.getDrawingCacheAlpha();
			} else {
				alpha = mAppText.getPaintAlpha();
			}
		}
		return alpha;
	}

	/**
	 * 长按后的弹起事件
	 */
	public void onLongClickUp(int x, int y, IAnimateListener listener,
			OrientationInvoker orientationInvoker) {
	}

	/**
	 * 判断点击的坐标是否在图标和文字范围内
	 */
	protected boolean inIconRange(int clickX, int clickY, int offsetX, int offsetY) {
		if (mAppPic == null) {
			return false;
		}
		int iconStartX = getAbsX() + mMargin_h + (int) (mAppPic.getWidth() * 0.194f);
		int iconStartY = getAbsY() + mMargin_v + (int) (mAppPic.getWidth() * 0.194f);
		if ((clickX + offsetX >= iconStartX)
				&& (clickX + offsetX <= iconStartX + mAppPic.getWidth())) {
			if ((clickY + offsetY >= iconStartY)
					&& (clickY + offsetY <= iconStartY + mAppPic.getHeight() + mTextCtrlHeight
							+ mIconTextDst)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 设置被抓起时的效果
	 */
	public void setDragEffect() {
		return;
	}

	/**
	 * 图标图片
	 * @author yangguanxiang
	 *
	 */
	protected class IconImage extends XComponent {
		private XMotion mXAngleMotion = null;
		/**
		 * 角度
		 */
		float mAngle;
		/**
		 * 锚点X
		 */
		int mAchorPicX;
		/**
		 * 锚点Y
		 */
		int mAchorPicY;

		// 编辑图标X坐标与图标的比例
		private float mEditPosRatioX;
		// 编辑图标Y坐标与图标的比例
		private float mEditPosRatioY;
		private final float mRatio = 0.75f; // 3.0f/4.0f

		// 控件的高度和宽度应该跟图片的实际大小相等
		public IconImage(int tickCount, int x, int y, int width, int height) {
			super(tickCount, x, y, width, height);
			final float rangeStart = 0.15f;
			final float rangeEnd = 0.85f;
			float tmp = rangeStart + (rangeEnd - rangeStart) * (float) Math.random();
			if (mAppPic != null) {
				mAchorPicX = (int) (mAppPic.getWidth() * tmp);
				tmp = rangeStart + (rangeEnd - rangeStart) * (float) Math.random();
				mAchorPicY = (int) (mAppPic.getHeight() * tmp);
			} else {
				mAchorPicX = (int) (mIntrinSize * tmp);
				tmp = rangeStart + (rangeEnd - rangeStart) * (float) Math.random();
				mAchorPicY = (int) (mIntrinSize * tmp);
			}
			// 默认值
			mEditPosRatioX = 0.791f; // 右下角，后面为右上角//0.194f + 0.53f;
			mEditPosRatioY = 0.736f; // 0;
		}

		/**
		 * 设置编辑图片X和Y坐标对于程序图标宽度的百分比
		 * 
		 * @param X
		 * @param Y
		 */
		public void setEditPicRatio(float x, float y) {
			mEditPosRatioX = x;
			mEditPosRatioY = y;
		}

		public void setRotateAnimator(XMotion motion) {
			if (mXAngleMotion != null) {
				detachAnimator(mXAngleMotion);
			}
			if (motion != null) {
				attachAnimator(motion);
				mXAngleMotion = motion;
				motion.reStart();
			}
		}

		public void exitEditMode() {
			if (mAppPic != null) {
				mAppPic.setAnchor(0, 0);
				mAppPic.setRotation(0);
			}

			mIsEditPress = false;
		}

		@Override
		protected void drawCurrentFrame(Canvas canvas) {
			int picWidth = mIntrinSize;
			if (mAppPic != null) {
				picWidth = mAppPic.getWidth();
			}
			if (mEditMode) {
				if (AppSettingDefault.APPFUNC_OPEN_EFFECT) {
					if (mXAngleMotion != null) {
						mAngle = mXAngleMotion.GetCurX() * mRatio; // 2.0f/3.0f;//
						if (mAppPic != null) {
							mAppPic.setRotation(mAngle);
							mAppPic.setAnchor(mAchorPicX, mAchorPicY);
							mAppPic.draw(canvas, mAppPic.getWidth() * 0.194f + mAchorPicX,
									mAppPic.getWidth() * 0.194f + mAchorPicY);
						}
					} else {
						if (mAppPic != null) {
							mAppPic.draw(canvas, picWidth * 0.194f, picWidth * 0.194f);
						}
					}
				} else {
					if (mAppPic != null) {
						mAppPic.draw(canvas, picWidth * 0.194f, picWidth * 0.194f);
					}
				}

				if (mShowEditPic) {
					if (mIsEditPress) {
						if (mEditLightPic != null) {
							mEditLightPic.setRotation(mAngle);
							int offsetX = (int) (picWidth * 0.53f);
							int offsetY = (int) (picWidth * 0.194f);
							mEditLightPic.setAnchor(mAchorPicX - offsetX, mAchorPicY + offsetY);

							mEditLightPic.draw(canvas, picWidth * 0.194f + mAchorPicX, picWidth
									* 0.194f + mAchorPicY);
						}
					} else {
						if (mEditPic != null) {
							mEditPic.setRotation(mAngle);

							int offsetX = (int) (picWidth * 0.53f);
							int offsetY = (int) (picWidth * 0.194f);
							mEditPic.setAnchor(mAchorPicX - offsetX, mAchorPicY + offsetY);

							mEditPic.draw(canvas, picWidth * 0.194f + mAchorPicX, picWidth * 0.194f
									+ mAchorPicY);
						}
						if (mLockPic != null && mIconStyle == APP_EDIT_BOTH) {
							mLockPic.setRotation(mAngle);

							int offsetX = (int) (picWidth * mEditPosRatioX * 1.06f);
							int offsetY = (int) (picWidth * mEditPosRatioY * 1.07f);
							mLockPic.setAnchor(mAchorPicX - offsetX, mAchorPicY - offsetY);

							mLockPic.draw(canvas, mAchorPicX, mAchorPicY);
						}
					}
				}
			} else if (mIconStyle == APP_EDIT_BOTH) {
				if (mAppPic != null) {
					mAppPic.draw(canvas, picWidth * 0.194f, picWidth * 0.194f);
				}
				if (mLockPic != null) {
					mLockPic.setAnchor(0, 0);
					mLockPic.draw(canvas, picWidth * mEditPosRatioX * 1.05f, picWidth
							* mEditPosRatioY * 1.06f);
				}

			} else if (mIconStyle == APP_EDIT_BOTH_TOP) {
				if (mAppPic != null) {
					mAppPic.draw(canvas, picWidth * 0.194f, picWidth * 0.194f);
				}
				if (mNewAppPic != null) {
					mNewAppPic.draw(canvas, picWidth * mEditPosRatioX, picWidth * 0.1f);
				}
			} else if (mIconStyle == APP_ICON_STORENUM) {
				if (mAppPic != null) {
					mAppPic.draw(canvas, picWidth * 0.194f, picWidth * 0.194f);
				}
				if (mGOStoreNumPic != null && mShowUpdate && mBeanListCount != null
						&& mCounterRect != null) {
					mGOStoreNumPic.draw(canvas);
					canvas.drawText(mBeanListCount, mCounterRect.centerX(), mCounterRect.centerY()
							+ mCounterPadding, mCountPaint);
				}

			} else if (mIconStyle == APP_ICON_SHOWUPDATE) {
				if (mAppPic != null) {
					mAppPic.draw(canvas, picWidth * 0.194f, picWidth * 0.194f);
				}
				if (mUpdatePic != null && mShowUpdate) {
					// 需要抖动的图片才需要以下注释的代码，设置图片的锚点，和绘制的偏移量
					//					int offsetX = (int) (picWidth * 0.53f);
					//					int offsetY = (int) (picWidth * 0.194f);
					//					mUpdatePic.setAnchor(mAchorPicX - offsetX, mAchorPicY + offsetY);
					//					mUpdatePic.draw(canvas, picWidth * 0.194f + mAchorPicX, picWidth * 0.194f
					//							+ mAchorPicY);

					mUpdatePic.draw(canvas, picWidth * mEditPosRatioX, picWidth * 0.1f);
				}
			} else if (mIconStyle == APP_ICON_NOTIFICATIONNUM) {
				if (mAppPic != null) {
					mAppPic.draw(canvas, picWidth * 0.194f, picWidth * 0.194f);
				}
				if (mNotificationNumPic != null && mUnreadCount != null && mCounterRect != null) {
					if (Integer.parseInt(mUnreadCount) > 0) {
						mNotificationNumPic.draw(canvas);
						canvas.drawText(mUnreadCount, mCounterRect.centerX(),
								mCounterRect.centerY() + mCounterPadding, mCountPaint);
					}
				}

			} else {
				if (mAppPic != null) {
					mAppPic.draw(canvas, picWidth * 0.194f, picWidth * 0.194f);
				}
			}

		}

		@Override
		protected boolean animate() {
			return false;
		}
	}

	@Override
	public void notify(int key, Object obj) {
		switch (key) {
			case AppFuncConstants.LOADTHEMERES : {
				// sTxtColor =
				// sThemeController.getThemeBean().mAppIconBean.mTextColor;
				sFocusedBgColor = mThemeController.getThemeBean().mAppIconBean.mIconBgColor;
				resetTitleColorValue();
				if (mAppText != null) {
					mAppText.setPaintColor(sTxtColor);
				}
				break;
			}
		}
	}

	public void setEditPic(BitmapDrawable drawable) {
		if (mEditPic != null) {
			mEditPic.setDrawable(drawable);
		} else {
			if (drawable != null) {
				mEditPic = new MImage(drawable);
			}
		}
	}

	public void setEditLightPic(BitmapDrawable drawable) {
		if (mEditLightPic != null) {
			mEditLightPic.setDrawable(drawable);
		} else {
			if (drawable != null) {
				mEditLightPic = new MImage(drawable);
			}
		}
	}

	/**
	 * 当前的点是否在编辑图片上
	 */
	public boolean isInEditPicComponent(int x, int y) {

		// 增加(int) (2 * DrawUtils.sDensity)，用于加大卸载按钮的响应区域
		int editWidth = mUtils.getStandardSize(47);
		int editHeight = editWidth;
		if (mEditPic != null) {
			editWidth = mEditPic.getWidth();
			editHeight = mEditPic.getHeight();
		}
		int editPicStartX = getAbsX() + (mWidth - mMargin_h - editWidth)
				- (int) (2 * DrawUtils.sDensity);
		int editPicStartY = getAbsY() + mMargin_v - (int) (2 * DrawUtils.sDensity);

		if ((x >= editPicStartX)
				&& (x <= editPicStartX + editWidth + (int) (2 * DrawUtils.sDensity))) {
			if ((y >= editPicStartY)
					&& (y <= editPicStartY + editHeight + (int) (2 * DrawUtils.sDensity))) {
				return true;
			}
		}

		return false;
	}

	public MImage getNewAppPic() {
		return mNewAppPic;
	}

	public void setNewAppPic(BitmapDrawable newAppPic) {
		if (mNewAppPic == null) {
			if (newAppPic != null) {
				mNewAppPic = new MImage(newAppPic);
			}
		} else {
			mNewAppPic.setDrawable(newAppPic);
		}
	}

	/**
	 * 设置有更新图片
	 * 
	 * @param upAppPic
	 */
	public void setUpdatePic(BitmapDrawable upAppPic) {
		if (mUpdatePic == null) {
			if (upAppPic != null) {
				mUpdatePic = new MImage(upAppPic);
			}
		} else {
			mUpdatePic.setDrawable(upAppPic);
		}
	}

	public MImage getUpdatePic() {
		return mUpdatePic;
	}

	public void setLockPic(BitmapDrawable lockPic) {
		if (lockPic != null) {
			if (mLockPic == null) {
				mLockPic = new MImage(lockPic);
			} else {
				mLockPic.setDrawable(lockPic);
			}
		}
	}

	/**
	 * 设置有应用更新,显示GOStore数字
	 * 
	 * @param GOStroeNumPic
	 */
	public void setGOStoreNumPic(Drawable goStroeNumPic) {
		if (goStroeNumPic == null) {
			mBeanListCount = null;
			mGOStoreNumPic = null;
			return;
		}
		int picWidth = mIntrinSize;
		if (mAppPic != null) {
			picWidth = mAppPic.getWidth();
		}
		int count = AppFuncFrame.getFunControler().getmBeancount();

		PreferencesManager preferences = new PreferencesManager(mContext,
				IPreferencesIds.DESK_SHAREPREFERENCES_FILE, Context.MODE_PRIVATE);
		int gostoreControl = preferences.getInt(FunControler.GOSTORE_SHOW_MESSAGE, 0);

		if (mCountPaint != null && count > 0 && gostoreControl == 1) {
			this.mGOStoreNumPic = goStroeNumPic;
			mBeanListCount = String.valueOf(count);
			int stringLength = (int) mCountPaint.measureText(mBeanListCount, 0,
					mBeanListCount.length() - 1);
			stringLength = Math.max(stringLength, 0);
			int notifyWidth = mActivity.getApplicationContext().getResources()
					.getDimensionPixelSize(R.dimen.dock_notify_width);
			int notifyHeight = mActivity.getApplicationContext().getResources()
					.getDimensionPixelSize(R.dimen.dock_notify_height);
			mCounterPadding = mActivity.getApplicationContext().getResources()
					.getDimensionPixelSize(R.dimen.counter_circle_padding);
			mFontSize = mActivity.getApplicationContext().getResources()
					.getDimensionPixelSize(R.dimen.dock_notify_font_size);
			mCounterRect.top = (int) (picWidth * 0.1f);
			mCounterRect.right = (int) (picWidth * 0.791f + goStroeNumPic.getMinimumWidth());
			mCounterRect.bottom = mCounterRect.top + notifyHeight;
			mCounterRect.left = mCounterRect.right - stringLength - notifyWidth;
			mGOStoreNumPic.setBounds(mCounterRect.left, mCounterRect.top, mCounterRect.right,
					mCounterRect.bottom);
			mCountPaint.setTextSize(mFontSize);
		} else {
			mBeanListCount = null;
			mGOStoreNumPic = null;
		}
	}

	/**
	 * 设置通讯统计未读数字
	 * 
	 * @param NotificationNumPic
	 * @param count
	 */
	public void setNotificationNumPic(Drawable NotificationNumPic, int count) {
		int picWidth = mIntrinSize;
		if (mAppPic != null) {
			picWidth = mAppPic.getWidth();
		}
		if (mCountPaint != null && count > 0 && null != NotificationNumPic) {
			mNotificationNumPic = NotificationNumPic;
			mUnreadCount = String.valueOf(count);
			int stringLength = (int) mCountPaint.measureText(mUnreadCount, 0,
					mUnreadCount.length() - 1);
			stringLength = Math.max(stringLength, 0);
			int notifyWidth = mActivity.getApplicationContext().getResources()
					.getDimensionPixelSize(R.dimen.dock_notify_width);
			int notifyHeight = mActivity.getApplicationContext().getResources()
					.getDimensionPixelSize(R.dimen.dock_notify_height);
			mCounterPadding = mActivity.getApplicationContext().getResources()
					.getDimensionPixelSize(R.dimen.counter_circle_padding);
			mFontSize = mActivity.getApplicationContext().getResources()
					.getDimensionPixelSize(R.dimen.dock_notify_font_size);
			mCounterRect.top = (int) (picWidth * 0.1f);
			mCounterRect.right = (int) (picWidth * 0.791f + mNotificationNumPic.getMinimumWidth());
			mCounterRect.bottom = mCounterRect.top + notifyHeight;
			mCounterRect.left = mCounterRect.right - stringLength - notifyWidth;
			mNotificationNumPic.setBounds(mCounterRect.left, mCounterRect.top, mCounterRect.right,
					mCounterRect.bottom);
			mCountPaint.setTextSize(mFontSize);
		} else {
			mUnreadCount = null;
			mNotificationNumPic = null;
		}
	}

	// public MImage getGOStoreNumPic() {
	// return mUpdatePic;
	// }
	// /**
	// * 设置有更新高亮图片
	// * @param upAppPic
	// */
	// public void setUpdatePicLight(BitmapDrawable upAppPicLight){
	// if (mUpdatePicLight == null) {
	// if(upAppPicLight != null){
	// mUpdatePicLight = new MImage(upAppPicLight);
	// }
	// } else {
	// mUpdatePicLight.setDrawable(upAppPicLight);
	// }
	// }
	//
	// public MImage getUpdatePicLight() {
	// return mUpdatePicLight;
	// }

	public void setAppTextDrawingCacheEnable(boolean enabled) {
		if (mAppText != null) {
			mAppText.setDrawingCacheEnabled(enabled);
		}
	}

	public void setIconDrawQuality(boolean high) {
		if (mAppPic != null) {
			mAppPic.setRenderQuality(high ? MImage.FILTER_BEST : MImage.FILTER_NONE);
		}
	}

	protected void setFontSize() {
		mTextHeight = (int) (GoLauncher.getAppFontSize() * DrawUtils.sDensity);
	}

	public void setShowUpadte(boolean show) {
		mShowUpdate = show;
	}

	public void setIsDrawAppText(boolean draw) {
		if (mAppText != null) {
			mAppText.setIsDrawText(draw);
		}
	}

	public boolean getIsDrawAppText() {
		if (mAppText != null) {
			return mAppText.getIsDrawText();
		}
		return true;
	}

	// ////////////////////////add by yangbing////////////////////
	/**
	 * 是否位于屏幕边沿
	 */
	public void setInEdge(boolean isInEdge) {
		if (mIsInEdge != isInEdge) {
			mIsInEdge = isInEdge;
			if (isInEdge) {
				if (mAppPic != null) {
					mAppPic.setColorFilter(AppFuncConstants.ICON_IN_EDGE_COLOR,
							PorterDuff.Mode.SRC_ATOP);
				}
				// mEditPic.setColorFilter(0x8081E8ED);
				if (mAppText != null) {
					mAppText.setPaintColor(AppFuncConstants.ICON_IN_EDGE_COLOR);
				}
			} else {
				if (mAppPic != null) {
					mAppPic.setColorFilter(0);
				}
				// mEditPic.setColorFilter(0);
				if (mAppText != null) {
					mAppText.setPaintColor(BaseAppIcon.sTxtColor);
				}
			}
		}
	}

	public boolean isInEdge() {
		return mIsInEdge;
	}

	public String getTitle() {
		return "";
	}

	/**
	 * 改变透明度 alpha: 0~255
	 */
	public AnimationInfo changeAlpha(int sourceAlpha, int targetAlpha, int totalStep,
			IAnimateListener listener, OrientationInvoker orientationInvoker, boolean batchMode) {
		return null;
	}

	/**
	 * 改变缩放比例 当sourceHScale < 0时，用图标当前的比例为初始比例 当sourceVScale < 0时，用图标当前的比例为初始比例
	 */
	public void changeScale(float sourceHScale, float sourceVScale, float targetHScale,
			float targetVScale, IAnimateListener listener) {

	}

	/**
	 * 改变图标位置
	 */
	// public void move(int sourceX, int sourceY, int targetX, int targetY,
	// IAnimateListener listener) {
	//
	// }

	public void unRegister() {
		// 注册加载资源事件
		DeliverMsgManager.getInstance().unRegisterDispenseMsgHandler(AppFuncConstants.LOADTHEMERES,
				this);
	}

	public void setShowTwoLines(boolean showTwoLines) {
		mShowTwoLines = showTwoLines;
	}

	public boolean isShowTwoLines() {
		return mShowTwoLines;
	}

	public void setTitle(String title) {
		mTitle = title;
		if (mAppText != null) {
			mAppText.setNameTxt(mTitle);
		}
	}

	public AnimationInfo moveIcon(Class<? extends XMotion> motionClazz, int type, int startX,
			int startY, int endX, int endY, int totalStep, int startSpeedX, int startSpeedY,
			IAnimateListener listener, OrientationInvoker orientationInvoker, boolean batchMode) {
		// if (startX == endX && startY == endY) {
		// return;
		// }
		if (mMoveMotion != null) {
			mAnimManager.cancelAnimation(this, mMoveMotion);
			//			detachAnimator(mMoveMotion);
			mMoveMotion = null;
		}
		if (motionClazz == XMElastic.class) {
			mMoveMotion = new XMElastic(1, type, startX, startY, endX, endY, totalStep, 0.1f, 1.0f);
		} else {
			mMoveMotion = new XALinear(1, type, startX, startY, endX, endY, totalStep, startSpeedX,
					startSpeedY);
		}
		AnimationInfo info = new AnimationInfo(AnimationInfo.TYPE_CHANGE_POSITION, this,
				mMoveMotion, listener);
		if (!batchMode) {
			mAnimManager.attachAnimation(info, orientationInvoker);
		}
		return info;
	}

	@Override
	public void keepCurrentOrientation() {
		OrientationControl.keepCurrentOrientation(mActivity);
	}

	@Override
	public void resetOrientation() {
		OrientationControl.setOrientation(mActivity);
	}
}
