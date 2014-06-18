package com.smartapp.appfreezer.ui;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

/**
 * tab栏滑动提示组件
 * 
 * @author xiedezhi
 * 
 */
public class ScrollTip extends FrameLayout {

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
	private int mAnimDuration = 200;
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
	/**
	 * 底部背景滑动块的drawable
	 */
	private Drawable mBackBlockDrawable;
	/**
	 * 底部背景滑动块的高度，单位px
	 */
	private int mBackBlockHeight = 0;
	/**
	 * 滑动块间距，单位px
	 */
	private int mBlockGap = 0;

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
	 * 设置底部背景滑动块的drawable
	 */
	public void setBackBlockDrawable(Drawable drawable) {
		mBackBlockDrawable = drawable;
	}

	/**
	 * 设置滑动块的高度，单位px
	 */
	public void setBackBlockHeight(int height) {
		if (height >= 0) {
			mBackBlockHeight = height;
		}
	}

	/**
	 * 设置滑动块的间距，单位px
	 */
	public void setBlockGap(int gap) {
		if (gap >= 0) {
			mBlockGap = gap;
		}
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
		for (int i = 0; i < mBlockList.size(); i++) {
			ImageView blockView = mBlockList.get(i);
			if (blockView != null && blockView.getParent() != null) {
				ViewParent parent = blockView.getParent();
				if (parent instanceof ViewGroup) {
					((ViewGroup) parent).removeAllViews();
				}
			}
		}
		removeAllViews();
		{
			// 初始化底部背景滑动块
			LinearLayout backBlock = new LinearLayout(getContext());
			backBlock.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout.LayoutParams backBlockItemLP = new LinearLayout.LayoutParams(
					0, mBackBlockHeight);
			backBlockItemLP.weight = 1;
			LinearLayout.LayoutParams gapLP = new LinearLayout.LayoutParams(
					mBlockGap, mBackBlockHeight);
			int listSize = mBlockList.size();
			for (int i = 0; i < listSize; i++) {
				ImageView backBlockItem = new ImageView(getContext());
				backBlockItem.setScaleType(mScaleType);
				backBlockItem.setImageDrawable(mBackBlockDrawable);
				backBlockItem.setVisibility(View.VISIBLE);
				backBlock.addView(backBlockItem, backBlockItemLP);
				if (i < listSize - 1) {
					ImageView gap = new ImageView(getContext());
					backBlock.addView(gap, gapLP);
				}
			}
			FrameLayout.LayoutParams backBlockLP = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT, mBackBlockHeight,
					Gravity.BOTTOM);
			this.addView(backBlock, backBlockLP);
		}

		{
			// 初始化滑动块
			LinearLayout block = new LinearLayout(getContext());
			block.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout.LayoutParams blockItemLP = new LinearLayout.LayoutParams(
					0, LinearLayout.LayoutParams.MATCH_PARENT);
			blockItemLP.weight = 1;
			LinearLayout.LayoutParams gapLP = new LinearLayout.LayoutParams(
					mBlockGap, mBackBlockHeight);
			int listSize = mBlockList.size();
			for (int i = 0; i < listSize; i++) {
				ImageView blockView = mBlockList.get(i);
				if (i == mCurrentBlock) {
					blockView.setVisibility(VISIBLE);
				} else {
					blockView.setVisibility(INVISIBLE);
				}
				block.addView(blockView, blockItemLP);
				if (i < listSize - 1) {
					ImageView gap = new ImageView(getContext());
					block.addView(gap, gapLP);
				}
			}
			FrameLayout.LayoutParams blockLP = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.MATCH_PARENT);
			this.addView(block, blockLP);
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
