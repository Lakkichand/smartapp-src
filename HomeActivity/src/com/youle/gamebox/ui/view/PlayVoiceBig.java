package com.youle.gamebox.ui.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;


/**
 * Created by Administrator on 14-8-21.
 */
public class PlayVoiceBig extends PlayView{
    public PlayVoiceBig(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected int[] getPlayRes() {
        int[] res={R.drawable.big_first,R.drawable.big_secend,R.drawable.big_third};
        return res ;
    }
}
