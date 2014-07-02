package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.adapter.ThemDetailAdapter;
import com.youle.gamebox.ui.api.special.SpecialAdetailApi;
import com.youle.gamebox.ui.bean.special.OtherSpecial;
import com.youle.gamebox.ui.bean.special.SpecialAdetailBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.view.SpecialDetailHeadView;
import org.json.JSONException;

/**
 * Created by Administrator on 14-6-23.
 */
public class SpecilDetailFragment extends BaseFragment {
    @InjectView(R.id.specialDetailList)
    ListView mSpecialDetailList;
    SpecialAdetailBean mDetailBean;
    private String id;

    public SpecilDetailFragment(String id) {
        this.id = id;
    }

    @Override
    protected int getViewId() {
        return R.layout.fragment_specail_detail;
    }

    private void initHeadView() {
        SpecialDetailHeadView headView = new SpecialDetailHeadView(getActivity(), mDetailBean);
        mSpecialDetailList.addHeaderView(headView);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mDetailBean == null) {
            loadData();
        }
    }

    private void initFootView() {
        View footView = LayoutInflater.from(getActivity()).inflate(R.layout.special_detail_foot, null);
        LinearLayout linearLayout = (LinearLayout) footView.findViewById(R.id.otherThemeLayout);
        for (OtherSpecial otherSpecial : mDetailBean.getOthers()) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.other_them_item, null);
            TextView textView = (TextView) view.findViewById(R.id.other_them_title);
            textView.setText(otherSpecial.getTitle());
            linearLayout.addView(view);
        }
        mSpecialDetailList.addFooterView(footView);
    }

    private void loadData() {
        SpecialAdetailApi specialAdetailApi = new SpecialAdetailApi();
        specialAdetailApi.setSpecialId(id);
        ZhidianHttpClient.request(specialAdetailApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    mDetailBean = jsonToBean(SpecialAdetailBean.class, jsonString);
                    initHeadView();
                    initFootView();
                    ThemDetailAdapter adapter = new ThemDetailAdapter(getActivity(), mDetailBean.getGames());
                    mSpecialDetailList.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
