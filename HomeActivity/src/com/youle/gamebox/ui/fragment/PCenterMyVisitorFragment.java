package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.adapter.GridItemClickListener;
import com.youle.gamebox.ui.adapter.MyVisitorAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.pcenter.MyVisitorApi;
import com.youle.gamebox.ui.bean.pcenter.MyVisitorBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.view.PullRefreshAndLoadMoreListView;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 2014/6/24.
 */
public class PCenterMyVisitorFragment extends NextPageFragment{
    @InjectView(R.id.pc_myvisitor_pullGridView)
    PullRefreshAndLoadMoreListView mPcMyvisitorPullGridView;
    @InjectView(R.id.no_message_tips_placer)
    View mNoContentPlacer;

    MyVisitorAdapter myVisitorAdapter;
    MyVisitorApi myVisitorApi;
    List<MyVisitorBean> myVisitorBeans = null;

//    @Override
//    protected int getViewId() {
//        return R.layout.pcmyvisitor_layout;
//    }

    @Override
    protected String getModelName() {
        return "我的访客";
    }

    @Override
    public AbstractApi getApi() {
        return myVisitorApi;
    }

    @Override
    public YouleBaseAdapter getAdapter() {
        return myVisitorAdapter;
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(MyVisitorBean.class,jsonStr,"data");
    }

    @Override
    protected void loadData() {

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (new UserInfoCache().getUserInfo() == null) {
            showNoContentLayout(true);
        } else {
            showNoContentLayout(false);
            myVisitorApi = new MyVisitorApi();
            UserInfoCache userInfoCache = new UserInfoCache();
            myVisitorApi.setSid(userInfoCache.getSid());
            ZhidianHttpClient.request(myVisitorApi, new JsonHttpListener(this) {
                @Override
                public void onRequestSuccess(String jsonString) {
                    super.onRequestSuccess(jsonString);
                    try {
                        myVisitorBeans = jsonToList(MyVisitorBean.class, jsonString, "data");
                        if (myVisitorBeans.size() == 0) {
                            showNoContentLayout(true);
                        } else {
                            showNoContentLayout(false);
                            myVisitorAdapter = new MyVisitorAdapter(getActivity(), myVisitorBeans);
                            myVisitorAdapter.setNumColumns(4);
                            getListView().setAdapter(myVisitorAdapter);
                            myVisitorAdapter.setOnGridClickListener(onGridItemClick);
//                            mPcMyvisitorPullGridView.setMode(PullToRefreshBase.Mode.BOTH);
//                            mPcMyvisitorPullGridView.setOnItemClickListener(onItemClickListener);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    private GridItemClickListener onGridItemClick = new GridItemClickListener() {
        @Override
        public void onGridItemClicked(View v, int position, long itemId) {
            MyVisitorBean myVisitorBean = myVisitorBeans.get(position);
            CommonActivity.startOtherUserDetail(getActivity(), myVisitorBean.getUid(), myVisitorBean.getNickName());
        }
    } ;



//    public void showNoContentLayout(boolean isShow) {
//        if (isShow) {
//            mNoContentPlacer.setVisibility(View.VISIBLE);
//        } else {
//            mNoContentPlacer.setVisibility(View.GONE);
//        }
//    }
}
