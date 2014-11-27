package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.bean.special.SpecialBean;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.view.RoundProgressView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 14-6-24.
 */
public class ThemDetailAdapter extends DownloadAdapter<GameBean> implements CompoundButton.OnCheckedChangeListener {
    public Set<GameBean> selectedGameBean = new HashSet<GameBean>();

    public ThemDetailAdapter(Context mContext, List<GameBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GameBean b = getItem(position);
        ButterknifeViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.them_detail_game_item, null);
            holder = new ButterknifeViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ButterknifeViewHolder) convertView.getTag();
        }
        holdersSet.add(holder.mRoundProgress);
        holder.mGameName.setText(b.getName());
        holder.mGameSize.setText(b.getSize());
        holder.mScro.setRating(b.getScore() / 2.0f);
        holder.mRoundProgress.setTag(b);
        holder.mRoundProgress.setFocusable(true);
        holder.mDownloadNumber.setText(b.getDownloads());
        holder.mGameType.setText(" | " + b.getCategory());
        holder.mRoundProgress.requestFocus();
        holder.mRoundProgress.setOnClickListener(downloadListener);
        ImageLoadUtil.displayImage(b.getIconUrl(), holder.mGameIcon);
        initDownloadStatus(holder.mRoundProgress, b);
        holder.mGamedesc.setText(b.getExplain());
        if (selectedGameBean.contains(b)) {
            holder.mCheck.setChecked(true);
        } else {
            holder.mCheck.setChecked(false);
        }
        if(position==(getCount()-1)){
            holder.grayLine.setVisibility(View.GONE);
        }else {
            holder.grayLine.setVisibility(View.VISIBLE);
        }
        holder.mCheck.setTag(b);
        holder.mCheck.setOnCheckedChangeListener(this);
        return convertView;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        GameBean b = (GameBean) buttonView.getTag();
        if (isChecked) {
            selectedGameBean.add(b);
        } else {
            selectedGameBean.remove(b);
        }
    }

    public void selectAll() {
        for (int i = 0; i < getCount(); i++) {
            selectedGameBean.add(getItem(i));
        }
        notifyDataSetChanged();
    }

    public void cancelAll() {
        selectedGameBean.clear();
        notifyDataSetChanged();
    }

    public void installAll(){
        for (GameBean b:selectedGameBean){
            downLoadBean(b);
        }
    }
    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'null'
     * for easy to all layout elements.
     *
     * @author Android Butter Zelezny, plugin for IntelliJ IDEA/Android Studio by Inmite (www.inmite.eu)
     */
    static class ButterknifeViewHolder {
        @InjectView(R.id.check)
        CheckBox mCheck;
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
        @InjectView(R.id.gamedesc)
        TextView mGamedesc;
        @InjectView(R.id.gray_line)
        ImageView grayLine ;
        ButterknifeViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
