package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import butterknife.InjectView;
import com.viewpagerindicator.TabPageIndicator;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 14-6-25.
 */
public class GonglueFragment extends BaseFragment {
    @InjectView(R.id.gonglueIndicator)
    TabPageIndicator mGonglueIndicator;
    @InjectView(R.id.gonglueViewpager)
    ViewPager mGonglueViewpager;
    private GonglueHomeFragment mGongluHomeFragment;
    private MyCatagroryFragment myCatagroryFragment;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGonglueViewpager.setAdapter(new GonglueViewPagerAdapter());
        mGonglueIndicator.setViewPager(mGonglueViewpager);
    }

    @Override
    protected int getViewId() {
        return R.layout.fragment_gonglue;
    }

    class GonglueViewPagerAdapter extends FragmentPagerAdapter {

        public GonglueViewPagerAdapter() {
            super(getFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                if (mGongluHomeFragment == null) {
                    mGongluHomeFragment = new GonglueHomeFragment();
                }
                return mGongluHomeFragment;
            } else if (position == 1) {
                if (myCatagroryFragment == null) {
                    myCatagroryFragment = new MyCatagroryFragment();
                }
                return myCatagroryFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return getResources().getStringArray(R.array.gonglue_tab).length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getResources().getStringArray(R.array.gonglue_tab)[position];
        }
    }
}
