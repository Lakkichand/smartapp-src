package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.bean.CommendGameBean;
import com.youle.gamebox.ui.greendao.RecommendGame;
import com.youle.gamebox.ui.util.ImageLoadUtil;

import java.util.List;

/**
 * Created by Administrator on 14-4-29.
 */
public class RecommendGameAdapter extends YouleBaseAdapter<CommendGameBean> {
    public RecommendGameAdapter(Context mContext, List<CommendGameBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommendGameBean game = getItem(position) ;
        ButterknifeViewHolder butterknifeViewHolder = null ;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.test_item, null);
            butterknifeViewHolder =new ButterknifeViewHolder(convertView) ;
            convertView.setTag(butterknifeViewHolder);
        }else{
            butterknifeViewHolder = (ButterknifeViewHolder) convertView.getTag();
        }
        ImageLoadUtil.displayImage(game.getIconPath(),butterknifeViewHolder.mGameIcon);
        butterknifeViewHolder.mGameName.setText(game.getName());
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
        @InjectView(R.id.gameName)
        TextView mGameName;

        ButterknifeViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
