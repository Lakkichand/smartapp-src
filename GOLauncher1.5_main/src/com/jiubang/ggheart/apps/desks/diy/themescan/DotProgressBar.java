package com.jiubang.ggheart.apps.desks.diy.themescan;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.jiubang.core.mars.MImage;

/**
 * 由一块块图片拼成的进度条，根据总的块数和当前突出的块来标识进度
 * 
 * @author yunfeng
 * 
 */
public class DotProgressBar extends View {

	private int mTotalNum = 0;
	private int mCurNum = 0;

	private MImage mNormalImage = null;
	private MImage mLightImage = null;

	private boolean mIsShow;

	/**
	 * 构造方法
	 * 
	 * @param context
	 *            程序上下文
	 * @param normalImage
	 *            标识个数的图片
	 * @param lightImage
	 *            标识当前进度的图片
	 * @param totalNum
	 *            总的标识进度的图片个数(个数需要大于零)
	 */
	public DotProgressBar(Context context, MImage normalImage, MImage lightImage, int totalNum) {
		super(context);
		mNormalImage = normalImage;
		mLightImage = lightImage;
		mTotalNum = totalNum;

		mIsShow = true;
	}

	public DotProgressBar(Context context) {
		super(context, null);
	}

	public DotProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);

		mIsShow = true;
	}

	public void setImage(MImage normalImage, MImage lightImage) {
		mNormalImage = normalImage;
		mLightImage = lightImage;
	}

	public void setIsShow(boolean isShow) {
		mIsShow = isShow;

		invalidate();
	}

	/**
	 * 设置当前进度
	 * 
	 * @param curNum
	 *            当前显示的是总个数中的第几个
	 */
	public void setCurProgress(int curNum) {
		mCurNum = curNum;
		invalidate();
	}

	/**
	 * 改变总的个数
	 * 
	 * @param totalNum
	 *            比啊是进度的图片的个数（大于零）
	 */
	public void setTotalNum(int totalNum) {
		mTotalNum = totalNum;
		// invalidate();
		requestLayout();
	}

	/**
	 * 获取进度总数
	 * 
	 * @return 返回进度总数
	 */
	public int getTotalNum() {
		return mTotalNum;
	}

	/**
	 * 获取进度条需要的宽度
	 * 
	 * @return 进度条显示需要的宽度
	 */
	public int getNeedW() {
		int w = 23 * mTotalNum;
		if (null != mLightImage) {
			w = mLightImage.getWidth() * mTotalNum;
		}
		return w;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// 根据个数设置大小，高度为图片的高度
		int w = getNeedW();
		int h = 23;
		if (null != mLightImage) {
			h = mLightImage.getHeight();
		}
		setMeasuredDimension(w, h);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (!mIsShow) {
			return;
		}

		if (mTotalNum <= 0) {
			return;
		}
		int width = getWidth();
		int height = getHeight();
		int eachW = width / mTotalNum;
		// 每次都调用，是怕图片在外边也有用到，改变了锚点
		setImageAnchor();
		for (int i = 0; i < mTotalNum; i++) {
			if (i != mCurNum) {
				if (null != mNormalImage) {
					mNormalImage.draw(canvas, eachW * i + eachW / 2, height / 2);
				}
			} else {
				if (null != mLightImage) {
					mLightImage.draw(canvas, eachW * i + eachW / 2, height / 2);
				}
			}
		}
	}

	private void setImageAnchor() {
		if (null != mNormalImage) {
			mNormalImage.setAnchor(MImage.HCENTER | MImage.VCENTER);
		}
		if (null != mLightImage) {
			mLightImage.setAnchor(MImage.HCENTER | MImage.VCENTER);
		}
	}

	public void recycle() {
		mNormalImage.getBitmap().recycle();
		mNormalImage = null;

		mLightImage.getBitmap().recycle();
		mLightImage = null;
	}

}
