package com.youle.gamebox.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import com.youle.gamebox.ui.R;


/*
 * Created by Administrator on 14-8-21.
 */
public class PlayVoiceSmall extends PlayView{
    public PlayVoiceSmall(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int[] getPlayRes() {
        int[] res={R.drawable.small_first,R.drawable.small_secend,R.drawable.small_third};
        return res ;
    }
}
