package com.youle.gamebox.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.ta.TAApplication;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.pcenter.DynamicCommentGameApi;
import com.youle.gamebox.ui.bean.DynamicSelectGameBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import org.json.JSONException;

import java.util.List;

/**
 * Created by Administrator on 14-7-23.
 */
public class SelectGameFragment extends NextPageFragment {
    public  interface  OnGameSelect{
        public void onGameSelect(DynamicSelectGameBean bean);
    }
    private  OnGameSelect mSelect ;
    public SelectGameFragment(OnGameSelect onGameSelect) {
        this.mSelect = onGameSelect ;
    }

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
    protected void loadData() {
        DynamicCommentGameApi api = new DynamicCommentGameApi();
        api.setSid(new UserInfoCache().getSid());
        api.setPackages(AppInfoUtils.getInstalledPackage(getActivity()));
        ZhidianHttpClient.request(api, new JsonHttpListener(
                this) {

            @Override
            public void onRequestSuccess(String jsonString) {
                super.onRequestSuccess(jsonString);
                try {
                    List<DynamicSelectGameBean> list = jsonToList(
                            DynamicSelectGameBean.class, jsonString, "data");
                    if (list.size() > 0) {
                        GameAdapter adapter = new GameAdapter(getActivity(), list);
                        getListView().setAdapter(adapter);
                        return;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDefaultTitle("选择游戏");
        listView.setMode(PullToRefreshBase.Mode.DISABLED);
        loadData();
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                DynamicSelectGameBean bean = (DynamicSelectGameBean) view
                        .getTag();
                if(mSelect!=null){
                    mSelect.onGameSelect(bean);
                }
            }
        });
    }

    @Override
    protected String getModelName() {
        return "选择游戏";
    }

    private class GameAdapter extends YouleBaseAdapter<DynamicSelectGameBean> {


        private LayoutInflater mInflater = LayoutInflater.from(TAApplication
                .getApplication());

        public GameAdapter(Context mContext, List<DynamicSelectGameBean> mList) {
            super(mContext, mList);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(
                        R.layout.dynamic_select_game_item, null);
            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            TextView name = (TextView) convertView.findViewById(R.id.name);
            DynamicSelectGameBean bean = mList.get(position);
            name.setText(bean.name);
            ImageLoadUtil.displayImage(bean.iconUrl, icon);
            convertView.setTag(bean);
            return convertView;
        }

        /**
         * 更新数据
         */
        public void update(List<DynamicSelectGameBean> list) {
            mList.clear();
            if (list == null || list.size() <= 0) {
                notifyDataSetChanged();
                return;
            }
            mList.addAll(list);
            notifyDataSetChanged();
        }
    }
}
