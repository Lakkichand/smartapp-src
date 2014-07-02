package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.bean.MyGiftBean;

import java.util.List;

/**
 * Created by Administrator on 14-6-23.
 */
public class MyGiftAdapter extends YouleBaseAdapter<MyGiftBean> implements View.OnClickListener {
    public MyGiftAdapter(Context mContext, List<MyGiftBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyGiftBean myGiftBean = getItem(position);
        ButterknifeViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.my_gift_item, null);
            viewHolder = new ButterknifeViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ButterknifeViewHolder) convertView.getTag();
        }
        viewHolder.mCode.setText(myGiftBean.getActivationCode());
        viewHolder.mGiftName.setText(myGiftBean.getTitle());
        viewHolder.mData.setText("兑换时期:" + myGiftBean.getExchangeFrom() + "~" + myGiftBean.getExchangeTo());
        viewHolder.mCopy.setTag(myGiftBean);
        viewHolder.mCopy.setOnClickListener(this);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        MyGiftBean bean = (MyGiftBean) v.getTag();
        ClipboardManager copy = (ClipboardManager) (mContext
                .getSystemService(Context.CLIPBOARD_SERVICE));
        copy.setText(bean.getActivationCode());
        Toast.makeText(mContext, "复制成功", Toast.LENGTH_SHORT).show();
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
        @InjectView(R.id.data)
        TextView mData;
        @InjectView(R.id.code)
        TextView mCode;
        @InjectView(R.id.copy)
        TextView mCopy;

        ButterknifeViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
