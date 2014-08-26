package com.youle.gamebox.ui.fragment;


import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.BaseActivity;
import com.youle.gamebox.ui.activity.DisplayBigImageActivity;
import com.youle.gamebox.ui.activity.GameDetailActivity;
import com.youle.gamebox.ui.api.game.GetGameDetailApi;
import com.youle.gamebox.ui.bean.GameDetailBean;
import com.youle.gamebox.ui.bean.LimitBean;
import com.youle.gamebox.ui.bean.MiniGameBean;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.view.GameDetailNewsItemView;
import org.json.JSONException;

import java.util.List;

public class AppDetailGamesFragment extends DetailDownloadFragment {
    @InjectView(R.id.gameIcon)
    ImageView mGameIcon;
    @InjectView(R.id.score)
    RatingBar mScore;
    @InjectView(R.id.version)
    TextView mVersion;
    @InjectView(R.id.downloadTimes)
    TextView mDownloadTimes;
    @InjectView(R.id.download)
    TextView mDownload;
    @InjectView(R.id.gameType)
    TextView mGameType;
    @InjectView(R.id.gameTime)
    TextView mGameTime;
    @InjectView(R.id.language)
    TextView mLanguage;
    @InjectView(R.id.newsLayout)
    LinearLayout mNewsLayout;
    @InjectView(R.id.imageLayout)
    LinearLayout mImageLayout;
    @InjectView(R.id.gameDesc)
    TextView mGameDesc;
    @InjectView(R.id.tagIcon)
    ImageView mTagIcon;
    @InjectView(R.id.showMore)
    LinearLayout mShowMore;
    @InjectView(R.id.aboutGameLayout)
    LinearLayout mAboutGameLayout;
    @InjectView(R.id.imageScrollView)
    HorizontalScrollView mImageScrollView;
    @InjectView(R.id.openLayout)
    LinearLayout mOpenLayout;
    @InjectView(R.id.comunity)
    TextView mComunity;
    @InjectView(R.id.openAndCommunity)
    LinearLayout mOpenAndCommunity;
    @InjectView(R.id.moreLayout)
    LinearLayout mMoreLayout;
    @InjectView(R.id.showMoreText)
    TextView mShowMoreText;
    @InjectView(R.id.showMoreIcon)
    ImageView mShowMoreIcon;
    @InjectView(R.id.mach_height)
    TextView mMachHeiht;
    @InjectView(R.id.bottomLayout)
    LinearLayout mBottomLayout;
    @InjectView(R.id.loading)
    View mStartLoad ;
    @InjectView(R.id.noNet)
    View mNoNetView;
    @InjectView(R.id.scroolView)
    View mContent ;
    private View newLine;
    private GameDetailBean mGameDetailBean;
    private long id;
    private int resouce;

