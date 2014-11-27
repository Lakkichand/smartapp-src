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
import com.youle.gamebox.ui.util.LOGUtil;

/**
 * Created by Administrator on 14-8-21.
 */
public abstract class PlayView extends FrameLayout {
    @InjectView(R.id.first)
    ImageView mFirst;
    @InjectView(R.id.secend)
    ImageView mSecend;
    @InjectView(R.id.third)
    ImageView mThird;
    private boolean stoped = false ;
    private Handler mHandler;
    public PlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.play_big, this);
//        ButterKnife.inject(this);
        mFirst = (ImageView) findViewById(R.id.first);
        mSecend= (ImageView) findViewById(R.id.secend);
        mThird= (ImageView) findViewById(R.id.third);
        mHandler = new Handler() ;
        int[] res = getPlayRes() ;
        LOGUtil.e("res",res);
        if(res!=null&&res.length==3){
            mFirst.setImageResource(res[0]);
            mSecend.setImageResource(res[1]);
            mThird.setImageResource(res[2]);
        }
    }

    protected abstract int[] getPlayRes() ;

    int position=0 ;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!stoped) {
                if (position == 4) {
                    position = 0;
                }
                if (position == 0) {
                    mFirst.setVisibility(VISIBLE);
                } else if (position == 1) {
                    mSecend.setVisibility(VISIBLE);
                } else if (position == 2) {
                    mThird.setVisibility(VISIBLE);
                } else {
                    reset();
                }
                position++;
                PlayView.this.postDelayed(this, 200);
            }else {
                endPlay();
            }
        }
    };
    private void reset(){
        mFirst.setVisibility(GONE);
        mSecend.setVisibility(GONE);
        mThird.setVisibility(GONE);
    }
    public void startPlay(){
        stoped = false;
        reset();
        mHandler.post(runnable);
    }
    public void endPlay(){
        stoped = true ;
        position=0 ;
        mHandler.removeCallbacks(runnable);
        mFirst.setVisibility(VISIBLE);
        mSecend.setVisibility(VISIBLE);
        mThird.setVisibility(VISIBLE);
    }
}
