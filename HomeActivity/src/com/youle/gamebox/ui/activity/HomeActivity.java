package com.youle.gamebox.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;

import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.view.SlidingPaneLayout;

/**
 * Created by Administrator on 14-5-12.
 */
public class HomeActivity extends BaseActivity implements View.OnClickListener {
    SlidingPaneLayout layout;
    @InjectView(R.id.rankShowLeft)
    RelativeLayout mRankShowLeft;
    @InjectView(R.id.titleSerch)
    ImageView mTitleSerch;
    @InjectView(R.id.downLoadManager)
    RelativeLayout mDownLoadManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home);
        ButterKnife.inject(this);
        layout = (SlidingPaneLayout) findViewById(R.id.slidingmenu);
        mDownLoadManager.setOnClickListener(this);
        mRankShowLeft.setOnClickListener(this);
        mTitleSerch.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rankShowLeft:
                if (layout.isOpen()) {
                    layout.closePane();
                } else {
                    layout.openPane();
                }
                break;
            case R.id.downLoadManager:
                Intent intent = new Intent(this, DownLoadManagerActivity.class);
                startActivity(intent);
                break;
            case R.id.titleSerch:
                Intent se = new Intent(this, SearchActivity.class);
                startActivity(se);
                break;
        }
    }
}
