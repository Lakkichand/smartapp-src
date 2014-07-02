package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import butterknife.InjectView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.ta.mvc.common.TAResponse;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.adapter.HomeGameAdapter;
import com.youle.gamebox.ui.api.game.HomeGameApi;
import com.youle.gamebox.ui.api.game.IndexOrdinaryApi;
import com.youle.gamebox.ui.bean.IndexHeadBean;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.greendao.JsonEntry;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.ModelConst;
import com.youle.gamebox.ui.view.IndexHomeHeadView;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-5-30.
 */
public class IndexHomeFragment extends BaseFragment {

    IndexHomeHeadView headView;
    @InjectView(R.id.hotGame)
    PullToRefreshListView mHotGame;
    private  IndexOrdinaryApi api = null ;
    private HomeGameApi gameApi = null ;
    HomeGameAdapter adapter ;
    private List<IndexHeadBean> indexHeadBeans ;
    private List<GameBean> recomendGameList ;
    @Override
    protected int getViewId() {
        return R.layout.fragment_index;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHotGame.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        PauseOnScrollListener scrollListener = new PauseOnScrollListener(ImageLoader.getInstance(),true,true) ;
        mHotGame.setOnScrollListener(scrollListener);
        if(headView==null) {
            loadData();
        }
        if(indexHeadBeans==null){
            loadCach(ModelConst.HOME_HEAD);
            requestHeadDate();
        }
        if(recomendGameList==null){
            loadCach(ModelConst.HOME_GAME);
            requestGame();
        }else {
            mHotGame.getRefreshableView().setAdapter(adapter);
        }
    }

    protected void loadData() {
        headView = new IndexHomeHeadView(getActivity());
        mHotGame.getRefreshableView().addHeaderView(headView);
;
    }

    private void requestHeadDate(){
        api = new IndexOrdinaryApi() ;
        ZhidianHttpClient.request(api,new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                cacheJson(ModelConst.HOME_HEAD,jsonString,api);
            }
        });
    }

    private void requestGame(){
       gameApi = new HomeGameApi() ;
        ZhidianHttpClient.request(gameApi,new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                cacheJson(ModelConst.HOME_GAME,jsonString,gameApi);
            }
        });
    }

    @Override
    public void onSuccess(TAResponse response) {
        JsonEntry entry = (JsonEntry) response.getData();
        if (entry==null) return;
        if(entry.getResouce().equals(ModelConst.HOME_HEAD)){
            parseHeadJson(entry.getJson());
        }else{
            parseHomeGame(entry.getJson()) ;
        }
    }

    private void parseHomeGame(String s) {
        try {
            recomendGameList = jsonToList(GameBean.class,s,"data");
            adapter = new HomeGameAdapter(getActivity(),recomendGameList) ;
            adapter.setNumColumns(3);
            mHotGame.getRefreshableView().setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseHeadJson(String json){
        try {
            indexHeadBeans = jsonToList(IndexHeadBean.class,json,"data") ;
            headView.initHead(indexHeadBeans);
            recomendGameList = jsonToList(GameBean.class,json,"choice") ;
            headView.initRecomendGame(recomendGameList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
