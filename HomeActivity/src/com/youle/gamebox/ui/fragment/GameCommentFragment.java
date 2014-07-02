package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import com.youle.gamebox.ui.adapter.GameCommentAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.game.GameCommentApi;
import com.youle.gamebox.ui.bean.GameComentBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-6-23.
 */
public class GameCommentFragment extends NextPageFragment {
    private GameCommentApi mGameCommentApi;
    private GameCommentAdapter mAdapter;
    private List<GameComentBean> mGameComentBeanList;
    private long id ;

    public GameCommentFragment(long id) {
        this.id = id;
    }

    @Override
    public AbstractApi getApi() {
        return mGameCommentApi;
    }

    @Override
    public YouleBaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mGameComentBeanList == null) {
            loadData();
        }
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(GameComentBean.class, jsonStr, "data");
    }

    private void loadData() {
        mGameCommentApi = new GameCommentApi(id+"");
        ZhidianHttpClient.request(mGameCommentApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    mGameComentBeanList = pasreJson(jsonString);
                    mAdapter = new GameCommentAdapter(getActivity(), mGameComentBeanList);
                    getListView().setAdapter(mAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
