package com.jiubang.ggheart.apps.desks.share;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.view.View;

/**
 * 
 * <br>类描述: 卡片
 * <br>功能详细描述:
 * 
 * @author  maxiaojun
 * @date  [2012-9-25]
 */
public class ShareItemView extends View {
	private ShareItem mShareItem;
	private Paint mPaint;
	private PaintFlagsDrawFilter mPaintFilter;

	private Rect mItemImgEdge = new Rect(); // 图片与相框左右边缘的距离
	private Rect mImageRect; // 图片绘制区域
	private Rect mBgRect; // 背景绘制区域
	private Rect mChectBoxRect; // 单选框绘制区域

	private int mWidth; // 组件宽
	private int mHeight; // 组件高
	private int mCheckBoxW; // 单选框宽度
	private int mCheckBoxH; // 单选框高度
	private int mCheckboxOffset; // 多选按钮与相框偏移的距离

	public ShareItemView(Context context) {
		super(context);
		mPaint = new Paint();
		mPaint.setAntiAlias(true); // 抗锯齿
		mPaintFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
	}

	/***
	 * 初始化
	 * 
	 * @param shareItem
	 * @param itemImgEdge
	 * @param checkboxOffset
	 * @param itemImgBottom
	 */
	public void initData(ShareItem shareItem, int checkboxOffset) {
		mShareItem = shareItem;

		mCheckboxOffset = checkboxOffset;
		mShareItem.bgNine.getPadding(mItemImgEdge);
		mCheckBoxW = mShareItem.selImg.getWidth();
		mCheckBoxH = mShareItem.selImg.getHeight();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (null == mShareItem || null == mShareItem.bgNine || null == mShareItem.selImg
				|| null == mShareItem.unSelImg) {
			return;
		}
		canvas.setDrawFilter(mPaintFilter);
		// 背景
		mShareItem.bgNine.setBounds(mBgRect);
		mShareItem.bgNine.draw(canvas);
		// 图片
		if (null != mShareItem.bitmap) {
			canvas.drawBitmap(mShareItem.bitmap, null, mImageRect, mPaint);
		}
		// 单选框
		if (mShareItem.isSelect) {
			canvas.drawBitmap(mShareItem.selImg, null, mChectBoxRect, mPaint);
		} else {
			canvas.drawBitmap(mShareItem.unSelImg, null, mChectBoxRect, mPaint);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mWidth = getWidth();
		mHeight = getHeight();
		mBgRect = new Rect(0, mCheckboxOffset, mWidth - mCheckboxOffset, mHeight);
		mImageRect = new Rect(mItemImgEdge.left, mCheckboxOffset + mItemImgEdge.top,
				mItemImgEdge.left + mShareItem.itemImgW, mItemImgEdge.top + mCheckboxOffset
						+ mShareItem.itemImgH);
		mChectBoxRect = new Rect(mWidth - mCheckBoxW, 0, mWidth, mCheckBoxH);
	}

	public ShareItem getShareItem() {
		return mShareItem;
	}

}
