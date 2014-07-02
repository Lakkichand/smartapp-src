package com.youle.gamebox.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.util.LOGUtil;

/**
 * Created by Administrator on 2014/5/13.
 */
public abstract class BaseTitleBarView extends LinearLayout {

    @InjectView(R.id.titlebar_left_linearLayout)
    LinearLayout titlebar_left_linearLayout;

    @InjectView(R.id.titlebar_middle_linearLayout)
    LinearLayout titlebar_middle_linearLayout;

    @InjectView(R.id.titlebar_right_linearLayout)
    LinearLayout titlebar_right_linearLayout;

    protected  Context mContext;
    protected ImageView leftImageView;
    protected ImageView rightImageView;
    protected TextView middleTextView;

    public BaseTitleBarView(Context context) {
        super(context);
        init(context);
    }


    public BaseTitleBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public BaseTitleBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.titlebar_customview, this);
        ButterKnife.inject(view);
        setTitleBarLeftView(null);
        setTitleBarMiddleView(null,"");
        setTitleBarRightView(null);
    }

    public void setTitleBarLeftView(View view) {
        titlebar_left_linearLayout.removeAllViews();
        if (view == null) {
            leftImageView = new ImageView(mContext);
            leftImageView.setImageResource(R.drawable.titelbar_canle);
            leftImageView.setOnClickListener(new OnTitleListener(1));
            titlebar_left_linearLayout.addView(leftImageView);
        } else {
            titlebar_left_linearLayout.addView(view);
        }

    }

    public void setVisiableLeftView(int visiable){
        titlebar_left_linearLayout.setVisibility(visiable);
    }
    public void setVisiableMiddleView(int visiable){
        titlebar_middle_linearLayout.setVisibility(visiable);
    }
    public void setVisiableRightView(int visiable){
        titlebar_right_linearLayout.setVisibility(visiable);
    }

    public void setTitleBarMiddleView(View view,String title) {
        titlebar_middle_linearLayout.removeAllViews();
        if(null !=null){
            titlebar_middle_linearLayout.addView(view);
        }else{
            middleTextView = new TextView(mContext);
            if (title != null && !"".equals(title)) {
                middleTextView.setText(title);
                titlebar_middle_linearLayout.addView(middleTextView);
            }
        }


    }

    public void setTitleBarRightView(View view) {
        titlebar_right_linearLayout.removeAllViews();
        if (view == null) {
            rightImageView = new ImageView(mContext);
            rightImageView.setImageResource(R.drawable.titlebar_right_search);
            rightImageView.setOnClickListener(new OnTitleListener(2));
            titlebar_right_linearLayout.removeAllViews();
            titlebar_right_linearLayout.addView(rightImageView);
        } else {
            titlebar_right_linearLayout.addView(view);
        }

    }

    class OnTitleListener implements OnClickListener{
        int onclik;
        OnTitleListener(int onclik) {
            this.onclik = onclik;
        }

        @Override
        public void onClick(View view) {
            switch (onclik){
                case 1:
                    left_ButBack();
                break;
                case 2:
                    right_ButBack();
                break;
            }
        }
    }
    protected abstract void left_ButBack();
    protected abstract void right_ButBack();

}
