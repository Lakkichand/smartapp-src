package com.youle.gamebox.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
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
    @InjectView(R.id.installAll)
    TextView mInstallAll;
    @InjectView(R.id.gray_line)
    ImageView mGrayImage ;
    private IListener iListener ;
    public  interface  IListener{
        public void check(CompoundButton button,boolean isCheck);
        public void install() ;
    }
    public SpecialDetailHeadView(Context context,SpecialAdetailBean bean,IListener listener) {
        super(context);
        this.iListener = listener ;
        LayoutInflater.from(context).inflate(R.layout.special_detail_head,this);
        ButterKnife.inject(this);
        this.bean = bean ;
        initView();
        mAllcheck.setOnCheckedChangeListener(onCheckedChangeListener);
        mInstallAll.setOnClickListener(onClickListener);
        if(mNewsLayout.getChildCount()==0){
            mGrayImage.setVisibility(GONE);
        }
    }

    private void initView() {
        mSpecial.setText(bean.getExplain());
        mTime.setText(bean.getTime());
        mNewsLayout.removeAllViews();
        if(bean.getSprees()!=null) {
            for (LimitBean limitBean : bean.getSprees()) {
                mNewsLayout.addView(new GameDetailNewsItemView(getContext(), limitBean, GameDetailNewsItemView.GIFT));
            }
        }
        if(bean.getGonglues()!=null) {
           for (LimitBean limitBean:bean.getGonglues()){
               mNewsLayout.addView(new GameDetailNewsItemView(getContext(),limitBean,GameDetailNewsItemView.STAGRAY));
           }
        }

        if(bean.getNews()!=null){
            for (LimitBean limitBean:bean.getNews()){
                mNewsLayout.addView(new GameDetailNewsItemView(getContext(),limitBean,GameDetailNewsItemView.NEWS));
            }
        }
    }

    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
           if(iListener!=null){
               iListener.check(buttonView,isChecked);
           }
        }
    };

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(iListener!=null){
                iListener.install();
            }
        }
    };
}
