package com.jiubang.ggheart.apps.desks.share;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;

import com.gau.go.launcherex.R;
import com.go.util.TextUtil;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;
import com.jiubang.ggheart.apps.desks.diy.IDiyFrameIds;
import com.jiubang.ggheart.apps.desks.diy.IDiyMsgIds;

/***
 * 绘制分享的图片（分一张图片和多张图片两种样式）
 * 
 * @author maxiaojun
 * 
 */
public class ShareImage {
	private Context mContext;
	private PaintFlagsDrawFilter mPaintFilter;

	// 多张图片的情况
	private int mGoIconEdge; // go图标左边距
	private int mGoBottom; // go图标下边距
	private int mImgEdge; // 图片左边距
	private int mPlaneTop; // 飞机图标上边距
	private int mPlaneRight; // 飞机图标右边距
	private int mImgMaxHeight; // 图片的最大高度
	private int mPlanarCodeEdge; // 二维码图片左边距
	private int mPlanarCodeWidth; // 二维码部分的宽度

	// 一张图片的情况
	private int mTitleTop_1;
	private int mTitleBottom_1;
	private int mImgMaxWidth_1;
	private int mImgEdge_1;
	private int mTextTop_1;
	private int mTextLeft_1;
	private int mPlanarCodeTop_1; // 二维码部分的宽度
	private int mPlanarCodeBottom_1; // 二维码部分的宽度
	private int mShareBottom_1;

	private ArrayList<Integer> mSelList; // 标识选中图片的索引
	private BitmapDrawable mGoIcon; // go图标
	private NinePatchDrawable mItemBgImg; // 每个桌面卡片的背景
	private NinePatchDrawable mBgImg; // 背景图片
	private BitmapDrawable mDefPlanarCode; // 默认二维码图片
	private BitmapDrawable mPlaneIcon; // 飞机

	private int mWidth; // 最终生成图片的宽度
	private int mHeight; // 最终生成图片的高度

	private int mScreenW; // 屏幕实际宽度
	private int mScreenH; // 屏幕实际高度

	private int mCardNum; // 卡片数量
	private int mCardWidth; // 每张图片的宽
	private int mCardHeight; // 每张图片的高

	private int mPCodeWidth; // 二维码图片宽度
	private int mPCodeHeight; // 二维码图片高度
	private int mGoIconSize; // Go图标size

	private Rect mRect = new Rect();
	private Rect mCardBgP = new Rect();
	private Rect mBgEdge = new Rect(); // 背景.9图片padding值

	private int mLineSpace = 20;
	private int mTitleSize = 30;
	private String mTitleText; // "Go桌面，秀出你的个性！";
	private int mTextSize1 = 24;
	private String mText1; // "扫描二维码，立刻免费拥有！";
	private int mTextSize2 = 18;
	private String mText2; // "Go桌面海量主题、丰富插件,让你手机桌面与众不同.";
	
	private int mType = ShareFrame.TYPE_CAPTURE;
	/***
	 * 构造方法
	 * 
	 * @param context
	 * @param imageRes
	 *            选中的桌面图片信息
	 */
	public ShareImage(Context context, ArrayList<Integer> selList, int type) {
		mContext = context;
		mSelList = selList;
		mType = type;
		mPaintFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
		initValue();
	}

