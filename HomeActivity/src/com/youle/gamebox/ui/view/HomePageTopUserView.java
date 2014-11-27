package com.youle.gamebox.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.bean.User;
import com.youle.gamebox.ui.bean.pcenter.PersonalBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;

/**
 * Created by Administrator on 2014/5/22.
 */
public class HomePageTopUserView extends LinearLayout {
    @InjectView(R.id.pcenter_laoyout_top_photo)
    ImageView mPcenterLaoyoutTopPhoto;
    @InjectView(R.id.pcenter_laoyout_top_userName)
    TextView mPcenterLaoyoutTopUserName;
    @InjectView(R.id.pcenter_laoyout_top_email)
    TextView mPcenterLaoyoutTopEmail;



    public HomePageTopUserView(Context context) {
        super(context);
        init(context);
    }

    public HomePageTopUserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public HomePageTopUserView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.homepage_layout_top_userinfo, this);
        ButterKnife.inject(this);
    }

    public void setViewData(PersonalBean personalBean){
        if(personalBean==null) return;
        if(null != personalBean.getAvatarUrl()&& !"".equals(personalBean.getAvatarUrl()))
        ImageLoadUtil.displayAvatarImage(personalBean.getAvatarUrl(),mPcenterLaoyoutTopPhoto);
        mPcenterLaoyoutTopUserName.setText(personalBean.getNickName());
        if(TextUtils.isEmpty(personalBean.getEmail())){
            mPcenterLaoyoutTopEmail.setVisibility(GONE);
        }else {
            mPcenterLaoyoutTopEmail.setVisibility(VISIBLE);
            mPcenterLaoyoutTopEmail.setText(personalBean.getEmail());
        }
    }

}
