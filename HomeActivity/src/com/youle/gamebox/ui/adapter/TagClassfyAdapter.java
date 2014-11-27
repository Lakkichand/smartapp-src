package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.bean.GameTagBean;

import java.util.List;

/**
 * Created by Administrator on 14-5-30.
 */
public class TagClassfyAdapter extends YouleBaseAdapter<GameTagBean> {


    public TagClassfyAdapter(Context mContext, List<GameTagBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GameTagBean b = getItem(position);
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.tag_classfy_item, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position % 2 != 0) {
            holder.mGameTag.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.grid_gray_bg));
        } else {
            holder.mGameTag.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.grid_bg_white));
        }
        holder.mGameTag.setText(b.getName());
        return convertView;
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'null'
     * for easy to all layout elements.
     *
     * @author Android Butter Zelezny, plugin for IntelliJ IDEA/Android Studio by Inmite (www.inmite.eu)
     */
    static class ViewHolder {
        @InjectView(R.id.gameTag)
        TextView mGameTag;

        ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
