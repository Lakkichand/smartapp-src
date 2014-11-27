package com.youle.gamebox.ui.fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserCache;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.ComunityActivity;
import com.youle.gamebox.ui.activity.HomeActivity;
import com.youle.gamebox.ui.api.CheckUpdataApi;
import com.youle.gamebox.ui.api.pcenter.PCLoginApi;
import com.youle.gamebox.ui.bean.LogAccount;
import com.youle.gamebox.ui.bean.UpdataBean;
import com.youle.gamebox.ui.bean.pcenter.PCUserInfoBean;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.*;
import com.youle.gamebox.ui.view.HomeTitleView;
import com.youle.gamebox.ui.view.UpdataDialog;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-5-12.
 */
public class IndexFragment extends BaseFragment implements View.OnClickListener,IndexHomeFragment.InotReadListener {
    @InjectView(R.id.rankLayout)
    LinearLayout mRankLayout;
    @InjectView(R.id.classfyLayout)
    LinearLayout mClassfyLayout;
    @InjectView(R.id.homeLayout)
    LinearLayout mHomeLayout;
    @InjectView(R.id.countryLayout)
    LinearLayout mCountryLayout;
    @InjectView(R.id.personLayout)
    LinearLayout mPersonLayout;
    @InjectView(R.id.tablayout)
    LinearLayout mTablayout;
    @InjectView(R.id.tabHost)
    TabHost mTabHost;
    @InjectView(R.id.titleLayout)
    LinearLayout titleLayout;

    @Override
    protected int getViewId() {
        return R.layout.index_fragment;
    }

    @Override
    protected String getModelName() {
        return "游乐游戏中心";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTabHost.setup();
        IndexHomeFragment fragment = (IndexHomeFragment) getFragmentManager().findFragmentById(R.id.home);
        fragment.setListener(this);
        mTabHost.addTab(mTabHost.newTabSpec("home").setIndicator("分类").setContent(R.id.classfy));
        mTabHost.addTab(mTabHost.newTabSpec("feed").setIndicator("排行").setContent(R.id.rank));
        mTabHost.addTab(mTabHost.newTabSpec("explore").setIndicator("主页").setContent(R.id.home));
        mTabHost.addTab(mTabHost.newTabSpec("setting").setIndicator("个人中心").setContent(R.id.pCenter));
        mTabHost.setCurrentTab(2);
        loadData();
        mHomeLayout.setSelected(true);
        ((HomeActivity) getActivity()).setmTabLayout(mTablayout);
        currentHomeTitleView = new HomeTitleView(getActivity(), HomeTitleView.TitleType.HOME);
        setTitleView(currentHomeTitleView);
        autoLogin();
        ((HomeActivity)getActivity()).setmIndexFragment(this);
        checkUpdate();
    }

