package com.jiubang.ggheart.apps.desks.share;

import android.graphics.Bitmap;
import android.graphics.drawable.NinePatchDrawable;

/**
 * 
 * <br>类描述: 一张卡片的信息
 * <br>功能详细描述:
 * 
 * @author  maxiaojun
 * @date  [2012-9-25]
 */
public class ShareItem {
	public boolean isSelect = false; // 是否选中
	public Bitmap bitmap; // 图片
	public int index = 0; // 第几个位置
	public int width;
	public int height;
	public int itemImgW;
	public int itemImgH;

	public Bitmap unSelImg;
	public Bitmap selImg;
	public NinePatchDrawable bgNine;
    /**
     * <默认构造函数>
     */
	public ShareItem(boolean isSelect, Bitmap img, int index, int width, int height, Bitmap selImg,
			Bitmap unSelImg, NinePatchDrawable bgImg, int itemImgW, int itemImgH) {
		this.isSelect = isSelect;
		this.bitmap = img;
		this.index = index;
		this.width = width;
		this.height = height;
		this.selImg = selImg;
		this.unSelImg = unSelImg;
		this.bgNine = bgImg;
		this.itemImgW = itemImgW;
		this.itemImgH = itemImgH;
	}

	public boolean isIsSelect() {
		return isSelect;
	}

	public void setIsSelect(boolean mIsSelect) {
		this.isSelect = mIsSelect;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap mBitmap) {
		this.bitmap = mBitmap;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int mIndex) {
		this.index = mIndex;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	
    /**
     * 清理资源
     */
	public void clear() {
		if (null != bitmap) {
			bitmap.recycle();
			bitmap = null;
		}
		if (null != unSelImg) {
			unSelImg.recycle();
			unSelImg = null;
		}
		if (null != selImg) {
			selImg.recycle();
			selImg = null;
		}
		if (null != bgNine) {
			bgNine = null;
		}
	}
}
