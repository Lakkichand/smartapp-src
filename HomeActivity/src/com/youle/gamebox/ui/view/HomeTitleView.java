package com.youle.gamebox.ui.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.YouleAplication;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.activity.DownLoadManagerActivity;
import com.youle.gamebox.ui.activity.HomeActivity;
import com.youle.gamebox.ui.activity.SearchActivity;
import com.youle.gamebox.ui.bean.MessageNumberBean;
import com.youle.gamebox.ui.fragment.DownloadManagerFragment;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.util.DownLoadUtil;

/**
 * Created by Administrator on 14-7-3.
 */
public class HomeTitleView extends LinearLayout implements View.OnClickListener {

    @InjectView(R.id.rankShowLeft)
    RelativeLayout mRankShowLeft;
    @InjectView(R.id.title)
    TextView mTitle;
    @InjectView(R.id.editImage)
    ImageView mEditImage;
    @InjectView(R.id.titleSerch)
    ImageView mTitleSerch;
    @InjectView(R.id.downLoadManager)
    RelativeLayout mDownLoadManager;
    @InjectView(R.id.searchLayout)
    LinearLayout mSearchLayout;
    TitleType type = TitleType.HOME;
    @InjectView(R.id.downloadNumber)
    TextView mDownloadNumber;
    @InjectView(R.id.notRead)
    TextView mNotRead;

    public enum TitleType {
        CLASSFY, RANK, HOME, COMMUNITY, PERSEN
    }

    public HomeTitleView(Context context, TitleType t) {
        super(context);
        this.type = t;
        LayoutInflater.from(context).inflate(R.layout.rank_title_layout, this);
        ButterKnife.inject(this);
        if (type == TitleType.PERSEN) {
            UserInfo userInfo = new UserInfoCache().getUserInfo();
            mSearchLayout.setVisibility(GONE);
            if (userInfo != null) {
                mEditImage.setVisibility(VISIBLE);
            } else {
                mEditImage.setVisibility(GONE);
            }
        } else {
            mSearchLayout.setVisibility(VISIBLE);
            mEditImage.setVisibility(GONE);
        }
        mRankShowLeft.setOnClickListener(this);
        mTitleSerch.setOnClickListener(this);
        mEditImage.setOnClickListener(this);
        mDownLoadManager.setOnClickListener(this);
        initDownloadNumber();
        if(getNoteReadNumber()>0){
            mNotRead.setVisibility(VISIBLE);
            mNotRead.setText(getNoteReadNumber()+"");
        }else {
            mNotRead.setVisibility(GONE);
        }
    }

    public  void initDownloadNumber(){
        if (DownLoadUtil.getDownLoadingNumber() > 0) {
            mDownloadNumber.setVisibility(VISIBLE);
            mDownloadNumber.setText(DownLoadUtil.getDownLoadingNumber() + "");
        } else {
            mDownloadNumber.setVisibility(GONE);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rankShowLeft:
                SlidingPaneLayout layout = ((HomeActivity) getContext()).getSlidingLayout();
                if (layout.isOpen()) {
                    layout.closePane();
                } else {
                    layout.openPane();
                }
                break;
            case R.id.downLoadManager:
                Intent intent = new Intent(getContext(), DownLoadManagerActivity.class);
                intent.putExtra(DownLoadManagerActivity.TYPE, DownloadManagerFragment.DOWN);
                getContext().startActivity(intent);
                break;
            case R.id.titleSerch:
                Intent se = new Intent(getContext(), SearchActivity.class);
                getContext().startActivity(se);
                break;
            case R.id.editImage:
                CommonActivity.startCommonA(getContext(), CommonActivity.FRAGMENT_USERINFO, -1);
                break;
        }
    }

    private int getNoteReadNumber() {
        MessageNumberBean bean = YouleAplication.messageNumberBean;
        int number = 0;
        if (bean != null) {
            number += bean.getGmCount();
            number += bean.getMsgCount();
        }
        return number;
    }
}
