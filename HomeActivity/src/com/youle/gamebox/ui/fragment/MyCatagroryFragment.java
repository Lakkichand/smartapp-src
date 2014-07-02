package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import com.ta.mvc.common.TAResponse;
import com.youle.gamebox.ui.adapter.CatagroryAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.MyCategroyApi;
import com.youle.gamebox.ui.bean.AppInfoBean;
import com.youle.gamebox.ui.bean.CatagroryBean;
import com.youle.gamebox.ui.greendao.JsonEntry;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.ModelConst;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-6-20.
 */
public class MyCatagroryFragment extends NextPageFragment{
    MyCategroyApi categroyApi ;
    List<CatagroryBean> mList ;
    CatagroryAdapter mAdapter ;
    @Override
    public AbstractApi getApi() {
        return categroyApi;
    }

    @Override
    public YouleBaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(CatagroryBean.class,jsonStr,"data");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mAdapter==null){
            loadCach(ModelConst.MY_CATAGRORY);
            getRemoteData();
        }
    }

    private void getRemoteData(){
        categroyApi = new MyCategroyApi();
        StringBuilder sb = new StringBuilder();
        List<AppInfoBean> apps= AppInfoUtils.getPhoneAppInfo(getActivity()) ;
        for (AppInfoBean b:apps){
            sb.append(b.getPackageName()).append(",");
        }
        categroyApi.setPackages(sb.toString());
        ZhidianHttpClient.request(categroyApi,new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                cacheJson(ModelConst.MY_CATAGRORY,jsonString,categroyApi);
            }
        });
    }

    @Override
    public void onSuccess(TAResponse response) {
        JsonEntry jsonEntry = (JsonEntry) response.getData();
        if(jsonEntry==null) return;
        try {
            mList = jsonToList(CatagroryBean.class,jsonEntry.getJson(),"data");
            mAdapter = new CatagroryAdapter(getActivity(),mList);
            getListView().setAdapter(mAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
