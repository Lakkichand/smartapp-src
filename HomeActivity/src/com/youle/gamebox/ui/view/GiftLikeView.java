package com.youle.gamebox.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.bean.LikeBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;

/**
 * Created by Administrator on 14-6-24.
 */
public class GiftLikeView extends LinearLayout {
    @InjectView(R.id.gameIcon)
    ImageView mGameIcon;
    @InjectView(R.id.gameName)
    TextView mGameName;
    @InjectView(R.id.gift_t)
    TextView mGiftT;

    public GiftLikeView(Context context,LikeBean giftLikeBean) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.like_game_item,this);
        ButterKnife.inject(this);
        ImageLoadUtil.displayImage(giftLikeBean.getIconUrl(),mGameIcon);
        mGameName.setText(giftLikeBean.getName());
    }
    public void setText(CharSequence value){
        mGiftT.setText(value);
    }
}
