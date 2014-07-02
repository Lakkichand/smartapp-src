package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.ButterKnife;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.LOGUtil;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-5-8.
 */
public abstract class NextPageFragment extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<ListView> {
    protected PullToRefreshListView listView;

    public abstract AbstractApi getApi();

    public abstract YouleBaseAdapter getAdapter();

    public abstract List pasreJson(String jsonStr) throws JSONException;

    private int totalpage = 0;

    public int getTotalpage() {
        return totalpage;
    }

    public ListView getListView() {
        return listView.getRefreshableView();
    }

    public void setTotalpage(int totalpage) {
        this.totalpage = totalpage;
    }

    private View mView = null;
    private LinearLayout mBottomLayou;
    private LinearLayout mHeadLayout ;
    private TextView mStartLoad ;
    private View mContent ;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.youle_list_base_fragment, container, false);
            listView = (PullToRefreshListView) mView.findViewById(R.id.listView);
            mBottomLayou = (LinearLayout) mView.findViewById(R.id.bottomView);
            mHeadLayout= (LinearLayout) mView.findViewById(R.id.headLayout);
            mStartLoad = (TextView) mView.findViewById(R.id.loadStart);
            mContent = mView.findViewById(R.id.contentLayout);
        }else if(mView.getParent()!=null){
            ((ViewGroup)mView.getParent()).removeView(mView);
        }
        return mView;
    }

    public void setOnScrollListener(AbsListView.OnScrollListener listener) {
        listView.setOnScrollListener(listener);
    }

//    @Override
//    public void onLoadStart() {
//        mStartLoad.setVisibility(View.VISIBLE);
//        mContent.setVisibility(View.GONE);
//    }
//
//    @Override
//    public void onSuccess(String content) {
//        mStartLoad.setVisibility(View.GONE);
//        mContent.setVisibility(View.VISIBLE);
//    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView.setOnRefreshListener(this);
        listView.setMode(PullToRefreshBase.Mode.BOTH);
    }

    @Override
    protected int getViewId() {
        return -1;
    }

    @Override
    public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
        refresh();
    }

    @Override
    public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
        loadNextPage();
    }

    private void refresh() {
        final AbstractApi api = getApi();
        api.setPageNo(1);
        requestData(api);
    }

    private void loadNextPage() {
        AbstractApi api = getApi();
        if (api.getPageNo() < getTotalpage()) {
            api.setPageNo(api.getPageNo() + 1);
            requestData(api);
        } else {
            listView.post(new Runnable() {
                @Override
                public void run() {
                    listView.onRefreshComplete();
                }
            });
        }
    }

    private void requestData(final AbstractApi abstractApi) {
        ZhidianHttpClient.request(abstractApi, new JsonHttpListener(false) {
            @Override
            public void onRequestSuccess(String content) {
                Toast.makeText(getActivity(), "onRequestSuccess", Toast.LENGTH_SHORT).show();
                listView.onRefreshComplete();
                try {
                    List list = pasreJson(content);
                    if (list != null) {
                        if (abstractApi.getPageNo() > 1) {
                            getAdapter().addDate(list);
                        } else {
                            getAdapter().resetDate(list);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
                AbstractApi api = getApi();
                api.setPageNo(api.getPageNo() - 1);
            }
        });
    }

    protected  void setBottomView(View view){
        mBottomLayou.removeAllViews();
        mBottomLayou.addView(view);
    }
    protected void setHeadView(View view){
        mHeadLayout.removeAllViews();
        mBottomLayou.addView(view);
    }
}
