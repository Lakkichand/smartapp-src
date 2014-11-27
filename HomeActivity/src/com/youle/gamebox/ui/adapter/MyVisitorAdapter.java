package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.bean.pcenter.MyVisitorBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.view.EmojiShowTextView;
import com.youle.gamebox.ui.view.RoundImageView;

import java.util.List;

/**
 * Created by Administrator on 2014/6/24.
 */
public class MyVisitorAdapter extends GridBaseAdapter<MyVisitorBean>{


    LayoutInflater inflater = null;
    public MyVisitorAdapter(Context mContext, List<MyVisitorBean> mList) {
        super(mContext, mList);
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    protected View getItemView(int position, View convertView, ViewGroup parent) {
        MyVisitorBean myVisitorBean = (MyVisitorBean) getItem(position);

        MyVisitordHolder mymsgboardHolder = null;
        if(convertView == null ){
            convertView = inflater.inflate(R.layout.pcmyvisitor_item_layout,null);
            mymsgboardHolder = new MyVisitordHolder(convertView);
            convertView.setTag(mymsgboardHolder);
        }else{
            mymsgboardHolder = (MyVisitordHolder)convertView.getTag();
        }
        String avatarUrl = myVisitorBean.getAvatarUrl();
        //头像
        //if(renturnNull(avatarUrl, null))
        ImageLoadUtil.displayAvatarImage(avatarUrl, mymsgboardHolder.mPcmyvisitorItemPhoto);
        mymsgboardHolder.mPcmyvisitorItemNickname.setText(myVisitorBean.getNickName());
        mymsgboardHolder.mPcmyvisitorItemTime.setText(myVisitorBean.getTime());

        return convertView;
    }





    static class MyVisitordHolder {
        @InjectView(R.id.pcmyvisitor_item_photo)
        RoundImageView mPcmyvisitorItemPhoto;
        @InjectView(R.id.pcmyvisitor_item_nickname)
        TextView mPcmyvisitorItemNickname;
        @InjectView(R.id.pcmyvisitor_item_time)
        TextView mPcmyvisitorItemTime;
        MyVisitordHolder(View view) {
            ButterKnife.inject(this,view);
        }
    }
}
