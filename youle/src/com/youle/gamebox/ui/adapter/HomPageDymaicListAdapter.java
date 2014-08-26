package com.youle.gamebox.ui.adapter;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.handmark.pulltorefresh.library.internal.Utils;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.*;
import com.youle.gamebox.ui.api.dynamic.DeleteCommentApi;
import com.youle.gamebox.ui.api.dynamic.PraiseApi;
import com.youle.gamebox.ui.bean.dynamic.DymaicCommentsBean;
import com.youle.gamebox.ui.bean.dynamic.DymaicListBean;
import com.youle.gamebox.ui.fragment.BigImageFragment;
import com.youle.gamebox.ui.fragment.HomePageDymaicListFragment;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.util.RecoderVoice;
import com.youle.gamebox.ui.util.UIUtil;
import com.youle.gamebox.ui.view.*;

/**
 * Created by Administrator on 2014/6/20.
 */
public class HomPageDymaicListAdapter extends YouleBaseAdapter<DymaicListBean> implements RecoderVoice.IPlayListener {

    private OnComentClickListenr clickListener;
    private LayoutInflater inflater;
    private View playCommentView;
    private View playView;

    @Override
    public void startPlay(File file) {
        startPlayView();
    }

    @Override
    public void playing(File file) {
    }

    @Override
    public void endPlay(File file) {
        resetPlayView();
    }

    private void startPlayView() {
        if(playCommentView!=null){
            PlayView playSmall = (PlayView) playCommentView.findViewById(R.id.playing_image);
            playSmall.startPlay();
        }
        if(playView!=null){
            PlayView playVoiceBig = (PlayVoiceBig) playView.findViewById(R.id.playing_image);
            playVoiceBig.startPlay();
        }
    }

    private void resetPlayView() {
        if(playCommentView!=null){
            PlayView playSmall = (PlayView) playCommentView.findViewById(R.id.playing_image);
            playSmall.endPlay();
        }
        if(playView!=null){
            PlayView playVoiceBig = (PlayVoiceBig) playView.findViewById(R.id.playing_image);
            playVoiceBig.endPlay();
        }
    }

    public interface OnComentClickListenr {
        public void onComentClick(DymaicListBean b, PublishDyView.SendModel model);

        public void onRepyClick(DymaicListBean d, DymaicCommentsBean c);

        public void onVoiceClick(String url,RecoderVoice.IPlayListener listener);
    }

