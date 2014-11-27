package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.bean.CatagroryBean;
import com.youle.gamebox.ui.fragment.GonglueListFragment;
import com.youle.gamebox.ui.fragment.StagoryDetailFragment;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;

import java.util.List;

/**
 * Created by Administrator on 14-6-20.
 */
public class CatagroryAdapter extends YouleBaseAdapter<CatagroryBean> implements View.OnClickListener{
    public CatagroryAdapter(Context mContext, List<CatagroryBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CatagroryBean bean = getItem(position);
        ButterknifeViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.categroy_item, null);
            holder = new ButterknifeViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ButterknifeViewHolder) convertView.getTag();
        }
        holder.mGameName.setText(bean.getName());
        String formate = getContext().getString(R.string.gonglue_number_format,bean.getAmount()+"");
        ImageLoadUtil.displayImage(bean.getIconUrl(), holder.mGameIcon);
        holder.mCategroryNumber.setText(Html.fromHtml(formate));
        holder.mLook.setTag(bean);
        holder.mLook.setOnClickListener(this);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        CatagroryBean bean = (CatagroryBean) v.getTag();
        GonglueListFragment gonglueListFragment = new GonglueListFragment();
        gonglueListFragment.setGameId(bean.getId()+"");
        ((BaseActivity)mContext).addFragment(gonglueListFragment,true);
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
        @InjectView(R.id.gift)
        ImageView mGift;
        @InjectView(R.id.gameIconLayout)
        RelativeLayout mGameIconLayout;
        @InjectView(R.id.gameName)
        TextView mGameName;
        @InjectView(R.id.categroryNumber)
        TextView mCategroryNumber;
        @InjectView(R.id.look)
        TextView mLook;

        ButterknifeViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
