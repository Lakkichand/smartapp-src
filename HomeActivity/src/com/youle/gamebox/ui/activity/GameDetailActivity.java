package com.youle.gamebox.ui.activity;

import android.content.Context;
import android.content.Intent;
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
    public static final String SHOW_TAB = "SHOW_TAB";
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
        int tab = getIntent().getIntExtra(SHOW_TAB,0);
        AppDetailFragment appDetailFragment = new AppDetailFragment(id,resouce,tab);
        addFragment(appDetailFragment,true);
    }
    public static void startGameDetailActivity(Context c,long gameId,String name,int src){
        Intent intent = new Intent(c, GameDetailActivity.class) ;
        intent.putExtra(GameDetailActivity.GAME_ID,gameId);
        intent.putExtra(GameDetailActivity.GAME_NAME,name);
        intent.putExtra(GameDetailActivity.GAME_RESOUCE,src);
        c.startActivity(intent);
    }
}
