package com.zhidian.wifibox.view;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

/**
 * 应用游戏中心，tab栏滑动提示组件
 * 
 * @author xiedezhi
 * 
 */
public class ScrollTip extends LinearLayout {

	private Context mContext = null;
	/**
	 * 当前滑动块位置
	 */
	private int mCurrentBlock = 0;
	/**
	 * 滑动块总数
	 */
	private int mTotalBlcok = 1;
	/**
	 * 滑动动画时间
	 */
	private int mAnimDuration = 100;
	/**
	 * 滑动块的scaleType
	 */
	private ImageView.ScaleType mScaleType = ScaleType.CENTER;
	/**
	 * 滑动块列表
	 */
	private ArrayList<ImageView> mBlockList;
	/**
	 * 滑动块drawable
	 */
	private Drawable mDrawable;

	public ScrollTip(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public ScrollTip(Context context) {
		super(context);
		mContext = context;
	}

	/**
	 * 设置滑动块
	 */
	public void setBlockDrawable(Drawable drawable) {
		mDrawable = drawable;
	}

	/**
	 * 设置滑动动画时间
	 */
	public void setAnimationDuration(int duration) {
		mAnimDuration = duration;
	}

	/**
	 * 设置滑动块的scaleType
	 */
	public void setScaleType(ImageView.ScaleType scaleType) {
		if (scaleType != null) {
			mScaleType = scaleType;
		}
	}

	/**
	 * 设置当前选中的下标
	 */
	public void setCurrentIndex(int index) {
		mCurrentBlock = index;
	}

	/**
	 * 初始化滑动条
	 */
	public void init(int count) {
		mTotalBlcok = count;
		setOrientation(HORIZONTAL);
		mBlockList = new ArrayList<ImageView>();
		createScrollBlock();
	}

	/**
	 * 设置好所有参数后要更新UI
	 */
	public void updateUI() {
		if (mBlockList == null) {
			return;
		}
		layout();
	}

	private void createScrollBlock() {
		for (int i = 0; i < mTotalBlcok; i++) {
			ImageView scrollBlock = new ImageView(mContext);
			scrollBlock.setScaleType(mScaleType);
			scrollBlock.setImageDrawable(mDrawable);
			scrollBlock.setVisibility(INVISIBLE);
			mBlockList.add(scrollBlock);
		}
	}

	private void layout() {
		removeAllViews();
		LayoutParams params = new LinearLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		params.weight = 1;
		int listSize = mBlockList.size();
		for (int i = 0; i < listSize; i++) {
			ImageView blockView = mBlockList.get(i);
			if (i == mCurrentBlock) {
				blockView.setVisibility(VISIBLE);
			} else {
				blockView.setVisibility(INVISIBLE);
			}
			addView(blockView, params);
		}
	}

	public void moveLeft(int step) {
		int goIndex = mCurrentBlock;
		int toIndex = goIndex - step;
		if (toIndex < 0) {
			toIndex = mTotalBlcok - 1;
		}
		ImageView goBlock = mBlockList.get(goIndex);
		ImageView toBlock = mBlockList.get(toIndex);
		toBlock.setVisibility(VISIBLE);
		goBlock.setVisibility(INVISIBLE);
		TranslateAnimation t = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, step, Animation.RELATIVE_TO_SELF,
				0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
		t.setDuration(mAnimDuration);
		toBlock.setAnimation(t);
		TranslateAnimation t2 = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
				-step, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0);
		t2.setDuration(mAnimDuration);
		goBlock.setAnimation(t2);
		mCurrentBlock = toIndex;
	}

	public void moveRight(int step) {
		int goIndex = mCurrentBlock;
		int toIndex = goIndex + step;
		if (toIndex >= mTotalBlcok) {
			toIndex = 0;
		}
		ImageView goBlock = mBlockList.get(goIndex);
		ImageView toBlock = mBlockList.get(toIndex);
		toBlock.setVisibility(VISIBLE);
		goBlock.setVisibility(INVISIBLE);
		TranslateAnimation t = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF,
				step, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0);
		t.setDuration(mAnimDuration);
		goBlock.setAnimation(t);
		TranslateAnimation t2 = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, -step, Animation.RELATIVE_TO_SELF,
				0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
		t2.setDuration(mAnimDuration);
		toBlock.setAnimation(t2);
		mCurrentBlock = toIndex;
	}

	/**
	 * 回收资源
	 */
	public void recycle() {
		if (mBlockList != null) {
			for (ImageView block : mBlockList) {
				block = null;
			}
			mBlockList = null;
		}

		if (mContext != null) {
			mContext = null;
		}
	}
}
