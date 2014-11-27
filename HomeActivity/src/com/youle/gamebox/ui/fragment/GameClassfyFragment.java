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
import com.youle.gamebox.ui.adapter.GameCatoryAdpater;
import com.youle.gamebox.ui.api.game.CategroryListApi;
import com.youle.gamebox.ui.api.game.GameCategroryApi;
import com.youle.gamebox.ui.bean.GameCategoryBean;
import com.youle.gamebox.ui.greendao.JsonEntry;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.ModelConst;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-5-30.
 */
public class GameClassfyFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    @InjectView(R.id.classfyGrid)
    GridView mClassfyGrid;
    List<GameCategoryBean> list =null ;
    GameCatoryAdpater adpater=null ;
    @Override
    protected int getViewId() {
        return R.layout.fragment_game_classfy;
    }

    @Override
    protected String getModelName() {
        return "游戏分类";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mClassfyGrid.setOnItemClickListener(this);
        if(adpater==null){
          loadCach(ModelConst.GAME_CATORY);
            loadRemoteData();
        }else {
            mClassfyGrid.setAdapter(adpater);
        }
    }

    protected void loadRemoteData() {
        final GameCategroryApi api = new GameCategroryApi();
        ZhidianHttpClient.request(api,new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                cacheJson(ModelConst.GAME_CATORY,jsonString,api);
            }
        });
    }

    @Override
    public void onSuccess(TAResponse response) {
        JsonEntry jsonEntry = (JsonEntry) response.getData();
        if(jsonEntry==null) return;
        try {
            list = jsonToList(GameCategoryBean.class,jsonEntry.getJson(),"data");
            adpater = new GameCatoryAdpater(getActivity(),list);
            mClassfyGrid.setAdapter(adpater);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GameCategoryBean bean = list.get(position);
        Intent intent  = new Intent(getActivity(),GameListActivity.class);
        intent.putExtra(GameListActivity.TYPE,CategroryListApi.GAME);
        intent.putExtra(GameListActivity.ID,bean.getId());
        intent.putExtra(GameListActivity.NAME,bean.getName());
        startActivity(intent);
    }
}
