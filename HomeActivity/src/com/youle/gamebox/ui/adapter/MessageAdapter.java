package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.bean.MessageBean;

import java.util.List;

/**
 * Created by Administrator on 14-6-26.
 */
public class MessageAdapter extends YouleBaseAdapter<MessageBean> {
    public MessageAdapter(Context mContext, List<MessageBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ButterknifeViewHolder holder = null;
        MessageBean messageBean = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.message_item, null);
            holder = new ButterknifeViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ButterknifeViewHolder) convertView.getTag();
        }
        holder.mTitle.setText(messageBean.getType() + messageBean.getContent());
        holder.mDate.setText(messageBean.getTime());
        return convertView;
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'null'
     * for easy to all layout elements.
     *
     * @author Android Butter Zelezny, plugin for IntelliJ IDEA/Android Studio by Inmite (www.inmite.eu)
     */
    static class ButterknifeViewHolder {
        @InjectView(R.id.title)
        TextView mTitle;
        @InjectView(R.id.date)
        TextView mDate;

        ButterknifeViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
