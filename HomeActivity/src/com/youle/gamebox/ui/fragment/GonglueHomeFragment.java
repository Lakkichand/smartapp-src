package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.api.GonglueApi;
import com.youle.gamebox.ui.bean.HomeGonglueBean;
import com.youle.gamebox.ui.bean.HotGame;
import com.youle.gamebox.ui.bean.LimitBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.SoftkeyboardUtil;
import com.youle.gamebox.ui.util.UIUtil;
import com.youle.gamebox.ui.view.GiftHotGameView;
import com.youle.gamebox.ui.view.GiftLikeView;
import org.json.JSONException;

/**
 * Created by Administrator on 14-6-25.
 */
public class GonglueHomeFragment extends BaseFragment {
    @InjectView(R.id.likeLayout)
    LinearLayout mLikeLayout;
    @InjectView(R.id.hotGonglueLayout)
    LinearLayout mHotGonglueLayout;
    @InjectView(R.id.hotGameLayout)
    LinearLayout mHotGameLayout;
    @InjectView(R.id.keyEdit)
    EditText mKeyEdit;
    @InjectView(R.id.serch)
    LinearLayout mSerch;
    private HomeGonglueBean mBean;

    @Override
    protected int getViewId() {
        return R.layout.fragment_recommend_categrory;
    }

    @Override
    protected String getModelName() {
        return "攻略首页";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mBean==null){
            loadData();
        }
        mSerch.setOnClickListener(serchOnclickListener);
    }

    private void loadData() {
        GonglueApi gonglueApi = new GonglueApi();
        gonglueApi.setPackages(AppInfoUtils.getInstalledPackage(getActivity()));
        gonglueApi.setSid(new UserInfoCache().getSid());
        ZhidianHttpClient.request(gonglueApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    mBean = jsonToBean(HomeGonglueBean.class, jsonString);
                    initLikeUI();
                    initHotGameUI();
                    initHotGonglue();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private View.OnClickListener serchOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            search(mKeyEdit.getText().toString().trim());
        }
    };
    private void search(String keyword) {
        SoftkeyboardUtil.hideSoftKeyBoard(getActivity(), mKeyEdit);
        if(keyword.length()>0){
                GonglueListFragment g = new GonglueListFragment();
                g.setKeyWorld(keyword);
                ((BaseActivity)getActivity()).addFragment(g,true);
        }else {
            UIUtil.toast(getActivity(), R.string.input_not_nul);
        }
    }
    private void initLikeUI() {
        mLikeLayout.removeAllViews();
        if (mBean.getLikes() != null) {
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            p.weight = 1;
            LinearLayout linearLayout = null;
            for (int i = 0; i < mBean.getLikes().size(); i++) {
                if (i % 2 == 0) {
                    linearLayout = new LinearLayout(getActivity());
                    linearLayout.setWeightSum(2.0f);
                    mLikeLayout.addView(linearLayout);
                }
                GiftLikeView child = new GiftLikeView(getActivity(), mBean.getLikes().get(i));
                if(i==1||i==2){
                    child.setBackgroundColor(getResources().getColor(R.color.serch_bg));
                }
                String html = getString(R.string.gonglue_number_format1,mBean.getLikes().get(i).getAmount());
                child.setText(Html.fromHtml(html));
                linearLayout.addView(child, p);
            }
            if (mBean.getLikes().size() == 1 || mBean.getLikes().size() == 3) {//
                TextView view = new TextView(getActivity());
                linearLayout.addView(view, p);
            }
        }
    }

    private void initHotGonglue() {
        mHotGonglueLayout.removeAllViews();
        if(mBean.getHots()==null) return;
        for (LimitBean bean : mBean.getHots()) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.hot_gonglue_item, null);
            TextView gonglueTitle = (TextView) view.findViewById(R.id.gonglue_title);
            gonglueTitle.setText(bean.getTitle());
            view.setTag(bean);
            mHotGonglueLayout.addView(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   LimitBean b = (LimitBean) v.getTag();
                    StagoryDetailFragment stagoryDetailFragment = new StagoryDetailFragment(b.getId()+"");
                    ((BaseActivity)getActivity()).addFragment(stagoryDetailFragment,true);
                }
            });
        }
    }

    private void initHotGameUI() {
        mHotGameLayout.removeAllViews();
        if (mBean.getHotGames() != null) {
            for (HotGame hotGame : mBean.getHotGames()) {
                GiftHotGameView giftHotGameView = new GiftHotGameView(getActivity(),hotGame,GiftHotGameView.ViewType.GONGLUE);
                mHotGameLayout.addView(giftHotGameView);
            }
        }
    }
}
