package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import butterknife.InjectView;
import com.viewpagerindicator.TabPageIndicator;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.HomeActivity;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.view.PCenterMyGameOptionView;
import com.youle.gamebox.ui.view.PCenterTopDefaultUserView;
import com.youle.gamebox.ui.view.PCenterTopUserView;


/**
 * Created by Administrator on 2014/5/22.
 */
public class PersonCenterFragment extends BaseFragment {
    PCenterTopUserView pCenterTopUserView;
    PCenterMyGameOptionView pCenterMyGameOptionView;
    PCenterTopDefaultUserView pCenterTopDefaultUserView;
    HomePagerAdapter pcenterAdapter;
    @InjectView(R.id.pcenter_viewpage)
    ViewPager mPcenteViewpage;
    @InjectView(R.id.pageIndicator)
    TabPageIndicator mTabPageIndicator;
    @InjectView(R.id.pcenter_layout_topLinear)
    LinearLayout mPcenterLayoutTopLinear;

    public PersonCenterFragment() {
    }

    @Override
    protected int getViewId() {
        return R.layout.pcenter_layout;
    }

    @Override
    protected String getModelName() {
        return "个人中心";
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pcenterAdapter = new HomePagerAdapter(getChildFragmentManager());
        mPcenteViewpage.setAdapter(pcenterAdapter);
        mTabPageIndicator.setViewPager(mPcenteViewpage);
        ((HomeActivity) getActivity()).setmPersonCenterFragment(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        selectView();
    }

    // set login userinfo or no login
    UserInfo userInfo = null;

    private void selectView() {
        userInfo = new UserInfoCache().getUserInfo();
        if (userInfo == null) {
            setTopDefaultView();
        } else {
            setTopUserView();
        }
    }


    private void setTopUserView() {
        if (pCenterTopUserView == null)
            pCenterTopUserView = new PCenterTopUserView(getActivity());
        mPcenterLayoutTopLinear.removeAllViews();
        mPcenterLayoutTopLinear.addView(pCenterTopUserView);
        pCenterTopUserView.setViewData(userInfo);
    }

    private void setTopDefaultView() {
        if (pCenterTopDefaultUserView == null)
            pCenterTopDefaultUserView = new PCenterTopDefaultUserView(getActivity());
        mPcenterLayoutTopLinear.removeAllViews();
        mPcenterLayoutTopLinear.addView(pCenterTopDefaultUserView);
        if (pCenterMyGameOptionView == null) pCenterMyGameOptionView = new PCenterMyGameOptionView(getActivity());
        pCenterMyGameOptionView.setOnclick(true);
    }


    public void onLogOut() {
        userInfo = null;
        selectView();
        if (myGameFragment != null) {
            myGameFragment.loadData();
        }
        if (homePageDymaicListFragment != null) {
            if (new UserInfoCache().getUserInfo() == null) {
                homePageDymaicListFragment.setUid(-1);
            } else {
                homePageDymaicListFragment.setUid(new UserInfoCache().getUserInfo().getUid());
            }
            homePageDymaicListFragment.loadData();
        }

        if (messageFragment != null) {
            messageFragment.loadData();
        }
    }


    private int getCustomColor(int color) {
        return getResources().getColor(color);
    }

    MyGameFragment myGameFragment = null;
    PCenterMyVisitorFragment pCenterMyVisitorFragment = null;
    HomePageDymaicListFragment homePageDymaicListFragment = null;
    MyMsgboardFragment messageFragment = null;
    // PCenterFragment pCenterFragment = null;
    String[] titles = new String[]{"我的游戏", "我的动态", "留言板", "我的访客"};

    class HomePagerAdapter extends FragmentPagerAdapter {
        FragmentManager fm;

        public HomePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public Fragment getItem(int postion) {

            if (postion == 0) {
                if (myGameFragment == null) {
                    myGameFragment = new MyGameFragment(-1, MyGameFragment.MY_GAME);
                }
                return myGameFragment;


            } else if (postion == 1) {
                if (homePageDymaicListFragment == null) {
                    UserInfo userInfo = new UserInfoCache().getUserInfo();
                    if (userInfo != null) {
                        homePageDymaicListFragment = new HomePageDymaicListFragment(userInfo.getUid(), HomePageDymaicListFragment.MY);
                    } else {
                        homePageDymaicListFragment = new HomePageDymaicListFragment(-1, HomePageDymaicListFragment.OTHER);
                    }
                }
                return homePageDymaicListFragment;
            } else if (postion == 2) {
                if (messageFragment == null) {
                    //MessageBoardInputView messageBoardInputView = new MessageBoardInputView(getActivity());
                    messageFragment = new MyMsgboardFragment(-1L, MyMsgboardFragment.MY);
                }

                return messageFragment;
            } else if (postion == 3) {
                if (pCenterMyVisitorFragment == null) {
                    pCenterMyVisitorFragment = new PCenterMyVisitorFragment();
                }
                return pCenterMyVisitorFragment;
            } else {
                return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = titles[position];
            return title;
        }
    }


}
