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
import com.youle.gamebox.ui.bean.GameCategoryBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;

import java.util.List;

/**
 * Created by Administrator on 14-6-17.
 */
public class GameCatoryAdpater extends YouleBaseAdapter<GameCategoryBean> {
    public GameCatoryAdpater(Context mContext, List<GameCategoryBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GameCategoryBean bean = getItem(position);
        ButterknifeViewHolder holder =null ;
        if(convertView==null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.game_catary_item,null);
            holder = new ButterknifeViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ButterknifeViewHolder) convertView.getTag();
        }
        holder.mName.setText(bean.getName());
        ImageLoadUtil.displayNotRundomImage(bean.getIconUrl(),holder.mImageview);
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
        @InjectView(R.id.imageview)
        ImageView mImageview;
        @InjectView(R.id.name)
        TextView mName;
        @InjectView(R.id.number)
        TextView mNumber;

        ButterknifeViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
