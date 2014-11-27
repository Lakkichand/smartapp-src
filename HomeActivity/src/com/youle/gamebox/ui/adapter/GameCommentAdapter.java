package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.bean.GameComentBean;
import com.youle.gamebox.ui.fragment.HomepageFragment;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.view.EmojiShowTextView;
import com.youle.gamebox.ui.view.RoundImageView;

import java.util.List;

/**
 * Created by Administrator on 14-6-23.
 */
public class GameCommentAdapter extends YouleBaseAdapter<GameComentBean> implements View.OnClickListener{
    public GameCommentAdapter(Context mContext, List<GameComentBean> mList) {
        super(mContext, mList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GameComentBean bean = getItem(position);
        ButterknifeViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.game_detail_coment_item, null);
            viewHolder = new ButterknifeViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ButterknifeViewHolder) convertView.getTag();
        }
        viewHolder.mUserAvatar.setOnClickListener(this);
        viewHolder.mUserAvatar.setTag(bean);
        viewHolder.mComment.setFaceText(bean.getContent());
        viewHolder.mTime.setText(bean.getCreateTime());
        viewHolder.mUserName.setText(bean.getUserName());
        ImageLoadUtil.displayAvatarImage(bean.getAvatarUrl(), viewHolder.mUserAvatar);
        return convertView;
    }

    @Override
    public void onClick(View v) {
        GameComentBean bean = (GameComentBean) v.getTag();
        CommonActivity.startOtherUserDetail(mContext,bean.getUid(),bean.getUserName());
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout file 'null'
     * for easy to all layout elements.
     *
     * @author Android Butter Zelezny, plugin for IntelliJ IDEA/Android Studio by Inmite (www.inmite.eu)
     */
    static class ButterknifeViewHolder {
        @InjectView(R.id.userAvatar)
        RoundImageView mUserAvatar;
        @InjectView(R.id.userName)
        TextView mUserName;
        @InjectView(R.id.time)
        TextView mTime;
        @InjectView(R.id.comment)
        EmojiShowTextView mComment;

        ButterknifeViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
