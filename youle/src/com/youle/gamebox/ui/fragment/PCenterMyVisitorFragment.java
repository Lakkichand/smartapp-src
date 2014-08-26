package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import butterknife.InjectView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.adapter.MyVisitorAdapter;
import com.youle.gamebox.ui.api.pcenter.MyVisitorApi;
import com.youle.gamebox.ui.bean.pcenter.MyVisitorBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.LOGUtil;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

import static com.handmark.pulltorefresh.library.PullToRefreshBase.Mode.BOTH;

/**
 * Created by Administrator on 2014/6/24.
 */
public class PCenterMyVisitorFragment extends BaseFragment {
    @InjectView(R.id.pc_myvisitor_pullGridView)
    PullToRefreshGridView mPcMyvisitorPullGridView;
    @InjectView(R.id.no_message_tips_placer)
    View mNoContentPlacer;

    MyVisitorAdapter myVisitorAdapter;
    MyVisitorApi myVisitorApi;
    List<MyVisitorBean> myVisitorBeans = null;

    @Override
    protected int getViewId() {
        return R.layout.pcmyvisitor_layout;
    }

    @Override
    protected String getModelName() {
        return "我的访客";
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
                            mPcMyvisitorPullGridView.setAdapter(myVisitorAdapter);
                            mPcMyvisitorPullGridView.setMode(PullToRefreshBase.Mode.BOTH);
                            mPcMyvisitorPullGridView.setOnItemClickListener(onItemClickListener);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MyVisitorBean myVisitorBean = myVisitorBeans.get(position);
            CommonActivity.startOtherUserDetail(getActivity(), myVisitorBean.getUid(), myVisitorBean.getNickName());
        }

    };



    public void showNoContentLayout(boolean isShow) {
        if (isShow) {
            mNoContentPlacer.setVisibility(View.VISIBLE);
        } else {
            mNoContentPlacer.setVisibility(View.GONE);
        }
    }
}
