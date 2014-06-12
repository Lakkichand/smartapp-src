package com.jiubang.ggheart.apps.appfunc.component;

import java.util.List;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;

import com.gau.go.launcherex.R;
import com.go.util.graphics.ImageUtil;
import com.go.util.log.Loger;
import com.jiubang.core.mars.EventType;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.appfunc.theme.AppFuncThemeController;
import com.jiubang.ggheart.apps.desks.appfunc.AppFuncFrame;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.appfunc.model.DeliverMsgManager;
import com.jiubang.ggheart.apps.desks.appfunc.model.IMsgHandler;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.DataType;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.data.theme.ThemeManager;
import com.jiubang.ggheart.data.theme.bean.AppFuncThemeBean;
import com.jiubang.ggheart.launcher.GOLauncherApp;
/**
 * 
 * <br>类描述:功能表tab栏中单独一个tab项的ui组件，包括图标和标题
 * <br>功能详细描述:
 * 
 * @author  
 * @date  
 */
public class AppFuncTabSingleTitle extends XComponent implements IMsgHandler, BroadCasterObserver {
	/**
	 * Tab栏图标默认
	 */
	protected BitmapDrawable mIconUnselected;
	/**
	 * Tab栏图标选中和聚焦
	 */
	protected BitmapDrawable mIconSelected;
	/**
	 * Tab栏图标当前
	 */
	protected BitmapDrawable mIconCurrent;
	/**
	 * 当前Tab背景图(竖屏)
	 */
	protected Drawable mIconSelected_v;
	/**
	 * 当前Tab背景图(横屏)
	 */
	protected Drawable mIconSelected_h;
	/**
	 * 当前Tab背景图绘制方式
	 */
	protected byte mIconSelectedDrawingWay;
	/**
	 * Tab聚焦时的背景图(竖屏)
	 */
	protected Drawable mIconFocused_v;
	/**
	 * Tab聚焦时的背景图(横屏)
	 */
	protected Drawable mIconFocused_h;
	/**
	 * Tab聚焦时的背景图绘制方式
	 */
	protected byte mIconFocusedDrawingWay;
	/**
	 * Tab普通状态下的背景图(竖屏)
	 */
	protected Drawable mIconBg_v;
	/**
	 * Tab普通状态下的背景图(横屏)
	 */
	protected Drawable mIconBg_h;
	/**
	 * Tab普通状态下的背景图绘制方式
	 */
	protected byte mIconBgDrawingWay;
	/**
	 * 是否绘制边线
	 */
	protected int mDrawLines;
	/**
	 * 是否已经获取公共资源
	 */
	protected boolean mHasGottenImages = false;
	/**
	 * 名称
	 */
	protected String mTabName;
	/**
	 * 画笔(用于写名称)
	 */
	protected Paint mPaint;
	/**
	 * 画笔(用于写名称,有字体投影)
	 */
	protected Paint mShadowPaint;
	/**
	 * 画笔(用于写名称,没有字体投影)
	 */
	protected Paint mNoShadowPaint;
	protected Activity mActivity;
	/**
	 * 文字尺寸
	 */
	protected int mTextSize;
	/**
	 * 图标宽度
	 */
	protected int mIconWidth;
	/**
	 * 图标高度
	 */
	protected int mIconHeight;
	/**
	 * Tab栏是否随屏幕旋转而旋转
	 */
	protected int mEnableOritentation;
	/**
	 * 竖屏情况下Tab栏文字到底边的距离
	 */
	protected static int sTitleGap_v;
	/**
	 * 横屏情况下Tab栏文字到底边的距离
	 */
	protected static int sTitleGap_h;
	/**
	 * 显示出来的名字
	 */
	protected String mShowString;
	/**
	 * 工具类引用
	 */
	protected AppFuncUtils mUtils;
	/**
	 * 背景图画笔
	 */
	protected Paint mBgPaint;
	/**
	 * 图标画笔
	 */
	protected Paint mIconPaint;
	/**
	 * 聚焦时的渐变对象
	 */
	protected Shader mFocusedShader;
	/**
	 * 选择时的渐变对象
	 */
	protected Shader mSelectedShader;
	/**
	 * 当前图片
	 */
	protected BitmapDrawable mCurrentImg;
	/**
	 * 主题控制器
	 */
	protected AppFuncThemeController mThemeCtrl;