    public HomPageDymaicListAdapter(Context mContext, List<DymaicListBean> mList, OnComentClickListenr listenr) {
        super(mContext, mList);
        this.clickListener = listenr;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HomePageDymaicListHolder homePageDymaicListHolder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.dymaiclist_item_layout, null);
            homePageDymaicListHolder = new HomePageDymaicListHolder(convertView);
            convertView.setTag(homePageDymaicListHolder);
        } else {
            homePageDymaicListHolder = (HomePageDymaicListHolder) convertView.getTag();
        }
        setItmeValue(homePageDymaicListHolder, position);
        return convertView;
    }

    private void setItmeValue(final HomePageDymaicListHolder holder, int position) {
        DymaicListBean dymaicListBean = mList.get(position);
        if (dymaicListBean != null) {
            ImageLoadUtil.displayAvatarImage(dymaicListBean.getAvatarUrl(), holder.mDymaiclistPhoto);
            holder.mDymaiclistPhoto.setTag(dymaicListBean);
            holder.mDymaiclistPhoto.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    DymaicListBean d = (DymaicListBean) v.getTag();
                    CommonActivity.startOtherUserDetail(mContext, d.getUid(), d.getNickName());
                }
            });
            holder.mDymaiclistNickName.setText(dymaicListBean.getNickName());
            holder.mDymaiclistPublicTime.setText(dymaicListBean.getTime());
            //发表内容
            if (TextUtils.isEmpty(dymaicListBean.getContent())) {
                holder.mDymaiclistContent.setVisibility(View.GONE);
            } else {
                holder.mDymaiclistContent.setVisibility(View.VISIBLE);
                holder.mDymaiclistContent.setFaceText(dymaicListBean.getContent());
            }
            //删除按钮
            if (dymaicListBean.getIsOwn()) {
                holder.mDeleteText.setVisibility(View.VISIBLE);
                holder.mDeleteText.setTag(dymaicListBean);
                holder.mDeleteText.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final DymaicListBean b = (DymaicListBean) v.getTag();
                        DeleteCommentApi deleteCommentApi = new DeleteCommentApi();
                        deleteCommentApi.did = b.getId() + "";
                        deleteCommentApi.sid = new UserInfoCache().getSid();
                        ZhidianHttpClient.request(deleteCommentApi, new JsonHttpListener(false) {
                            @Override
                            public void onRequestSuccess(String jsonString) {
                                UIUtil.toast(getContext(), "删除动态成功");
                                mList.remove(b);
                                notifyDataSetChanged();
                            }

                            @Override
                            public void onResultFail(String jsonString) {
                                UIUtil.toast(getContext(), "删除动态失败");
                            }
                        });
                    }
                });
            } else {
                holder.mDeleteText.setVisibility(View.GONE);
            }
            //语音
            if (TextUtils.isEmpty(dymaicListBean.getVoiceUrl())) {
                holder.voiceLayout.setVisibility(View.GONE);
            } else {
                holder.voiceLayout.setVisibility(View.VISIBLE);
                holder.mDymaiclistVoiceSeconds.setText(dymaicListBean.getVoiceSeconds() / 1000 + "'");
                holder.voiceLayout.setTag(dymaicListBean);
                holder.voiceLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DymaicListBean d = (DymaicListBean) v.getTag();
                        if (clickListener != null) {
                            resetPlayView();
                            playView=holder.voiceLayout;
                            playCommentView=null ;
                            clickListener.onVoiceClick(d.getVoiceUrl(),HomPageDymaicListAdapter.this);
                        }
                    }
                });
            }
            //图片
            if (TextUtils.isEmpty(dymaicListBean.getImageThumbnailUrl())) {
                holder.mDymaiclistImageUrl.setVisibility(View.GONE);
            } else {
                holder.mDymaiclistImageUrl.setVisibility(View.VISIBLE);
                ImageLoadUtil.displayNotRundomImage(dymaicListBean.getImageThumbnailUrl(), holder.mDymaiclistImageUrl);
                holder.mDymaiclistImageUrl.setTag(dymaicListBean);
                holder.mDymaiclistImageUrl.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DymaicListBean d = (DymaicListBean) v.getTag();
                        DisplayBigImageActivity.startDisplayImage(mContext, d.getImageUrl());
                    }
                });
            }
            // 游戏
            if (TextUtils.isEmpty(dymaicListBean.getIconUrl())) {
                holder.mDymaiclistGameLinear.setVisibility(View.GONE);
            } else {
                holder.mDymaiclistGameLinear.setVisibility(View.VISIBLE);
                ImageLoadUtil.displayImage(dymaicListBean.getIconUrl(), holder.mDymaiclistGameIcon);
                holder.mDymaiclistGameContent.setText(dymaicListBean.getExplain());
                holder.mDymaiclistGameLinear.setTag(dymaicListBean);
                holder.mDymaiclistGameLinear.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DymaicListBean b = (DymaicListBean) v.getTag();
                        GameDetailActivity.startGameDetailActivity(mContext, b.getAppId(), b.getName(), b.getSource());
                    }
                });
            }
            // 赞 ==
            String laund = String.valueOf(dymaicListBean.getlAmount());
            holder.mDymaiclistAmountLaund.setText(laund.equals("0") ? "" : laund);
            holder.mDymaiclistAmountLaund.setTag(dymaicListBean);
            holder.mDymaiclistAmountLaund.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    final DymaicListBean b = (DymaicListBean) v.getTag();
                    final UserInfo userInfo = new UserInfoCache().getUserInfo();
                    if (userInfo != null) {
                        final PraiseApi praiseApi = new PraiseApi();
                        praiseApi.setSid(userInfo.getSid());
                        praiseApi.setDid(b.getId());
                        if (b.getIsLike()) {
                            praiseApi.setType(PraiseApi.NOT_AGREE);
                        } else {
                            praiseApi.setType(praiseApi.AGREE);
                        }
                        ZhidianHttpClient.request(praiseApi, new JsonHttpListener(false) {
                            @Override
                            public void onRequestSuccess(String jsonString) {
                                super.onRequestSuccess(jsonString);
                                if (b.getIsLike()) {
                                    b.setIsLike(false);
                                    UIUtil.toast(mContext, R.string.cancelPraise);
                                    String newp = b.getPraiseNames().replace("，" + userInfo.getNickName(), "").replace(userInfo.getNickName(), "");
                                    b.setPraiseNames(newp);
                                    b.setlAmount(b.getlAmount() - 1);
                                } else {
                                    UIUtil.toast(mContext, R.string.praise_success);
                                    b.setIsLike(true);
                                    if (!TextUtils.isEmpty(b.getPraiseNames())) {
                                        String newp = b.getPraiseNames() + "，" + userInfo.getNickName();
                                        b.setPraiseNames(newp);
                                    } else {
                                        b.setPraiseNames(userInfo.getNickName());
                                    }
                                    b.setlAmount(b.getlAmount() + 1);
                                }
                                notifyDataSetChanged();
                            }
                        });
                    } else {
                        CommonActivity.startCommonA(mContext, CommonActivity.FRAGMENT_LOGIN, -1);
                    }
                }
            });
            if (!TextUtils.isEmpty(dymaicListBean.getPraiseNames())) {
                String html = mContext.getString(R.string.parise_format, dymaicListBean.getPraiseNames(), dymaicListBean.getlAmount());
                holder.mDymaiclistAmountLaundText.setText(Html.fromHtml(html));
                holder.pairLine.setVisibility(View.VISIBLE);
                holder.mDymaiclistAmountLaundLinear.setVisibility(View.VISIBLE);

            } else {
                holder.mDymaiclistAmountLaundLinear.setVisibility(View.GONE);
                holder.pairLine.setVisibility(View.GONE);
            }
            int voice = dymaicListBean.getvAmount();
            if (voice > 0) {
                holder.mDymaiclistAmountVoice.setText(voice + "");
            } else {
                holder.mDymaiclistAmountVoice.setText("");
            }
            holder.mDymaiclistAmountVoice.setTag(dymaicListBean);
            holder.mDymaiclistAmountVoice.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO 声音评论
                    if (clickListener != null) {
                        clickListener.onComentClick((DymaicListBean) v.getTag(), PublishDyView.SendModel.VOICE);
                    }
                }
            });
            if(dymaicListBean.gettAmount()>0) {
                holder.mDymaiclistAmountContent.setText(dymaicListBean.gettAmount()+"");
            }else {
                holder.mDymaiclistAmountContent.setText( "");
            }
            holder.mDymaiclistAmountContent.setTag(dymaicListBean);
            holder.mDymaiclistAmountContent.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onComentClick((DymaicListBean) v.getTag(), PublishDyView.SendModel.TEXT);
                    }
                }
            });

            //评论
            List<DymaicCommentsBean> comments = dymaicListBean.getComments();
            if (comments == null) {
                holder.mDymaiclistCommentsLinear.setVisibility(View.GONE);
                holder.mDymaiclistCommentsBotlinear.setVisibility(View.GONE);
            } else {
                holder.mDymaiclistCommentsLinear.setVisibility(View.VISIBLE);
                holder.mDymaiclistCommentsLinear.removeAllViews();
                for (int i = 0; i < comments.size(); i++) {
                    DymaicCommentsBean dymaicCommentsBean = comments.get(i);
                    commentsAddView(dymaicListBean, dymaicCommentsBean, holder.mDymaiclistCommentsLinear);
                }
            }
        }
    }

    //add comments
    private void commentsAddView(DymaicListBean d, DymaicCommentsBean dymaicCommentsBean, LinearLayout linearLayout) {
        if (1 == dymaicCommentsBean.getLevel()) {
            //动态评论
            EmojiShowTextView cTextView = (EmojiShowTextView) inflater.inflate(R.layout.comment_text_item, null);
            cTextView.setTag(R.string.c_tag, dymaicCommentsBean);
            cTextView.setTag(R.string.d_tag, d);
            cTextView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    DymaicCommentsBean c = (DymaicCommentsBean) v.getTag(R.string.c_tag);
                    DymaicListBean d = (DymaicListBean) v.getTag(R.string.d_tag);
                    if (clickListener != null) {
                        clickListener.onRepyClick(d, c);
                    }
                }
            });
            if (1 == dymaicCommentsBean.getType()) {
                //文字
                cTextView.setFaceText((getTextFont(dymaicCommentsBean.getNickName()) + " :" + dymaicCommentsBean.getContent()));
                linearLayout.addView(cTextView);
            } else {
                //语音
                View inflate = inflater.inflate(R.layout.dymaic_item_voice_layout, null);
                TextView cName = (TextView) inflate.findViewById(R.id.dymaiclist_item_name);
                TextView cVoice = (TextView) inflate.findViewById(R.id.dymaiclist_item_voiceSeconds);
                View  vLayout = inflate.findViewById(R.id.voiceLayout);
                vLayout.setTag(dymaicCommentsBean);
                vLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DymaicCommentsBean b = (DymaicCommentsBean) v.getTag();
                        if (clickListener != null) {
                            resetPlayView();
                            playCommentView=v ;
                            playView=null ;
//                            v.findViewById(R.id.normal_image).setVisibility(View.GONE);
//                            v.findViewById(R.id.playing_image).setVisibility(View.VISIBLE);
                            clickListener.onVoiceClick(b.getVoiceUrl(),HomPageDymaicListAdapter.this);
                        }
                    }
                });
                cName.setText(Html.fromHtml(getTextFont(dymaicCommentsBean.getNickName()) + " :"));
                cVoice.setText(String.valueOf(dymaicCommentsBean.getVoiceSeconds() / 1000 + "'"));
                linearLayout.addView(inflate);
                inflate.setTag(R.string.c_tag, dymaicCommentsBean);
                inflate.setTag(R.string.d_tag, d);
                inflate.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DymaicCommentsBean c = (DymaicCommentsBean) v.getTag(R.string.c_tag);
                        DymaicListBean d = (DymaicListBean) v.getTag(R.string.d_tag);
                        if (clickListener != null) {
                            clickListener.onRepyClick(d, c);
                        }
                    }
                });

            }

        } else if (2 == dymaicCommentsBean.getLevel()) {
            // 评论 回复
            EmojiShowTextView cTextView = (EmojiShowTextView) inflater.inflate(R.layout.comment_text_item, null);
            if (1 == dymaicCommentsBean.getType()) {
                //文字
                cTextView.setFaceText(getTextFont(dymaicCommentsBean.getNickName()) + " 回复 "
                        + getTextFont(dymaicCommentsBean.getrNickName()) + " :" + getConnetTextFont(dymaicCommentsBean.getContent()));
                linearLayout.addView(cTextView);
                cTextView.setTag(R.string.c_tag, dymaicCommentsBean);
                cTextView.setTag(R.string.d_tag, d);
                cTextView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DymaicCommentsBean b = (DymaicCommentsBean) v.getTag(R.string.c_tag);
                        DymaicListBean d = (DymaicListBean) v.getTag(R.string.d_tag);
                        if (clickListener != null) {
                            clickListener.onRepyClick(d, b);
                        }
                    }
                });
            } else {
                //语音
                View inflate = inflater.inflate(R.layout.dymaic_item_voice_layout, null);
                inflate.setTag(R.string.c_tag, dymaicCommentsBean);
                inflate.setTag(R.string.d_tag, d);
                inflate.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DymaicCommentsBean b = (DymaicCommentsBean) v.getTag(R.string.c_tag);
                        DymaicListBean d = (DymaicListBean) v.getTag(R.string.d_tag);
                        if (clickListener != null) {
                            clickListener.onRepyClick(d, b);
                        }
                    }
                });
                TextView cName = (TextView) inflate.findViewById(R.id.dymaiclist_item_name);
                TextView cVoice = (TextView) inflate.findViewById(R.id.dymaiclist_item_voiceSeconds);
                String nameHtm = mContext.getString(R.string.dy_reply_format,dymaicCommentsBean.getNickName(),dymaicCommentsBean.getrNickName());
                cName.setText(Html.fromHtml(nameHtm));
                cVoice.setText(dymaicCommentsBean.getVoiceSeconds()/1000 + "'");
                linearLayout.addView(inflate);
                View  vLayout = inflate.findViewById(R.id.voiceLayout);
                vLayout.setTag(dymaicCommentsBean);
                vLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DymaicCommentsBean b = (DymaicCommentsBean) v.getTag();
                        if (clickListener != null) {
                            resetPlayView();
                            playCommentView=v ;
                            playView=null ;
                            clickListener.onVoiceClick(b.getVoiceUrl(),HomPageDymaicListAdapter.this);
                        }
                    }
                });
            }

        }
    }

    private String getTextFont(String text) {
        return "<font color='#30a6d3'>" + text + "</font> ";

    }

    private String getConnetTextFont(String text) {
        return "<font color='#323232'>" + text + "</font> ";

    }

    static class HomePageDymaicListHolder {
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
        @InjectView(R.id.pair_line)
        View pairLine;
        @InjectView(R.id.delete)
        TextView mDeleteText;
        @InjectView(R.id.voiceLayout)
        View voiceLayout;

        HomePageDymaicListHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

}
