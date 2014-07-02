package com.youle.gamebox.ui.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.InjectView;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.GameDetailActivity;
import com.youle.gamebox.ui.api.game.GetGameDetailApi;
import com.youle.gamebox.ui.bean.GameDetailBean;
import com.youle.gamebox.ui.bean.LimitBean;
import com.youle.gamebox.ui.bean.MiniGameBean;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.view.GameDetailNewsItemView;
import org.json.JSONException;

public class AppDetailGamesFragment extends BaseFragment {
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
    }

    @Override
    protected int getViewId() {
        return R.layout.fragment_game_detail;
    }

    protected void loadData() {
        GetGameDetailApi getGameDetailApi = new GetGameDetailApi();
        getGameDetailApi.setId(id + "");
        getGameDetailApi.setSource(resouce + "");
        ZhidianHttpClient.request(getGameDetailApi, new JsonHttpListener(this) {
            @Override
            public void onRequestSuccess(String jsonString) {
                try {
                    mGameDetailBean = jsonToBean(GameDetailBean.class, jsonString);
                    initView();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initView() {
        ImageLoadUtil.displayImage(mGameDetailBean.getIconUrl(), mGameIcon);
        mScore.setRating(mGameDetailBean.getScore() / 2.0f);
        mVersion.setText("版本:" + mGameDetailBean.getVersion() + "|" + mGameDetailBean.getSize());
        mDownloadTimes.setText("下载:" + mGameDetailBean.getDownloads());
        mGameType.setText("类型:");
        mLanguage.setText("语言:" + mGameDetailBean.getLanguage());
        mGameTime.setText("时间:" + mGameDetailBean.getDate());
        mGameDesc.setText(mGameDetailBean.getContent());
        mNewsLayout.removeAllViews();
        mImageLayout.removeAllViews();
        mAboutGameLayout.removeAllViews();
        if (mGameDetailBean.getSprees() != null && mGameDetailBean.getSprees().size() > 0) {
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
            for (LimitBean news : mGameDetailBean.getGonglues()) {
                mNewsLayout.addView(new GameDetailNewsItemView(getActivity(), news, GameDetailNewsItemView.STAGRAY));
            }
        }
        if (mGameDetailBean.getNews() != null && mGameDetailBean.getNews().size() > 0) {
            for (LimitBean news : mGameDetailBean.getNews()) {
                mNewsLayout.addView(new GameDetailNewsItemView(getActivity(), news, GameDetailNewsItemView.NEWS));
            }
        }
        if (mGameDetailBean.getSpecials() != null && mGameDetailBean.getSpecials().size() > 0) {
            for (LimitBean news : mGameDetailBean.getSpecials()) {
                mNewsLayout.addView(new GameDetailNewsItemView(getActivity(), news, GameDetailNewsItemView.THEME));
            }
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
    private View.OnClickListener onGameOnclickListener =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                MiniGameBean miniGameBean = (MiniGameBean) v.getTag();
            Intent intent = new Intent(getActivity(), GameDetailActivity.class) ;
            intent.putExtra(GameDetailActivity.GAME_ID,miniGameBean.getId());
            intent.putExtra(GameDetailActivity.GAME_NAME,miniGameBean.getName());
            intent.putExtra(GameDetailActivity.GAME_RESOUCE,miniGameBean.getSource());
            startActivity(intent);
        }
    };

    private void initImages() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.game_detail_weight), LinearLayout.LayoutParams.MATCH_PARENT);
        p.setMargins(getResources().getDimensionPixelSize(R.dimen.game_detail_margin), 0, getResources().getDimensionPixelSize(R.dimen.game_detail_margin), 0);
        ;
        for (String url : mGameDetailBean.getScreenshotsUrls()) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mImageLayout.addView(imageView, p);
            ImageLoadUtil.displayNotRundomImage(url, imageView);
        }
    }

    private void initNews(int type) {
    }
}
