package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.GameDetailActivity;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.view.RoundProgressView;

import java.util.List;

/**
 * Created by Administrator on 14-6-17.
 */
public class GameListAdapter extends DownloadAdapter<GameBean> {
    public GameListAdapter(Context mContext, List<GameBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final GameBean bean = getItem(position);
        LOGUtil.e("getView", bean.getName() + "|" + bean.getDownloadPath());
        ButterknifeViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.game_list_item, null);
            holder = new ButterknifeViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ButterknifeViewHolder) convertView.getTag();
        }
        holdersSet.add(holder.mRoundProgress);
        holder.mGameName.setText(bean.getName());
        holder.mGameSize.setText(bean.getSize());
        holder.mScro.setRating(bean.getScore() / 2.0f);
        holder.mRoundProgress.setTag(bean);
        holder.mRoundProgress.setFocusable(true);
        holder.mDownloadNumber.setText(bean.getDownloads());
        holder.mGameType.setText(" | " + bean.getCategory());
        holder.mRoundProgress.requestFocus();
        holder.mRoundProgress.setOnClickListener(downloadListener);
        ImageLoadUtil.displayImage(bean.getIconUrl(), holder.mGameIcon);
        if (bean.getHasSpree()) {
            holder.mGift.setVisibility(View.VISIBLE);
        } else {
            holder.mGift.setVisibility(View.GONE);
        }
        initDownloadStatus(holder.mRoundProgress, bean);
        holder.mRoundProgress.setTag(bean);
        holder.mRoundProgress.setOnClickListener(downloadListener);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GameDetailActivity.class);
                intent.putExtra(GameDetailActivity.GAME_NAME, bean.getName());
                intent.putExtra(GameDetailActivity.GAME_ID, bean.getId());
                intent.putExtra(GameDetailActivity.GAME_RESOUCE, bean.getSource());
                mContext.startActivity(intent);
            }
        });
        return convertView;
    }


    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'null'
     * for easy to all layout elements.
     *
     * @author Android Butter Zelezny, plugin for IntelliJ IDEA/Android Studio by Inmite (www.inmite.eu)
     */
    static class ButterknifeViewHolder {
        @InjectView(R.id.gameIcon)
        ImageView mGameIcon;
        @InjectView(R.id.gift)
        ImageView mGift;
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

        ButterknifeViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
