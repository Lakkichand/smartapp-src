package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import com.youle.gamebox.ui.account.UserInfoCache;
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
        onSelected();
    }

    @Override
    protected String getModelName() {
        return "我的礼包";
    }

    public void onSelected(){
        if (myGiftBeanList == null) {
            if(new UserInfoCache().getUserInfo()!=null) {
                loadData();
            }else {
                showNoContentLayout(true,"未登录");
            }
        }
    }
    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(MyGiftBean.class, jsonStr, "data");
    }

    protected void loadData() {
        myGiftApi = new MyGiftApi();
        myGiftApi.setSid(new UserInfoCache().getSid());
        ZhidianHttpClient.request(myGiftApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    myGiftBeanList = pasreJson(jsonString);
                    mAdapter = new MyGiftAdapter(getActivity(), myGiftBeanList);
                    getListView().setAdapter(mAdapter);
                    if(myGiftBeanList.size()>0){
                        showNoContentLayout(false);
                    }else {
                        showNoContentLayout(true);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
