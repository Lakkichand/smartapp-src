package com.youle.gamebox.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RatingBar;

/**
 * Created by Administrator on 2014/5/15.
 */
public class BaseRatingBarView extends RatingBar{
    private int numStars = 5 ;
    public BaseRatingBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BaseRatingBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseRatingBarView(Context context) {
        super(context);
    }

    private void init(Context context){
        setFocusable(false);
        setNumStars(numStars);
    }
}
