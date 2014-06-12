package com.jiubang.ggheart.apps.appfunc.component;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.go.util.graphics.ImageUtil;
import com.jiubang.core.mars.EventType;
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
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * 暂时专用于“最近打开”清楚所有记录的按钮
 * 
 */
public class MButton extends AppFuncDockContent implements IMsgHandler, BroadCasterObserver {
	/**
	 * 水平
	 */
	public static final byte HORIZONTAL = 2;
	/**
	 * 竖直
	 */
	public static final byte VERTICAL = 1;

	/**
	 * 按钮上的字
	 */
	protected String mTitle;

	protected Context mContext;

	protected byte mOrientation = VERTICAL;
	protected Drawable mButtonBg = null;
	protected Drawable mButtonHPop = null;
	protected Drawable mButtonHPush = null;
	protected Drawable mButtonVPush;
	protected Drawable mButtonVPop;

	// 画笔
	protected Paint mPaint;
	// 文字宽度
	protected int mTextWidth;
	// 文字高度
	protected int mTextHeight;
	// 文字位置
	protected int mTextXPos;
	protected int mTextYPos;
	/**
	 * 选中时的绘图方式
	 */
	protected byte mSelectedDrawingWay;
	/**
	 * 未选中时的绘图方式
	 */
	protected byte mUnselectedDrawingWay;

	private AppFuncThemeController mThemeController;

	public MButton(Context context, int tickCount, int x, int y, int width, int height) {
		super(context, tickCount, x, y, width, height, HIDE_TYPE_BOTTOM);
		mContext = context;
		mPaint = new Paint();
		mPaint.setColor(Color.WHITE);
		mPaint.setAntiAlias(true);
		mThemeController = AppFuncFrame.getThemeController();
		// 注册主题改变事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.THEME_CHANGE,
				this);
		// 注册加载主题资源事件
		DeliverMsgManager.getInstance().registerDispenseMsgHandler(AppFuncConstants.LOADTHEMERES,
				this);
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

	protected void setTypeface(Typeface tf, int style) {
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

	protected void setTypeface(Typeface tf) {
		if (mPaint.getTypeface() != tf) {
			mPaint.setTypeface(tf);
		}
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		AppFuncUtils instance = AppFuncUtils.getInstance(mContext);
		int mTabBgDrawingWay = -1;
		if (mIsPressed) {
			mTabBgDrawingWay = mSelectedDrawingWay;
			if (instance.isVertical()) {
				if (mButtonVPush != null) {
					ImageUtil.drawImage(canvas, mButtonVPush, mTabBgDrawingWay, 0, 0, mWidth,
							mHeight, mPaint);
				} else {
					if (mButtonHPush != null) {
						canvas.save();
						canvas.rotate(-90.0f);
						canvas.translate(-mHeight, 0);
						int bgHeight = 0;
						if (mButtonBg != null) {
							bgHeight = mButtonBg.getIntrinsicHeight();
						}
						ImageUtil.drawImage(canvas, mButtonHPush, mTabBgDrawingWay, 0, mWidth
								- bgHeight, mHeight, mWidth, mPaint);
						canvas.restore();
					}
				}
			} else {
				if (mButtonHPush != null) {
					ImageUtil.drawImage(canvas, mButtonHPush, mTabBgDrawingWay, 0, 0, mWidth,
							mHeight, mPaint);
				} else {
					if (mButtonVPush != null) {
						canvas.save();
						canvas.rotate(-90.0f);
						canvas.translate(-mHeight, 0);
						ImageUtil.drawImage(canvas, mButtonVPush, mTabBgDrawingWay, 0, 0, mHeight,
								mWidth, mPaint);
						canvas.restore();
					}
				}
			}
		} else {
			mTabBgDrawingWay = mUnselectedDrawingWay;
			if (instance.isVertical()) {
				if (mButtonVPop != null) {
					ImageUtil.drawImage(canvas, mButtonVPop, mTabBgDrawingWay, 0, 0, mWidth,
							mHeight, mPaint);
				} else {
					if (mButtonHPop != null) {
						canvas.save();
						canvas.rotate(-90.0f);
						canvas.translate(-mHeight, 0);

						int bgHeight = 0;
						if (mButtonBg != null) {
							bgHeight = mButtonBg.getIntrinsicHeight();
						}
						ImageUtil.drawImage(canvas, mButtonHPop, mTabBgDrawingWay, 0, mWidth
								- bgHeight, mHeight, mWidth, mPaint);
						canvas.restore();
					}
				}
			} else {
				if (mButtonHPop != null) {
					ImageUtil.drawImage(canvas, mButtonHPop, mTabBgDrawingWay, 0, 0, mWidth,
							mHeight, mPaint);
				} else {
					if (mButtonVPush != null) {
						canvas.save();
						canvas.rotate(-90.0f);
						canvas.translate(-mHeight, 0);
						ImageUtil.drawImage(canvas, mButtonVPop, mTabBgDrawingWay, 0, 0, mHeight,
								mWidth, mPaint);
						canvas.restore();
					}
				}
			}
		}

		if (null == mTitle) {
			return;
		}

		if (VERTICAL == mOrientation) {
			canvas.drawText(mTitle, mTextXPos, mTextYPos, mPaint);
		} else {
			canvas.save();
			canvas.rotate(-90);
			canvas.translate(-mHeight, 0);
			canvas.drawText(mTitle, mTextXPos, mTextYPos, mPaint);
			canvas.restore();
		}
	}

	@Override
	protected boolean animate() {
		return false;
	}

	public void setText(String title) {
		mTitle = title;
	}

	public void setOrientation(byte orientation) {
		this.mOrientation = orientation;
		if (HORIZONTAL == orientation) { // 如果是水平的方式
			mButtonBg = mButtonHPop;
		} else if (VERTICAL == orientation) {
			mButtonBg = mButtonVPop;
		}
	}

	/**
	 * 设置大小
	 * 
	 * @param width
	 *            宽度
	 * @param height
	 *            高度
	 */
	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		if (mButtonHPush != null) {
			mButtonHPush.setBounds(0, 0, width, height);
		}

		if (mButtonHPop != null) {
			mButtonHPop.setBounds(0, 0, width, height);
		}

		reCaculatTextSize();
	}