    public AppDetailGamesFragment(long id, int resouce) {
        super();
        this.id = id;
        this.resouce = resouce;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mGameDetailBean == null) {
            loadData();
        }
        mDownloadIcon = (ImageView) view.findViewById(R.id.downloadIcon);
        mDownloadText = (TextView) view.findViewById(R.id.downloadText);
        mDownloadLayout = (RelativeLayout) view.findViewById(R.id.downloadLayout);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        newLine = view.findViewById(R.id.newsLayoutLine);
        mDownloadLayout.setOnClickListener(onClickListener);
        mComunity.setOnClickListener(onClickListener);
        mOpenLayout.setOnClickListener(onClickListener);
        mMoreLayout.setOnClickListener(onClickListener);
        mNoNetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
    }

    @Override
    protected int getViewId() {
        return R.layout.fragment_game_detail;
    }

    @Override
    public void onFailure(Throwable error) {
        mNoNetView.setVisibility(View.VISIBLE);
        mStartLoad.setVisibility(View.GONE);
        mContent.setVisibility(View.GONE);
    }
    @Override
    protected String getModelName() {
        return "游戏详情";
    }


    @Override
    public void onLoadStart() {
        mStartLoad.setVisibility(View.VISIBLE);
        mNoNetView.setVisibility(View.GONE);
        mContent.setVisibility(View.GONE);
    }
    @Override
    public void onSuccess(String content) {
        mStartLoad.setVisibility(View.GONE);
        mNoNetView.setVisibility(View.GONE);
        mContent.setVisibility(View.VISIBLE);
    }
    protected void loadData() {
        GetGameDetailApi getGameDetailApi = new GetGameDetailApi();
        getGameDetailApi.setId(id + "");
        getGameDetailApi.setSource(resouce + "");
        ZhidianHttpClient.request(getGameDetailApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    if(getActivity()==null) return;
                    mGameDetailBean = jsonToBean(GameDetailBean.class, jsonString);
                    if (mGameDetailBean != null) {
                        initView();
                        initButtonLayout();
                        GameBean gameBean = new GameBean();
                        gameBean.setPackageName(mGameDetailBean.getPackageName());
                        gameBean.setDownloadUrl(mGameDetailBean.getDownloadUrl());
                        initDownloadStatus(gameBean);
                        mProgressBar.setTag(mGameDetailBean.getDownloadUrl());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isNull = true;

    private void initView() {
        ImageLoadUtil.displayImage(mGameDetailBean.getIconUrl(), mGameIcon);
        mScore.setRating(mGameDetailBean.getScore() / 2.0f);
        mVersion.setText("版本：" + mGameDetailBean.getVersion());
        mDownloadTimes.setText("下载：" + mGameDetailBean.getDownloads());
        mGameType.setText("类型：" + mGameDetailBean.getCategory());
        mLanguage.setText("语言：" + mGameDetailBean.getLanguage());
        mGameTime.setText("时间：" + mGameDetailBean.getDate());
        mDownload.setText("大小：" + mGameDetailBean.getSize());
        mGameDesc.setText(mGameDetailBean.getContent());
        if(mGameDesc.getLineCount()<4){
            mMoreLayout.setVisibility(View.GONE);
        }else {
            mGameDesc.setMaxLines(3);
        }
        mNewsLayout.removeAllViews();
        mImageLayout.removeAllViews();
        mAboutGameLayout.removeAllViews();
        if (mGameDetailBean.getSprees() != null && mGameDetailBean.getSprees().size() > 0) {
            isNull = false;
            for (LimitBean news : mGameDetailBean.getSprees()) {
                mNewsLayout.addView(new GameDetailNewsItemView(getActivity(), news, GameDetailNewsItemView.GIFT));
            }
        }
        if (mGameDetailBean.getScreenshotsUrls() != null && mGameDetailBean.getScreenshotsUrls().size() > 0) {
            initImages();
        } else {
            mImageScrollView.setVisibility(View.GONE);
        }
        if (mGameDetailBean.getGames() != null && mGameDetailBean.getGames().size() > 0) {
            initGames();
        }
        if (mGameDetailBean.getGonglues() != null && mGameDetailBean.getGonglues().size() > 0) {
            isNull = false;
            for (LimitBean news : mGameDetailBean.getGonglues()) {
                mNewsLayout.addView(new GameDetailNewsItemView(getActivity(), news, GameDetailNewsItemView.STAGRAY));
            }
        }
        if (mGameDetailBean.getNews() != null && mGameDetailBean.getNews().size() > 0) {
            isNull = false;
            for (LimitBean news : mGameDetailBean.getNews()) {
                mNewsLayout.addView(new GameDetailNewsItemView(getActivity(), news, GameDetailNewsItemView.NEWS));
            }
        }
        if (mGameDetailBean.getSpecials() != null && mGameDetailBean.getSpecials().size() > 0) {
            isNull = false;
            for (LimitBean news : mGameDetailBean.getSpecials()) {
                mNewsLayout.addView(new GameDetailNewsItemView(getActivity(), news, GameDetailNewsItemView.THEME));
            }
        }
        if (isNull) {
            mNewsLayout.setVisibility(View.GONE);
            newLine.setVisibility(View.GONE);
        }

    }


    private void initGames() {
        if (getActivity() == null) return;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        for (MiniGameBean b : mGameDetailBean.getGames()) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.litle_game_layout, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.gameIcon);
            TextView textView = (TextView) view.findViewById(R.id.gameName);
            ImageLoadUtil.displayImage(b.getIconUrl(), imageView);
            textView.setText(b.getName());
            view.setTag(b);
            view.setOnClickListener(onGameOnclickListener);
            mAboutGameLayout.addView(view, layoutParams);
        }
        if (mGameDetailBean.getGames().size() < 4) {
            for (int i = mGameDetailBean.getGames().size(); i < 4; i++) {
                TextView t = new TextView(getActivity());
                mAboutGameLayout.addView(t, layoutParams);
            }
        }
    }

    private View.OnClickListener onGameOnclickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MiniGameBean miniGameBean = (MiniGameBean) v.getTag();
            GameDetailActivity.startGameDetailActivity(getActivity(), miniGameBean.getId(), miniGameBean.getName(), miniGameBean.getSource());
        }
    };

    private void initImages() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.game_detail_weight), getResources().getDimensionPixelSize(R.dimen.game_detail_height));
        p.setMargins(getResources().getDimensionPixelSize(R.dimen.game_detail_margin), 0, getResources().getDimensionPixelSize(R.dimen.game_detail_margin), 0);
        for (String url : mGameDetailBean.getScreenshotsUrls()) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setOnClickListener(imageOnClickListener);
            mImageLayout.addView(imageView, p);
            ImageLoadUtil.displayNotRundomImage(url, imageView);
        }
        if(mImageLayout.getChildCount()<3){
            mImageLayout.setGravity(Gravity.CENTER);
        }
    }

    private View.OnClickListener imageOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mGameDetailBean.getRawScreenshotsUrls()!=null) {
                DisplayMoreBigImageFragment displayMoreBigImageFragment = new DisplayMoreBigImageFragment(mGameDetailBean.getRawScreenshotsUrls(),mGameDetailBean.getName());
                ((BaseActivity) getActivity()).addFragment(displayMoreBigImageFragment, true);
            }
        }
    };

    private void initButtonLayout() {
        if (AppInfoUtils.isInstall(getActivity(), mGameDetailBean.getPackageName())) {
            mDownloadLayout.setVisibility(View.GONE);
            mOpenAndCommunity.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(mGameDetailBean.getForumUrl())) {
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
        mMachHeiht.setHeight(mBottomLayout.getHeight());
    }

    private int maxLine = 3;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.downloadLayout) {
                if (mGameDetailBean != null) {
                    GameBean gameBean = new GameBean();
                    gameBean.setId(mGameDetailBean.getId());
                    gameBean.setDownloadUrl(mGameDetailBean.getDownloadUrl());
                    gameBean.setIconUrl(mGameDetailBean.getIconUrl());
                    gameBean.setPackageName(mGameDetailBean.getPackageName());
                    gameBean.setName(mGameDetailBean.getName());
                    gameBean.setHasSpree(mGameDetailBean.isHasSpree());
                    gameBean.setDownloads(mGameDetailBean.getDownloads());
                    gameBean.setScore(mGameDetailBean.getScore());
                    gameBean.setSize(mGameDetailBean.getSize());
                    gameBean.setCategory(mGameDetailBean.getCategory());
                    downLoadBean(gameBean);
                }
            } else if (v.getId() == R.id.openLayout) {
                AppInfoUtils.startAPP(getActivity(), mGameDetailBean.getPackageName());
            } else if (v.getId() == R.id.comunity) {
                WebViewFragment webViewFragment = new WebViewFragment(mGameDetailBean.getName(), mGameDetailBean.getForumUrl());
                ((BaseActivity) getActivity()).addFragment(webViewFragment, true);
            } else if (v.getId() == R.id.moreLayout) {

                if (maxLine == 3) {
                    Animation route = AnimationUtils.loadAnimation(getActivity(),R.anim.rote);
                    mGameDesc.setMaxLines(100);
                    maxLine = 100;
                    mShowMoreText.setText(R.string.fold);
                    mShowMoreIcon.startAnimation(route);
                } else {
                    Animation route1 = AnimationUtils.loadAnimation(getActivity(),R.anim.rote1);
                    maxLine = 3;
                    mGameDesc.setMaxLines(3);
                    mShowMoreText.setText(R.string.see_more);
                    mShowMoreIcon.startAnimation(route1);
                }
            }
        }
    };
}
