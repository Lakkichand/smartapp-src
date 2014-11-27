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
import com.youle.gamebox.ui.bean.LimitBean;

/**
 * Created by Administrator on 14-6-3.
 */
public class GameDetailNewsItemView extends LinearLayout implements View.OnClickListener{
    @InjectView(R.id.type)
    TextView mType;
    @InjectView(R.id.title)
    TextView mTitle;
    public static final int NEWS = 5;
    public static final int GIFT = 7;
    public static  final  int STAGRAY = 3 ;
    public static final  int THEME =2 ;
    public static  final  int COMMUNITY = 6 ;
    private int type;
    public GameDetailNewsItemView(Context context, LimitBean bean,int type) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.news_item, this);
        ButterKnife.inject(this);
        this.type = type ;
        initView(bean);
        setOnClickListener(this);
        setTag(bean);
    }

    private void initView(LimitBean bean) {
        switch (type) {
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
        LimitBean bean = (LimitBean) v.getTag();
        if(type==GIFT){
            Intent intent = new Intent(getContext(), MyRelationActivity.class);
            intent.putExtra(MyRelationActivity.ID,bean.getId()+"");
            intent.putExtra(MyRelationActivity.RELATION,MyRelationActivity.GIFT_DETAIL);
            getContext().startActivity(intent);
        }else  if(type == THEME){
            Intent intent = new Intent(getContext(), MyRelationActivity.class);
            intent.putExtra(MyRelationActivity.ID,bean.getId()+"");
            intent.putExtra(MyRelationActivity.RELATION,MyRelationActivity.SPECIAL_DETAIL);
            getContext().startActivity(intent);
        }else  if(type == STAGRAY||type == NEWS){
            Intent intent = new Intent(getContext(), MyRelationActivity.class);
            intent.putExtra(MyRelationActivity.ID,bean.getId()+"");
            intent.putExtra(MyRelationActivity.RELATION,MyRelationActivity.STAGRY_DETAIL);
            getContext().startActivity(intent);
        }
    }
}
