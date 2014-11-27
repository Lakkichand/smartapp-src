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
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.bean.special.SpecialBean;
import com.youle.gamebox.ui.fragment.SpecilDetailFragment;
import com.youle.gamebox.ui.util.ImageLoadUtil;

import java.util.List;

/**
 * Created by Administrator on 14-6-23.
 */
public class SpecialAdapter extends YouleBaseAdapter<SpecialBean> {
    public SpecialAdapter(Context mContext, List<SpecialBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final SpecialBean specialBean = getItem(position);
        ButterknifeViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.special_item, null);
            viewHolder = new ButterknifeViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ButterknifeViewHolder) convertView.getTag();
        }
        viewHolder.mSpecialTitle.setText(specialBean.getName());
        viewHolder.mSpecialContent.setText(specialBean.getExplain());
        viewHolder.mDataTime.setText(specialBean.getUpdateDate());
        ImageLoadUtil.displayNotRundomImage(specialBean.getImageUrl(), viewHolder.mSpecialLogo);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpecilDetailFragment specilDetailFragment = new SpecilDetailFragment(specialBean.getId()+"") ;
                ((BaseActivity)mContext).addFragment(specilDetailFragment,true);
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
        @InjectView(R.id.specialTitle)
        TextView mSpecialTitle;
        @InjectView(R.id.specialLogo)
        ImageView mSpecialLogo;
        @InjectView(R.id.specialContent)
        TextView mSpecialContent;
        @InjectView(R.id.dataTime)
        TextView mDataTime;

        ButterknifeViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
