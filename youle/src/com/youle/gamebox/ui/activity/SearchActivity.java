package com.youle.gamebox.ui.activity;

import android.os.Bundle;
import com.youle.gamebox.ui.fragment.SerchFragment;

/**
 * Created by Administrator on 14-6-25.
 */
public class SearchActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SerchFragment serchFragment = new SerchFragment();
        addFragment(serchFragment, true);
    }
}
