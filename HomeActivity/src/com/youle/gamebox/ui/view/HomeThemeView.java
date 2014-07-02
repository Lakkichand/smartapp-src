package com.youle.gamebox.ui.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.MyRelationActivity;
import com.youle.gamebox.ui.bean.IndexHeadBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;

/**
 * Created by Administrator on 14-6-11.
 */
public class HomeThemeView extends LinearLayout implements View.OnClickListener {
    @InjectView(R.id.imageView)
    ImageView mImageView;
    @InjectView(R.id.title)
    TextView mTitle;
    private  IndexHeadBean mIndexHeadBean ;
    public HomeThemeView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.home_them_item,this) ;
        ButterKnife.inject(this);
        setOnClickListener(this);
    }
    public void initData(IndexHeadBean bean){
        this.mIndexHeadBean = bean ;
        mTitle.setText(bean.getTitle());
        ImageLoadUtil.displayNotRundomImage(bean.getImgUrl(),mImageView);
    }

    @Override
    public void onClick(View v) {
        Intent  intent = new Intent(getContext(), MyRelationActivity.class);
        intent.putExtra(MyRelationActivity.RELATION,MyRelationActivity.SPECIAL_DETAIL);
        intent.putExtra(MyRelationActivity.ID,mIndexHeadBean.getTarget()) ;
        getContext().startActivity(intent);
    }
}
