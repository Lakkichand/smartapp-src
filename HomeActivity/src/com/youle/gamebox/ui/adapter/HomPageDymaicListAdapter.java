package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.bean.dynamic.DymaicCommentsBean;
import com.youle.gamebox.ui.bean.dynamic.DymaicListBean;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.view.EmojiShowTextView;
import com.youle.gamebox.ui.view.RoundImageView;

import java.util.List;

/**
 * Created by Administrator on 2014/6/20.
 */
public class HomPageDymaicListAdapter extends YouleBaseAdapter<DymaicListBean>{


    private LayoutInflater inflater;
    public HomPageDymaicListAdapter(Context mContext, List<DymaicListBean> mList) {
        super(mContext, mList);
         inflater = LayoutInflater.from(mContext);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HomePageDymaicListHolder homePageDymaicListHolder = null;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.dymaiclist_item_layout,null);
            homePageDymaicListHolder = new HomePageDymaicListHolder(convertView);
            convertView.setTag(homePageDymaicListHolder);
        }else{
            homePageDymaicListHolder = (HomePageDymaicListHolder)convertView.getTag();
        }
        setItmeValue(homePageDymaicListHolder,position);
        return convertView;
    }

    private void setItmeValue(HomePageDymaicListHolder holder,int position){
        DymaicListBean dymaicListBean = mList.get(position);
        if(dymaicListBean!=null){
            if(dymaicListBean.getAvatarUrl()!=null && !"".equals(dymaicListBean.getAvatarUrl()) )
            ImageLoadUtil.displayImage(dymaicListBean.getAvatarUrl(), holder.mDymaiclistPhoto);
            holder.mDymaiclistNickName.setText(dymaicListBean.getNickName());
            holder.mDymaiclistPublicTime.setText(dymaicListBean.getTime());
            //发表内容
            if(dymaicListBean.getContent()!=null &&!"".equals(dymaicListBean.getContent()))
                holder.mDymaiclistContent.setFaceText(dymaicListBean.getContent());
            //语音
            if(renturnNull(dymaicListBean.getVoiceUrl(),holder.mDymaiclistVoiceSeconds))
                holder.mDymaiclistVoiceSeconds.setText(String.valueOf(dymaicListBean.getVoiceSeconds()));
            //图片
            if(renturnNull(dymaicListBean.getImageUrl(),holder.mDymaiclistImageUrl))
                ImageLoadUtil.displayImage(dymaicListBean.getImageUrl(), holder.mDymaiclistImageUrl);
            // 游戏
            LOGUtil.d("text","=============="+dymaicListBean.getIconUrl());
            if(renturnsNull(dymaicListBean.getName(),holder.mDymaiclistGameLinear)&& renturnNull(dymaicListBean.getIconUrl(),holder.mDymaiclistGameLinear)){
                ImageLoadUtil.displayImage(dymaicListBean.getIconUrl(), holder.mDymaiclistGameIcon);
                holder.mDymaiclistGameContent.setText(dymaicListBean.getExplain());
            }
            // 赞 ==
            String laund = String.valueOf(dymaicListBean.getlAmount());
            holder.mDymaiclistAmountLaund.setText(laund.equals("0")?"":laund);
            if(!"".equals(dymaicListBean.getPraiseNames())){
                holder.mDymaiclistAmountLaundText.setText(dymaicListBean.getPraiseNames());
                holder.mDymaiclistAmountLaundLinear.setVisibility(View.VISIBLE);

            }else{
                holder.mDymaiclistAmountLaundLinear.setVisibility(View.GONE);
            }
            String voice = String.valueOf(dymaicListBean.getvAmount());
            holder.mDymaiclistAmountVoice.setText(voice.equals("null")?"":voice);
            String comment = String.valueOf(dymaicListBean.gettAmount());
            holder.mDymaiclistAmountContent.setText(comment.equals("0")?"":comment);

            //评论
            List<DymaicCommentsBean> comments = dymaicListBean.getComments();
            if (comments==null){
                holder.mDymaiclistCommentsLinear.setVisibility(View.GONE);
                holder.mDymaiclistCommentsBotlinear.setVisibility(View.GONE);
            }else{
                holder.mDymaiclistCommentsLinear.setVisibility(View.VISIBLE);
                holder.mDymaiclistCommentsLinear.removeAllViews();
                //增加评论
                if(comments.size()<4){
                    holder.mDymaiclistCommentsBotlinear.setVisibility(View.GONE);
                }else{
                    holder.mDymaiclistCommentsBotlinear.setVisibility(View.VISIBLE);
                }
                for (int i = 0; i <comments.size() ; i++) {
                    if(i>2){
                        break;
                    }
                    DymaicCommentsBean dymaicCommentsBean = comments.get(i);
                    commentsAddView(dymaicCommentsBean,holder.mDymaiclistCommentsLinear);
                }
                if(dymaicListBean.getComments().size()>3){
                    holder.mDymaiclistCommentsBotlinear.setVisibility(View.VISIBLE);
                }else{
                    holder.mDymaiclistCommentsBotlinear.setVisibility(View.GONE);
                }

            }
        }

    }

    //add comments
    private void commentsAddView(DymaicCommentsBean dymaicCommentsBean,LinearLayout linearLayout){
        if (1 == dymaicCommentsBean.getLevel()){
            //动态评论
            TextView cTextView = new TextView(mContext);
            if(1 == dymaicCommentsBean.getType()){
                //文字

                cTextView.setText(Html.fromHtml(getTextFont(dymaicCommentsBean.getNickName())+" :" + dymaicCommentsBean.getContent()));
                linearLayout.addView(cTextView);
            }else{
                //语音
                View inflate = inflater.inflate(R.layout.dymaic_item_voice_layout, null);
                TextView cName = (TextView)inflate.findViewById(R.id.dymaiclist_item_name);
                TextView cVoice = (TextView)inflate.findViewById(R.id.dymaiclist_item_voiceSeconds);
                cName.setText(Html.fromHtml(getTextFont(dymaicCommentsBean.getNickName())+ " :"));
                cVoice.setText(String.valueOf(dymaicCommentsBean.getVoiceSeconds()+"''"));
                linearLayout.addView(inflate);


            }

        }else if(2 == dymaicCommentsBean.getLevel()){
            // 评论 回复
            TextView cTextView = new TextView(mContext);
            if(1 == dymaicCommentsBean.getType()){
                //文字
                cTextView.setText(Html.fromHtml(getTextFont(dymaicCommentsBean.getrNickName().equals("null")?"匿名":dymaicCommentsBean.getrNickName()) +" 回复 "
                        +getTextFont(dymaicCommentsBean.getNickName())+" :"+getConnetTextFont(dymaicCommentsBean.getContent())));
                linearLayout.addView(cTextView);
            }else{
                //语音
                View inflate = inflater.inflate(R.layout.dymaic_item_voice_layout, null);
                TextView cName = (TextView)inflate.findViewById(R.id.dymaiclist_item_name);
                TextView cVoice = (TextView)inflate.findViewById(R.id.dymaiclist_item_voiceSeconds);
                cName.setText(Html.fromHtml(getTextFont(dymaicCommentsBean.getrNickName().equals("null")?"匿名":dymaicCommentsBean.getrNickName()) +" 回复 "
                        +getTextFont(dymaicCommentsBean.getNickName())+" :"));
                cVoice.setText(String.valueOf(dymaicCommentsBean.getVoiceSeconds() + "''"));
                linearLayout.addView(inflate);
            }

        }
    }

    private String getTextFont(String text){
        return "<font color='#30a6d3'>"+text+"</font> ";

    }
    private String getConnetTextFont(String text){
        return "<font color='#323232'>"+text+"</font> ";

    }

    static class HomePageDymaicListHolder{
        @InjectView(R.id.dymaiclist_photo)
        RoundImageView mDymaiclistPhoto;
        @InjectView(R.id.dymaiclist_nickName)
        TextView mDymaiclistNickName;
        @InjectView(R.id.dymaiclist_publicTime)
        TextView mDymaiclistPublicTime;
        @InjectView(R.id.dymaiclist_content)
        EmojiShowTextView mDymaiclistContent;
        @InjectView(R.id.dymaiclist_voiceSeconds)
        TextView mDymaiclistVoiceSeconds;
        @InjectView(R.id.dymaiclist_imageUrl)
        ImageView mDymaiclistImageUrl;
        @InjectView(R.id.dymaiclist_gameIcon)
        ImageView mDymaiclistGameIcon;
        @InjectView(R.id.dymaiclist_gameContent)
        TextView mDymaiclistGameContent;
        @InjectView(R.id.dymaiclist_game_linear)
        LinearLayout mDymaiclistGameLinear;
        @InjectView(R.id.dymaiclist_amount_laund)
        TextView mDymaiclistAmountLaund;
        @InjectView(R.id.dymaiclist_amount_voice)
        TextView mDymaiclistAmountVoice;
        @InjectView(R.id.dymaiclist_amount_content)
        TextView mDymaiclistAmountContent;
        @InjectView(R.id.dymaiclist_amount_line)
        View mDymaiclistAmountLine;
        @InjectView(R.id.dymaiclist_amount_linear)
        RelativeLayout mDymaiclistAmountLinear;
        @InjectView(R.id.dymaiclist_amount_laundText)
        TextView mDymaiclistAmountLaundText;
        @InjectView(R.id.dymaiclist_amount_laundLinear)
        LinearLayout mDymaiclistAmountLaundLinear;
        @InjectView(R.id.dymaiclist_comments_linear)
        LinearLayout mDymaiclistCommentsLinear;
        @InjectView(R.id.dymaiclist_comments_readAll)
        TextView mDymaiclistCommentsReadAll;
        @InjectView(R.id.dymaiclist_comments_Botlinear)
        LinearLayout mDymaiclistCommentsBotlinear;
        HomePageDymaicListHolder(View view) {
            ButterKnife.inject(this,view);
        }
    }

    //

    private boolean renturnNull(String text,View view){
        if(text ==null || "".equals(text)){
            view.setVisibility(View.GONE);
            return false;
        }else{
            view.setVisibility(View.VISIBLE);
            return true;
        }

    }
    private boolean renturnsNull(String text,View view){
        if(text ==null || "null".equals(text)){
            view.setVisibility(View.GONE);
            return false;
        }else{
            view.setVisibility(View.VISIBLE);
            return true;
        }

    }
}
