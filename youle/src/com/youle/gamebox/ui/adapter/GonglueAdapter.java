package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.bean.GonglueBean;

import java.util.List;

/**
 * Created by Administrator on 14-6-25.
 */
public class GonglueAdapter extends YouleBaseAdapter<GonglueBean> {


    public GonglueAdapter(Context mContext, List<GonglueBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GonglueBean gonglueBean = getItem(position);
        ButterknifeViewHolder holder = null ;
        if(convertView==null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gonglue_item,null);
            holder = new ButterknifeViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder = (ButterknifeViewHolder) convertView.getTag();
        }
        holder.mData.setText(gonglueBean.getDate());
        holder.mGonglueContent.setText(gonglueBean.getExplain());
        holder.mGonglueTitle.setText(gonglueBean.getTitle());
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
        @InjectView(R.id.gonglueTitle)
        TextView mGonglueTitle;
        @InjectView(R.id.gonglueContent)
        TextView mGonglueContent;
        @InjectView(R.id.data)
        TextView mData;

        ButterknifeViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
