package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.*;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.activity.GameDetailActivity;
import com.youle.gamebox.ui.api.StagoryDetailApi;
import com.youle.gamebox.ui.bean.StagoryDetailBean;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import org.json.JSONException;

/**
 * Created by Administrator on 14-6-27.
 */
public class StagoryDetailFragment extends DetailDownloadFragment implements View.OnClickListener {
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
    TextView tittleTextView;
    @InjectView(R.id.openAndCommunity)
    LinearLayout mOpenAndCommunity;
    @InjectView(R.id.gameDetail)
    TextView mGameDetail;
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
    protected String getModelName() {
        return "攻略详情";
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mOpenLayout.setOnClickListener(this);
        initTitle();
        if (mStagoryDetailBean == null) {
            loadData();
        }
        mDownloadIcon = (ImageView) view.findViewById(R.id.downloadIcon);
        mDownloadText = (TextView) view.findViewById(R.id.downloadText);
        mDownloadLayout = (RelativeLayout) view.findViewById(R.id.downloadLayout);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.openLayout) {
            AppInfoUtils.startAPP(getActivity(), mStagoryDetailBean.getPackageName());
        } else if (v.getId() == R.id.back) {
            if (getActivity() != null) {
                ((BaseActivity) getActivity()).onBackPressed();
            }
        }
    }


    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.downloadLayout) {
                if (mStagoryDetailBean != null) {
                    GameBean gameBean = (GameBean) v.getTag();
                    gameBean.setId(mStagoryDetailBean.getId());
                    gameBean.setDownloadUrl(mStagoryDetailBean.getDownloadUrl());
                    gameBean.setIconUrl(mStagoryDetailBean.getIconUrl());
                    gameBean.setPackageName(mStagoryDetailBean.getPackageName());
                    gameBean.setName(mStagoryDetailBean.getGameName());
                    downLoadBean(gameBean);
                }
            } else if (v.getId() == R.id.openLayout) {
                AppInfoUtils.startAPP(getActivity(), mStagoryDetailBean.getPackageName());
            } else if (v.getId() == R.id.comunity) {
                WebViewFragment webViewFragment = new WebViewFragment(mStagoryDetailBean.getGameName(), mStagoryDetailBean.getForumUrl());
                ((BaseActivity) getActivity()).addFragment(webViewFragment, true);
            } else if (v.getId() == R.id.gameDetail) {
                GameDetailActivity.startGameDetailActivity(getActivity(), mStagoryDetailBean.getAppId(), mStagoryDetailBean.getGameName(), mStagoryDetailBean.getSource());
            }
        }
    };

    private void loadData() {
        mStagoryDetailApi = new StagoryDetailApi();
        mStagoryDetailApi.setId(id);
        ZhidianHttpClient.request(mStagoryDetailApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    mStagoryDetailBean = jsonToBean(StagoryDetailBean.class, jsonString);
                    if (mStagoryDetailBean != null && getActivity() != null) {
                        mProgressBar.setTag(mStagoryDetailBean.getDownloadUrl());
                        initView();
                        initButtonLayout();
                        GameBean gameBean = new GameBean();
                        gameBean.setPackageName(mStagoryDetailBean.getPackageName());
                        gameBean.setDownloadUrl(mStagoryDetailBean.getDownloadUrl());
                        gameBean.setSize(mStagoryDetailBean.getSize());
                        gameBean.setScore(mStagoryDetailBean.getScore());
                        gameBean.setCategory(mStagoryDetailBean.getCategory());
                        gameBean.setDownloads(mStagoryDetailBean.getDownloads());
                        gameBean.setHasSpree(mStagoryDetailBean.isHasSpree());
                        gameBean.setId(mStagoryDetailBean.getAppId());
                        initDownloadStatus(gameBean);
                        mDownloadLayout.setTag(gameBean);
                        mDownloadLayout.setOnClickListener(onClickListener);
                        mComunity.setOnClickListener(onClickListener);
                        mOpenLayout.setOnClickListener(onClickListener);
                        mGameDetail.setOnClickListener(onClickListener);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initButtonLayout() {
        if (AppInfoUtils.isInstall(getActivity(), mStagoryDetailBean.getPackageName())) {
            mDownloadLayout.setVisibility(View.GONE);
            if (mStagoryDetailBean.getForumUrl() == null) {
                mComunity.setVisibility(View.GONE);
                mOpenLayout.setVisibility(View.VISIBLE);
            } else {
                mComunity.setVisibility(View.VISIBLE);
                mOpenLayout.setVisibility(View.VISIBLE);
            }
        } else {
            mDownloadLayout.setVisibility(View.VISIBLE);
            mOpenAndCommunity.setVisibility(View.GONE);
        }
    }

    private void initView() {
        ImageLoadUtil.displayImage(mStagoryDetailBean.getIconUrl(), mGameIcon);
        mGameName.setText(mStagoryDetailBean.getGameName());
        mGameType.setText("版本:" + mStagoryDetailBean.getVersion() + "|" + mStagoryDetailBean.getSize());
        mScore.setRating(mStagoryDetailBean.getScore() / 2.0f);
        mContent.loadData(mStagoryDetailBean.getContent().trim(), "text/html; charset=UTF-8", null);
        tittleTextView.setText(mStagoryDetailBean.getTitle());
        if (mStagoryDetailBean.getForumUrl() == null || mStagoryDetailBean.getForumUrl().trim().length() > 0) {
            mComunity.setVisibility(View.GONE);
        } else {
            mComunity.setVisibility(View.VISIBLE);
        }
        mStagoryTitle.setText(mStagoryDetailBean.getTitle());
        mData.setText(mStagoryDetailBean.getDate());
    }

    private void initTitle() {
        View tittleView = LayoutInflater.from(getActivity()).inflate(R.layout.default_title_layout, null);
        tittleView.findViewById(R.id.back).setOnClickListener(this);
        tittleTextView = (TextView) tittleView.findViewById(R.id.title);
        ((BaseActivity) getActivity()).setmTitleView(tittleView);
    }
}
