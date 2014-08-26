package com.youle.gamebox.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.fragment.MessageManagerFragment;

/**
 * Created by Administrator on 14-6-26.
 */
public class MessageActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MessageManagerFragment messageManagerFragment = new MessageManagerFragment();
        addFragment(messageManagerFragment, true);
        initTitleView();
    }

    private void initTitleView() {
        View titleView = LayoutInflater.from(this).inflate(R.layout.default_title_layout, null);
        titleView.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView title = (TextView) titleView.findViewById(R.id.title);
        title.setText(R.string.message_manager);
        setmTitleView(titleView);
    }
    
    @Override
    public void loadStart() {
    }
}
