package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import com.ta.mvc.common.TAResponse;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.YouleAplication;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.adapter.CatagroryAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.MyCategroyApi;
import com.youle.gamebox.ui.bean.AppInfoBean;
import com.youle.gamebox.ui.bean.CatagroryBean;
import com.youle.gamebox.ui.greendao.JsonEntry;
import com.youle.gamebox.ui.greendao.UserInfo;
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
        totalpage=1 ;
        if(mAdapter==null){
//            setDefaultTitle(getString(R.string.myStagry));
            loadData();
        }
    }

    protected void loadData(){
        categroyApi = new MyCategroyApi();
        UserInfo userInfo = new UserInfoCache().getUserInfo() ;
        if(userInfo==null) {
        }else {
            categroyApi.setSid(new UserInfoCache().getSid());
        }
        StringBuilder sb = new StringBuilder();
        List<AppInfoBean> apps = AppInfoUtils.getPhoneAppInfo(getActivity());
        for (AppInfoBean b : apps) {
            sb.append(b.getPackageName()).append(",");
        }
        categroyApi.setPackages(sb.toString());
        ZhidianHttpClient.request(categroyApi,new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    mList = jsonToList(CatagroryBean.class,jsonString,"data");
                    if(mList.size()==0){
                        showNoContentLayout(true);
                    }else {
                        mAdapter = new CatagroryAdapter(getActivity(), mList);
                        getListView().setAdapter(mAdapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected String getModelName() {
        return "我的攻略";
    }
}
