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
import com.youle.gamebox.ui.bean.User;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.util.TOASTUtil;
import com.youle.gamebox.ui.view.PCenterMyGameOptionView;
import com.youle.gamebox.ui.view.PCenterTopDefaultUserView;
import com.youle.gamebox.ui.view.PCenterTopUserView;


/**
 * Created by Administrator on 2014/5/22.
 */
public class PCenterFragment extends BaseFragment implements View.OnClickListener {
    PCenterTopUserView pCenterTopUserView ;
    PCenterMyGameOptionView pCenterMyGameOptionView;
    PCenterTopDefaultUserView pCenterTopDefaultUserView;
    @InjectView(R.id.pcenter_layout_topLinear)
    LinearLayout mPcenterLayoutTopLinear;
    @InjectView(R.id.pcenter_game_linear)
    LinearLayout mPcenterGameLinear;
    @InjectView(R.id.pcenter_dymaic_linear)
    LinearLayout mPcenterDymaicLinear;
    @InjectView(R.id.pcenter_message_linear)
    LinearLayout mPcenterMessageLinear;
    @InjectView(R.id.pcenter_viewpage)
    ViewPager mPcenteViewpage;
    HomePagerAdapter pcenterAdapter = null ;
    @InjectView(R.id.pcenter_game_title)
    TextView mPcenterGameTitle;
    @InjectView(R.id.pcenter_game_line)
    View mPcenterGameLine;
    @InjectView(R.id.pcenter_dymaic_title)
    TextView mPcenterDymaicTitle;
    @InjectView(R.id.pcenter_dymaic_line)
    View mPcenterDymaicLine;
    @InjectView(R.id.pcenter_mymessage_title)
    TextView mPcenterMymessageTitle;
    @InjectView(R.id.pcenter_mymessage_line)
    View mPcenterMymessageLine;
    @InjectView(R.id.pcenter_visitor_title)
    TextView mPcenterVisitorTitle;
    @InjectView(R.id.pcenter_visitor_line)
    View mPcenterVisitorLine;
    @InjectView(R.id.pcenter_visitor_linear)
    LinearLayout mPcenterVisitorLinear;


    @Override
    protected int getViewId() {
        return R.layout.pcenter_layout;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PcenterChangeListener homePageChangeListener = new PcenterChangeListener(0);
        mPcenteViewpage.setOnPageChangeListener(homePageChangeListener);
        pcenterAdapter = new HomePagerAdapter(getActivity().getSupportFragmentManager());
        mPcenteViewpage.setAdapter(pcenterAdapter);
        mPcenteViewpage.setCurrentItem(0);
        mPcenterGameLinear.setOnClickListener(this);
        mPcenterDymaicLinear.setOnClickListener(this);
        mPcenterMessageLinear.setOnClickListener(this);
        mPcenterVisitorLinear.setOnClickListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        selectView();
    }

    // set login userinfo or no login
    UserInfo userInfo = null;

    private void selectView(){
        userInfo = new UserInfoCache().getUserInfo();
        if(userInfo==null){
            setTopDefaultView();
        }else{
            Long lastLogin = userInfo.getLastLogin();
            long currentTimeMillis = System.currentTimeMillis();
            if(currentTimeMillis - lastLogin > 86400000){
                TOASTUtil.showSHORT(getActivity(),"ssionid 已过期");
                setTopDefaultView();
            }else{
                setTopUserView();
            }
        }
    }


