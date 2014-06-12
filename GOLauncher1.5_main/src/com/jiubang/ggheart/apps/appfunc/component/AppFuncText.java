package com.jiubang.ggheart.apps.appfunc.component;

import java.util.List;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.DataType;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.launcher.GOLauncherApp;

/**
 * 
 * @author 
 * @version 
 * 文字控件
 */
public class AppFuncText extends XComponent implements BroadCasterObserver {
	public static final int HCENTER_TOP = 1;
	public static final int HCENTER_VCENTER = 2;
	public static final int HCENTER_BOTTOM = 3;
	public static final int LEFT_TOP = 4;
	public static final int LEFT_VCENTER = 5;
	public static final int LEFT_BOTTOM = 6;
	public static final int RIGHT_TOP = 7;
	public static final int RIGHT_VCENTER = 8;
	public static final int RIGHT_BOTTOM = 9;

	/**
	 * 字体Size
	 */
	private float mTextSize;
	/**
	 * 文本内容
	 */
	private String mText;
	/**
	 * 显示的文本
	 */
	private String mShowText;
	/**
	 * 画笔参数
	 */
	private Paint mPaint;
	/**
	 * 背景
	 */
	private Drawable mBgImage;
	/**
	 * 字体方向
	 */
	private int mTextAlign = LEFT_VCENTER;
	/**
	 * 字体类型
	 */
	private FontMetrics mFontMetrics;
	/**
	 * 字体高度
	 */
	private int mTextHeight;
	/**
	 * 字体X坐标
	 */
	private int mTextY;
	/**
	 * 字体Y坐标
	 */
	private float mTextX;
	/**
	 * 是否可编辑
	 **/
	private boolean mIsEditable;
	/**
	 * 是否需要重绘
	 */
	private boolean mIsRepaint;

	private Activity mActivity;
	private boolean mHideText;
	/**
	 * 是否显示阴影
	 */
	private boolean mShowShadow = true;

	private int mShadowColor = 0xcc000000;
	/**
	 * 阴影最大透明值
	 */
	private final int mMaxShadowColor = 0xcc000000;

	private int mAlpha = 255;
	/**
	 * 是否显示点击效果
	 */
	private boolean mShowPressEffect = false;
	/**
	 * 当文字长度超出范围是否显示省略号
	 */
	private boolean mShowEllipsis = false;

	public AppFuncText(int tickCount, int x, int y, int width, int height, Activity activity) {
		super(tickCount, x, y, width, height);
		mActivity = activity;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mFontMetrics = mPaint.getFontMetrics();
		mTextHeight = (int) (mFontMetrics.bottom - mFontMetrics.top);
		mText = "";
		mShowText = "";
		mIsEditable = true;
		mPaint.setColor(0xffffffff);
	}

	@Override
	protected void onShow() {
		super.onShow();
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		// controler.registerObserver(this);
		initTypeface(controler.getUsedFontBean().mFontTypeface,
				controler.getUsedFontBean().mFontStyle);
	}

	/**
	 * 点击时是否显示阴影效果
	 * 
	 * @param show
	 */
	public void setShowPressEffect(boolean show) {
		mShowPressEffect = show;
	}

	public void setTextColor(int color) {
		if (mPaint != null) {
			mPaint.setColor(color);
		}
	}

