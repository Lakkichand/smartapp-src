package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.dynamic.DymaicCommentPublicApi;
import com.youle.gamebox.ui.api.dynamic.DymaicListApi;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2014/6/18.
 */
public class DymaicListFragment extends NextPageFragment{
    @InjectView(R.id.dymaiclist_List)
    ListView mDymaiclistList;

    @Override
    public AbstractApi getApi() {
        return null;
    }

    @Override
    public YouleBaseAdapter getAdapter() {
        return null;
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return null;
    }

    @Override
    protected int getViewId() {
        return R.layout.dymaiclist_layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DymaicListApi dymaicListApi = new DymaicListApi();
        ZhidianHttpClient.request(dymaicListApi,new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
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
