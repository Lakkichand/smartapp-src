package com.youle.gamebox.ui.activity;

import android.os.Bundle;
import com.youle.gamebox.ui.fragment.BaseFragment;
import com.youle.gamebox.ui.fragment.LoginFragment;
import com.youle.gamebox.ui.fragment.PCenterFragment;

/**
 * Created by Administrator on 2014/5/12.
 */
public class AppDetailActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PCenterFragment pCenterFragment = new PCenterFragment();
        addFragment(pCenterFragment, true);

    }

    public void addFragment(BaseFragment fragment){
        addFragment(fragment, true);
    }

}
