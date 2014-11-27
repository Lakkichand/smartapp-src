package com.youle.gamebox.ui.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.ta.util.config.TAIConfig;
import com.umeng.analytics.MobclickAgent;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.YouleAplication;
import com.youle.gamebox.ui.api.CheckUpdataApi;
import com.youle.gamebox.ui.bean.UpdataBean;
import com.youle.gamebox.ui.fragment.*;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.CacheManager;
import com.youle.gamebox.ui.util.UIUtil;
import com.youle.gamebox.ui.view.SlidingPaneLayout;

/**
 * Created by Administrator on 14-5-12.
 */
public class HomeActivity extends BaseActivity {
    public static final String MANAGE="MANAGE";
    public static final int MANAGE_VALUE=1;
    SlidingPaneLayout layout;
    @InjectView(R.id.contentFragment)
    LinearLayout mContentFragment;
    @InjectView(R.id.slidingmenu)
    SlidingPaneLayout mSlidingmenu;
    private View mTabLayout;
    private LeftMenuFragment leftMenuFragment;
    private PersonCenterFragment mPersonCenterFragment;
    private IndexFragment mIndexFragment;

    public void setmIndexFragment(IndexFragment mIndexFragment) {
        this.mIndexFragment = mIndexFragment;
    }

    public void setmPersonCenterFragment(PersonCenterFragment mPersonCenterFragment) {
        this.mPersonCenterFragment = mPersonCenterFragment;
    }

    public void setLeftMenuFragment(LeftMenuFragment leftMenuFragment) {
        this.leftMenuFragment = leftMenuFragment;
    }

    public void setmTabLayout(View mTabLayout) {
        this.mTabLayout = mTabLayout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home);
        MobclickAgent.updateOnlineConfig(this);
        MobclickAgent.openActivityDurationTrack(false);
        MobclickAgent.setDebugMode(true);
        ButterKnife.inject(this);
        layout = (SlidingPaneLayout) findViewById(R.id.slidingmenu);
        layout.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelOpened(View panel) {
                if (leftMenuFragment != null) {
                    leftMenuFragment.initNumber();
                }
            }

            @Override
            public void onPanelClosed(View panel) {

            }
        });
        TAIConfig taiConfig = getTAApplication().getPreferenceConfig();
        if (taiConfig.getBoolean(SettingActivity.CLEAN_CACH, true)) {
            CacheManager cacheManager = new CacheManager(getPackageManager());
            cacheManager.setOnActionListener(new CacheManager.OnActionListener() {
                @Override
                public void onScanStarted(int appsCount) {

                }

                @Override
                public void onCleanCompleted(long cacheSize) {
                    UIUtil.toast(HomeActivity.this, "已经释放" + Formatter.formatFileSize(HomeActivity.this, cacheSize));
                }
            });
            cacheManager.cleanAllCache();
        }
        int i = getIntent().getIntExtra(MANAGE,-1);
        if(i==MANAGE_VALUE){
            Intent intent = new Intent(this,DownLoadManagerActivity.class);
            intent.putExtra(DownLoadManagerActivity.TYPE, DownloadManagerFragment.DOWN);
            startActivity(intent);
        }
    }

    public SlidingPaneLayout getSlidingLayout() {
        return layout;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        YouleAplication.messageNumberBean = null;
    }


    @Override
    public void onBackPressed() {
        if (mSlidingmenu.isOpen()) {
            mSlidingmenu.closePane();
        } else {
            super.onBackPressed();
        }
    }
    private long mkeyTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if((System.currentTimeMillis() - mkeyTime) > 2000){
                mkeyTime = System.currentTimeMillis();
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            }else{
                finish();
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void initDownLoadNumber() {
        mIndexFragment.initDownLoadNumber();
    }

    public void onLogoutSuccess() {
        mPersonCenterFragment.onLogOut();
    }
}
