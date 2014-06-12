package com.jiubang.ggheart.apps.appfunc.component;

import java.util.List;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.gau.go.launcherex.R;
import com.jiubang.core.mars.EventType;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;
import com.jiubang.ggheart.apps.font.FontBean;
import com.jiubang.ggheart.data.BroadCaster.BroadCasterObserver;
import com.jiubang.ggheart.data.DataType;
import com.jiubang.ggheart.data.GoSettingControler;
import com.jiubang.ggheart.launcher.GOLauncherApp;

public class XText extends XComponent implements BroadCasterObserver {
	public static final int HCENTER = 1;
	public static final int VCENTER = 2;
	public static final int LEFT = 4;
	public static final int RIGHT = 8;
	public static final int TOP = 16;
	public static final int BOTTOM = 32;
	public static final int BASELINE = 64;

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
	private int mTextAlign;
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
	/**
	 * 字体改变时是否重构文字
	 */
	private boolean mReBuildText = false;

	public XText(int tickCount, int x, int y, int width, int height, Activity activity) {
		super(tickCount, x, y, width, height);
		mActivity = activity;
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mFontMetrics = mPaint.getFontMetrics();
		mTextHeight = (int) (mFontMetrics.bottom - mFontMetrics.top);
		mText = "";
		mShowText = "";
		mIsEditable = true;
		mPaint.setColor(0xFF000000);
	}

	@Override
	protected void onShow() {
		super.onShow();
		GoSettingControler controler = GOLauncherApp.getSettingControler();
		controler.registerObserver(this);
		initTypeface(controler.getUsedFontBean().mFontTypeface,
				controler.getUsedFontBean().mFontStyle);
	}

	public void setTxtColor(int color) {
		if (mPaint != null) {
			mPaint.setColor(color);
		}
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
	protected void drawCurrentFrame(Canvas canvas) {
		if (mBgImage != null) {
			mBgImage.draw(canvas);
		}
		canvas.drawText(mShowText, mTextX, mTextY, mPaint);
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
		if (this.mTextSize != textSize) {
			mReBuildText = true;// 字体大小有变化，需要重构文本信息
			this.mTextSize = textSize;
			mPaint.setTextSize(textSize);
			mFontMetrics = mPaint.getFontMetrics();
			mTextHeight = (int) (mFontMetrics.bottom - mFontMetrics.top);
			// mTextHeight = (int)mTextSize;
			// 默认距离
			mTextX = 10;
			mTextY = (int) (mHeight - mFontMetrics.descent);
		}
	}

	public String getText() {
		return mText;
	}

	public void setText(String text) {
		if (!mText.equals(text) || mReBuildText) {
			mReBuildText = false;
			text = text.replaceAll("\\s+", " ");
			mText = text;
			mShowText = AppFuncUtils.getInstance(mActivity).cutString(mText, mWidth - 10, mPaint);
		}
		// 防御性保护
		if (mShowText == null) {
			// 目前只有文件夹在使用此类，因此暂时在此命名为文件夹
			mShowText = mActivity.getResources().getString(R.string.folder_name);
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

	private void update(int align) {
		switch (align) {
			case LEFT | TOP :
				// mTextY = (int) (mHeight - (mHeight - mTextHeight -
				// mFontMetrics.descent));
			case LEFT | VCENTER :
				mTextY = (int) (mHeight / 2 + (mTextHeight - mFontMetrics.descent * 2) / 2);
			case LEFT | BOTTOM :
				mPaint.setTextAlign(Paint.Align.LEFT);
				break;
			case RIGHT | TOP :
				// mTextY = (int) (mHeight - (mHeight - mTextHeight -
				// mFontMetrics.descent));
			case RIGHT | VCENTER :
				mTextY = (int) (mHeight / 2 + (mTextHeight - mFontMetrics.descent * 2) / 2);
			case RIGHT | BOTTOM :
				mPaint.setTextAlign(Paint.Align.RIGHT);
				break;
			case HCENTER | TOP :
				// mTextY = (int) (mHeight - (mHeight - mTextHeight -
				// mFontMetrics.descent));
			case HCENTER | VCENTER :
				mTextY = (int) (mHeight / 2 + (mTextHeight - mFontMetrics.descent * 2) / 2);
			case HCENTER | BOTTOM :
				mPaint.setTextAlign(Paint.Align.CENTER);
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
		update(mTextAlign);
	}

	public boolean isEditable() {
		return mIsEditable;
	}

	public void setEditable(boolean mIsEditable) {
		this.mIsEditable = mIsEditable;
	}

	public void setTextXY(int x, int y) {
		this.mTextX = x;
		this.mTextY = y;
	}

	@Override
	public boolean onTouch(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN : {
				if (XYInRange((int) event.getX(), (int) event.getY()) && mIsEditable) {
					mIsSelected = true;
					return true;
				}
			}
				break;
			case MotionEvent.ACTION_UP : {
				if (mIsSelected) {
					if (XYInRange((int) event.getX(), (int) event.getY()) && mIsEditable) {
						if (mEventListener != null) {
							// 通知Folder弹出重命名框
							mEventListener.onEventFired(this, EventType.SHOW_EDITDIALOG, mText, 0,
									null);
						}
					}
					mIsSelected = false;
					return true;
				}
			}
		}
		return false;
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
