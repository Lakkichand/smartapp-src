package com.youle.gamebox.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.fragment.AppDetailFragment;

/**
 * Created by Administrator on 14-6-23.
 */
public class GameDetailActivity extends BaseActivity {
    public static String GAME_NAME = "name";
    public static final String GAME_ID = "id";
    public static final String GAME_RESOUCE= "resouce";
    private String name;
    private long id;
    private int resouce ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        name = getIntent().getStringExtra(GAME_NAME);
        id = getIntent().getLongExtra(GAME_ID, -1);
        resouce = getIntent().getIntExtra(GAME_RESOUCE,0);
        View titleView = LayoutInflater.from(this).inflate(R.layout.default_title_layout, null);
        titleView.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView textView = (TextView) titleView.findViewById(R.id.title);
        textView.setText(name);
        setmTitleView(titleView);
        AppDetailFragment appDetailFragment = new AppDetailFragment(id,resouce);
        addFragment(appDetailFragment,true);
    }
}
