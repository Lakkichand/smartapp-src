package com.youle.gamebox.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.api.pcenter.PCSignApi;
import com.youle.gamebox.ui.bean.User;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.CodeCheck;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.util.TOASTUtil;
import org.json.JSONObject;

/**
 * Created by Administrator on 2014/5/22.
 */
public class PCenterTopUserView extends LinearLayout implements View.OnClickListener {
    @InjectView(R.id.pcenter_laoyout_top_photo)
    ImageView mPcenterLaoyoutTopPhoto;
    @InjectView(R.id.pcenter_laoyout_top_userName)
    TextView mPcenterLaoyoutTopUserName;
    @InjectView(R.id.pcenter_laoyout_top_email)
    TextView mPcenterLaoyoutTopEmail;
    @InjectView(R.id.pcenter_laoyout_top_Integral)
    TextView mPcenterLaoyoutTopIntegral;
    Context mContext;
    @InjectView(R.id.pcenter_laoyout_top_qd)
    TextView mPcenterLaoyoutTopQd;
    @InjectView(R.id.sigle_icon)
    ImageView mSigleIcon;

    public PCenterTopUserView(Context context) {
        super(context);
        init(context);
    }

    public PCenterTopUserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public PCenterTopUserView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.pcenter_layout_top_userinfo, this);
        ButterKnife.inject(this);
        mPcenterLaoyoutTopPhoto.setOnClickListener(this);
        mPcenterLaoyoutTopQd.setOnClickListener(this);
    }

    public void setViewData(UserInfo user) {
        if (user == null) return;
        if (TextUtils.isEmpty(user.getSmallAvatarUrl())) {
            mPcenterLaoyoutTopPhoto.setImageResource(R.drawable.pc_user_photo);
        } else {
            ImageLoadUtil.displayAvatarImage(user.getSmallAvatarUrl(), mPcenterLaoyoutTopPhoto);

        }
        mPcenterLaoyoutTopUserName.setText(user.getNickName());
        boolean sign = user.getIsSign();
        if (sign) {
            mPcenterLaoyoutTopQd.setEnabled(false);
            mPcenterLaoyoutTopQd.setText("已签到");
            mPcenterLaoyoutTopQd.setPadding(0,0,0,0);
            mSigleIcon.setVisibility(GONE);
        } else {
            mPcenterLaoyoutTopQd.setEnabled(true);
            mPcenterLaoyoutTopQd.setText("签到");
            mSigleIcon.setVisibility(VISIBLE);
        }
        LOGUtil.e("PCenter",user.getScore());
        String html = getContext().getString(R.string.sigle_score,user.getScore());
        mPcenterLaoyoutTopIntegral.setText(Html.fromHtml(html));
        if(TextUtils.isEmpty(user.getContact())){
            mPcenterLaoyoutTopEmail.setVisibility(GONE);
        }else {
            mPcenterLaoyoutTopEmail.setVisibility(VISIBLE);
            mPcenterLaoyoutTopEmail.setText(user.getContact());
        }

    }

    @Override
    public void onClick(View v) {
        if (v == mPcenterLaoyoutTopPhoto) {
            Bundle homeBundle = new Bundle();
            UserInfo userInfo = new UserInfoCache().getUserInfo();
            if (userInfo != null) {
                CommonActivity.startOtherUserDetail(mContext, userInfo.getUid(), userInfo.getNickName());
            }
        } else if (v == mPcenterLaoyoutTopQd) {
            sign();
        }

    }


    private void sign() {
        String sid = new UserInfoCache().getSid();
        if (null == sid || "".equals(sid)) {
            TOASTUtil.showSHORT(mContext, R.string.pcenter_sign_falg);
        } else {
            PCSignApi pcSignApi = new PCSignApi();
            pcSignApi.setSid(sid);
            ZhidianHttpClient.request(pcSignApi, new JsonHttpListener(false) {
                @Override
                public void onRequestSuccess(String jsonString) {
                    super.onRequestSuccess(jsonString);
                    jsonRequest(jsonString);
                }


                @Override
                public void onResultFail(String jsonString) {
                    super.onResultFail(jsonString);
                    jsonRequest(jsonString);
                }
            });

        }
    }


    private void jsonRequest(String jsonString) {
        try {
            String code = CodeCheck.jsonToCode(jsonString, "code");
            if ("1000".equals(code)) {
                int score = new JSONObject(jsonString).getInt("data");
                //签到成功
                UserInfo userInfo = new UserInfoCache().getUserInfo();
                if (userInfo != null) {
                    userInfo.setIsSign(true);
                    userInfo.setScore(score);
                    new UserInfoCache().saveUserInfo(userInfo);
                    setViewData(userInfo);
                }

            } else if ("1001".equals(code)) {
                //服务器 错误

            } else if ("1100".equals(code)) {
                //sid无效，请登录在操作
                TOASTUtil.showSHORT(mContext, R.string.pcenter_sign_falg);
            } else if ("1102".equals(code)) {
                //今天已经签到过，不能重复签到
                mPcenterLaoyoutTopQd.setText("已签到");
                mPcenterLaoyoutTopQd.setEnabled(false);
            }
        } catch (Exception e) {

        }
    }
}
