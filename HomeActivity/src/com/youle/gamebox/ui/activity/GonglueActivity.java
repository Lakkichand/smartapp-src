package com.youle.gamebox.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.fragment.GonglueFragment;

/**
 * Created by Administrator on 14-6-25.
 */
public class GonglueActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GonglueFragment gonglueFragment = new GonglueFragment();
        addFragment(gonglueFragment, true);
        View titleView = LayoutInflater.from(this).inflate(R.layout.default_title_layout, null);
        titleView.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView textView = (TextView) titleView.findViewById(R.id.title);
        textView.setText("游戏攻略");
        setmTitleView(titleView);
    }
}
