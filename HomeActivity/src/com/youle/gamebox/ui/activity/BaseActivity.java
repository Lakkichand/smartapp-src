package com.youle.gamebox.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.InjectView;
import com.ta.TAActivity;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.fragment.BaseFragment;
import com.youle.gamebox.ui.util.DownLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;

/**
 * Created by Administrator on 14-4-22.
 */
public class BaseActivity extends TAActivity {
    LinearLayout mTitleLayout;
    LinearLayout mTitle;
    TextView mLoadStart;
    FrameLayout mContent;
    private FragmentManager baseFragmentManager;

    public void setmTitleView(View view) {
        mTitleLayout.removeAllViews();
        mTitleLayout.addView(view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youle_base_fragment);
        mTitleLayout = (LinearLayout) findViewById(R.id.titleLayout);
        mLoadStart = (TextView) findViewById(R.id.loadStart);
        mContent = (FrameLayout) findViewById(R.id.content);
        baseFragmentManager = getSupportFragmentManager();
        DownLoadUtil.init(this);
    }

    public void loadStart(){
        LOGUtil.e(getClass().getSimpleName(),"loadStart");
       mLoadStart.setVisibility(View.VISIBLE);
        mContent.setVisibility(View.GONE);
    }

    public void loadSuccess(){
        LOGUtil.e(getClass().getSimpleName(),"loadSuccess");
        mLoadStart.setVisibility(View.GONE);
        mContent.setVisibility(View.VISIBLE);
    }

    public void loadFail(){
        mLoadStart.setVisibility(View.VISIBLE);
        mLoadStart.setText("加载失败");
        mContent.setVisibility(View.GONE);
    }
    public void addFragment(BaseFragment f, boolean isAddToBackStack) {
        String tag = f.toString();
        if (baseFragmentManager.findFragmentByTag(tag) == null) {
            final FragmentTransaction ft = baseFragmentManager.beginTransaction();
            ft.add(R.id.content, f);
            if (isAddToBackStack) {
                ft.addToBackStack(null);
            }
            ft.commit();
        }
    }

    public void addFragment(BaseFragment f, int id, boolean isAddToBackStack) {
        String tag = f.toString();
        if (baseFragmentManager.findFragmentByTag(tag) == null) {
            final FragmentTransaction ft = baseFragmentManager.beginTransaction();
            ft.add(id, f, tag);
            if (isAddToBackStack) {
                ft.addToBackStack(tag);
            }
            ft.commit();
        }
    }


    @Override
    public void onBackPressed() {
        LOGUtil.e(getClass().getSimpleName(), "onBackPressed=" + baseFragmentManager.getBackStackEntryCount());
        if (baseFragmentManager.getBackStackEntryCount() > 1) {
            baseFragmentManager.popBackStack();
        } else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
