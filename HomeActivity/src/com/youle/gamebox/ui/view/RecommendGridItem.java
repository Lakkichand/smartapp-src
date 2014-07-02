package com.youle.gamebox.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;

/**
 * Created by Administrator on 14-6-3.
 */
public class RecommendGridItem extends LinearLayout {
    @InjectView(R.id.gameIcon)
    ImageView mGameIcon;
    @InjectView(R.id.gameIconLayout)
    RelativeLayout mGameIconLayout;
    @InjectView(R.id.gameName)
    TextView mGameName;
    @InjectView(R.id.gameSize)
    TextView mGameSize;
    @InjectView(R.id.gameType)
    TextView mGameType;
    @InjectView(R.id.gamedesLayout)
    LinearLayout mGamedesLayout;
    @InjectView(R.id.scro)
    RatingBar mScro;
    @InjectView(R.id.roundProgress)
    RoundProgressView mRoundProgress;
    @InjectView(R.id.downloadNumber)
    TextView mDownloadNumber;
    @InjectView(R.id.gift)
    ImageView mGift;
    private GameBean bean;

    public RecommendGridItem(Context context, GameBean bean) {
        super(context);
        this.bean = bean;
        LayoutInflater.from(context).inflate(R.layout.recomend_game_item, this);
        ButterKnife.inject(this);
        initGameBean();
    }

    private void initGameBean() {
        mGameName.setText(bean.getName());
        mGameSize.setText(bean.getSize());
        mGameType.setText(" | "+bean.getCategory());
        mScro.setRating(bean.getScore() / 2.0f);
        ImageLoadUtil.displayImage(bean.getIconUrl(), mGameIcon);
        mDownloadNumber.setText(bean.getDownloads());
        if(bean.getHasSpree()){
            mGift.setVisibility(VISIBLE);
        }else {
           mGift.setVisibility(GONE);
        }
    }
}
