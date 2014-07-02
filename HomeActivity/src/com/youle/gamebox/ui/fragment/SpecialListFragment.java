package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import com.youle.gamebox.ui.adapter.SpecialAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.special.SpecialListApi;
import com.youle.gamebox.ui.bean.special.SpecialBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-6-23.
 */
public class SpecialListFragment extends NextPageFragment {
    private SpecialListApi mSpecialListApi;
    private SpecialAdapter mAdapter;
    private List<SpecialBean> specialBeanList;

    @Override
    public AbstractApi getApi() {
        return mSpecialListApi;
    }

    @Override
    public YouleBaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(specialBeanList==null){
            requestData();
        }
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(SpecialBean.class, jsonStr, "data");
    }

    private void requestData() {
        mSpecialListApi = new SpecialListApi();
        ZhidianHttpClient.request(mSpecialListApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    specialBeanList = pasreJson(jsonString);
                    mAdapter = new SpecialAdapter(getActivity(), specialBeanList);
                    getListView().setAdapter(mAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
