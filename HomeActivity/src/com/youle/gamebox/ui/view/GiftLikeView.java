package com.youle.gamebox.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.GameDetailActivity;
import com.youle.gamebox.ui.bean.LikeBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;

/**
 * Created by Administrator on 14-6-24.
 */
public class GiftLikeView extends LinearLayout implements View.OnClickListener {
    @InjectView(R.id.gameIcon)
    ImageView mGameIcon;
    @InjectView(R.id.gameName)
    TextView mGameName;
    @InjectView(R.id.gift_t)
    TextView mGiftT;
    private LikeBean likeBean ;

    public GiftLikeView(Context context,LikeBean giftLikeBean) {
        super(context);
        this.likeBean = giftLikeBean;
        setOnClickListener(this);
        LayoutInflater.from(context).inflate(R.layout.like_game_item,this);
        ButterKnife.inject(this);
        ImageLoadUtil.displayImage(giftLikeBean.getIconUrl(),mGameIcon);
        mGameName.setText(giftLikeBean.getName());
    }
    public void setText(CharSequence value){
        mGiftT.setText(value);
    }

    @Override
    public void onClick(View v) {
        GameDetailActivity.startGameDetailActivity(getContext(),likeBean.getId(),likeBean.getName(),likeBean.getSource());
    }
}
