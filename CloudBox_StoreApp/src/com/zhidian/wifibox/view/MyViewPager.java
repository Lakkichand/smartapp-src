package com.zhidian.wifibox.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;

/**
 * 自定义ViewPager，用于解决ViewPager与HorizontalScrollView冲突
 * 
 * @author zhaoyl
 * 
 */
public class MyViewPager extends ViewPager {

	public MyViewPager(Context context) {
		super(context);
	}

	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (v instanceof HorizontalScrollView) {
			return true;
		}
		return super.canScroll(v, checkV, dx, x, y);
	}

}
