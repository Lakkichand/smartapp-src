package com.youle.gamebox.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.ta.TAActivity;
import com.umeng.analytics.MobclickAgent;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.fragment.BaseFragment;
import com.youle.gamebox.ui.util.DownLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.view.ViewServer;
import pl.droidsonroids.gif.GifImageView;

import java.util.Stack;

/**
 * Created by Administrator on 14-4-22.
 */
public class BaseActivity extends TAActivity{
    LinearLayout mTitleLayout;
    //@InjectView(R.id.title)
    LinearLayout mTitle;
    //@InjectView(R.id.loadStart)
    GifImageView mLoadStart;
    View mNoNetView;
    //@InjectView(R.id.content)
    FrameLayout mContent;
    private FragmentManager baseFragmentManager;
    private Stack<View> titleStack = new Stack<View>();

    public void setmTitleView(View view) {
        titleStack.add(view);
        mTitleLayout.removeAllViews();
        mTitleLayout.addView(view);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewServer.get(this).addWindow(this);
        setContentView(R.layout.youle_base_fragment);
        mTitleLayout = (LinearLayout) findViewById(R.id.titleLayout);
        mLoadStart = (GifImageView) findViewById(R.id.loadStart);
        mContent = (FrameLayout) findViewById(R.id.content);
        mNoNetView = findViewById(R.id.noNet);
        baseFragmentManager = getSupportFragmentManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewServer.get(this).setFocusedWindow(this);
        DownLoadUtil.init();
        DownLoadUtil.init(this);
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public void loadStart() {
        LOGUtil.e(getClass().getSimpleName(), "loadStart");
        mLoadStart.setVisibility(View.VISIBLE);
        mContent.setVisibility(View.GONE);
        mNoNetView.setVisibility(View.GONE);
    }

    public void loadSuccess() {
        LOGUtil.e(getClass().getSimpleName(), "loadSuccess");
        mLoadStart.setVisibility(View.GONE);
        mContent.setVisibility(View.VISIBLE);
        mNoNetView.setVisibility(View.GONE);
    }

    public void loadFail() {
        mLoadStart.setVisibility(View.GONE);
        mContent.setVisibility(View.GONE);
        mNoNetView.setVisibility(View.VISIBLE);
    }
    public void addFragment(BaseFragment f, boolean isAddToBackStack) {
        String tag = f.getClass().getSimpleName();
        if (baseFragmentManager.findFragmentByTag(tag) == null) {
            final FragmentTransaction ft = baseFragmentManager.beginTransaction();
            ft.add(R.id.content, f);
            if (isAddToBackStack) {
                ft.addToBackStack(tag);
            }
            ft.commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewServer.get(this).removeWindow(this);
    }

    public void addFragment(BaseFragment f, int id, boolean isAddToBackStack) {
        String tag = f.getClass().getSimpleName();
        if (baseFragmentManager.findFragmentByTag(tag) == null) {
            final FragmentTransaction ft = baseFragmentManager.beginTransaction();
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.add(id, f, tag);
            if (isAddToBackStack) {
                ft.addToBackStack(tag);
            }
            ft.commit();
        }
    }

    public void backTitle() {
        if (!titleStack.empty()) {
            titleStack.pop();
            if(!titleStack.empty()) {
                mTitleLayout.removeAllViews();
                mTitleLayout.addView(titleStack.peek());
            }
        }
    }

    @Override
    public void onBackPressed() {
        LOGUtil.e(getClass().getSimpleName(), "onBackPressed=" + baseFragmentManager.getBackStackEntryCount() + "|"+titleStack.size());
        if (baseFragmentManager.getBackStackEntryCount() > 1) {
            baseFragmentManager.popBackStack();
            backTitle();
        } else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
