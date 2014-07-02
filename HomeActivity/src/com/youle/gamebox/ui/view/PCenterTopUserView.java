package com.youle.gamebox.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.LogUserCache;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.api.pcenter.PCSignApi;
import com.youle.gamebox.ui.bean.User;
import com.youle.gamebox.ui.fragment.HomepageFragment;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.CodeCheck;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.util.TOASTUtil;

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
        mPcenterLaoyoutTopIntegral.setOnClickListener(this);
        mPcenterLaoyoutTopPhoto.setOnClickListener(this);
        mPcenterLaoyoutTopQd.setOnClickListener(this);
    }

    public void setViewData(User user){
        if(user==null) return;
        String bigAvatarUrl = user.getBigAvatarUrl();
        LOGUtil.d("junjun","--"+bigAvatarUrl);
        if(bigAvatarUrl==null || "null".equals(bigAvatarUrl)){
        mPcenterLaoyoutTopPhoto.setImageResource(R.drawable.pc_user_photo);
        }else{
        ImageLoadUtil.displayImage(bigAvatarUrl,mPcenterLaoyoutTopPhoto);
        }
        mPcenterLaoyoutTopUserName.setText(user.getUserName());
        boolean sign = user.isSign();
        if(sign){
            mPcenterLaoyoutTopQd.setEnabled(false);
            mPcenterLaoyoutTopQd.setText("已签到");
        }else{
            mPcenterLaoyoutTopQd.setEnabled(true);
            mPcenterLaoyoutTopQd.setText("签到");
        }

    }

    @Override
    public void onClick(View v) {
        if(v == mPcenterLaoyoutTopPhoto){
            Bundle homeBundle = new Bundle();
            UserInfo userInfo = new UserInfoCache().getUserInfo();
            if(userInfo!=null) {
                homeBundle.putString(HomepageFragment.KEY_TITLE, userInfo.getNickName());
                homeBundle.putLong(HomepageFragment.KEY_UID,userInfo.getUid());
                CommonActivity.startCommonA(mContext, CommonActivity.FRAGMENT_HOMEPAGE, homeBundle);
            }

        }else if(v == mPcenterLaoyoutTopIntegral){
            new UserInfoCache().delUserInfoTable();
        }else if(v == mPcenterLaoyoutTopQd){
            sign();
        }

    }


    private void sign(){
        String sid = new UserInfoCache().getSid();
        if(null == sid || "".equals(sid)){
            TOASTUtil.showSHORT(mContext,R.string.pcenter_sign_falg);
        }else{
        PCSignApi pcSignApi =new PCSignApi();
        pcSignApi.setSid(sid);
        ZhidianHttpClient.request(pcSignApi,new JsonHttpListener(false) {
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


    private void  jsonRequest(String jsonString){
        try {
            String code = CodeCheck.jsonToCode(jsonString, "code");
            if("1000".equals(code)){
                //签到成功
                mPcenterLaoyoutTopQd.setText("已签到");
                mPcenterLaoyoutTopQd.setEnabled(false);
                UserInfo userInfo = new UserInfoCache().getUserInfo();
                if(userInfo!=null){
                    userInfo.setIsSign(true);
                    new UserInfoCache().saveUserInfo(userInfo);
                }

            }else if("1001".equals(code)){
                //服务器 错误

            }else if("1100".equals(code)){
                //sid无效，请登录在操作
                TOASTUtil.showSHORT(mContext,R.string.pcenter_sign_falg);
            }else if("1102".equals(code)){
                //今天已经签到过，不能重复签到
                mPcenterLaoyoutTopQd.setText("已签到");
                mPcenterLaoyoutTopQd.setEnabled(false);
            }
        }catch (Exception e){

        }
    }
}
