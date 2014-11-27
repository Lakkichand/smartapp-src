package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.CommentMsgBoardActivity;
import com.youle.gamebox.ui.activity.CommonActivity;
import com.youle.gamebox.ui.adapter.MymsgboardAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.pcenter.MymsgboardApi;
import com.youle.gamebox.ui.api.person.MsgboardPublishApi;
import com.youle.gamebox.ui.bean.pcenter.MymsgboardBean;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.UIUtil;
import com.youle.gamebox.ui.view.AddBoradBottomView;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2014/6/18.
 */
public class MyMsgboardFragment extends NextPageFragment implements AddBoradBottomView.ISendListener {
    MymsgboardApi mymsgboardApi;
    MymsgboardAdapter mymsgboardAdapter;
    private Long uid = null;
    private int type;
    public static final int MY = 1;
    public static final int OTHER = 2;

    public MyMsgboardFragment(Long uid, int type) {
        this.uid = uid;
        this.type = type;
    }

    @Override
    public AbstractApi getApi() {
        if (mymsgboardApi == null) {
            mymsgboardApi = new MymsgboardApi();
            mymsgboardApi.setUid(Long.valueOf(1));
        }
        return mymsgboardApi;
    }

    public MyMsgboardFragment() {

    }

    public MyMsgboardFragment(long uid) {
        this.uid = uid;
    }

    private AddBoradBottomView addBoradBottomView ;

    @Override
    public YouleBaseAdapter getAdapter() {
        return mymsgboardAdapter;
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(MymsgboardBean.class, jsonStr, "data");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (type == MY && new UserInfoCache().getUserInfo() == null) {
            showNoContentLayout(true);
        } else {
            loadData();
        }
        if(type ==OTHER&&addBoradBottomView==null){
            addBoradBottomView = new AddBoradBottomView(getActivity(),this);
            setBottomView(addBoradBottomView);
        }
    }

    @Override
    protected String getModelName() {
        return "留言板";
    }

    public void loadData() {
        if (mymsgboardApi == null) {
            mymsgboardApi = new MymsgboardApi();
        }


        if (type == MY) {
            UserInfo userInfo = new UserInfoCache().getUserInfo();
            if (userInfo != null) {
                mymsgboardApi.setSid(userInfo.getSid());
                mymsgboardApi.setUid(userInfo.getUid());
            }
        } else {
            mymsgboardApi.setUid(this.uid);
        }
        ZhidianHttpClient.request(mymsgboardApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    List<MymsgboardBean> mymsgboardBeans = jsonToList(MymsgboardBean.class, jsonString, "data");
                    if (mymsgboardBeans.size() == 0) {
                        showNoContentLayout(true);
                    } else {
                        showNoContentLayout(false);
                        mymsgboardAdapter = new MymsgboardAdapter(getActivity(), mymsgboardBeans);
                        getListView().setAdapter(mymsgboardAdapter);
                        if (type == MY) {
                            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    MymsgboardBean mymsgboardBean = (MymsgboardBean) getAdapter().getItem(position - 1);
                                    CommentMsgBoardActivity.startMsgComment(getActivity(), mymsgboardBean.getNickName(), mymsgboardBean.getId());
                                }
                            });
                        }
                        showNoContentLayout(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                super.onRequestSuccess(jsonString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable error) {
                super.onFailure(statusCode, headers, responseString, error);
                showNoContentLayout(true);
            }

            @Override
            public void onResultFail(String jsonString) {
                super.onResultFail(jsonString);
            }
        });
    }


    private void setData(String str) {
        if (str == null) return;
        try {
            JSONObject jsonObject = new JSONObject(str);
            setTotalpage(jsonObject.getInt("totalPages"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setOnItemClickeInterface(OnItemClickeInterface onItemClickeInterface) {
        this.onItemClickeInterface = onItemClickeInterface;
    }

    OnItemClickeInterface onItemClickeInterface;

    @Override
    public void send(String content) {
        MsgboardPublishApi  msgboardPublishApi = new MsgboardPublishApi() ;
        UserInfo userInfo = new UserInfoCache().getUserInfo();
        if(userInfo==null){
            CommonActivity.startCommonA(getActivity(),CommonActivity.FRAGMENT_LOGIN,-1);
        }else {
            msgboardPublishApi.setSid(userInfo.getSid());
            msgboardPublishApi.setUid(uid+"");
            msgboardPublishApi.setContent(content);
            ZhidianHttpClient.request(msgboardPublishApi,new JsonHttpListener(getActivity(),"正在留言"){
                @Override
                public void onRequestSuccess(String jsonString) {
                    UIUtil.toast(getActivity(), R.string.board_success);
                    addBoradBottomView.clean();
                    loadData();
                }
            });
        }
    }

    public interface OnItemClickeInterface {
        public void clickeMessageBoard(MymsgboardBean mymsgboardBean);
    }


}