	private void initValue() {
		Resources resource = mContext.getResources();
		// 文字
		mTitleText = resource.getString(R.string.share_img_title_text);
		mText1 = resource.getString(R.string.share_img_text_1);
		mText2 = resource.getString(R.string.share_img_text_4);
		mTitleSize = (int) resource.getDimension(R.dimen.share_img_text_size_1);
		mTextSize1 = (int) resource.getDimension(R.dimen.share_img_text_size_2);
		mTextSize2 = (int) resource.getDimension(R.dimen.share_img_text_size_3);
		mLineSpace = (int) resource.getDimension(R.dimen.share_img_line_space);
		// 多张图片
		mGoIconEdge = (int) resource.getDimension(R.dimen.share_img_go_padding_edge);
		mGoBottom = (int) resource.getDimension(R.dimen.share_img_go_padding_bottom);
		mPlaneTop = (int) resource.getDimension(R.dimen.share_img_plane_padding_top);
		mPlaneRight = (int) resource.getDimension(R.dimen.share_img_plane_padding_right);
		mImgEdge = (int) resource.getDimension(R.dimen.share_img_padding_edge);
		mImgMaxHeight = (int) resource.getDimension(R.dimen.share_img_max_height);
		mPlanarCodeEdge = (int) resource.getDimension(R.dimen.share_img_planar_code_edge);
		mPlanarCodeWidth = (int) resource.getDimension(R.dimen.share_img_planar_code_width);
		// 一张图片
		mTitleTop_1 = (int) resource.getDimension(R.dimen.share_img_title_top_1);
		mTitleBottom_1 = (int) resource.getDimension(R.dimen.share_img_title_bottom_1);
		mImgMaxWidth_1 = (int) resource.getDimension(R.dimen.share_img_max_width_1);
		mImgEdge_1 = (int) resource.getDimension(R.dimen.share_img_edge_1);
		mTextTop_1 = (int) resource.getDimension(R.dimen.share_text_top_1);
		mTextLeft_1 = (int) resource.getDimension(R.dimen.share_text_left_1);
		mPlanarCodeTop_1 = (int) resource.getDimension(R.dimen.share_planarcode_top_1);
		mPlanarCodeBottom_1 = (int) resource.getDimension(R.dimen.share_planarcode_bottom_1);
		mShareBottom_1 = (int) resource.getDimension(R.dimen.share_bottom_1);

		mGoIcon = (BitmapDrawable) resource.getDrawable(R.drawable.icon);
		mBgImg = (NinePatchDrawable) resource.getDrawable(R.drawable.share_img_bg);
		mItemBgImg = (NinePatchDrawable) resource.getDrawable(R.drawable.share_item_pic_bg);
		mPlaneIcon = (BitmapDrawable) resource.getDrawable(R.drawable.guide_golauncherex);

		mDefPlanarCode = (BitmapDrawable) resource
				.getDrawable(R.drawable.share_def_planar_code_200_no);

		mPCodeWidth = mDefPlanarCode.getIntrinsicWidth();
		mPCodeHeight = mDefPlanarCode.getIntrinsicHeight();
		mGoIconSize = mGoIcon.getIntrinsicHeight();

		mScreenW = GoLauncher.getDisplayWidth();
		mScreenH = GoLauncher.getDisplayHeight();

		mItemBgImg.getPadding(mCardBgP);
		mBgImg.getPadding(mBgEdge);

		compute();
	}

	/***
	 * 计算坐标
	 */
	public void compute() {
		mCardNum = mSelList.size();
		// 计算创建图片的宽高
		if (mCardNum < 2) {
			if (mScreenW <= mImgMaxWidth_1) {
				mCardHeight = mScreenH;
				mCardWidth = mScreenW;
			} else {
				mCardWidth = mImgMaxWidth_1;
				mCardHeight = mScreenH * mCardWidth / mScreenW;
			}
			mWidth = mCardWidth + mImgEdge_1 * 2 + mBgEdge.left + mBgEdge.right;
			mHeight = mTitleTop_1 + mTitleSize + mTitleBottom_1 + mCardHeight + mTextTop_1
					+ (mTextSize1 + mLineSpace) * 2 + mPlanarCodeTop_1 + mPCodeHeight
					+ mPlanarCodeBottom_1 + mTextSize1 + mLineSpace + mTextSize2 + mLineSpace
					+ mShareBottom_1 + mBgEdge.top + mBgEdge.bottom;
		} else {
			if (mScreenH <= mImgMaxHeight) {
				mCardHeight = mScreenH;
				mCardWidth = mScreenW;
			} else {
				mCardHeight = mImgMaxHeight;
				mCardWidth = mScreenW * mCardHeight / mScreenH;
			}
			mWidth = mImgEdge + (mCardWidth + mImgEdge) * mCardNum + 2 * mPlanarCodeEdge
					+ mPlanarCodeWidth + mBgEdge.left + mBgEdge.right;
			mHeight = mGoIconEdge + mGoIconSize + mGoBottom + mCardHeight + mImgEdge + mBgEdge.top
					+ mBgEdge.bottom;
		}
	}

	/***
	 * 拼图
	 * 
	 * @param workspaceImg
	 * @param dockImg
	 * @param bgImg
	 * @return
	 */
	public Bitmap spellImage() {
		if (mSelList == null || mSelList.size() == 0) {
			return null;
		}

		if (mSelList.size() == 1) {
			return spellImageOne();
		} else {
			return spellImageMore();
		}
	}

