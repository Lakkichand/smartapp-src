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
import com.youle.gamebox.ui.greendao.UserInfo;

/**
 * Created by Administrator on 2014/6/17.
 */
public class CountryFragment extends BaseFragment implements View.OnClickListener {
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

    @Override
    protected int getViewId() {
        return R.layout.country_layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HomePageChangeListener homePageChangeListener = new HomePageChangeListener(0);
        mHomepageViewpage.setOnPageChangeListener(homePageChangeListener);
        homePagerAdapter = new HomePagerAdapter(getActivity().getSupportFragmentManager());
        mHomepageViewpage.setAdapter(homePagerAdapter);
        mHomepageViewpage.setCurrentItem(0);
        mHomepageMessageLinear.setOnClickListener(this);
        mHomepageDymaicLinear.setOnClickListener(this);
        mHomepageGameLinear.setOnClickListener(this);




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
        int textColor= Color.parseColor("#888888");
        int lineColor= Color.parseColor("#888888");
        mHomepageMessageTitle.setTextColor(textColor);
        mHomepageDymaicTitle.setTextColor(textColor);
        mHomepageGameTitle.setTextColor(textColor);
        mHomepageMessageLine.setBackgroundColor(lineColor);
        mHomepageDymaicLine.setBackgroundColor(lineColor);
        mHomepageGameLine.setBackgroundColor(lineColor);
    }

    // selected

    public void setTitleSelctor(TextView title,View view){
        int textColor= Color.parseColor("#ff8933");
        int lineColor= Color.parseColor("#fe7103");
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


    WebViewFragment webViewFragment  = null;
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
                    if(homePageDymaicListFragment == null){
                        homePageDymaicListFragment = new HomePageDymaicListFragment();
                        UserInfo userInfo = new UserInfoCache().getUserInfo();
                        if(userInfo!=null) {
                            Bundle bundle = new Bundle();
                            bundle.putString(HomepageFragment.KEY_TITLE, userInfo.getNickName());
                            bundle.putLong(HomepageFragment.KEY_UID, userInfo.getUid());
                            homePageDymaicListFragment.setArguments(bundle);
                        }else{
                            Bundle bundle = new Bundle();
                            bundle.putString(HomepageFragment.KEY_TITLE, "no");
                            bundle.putLong(HomepageFragment.KEY_UID, 1);
                            homePageDymaicListFragment.setArguments(bundle);
                        }
                    }
                    return homePageDymaicListFragment;
                case 1:

                    if(webViewFragment == null){
                        webViewFragment =  new WebViewFragment("百度","http://www.baidu.com");
                    }
                    return webViewFragment;
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
