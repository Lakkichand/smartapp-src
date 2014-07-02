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
import com.youle.gamebox.ui.activity.GameListActivity;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.util.DownLoadUtil;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.view.RoundProgressView;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 14-6-4.
 */
public class RankGameAdapter extends DownloadAdapter<GameBean> {
    public RankGameAdapter(Context mContext, List<GameBean> mList) {
        super(mContext, mList);
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final GameBean b = getItem(position);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.rank_game_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holdersSet.add(holder.progressView);
        holder.mGameName.setText(b.getName());
        holder.mGameSize.setText(b.getSize());
        holder.mScro.setRating(b.getScore() / 2.0f);
        holder.progressView.setTag(b);
        holder.progressView.setFocusable(true);
        holder.downLoadNumText.setText(b.getDownloads());
        holder.mGameType.setText(" | " + b.getCategory());
        holder.progressView.requestFocus();
        holder.progressView.setOnClickListener(downloadListener);
        ImageLoadUtil.displayImage(b.getIconUrl(), holder.mGameIcon);
        holder.mNumber.setText("" + (position + 1));
        if(b.getHasSpree()){
            holder.mGift.setVisibility(View.VISIBLE);
        }else {
            holder.mGift.setVisibility(View.GONE);
        }
        if (position > 2) {
            holder.mNumber.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.rank_grey_icon));
        } else {
            holder.mNumber.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.rank_yellow_icon));
        }
        initDownloadStatus(holder.progressView, b);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, GameDetailActivity.class);
                intent.putExtra(GameDetailActivity.GAME_NAME,b.getName());
                intent.putExtra(GameDetailActivity.GAME_ID, b.getId());
                intent.putExtra(GameDetailActivity.GAME_RESOUCE,b.getSource());
                mContext.startActivity(intent);

            }
        });
        return convertView;
    }




    static class ViewHolder {
        @InjectView(R.id.number)
        TextView mNumber;
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
        RoundProgressView progressView;
        @InjectView(R.id.downloadNumber)
        TextView downLoadNumText ;
        @InjectView(R.id.gift)
         ImageView mGift ;
        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
