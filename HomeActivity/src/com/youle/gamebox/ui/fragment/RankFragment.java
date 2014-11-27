package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import butterknife.InjectView;
import com.viewpagerindicator.TabPageIndicator;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.HomeActivity;
import com.youle.gamebox.ui.api.RankApi;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.view.HomeTitleView;

/**
 * Created by Administrator on 14-5-12.
 */
public class RankFragment extends BaseFragment implements ViewPager.OnPageChangeListener {
    private static String[] CONTENT  ;
    @InjectView(R.id.titles)
    TabPageIndicator mTitles;
    @InjectView(R.id.rankviewPage)
    ViewPager mViewPage;
    private RankItemFragment allFragment  ;
    private RankItemFragment newFragment ;
    private RankItemFragment monthFragment ;
    private RankItemFragment weekFragment ;
    PagerAdapter adapter  ;
    @Override
    protected int getViewId() {
        return R.layout.fragment_rank;
    }

    @Override
    protected String getModelName() {
        return "排行";
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTitles.setOnPageChangeListener(this);
        mViewPage.setOffscreenPageLimit(0);
        if(adapter==null) {
            loadData();
        }else{
            mViewPage.setAdapter(adapter);
        }
    }

    private void requestRankGame(RankApi.RankType type){

    }

    protected void loadData() {
        CONTENT = getActivity().getResources().getStringArray(R.array.rank_game) ;
        adapter = new RankAdapter(getChildFragmentManager());
        mViewPage.setAdapter(adapter);
        mTitles.setViewPager(mViewPage);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        LOGUtil.e(TAG,"onPageSelected:"+position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        LOGUtil.e(TAG,"onPageScrollStateChanged="+state);
        if(state == ViewPager.SCROLL_STATE_IDLE){
            fillCurrentPage(mViewPage.getCurrentItem());
        }
    }

    private void fillCurrentPage(int position){
        switch (position){
            case 0:
                allFragment.fillData();
                break;
            case 1:
                newFragment.fillData();
                break;
            case 2:
                monthFragment.fillData();
                break;
            case 3:
                weekFragment.fillData();
                break;
        }
    }



    class RankAdapter extends FragmentPagerAdapter{
        public RankAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i){
                case 0:
                    if(allFragment==null){
                        allFragment = new RankItemFragment(RankApi.RankType.ALL) ;
                    }
                    return allFragment ;
                case 1:
                    if(newFragment == null){
                        newFragment = new RankItemFragment(RankApi.RankType.NEW) ;
                    }
                    return  newFragment;
                case 2:
                    if(monthFragment==null){
                        monthFragment = new RankItemFragment(RankApi.RankType.WEEK) ;
                    }
                    return  monthFragment ;
                case 3:
                    if(weekFragment==null){
                        weekFragment = new RankItemFragment(RankApi.RankType.MONTH);
                    }
                    return weekFragment ;
            }
            return  null ;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return CONTENT[position % CONTENT.length].toUpperCase();
        }

        @Override
        public int getCount() {
            return CONTENT.length;
        }
    }

}
