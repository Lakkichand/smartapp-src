/**
 * 文字和背景的按钮，（背景图必须是9切图，用于确定文字的x轴开始位置）如果有文字的话。如果没有文字则只画背景
 */
package com.jiubang.ggheart.apps.appfunc.component;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.view.MotionEvent;

import com.go.util.graphics.ImageUtil;
import com.jiubang.core.mars.XComponent;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncConstants;
import com.jiubang.ggheart.apps.desks.appfunc.help.AppFuncUtils;

/**
 * 
 * @author 
 * @version 
 * 图片型按钮类
 */
public class AppFuncImageButton extends XComponent {

	protected Activity mActivity = null;
	protected Drawable mIconPressed = null;
	protected Drawable mIcon = null;
	private Drawable mChildIcon = null;
	private Rect mIconR = new Rect();
	private Rect mChildIconR = new Rect();

	private boolean mRepaint = false;

	private OnClickListener mClickListener = null;
	private AppFuncUtils mUtils = null;
	/**
	 * 背景图到组件边缘的距离
	 */
	private int mHPadding = 0;
	private int mVPadding = 0;

	private String mText = "";

	private Paint mTxtPaint = null;
	private Paint mDrawablePaint = null;
	private int mTextSize = 12;

	private int mTextColor = 0xffffffff;
	/**
	 * 文字绘制的开始位置
	 */
	private float mTextStartX = 0f;
	private float mTextStartY = 0f;
	/**
	 * 文字到边缘的距离
	 */
	private int mTextPaddingLeft = 0;
	private int mTextPaddingRight = 0;
	private int mTextPaddingV = 0;

	private boolean mTextAlignCenter = false;

	private boolean mEnable = true;

	public AppFuncImageButton(Activity activity, int tickCount, int x, int y, int width, int height) {
		super(tickCount, x, y, width, height);
		mActivity = activity;
		mUtils = AppFuncUtils.getInstance(mActivity);
		mTextSize = mUtils.getStandardSize(mTextSize);
		mTxtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTxtPaint.setTextSize(mTextSize);
		mTxtPaint.setColor(mTextColor);
		mDrawablePaint = new Paint();
	}

	@Override
	public void layout(int i, int j, int k, int l) {
		super.layout(i, j, k, l);
		mIconR.set(mHPadding, mVPadding, mWidth - mHPadding, mHeight - mVPadding);
		if (mIcon != null) {
			mIcon.setBounds(mIconR);
		}
		if (mIconPressed != null) {
			mIconPressed.setBounds(mIconR);
		}

		if (mChildIcon != null) {
			int iconw = mIconR.bottom - mIconR.top;
			mChildIconR.set(mIconR.left, mIconR.top, mIconR.left + iconw, mIconR.top + iconw);
			mChildIcon.setBounds(mChildIconR);
		}

		if (mText != null && !"".equals(mText)) {
			if (mTextSize > mHeight) {
				setTextSize(mHeight);
			}
			calculateTextPaddingV();
			calculateTextStartPosition();
		}

		if (mIsPressed) {
			mIsPressed = false;
		}
	}

	@Override
	protected void drawCurrentFrame(Canvas canvas) {
		if (mIsPressed) {
			if (mIconPressed != null) {
				ImageUtil.drawImage(canvas, mIconPressed, ImageUtil.CENTERMODE, mHPadding,
						mVPadding, mWidth - mHPadding, mHeight - mVPadding, mDrawablePaint);
			} else if (mIcon != null) {
				ImageUtil.drawImage(canvas, mIcon, ImageUtil.CENTERMODE, mHPadding, mVPadding,
						mWidth - mHPadding, mHeight - mVPadding, mDrawablePaint);
			}
		} else {
			if (mIcon != null) {
				ImageUtil.drawImage(canvas, mIcon, ImageUtil.CENTERMODE, mHPadding, mVPadding,
						mWidth - mHPadding, mHeight - mVPadding, mDrawablePaint);
			} else if (mIconPressed != null) {
				ImageUtil.drawImage(canvas, mIconPressed, ImageUtil.CENTERMODE, mHPadding,
						mVPadding, mWidth - mHPadding, mHeight - mVPadding, mDrawablePaint);
			}
		}

		if (mChildIcon != null) {
			// mChildIcon.draw(canvas);
			Rect rect = mChildIcon.getBounds();
			ImageUtil.drawImage(canvas, mChildIcon, ImageUtil.CENTERMODE, rect.left, rect.top,
					rect.right, rect.bottom, mDrawablePaint);
		}

		// 绘制文字
		if (mText != null && !"".equals(mText)) {
			canvas.drawText(mText, mTextStartX, mTextStartY, mTxtPaint);
		}
	}

