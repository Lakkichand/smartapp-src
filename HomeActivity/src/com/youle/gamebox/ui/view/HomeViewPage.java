package com.youle.gamebox.ui.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Administrator on 14-6-19.
 */
public class HomeViewPage extends ViewPager {
    public HomeViewPage(Context context) {
        super(context);
    }

    public HomeViewPage(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return false ;
    }
}
