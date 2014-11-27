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
import com.youle.gamebox.ui.activity.HomeActivity;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.view.RoundProgressView;

import java.util.List;

/**
 * Created by Administrator on 14-6-4.
 */
public class MyGameAdapter extends DownloadAdapter<GameBean> {

    public MyGameAdapter(Context mContext, List<GameBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final GameBean b = getItem(position);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.my_game_item, null);
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
        holder.mDetailLayout.setTag(R.string.bean, b);
        holder.mDetailLayout.setTag(R.string.tag,0);
        holder.mGiftDetail.setTag(R.string.bean,b);
        holder.mGiftDetail.setTag(R.string.tag,2);
        holder.mStageryDetail.setTag(R.string.bean,b);
        holder.mStageryDetail.setTag(R.string.tag,3);
        holder.mUninstallDetail.setTag(b);
        holder.mUninstallDetail.setOnClickListener(uninstallClicklistener);
        holder.mDetailLayout.setOnClickListener(detailClickListener);
        holder.mGiftDetail.setOnClickListener(detailClickListener);
        holder.mStageryDetail.setOnClickListener(detailClickListener);
        ImageLoadUtil.displayImage(b.getIconUrl(), holder.mGameIcon);
        if (b.getHasSpree()) {
            holder.mGift.setVisibility(View.VISIBLE);
        } else {
            holder.mGift.setVisibility(View.GONE);
        }
        initDownloadStatus(holder.progressView, b);
        final ViewHolder finalHolder = holder;
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mContext instanceof HomeActivity) {
                    finalHolder.mMoreLayout.setVisibility(finalHolder.mMoreLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                }
            }
        });
        return convertView;
    }

    private View.OnClickListener detailClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GameBean b = (GameBean) v.getTag(R.string.bean);
            int tab = (Integer)v.getTag(R.string.tag);
            Intent intent = new Intent(mContext, GameDetailActivity.class);
            intent.putExtra(GameDetailActivity.GAME_NAME, b.getName());
            intent.putExtra(GameDetailActivity.GAME_ID, b.getId());
            intent.putExtra(GameDetailActivity.GAME_RESOUCE, b.getSource());
            intent.putExtra(GameDetailActivity.SHOW_TAB,tab);
            mContext.startActivity(intent);
        }
    };

    private View.OnClickListener uninstallClicklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GameBean bean = (GameBean) v.getTag();
            AppInfoUtils.uninstall(getContext(),bean.getPackageName());
        }
    };

    static class ViewHolder {
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
        TextView downLoadNumText;
        @InjectView(R.id.gift)
        ImageView mGift;
        @InjectView(R.id.moreLayout)
        LinearLayout mMoreLayout;
        @InjectView(R.id.detail)
        LinearLayout mDetailLayout;
        @InjectView(R.id.gift_detail)
        LinearLayout mGiftDetail;
        @InjectView(R.id.stageryDetail)
        LinearLayout mStageryDetail;
        @InjectView(R.id.uninstall)
        LinearLayout mUninstallDetail;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
