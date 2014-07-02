package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.adapter.MessageAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.GetMessageListApi;
import com.youle.gamebox.ui.bean.MessageBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-6-26.
 */
public class MessageListFragment extends NextPageFragment {
    private GetMessageListApi mGetMessageListApi ;
    private MessageAdapter mAdapter ;
    private List<MessageBean> messageBeanList ;
    private int type ;
    public static final int GIFT = 2 ;
    public static final int COMUNITY= 3 ;
    public static final int PRIVATE_MESSAGE = 4 ;
    public static final int SYSTEM = 5 ;
    public static final int MESSAGE_RECOMMENT = 7 ;
    public MessageListFragment(int type) {
        this.type = type;
    }

    @Override
    public AbstractApi getApi() {
        return mGetMessageListApi;
    }

    @Override
    public YouleBaseAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public List pasreJson(String jsonStr) throws JSONException {
        return jsonToList(MessageBean.class,jsonStr,"data");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(messageBeanList==null){
            loadData();
        }
    }

    private void loadData(){
        mGetMessageListApi = new GetMessageListApi();
        mGetMessageListApi.setSid(new UserInfoCache().getSid());
        mGetMessageListApi.setType(type);
        ZhidianHttpClient.request(mGetMessageListApi,new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    messageBeanList = pasreJson(jsonString);
                    mAdapter = new MessageAdapter(getActivity(),messageBeanList);
                    getListView().setAdapter(mAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
