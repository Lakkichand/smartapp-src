package com.youle.gamebox.ui.view;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.MyRelationActivity;
import com.youle.gamebox.ui.bean.IndexHeadBean;

/**
 * Created by Administrator on 14-6-3.
 */
public class NewsItemView extends LinearLayout implements View.OnClickListener {
    @InjectView(R.id.type)
    TextView mType;
    @InjectView(R.id.title)
    TextView mTitle;

    private static final int NEWS = 5;
    private static final int GIFT = 7;
    private static  final  int STAGRAY = 3 ;
    private static final  int THEME =2 ;
    private static  final  int COMMUNITY = 6 ;
    private IndexHeadBean mIndexHeadBean ;
    public NewsItemView(Context context, IndexHeadBean bean) {
        super(context);
        mIndexHeadBean = bean ;
        LayoutInflater.from(context).inflate(R.layout.news_item, this);
        ButterKnife.inject(this);
        initView(bean);
        setOnClickListener(this);
    }

    private void initView(IndexHeadBean bean) {
        switch (bean.getType()) {
            case NEWS:
                mType.setText(R.string.news);
                mType.setBackgroundColor(getContext().getResources().getColor(R.color.home_blu));
                break;
            case GIFT:
                mType.setText(R.string.gift);
                mType.setBackgroundColor(getContext().getResources().getColor(R.color.home_red));
                break;
            case STAGRAY:
                mType.setText(R.string.strategy);
                mType.setBackgroundColor(getContext().getResources().getColor(R.color.home_green));
                break;
            case THEME:
                mType.setText(R.string.theme);
                mType.setBackgroundColor(getContext().getResources().getColor(R.color.home_yello));
                break;
            case COMMUNITY:
                mType.setText(R.string.commnity);
                mType.setBackgroundColor(getContext().getResources().getColor(R.color.home_orenge));
                break;
        }
        mTitle.setText(bean.getTitle());
    }

    @Override
    public void onClick(View v) {
       if(mIndexHeadBean.getType()==GIFT){
           Intent intent = new Intent(getContext(), MyRelationActivity.class);
           intent.putExtra(MyRelationActivity.ID,mIndexHeadBean.getTarget());
           intent.putExtra(MyRelationActivity.RELATION,MyRelationActivity.GIFT_DETAIL);
           getContext().startActivity(intent);
       }else  if(mIndexHeadBean.getType() == THEME){
           Intent intent = new Intent(getContext(), MyRelationActivity.class);
           intent.putExtra(MyRelationActivity.ID,mIndexHeadBean.getTarget());
           intent.putExtra(MyRelationActivity.RELATION,MyRelationActivity.SPECIAL_DETAIL);
           getContext().startActivity(intent);
       }else  if(mIndexHeadBean.getType() == STAGRAY||mIndexHeadBean.getType() == NEWS) {
           Intent intent = new Intent(getContext(), MyRelationActivity.class);
           intent.putExtra(MyRelationActivity.ID, mIndexHeadBean.getTarget());
           intent.putExtra(MyRelationActivity.RELATION, MyRelationActivity.STAGRY_DETAIL);
           getContext().startActivity(intent);
       }
    }
}
