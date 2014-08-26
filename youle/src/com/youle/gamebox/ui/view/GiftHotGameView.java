package com.youle.gamebox.ui.view;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.activity.GameDetailActivity;
import com.youle.gamebox.ui.bean.HotGame;
import com.youle.gamebox.ui.fragment.AllGiftFragment;
import com.youle.gamebox.ui.fragment.GonglueListFragment;
import com.youle.gamebox.ui.util.ImageLoadUtil;

/**
 * Created by Administrator on 14-6-24.
 */
public class GiftHotGameView extends LinearLayout implements View.OnClickListener {
    @InjectView(R.id.gameIcon)
    ImageView mGameIcon;
    @InjectView(R.id.gameName)
    TextView mGameName;
    @InjectView(R.id.giftNumber)
    TextView mGiftNumber;
    @InjectView(R.id.getGift)
    TextView mGetGift;
    ViewType type = ViewType.GIFT;
    private HotGame mHotGame ;
    @Override
    public void onClick(View v) {
        GameDetailActivity.startGameDetailActivity(getContext(),mHotGame.getId(),mHotGame.getName(),mHotGame.getSource());
    }

    public enum ViewType {
        GIFT, GONGLUE
    }

    public GiftHotGameView(Context context, HotGame hotGame, ViewType type) {
        super(context);
        this.type = type;
        initView(hotGame);
    }

    public GiftHotGameView(Context context, HotGame giftHotGame) {
        super(context);
        initView(giftHotGame);
    }

    private void initView(final HotGame giftHotGame) {
        setOnClickListener(this);
        LayoutInflater.from(getContext()).inflate(R.layout.gift_hot_game_item, this);
        ButterKnife.inject(this);
        this.mHotGame = giftHotGame;
        ImageLoadUtil.displayImage(giftHotGame.getIconUrl(), mGameIcon);
        mGameName.setText(giftHotGame.getName());
        if (type == ViewType.GONGLUE) {
            String html = getContext().getString(R.string.gonglue_number_format2, giftHotGame.getAmount() + "");
            mGiftNumber.setText(Html.fromHtml(html));
            mGetGift.setText(R.string.look);
        } else {
            String html = getContext().getString(R.string.gift_number_format2, giftHotGame.getAmount() + "");
            mGiftNumber.setText(Html.fromHtml(html));
        }
        mGetGift.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == ViewType.GONGLUE) {
                    GonglueListFragment gonglueListFragment = new GonglueListFragment();
                    gonglueListFragment.setGameId(giftHotGame.getId()+"");
                    ((BaseActivity) getContext()).addFragment(gonglueListFragment, true);
                }else{
                    AllGiftFragment allGiftFragment = new AllGiftFragment();
                    allGiftFragment.setGameId(giftHotGame.getId()+"");
                    ((BaseActivity)getContext()).addFragment(allGiftFragment,true);
                }
            }
        });
    }

}
