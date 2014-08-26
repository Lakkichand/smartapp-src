package com.youle.gamebox.ui.activity;

import android.os.Bundle;
import com.youle.gamebox.ui.fragment.GameListFragment;

/**
 * Created by Administrator on 14-6-20.
 */
public class GameListActivity extends BaseActivity {
    public static  String TYPE = "type";
    public static  String ID = "id";
    public static  String NAME = "name";
    private int type ;
    private long id ;
    private String name ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getIntent().getIntExtra(TYPE,1);
        id = getIntent().getLongExtra(ID,1);
        name = getIntent().getStringExtra(NAME);
        GameListFragment gameListFragment = new GameListFragment(type,id,name);
        addFragment(gameListFragment,true);
    }
}
