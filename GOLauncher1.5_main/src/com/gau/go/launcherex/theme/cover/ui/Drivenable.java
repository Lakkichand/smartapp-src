package com.gau.go.launcherex.theme.cover.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * 
 * <br>类描述:可被驱动的
 * <br>功能详细描述:
 * 
 */
public abstract class Drivenable implements IDrawable, ICleanable {

	public static final int TOUCH_STATE_IN_NORMAL = 0;
	public static final int TOUCH_STATE_IN_CLICK = 1;
	public volatile int mTouchState = TOUCH_STATE_IN_NORMAL;
	public float mSpeedY;
	public float mSpeedX;
	public float mX;
	public float mY;
	public int mAngle;
	public float mPivotX;
	public float mPivotY;
	public int mDefaultAngle;
	public Bitmap mBitmap;
	public Bitmap mShadow;
	public Rect mLimitRect;
	public int mActionOneType;
	public int mActionTwoType;
	protected volatile int mCurrentActionIndex;
	
	public float mScale = 1.0f;
	public boolean mIsBaseDraw = true;
	public Context mContext;
	
	public abstract boolean checkOutOfBound();
	
	protected void recycle(Bitmap[] bitmaps) {
		if (bitmaps != null) {
			for (Bitmap bitmap : bitmaps) {
				recycle(bitmap);
			}
		}
	}

	/**
	 * <br>功能简述:不销毁bitmap，因为bitmap属于共用，在{@link BitmapLoader}统一处理，先置为null,消除引用
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param bitmap
	 */
	protected void recycle(Bitmap bitmap) {
		if (bitmap != null) {
			bitmap = null;
		}
	}
	
	
}
