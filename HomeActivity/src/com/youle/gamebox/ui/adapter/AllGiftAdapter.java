package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.bean.GiftBean;
import com.youle.gamebox.ui.fragment.GiftDetailFragment;

import java.util.List;

/**
 * Created by Administrator on 14-6-24.
 */
public class AllGiftAdapter extends YouleBaseAdapter<GiftBean> {
    public AllGiftAdapter(Context mContext, List<GiftBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final GiftBean bean = getItem(position);
        ButterknifeViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.gift_all_item, null);
            holder = new ButterknifeViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ButterknifeViewHolder) convertView.getTag();
        }
        holder.mGiftName.setText(bean.getTitle());
        holder.mGiftdesc.setText(bean.getContent().trim());
        holder.mRestNumber.setText(bean.getRest() + "");
        holder.mTotalNumber.setText(bean.getTotal() + "");
        if (bean.getStatus() == GiftBean.NOMOR) {
            holder.mGetGift.setText(R.string.lingqu);
            holder.mGetGift.setEnabled(true);
        } else if (bean.getStatus() == GiftBean.HAVE_NO) {
            holder.mGetGift.setText(R.string.lingqu_no);
            holder.mGetGift.setEnabled(false);
        } else if (bean.getStatus() == GiftBean.HAS_GOT) {
            holder.mGetGift.setText(R.string.lingqued);
            holder.mGetGift.setEnabled(false);
        } else if (bean.getStatus() == GiftBean.TIME_OUT) {
            holder.mGetGift.setText(R.string.time_out);
            holder.mGetGift.setEnabled(false);
        } else if (bean.getStatus() == GiftBean.NOT_START) {
            holder.mGetGift.setText(R.string.not_start);
            holder.mGetGift.setEnabled(false);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GiftDetailFragment giftDetailFragment = new GiftDetailFragment(bean.getId()+"");
                ((BaseActivity)mContext).addFragment(giftDetailFragment,true);
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
        @InjectView(R.id.giftName)
        TextView mGiftName;
        @InjectView(R.id.restNumber)
        TextView mRestNumber;
        @InjectView(R.id.totalNumber)
        TextView mTotalNumber;
        @InjectView(R.id.getGift)
        TextView mGetGift;
        @InjectView(R.id.giftdesc)
        TextView mGiftdesc;

        ButterknifeViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
