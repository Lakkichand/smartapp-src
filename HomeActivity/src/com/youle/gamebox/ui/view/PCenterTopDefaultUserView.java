package com.youle.gamebox.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.util.LOGUtil;

/**
 * Created by Administrator on 2014/5/22.
 */
public class PCenterTopDefaultUserView extends LinearLayout {

    @InjectView(R.id.pcenter_laoyout_top_login)
    Button mPcenterLaoyoutTopLogin;
    @InjectView(R.id.pcenter_laoyout_top_register)
    Button mPcenterLaoyoutTopRegister;
    Context mContext;
    @InjectView(R.id.pcenter_laoyout_top_photo)
    RoundImageView mPcenterLaoyoutTopPhoto;

    public Button getBut() {
        return mPcenterLaoyoutTopLogin;
    }

    public PCenterTopDefaultUserView(Context context) {
        super(context);
        mContext = context;
        init(context);
    }

    public PCenterTopDefaultUserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public PCenterTopDefaultUserView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.pcenter_layout_top_defaultuser, this);
        ButterKnife.inject(this);
        mPcenterLaoyoutTopLogin.setOnClickListener(myOnClickListener);
        mPcenterLaoyoutTopRegister.setOnClickListener(myOnClickListener);
    }

    OnClickListener myOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            LOGUtil.d("junjun", "onClick");
            if (view == mPcenterLaoyoutTopLogin) {
               CommonActivity.startCommonA(mContext, CommonActivity.FRAGMENT_LOGIN,null);
            } else if (view == mPcenterLaoyoutTopRegister) {
                CommonActivity.startCommonA(mContext, CommonActivity.FRAGMENT_REGISTER,null);
            }
        }
    };


}
