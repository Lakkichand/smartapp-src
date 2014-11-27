package com.youle.gamebox.ui.fragment;

import java.util.List;
import org.json.JSONException;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.adapter.GonglueAdapter;
import com.youle.gamebox.ui.adapter.YouleBaseAdapter;
import com.youle.gamebox.ui.api.AbstractApi;
import com.youle.gamebox.ui.api.NewsListApi;
import com.youle.gamebox.ui.bean.GonglueBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;

public class NewsListFragment extends NextPageFragment {

	private NewsListApi newsListApi;// 接口
	private GonglueAdapter mAdapter;
	private List<GonglueBean> glList;

	@Override
	public AbstractApi getApi() {
		return newsListApi;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public YouleBaseAdapter getAdapter() {
		return mAdapter;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List pasreJson(String jsonStr) throws JSONException {
		return jsonToList(GonglueBean.class, jsonStr, "data");
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		loadData();
	}

    @Override
    protected String getModelName() {
        return "新闻列表";
    }

    /**
	 * 加载数据
	 */
	protected void loadData() {
		newsListApi = new NewsListApi();
		ZhidianHttpClient.request(newsListApi, new JsonHttpListener(this) {
			@SuppressWarnings("unchecked")
			@Override
			public void onRequestSuccess(String jsonString) {
				super.onRequestSuccess(jsonString);
				try {
					glList = pasreJson(jsonString);
					mAdapter = new GonglueAdapter(getActivity(), glList);
					getListView().setAdapter(mAdapter);
					gotoGolueDetail();
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		});

	}

	public void gotoGolueDetail() {
		getListView().setOnItemClickListener(
				new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						GonglueBean gonglueBean = mAdapter
								.getItem(position - 1);
						if (gonglueBean != null) {
							StagoryDetailFragment stagoryDetailFragment = new StagoryDetailFragment(
									gonglueBean.getId() + "");
							((BaseActivity) getActivity()).addFragment(
									stagoryDetailFragment, true);
						}
					}
				});
	}

}
