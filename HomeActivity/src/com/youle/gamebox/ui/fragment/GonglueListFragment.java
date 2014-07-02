package com.youle.gamebox.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.adapter.GonglueAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.SerchGonglueApi;
import com.youle.gamebox.ui.bean.GonglueBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-6-25.
 */
public class GonglueListFragment extends NextPageFragment {
    private SerchGonglueApi mSerchGonglueApi;
    private GonglueAdapter mAdapter;
    private List<GonglueBean> gonglueBeanList;
    private String keyWorld;
    private String gameId;

    public void setKeyWorld(String keyWorld) {
        this.keyWorld = keyWorld;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    @Override
    public AbstractApi getApi() {
        return mSerchGonglueApi;
    }

    @Override
    public YouleBaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(GonglueBean.class, jsonStr, "data");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        if(keyWorld!=null){
//            loadData();
//        }else if(gonglueBeanList==null){
//            loadData();
//        }
        loadData();
    }

    private void loadData() {
        mSerchGonglueApi = new SerchGonglueApi();
        mSerchGonglueApi.setKeyword(keyWorld);
        mSerchGonglueApi.setGameId(gameId);
        ZhidianHttpClient.request(mSerchGonglueApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    gonglueBeanList = pasreJson(jsonString);
                    mAdapter = new GonglueAdapter(getActivity(), gonglueBeanList);
                    getListView().setAdapter(mAdapter);
                    gotoGolueDetail();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void gotoGolueDetail() {
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GonglueBean gonglueBean = mAdapter.getItem(position-1);
                if (gonglueBean != null) {
                    StagoryDetailFragment stagoryDetailFragment = new StagoryDetailFragment(gonglueBean.getId() + "");
                    ((BaseActivity) getActivity()).addFragment(stagoryDetailFragment, true);
                }
            }
        });
    }
}
