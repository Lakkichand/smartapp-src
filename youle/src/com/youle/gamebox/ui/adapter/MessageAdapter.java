package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.YouleAplication;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.bean.MessageBean;
import com.youle.gamebox.ui.fragment.WebViewFragment;
import com.youle.gamebox.ui.view.EmojiShowTextView;

import java.util.List;

/**
 * Created by Administrator on 14-6-26.
 */
public class MessageAdapter extends YouleBaseAdapter<MessageBean> {
    private  IMessageRead iMessageRead ;
    public MessageAdapter(Context mContext, List<MessageBean> mList,IMessageRead messageRead) {
        super(mContext, mList);
        this.iMessageRead = messageRead ;
    }
    public interface IMessageRead{
        public void readOneMessage() ;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ButterknifeViewHolder holder = null;
        MessageBean messageBean = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.message_item, null);
            holder = new ButterknifeViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ButterknifeViewHolder) convertView.getTag();
        }
        CharSequence text = getContentText(messageBean);
        holder.mTitle.setFaceText(messageBean.getMsgTitle() + text);
        holder.mDate.setText(messageBean.getTime());
        if (!TextUtils.isEmpty(messageBean.getHttpUrl())) {
            holder.tvCheckDetail.setVisibility(View.VISIBLE);
            holder.tvCheckDetail.setText(Html.fromHtml("<u>" + "查看详情" + "</u>"));
            holder.tvCheckDetail.setTag(messageBean);
            holder.tvCheckDetail.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    MessageBean m = (MessageBean) v.getTag();
                    WebViewFragment webViewFragment = new WebViewFragment("消息详情",m.getHttpUrl());
                    ((BaseActivity)getContext()).addFragment(webViewFragment,true);
                }
            });
        } else {
            holder.tvCheckDetail.setVisibility(View.GONE);
        }

        boolean isRead = messageBean.isRead();
        if (isRead) {
            holder.imageQQ.setImageResource(R.drawable.qq_c);
            holder.mTitle.setTextColor(mContext.getResources().getColor(
                    R.color.no_read_text));
            holder.mDate.setTextColor(mContext.getResources().getColor(
                    R.color.no_read_date));
            holder.tvCheckDetail.setTextColor(mContext.getResources().getColor(
                    R.color.line_yello));
        } else {
            holder.imageQQ.setImageResource(R.drawable.qq_f);
            holder.mTitle.setTextColor(mContext.getResources().getColor(
                    R.color.read_text));
            holder.mDate.setTextColor(mContext.getResources().getColor(
                    R.color.read_date));
            holder.tvCheckDetail.setTextColor(mContext.getResources().getColor(
                    R.color.read_button));
        }

        if (position == 0) {
            holder.view.setVisibility(View.GONE);

        } else {
            holder.view.setVisibility(View.VISIBLE);

        }
        return convertView;
    }

    private CharSequence getContentText(MessageBean messageBean) {
        if (neadTitle(messageBean.getMsgType())) {
            return messageBean.getContent();
        } else if (messageBean.getMsgType() == 43) {//动态回复
            if (TextUtils.isEmpty(messageBean.getDynamicTime())) {
                return getContext().getString(R.string.message_reply_format, messageBean.getUserName(), messageBean.getContent(),"");
            } else {
                return getContext().getString(R.string.message_voice_reply, messageBean.getUserName(), messageBean.getDynamicTime(),"发布的动态");
            }
        } else if (messageBean.getMsgType() == 42) {//动态评论
            if (TextUtils.isEmpty(messageBean.getDynamicTime())) {
                return getContext().getString(R.string.message_comment_fomat, messageBean.getUserName(), messageBean.getContent(),"");
            } else {
                return getContext().getString(R.string.message_comment_fomat, messageBean.getUserName(), messageBean.getDynamicTime(),"发布的动态");
            }
        }else if(messageBean.getMsgType()==44){//留言
            return getContext().getString(R.string.message_bord_fomat, messageBean.getUserName());
        }else if(messageBean.getMsgType()==45){
            return getContext().getString(R.string.message_bord_reply_fomat, messageBean.getUserName(), messageBean.getContent(),"");
        }else if(messageBean.getMsgType()==46){
            if(TextUtils.isEmpty(messageBean.getDynamicTime())) {
                return getContext().getString(R.string.message_like_fomat, messageBean.getUserName(), messageBean.getContent());
            }else {
                return getContext().getString(R.string.message_like_fomat, messageBean.getUserName(), messageBean.getDynamicTime());
            }
        }else  if(messageBean.getMsgType()==41){
            return getContext().getString(R.string.message_private_fomat,messageBean.getUserName(),messageBean.getContent());
        }else if(messageBean.getMsgType()==40){
            return getContext().getString(R.string.message_private_fomat1,messageBean.getUserName(),messageBean.getContent());
        }
        return messageBean.getContent();
    }

    private boolean neadTitle(int type) {
        if (type == 2) return true;
        if (type == 3) return true;
        if (type == 5) return true;
        if (type == 7) return true;
        if (type == 8) return true;
        return false;
    }

    /**
     * This class contains all butterknife-injected Views & Layouts from layout
     * file 'null' for easy to all layout elements.
     *
     * @author Android Butter Zelezny, plugin for IntelliJ IDEA/Android Studio
     *         by Inmite (www.inmite.eu)
     */
    static class ButterknifeViewHolder {
        @InjectView(R.id.title)
        EmojiShowTextView mTitle;
        @InjectView(R.id.date)
        TextView mDate;
        @InjectView(R.id.check_detail)
        TextView tvCheckDetail;// 查看详情
        @InjectView(R.id.message_item_qq)
        ImageView imageQQ; // 圈圈
        @InjectView(R.id.message_item_top)
        View view; // 顶部的竖线

        ButterknifeViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