	/***
	 * 一张图片的情况
	 */
	private Bitmap spellImageOne() {
		if (mRect == null) {
			mRect = new Rect();
		}
		Paint paint = new Paint();
		Bitmap img = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas();
		canvas.setDrawFilter(mPaintFilter);
		canvas.setBitmap(img);

		// 画背景
		mRect.set(0, 0, mWidth, mHeight);
		mBgImg.setBounds(mRect);
		mBgImg.draw(canvas);
		canvas.translate(mBgEdge.left, mBgEdge.top);

		int x = 0;
		int y = 0;
		// 画图片及其背景
		y = mImgEdge_1;
		x = mImgEdge_1;
		mItemBgImg.setBounds(x - mCardBgP.left, y - mCardBgP.top, x + mCardWidth + mCardBgP.right,
				y + mCardHeight + mCardBgP.bottom);
		mItemBgImg.draw(canvas);
		mRect.set(x, y, x + mCardWidth, y + mCardHeight);
		Bitmap bitmap = getCardImg(mSelList.get(0));
		canvas.drawBitmap(bitmap, null, mRect, paint);
		bitmap.recycle();
		bitmap = null;
		
		paint.setTextSize(mTitleSize);
		
		x = (int) (mWidth - mGoIconSize - paint.measureText(mTitleText)) / 2;
		y += mGoIconEdge + mCardHeight + 34;
		// 画GO图标
		mRect.set(x, y, x + mGoIconSize, y + mGoIconSize);
		canvas.drawBitmap(mGoIcon.getBitmap(), null, mRect, paint);

		// Go桌面，秀出你的个性！
		paint.setAntiAlias(true);
		paint.setColor(0xff000000);
		x += mGoIconSize;
		y += mTitleTop_1 + mTitleSize * 2 / 3;
		canvas.drawText(mTitleText, x, y, paint);


		// Go桌面海量主题、丰富插件,让你手机桌面与众不同
		paint.setColor(0xff7e817a);
		paint.setTextSize(mTextSize1);

		x = mTextLeft_1;
		y += mTextTop_1;
		String[] text = TextUtil.typeString(mText2, mWidth - 2 * mTextLeft_1, paint);
		if (text != null) {
			for (int i = 0; i < text.length; i++) {
				y += mTextSize1 + mLineSpace;
				canvas.drawText(text[i], x, y, paint);
			}
		}
		// 画二维码
		x = (mWidth - mPCodeWidth) / 2;
		y += mPlanarCodeTop_1 = mTextSize1 + mLineSpace;
		mRect.set(x, y, x + mPCodeWidth, y + mPCodeHeight);
		canvas.drawBitmap(mDefPlanarCode.getBitmap(), null, mRect, paint);

		int textW;
		// 扫描二维码，立刻免费拥有
		paint.setColor(0xff5ebe00);
		paint.setTextSize(mTextSize1);
		textW = (int) paint.measureText(mText1);
		x = (mWidth - textW) / 2;
		y += mPCodeHeight + mPlanarCodeBottom_1;
		canvas.drawText(mText1, x, y, paint);
		
		String textLink = "或登录:";
		y += mTextSize1 + 19;
		paint.setColor(0xff7e817a);
		canvas.drawText(textLink, x, y, paint);
		x += paint.measureText(textLink);
		textLink = "www.app.3g.cn";
		paint.setColor(0xff5ebe00);
		canvas.drawText(textLink, x, y, paint);
		
		return img;
	}

