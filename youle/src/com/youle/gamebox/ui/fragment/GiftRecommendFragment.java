package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.api.GetRecommentGiftApi;
import com.youle.gamebox.ui.bean.HotGame;
import com.youle.gamebox.ui.bean.HotGiftBean;
import com.youle.gamebox.ui.bean.RecomentGiftBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.SoftkeyboardUtil;
import com.youle.gamebox.ui.util.UIUtil;
import com.youle.gamebox.ui.view.GiftHotGameView;
import com.youle.gamebox.ui.view.GiftLikeView;
import com.youle.gamebox.ui.view.HotGitfView;
import org.json.JSONException;

/**
 * Created by Administrator on 14-6-24.
 */
public class GiftRecommendFragment extends BaseFragment {
    @InjectView(R.id.likeLayout)
    LinearLayout mLikeLayout;
    @InjectView(R.id.hotGiftLayout)
    LinearLayout mHotGiftLayout;
    @InjectView(R.id.hotGameLayout)
    LinearLayout mHotGameLayout;
    @InjectView(R.id.serch)
    LinearLayout mSerch;
    @InjectView(R.id.keyEdit)
    EditText mKeyEdit;
    private RecomentGiftBean mRecomentGiftBean;

    @Override
    protected int getViewId() {
        return R.layout.fragment_recommend_gift;
    }

    @Override
    protected String getModelName() {
        return "推荐礼包";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mRecomentGiftBean == null) {
            loadData();
        }
        mSerch.setOnClickListener(serchOnclickListener);
    }

    private void loadData() {
        GetRecommentGiftApi getRecommentGiftApi = new GetRecommentGiftApi();
        getRecommentGiftApi.setSid(new UserInfoCache().getSid());
        getRecommentGiftApi.setPackages(AppInfoUtils.getInstalledPackage(getActivity()));
        ZhidianHttpClient.request(getRecommentGiftApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    mRecomentGiftBean = jsonToBean(RecomentGiftBean.class, jsonString);
                    if (mRecomentGiftBean != null) {
                        initLikeUI();
                        initHotGiftUI();
                        initHotGameUI();
                    }
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
    private void initLikeUI() {
        mLikeLayout.removeAllViews();
        if (mRecomentGiftBean.getLikes() != null) {
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            p.weight = 1;
            LinearLayout linearLayout = null;
            for (int i = 0; i < mRecomentGiftBean.getLikes().size(); i++) {
                if (i % 2 == 0) {
                    linearLayout = new LinearLayout(getActivity());
                    linearLayout.setWeightSum(2.0f);
                    mLikeLayout.addView(linearLayout);
                }
                GiftLikeView child = new GiftLikeView(getActivity(), mRecomentGiftBean.getLikes().get(i));
                if(i==1||i==2){
                    child.setBackgroundColor(getResources().getColor(R.color.serch_bg));
                }
                String html = getString(R.string.gift_number_format1,mRecomentGiftBean.getLikes().get(i).getAmount());
                child.setText(Html.fromHtml(html));
                linearLayout.addView(child, p);
            }
            if (mRecomentGiftBean.getLikes().size() == 1 || mRecomentGiftBean.getLikes().size() == 3) {//
                TextView view = new TextView(getActivity());
                linearLayout.addView(view, p);
            }
        }
    }

    private void initHotGiftUI() {
        mHotGiftLayout.removeAllViews();
        if (mRecomentGiftBean.getHotSprees() != null) {
            for (int i = 0; i < mRecomentGiftBean.getHotSprees().size(); i++) {
                HotGiftBean hotGiftBean = mRecomentGiftBean.getHotSprees().get(i);
                HotGitfView hotGitfView = new HotGitfView(getActivity(), hotGiftBean);
                mHotGiftLayout.addView(hotGitfView);
                if(i==mRecomentGiftBean.getHotSprees().size()-1){
                   hotGitfView.isLast();
                }
            }
        }
    }

    private void search(String keyword) {
        SoftkeyboardUtil.hideSoftKeyBoard(getActivity(),mKeyEdit);
        if(keyword.length()>0){
                AllGiftFragment all = new AllGiftFragment(keyword);
                ((BaseActivity)getActivity()).addFragment(all,true);
        }else {
            UIUtil.toast(getActivity(), R.string.input_not_nul);
        }
    }
    private void initHotGameUI() {
        mHotGameLayout.removeAllViews();
        if (mRecomentGiftBean.getHotGames() != null) {
            for (HotGame hotGame : mRecomentGiftBean.getHotGames()) {
                mHotGameLayout.addView(new GiftHotGameView(getActivity(), hotGame));
            }
        }
    }
}