	@Override
	protected boolean animate() {
		if (mRepaint) {
			mRepaint = false;
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @author 
	 * @version 
	 * 点击事件监听器
	 */
	public static interface OnClickListener {
		/**
		 * 点击事件时触发
		 * 
		 * @param view
		 */
		public void onClick(XComponent view);
	}

	public Drawable getIconPressed() {
		return mIconPressed;
	}

	public void setIconPressed(Drawable iconPressed) {
		this.mIconPressed = iconPressed;
	}

	public Drawable getIcon() {
		return mIcon;
	}

	public void setIcon(Drawable icon) {
		this.mIcon = icon;
	}

	public OnClickListener getClickListener() {
		return mClickListener;
	}

	public void setChildIcon(Drawable child) {
		mChildIcon = child;
	}

	public void setClickListener(OnClickListener clickListener) {
		this.mClickListener = clickListener;
	}

	@Override
	public boolean onTouch(MotionEvent e) {
		if (!mEnable) {
			return false;
		}
		switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN : {

				mIsPressed = true;
				break;
			}
			case MotionEvent.ACTION_CANCEL : {
				mIsPressed = false;
				break;
			}
			case MotionEvent.ACTION_UP : {
				mIsPressed = false;
				if (XYInRange((int) e.getX(), (int) e.getY()) && mClickListener != null) {
					mClickListener.onClick(this);
				}
				break;
			}
			default : {

			}
		}

		return true;
	}

	public void setText(String text) {
		if (text == null) {
			mText = "";
			return;
		}
		mText = text;
	}

	/**
	 * 计算垂直方向的边距
	 */
	private void calculateTextPaddingV() {
		int padding = (mHeight - mTextSize) / 2;
		mTextPaddingV = mUtils.getStandardSize(padding);
	}

	public void setTextSize(int size) {
		mTextSize = mUtils.getStandardSize(size);
		mTxtPaint.setTextSize(mTextSize);
	}

	/**
	 * 计算文字的开始位置和结束位置(layout的时候调用)
	 */
	private void calculateTextStartPosition() {

		if (mTextAlignCenter) {
			mTextStartX = (mWidth - mTxtPaint.measureText(mText)) / 2;
		} else {
			if (mChildIcon != null) {
				int padding = mChildIconR.right;
				mTextStartX = padding;
			} else {
				int padding = getIconLeftPadding();
				mTextStartX = mHPadding + padding + mTextPaddingLeft / 2;
			}
		}

		FontMetrics fm = mTxtPaint.getFontMetrics();
		mTextStartY = mHeight - mTextPaddingV - fm.bottom / 2;
	}

	/**
	 * 根据文字计算整个控件的宽度，包括空白区域，背景图本身的x轴padding
	 * 
	 * @param text
	 * @return 按钮需要的宽度
	 */
	public int calculateWidthByText(String text, int iconW) {
		if (text == null) {
			text = "";
		}
		int padding_left = 0;
		if (mChildIcon != null) {
			padding_left = iconW;
		} else {
			padding_left = getIconLeftPadding();
		}

		float text_width = mTxtPaint.measureText(text);

		return Math.round(mHPadding + padding_left + mTextPaddingLeft + mTextPaddingRight
				+ text_width);
	}

	/**
	 * 获取背景图设置的左边padding值
	 * 
	 * @return
	 */
	private int getIconLeftPadding() {
		int padding_left = 0;
		// 考虑9切图的padding设置
		if (mIcon != null && mIcon instanceof NinePatchDrawable) {
			Rect rt = new Rect();
			mIcon.getPadding(rt);
			padding_left = rt.left;
		}

		return padding_left;
	}

	public void setTextPaddingLeft(int padding) {
		mTextPaddingLeft = mUtils.getStandardSize(padding);
	}

	public void setTextPaddingRight(int padding) {
		mTextPaddingRight = mUtils.getStandardSize(padding);
	}

	public void setTextColor(int color) {
		mTextColor = color;
		mTxtPaint.setColor(mTextColor);
	}

	public void setTextAlignCenter(boolean align) {
		mTextAlignCenter = align;
	}

	/**
	 * 设置图片的水平边距
	 * 
	 * @param hPadding
	 */
	public void setImageHPadding(int hPadding) {
		mHPadding = hPadding;
	}

	/**
	 * 设置图片的垂直边距
	 * 
	 * @param vPadding
	 */
	public void setImageVPadding(int vPadding) {
		mVPadding = vPadding;
	}

	// @Override
	// public boolean XYInRange(int x, int y) {
	// if ((x >= getAbsX() - 5) && (x <= getAbsX() + mWidth + 5)) {
	// if ((y >= getAbsY() - 5) && (y <= getAbsY() + mHeight + 5)) {
	// return true;
	// }
	// }
	// return false;
	// }
	/**
	 * 当组件退出显示时，由框架调用
	 */
	@Override
	public void close() {
		super.close();
		if (mIsPressed) {
			mIsPressed = false;
		}
	}

	public void setEnable(boolean enable) {
		if (mEnable != enable) {
			mEnable = enable;
			if (enable) {
				mTxtPaint.setAlpha(255);
				mDrawablePaint.setAlpha(255);
			} else {
				mTxtPaint.setAlpha(AppFuncConstants.DISABLED_COMPONENT_ALPHA);
				mDrawablePaint.setAlpha(AppFuncConstants.DISABLED_COMPONENT_ALPHA);
				invalidate();
			}
		}
	}
}
