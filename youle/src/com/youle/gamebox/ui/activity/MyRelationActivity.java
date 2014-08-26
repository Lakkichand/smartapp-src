package com.youle.gamebox.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.api.MyCategroyApi;
import com.youle.gamebox.ui.fragment.*;

/**
 * Created by Administrator on 14-6-20.
 */
public class MyRelationActivity extends BaseActivity {
    public static String RELATION = "RELATION";
    public static final String  ID = "id";//专题ID
    public static int CATAGRORY = 1;//我的攻略
    public static int GIFT = 2;//我的礼包
    public static int SPECIAL = 3;//专题
    public static int SPECIAL_DETAIL = 4 ;//专题详情
    public static int GIFT_DETAIL = 5 ;//专题详情
    public static int STAGRY_DETAIL = 6 ;//攻略详情
    public static int WEB = 7 ;//网页详情
    public static int GAME_DETAIL= 8 ;//游戏详情
    public static final String URL="url";
    private int relation;
    private String id ;
    private String  url ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        relation = getIntent().getIntExtra(RELATION, 1);
        id = getIntent().getStringExtra(ID);
        url = getIntent().getStringExtra(URL);
        if (relation == CATAGRORY) {
            MyCatagroryFragment myCatagroryFragment = new MyCatagroryFragment();
            addFragment(myCatagroryFragment, true);
        } else if (relation == GIFT) {
            MyGiftFragment myGiftFragment = new MyGiftFragment();
            addFragment(myGiftFragment, true);
        } else if (relation == SPECIAL) {
            SpecialListFragment specialListFragment = new SpecialListFragment();
            addFragment(specialListFragment, true);
        }else if(relation==SPECIAL_DETAIL){
            SpecilDetailFragment specilDetailFragment = new SpecilDetailFragment(id);
            addFragment(specilDetailFragment,true);
        }else  if(relation == GIFT_DETAIL){
            GiftDetailFragment giftDetailFragment = new GiftDetailFragment(id);
            addFragment(giftDetailFragment,true);
        } else  if(relation == STAGRY_DETAIL){
            StagoryDetailFragment stagoryDetailFragment= new StagoryDetailFragment(id);
            addFragment(stagoryDetailFragment,true);
        } else  if(relation == WEB){
            WebViewFragment webViewFragment = new WebViewFragment("",url);
            addFragment(webViewFragment,true);
        } else  if(relation == GAME_DETAIL){
            AppDetailGamesFragment appDetailGamesFragment = new AppDetailGamesFragment(Long.parseLong(id),1);
            addFragment(appDetailGamesFragment,true);
        }
        initTitle();
    }

    private void initTitle() {
        View titleView = LayoutInflater.from(this).inflate(R.layout.default_title_layout, null);
        titleView.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView t = (TextView) titleView.findViewById(R.id.title);
        if (relation == CATAGRORY) {
            t.setText("我的攻略");
        } else if (relation == GIFT) {
            t.setText("我的礼包");
        } else if (relation == SPECIAL) {
            t.setText("精选专题");
        }
        setmTitleView(titleView);
    }
}
