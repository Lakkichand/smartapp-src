package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.adapter.GameListAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.game.CategroryListApi;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-6-17.
 */
public class GameListFragment  extends NextPageFragment{
    private int mType  ;
    private  long mid ;
    private CategroryListApi mApi;
    private GameListAdapter mAdapter;
    private TextView mTitleText ;
    private String mTitle;

    public GameListFragment(int mType, long mid,String name) {
        super();
        this.mType = mType;
        this.mid = mid;
        this.mTitle = name ;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mAdapter!=null){
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public AbstractApi getApi() {
        return mApi;
    }

    @Override
    public YouleBaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(GameBean.class,jsonStr,"data");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View titleView = LayoutInflater.from(getActivity()).inflate(R.layout.default_title_layout,null);
        setTitleView(titleView);
        titleView.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        mTitleText= (TextView) titleView.findViewById(R.id.title);
        mTitleText.setText(mTitle);
        if(mAdapter==null){
            loadData();
        }else {
            getListView().setAdapter(mAdapter);
        }
    }

    @Override
    protected String getModelName() {
        return "游戏搜索";
    }


    public void loadData() {
        mApi = new CategroryListApi(mType);
        mApi.setId(mid);
        ZhidianHttpClient.request(mApi,new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    List<GameBean> list = pasreJson(jsonString);
                    if(list.size()>0) {
                        mAdapter = new GameListAdapter(getActivity(), list);
                        getListView().setAdapter(mAdapter);
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
