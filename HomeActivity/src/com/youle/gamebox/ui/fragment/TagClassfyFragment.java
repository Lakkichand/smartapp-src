package com.youle.gamebox.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import butterknife.InjectView;
import com.ta.mvc.common.TAResponse;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.GameListActivity;
import com.youle.gamebox.ui.adapter.TagClassfyAdapter;
import com.youle.gamebox.ui.api.GameTagApi;
import com.youle.gamebox.ui.api.game.CategroryListApi;
import com.youle.gamebox.ui.bean.GameTagBean;
import com.youle.gamebox.ui.greendao.JsonEntry;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.util.ModelConst;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-5-30.
 */
public class TagClassfyFragment extends BaseFragment implements AdapterView.OnItemClickListener{
    @InjectView(R.id.tagGrid)
    GridView mTagGrid;

    List<GameTagBean> tagBeanList;
    TagClassfyAdapter  adapter ;
    protected int getViewId() {
        return R.layout.fragment_tag_calssfy;
    }

    @Override
    protected String getModelName() {
        return "标签分类";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTagGrid.setOnItemClickListener(this);
        if (tagBeanList == null) {
            getLocalData();
            loadData();
        }else{
           mTagGrid.setAdapter(adapter);
        }
    }

    private void getLocalData() {
        loadCach(ModelConst.GAME_TAG);
    }


    protected void loadData() {
        final GameTagApi gameTagApi = new GameTagApi();
        ZhidianHttpClient.request(gameTagApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                cacheJson(ModelConst.GAME_TAG, jsonString, gameTagApi);
            }
        });
    }


    @Override
    public void onSuccess(TAResponse response) {
        JsonEntry jsonEntry = (JsonEntry) response.getData();
        if (jsonEntry==null) return;
        parseJson(jsonEntry.getJson());
    }

    private void parseJson(String content) {
        LOGUtil.e("TAG","parseJson");
        try {
            tagBeanList = jsonToList(GameTagBean.class, content, "data");
            LOGUtil.e("TAG","tagBeanList Size "+tagBeanList.size());
            adapter = new TagClassfyAdapter(getActivity(), tagBeanList);
            mTagGrid.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GameTagBean bean = tagBeanList.get(position);
        Intent intent  = new Intent(getActivity(),GameListActivity.class);
        intent.putExtra(GameListActivity.TYPE,CategroryListApi.TAG);
        intent.putExtra(GameListActivity.ID,bean.getId());
        intent.putExtra(GameListActivity.NAME,bean.getName());
        startActivity(intent);
    }
}
