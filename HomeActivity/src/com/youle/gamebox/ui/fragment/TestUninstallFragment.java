package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import butterknife.InjectView;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.ta.mvc.common.TAResponse;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.adapter.UnIndtallAdapter;
import com.youle.gamebox.ui.bean.AppInfoBean;
import com.youle.gamebox.ui.receiver.PackageReceiver;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.LOGUtil;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-4-23.
 */
public class TestUninstallFragment extends BaseFragment {
//    @InjectView(R.id.hello)
//    TextView mHello;
//    @InjectView(R.id.test)
//    TextView mTest;
    @InjectView(R.id.listView)
    PullToRefreshListView mListView;
    @Override
    protected int getViewId() {
        return R.layout.main;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();
    }

    protected void loadData() {
        List<AppInfoBean> phoneAppInfo = AppInfoUtils.getPhoneAppInfo(getActivity());

        String str = "";
        for (int i = 0; i <phoneAppInfo.size() ; i++) {
            AppInfoBean appInfoBean = phoneAppInfo.get(i);
            str += appInfoBean.getPackageName() + "|" + appInfoBean.getName()+"|" + appInfoBean.getVersion()+"|" + appInfoBean.getVersionCode() + "\n";


        }
        LOGUtil.d("test","----"+str);

        UnIndtallAdapter adapter = new UnIndtallAdapter(getActivity(), phoneAppInfo);
        PackageReceiver.setHandler(adapter.getRemoveHandler());
        mListView.setAdapter(adapter);
       /* String appInfoPack = AppInfoUtils.getPhoneAppInfoPack(phoneAppInfo);
        final SpreeGameListApi packageCheckApi = new SpreeGameListApi();
        packageCheckApi.setPackages(appInfoPack);
        ZhidianHttpClient.request(packageCheckApi, new JsonHttpListener(getActivity()) {
            @Override
            public void onRequestSuccess(String jsonString) {
//                cacheJson(ModelConst.LATESTGAME, jsonString, packageCheckApi);
                try {
                    pasrseJson(jsonString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

    @Override
    public void onSuccess(TAResponse response) {
       /* try {
            pasrseJson(response.getData().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    private void pasrseJson(String json) throws JSONException {
        List<AppInfoBean> list = jsonToList(AppInfoBean.class, json, "games");
        UnIndtallAdapter adapter = new UnIndtallAdapter(getActivity(), list);
        PackageReceiver.setHandler(adapter.getRemoveHandler());
        mListView.setAdapter(adapter);
    }

}
