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
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.api.pcenter.PersonalApi;
import com.youle.gamebox.ui.bean.User;
import com.youle.gamebox.ui.bean.pcenter.PersonalBean;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.view.BaseTitleBarView;
import com.youle.gamebox.ui.view.HomePageTopUserView;

/**
 * Created by Administrator on 2014/6/17.
 */
public class HomepageFragment extends BaseFragment implements View.OnClickListener {
    public static final String USER_B = "userB";
    public static final String KEY_TITLE = "key_title";
    public static final String KEY_UID = "key_uid";
    @InjectView(R.id.homepage_top_linear)
    LinearLayout mHomepageTopLinear;
    @InjectView(R.id.homepage_message_title)
    TextView mHomepageMessageTitle;
    @InjectView(R.id.homepage_message_line)
    View mHomepageMessageLine;
    @InjectView(R.id.homepage_message_linear)
    LinearLayout mHomepageMessageLinear;
    @InjectView(R.id.homepage_dymaic_title)
    TextView mHomepageDymaicTitle;
    @InjectView(R.id.homepage_dymaic_line)
    View mHomepageDymaicLine;
    @InjectView(R.id.homepage_dymaic_linear)
    LinearLayout mHomepageDymaicLinear;
    @InjectView(R.id.homepage_game_title)
    TextView mHomepageGameTitle;
    @InjectView(R.id.homepage_game_line)
    View mHomepageGameLine;
    @InjectView(R.id.homepage_game_linear)
    LinearLayout mHomepageGameLinear;
    @InjectView(R.id.homepage_viewpage)
    ViewPager mHomepageViewpage;
    HomePagerAdapter homePagerAdapter = null ;
    String nickName="";
    Long uid = Long.valueOf(0);
    @Override
    protected int getViewId() {
        return R.layout.homepage_layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getUserInfo();
        if(0 ==uid)return;
        BaseTitleBarView baseTitleBarView = setTitleView();
        baseTitleBarView.setTitleBarMiddleView(null, nickName+"的主页");
        baseTitleBarView.setVisiableRightView(View.GONE);
        addTopView();
        HomePageChangeListener homePageChangeListener = new HomePageChangeListener(0);
        mHomepageViewpage.setOnPageChangeListener(homePageChangeListener);
        homePagerAdapter = new HomePagerAdapter(getActivity().getSupportFragmentManager());
        mHomepageViewpage.setAdapter(homePagerAdapter);
        mHomepageViewpage.setCurrentItem(0);
        mHomepageMessageLinear.setOnClickListener(this);
        mHomepageDymaicLinear.setOnClickListener(this);
        mHomepageGameLinear.setOnClickListener(this);
    }

    // get homepage userinfo
    private void getdata(){
        final PersonalApi personalApi = new PersonalApi();
        personalApi.setUid(uid);
        String sid = new UserInfoCache().getSid();
        if(null == sid || "".equals(sid)){

        }else{

            personalApi.setSid(sid);
        }

        ZhidianHttpClient.request(personalApi,new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                try {
                    PersonalBean personalBean = jsonToBean(PersonalBean.class, jsonString,"account");
                    if(personalBean!=null)
                        if(homePageTopUserView!=null)homePageTopUserView.setViewData(personalBean);
                }catch (Exception e){

                }



            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
            }
        });




    }
    private void getUserInfo(){
        Bundle bundle = getArguments();
        if(bundle!=null){
        nickName = bundle.getString(KEY_TITLE);
        uid = bundle.getLong(KEY_UID);
    }

        }




    //add top view
    HomePageTopUserView homePageTopUserView = null;
    public void addTopView(){
        homePageTopUserView = new HomePageTopUserView(getActivity());
        mHomepageTopLinear.removeAllViews();
        mHomepageTopLinear.addView(homePageTopUserView);
        getdata();
    }





    // set Title color

    public void setTitleColor(int index){
        setTitleAllTextColor();
        switch (index){
            case 0:
                setTitleSelctor(mHomepageMessageTitle,mHomepageMessageLine);
                break;
            case 1:
                setTitleSelctor(mHomepageDymaicTitle,mHomepageDymaicLine);
                break;
            case 2:
                setTitleSelctor(mHomepageGameTitle,mHomepageGameLine);
                break;
        }
    }

    // select yes:#00c6ff  no: #404040
    public void setTitleAllTextColor(){
        int textColor= Color.parseColor("#404040");
        int lineColor= Color.parseColor("#404040");
        mHomepageMessageTitle.setTextColor(textColor);
        mHomepageDymaicTitle.setTextColor(textColor);
        mHomepageGameTitle.setTextColor(textColor);
        mHomepageMessageLine.setBackgroundColor(lineColor);
        mHomepageDymaicLine.setBackgroundColor(lineColor);
        mHomepageGameLine.setBackgroundColor(lineColor);
    }

    // selected

    public void setTitleSelctor(TextView title,View view){
        int textColor= Color.parseColor("#00c6ff");
        int lineColor= Color.parseColor("#00c6ff");
        title.setTextColor(textColor);
        view.setBackgroundColor(lineColor);
    }

    @Override
    public void onClick(View v) {
        if(v == mHomepageMessageLinear){
            setTitleColor(0);
            mHomepageViewpage.setCurrentItem(0);
        }else if(v == mHomepageDymaicLinear){
            setTitleColor(1);
            mHomepageViewpage.setCurrentItem(1);
        }else if(v == mHomepageGameLinear){
            setTitleColor(2);
            mHomepageViewpage.setCurrentItem(2);
        }
    }


    //HomePageChangeListener

    class HomePageChangeListener implements ViewPager.OnPageChangeListener{

        HomePageChangeListener(int index) {
            setTitleColor(index);
        }

        @Override
        public void onPageScrolled(int i, float v, int i2) {

        }

        @Override
        public void onPageSelected(int i) {
            setTitleColor(i);
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    }


    MyMsgboardFragment myMsgboardFragment = null;
    HomePageDymaicListFragment homePageDymaicListFragment = null;
    PCenterMyGameFragment pCenterMyGameFragment3 = null;
    String[] titles = new String[]{"留言","动态","游戏"};
    class HomePagerAdapter extends FragmentPagerAdapter {
        FragmentManager fm ;
        public HomePagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int postion) {
            switch (postion){
                case 0:
                    if(myMsgboardFragment == null){
                        myMsgboardFragment = new MyMsgboardFragment();
                        myMsgboardFragment.setArguments(getArguments());
                    }
                    return myMsgboardFragment;
                case 1:
                    if(homePageDymaicListFragment == null){
                        homePageDymaicListFragment = new HomePageDymaicListFragment();
                        homePageDymaicListFragment.setArguments(getArguments());
                    }
                    return homePageDymaicListFragment;
                case 2:
                    if(pCenterMyGameFragment3 == null){
                        pCenterMyGameFragment3 = new PCenterMyGameFragment();
                    }
                    return pCenterMyGameFragment3;
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = titles[position];
            return title;
        }
    }




}
