package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import butterknife.InjectView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.ta.mvc.common.TAResponse;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.YouleAplication;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.GameDetailActivity;
import com.youle.gamebox.ui.adapter.GridItemClickListener;
import com.youle.gamebox.ui.adapter.HomeGameAdapter;
import com.youle.gamebox.ui.adapter.ListAsGridBaseAdapter;
import com.youle.gamebox.ui.api.game.HomeGameApi;
import com.youle.gamebox.ui.api.game.IndexOrdinaryApi;
import com.youle.gamebox.ui.bean.IndexHeadBean;
import com.youle.gamebox.ui.bean.MessageNumberBean;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.greendao.JsonEntry;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.util.ModelConst;
import com.youle.gamebox.ui.util.UIUtil;
import com.youle.gamebox.ui.view.HomeTitleView;
import com.youle.gamebox.ui.view.IndexHomeHeadView;
import org.apache.commons.logging.Log;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 14-5-30.
 */
public class IndexHomeFragment extends BaseFragment {

    IndexHomeHeadView headView;
    @InjectView(R.id.hotGame)
    PullToRefreshListView mHotGame;
    private IndexOrdinaryApi api = null;
    private HomeGameApi gameApi = null;
    HomeGameAdapter adapter;
    private List<IndexHeadBean> indexHeadBeans;
    private List<GameBean> recomendGameList;
    private int totalPage = 1;
    InotReadListener listener;

    public void setListener(InotReadListener listener) {
        this.listener = listener;
    }

    public interface InotReadListener {
        public void notifyNotRead();
    }

    @Override
    protected int getViewId() {
        return R.layout.fragment_index;
    }

    @Override
    protected String getModelName() {
        return "首页";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHotGame.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        PauseOnScrollListener scrollListener = new PauseOnScrollListener(ImageLoader.getInstance(), true, true);
        mHotGame.setOnScrollListener(scrollListener);
        mHotGame.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {

            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (totalPage > gameApi.getPageNo()) {
                    refreshView.post(new Runnable() {
                        @Override
                        public void run() {
                            loadNexPage();
                        }
                    });
                } else {
                    refreshView.post(new Runnable() {
                        @Override
                        public void run() {
                            mHotGame.onRefreshComplete();
                        }
                    });
                }
            }
        });
        if (headView == null) {
            loadData();
        }
        if (indexHeadBeans == null) {
            loadCach(ModelConst.HOME_HEAD);
            requestHeadDate();
        }
        if (recomendGameList == null) {
            loadCach(ModelConst.HOME_GAME);
            requestGame();
        } else {
            mHotGame.getRefreshableView().setAdapter(adapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (indexHeadBeans != null && headView != null) {
//            initHeadView();
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void loadNexPage() {
        gameApi.setPageNo(gameApi.getPageNo() + 1);
        ZhidianHttpClient.request(gameApi, new JsonHttpListener(false) {
            @Override
            public void onRequestSuccess(String jsonString) {
                mHotGame.onRefreshComplete();
                parseHomeGame(jsonString,true);
            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
                gameApi.setPageNo(api.getPageNo() - 1);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable error) {
                super.onFailure(statusCode, headers, responseString, error);
                gameApi.setPageNo(api.getPageNo() - 1);
            }

        });
    }

    protected void loadData() {
        headView = new IndexHomeHeadView(getActivity());
        mHotGame.getRefreshableView().addHeaderView(headView);
        ;
    }

    private void requestHeadDate() {
        api = new IndexOrdinaryApi();
        api.setPackageVersions(AppInfoUtils.getPkgAndVersion(getActivity()));
        api.sid = new UserInfoCache().getSid();
        ZhidianHttpClient.request(api, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    MessageNumberBean bean = jsonToBean(MessageNumberBean.class, jsonString, "leftprompt");
                    YouleAplication.messageNumberBean = bean;
                    if (listener != null) {
                        listener.notifyNotRead();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cacheJson(ModelConst.HOME_HEAD, jsonString, api);
            }
        });
    }

    private void requestGame() {
        gameApi = new HomeGameApi();
        ZhidianHttpClient.request(gameApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                cacheJson(ModelConst.HOME_GAME, jsonString, gameApi);
            }
        });
    }

    @Override
    public void onSuccess(TAResponse response) {
        JsonEntry entry = (JsonEntry) response.getData();
        if (entry == null) return;
        if (entry.getResouce().equals(ModelConst.HOME_HEAD)) {
            parseHeadJson(entry.getJson());
        } else {
            parseHomeGame(entry.getJson(),false);
        }
    }

    private void parseHomeGame(String s,boolean isNextPage) {
        try {
                JSONObject js = new JSONObject(s);
                totalPage = js.optInt("totalPages");
                if (!isNextPage) {
                    recomendGameList = jsonToList(GameBean.class, s, "data");
                    adapter = new HomeGameAdapter(getActivity(), recomendGameList);
                    adapter.setOnGridClickListener(gridItemClickListener);
                    adapter.setNumColumns(3);
                    mHotGame.getRefreshableView().setAdapter(adapter);
                } else {
                    List<GameBean> nextPageList = jsonToList(GameBean.class, s, "data");
                    recomendGameList.addAll(nextPageList);
                    adapter.notifyDataSetChanged();
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseHeadJson(String json) {
        try {
            indexHeadBeans = jsonToList(IndexHeadBean.class, json, "data");
            recomendGameList = jsonToList(GameBean.class, json, "choice");
            initHeadView();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initHeadView() {
        headView.initRecomendGame(recomendGameList);
        headView.initHead(indexHeadBeans);
    }

    private GridItemClickListener gridItemClickListener = new GridItemClickListener() {
        @Override
        public void onGridItemClicked(View v, int position, long itemId) {
            GameBean b = adapter.getItem(position);
            GameDetailActivity.startGameDetailActivity(getActivity(), b.getId(), b.getName(), b.getSource());
        }
    };
}