	protected static final int COLOR_WHITE = 0xCCFFFFFF;
	protected static final int COLOR_BLACK = 0x7F000000;
	/**
	 * 控件规格，以Hight Density为标准
	 */
	protected static final int TAB_ICON_WIDTH_ID = R.dimen.appfunc_tab_icon_width;
	protected static final int TAB_ICON_HEIGHT_ID = R.dimen.appfunc_tab_icon_height;
	protected static final int TAB_TEXT_SIZE_ID = R.dimen.appfunc_tab_text_size;

	public AppFuncTabSingleTitle(Activity activity, int tickCount, int x, int y, int width,
			int height, String name, int id) {
		super(tickCount, x, y, width, height);
		mActivity = activity;
		mUtils = AppFuncUtils.getInstance(mActivity);
		mThemeCtrl = AppFuncFrame.getThemeController();
		mTabName = name;
		mId = id;
		mTextSize = mUtils.getDimensionPixelSize(TAB_TEXT_SIZE_ID);
		setmNoShadowPaint();
		setmShadowPaint();
		mPaint = mNoShadowPaint;
		// float density = mUtils.getDensity();
		// if (mUtils.getSmallerBound() <= 320 * density) {
		// sIconWidth = (int) (320 * density * TAB_ICON_WIDTH / 480);
		// sIconHeight = (int) (320 * density * TAB_ICON_HEIGHT / 480);
		// } else {
		// sIconWidth = (int) (density * TAB_ICON_WIDTH);
		// sIconHeight = (int) (density * TAB_ICON_HEIGHT);
		// }
		mIconWidth = mUtils.getDimensionPixelSize(TAB_ICON_WIDTH_ID);
		mIconHeight = mUtils.getDimensionPixelSize(TAB_ICON_HEIGHT_ID);

		// 注册主题改变事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.THEME_CHANGE,
				this);
		// 注册加载主题资源事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.LOADTHEMERES,
				this);
		// 注册重新加载TABHOME资源事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(
				AppFuncConstants.RELOADTABHOMETHEMERES, this);

	}

	/**
	 * 设置写tab名称的画笔,无投影
	 * */
	private void setmNoShadowPaint() {
		if (mNoShadowPaint != null) {
			mNoShadowPaint = null;
		}
		mNoShadowPaint = new Paint();
		mNoShadowPaint.setTextSize(mTextSize);
		mNoShadowPaint.setAntiAlias(true);
		mNoShadowPaint.setColor(mThemeCtrl.getThemeBean().mTabTitleBean.mTabTitleColorUnSelected);
	}

	/**
	 * 设置写tab名称的画笔，有投影
	 * */
	private void setmShadowPaint() {
		if (mShadowPaint != null) {
			mShadowPaint = null;
		}
		mShadowPaint = new Paint();
		mShadowPaint.setTextSize(mTextSize);
		mShadowPaint.setAntiAlias(true);
		mShadowPaint.setColor(mThemeCtrl.getThemeBean().mTabTitleBean.mTabTitleColorSelected);
		if (mThemeCtrl.isDefaultTheme()) {
			mShadowPaint.setShadowLayer(3.0f, 0.0f, 2.0f, 0xff000000);
		}
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		if (mBgPaint == null) {
			mBgPaint = new Paint();
		} else {
			mBgPaint.reset();
		}
		if (mIconPaint == null) {
			mIconPaint = new Paint();
		} else {
			mIconPaint.reset();
		}

		// 先绘制背景图
		if (mIsFocused || mIsPressed) {
			// 默认主题直接画图形
			// if (mThemeCtrl.isDefaultTheme()) {
			// drawGradientBitmap(canvas, mBgPaint, mFocusedShader);
			// } else {
			Drawable iconFocus = null;
			if (mUtils.isVertical()) {
				iconFocus = (mIconFocused_v != null) ? mIconFocused_v : mIconFocused_h;
			} else {
				iconFocus = (mIconFocused_h != null) ? mIconFocused_h : mIconFocused_v;
			}
			if (iconFocus != null) {
				iconFocus.setDither(true);
				ImageUtil.drawImage(canvas, iconFocus, mIconFocusedDrawingWay, 0, 0, mWidth,
						mHeight, mIconPaint);
			}
			// }
			if (mIconSelected != null) {
				mCurrentImg = mIconSelected;
			} else if (mIconUnselected != null) {
				mCurrentImg = mIconUnselected;
			} else {
				mCurrentImg = mIconCurrent;
			}
			mPaint = mNoShadowPaint;
		} else if (mIsSelected) {
			// 默认主题直接画图形
			// if (mThemeCtrl.isDefaultTheme()) {
			// drawGradientBitmap(canvas, mBgPaint, mSelectedShader);
			// } else {
			Drawable iconSelected = null;
			if (mUtils.isVertical()) {
				iconSelected = (mIconSelected_v != null) ? mIconSelected_v : mIconSelected_h;
			} else {
				iconSelected = (mIconSelected_h != null) ? mIconSelected_h : mIconSelected_v;
			}
			if (iconSelected != null) {
				iconSelected.setDither(true);
				ImageUtil.drawImage(canvas, iconSelected, mIconSelectedDrawingWay, 0, 0, mWidth,
						mHeight, mIconPaint);
			}
			// }
			if (mIconCurrent != null) {
				mCurrentImg = mIconCurrent;
			} else {
				String tabHomeBgPackage = mThemeCtrl.getThemeBean().mHomeBean.mTabHomeBgPackage;
				if (tabHomeBgPackage.equals(ThemeManager.DEFAULT_THEME_PACKAGE)) {
					if (mIconSelected != null) {
						mCurrentImg = mIconSelected;
					} else {
						mCurrentImg = mIconUnselected;
					}
				} else {
					if (mIconUnselected != null) {
						mCurrentImg = mIconUnselected;
					} else {
						mCurrentImg = mIconSelected;
					}
				}
			}
			mPaint = mShadowPaint;
		} else {
			// 默认主题直接画图形
			// if (mThemeCtrl.isDefaultTheme()) {
			// drawNormalBitmap(canvas, mBgPaint);
			// } else {
			Drawable iconBg = null;
			if (mUtils.isVertical()) {
				iconBg = (mIconBg_v != null) ? mIconBg_v : mIconBg_h;
			} else {
				iconBg = (mIconBg_h != null) ? mIconBg_h : mIconBg_v;
			}
			if (iconBg != null) {
				iconBg.setDither(true);
				ImageUtil.drawImage(canvas, iconBg, mIconBgDrawingWay, 0, 0, mWidth, mHeight,
						mIconPaint);
			}
			// }
			if (mIconUnselected != null) {
				mCurrentImg = mIconUnselected;
			} else if (mIconSelected != null) {
				mCurrentImg = mIconSelected;
			} else {
				mCurrentImg = mIconCurrent;
			}
			mPaint = mNoShadowPaint;
		}

		// 画线
		// if (mThemeCtrl.isDefaultTheme()) {
		// drawLines(canvas, mBgPaint);
		// } else {
		if (mDrawLines == 1) {
			drawLines(canvas, mBgPaint);
		}
		// }

		int x, y;
		if (mCurrentImg != null) {
			// 再画图标
			x = (mWidth - mIconWidth) / 2;
			if (mUtils.isVertical()) {
				y = sTitleGap_v;
			} else {
				y = sTitleGap_h;
			}
			ImageUtil.drawImage(canvas, mCurrentImg, ImageUtil.CENTERMODE, 0, 0, mWidth, mHeight
					- mTextSize - y, mIconPaint);
			// 再写Tab名称
			if (mShowString != null) {
				x = (mWidth - (int) mPaint.measureText(mShowString)) / 2;
				if (mUtils.isVertical()) {
					if (mThemeCtrl.isDefaultTheme()) {
						// 默认主题特殊处理
						y = mHeight - sTitleGap_v - mUtils.getStandardSize(5);
					} else {
						y = mHeight - sTitleGap_v;
					}
				} else {
					y = mHeight - sTitleGap_h;
				}

				canvas.drawText(mShowString, x, y, mPaint);
			}
		} else {
			if (mShowString != null) {
				x = (mWidth - (int) mPaint.measureText(mShowString)) / 2;
				y = mHeight / 2;
				canvas.drawText(mShowString, x, y, mPaint);
			}
		}
	}

	/**
	 * 绘制tab名称
	 * */
	private void drawText() {
		// canvas.drawText(mShowString, x, y, mPaint);

	}

	@Override
	protected void onShow() {
		super.onShow();
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		controler.registerObserver(this);
		initTypeface(controler.getUsedFontBean().mFontTypeface,
				controler.getUsedFontBean().mFontStyle);
	}

	@Override
	protected void onHide() {
		super.onHide();
		GOLauncherApp.getSettingControler().unRegisterObserver(this);
	}

	private void initTypeface(Typeface typeface, int style) {
		setTypeface(typeface, style);
	}

	public void setTypeface(Typeface tf, int style) {
		if (style > 0) {
			if (tf == null) {
				tf = Typeface.defaultFromStyle(style);
			} else {
				tf = Typeface.create(tf, style);
			}

			setTypeface(tf);
			// now compute what (if any) algorithmic styling is needed
			int typefaceStyle = tf != null ? tf.getStyle() : 0;
			int need = style & ~typefaceStyle;
			mPaint.setFakeBoldText((need & Typeface.BOLD) != 0);
			mPaint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
		} else {
			mPaint.setFakeBoldText(false);
			mPaint.setTextSkewX(0);
			setTypeface(tf);
		}
	}

	public void setTypeface(Typeface tf) {
		if (mPaint.getTypeface() != tf) {
			mPaint.setTypeface(tf);
		}
	}

	@Override
	protected boolean animate() {
		return false;
	}

	/**
	 * 画渐变矩形
	 */
	protected void drawGradientBitmap(Canvas canvas, Paint paint, Shader shader) {
		Shader saveShader = paint.getShader();

		if (shader != null) {
			paint.setShader(shader);
		}
		if (mUtils.isVertical()) {
			canvas.drawRect(0, 0, mWidth, mHeight - 2, paint);
		} else {
			canvas.drawRect(0, 0, mWidth - 2, mHeight, paint);
		}

		paint.setShader(saveShader);
	}

	/**
	 * 画普通矩形
	 * 
	 * @param canvas
	 */
	protected void drawNormalBitmap(Canvas canvas, Paint paint) {
		paint.setStyle(Style.FILL);
		paint.setColor(AppFuncConstants.TAB_EMPTY);
		if (mUtils.isVertical()) {
			canvas.drawRect(0, 0, mWidth, mHeight - 2, paint);
		} else {
			canvas.drawRect(0, 0, mWidth - 2, mHeight, paint);
		}
	}

	/**
	 * 画线
	 * 
	 * @param canvas
	 * @param paint
	 */
	protected void drawLines(Canvas canvas, Paint paint) {
		paint.setColor(AppFuncConstants.WHITE_LINE);
		if (mUtils.isVertical()) {
			// 在左边画一条白线，透明度25%
			// 在底边画一条线，高度3px
			// 最后在右边画一条黑线，透明度30%
			if (mId != AppFuncConstants.ALLAPPS) {
				// 第一个Tab不画左边的白线
				canvas.drawLine(0, 0, 0, mHeight, paint);
			}
			paint.setColor(AppFuncConstants.TAB_SELECT_BOTTOM);
			paint.setStrokeWidth(3.0f);
			canvas.drawLine(0, mHeight - 2, mWidth, mHeight - 2, paint);
			paint.setStrokeWidth(0);
			if (mId != AppFuncConstants.PROCESSMANAGEMENT) {
				// 最后一个Tab不画右边黑线
				paint.setColor(AppFuncConstants.BLACK_LINE);
				canvas.drawLine(mWidth - 1, 0, mWidth - 1, mHeight - 3, paint);
			}
		} else {
			// 在上边画一条白线，透明度25%
			// 在右边画一条线，宽度3px
			// 最后在底边画一条黑线，透明度30%
			if (mId != AppFuncConstants.ALLAPPS) {
				// 第一个Tab不画上边的白线
				canvas.drawLine(0, 0, mWidth, 0, paint);
			}
			paint.setColor(AppFuncConstants.TAB_SELECT_BOTTOM);
			paint.setStrokeWidth(3.0f);
			canvas.drawLine(mWidth - 2, 0, mWidth - 2, mHeight, paint);
			paint.setStrokeWidth(0);
			if (mId != AppFuncConstants.PROCESSMANAGEMENT) {
				// 最后一个Tab不画底边黑线
				paint.setColor(AppFuncConstants.BLACK_LINE);
				canvas.drawLine(0, mHeight - 1, mWidth - 3, mHeight - 1, paint);
			}
		}
	}

	/**
	 * 重新布局
	 */
	@Override
	public void layout(int left, int top, int right, int bottom) {
		mX = left;
		mY = top;
		if (mWidth == 0) {
			mWidth = right - left;
		}
		if (mHeight == 0) {
			mHeight = bottom - top;
		}
		setIsPressed(false);
		// / setSelected(false);
		// setFocused(false);
		mFocusedShader = new LinearGradient(0, 0, 0, mHeight, AppFuncConstants.TAB_FOCUS_TOP,
				AppFuncConstants.TAB_FOCUS_BOTTOM, Shader.TileMode.REPEAT);
		mSelectedShader = new LinearGradient(0, 0, 0, mHeight, AppFuncConstants.TAB_SELECT_TOP,
				AppFuncConstants.TAB_SELECT_BOTTOM, Shader.TileMode.REPEAT);

		// setSize(width, mHeight);
		// 根据TabWidget的宽高设置显示的字体
		mShowString = mUtils.cutString(mTabName, mWidth - 10, mPaint);
		// 重新获取图片资源
		// getTabImages();

		// super.layout(left, top, right, bottom)
	}

	/**
	 * 获取公共图片资源
	 */
	protected void getTabImages() {

		String curPackageName = ThemeManager.getInstance(mActivity).getCurThemePackage();
		String packageName = null;
		// 如果设置项里的分隔和当前主题不同就取设置项中指定package里的资源
		if (!curPackageName.equals(GOLauncherApp.getSettingControler().getFunAppSetting()
				.getTabHomeBgSetting())) {
			packageName = GOLauncherApp.getSettingControler().getFunAppSetting()
					.getTabHomeBgSetting();
		} else {
			packageName = curPackageName;
		}
		try {
			mIconUnselected = (BitmapDrawable) mThemeCtrl
					.getDrawable(
							mThemeCtrl.getThemeBean().mTabIconBeanMap.get(getTabBeanNameById()).mTabIconUnSelected,
							packageName);

			mIconSelected = (BitmapDrawable) mThemeCtrl
					.getDrawable(
							mThemeCtrl.getThemeBean().mTabIconBeanMap.get(getTabBeanNameById()).mTabIconSelected,
							packageName);

			mIconCurrent = (BitmapDrawable) mThemeCtrl
					.getDrawable(
							mThemeCtrl.getThemeBean().mTabIconBeanMap.get(getTabBeanNameById()).mTabIconCurrent,
							packageName);
		} catch (Exception e) {
			Loger.i("AppFuncTabSingleTitle.getTabImages()", "err theme exception!");
		}

		if (mHasGottenImages == false) {
			if (GOLauncherApp.getSettingControler().getFunAppSetting().getTabHomeBgSetting()
					.equals(ThemeManager.DEFAULT_THEME_PACKAGE)) {
				mIconFocused_v = mThemeCtrl.getDrawable(String.valueOf(R.drawable.tab_bg_focused));
			} else {
				mIconFocused_v = mThemeCtrl.getDrawable(
						mThemeCtrl.getThemeBean().mTabBean.mTabFocusedBottomVerPath, packageName);
			}
			mIconBg_v = mThemeCtrl.getDrawable(
					mThemeCtrl.getThemeBean().mTabBean.mTabBgBottomVerPath, packageName);
			mIconSelected_v = mThemeCtrl.getDrawable(
					mThemeCtrl.getThemeBean().mTabBean.mTabSelectedBottomVerPath, packageName);
			sTitleGap_v = mUtils
					.getStandardSize(mThemeCtrl.getThemeBean().mTabTitleBean.mTabTitleGapVer);
			mIconBg_h = mThemeCtrl.getDrawable(
					mThemeCtrl.getThemeBean().mTabBean.mTabBgBottomHorPath, packageName);
			mIconSelected_h = mThemeCtrl.getDrawable(
					mThemeCtrl.getThemeBean().mTabBean.mTabSelectedBottomHorPath, packageName);
			mIconFocused_h = mThemeCtrl.getDrawable(
					mThemeCtrl.getThemeBean().mTabBean.mTabFocusedBottomHorPath, packageName);
			sTitleGap_h = mUtils
					.getStandardSize(mThemeCtrl.getThemeBean().mTabTitleBean.mTabTitleGapHor);
			mIconBgDrawingWay = mThemeCtrl.getThemeBean().mTabBean.mTabBgDrawingWay;
			mIconSelectedDrawingWay = mThemeCtrl.getThemeBean().mTabBean.mTabSelectedDrawingWay;
			mIconFocusedDrawingWay = mThemeCtrl.getThemeBean().mTabBean.mTabFocusedDrawingWay;
			mDrawLines = mThemeCtrl.getThemeBean().mTabBean.mTabCutLineEnabled;
			mEnableOritentation = mThemeCtrl.getThemeBean().mTabBean.mTabOrientationEnabled;
			mHasGottenImages = true;
		}
		// }
	}

	//	public static void destroyStaticImages() {
	//		recycleStaticImage(sIconBg_v);
	//		recycleStaticImage(sIconSelected_v);
	//		recycleStaticImage(sIconFocused_v);
	//		recycleStaticImage(sIconBg_h);
	//		recycleStaticImage(sIconSelected_h);
	//		recycleStaticImage(sIconFocused_h);
	//	}

	//	private static void recycleStaticImage(Drawable drawable) {
	//		if ((drawable != null) && (drawable instanceof BitmapDrawable)) {
	//			BitmapDrawable map = ( BitmapDrawable ) drawable;
	//			if (map.getBitmap().isRecycled() == false) {
	//				map.getBitmap().recycle();
	//				drawable = null;
	//			}
	//		}
	//	}

	@Override
	public void notify(int key, Object obj) {
		switch (key) {
			case AppFuncConstants.THEME_CHANGE : {
				mIconUnselected = null;
				mIconSelected = null;
				mIconCurrent = null;
				mCurrentImg = null;
				mIconBg_v = null;
				mIconBg_h = null;
				mIconSelected_v = null;
				mIconSelected_h = null;
				mIconFocused_v = null;
				mIconFocused_h = null;
				if (mHasGottenImages) {
					mHasGottenImages = false;
				}

				break;
			}
			case AppFuncConstants.LOADTHEMERES : {
				getTabImages();
				setmNoShadowPaint();
				setmShadowPaint();
				break;
			}
			case AppFuncConstants.RELOADTABHOMETHEMERES : {
				if (mHasGottenImages) {
					mHasGottenImages = false;
				}
				break;
			}
		}
	}

	/**
	 * 根据Tab标识得到对应的TabThemeBean中的标识
	 * 
	 * @param id
	 * @return
	 */
	private String getTabBeanNameById() {
		if (mId == AppFuncConstants.ALLAPPS) {
			return AppFuncThemeBean.ALLAPPS_TAB_NAME;
		} else if (mId == AppFuncConstants.RECENTAPPS) {
			return AppFuncThemeBean.RECENTAPPS_TAB_NAME;
		} else // (mId == AppFuncConstants.PROCESSMANAGEMENT)
		{
			return AppFuncThemeBean.PROCESS_TAB_NAME;
		}
	}

	@Override
	public boolean onKey(KeyEvent event) {
		boolean onKey = super.onKey(event);
		if (mIsFocused
				&& event.getAction() == KeyEvent.ACTION_UP
				&& (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
			if (mEventListener != null) {
				mEventListener.onEventFired(this, EventType.CLICKEVENT, event, 0, 0);
				onKey = true;
			}
		}
		return onKey;
	}

	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case IDiyMsgIds.APPCORE_DATACHANGE :
				if (DataType.DATATYPE_DESKFONTCHANGED == param) {
					if (object instanceof FontBean) {
						FontBean bean = (FontBean) object;
						initTypeface(bean.mFontTypeface, bean.mFontStyle);
					}
				}
				break;
			default :
				break;
		}
	}
}
