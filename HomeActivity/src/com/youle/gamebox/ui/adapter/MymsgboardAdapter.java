package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.bean.pcenter.MymsgboardBean;
import com.youle.gamebox.ui.bean.pcenter.MymsgboardCommentsBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.view.EmojiShowTextView;
import com.youle.gamebox.ui.view.RoundImageView;

import java.util.List;

/**
 * Created by Administrator on 2014/6/24.
 */
public class MymsgboardAdapter extends YouleBaseAdapter<MymsgboardBean> {
    LayoutInflater inflater;


    public MymsgboardAdapter(Context mContext, List<MymsgboardBean> mList) {
        super(mContext, mList);
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MymsgboardBean mymsgboardBean = mList.get(position);

        MymsgboardHolder mymsgboardHolder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.mymsgboardlist_item_layout, null);
            mymsgboardHolder = new MymsgboardHolder(convertView);
            convertView.setTag(mymsgboardHolder);
        } else {
            mymsgboardHolder = (MymsgboardHolder) convertView.getTag();
        }
        String avatarUrl = mymsgboardBean.getAvatarUrl();

        //头像
        if (renturnNull(mymsgboardBean.getAvatarUrl(), null))
            ImageLoadUtil.displayAvatarImage(mymsgboardBean.getAvatarUrl(), mymsgboardHolder.mMessagelistItemPhoto);
        mymsgboardHolder.mMessagelistItemNickName.setText(mymsgboardBean.getNickName());
        mymsgboardHolder.mMessagelistItemPublicTime.setText(mymsgboardBean.getTime());
        //内容
        if (renturnNull(mymsgboardBean.getContent(), mymsgboardHolder.mMessagelistItemContent))
            mymsgboardHolder.mMessagelistItemContent.setFaceText(mymsgboardBean.getContent());

        //留言 回复
        List<MymsgboardCommentsBean> comments = mymsgboardBean.getComments();
        if (comments == null) {
            mymsgboardHolder.mMessagelistItemCommentsLienar.setVisibility(View.GONE);
            mymsgboardHolder.mMessagelistItemCommentsBootomLienar.setVisibility(View.GONE);
        } else {
            mymsgboardHolder.mMessagelistItemCommentsLienar.setVisibility(View.VISIBLE);
            mymsgboardHolder.mMessagelistItemCommentsBootomLienar.setVisibility(View.VISIBLE);
            if (comments == null) {
                mymsgboardHolder.mMessagelistItemCommentsLienar.setVisibility(View.GONE);
                mymsgboardHolder.mMessagelistItemCommentsBootomLienar.setVisibility(View.GONE);
            } else {
                commentsAddView(comments, mymsgboardHolder);
            }


        }


        return convertView;
    }

    private void commentsAddView(List<MymsgboardCommentsBean> commentsBeans, MymsgboardHolder mymsgboardHolder) {
        mymsgboardHolder.mMessagelistItemCommentsLienar.removeAllViews();
        if (commentsBeans.size() == 0) return;
        for (int i = 0; i < commentsBeans.size(); i++) {
            MymsgboardCommentsBean mymsgboardCommentsBean = commentsBeans.get(i);
            TextView textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.msg_comment_text,null);
            textView.setText(Html.fromHtml(
                    getTextFont(mymsgboardCommentsBean.getNickName().equals("null") ? "匿名" : mymsgboardCommentsBean.getNickName())
                    + getConnetTextFont(":" + mymsgboardCommentsBean.getContent())));
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            mymsgboardHolder.mMessagelistItemCommentsLienar.addView(textView);
        }

    }


    private String getTextFont(String text) {
        return "<font color='#30a6d3'>"+text+"</font> ";

    }

    private String getConnetTextFont(String text) {
        return "<font color='#323232'>"+text+"</font> ";

    }

    private boolean renturnNull(String text, View view) {
        if (text == null || "".equals(text)) {
            if (view != null)
                view.setVisibility(View.GONE);
            return false;
        } else {
            if (view != null)
                view.setVisibility(View.VISIBLE);
            return true;
        }

    }

    private boolean renturnSNull(String text, View view) {
        if (text == null || "null".equals(text)) {
            view.setVisibility(View.GONE);
            return false;
        } else {
            view.setVisibility(View.VISIBLE);
            return true;
        }

    }

    static class MymsgboardHolder {
        @InjectView(R.id.messagelist_item_photo)
        RoundImageView mMessagelistItemPhoto;
        @InjectView(R.id.messagelist_item_nickName)
        TextView mMessagelistItemNickName;
        @InjectView(R.id.messagelist_item_publicTime)
        TextView mMessagelistItemPublicTime;
        @InjectView(R.id.messagelist_item_content)
        EmojiShowTextView mMessagelistItemContent;
        @InjectView(R.id.messagelist_item_comments_lienar)
        LinearLayout mMessagelistItemCommentsLienar;
        @InjectView(R.id.messagelist_item_comments_bootom_lienar)
        View mMessagelistItemCommentsBootomLienar;

        MymsgboardHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
