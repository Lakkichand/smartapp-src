package com.youle.gamebox.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.activity.GameDetailActivity;
import com.youle.gamebox.ui.api.SearchHomeApi;
import com.youle.gamebox.ui.bean.MiniGameBean;
import com.youle.gamebox.ui.bean.SearchBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.UIUtil;
import org.json.JSONException;

/**
 * Created by Administrator on 14-6-25.
 */
public class SerchFragment extends BaseFragment{
    @InjectView(R.id.game)
    RadioButton mGame;
    @InjectView(R.id.gift)
    RadioButton mGift;
    @InjectView(R.id.gonglue)
    RadioButton mGonglue;
    @InjectView(R.id.searchRadioGroup)
    RadioGroup mSearchRadioGroup;
    @InjectView(R.id.keyEdit)
    EditText mKeyEdit;
    @InjectView(R.id.search_but)
    LinearLayout mSearchBut;
    @InjectView(R.id.likeGameLayout)
    LinearLayout mLikeGameLayout;
    @InjectView(R.id.hotSerchLayout)
    LinearLayout mHotSerchLayout;
    private SearchBean mSearchBean;
    private final int GAME = 0 ;//游戏
    private final int STAGRY= 1 ;//游戏
    private final int GIFT = 2 ;//游戏
    private int currentSech = GAME ;
    @Override
    protected int getViewId() {
        return R.layout.fragment_serch_layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initTitle();
        mSearchRadioGroup.setOnCheckedChangeListener(onCheckedChangeListener);
        mSearchBut.setOnClickListener(onSerchClickListener);
        if (mSearchBean == null) {
            loadData();
        }
    }

    private void initTitle() {
        View tititlView = LayoutInflater.from(getActivity()).inflate(R.layout.default_title_layout, null);
        tititlView.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        TextView text = (TextView) tititlView.findViewById(R.id.title);
        text.setText("搜索");
        setTitleView(tititlView);
    }

    private void initGames() {
        if (getActivity() == null) return;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        for (MiniGameBean b : mSearchBean.getGames()) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.litle_game_layout, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.gameIcon);
            TextView textView = (TextView) view.findViewById(R.id.gameName);
            ImageLoadUtil.displayImage(b.getIconUrl(), imageView);
            textView.setText(b.getName());
            mLikeGameLayout.addView(view, layoutParams);
            view.setTag(b);
            view.setOnClickListener(onClickListener);
        }
        if (mSearchBean.getGames().size() < 4) {
            for (int i = mSearchBean.getGames().size(); i < 4; i++) {
                TextView t = new TextView(getActivity());
                mLikeGameLayout.addView(t, layoutParams);
            }
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MiniGameBean b = (MiniGameBean) v.getTag();
            Intent intent = new Intent(getActivity(), GameDetailActivity.class);
            intent.putExtra(GameDetailActivity.GAME_ID,b.getId());
            intent.putExtra(GameDetailActivity.GAME_RESOUCE,b.getSource());
            intent.putExtra(GameDetailActivity.GAME_NAME,b.getName());
            startActivity(intent);
        }
    };


    private RadioGroup.OnCheckedChangeListener onCheckedChangeListener= new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if(checkedId == R.id.gift){
                currentSech = GIFT;
            }else if(checkedId == R.id.game){
                currentSech = GAME ;
            }else {
                currentSech = STAGRY;
            }
        }
    };
    private View.OnClickListener onSerchClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
             if(v.getId() == R.id.search_but){
                 search() ;
             }
        }
    };

    private void search() {
        String  keyword = mKeyEdit.getText().toString().trim();
        if(keyword.length()>0){
            if(currentSech==GAME){
               SeachGameFragment searchGameFragment = new SeachGameFragment(keyword);
                ((BaseActivity)getActivity()).addFragment(searchGameFragment,true);
            }else  if(currentSech == GIFT){
                AllGiftFragment all = new AllGiftFragment(keyword);
                ((BaseActivity)getActivity()).addFragment(all,true);
            }else {
                GonglueListFragment g = new GonglueListFragment();
                g.setKeyWorld(keyword);
                ((BaseActivity)getActivity()).addFragment(g,true);
            }
        }else {
            UIUtil.toast(getActivity(),R.string.input_not_nul);
        }
    }

    private void loadData() {
        SearchHomeApi searchHomeApi = new SearchHomeApi();
        searchHomeApi.setSid(new UserInfoCache().getSid());
        ZhidianHttpClient.request(searchHomeApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    mSearchBean = jsonToBean(SearchBean.class, jsonString);
                    if (mSearchBean != null && getActivity() != null) {
                        initGames();
                        initHotTabs();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initHotTabs() {
        LinearLayout linearLayout = null;
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,getResources().getDimensionPixelSize(R.dimen.tab_height));
        p.weight = 1 ;
        for (int i = 0; i < mSearchBean.getTabs().size(); i++) {
            if (i % 3 == 0) {
                linearLayout = new LinearLayout(getActivity());
                linearLayout.setWeightSum(3);
                mHotSerchLayout.addView(linearLayout);
            }
            TextView textView = new TextView(getActivity());
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(getResources().getColor(R.color.tag_indicator_theme));
            textView.setText(mSearchBean.getTabs().get(i));
            if(i%2!=0){
                textView.setBackgroundColor(getResources().getColor(R.color.home_item_bg_1));
            }
           linearLayout.addView(textView,p);
        }
        for (int i = 0; i <= mSearchBean.getTabs().size() % 3; i++) {
            TextView textView = new TextView(getActivity());
            linearLayout.addView(textView,p);
        }
    }

}