    private void setTopUserView(){
        if(pCenterTopUserView == null)
        pCenterTopUserView = new PCenterTopUserView(getActivity());
        mPcenterLayoutTopLinear.removeAllViews();
        mPcenterLayoutTopLinear.addView(pCenterTopUserView);
        User user = new User();
        user.setUid(userInfo.getUid());
        user.setUserName(userInfo.getUserName());
        user.setNickName(userInfo.getNickName());
        user.setBigAvatarUrl(userInfo.getBigAvatarUrl());
        user.setSmallAvatarUrl(userInfo.getSmallAvatarUrl());
        user.setQq(userInfo.getQq());
        user.setContact(userInfo.getContact());
        user.setSignature(userInfo.getSignature());
        user.setSign(userInfo.getIsSign());
        pCenterTopUserView.setViewData(user);
    }
    private void setTopDefaultView(){
        if(pCenterTopDefaultUserView == null)
        pCenterTopDefaultUserView = new PCenterTopDefaultUserView(getActivity());
        mPcenterLayoutTopLinear.removeAllViews();
        mPcenterLayoutTopLinear.addView(pCenterTopDefaultUserView);
        if(pCenterMyGameOptionView ==null)pCenterMyGameOptionView = new PCenterMyGameOptionView(getActivity());
        pCenterMyGameOptionView.setOnclick(true);
    }




//HomePageChangeListener

    class PcenterChangeListener implements ViewPager.OnPageChangeListener{

        PcenterChangeListener(int index) {
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

    // set Title color

    public void setTitleColor(int index){
        setTitleAllTextColor();
        switch (index){
            case 0:
                setTitleSelctor(mPcenterGameTitle,mPcenterGameLine);
                break;
            case 1:
                setTitleSelctor(mPcenterDymaicTitle,mPcenterDymaicLine);
                break;
            case 2:
                setTitleSelctor(mPcenterMymessageTitle,mPcenterMymessageLine);
                break;
            case 3:
                setTitleSelctor(mPcenterVisitorTitle,mPcenterVisitorLine);
                break;
        }
    }

    // select yes:#00c6ff  no: #404040
    public void setTitleAllTextColor(){
        int textColor= Color.parseColor("#888888");
        int lineColor= Color.parseColor("#888888");
        mPcenterGameTitle.setTextColor(textColor);
        mPcenterDymaicTitle.setTextColor(textColor);
        mPcenterVisitorTitle.setTextColor(textColor);
        mPcenterMymessageTitle.setTextColor(textColor);
        mPcenterGameLine.setBackgroundColor(lineColor);
        mPcenterDymaicLine.setBackgroundColor(lineColor);
        mPcenterMymessageLine.setBackgroundColor(lineColor);
        mPcenterVisitorLine.setBackgroundColor(lineColor);
    }

    // selected

    public void setTitleSelctor(TextView title,View view){
        int textColor= Color.parseColor("#ff8933");
        int lineColor= Color.parseColor("#fe7103");
        title.setTextColor(textColor);
        view.setBackgroundColor(lineColor);
    }

    PCenterMyVisitorFragment pCenterMyVisitorFragment = null;
    HomePageDymaicListFragment homePageDymaicListFragment = null;
    MyMsgboardFragment messageFragment = null;
    PCenterFragment pCenterFragment =null;
    String[] titles = new String[]{"我的游戏","我的动态","留言板","我的访客"};
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
                    if(pCenterMyVisitorFragment == null){
                        pCenterMyVisitorFragment = new PCenterMyVisitorFragment();
                    }
                    return pCenterMyVisitorFragment;
                case 1:
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
                case 2:
                    if(messageFragment == null){
                        messageFragment = new MyMsgboardFragment();
                    }
                    return messageFragment;
                case 3:
                    if(pCenterFragment == null){
                        pCenterFragment = new PCenterFragment();
                    }
                    return pCenterFragment;
            }
            return null;
        }



        @Override
        public CharSequence getPageTitle(int position) {
            String title = titles[position];
            return title;
        }
    }




    @Override
    public void onClick(View view) {
        if(view == mPcenterGameLinear){
            setTitleColor(0);
            mPcenteViewpage.setCurrentItem(0);
        }else if(view == mPcenterDymaicLinear){
            setTitleColor(1);
            mPcenteViewpage.setCurrentItem(1);
        }else if(view == mPcenterMessageLinear){
            setTitleColor(2);
            mPcenteViewpage.setCurrentItem(2);
        }else if(view == mPcenterVisitorLinear){
            setTitleColor(3);
            mPcenteViewpage.setCurrentItem(3);
        }

    }



}
