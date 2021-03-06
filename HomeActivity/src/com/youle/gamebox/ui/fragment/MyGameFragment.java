package com.youle.gamebox.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import butterknife.InjectView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.GameDetailActivity;
import com.youle.gamebox.ui.activity.GameListActivity;
import com.youle.gamebox.ui.adapter.MyGameAdapter;
import com.youle.gamebox.ui.adapter.RankOnScrollListener;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.person.MyGameApi;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.AppInfoUtils;
import org.apache.http.Header;
import org.json.JSONException;
import pl.droidsonroids.gif.GifImageView;

import java.util.List;

/**
 * Created by Administrator on 14-6-4.
 */
public class MyGameFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    @InjectView(R.id.loading)
    GifImageView mLoading;
    @InjectView(R.id.noNet)
    LinearLayout mNoNet;
    @InjectView(R.id.listView)
    ListView mListView;
    @InjectView(R.id.placer)
    ImageView mPlacer;
    @InjectView(R.id.no_data_tip)
    TextView mNoDataTip;
    @InjectView(R.id.no_message_tips_placer)
    RelativeLayout mNoMessageTipsPlacer;
    private MyGameApi api;
    private MyGameAdapter adapter;
    private List<GameBean> gameBeans;
    public static final int MY_GAME = 1;
    public static final int OTHER_GAME = 2;
    private int type;
    private long uid;

    public MyGameFragment() {
    }

    public MyGameFragment(long uid, int type) {
        this.uid = uid;
        this.type = type;
    }


    //    @Override
    public AbstractApi getApi() {
        return api;
    }

    private AbstractApi initApi() {
        api = new MyGameApi();
        if (type == MY_GAME) {
            UserInfo userInfo = new UserInfoCache().getUserInfo();
            if (userInfo != null) {
                api.setSid(userInfo.getSid());
                api.setUid(userInfo.getUid() + "");
            } else {
                api.setSid("");
                api.setUid("");
            }
            api.setPackages(AppInfoUtils.getInstalledPackage(getActivity()));
        } else {
            api.setUid(uid + "");
            UserInfo userInfo = new UserInfoCache().getUserInfo();
            if (userInfo != null) {
                api.setSid(userInfo.getSid());
            }
            api.setPackages(AppInfoUtils.getInstalledPackage(getActivity()));
        }
        return api;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onLoadStart() {
        super.onLoadStart();
        mListView.setVisibility(View.GONE);
        mLoading.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSuccess(String content) {
        mLoading.setVisibility(View.GONE);
        mListView.setVisibility(View.VISIBLE);
        mNoDataTip.setVisibility(View.GONE);
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (adapter != null) {
            mListView.setAdapter(adapter);
        }else {
            mListView.addHeaderView(headView);
        }
        mListView.setOnItemClickListener(this);

    }

    @Override
    protected int getViewId() {
        return R.layout.my_game_fragment;
    }


    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(GameBean.class, jsonStr, "data");
    }

    protected void loadData() {
        initApi();
        ZhidianHttpClient.request(api, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                initAdapter(jsonString);
            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable error) {
                super.onFailure(statusCode, headers, responseString, error);
                showNoContentLayout(true);
            }
        });
    }

    private void showNoContentLayout(boolean show) {
        if(show){
            mNoMessageTipsPlacer.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
            mLoading.setVisibility(View.GONE);
        }else {
            mNoMessageTipsPlacer.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
            mLoading.setVisibility(View.GONE);
        }
    }

    @Override
    protected String getModelName() {
        return "我的游戏";
    }

    private void initAdapter(String str) {
        try {
            gameBeans = jsonToList(GameBean.class, str, "data");
            if (gameBeans.size() == 0) {
                showNoContentLayout(true);
                return;
            }
            showNoContentLayout(false);
            adapter = new MyGameAdapter(getActivity(), gameBeans);
            mListView.setAdapter(adapter);
            boolean pauseOnScroll = true; // or true
            boolean pauseOnFling = true; // or false
            PauseOnScrollListener listener = new RankOnScrollListener(adapter, ImageLoader.getInstance(), pauseOnScroll, pauseOnFling);
            mListView.setOnScrollListener(listener);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GameBean gameBean = gameBeans.get(position);
        Toast.makeText(getActivity(), gameBean.getName(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), GameDetailActivity.class);
        intent.putExtra(GameListActivity.NAME, gameBean.getName());
        intent.putExtra(GameListActivity.ID, gameBean.getId());
        startActivity(intent);
    }
}
