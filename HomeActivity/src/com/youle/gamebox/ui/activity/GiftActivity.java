package com.youle.gamebox.ui.activity;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.fragment.GiftFragment;

/**
 * Created by Administrator on 14-6-24.
 */
public class GiftActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GiftFragment giftFragment = new GiftFragment();
        addFragment(giftFragment, true);
        View titleView = LayoutInflater.from(this).inflate(R.layout.default_title_layout, null);
        titleView.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView textView = (TextView) titleView.findViewById(R.id.title);
        textView.setText("领取礼包");
        setmTitleView(titleView);
    }
}
