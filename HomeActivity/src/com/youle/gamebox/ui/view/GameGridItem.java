package com.youle.gamebox.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 14-5-30.
 */
public class GameGridItem extends LinearLayout {
    /**
     *
     * @param context
     * @param layoutId 每个布局里面相同控件的id必须是一样的。否则会出现空指针异常
     */
    public GameGridItem(Context context,int layoutId) {
        super(context);
        LayoutInflater.from(context).inflate(layoutId,this);
        ButterKnife.inject(this);
    }
}
