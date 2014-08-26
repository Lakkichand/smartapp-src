package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import butterknife.InjectView;
import com.viewpagerindicator.TabPageIndicator;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.view.HomeTitleView;

/**
 * Created by Administrator on 14-5-30.
 */
public class ClassfyFragment extends BaseFragment implements ViewPager.OnPageChangeListener {
    private String[] CONTENT;
    @InjectView(R.id.titles)
    TabPageIndicator mTitles;
    @InjectView(R.id.tagViewPage)
    ViewPager mViewPage;
    private Fragment gameFragment;
    private Fragment tagFragment;

    @Override
    protected int getViewId() {
        return R.layout.fragment_cassfy;
    }

    @Override
    protected String getModelName() {
        return "排行版";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
    }

    protected void loadData() {
        CONTENT = getResources().getStringArray(R.array.classfy);
        FragmentPagerAdapter adapter = new GoogleMusicAdapter();
        mViewPage.setAdapter(adapter);
        mTitles.setViewPager(mViewPage);
        mTitles.setOnPageChangeListener(this);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    class GoogleMusicAdapter extends FragmentPagerAdapter {
        public GoogleMusicAdapter() {
            super(getFragmentManager());
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                if (gameFragment == null) {
                    gameFragment = new GameClassfyFragment();
                }
                return gameFragment;
            } else if (position == 1) {
                if (tagFragment == null) {
                    tagFragment = new TagClassfyFragment();
                }
                return tagFragment;
            }
            return null;
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
