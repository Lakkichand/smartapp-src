package com.jiubang.ggheart.apps.desks.Preferences.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * 
 * <br>类描述:自定义单选多选ITEM的对象。
 * <br>功能详细描述:重写onInterceptTouchEvent。拦截子控件的点击事件
 * 
 * @author  licanhui
 * @date  [2012-9-20]
 */
public class DeskSettingSingleDialogItemView extends LinearLayout {

	public DeskSettingSingleDialogItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		//重写
		return true;
	}
}
