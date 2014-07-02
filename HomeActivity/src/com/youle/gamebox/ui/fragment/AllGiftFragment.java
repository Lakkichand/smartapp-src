package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.adapter.AllGiftAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.GetAllSpreeApi;
import com.youle.gamebox.ui.bean.GiftBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-6-24.
 */
public class AllGiftFragment extends NextPageFragment {
    private GetAllSpreeApi mGetAllSpreeApi;
    private AllGiftAdapter mAdapter;
    private List<GiftBean> giftBeanList;
    private String keyWorld ;
    private String gameId ;
    public AllGiftFragment(String keyWorld) {
        this.keyWorld = keyWorld;
    }

    public void setKeyWorld(String keyWorld) {
        this.keyWorld = keyWorld;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public AllGiftFragment() {
    }

    @Override
    public AbstractApi getApi() {
        return mGetAllSpreeApi;
    }

    @Override
    public YouleBaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(GiftBean.class, jsonStr, "data");
    }

    public void onSelected(){
        if (giftBeanList == null||keyWorld!=null) {
            loadData();
        }
    }

    private void loadData() {
        mGetAllSpreeApi = new GetAllSpreeApi();
        mGetAllSpreeApi.setKeyword(keyWorld);
        mGetAllSpreeApi.setGameId(gameId);
        mGetAllSpreeApi.setSid(new UserInfoCache().getSid());
        ZhidianHttpClient.request(mGetAllSpreeApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    giftBeanList = pasreJson(jsonString);
                    mAdapter = new AllGiftAdapter(getActivity(), giftBeanList);
                    getListView().setAdapter(mAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
