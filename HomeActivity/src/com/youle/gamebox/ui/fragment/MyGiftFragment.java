package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import com.youle.gamebox.ui.adapter.MyGiftAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.MyGiftApi;
import com.youle.gamebox.ui.bean.MyGiftBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-6-23.
 */
public class MyGiftFragment extends NextPageFragment {
    private MyGiftApi myGiftApi;
    private MyGiftAdapter mAdapter;
    private List<MyGiftBean> myGiftBeanList;

    @Override
    public AbstractApi getApi() {
        return myGiftApi;
    }

    @Override
    public YouleBaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void onSelected(){
        if (myGiftBeanList == null) {
            loadData();
        }
    }
    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(MyGiftBean.class, jsonStr, "data");
    }

    private void loadData() {
        myGiftApi = new MyGiftApi();
        ZhidianHttpClient.request(myGiftApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    myGiftBeanList = pasreJson(jsonString);
                    mAdapter = new MyGiftAdapter(getActivity(), myGiftBeanList);
                    getListView().setAdapter(mAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
