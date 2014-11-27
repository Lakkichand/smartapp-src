package com.youle.gamebox.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import butterknife.InjectView;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TabPageIndicator;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.YouleAplication;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.util.LOGUtil;

/**
 * Created by Administrator on 2014/6/17.
 */
public class CountryFragment extends BaseFragment {
    @InjectView(R.id.homepage_viewpage)
    ViewPager mHomepageViewpage;
    HomePagerAdapter homePagerAdapter = null;
    @InjectView(R.id.titles)
    TabPageIndicator mPageIndicator ;
    @Override
    protected int getViewId() {
        return R.layout.country_layout;
    }

    @Override
    protected String getModelName() {
        return "社区";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDefaultTitle("社区");
        homePagerAdapter = new HomePagerAdapter(getActivity().getSupportFragmentManager());
        mHomepageViewpage.setAdapter(homePagerAdapter);
        mPageIndicator.setViewPager(mHomepageViewpage);
        mHomepageViewpage.setCurrentItem(0);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LOGUtil.e("onAc",resultCode);
    }

    //HomePageChangeListener



    WebViewFragment webViewFragment = null;
    HomePageDymaicListFragment homePageDymaicListFragment = null;

    WebViewFragment pCenterMyGameFragment3 = null;
    String[] titles = new String[]{"社区", "论坛", "我的论坛"};

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
                    if (homePageDymaicListFragment == null) {
                        homePageDymaicListFragment = new HomePageDymaicListFragment(-1, HomePageDymaicListFragment.OTHER);
                        UserInfo userInfo = new UserInfoCache().getUserInfo();
                        if (userInfo != null) {
                            Bundle bundle = new Bundle();
                            bundle.putString(HomepageFragment.KEY_TITLE, userInfo.getNickName());
                            bundle.putLong(HomepageFragment.KEY_UID, userInfo.getUid());
                            homePageDymaicListFragment.setArguments(bundle);
                        } else {
                            Bundle bundle = new Bundle();
                            bundle.putString(HomepageFragment.KEY_TITLE, "no");
                            bundle.putLong(HomepageFragment.KEY_UID, 1);
                            homePageDymaicListFragment.setArguments(bundle);
                        }
                    }
                    return homePageDymaicListFragment;
                case 1:

                    if (webViewFragment == null) {
                        webViewFragment = new WebViewFragment("社区", YouleAplication.COUNTRY_RUL);
                    }
                    return webViewFragment;
                case 2:
                    if (pCenterMyGameFragment3 == null) {
                        pCenterMyGameFragment3 = new WebViewFragment("社区", YouleAplication.MY_COUNTRY_URL);
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
