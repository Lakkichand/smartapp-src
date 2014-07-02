package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.adapter.HomPageDymaicListAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.dynamic.DymaicListApi;
import com.youle.gamebox.ui.api.pcenter.HomePageDymaicListApi;
import com.youle.gamebox.ui.bean.dynamic.DymaicListBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.LOGUtil;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2014/6/18.
 */
public class HomePageDymaicListFragment extends NextPageFragment{
    HomePageDymaicListApi homePageDymaicListApi;
    HomPageDymaicListAdapter homPageDymaicListAdapter;

    @Override
    public AbstractApi getApi() {
        if(homePageDymaicListApi==null){
            homePageDymaicListApi = new HomePageDymaicListApi();
            Bundle arguments = getArguments();
            if(arguments!=null){
            long uid = arguments.getLong(HomepageFragment.KEY_UID);
            homePageDymaicListApi.setUid(Long.valueOf(uid));
            }
        }
        return homePageDymaicListApi;
    }

    @Override
    public YouleBaseAdapter getAdapter() {
        return homPageDymaicListAdapter;
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(DymaicListBean.class, jsonStr, "data");
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = getArguments();
        if(arguments==null)return;
        long uid = arguments.getLong(HomepageFragment.KEY_UID);
        homePageDymaicListApi = new HomePageDymaicListApi();
        //homePageDymaicListApi.setUid(Long.valueOf(uid));
        homePageDymaicListApi.setUid(Long.valueOf(1));
        ZhidianHttpClient.request(homePageDymaicListApi,new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                try {
                    List<DymaicListBean> dymaicListBeans = jsonToList(DymaicListBean.class, jsonString, "data");
                    homPageDymaicListAdapter = new HomPageDymaicListAdapter(getActivity(),dymaicListBeans);
                    getListView().setAdapter(homPageDymaicListAdapter);
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
