package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.GameDetailActivity;
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
    private String keyWorld;
    private String gameId;

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
        if (giftBeanList == null) {
            loadData();
        }
        if(!TextUtils.isEmpty(keyWorld)&&gameId==null){
                setDefaultTitle("礼包搜索");
        }
    }

    @Override
    protected String getModelName() {
        return "礼包";
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(GiftBean.class, jsonStr, "data");
    }

    public void onSelected() {
        if (giftBeanList == null) {
            loadData();
        }
    }

    protected void loadData() {
        mGetAllSpreeApi = new GetAllSpreeApi();
        mGetAllSpreeApi.setKeyword(keyWorld);
        mGetAllSpreeApi.setGameId(gameId);
        mGetAllSpreeApi.setSid(new UserInfoCache().getSid());
        ZhidianHttpClient.request(mGetAllSpreeApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    giftBeanList = pasreJson(jsonString);
                    if (giftBeanList.size() > 0) {
                        showNoContentLayout(false);
                        mAdapter = new AllGiftAdapter(getActivity(), giftBeanList);
                        getListView().setAdapter(mAdapter);
                    } else {
                        if(!TextUtils.isEmpty(keyWorld)) {
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