	@Override
	protected void onHide() {
		super.onHide();
		// GOLauncherApp.getSettingControler().unRegisterObserver(this);
		setIsPressed(false);
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
	protected void drawCurrentFrame(Canvas canvas) {
		if (mBgImage != null) {
			mBgImage.draw(canvas);
		}

		if (mIsPressed) {
			if (mShowPressEffect) {
				mPaint.setShadowLayer(8, 0, 0, 0xffffffff);
			} else if (mShowShadow) {
				mPaint.setShadowLayer(2, 0, 2, mShadowColor);
			} else {
				mPaint.clearShadowLayer();
			}
		} else {
			if (mShowShadow) {
				mPaint.setShadowLayer(2, 0, 2, mShadowColor);
			} else {
				mPaint.clearShadowLayer();
			}
		}

		if (!mHideText) {
			mPaint.setAlpha(mAlpha);
			canvas.drawText(mShowText, mTextX, mTextY, mPaint);
		}
	}

	@Override
	public void layout(int i, int j, int k, int l) {
		setPosition(i, j, k, l);
		mShowText = genShowText();
		updateTextSize();
		updateAlign(mTextAlign);

	}

	@Override
	protected boolean animate() {
		if (mIsRepaint) {
			mIsRepaint = false;
			return true;
		}
		return false;
	}

	public float getTextSize() {
		return mTextSize;
	}

	public void setTextSize(float textSize) {
		mTextSize = textSize;
		mPaint.setTextSize(mTextSize);
	}

	private void updateTextSize() {
		mFontMetrics = mPaint.getFontMetrics();
		mTextHeight = (int) (mFontMetrics.bottom - mFontMetrics.top);
		// // mTextHeight = (int)mTextSize;
		// // 默认距离
		// mTextX = 10;
		// mTextY = (int) (mHeight - mFontMetrics.descent);
	}

	public String getText() {
		return mText;
	}

	public void setText(String text) {
		if (text == null) {
			text = "";
		}
		if (!mText.equals(text)) {
			text = text.replaceAll("\\s+", " ");
			mText = text;
		}
		mIsRepaint = true;
	}

	public Drawable getBgImage() {
		return mBgImage;
	}

	public void setBgImage(Drawable bgImage) {
		mBgImage = bgImage;
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		if (mBgImage != null) {
			mBgImage.setBounds(0, 0, width, height);
		}
	}

	private void updateAlign(int align) {
		switch (align) {
			case LEFT_TOP :
				mTextY = (int) (mHeight - (mHeight - mTextHeight - mFontMetrics.descent));
				mTextX = 0;
				// mPaint.setTextAlign(Paint.Align.LEFT);
				break;
			case LEFT_VCENTER :
				mTextY = (int) (mHeight / 2 + (mTextHeight - mFontMetrics.descent * 2) / 2);
				mTextX = 0;
				// mPaint.setTextAlign(Paint.Align.LEFT);
				break;
			case LEFT_BOTTOM :
				mTextY = (int) (mHeight - mFontMetrics.descent * 2);
				mTextX = 0;
				// mPaint.setTextAlign(Paint.Align.LEFT);
				break;
			case RIGHT_TOP :
				mTextY = (int) (mHeight - (mHeight - mTextHeight - mFontMetrics.descent));
				mTextX = mWidth - mPaint.measureText(mShowText);
				// mPaint.setTextAlign(Paint.Align.RIGHT);
				break;
			case RIGHT_VCENTER :
				mTextY = (int) (mHeight / 2 + (mTextHeight - mFontMetrics.descent * 2) / 2);
				mTextX = mWidth - mPaint.measureText(mShowText);
				// mPaint.setTextAlign(Paint.Align.RIGHT);
				break;
			case RIGHT_BOTTOM :
				mTextY = (int) (mHeight - mFontMetrics.descent * 2);
				mTextX = mWidth - mPaint.measureText(mShowText);
				// mPaint.setTextAlign(Paint.Align.RIGHT);
				break;
			case HCENTER_TOP :
				mTextY = (int) (mHeight - (mHeight - mTextHeight - mFontMetrics.descent));
				mTextX = (mWidth - mPaint.measureText(mShowText)) / 2;
				// mPaint.setTextAlign(Paint.Align.CENTER);
				break;
			case HCENTER_VCENTER :
				mTextY = (int) (mHeight / 2 + (mTextHeight - mFontMetrics.descent * 2) / 2);
				mTextX = (mWidth - mPaint.measureText(mShowText)) / 2;
				// mPaint.setTextAlign(Paint.Align.CENTER);
				break;
			case HCENTER_BOTTOM :
				mTextY = (int) (mHeight - mFontMetrics.descent * 2);
				mTextX = (mWidth - mPaint.measureText(mShowText)) / 2;
				// mPaint.setTextAlign(Paint.Align.CENTER);
				break;
			default :
				throw new IllegalArgumentException();
		}
	}

	public int getTextAlign() {
		return mTextAlign;
	}

	public void setTextAlign(int mTextAlign) {
		this.mTextAlign = mTextAlign;
	}

	public boolean isEditable() {
		return mIsEditable;
	}

	public void setEditable(boolean editable) {
		this.mIsEditable = editable;
		if (mIsEditable) {
			mAlpha = 255;
		} else {
			mAlpha = AppFuncConstants.DISABLED_COMPONENT_ALPHA;
		}
		invalidate();
	}

	// public void setTextXY(int x, int y) {
	// this.mTextX = x;
	// this.mTextY = y;
	// }

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

	public void hideText(boolean hide) {
		mHideText = hide;
	}

	public void setAlpha(int alpha) {
		mAlpha = alpha;
		int temp = (int) (((mAlpha * 1.0f) / 255) * (mMaxShadowColor >>> 24));
		mShadowColor = (temp << 24 | 0x00ffffff) & mMaxShadowColor; // 改变阴影透明值
	}

	/**
	 * 是否显示文字阴影，默认显示
	 * 
	 * @param show
	 */
	public void setShowShadow(boolean show) {
		mShowShadow = show;
	}

	/**
	 * 文字阴影颜色，默认为白色
	 * 
	 * @param color
	 */
	public void setShowdowColor(int color) {
		mShadowColor = color;
	}

	private String genShowText() {
		if (mShowEllipsis) {
			float fullTextWidth = mPaint.measureText(mText);
			int textAreaWidth = mWidth;
			if (fullTextWidth > textAreaWidth) {
				// 加点点
				float ellipsisWidth = mPaint.measureText("...");
				return AppFuncUtils.getInstance(mActivity).cutString(mText,
						textAreaWidth - (int) ellipsisWidth, mPaint)
						+ "...";
			} else {
				return mText;
			}
		} else {
			return AppFuncUtils.getInstance(mActivity).cutString(mText, mWidth, mPaint);
		}
	}

	/**
	 * 设置当文字超过显示区域时是否显示。。。
	 * 
	 * @param show
	 */
	public void setShowEllipsis(boolean show) {
		mShowEllipsis = show;
	}
}
