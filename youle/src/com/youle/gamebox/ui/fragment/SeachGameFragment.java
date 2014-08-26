package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.adapter.GameListAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.game.SearchGameApi;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-6-25.
 */
public class SeachGameFragment extends NextPageFragment {
    private SearchGameApi mSearchGameApi;
    private GameListAdapter mAdapter;
    private String keyword;
    private List<GameBean> mGameList;

    public SeachGameFragment(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public AbstractApi getApi() {
        return mSearchGameApi;
    }

    @Override
    public YouleBaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDefaultTitle("游戏搜索");
            loadData();
    }

    @Override
    protected String getModelName() {
        return "游戏搜索";
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(GameBean.class, jsonStr, "data");
    }

    protected void loadData() {
        mSearchGameApi = new SearchGameApi();
        mSearchGameApi.setKeyword(keyword);
        ZhidianHttpClient.request(mSearchGameApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    mGameList = jsonToList(GameBean.class, jsonString, "data");
                    if(mGameList.size()>0) {
                        showNoContentLayout(false);
                        mAdapter = new GameListAdapter(getActivity(), mGameList);
                        getListView().setAdapter(mAdapter);
                    }else {
                        if(!TextUtils.isEmpty(keyword)) {
                            showNoContentLayout(true, getString(R.string.serch_no_data));
                        }else {
                            showNoContentLayout(true);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
