package com.youle.gamebox.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.bean.LimitBean;
import com.youle.gamebox.ui.bean.special.SpecialAdetailBean;

/**
 * Created by Administrator on 14-6-24.
 */
public class SpecialDetailHeadView extends LinearLayout {
    SpecialAdetailBean bean ;
    @InjectView(R.id.time)
    TextView mTime;
    @InjectView(R.id.special)
    TextView mSpecial;
    @InjectView(R.id.newsLayout)
    LinearLayout mNewsLayout;
    @InjectView(R.id.allcheck)
    CheckBox mAllcheck;

    public SpecialDetailHeadView(Context context,SpecialAdetailBean bean) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.special_detail_head,this);
        ButterKnife.inject(this);
        this.bean = bean ;
        initView();
    }

    private void initView() {
        mSpecial.setText(bean.getExplain());
        mNewsLayout.removeAllViews();
        if(bean.getSprees()!=null) {//只做了礼包，后面的需要补全
            for (LimitBean limitBean : bean.getSprees()) {
                mNewsLayout.addView(new GameDetailNewsItemView(getContext(), limitBean, GameDetailNewsItemView.GIFT));
            }
        }
    }
}
