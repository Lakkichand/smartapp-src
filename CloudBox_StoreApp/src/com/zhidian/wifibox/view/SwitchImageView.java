package com.zhidian.wifibox.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

/**
 * 会换图片的imageview
 * 
 * @author xiedezhi
 * 
 */
public class SwitchImageView extends ImageView {

	private Drawable mDrawableOne;

	private Drawable mDrawableTwo;

	private int mCurrent = 0;

	private Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			// 显示5秒后自动消失
			if (mCurrent > 10) {
				setVisibility(View.GONE);
				removeCallbacks(mRunnable);
			} else {
				setVisibility(View.VISIBLE);
				if (mCurrent % 2 == 0) {
					setImageDrawable(mDrawableOne);
				} else {
					setImageDrawable(mDrawableTwo);
				}
				mCurrent++;
				removeCallbacks(mRunnable);
				postDelayed(mRunnable, 500);
			}
		}
	};

	public SwitchImageView(Context context, Drawable one, Drawable two) {
		super(context);
		mDrawableOne = one;
		mDrawableTwo = two;
		postDelayed(mRunnable, 500);
	}

}