	/***
	 * 多张图片的情况
	 */
	private Bitmap spellImageMore() {
		if (mRect == null) {
			mRect = new Rect();
		}
		Paint paint = new Paint();
		Bitmap img = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas();
		canvas.setDrawFilter(mPaintFilter);
		canvas.setBitmap(img);

		// 画背景
		mRect.set(0, 0, mWidth, mHeight);
		mBgImg.setBounds(mRect);
		mBgImg.draw(canvas);

		canvas.translate(mBgEdge.left, mBgEdge.top);

		// 画图片及其背景
		int cardTop = mGoBottom + mGoIconEdge;
		int cardLeft = 0;
		for (int i = 0; i < mSelList.size(); i++) {
			cardLeft = mImgEdge + (mCardWidth + mImgEdge) * i;
			mItemBgImg.setBounds(cardLeft - mCardBgP.left, cardTop - mCardBgP.top, cardLeft
					+ mCardWidth + mCardBgP.right, cardTop + mCardHeight + mCardBgP.bottom);
			mItemBgImg.draw(canvas);
			mRect.set(cardLeft, cardTop, cardLeft + mCardWidth, cardTop + mCardHeight);
//			Bitmap bitmap = getCardImg(mSelList.get(i));
			canvas.drawBitmap(getCardImg(mSelList.get(i)), null, mRect, paint);
//			bitmap.recycle();
//			bitmap = null;
		}
		
		int x = cardLeft + mCardWidth + mGoIconEdge + (mWidth - cardLeft - mCardWidth - mImgEdge - mGoIconSize - mGoIconEdge) / 2;
		int y = cardTop;
		// 画GO图标
		mRect.set(x, y, x + mGoIconSize, y + mGoIconSize);
		canvas.drawBitmap(mGoIcon.getBitmap(), null, mRect, paint);
	
		
		// Go桌面，秀出你的个性！
		paint.setAntiAlias(true);
		paint.setColor(0xff000000);
		paint.setTextSize(mTitleSize);
		x = cardLeft + mCardWidth + mGoIconEdge;
		y += mGoIconSize + mTitleTop_1 + 14;
		canvas.drawText(mTitleText, x, y, paint);
		
		// 画二维码
		int pcLeft = (mCardWidth + mImgEdge) * mSelList.size() + mPlanarCodeEdge
				+ (mPlanarCodeWidth - mPCodeWidth) / 2;
		y += mTitleSize + 53;
		int pcBottom = y + mPCodeHeight;
		mRect.set(pcLeft, y, pcLeft + mPCodeWidth, pcBottom);
		canvas.drawBitmap(mDefPlanarCode.getBitmap(), null, mRect, paint);


		// 扫描二维码，立刻免费拥有
		paint.setColor(0xff5ebe00);
		paint.setTextSize(mTextSize1);
		x = (mCardWidth + mImgEdge) * mSelList.size() + mPlanarCodeEdge;
		y = pcBottom + 52;
		canvas.drawText(mText1, x, y, paint);

		String textLink = "或登录:";
		y += mTextSize1 + 19;
		paint.setColor(0xff7e817a);
		canvas.drawText(textLink, x, y, paint);
		x += paint.measureText(textLink);
		textLink = "www.app.3g.cn";
		paint.setColor(0xff5ebe00);
		canvas.drawText(textLink, x, y, paint);
		
		// Go桌面海量主题、丰富插件,让你手机桌面与众不同
		x = (mCardWidth + mImgEdge) * mSelList.size() + mPlanarCodeEdge;
		paint.setColor(0xff7e817a);
		paint.setTextSize(mTextSize1);
		String[] text = TextUtil.typeString(mText2, mPlanarCodeWidth, paint);
		if (text != null) {
			y += 70;
			for (int i = 0; i < text.length; i++) {
				y += mTextSize1 + mLineSpace;
				canvas.drawText(text[i], x, y, paint);
			}
		}
		return img;
	}

	/***
	 * 获取要分享的桌面图片
	 * 
	 * @param index
	 * @return
	 */
	private Bitmap getCardImg(int index) {
		Bitmap bitmap = null;
		Bundle bundle = new Bundle();
		ArrayList<Float> list = new ArrayList<Float>(2);
		list.add((float) index);
		list.add(mCardWidth * 1.0f / mScreenW);
		GoLauncher.sendMessage(this, IDiyFrameIds.SCREEN_FRAME, IDiyMsgIds.GET_SHARE_IMAGE, -1,
				bundle, list);
		bitmap = bundle.getParcelable("image");
		return bitmap;
	}

	/***
	 * 资源回收
	 */
	public void clear() {
		if (null != mGoIcon) {
			mGoIcon = null;
		}

		if (null != mItemBgImg) {
			mItemBgImg = null;
		}

		if (null != mBgImg) {
			mBgImg = null;
		}

		if (null != mDefPlanarCode) {
			mDefPlanarCode = null;
		}

		if (null != mPlaneIcon) {
			mPlaneIcon = null;
		}

		if (null != mSelList) {
			mSelList = null;
		}
		if (null != mRect) {
			mRect = null;
		}
		if (null != mCardBgP) {
			mCardBgP = null;
		}
		if (null != mBgEdge) {
			mBgEdge = null;
		}
	}
}