    private void checkUpdate() {
        CheckUpdataApi checkUpdataApi = new CheckUpdataApi();
        try {
            PackageInfo pi=getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            checkUpdataApi.setChannelCode(SDKUtils.getChanalID(getActivity()));
            checkUpdataApi.setVersionCode(pi.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ZhidianHttpClient.request(checkUpdataApi,new JsonHttpListener(false){
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    UpdataBean upBean = jsonToBean(UpdataBean.class,jsonString);
                    if(upBean!=null){
                        new UpdataDialog(getActivity(),upBean).show();
                    }else {
//                        new UpdataDialog(getActivity()).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void loadData() {
        mRankLayout.setOnClickListener(this);
        mClassfyLayout.setOnClickListener(this);
        mHomeLayout.setOnClickListener(this);
        mCountryLayout.setOnClickListener(this);
        mPersonLayout.setOnClickListener(this);
    }


    private void resetTab() {
        mRankLayout.setSelected(false);
        mClassfyLayout.setSelected(false);
        mHomeLayout.setSelected(false);
        mCountryLayout.setSelected(false);
        mPersonLayout.setSelected(false);
    }
    public void setTitleView(View view){
        titleLayout.removeAllViews();
        titleLayout.addView(view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rankLayout:
                resetTab();
                mTabHost.setCurrentTab(1);
                mRankLayout.setSelected(true);
                setTitleView(new HomeTitleView(getActivity(), HomeTitleView.TitleType.RANK));
                break;
            case R.id.classfyLayout:
                resetTab();
                mTabHost.setCurrentTab(0);
                mClassfyLayout.setSelected(true);
                setTitleView(new HomeTitleView(getActivity(), HomeTitleView.TitleType.CLASSFY));
                break;
            case R.id.homeLayout:
                resetTab();
                mTabHost.setCurrentTab(2);
                mHomeLayout.setSelected(true);
                setTitleView(new HomeTitleView(getActivity(), HomeTitleView.TitleType.HOME));
                break;
            case R.id.countryLayout:
                Intent intent = new Intent(getActivity(), ComunityActivity.class);
                startActivity(intent);
                break;
            case R.id.personLayout:
                resetTab();
                mTabHost.setCurrentTab(3);
                mPersonLayout.setSelected(true);
                setTitleView(new HomeTitleView(getActivity(), HomeTitleView.TitleType.PERSEN));
                break;
        }
    }
    HomeTitleView currentHomeTitleView ;
    @Override
    public void onResume() {
        super.onResume();
        if(mTabHost.getCurrentTab()==3){
            currentHomeTitleView = new HomeTitleView(getActivity(),HomeTitleView.TitleType.PERSEN);
            setTitleView(currentHomeTitleView);
        }else {
            currentHomeTitleView = new HomeTitleView(getActivity(),HomeTitleView.TitleType.HOME);
            setTitleView(currentHomeTitleView);
        }
    }

    public void initDownLoadNumber(){
        if(mTabHost.getCurrentTab()!=3){
           currentHomeTitleView.initDownloadNumber();
        }
    }

    @Override
    public void notifyNotRead() {
       setTitleView(new HomeTitleView(getActivity(), HomeTitleView.TitleType.HOME));
    }

    /**
     *  启动就自动登录
     */
    private void autoLogin() {
        UserInfo userInfo = new UserInfoCache().getUserInfo();
        if (userInfo == null) {
            return;
        }

        //logAccountTemporary 用于临时保存自动登陆用户的信息
        LogAccount logAccountTemporary = null;

        //遍历取出上次登陆的账号
        List<LogAccount> logAccounts = new UserCache().getAccountList();
        if(logAccounts==null) return;
        for(LogAccount logAccount : logAccounts) {
            if(logAccount.getUserName().equals(userInfo.getUserName())) {
                logAccountTemporary = logAccount;
                break;
            }
        }

        requestHttpForAutoLogin(logAccountTemporary);

    }

    private void requestHttpForAutoLogin(LogAccount logAccount) {
        if(logAccount==null) return;
        PCLoginApi pcLoginApi = new PCLoginApi();
        pcLoginApi.setUserName(logAccount.getUserName());
        pcLoginApi.setPassword(logAccount.getPassword());
        pcLoginApi.setPackageVersions(AppInfoUtils.getPkgAndVersion(getActivity()));
        ZhidianHttpClient.request(pcLoginApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                try {
                    PCUserInfoBean pcUserInfoBean = jsonToBean(PCUserInfoBean.class, jsonString, "data");
                    String sid = CodeCheck.jsonToCode(jsonString, "sid");
                    saveUserInfo(pcUserInfoBean, sid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // sava user info
    private void saveUserInfo(PCUserInfoBean pcUserInfoBean,String sid){
        if(pcUserInfoBean == null) {
            return;
        }
        UserInfo userInfo = new UserInfo();
        userInfo.setSid(sid);
        userInfo.setUid(pcUserInfoBean.getUid());
        userInfo.setUserName(pcUserInfoBean.getUserName());
        userInfo.setNickName(pcUserInfoBean.getNickName());
        userInfo.setSignature(pcUserInfoBean.getSignature());
        userInfo.setQq(pcUserInfoBean.getQq());
        userInfo.setContact(pcUserInfoBean.getEmail());
        userInfo.setBigAvatarUrl(pcUserInfoBean.getBigAvatarUrl());
        userInfo.setSmallAvatarUrl(pcUserInfoBean.getSmallAvatarUrl());
        userInfo.setIsSign(pcUserInfoBean.isSign());
        userInfo.setScore(pcUserInfoBean.getScore());
        new UserInfoCache().saveUserInfo(userInfo);
    }

}
