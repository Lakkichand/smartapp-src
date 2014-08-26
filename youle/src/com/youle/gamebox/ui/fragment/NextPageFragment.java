package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.view.GCommentSoftKeyLayout;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 14-5-8.
 */
public abstract class NextPageFragment extends BaseFragment implements PullToRefreshBase.OnRefreshListener2<ListView>,GCommentSoftKeyLayout.ISoftKeybordListener {
    protected PullToRefreshListView listView;

    public abstract AbstractApi getApi();

    public abstract YouleBaseAdapter getAdapter();

    public abstract List pasreJson(String jsonStr) throws JSONException;

    protected abstract void loadData();
    protected void onRefreshRequestSuccess(String json){}
    protected void onNextPageRequestSuccess(String json){}
    private int totalpage = 2;

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
    private LinearLayout mHeadLayout;
    private View mStartLoad;
    private View mContent;
    private View mNoContentPlacer;
    private ViewGroup mMessageInputView;
    private View mNoNetView;
    private TextView mNoDataTipText;
    private GCommentSoftKeyLayout layout ;
    View recodingView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.youle_list_base_fragment, container, false);
            listView = (PullToRefreshListView) mView.findViewById(R.id.listView);
            mBottomLayou = (LinearLayout) mView.findViewById(R.id.bottomView);
            mHeadLayout = (LinearLayout) mView.findViewById(R.id.headLayout);
            mStartLoad = mView.findViewById(R.id.loading);
            mContent = mView.findViewById(R.id.content);
            mNoNetView = mView.findViewById(R.id.noNet);
            mNoContentPlacer = mView.findViewById(R.id.no_message_tips_placer);
            mMessageInputView = (ViewGroup) mView.findViewById(R.id.messageborad_layout);
            recodingView = mView.findViewById(R.id.recoding);
            mNoDataTipText = (TextView) mView.findViewById(R.id.no_data_tip);
            mNoNetView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadData();
                }
            });
        } else if (mView.getParent() != null) {
            ((ViewGroup) mView.getParent()).removeView(mView);
        }
        return mView;
    }

    @Override
    public void showScro(boolean show) {

    }

    public void setOnScrollListener(AbsListView.OnScrollListener listener) {
        listView.setOnScrollListener(listener);
    }

    @Override
    public void onLoadStart() {
        mStartLoad.setVisibility(View.VISIBLE);
        mContent.setVisibility(View.GONE);
        mNoNetView.setVisibility(View.GONE);
    }

    @Override
    public void onSuccess(String content) {
        mStartLoad.setVisibility(View.GONE);
        mContent.setVisibility(View.VISIBLE);
        mNoNetView.setVisibility(View.GONE);
    }


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
        if (api != null) {
            api.setPageNo(1);
            requestData(api);
        }
    }

    private void loadNextPage() {
        AbstractApi api = getApi();
        if (api!=null&&api.getPageNo() < getTotalpage()) {
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

    @Override
    public void onFailure(Throwable error) {
        mNoNetView.setVisibility(View.VISIBLE);
        mContent.setVisibility(View.GONE);
        mStartLoad.setVisibility(View.GONE);
    }

    private void requestData(final AbstractApi abstractApi) {
        ZhidianHttpClient.request(abstractApi, new JsonHttpListener(false) {
            @Override
            public void onRequestSuccess(String content) {
                if (getAdapter() == null) return;
                listView.onRefreshComplete();
                try {
                    List list = pasreJson(content);
                    JSONObject jsonObject = new JSONObject(content);
                    setTotalpage(jsonObject.getInt("totalPages"));
                    if (list != null) {
                        if (abstractApi.getPageNo() > 1) {
                            if (getAdapter() != null) {
                                getAdapter().addDate(list);
                                onNextPageRequestSuccess(content);
                            }
                        } else {
                            if (getAdapter() != null) {
                                getAdapter().resetDate(list);
                                onRefreshRequestSuccess(content);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
                listView.onRefreshComplete();
                AbstractApi api = getApi();
                api.setPageNo(api.getPageNo() - 1);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable error) {
                super.onFailure(statusCode, headers, responseString, error);
                listView.onRefreshComplete();
                Toast.makeText(getActivity(), "刷新失败", Toast.LENGTH_SHORT).show();
            }

        });
    }

    protected void setBottomView(View view) {
        mBottomLayou.removeAllViews();
        mBottomLayou.addView(view);
    }

    protected void setHeadView(View view) {
        mHeadLayout.removeAllViews();
        mBottomLayou.addView(view);
    }

    public void showNoContentLayout(boolean isShow) {
        if (isShow) {
            mNoContentPlacer.setVisibility(View.VISIBLE);
        } else {
            mNoContentPlacer.setVisibility(View.GONE);
        }
    }

    public void showNoContentLayout(boolean isShow, String tip) {
        if (isShow) {
            mNoContentPlacer.setVisibility(View.VISIBLE);
            mNoDataTipText.setText(tip);
        } else {
            mNoContentPlacer.setVisibility(View.GONE);
        }
    }

    public View addBottomView(View bottomView) {
        if(bottomView!=null) {
            mMessageInputView.addView(bottomView);
            mMessageInputView.setVisibility(View.VISIBLE);
        }
        return bottomView;
    }
}
