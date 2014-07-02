package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TabHost;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;

/**
 * Created by Administrator on 14-5-12.
 */
public class IndexFragment extends BaseFragment implements View.OnClickListener {
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
    TabHost mTabHost ;
    private RankFragment mRankFragment;
    private ClassfyFragment mClassfyFragment;
    private PCenterFragment mPcentfragment;
    private IndexHomeFragment mIndexHomeFragment;
    @Override
    protected int getViewId() {
        return R.layout.index_fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTabHost.setup();

        mTabHost.addTab(mTabHost.newTabSpec("home").setIndicator("分类").setContent(R.id.classfy));
        mTabHost.addTab(mTabHost.newTabSpec("feed").setIndicator("排行").setContent(R.id.rank));
        mTabHost.addTab(mTabHost.newTabSpec("explore").setIndicator("主页").setContent(R.id.home));
        mTabHost.addTab(mTabHost.newTabSpec("comunity").setIndicator("动态").setContent(R.id.comunity));
        mTabHost.addTab(mTabHost.newTabSpec("setting").setIndicator("个人中心").setContent(R.id.pCenter));
        mTabHost.setCurrentTab(2);
        loadData();
        mHomeLayout.setSelected(true);
    }



    protected void loadData() {
        mRankLayout.setOnClickListener(this);
        mClassfyLayout.setOnClickListener(this);
        mHomeLayout.setOnClickListener(this);
        mCountryLayout.setOnClickListener(this);
        mPersonLayout.setOnClickListener(this);
    }

    private void resetTab(){
        mRankLayout.setSelected(false);
        mClassfyLayout.setSelected(false);
        mHomeLayout.setSelected(false);
        mCountryLayout.setSelected(false);
        mPersonLayout.setSelected(false);
//        ((HomeActivity)getActivity()).getmSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
    }

    @Override
    public void onClick(View v) {
        resetTab();
        switch (v.getId()){
            case R.id.rankLayout:
                mTabHost.setCurrentTab(1);
                mRankLayout.setSelected(true);
                break;
            case R.id.classfyLayout:
                mTabHost.setCurrentTab(0);
                mClassfyLayout.setSelected(true);
                break;
            case R.id.homeLayout:
                mTabHost.setCurrentTab(2);
                mHomeLayout.setSelected(true);
                break;
            case R.id.countryLayout:
                mTabHost.setCurrentTab(3);
                mCountryLayout.setSelected(true);
                break;
            case R.id.personLayout:
                mTabHost.setCurrentTab(4);
                mPersonLayout.setSelected(true);
                break;
        }
    }
}
