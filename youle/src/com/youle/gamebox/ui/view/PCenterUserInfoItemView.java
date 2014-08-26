package com.youle.gamebox.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.fragment.UserInfoModfyFragment;

/**
 * Created by Administrator on 2014/5/22.
 */
public class PCenterUserInfoItemView extends LinearLayout {


    @InjectView(R.id.userinfo_item_title)
    TextView mUserinfoItemTitle;
    @InjectView(R.id.userinfo_item_connent)
    TextView mUserinfoItemConnent;
    @InjectView(R.id.userinfo_item_modfy)
    ImageView mUserinfoItemModfy;
    Context mContext;

    public PCenterUserInfoItemView(Context context) {
        super(context);
        init(context);
    }

    public PCenterUserInfoItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public PCenterUserInfoItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.userinfo_item_layout, this);
        ButterKnife.inject(this);


    }

    public void setData(boolean canUpdate,final String title,final String connent){
        mUserinfoItemTitle.setText(title+":");
        mUserinfoItemConnent.setText(connent);

    }

}
