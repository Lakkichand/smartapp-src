package com.gau.go.launcherex.theme.cover.ui;

import com.gau.go.launcherex.theme.cover.CoverBitmapLoader;

import android.graphics.Bitmap;
import android.graphics.Rect;

/**
 * 景物的基类
 * @author jiangxuwen
 *
 */
public abstract class BaseElement implements IDrawable, ICleanable {
	public Bitmap mBitmap; // 显示的位图
	public Bitmap mShadow; // 影子显示的位图
	public boolean mAllowDrag; // 是否可拖拽的标志位
	public float mScale; // 缩放比例
	public int mDefaultX; // 默认坐标x
	public int mDefaultY; // 默认坐标y
	public float mX; // 坐标x
	public float mY; // 坐标y
	public int mWidth; // 宽度
	public int mHeight; // 高度
	public Rect mLimitRect; // 限制的显示区域
	public int mAlpha = 255; // 透明度
	public boolean mAlive = true; // 是否存活（是否绘制的标识）

	public BaseElement() {
		mLimitRect = new Rect();
	}

	@Override
	public void cleanUp() {
		recycle(mBitmap);
		recycle(mShadow);
	}

	protected void recycle(Bitmap[] bitmaps) {
		if (bitmaps != null) {
			for (Bitmap bitmap : bitmaps) {
				recycle(bitmap);
			}
		}
	}

	/**
	 * <br>功能简述:不销毁bitmap，因为bitmap属于共用，在{@link CoverBitmapLoader}统一处理，先置为null,消除引用
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
