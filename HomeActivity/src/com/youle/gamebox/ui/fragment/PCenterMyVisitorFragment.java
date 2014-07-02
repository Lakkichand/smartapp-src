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

import java.util.ArrayList;
import java.util.List;

import static com.handmark.pulltorefresh.library.PullToRefreshBase.Mode.BOTH;

/**
 * Created by Administrator on 2014/6/24.
 */
public class PCenterMyVisitorFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    @InjectView(R.id.pc_myvisitor_pullGridView)
    PullToRefreshGridView mPcMyvisitorPullGridView;
    MyVisitorAdapter myVisitorAdapter;
    MyVisitorApi myVisitorApi;
    List<MyVisitorBean> myVisitorBeans =null;
    @Override
    protected int getViewId() {
        return R.layout.pcmyvisitor_layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myVisitorApi = new MyVisitorApi();
        myVisitorApi.setSid(new UserInfoCache().getSid());
        myVisitorBeans = addData();
        myVisitorAdapter = new MyVisitorAdapter(getActivity(),myVisitorBeans );
        mPcMyvisitorPullGridView.setAdapter(myVisitorAdapter);
        mPcMyvisitorPullGridView.setMode(BOTH);
        mPcMyvisitorPullGridView.setOnItemClickListener(this);

        /*ZhidianHttpClient.request(myVisitorApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                try {
                    List<MyVisitorBean> myVisitorBeans = jsonToList(MyVisitorBean.class, jsonString, "data");
                    myVisitorAdapter = new MyVisitorAdapter(getActivity(), addData());
                    mPcMyvisitorPullGridView.setAdapter(myVisitorAdapter);
                    mPcMyvisitorPullGridView.setMode(PullToRefreshBase.Mode.BOTH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
            }
        });*/

    }
    
    
    private  List<MyVisitorBean> addData(){
        List<MyVisitorBean> myVisitorBeans = new ArrayList<MyVisitorBean>();
        for (int i = 0; i < 15; i++) {
            MyVisitorBean myVisitorBean = new MyVisitorBean();
            myVisitorBean.setUid(Long.valueOf(1));
            myVisitorBean.setNickName("超神"+i+"几连杀");
            myVisitorBean.setAvatarUrl("http://oss.aliyuncs.com/zdy6/wap/icon/201405281143468419.png");
            myVisitorBean.setTime("2014-6-"+i);
            myVisitorBeans.add(myVisitorBean);
        }
        return myVisitorBeans;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MyVisitorBean myVisitorBean = myVisitorBeans.get(position);
        Bundle bundle = new Bundle();
        bundle.putString(HomepageFragment.KEY_TITLE, myVisitorBean.getNickName());
        bundle.putLong(HomepageFragment.KEY_UID, myVisitorBean.getUid());
        CommonActivity.startCommonA(getActivity(), CommonActivity.FRAGMENT_HOMEPAGE, bundle);

    }
}
