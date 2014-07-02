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
import com.youle.gamebox.ui.bean.GameTagBean;

import java.util.List;

/**
 * Created by Administrator on 14-5-30.
 */
public class GameCassfyAdapter extends YouleBaseAdapter<GameTagBean> {
    public GameCassfyAdapter(Context mContext, List<GameTagBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GameTagBean bean = getItem(position) ;
        ButterknifeViewHolder holder =null ;
        if(convertView==null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.game_classfy_item,null) ;
            holder = new ButterknifeViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ButterknifeViewHolder) convertView.getTag();
        }
        holder.mGameName.setText(bean.getName());
        return convertView;
    }
/**
 * This class contains all butterknife-injected Views & Layouts from layout file 'null'
 * for easy to all layout elements.
 *
 * @author Android Butter Zelezny, plugin for IntelliJ IDEA/Android Studio by Inmite (www.inmite.eu)
 */
    static

    class ButterknifeViewHolder {
        @InjectView(R.id.gameBackground)
        ImageView mGameBackground;
        @InjectView(R.id.gameName)
        TextView mGameName;
        @InjectView(R.id.gameNumber)
        TextView mGameNumber;

        ButterknifeViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
