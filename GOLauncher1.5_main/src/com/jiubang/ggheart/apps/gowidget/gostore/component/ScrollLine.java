package com.jiubang.ggheart.apps.gowidget.gostore.component;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;

import com.jiubang.ggheart.apps.gowidget.gostore.common.GoStorePublicDefine;

public class ScrollLine extends LinearLayout {

	private Context mContext = null;
	private int mCurrentBlock = 0;
	private int mTotalBlcok = 1;
	private int mAnimDuration = 200;

	private ArrayList<Button> mBlockList;

	public ScrollLine(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public ScrollLine(Context context, int count) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		init(count);
	}

	public ScrollLine(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public void init(int count) {
		mTotalBlcok = count;
		setOrientation(HORIZONTAL);
		mBlockList = new ArrayList<Button>();
		createScrollBlock();
	}

	public void setBlockDrawable(Drawable drawable) {
		if (mBlockList != null) {
			for (Button blickView : mBlockList) {
				blickView.setBackgroundDrawable(drawable);
			}
		}
	}

	public void setBlockResource(int resid) {
		if (mBlockList != null) {
			for (Button blickView : mBlockList) {
				blickView.setBackgroundResource(resid);
			}
		}
	}

	public void setBlockColor(int color) {
		if (mBlockList != null) {
			for (Button blickView : mBlockList) {
				blickView.setBackgroundColor(color);
			}
		}
	}

	public void setBlockCount(int count) {
		mTotalBlcok = count;
	}

	public void setIndex(int index) {
		mCurrentBlock = index;
	}

	public void moveLeft(int step) {
		int goIndex = mCurrentBlock;
		int toIndex = goIndex - step;
		if (toIndex < 0) {
			toIndex = mTotalBlcok - 1;
		}
		Button goBlock = mBlockList.get(goIndex);
		Button toBlock = mBlockList.get(toIndex);
		toBlock.setVisibility(VISIBLE);
		goBlock.setVisibility(INVISIBLE);
		TranslateAnimation t = new TranslateAnimation(Animation.RELATIVE_TO_SELF, step,
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0);
		t.setDuration(mAnimDuration);
		toBlock.setAnimation(t);
		TranslateAnimation t2 = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, -step, Animation.RELATIVE_TO_SELF, 0,
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
		Button goBlock = mBlockList.get(goIndex);
		Button toBlock = mBlockList.get(toIndex);
		toBlock.setVisibility(VISIBLE);
		goBlock.setVisibility(INVISIBLE);
		TranslateAnimation t = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, step, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0);
		t.setDuration(mAnimDuration);
		goBlock.setAnimation(t);
		TranslateAnimation t2 = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -step,
				Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0);
		t2.setDuration(mAnimDuration);
		toBlock.setAnimation(t2);
		mCurrentBlock = toIndex;
	}

	public void setAnimationDuration(int duration) {
		mAnimDuration = duration;
	}

	public void setBlockSize(int width, int height) {
		if (mBlockList != null) {
			for (Button blockView : mBlockList) {
				blockView.setWidth(width);
				blockView.setHeight(height);
			}
		}
		createScrollBlock();
	}

	/**
	 * 设置好所有参数后要更新UI
	 */
	public void updateUi() {
		if (mBlockList == null) {
			return;
		}
		layout();
	}

	private void createScrollBlock() {
		for (int i = 0; i < mTotalBlcok; i++) {
			Button scrollBlock = new Button(mContext);
			scrollBlock.setBackgroundDrawable(null);
			scrollBlock.setBackgroundColor(GoStorePublicDefine.GREEN);
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
			Button blockView = mBlockList.get(i);
			if (i == mCurrentBlock) {
				blockView.setVisibility(VISIBLE);
			}
			addView(blockView, params);
		}
	}

	public void recycle() {
		if (mBlockList != null) {
			for (Button button : mBlockList) {
				button = null;
			}
			mBlockList = null;
		}

		if (mContext != null) {
			mContext = null;
		}
	}
}
