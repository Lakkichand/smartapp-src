package com.youle.gamebox.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.InjectView;
import com.viewpagerindicator.TabPageIndicator;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.api.pcenter.PersonalApi;
import com.youle.gamebox.ui.api.person.MsgboardPublishApi;
import com.youle.gamebox.ui.bean.pcenter.PersonalBean;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.view.HomePageTopUserView;

/**
 * Created by Administrator on 2014/6/17.
 */
public class HomepageFragment extends BaseFragment {
    public static final String USER_B = "userB";
    public static final String KEY_TITLE = "key_title";
    public static final String KEY_UID = "key_uid";

    HomePagerAdapter homePagerAdapter = null;
    String nickName = "";
    Long uid = null;
    @InjectView(R.id.homepage_top_linear)
    LinearLayout mHomepageTopLinear;
    @InjectView(R.id.titles)
    TabPageIndicator mTitles;
    @InjectView(R.id.homepage_viewpage)
    ViewPager mHomepageViewpage;
    PersonalBean personalBean ;
    private String linkId ;
    private int currentItem=0 ;
    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public HomepageFragment(Long uid, String nickName) {
        this.uid = uid;
        this.nickName = nickName;
    }

    @Override
    protected int getViewId() {
        return R.layout.homepage_indicator;
    }

    @Override
    protected String getModelName() {
        return "用户详情";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addTopView();
        initPageViewAdapter();
    }

    private void initTitleBar() {
        setDefaultTitle(getString(R.string.name_title_format,personalBean.getNickName()));
    }

    private void initPageViewAdapter() {

        homePagerAdapter = new HomePagerAdapter(getChildFragmentManager());
        mHomepageViewpage.setAdapter(homePagerAdapter);
        mTitles.setViewPager(mHomepageViewpage);
        mTitles.setCurrentItem(currentItem);
    }
    public void setCurrentTab(int item){
        currentItem = item;
    }
    // get homepage userinfo
    private void loadTopUserData() {
        final PersonalApi personalApi = new PersonalApi();
        personalApi.setUid(uid);
        String sid = new UserInfoCache().getSid();
        if (null == sid || "".equals(sid)) {

        } else {

            personalApi.setSid(sid);
        }

        ZhidianHttpClient.request(personalApi, new JsonHttpListener(getActivity(),"正在加载个人信息") {
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                try {
                     personalBean = jsonToBean(PersonalBean.class, jsonString, "account");
                    initTitleBar();
                    if (personalBean != null)
                        if (homePageTopUserView != null) homePageTopUserView.setViewData(personalBean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
            }
        });

    }


    //add top view
    HomePageTopUserView homePageTopUserView = null;

    public void addTopView() {
        homePageTopUserView = new HomePageTopUserView(getActivity());
        mHomepageTopLinear.removeAllViews();
        mHomepageTopLinear.addView(homePageTopUserView);
        loadTopUserData();
    }


    MyMsgboardFragment myMsgboardFragment = null;
    HomePageDymaicListFragment homePageDymaicListFragment = null;
    MyGameFragment mMyGameFragment = null;
    String[] titles = new String[]{"留言", "动态", "游戏"};

    class HomePagerAdapter extends FragmentPagerAdapter {
        FragmentManager fm;

        public HomePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int postion) {
            switch (postion) {
                case 0:
                    if (myMsgboardFragment == null) {
                        myMsgboardFragment = new MyMsgboardFragment(uid, MyMsgboardFragment.OTHER);
                        myMsgboardFragment.setArguments(getArguments());
                    }
                    return myMsgboardFragment;
                case 1:
                    if (homePageDymaicListFragment == null) {
                        homePageDymaicListFragment = new HomePageDymaicListFragment(uid, HomePageDymaicListFragment.OTHER);
                        homePageDymaicListFragment.setLinkId(linkId);
                        homePageDymaicListFragment.setArguments(getArguments());
                    }
                    return homePageDymaicListFragment;
                case 2:
                    if (mMyGameFragment == null) {
                        mMyGameFragment = new MyGameFragment(uid, MyGameFragment.OTHER_GAME);
                    }
                    return mMyGameFragment;
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            UserInfo userInfo = new  UserInfoCache().getUserInfo();
            if(userInfo==null||uid!=userInfo.getUid().longValue()){
                return  "TA的"+titles[position];
            }else {
                return "我的"+titles[position];
            }
        }
    }




}
