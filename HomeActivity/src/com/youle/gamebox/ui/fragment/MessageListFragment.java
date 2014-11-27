package com.youle.gamebox.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.YouleAplication;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.adapter.MessageAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.GetMessageListApi;
import com.youle.gamebox.ui.bean.MessageBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.listener.NewMsgCallBackListener;

import org.json.JSONException;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 14-6-26.
 */
@SuppressLint("ValidFragment")
public class MessageListFragment extends NextPageFragment implements MessageAdapter.IMessageRead,AdapterView.OnItemClickListener{
    private GetMessageListApi mGetMessageListApi ;
    private MessageAdapter mAdapter ;
    private List<MessageBean> messageBeanList ;
    private int type ;
    public static final int GIFT = 2 ;// 礼包
    public static final int COMUNITY= 3 ;// 论坛
    public static final int PRIVATE_MESSAGE = 4 ; // 互动私信
    public static final int SYSTEM = 5 ;//系统
    public static final int MESSAGE_RECOMMENT = 7 ;///活动推荐
    
    private Map<String, Integer> map;
    private NewMsgCallBackListener backListener;
    
    public MessageListFragment(int type) {
        this.type = type;
    }
    
    public MessageListFragment(int type, NewMsgCallBackListener listener) {
        this.type = type;
        backListener = listener;
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
        if(messageBeanList==null&&type==SYSTEM){
            loadData();
        }
        getListView().setOnItemClickListener(this);
    }

    @Override
    protected void onRefreshRequestSuccess(String json) {
        onNextPageRequestSuccess(json);
    }

    @Override
    protected void onNextPageRequestSuccess(String json) {
        try {
            map = jsonToMap(json,"accounts");
            if(backListener!=null){
                backListener.callBack(map);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void loadData(){
        mGetMessageListApi = new GetMessageListApi();
        mGetMessageListApi.setSid(new UserInfoCache().getSid());
        mGetMessageListApi.setType(type);
        ZhidianHttpClient.request(mGetMessageListApi,new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                	map = jsonToMap(jsonString, "accounts");
                    messageBeanList = pasreJson(jsonString);
                    if(YouleAplication.messageNumberBean!=null){
                        YouleAplication.messageNumberBean.setMsgCount(0);
                    }
                    for (String key:map.keySet()){
                            if(YouleAplication.messageNumberBean!=null){
                                int count= YouleAplication.messageNumberBean.getMsgCount() ;
                                YouleAplication.messageNumberBean.setMsgCount(count+map.get(key));
                            }
                    }
                    if (backListener != null) {
                    	backListener.callBack(map);
					}
                    mAdapter = new MessageAdapter(getActivity(),messageBeanList,MessageListFragment.this);
                    getListView().setAdapter(mAdapter);
                    if(messageBeanList.size()>0){
                        showNoContentLayout(false);
                    }else {
                        showNoContentLayout(true);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }         
        });
    }

	@Override
	public void onLoadStart() {
	}
    
//	@Override
//	public void onSuccess(String content) {
//		// TODO Auto-generated method stub
//		super.onSuccess(content);
//	}
	
	@Override
	public void onFailure(Throwable error) {
		// TODO Auto-generated method stub
		super.onFailure(error);
	}

    @Override
    protected String getModelName() {
        if(type == SYSTEM) {
            return "系统消息";
        }else if (type ==MESSAGE_RECOMMENT){
            return "活动消息";
        }else if(type == GIFT){
            return  "礼包消息";
        }else if(type == COMUNITY){
            return  "社区消息";
        }else {
            return "互动消息";
        }
    }

    @Override
    public void readOneMessage() {
        int value = map.get(type+"");
        if(value>0){
            value = value -1 ;
            map.put(type+"",value);
            if(backListener!=null){
                backListener.callBack(map);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       MessageBean bean = messageBeanList.get(position-2);
        if(bean.getMsgType()==43||bean.getMsgType()==46||bean.getMsgType()==46||bean.getMsgType()==42){
            HomepageFragment homepageFragment = new HomepageFragment(bean.getUid(),null);
            homepageFragment.setLinkId(bean.getLinkId()+"");
            ((BaseActivity)getActivity()).addFragment(homepageFragment,true);
            homepageFragment.setCurrentTab(1);
        }else if(bean.getMsgType()==44||bean.getMsgType()==45){
            HomepageFragment homepageFragment = new HomepageFragment(bean.getUid(),null);
            homepageFragment.setLinkId(bean.getLinkId()+"");
            ((BaseActivity)getActivity()).addFragment(homepageFragment,true);
            homepageFragment.setCurrentTab(0);
        }
    }
}
