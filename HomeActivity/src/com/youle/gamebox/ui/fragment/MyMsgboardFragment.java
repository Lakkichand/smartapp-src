package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import com.youle.gamebox.ui.adapter.HomPageDymaicListAdapter;
import com.youle.gamebox.ui.adapter.MymsgboardAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.pcenter.HomePageDymaicListApi;
import com.youle.gamebox.ui.api.pcenter.MymsgboardApi;
import com.youle.gamebox.ui.bean.dynamic.DymaicListBean;
import com.youle.gamebox.ui.bean.pcenter.MymsgboardBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2014/6/18.
 */
public class MyMsgboardFragment extends NextPageFragment{
    MymsgboardApi mymsgboardApi;
    MymsgboardAdapter mymsgboardAdapter;

    @Override
    public AbstractApi getApi() {
        if(mymsgboardApi==null){
            mymsgboardApi = new MymsgboardApi();
            mymsgboardApi.setUid(Long.valueOf(1));
        }
        return mymsgboardApi;
    }

    @Override
    public YouleBaseAdapter getAdapter() {
        return mymsgboardAdapter;
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(MymsgboardBean.class, jsonStr, "data");
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mymsgboardApi = new MymsgboardApi();
        mymsgboardApi.setUid(Long.valueOf(1));
        ZhidianHttpClient.request(mymsgboardApi,new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                try {
                    List<MymsgboardBean> mymsgboardBeans = jsonToList(MymsgboardBean.class, jsonString, "data");
                    mymsgboardAdapter = new MymsgboardAdapter(getActivity(),mymsgboardBeans);
                    getListView().setAdapter(mymsgboardAdapter);
                }catch (Exception e){
                   e.printStackTrace();
                }
            }

            @Override
            public void onResultFail(String jsonString) {
                super .onResultFail(jsonString);
            }
        });


    }

    private void setData(String str){
        if (str==null) return;
        try {
            JSONObject jsonObject = new JSONObject(str);
            setTotalpage(jsonObject.getInt("totalPages"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
