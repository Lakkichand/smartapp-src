package com.youle.gamebox.ui.activity;

import android.os.Bundle;
import com.youle.gamebox.ui.fragment.DownloadManagerFragment;

/**
 * 下载管理activity
 * Created by  lihongbo on 14-6-16.
 */
public class DownLoadManagerActivity extends BaseActivity {
    public static final String TYPE = "type";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int type  = getIntent().getIntExtra(TYPE,2);
        DownloadManagerFragment fragment = new DownloadManagerFragment(type) ;
        addFragment(fragment,true);
    }
}

