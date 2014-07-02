package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.*;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.api.StagoryDetailApi;
import com.youle.gamebox.ui.bean.StagoryDetailBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by Administrator on 14-6-27.
 */
public class StagoryDetailFragment extends BaseFragment implements View.OnClickListener {
    @InjectView(R.id.gameIcon)
    ImageView mGameIcon;
    @InjectView(R.id.giftIcon)
    ImageView mGiftIcon;
    @InjectView(R.id.gameIconLayout)
    RelativeLayout mGameIconLayout;
    @InjectView(R.id.gameName)
    TextView mGameName;
    @InjectView(R.id.gameType)
    TextView mGameType;
    @InjectView(R.id.score)
    RatingBar mScore;
    @InjectView(R.id.stagoryTitle)
    TextView mStagoryTitle;
    @InjectView(R.id.data)
    TextView mData;
    @InjectView(R.id.content)
    WebView mContent;
    @InjectView(R.id.openLayout)
    LinearLayout mOpenLayout;
    @InjectView(R.id.comunity)
    TextView mComunity;
    TextView tittleTextView ;
    private StagoryDetailApi mStagoryDetailApi;
    private StagoryDetailBean mStagoryDetailBean;
    private String id;

    public StagoryDetailFragment(String id) {
        this.id = id;
    }

    @Override
    protected int getViewId() {
        return R.layout.stagory_detail;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mOpenLayout.setOnClickListener(this);
        initTitle();
        if (mStagoryDetailBean == null) {
            loadData();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.openLayout) {
            AppInfoUtils.startAPP(getActivity(), mStagoryDetailBean.getPackageName());
        } else if (v.getId() == R.id.back) {
            ((BaseActivity) getActivity()).onBackPressed();
        }
    }


    private void loadData() {
        mStagoryDetailApi = new StagoryDetailApi();
        mStagoryDetailApi.setId(id);
        ZhidianHttpClient.request(mStagoryDetailApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    mStagoryDetailBean = jsonToBean(StagoryDetailBean.class, jsonString);
                    if (mStagoryDetailBean != null) {
                        initView();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initView() {
        ImageLoadUtil.displayImage(mStagoryDetailBean.getIconUrl(), mGameIcon);
        mGameName.setText(mStagoryDetailBean.getGameName());
        mGameType.setText("版本:" + mStagoryDetailBean.getVersion() + "|" + mStagoryDetailBean.getSize());
        mScore.setRating(mStagoryDetailBean.getScore() / 2.0f);
        mContent.loadData(mStagoryDetailBean.getContent().trim(), "text/html; charset=UTF-8", null);
        tittleTextView.setText(mStagoryDetailBean.getTitle());
        if(mStagoryDetailBean.getForumUrl()==null||mStagoryDetailBean.getForumUrl().trim().length()>0){
            mComunity.setVisibility(View.GONE);
        }else {
            mComunity.setVisibility(View.VISIBLE);
        }
    }

    private void initTitle() {
        View tittleView = LayoutInflater.from(getActivity()).inflate(R.layout.default_title_layout, null);
        tittleView.findViewById(R.id.back).setOnClickListener(this);
        tittleTextView = (TextView) tittleView.findViewById(R.id.title);
        ((BaseActivity)getActivity()).setmTitleView(tittleView);
    }
}