	protected void reCaculatTextSize() {
		if (mPaint != null && mTitle != null) {
			if (VERTICAL == mOrientation) {
				mTextHeight = mHeight / 3;
			} else {
				mTextHeight = mWidth / 3;
			}
			mPaint.setTextSize(mTextHeight);
			mTextWidth = (int) mPaint.measureText(mTitle);

			if (VERTICAL == mOrientation) {
				mTextXPos = (mWidth - mTextWidth) / 2;
				mTextYPos = (mHeight + mTextHeight) / 2;
			} else {
				mTextXPos = (mHeight - mTextWidth) / 2;
				mTextYPos = (mWidth + mTextHeight) / 2;
			}
		}
	}

	@Override
	public void notify(int key, Object obj) {
		switch (key) {
			case AppFuncConstants.THEME_CHANGE : {
				mButtonHPop = null;
				mButtonHPush = null;
				mButtonBg = null;
			}
				break;
			case AppFuncConstants.LOADTHEMERES : {
				loadResource();
			}
				break;
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

	private void loadResource() {
		mButtonHPush = mThemeController
				.getDrawable(mThemeController.getThemeBean().mClearHistoryBean.mClearHistoryBottomSelectedHorPath);
		mButtonHPop = mThemeController
				.getDrawable(mThemeController.getThemeBean().mClearHistoryBean.mClearHistoryBottomUnselectedHorPath);
		mButtonVPush = mThemeController
				.getDrawable(mThemeController.getThemeBean().mClearHistoryBean.mClearHistoryBottomSelectedVerPath);
		mButtonVPop = mThemeController
				.getDrawable(mThemeController.getThemeBean().mClearHistoryBean.mClearHistoryBottomUnselectedVerPath);
		mSelectedDrawingWay = mThemeController.getThemeBean().mClearHistoryBean.mClearHistorySelectedDrawingWay;
		mUnselectedDrawingWay = mThemeController.getThemeBean().mClearHistoryBean.mClearHistoryUnselectedDrawingWay;
		// 字体颜色
		mPaint.setColor(mThemeController.getThemeBean().mClearHistoryBean.mClearHistoryTextColor);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onBCChange(int msgId, int param, Object object, List objects) {
		switch (msgId) {
			case IDiyMsgIds.APPCORE_DATACHANGE :
				if (DataType.DATATYPE_DESKFONTCHANGED == param) {
					if (object instanceof FontBean) {
						FontBean bean = (FontBean) object;
						initTypeface(bean.mFontTypeface, bean.mFontStyle);
						reCaculatTextSize();
					}
				}
				break;
			default :
				break;
		}
	}

	@Override
	public boolean onTouch(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mIsPressed = true;
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			mIsPressed = false;
			byte eventType = EventType.CLICKEVENT;
			mEventListener.onEventFired(this, eventType, event, 0, null);
			return true;
		}
		return false;
	}

	@Override
	public void resetResource() {
		mButtonHPop = null;
		mButtonHPush = null;
		mButtonBg = null;
	}

	@Override
	public void loadResource(String packageName) {
		mButtonHPush = mThemeController
				.getDrawable(mThemeController.getThemeBean().mClearHistoryBean.mClearHistoryBottomSelectedHorPath);
		mButtonHPop = mThemeController
				.getDrawable(mThemeController.getThemeBean().mClearHistoryBean.mClearHistoryBottomUnselectedHorPath);
		mButtonVPush = mThemeController
				.getDrawable(mThemeController.getThemeBean().mClearHistoryBean.mClearHistoryBottomSelectedVerPath);
		mButtonVPop = mThemeController
				.getDrawable(mThemeController.getThemeBean().mClearHistoryBean.mClearHistoryBottomUnselectedVerPath);
		mSelectedDrawingWay = mThemeController.getThemeBean().mClearHistoryBean.mClearHistorySelectedDrawingWay;
		mUnselectedDrawingWay = mThemeController.getThemeBean().mClearHistoryBean.mClearHistoryUnselectedDrawingWay;
		// 字体颜色
		mPaint.setColor(mThemeController.getThemeBean().mClearHistoryBean.mClearHistoryTextColor);
	}

}
