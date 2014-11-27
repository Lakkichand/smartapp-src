package com.youle.gamebox.ui.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.youle.gamebox.ui.DaoManager;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.GameDetailActivity;
import com.youle.gamebox.ui.activity.HomeActivity;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.DownLoadUtil;
import com.youle.gamebox.ui.util.ImageLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;

import java.io.File;

/**
 * Created by Administrator on 14-6-3.
 */
public class RecommendGridItem extends LinearLayout implements DownLoadUtil.IDownLoadListener {
    @InjectView(R.id.gameIcon)
    ImageView mGameIcon;
    @InjectView(R.id.gameIconLayout)
    RelativeLayout mGameIconLayout;
    @InjectView(R.id.gameName)
    TextView mGameName;
    @InjectView(R.id.gameSize)
    TextView mGameSize;
    @InjectView(R.id.gameType)
    TextView mGameType;
    @InjectView(R.id.gamedesLayout)
    LinearLayout mGamedesLayout;
    @InjectView(R.id.scro)
    RatingBar mScro;
    @InjectView(R.id.roundProgress)
    RoundProgressView mRoundProgress;
    @InjectView(R.id.downloadNumber)
    TextView mDownloadNumber;
    @InjectView(R.id.gift)
    ImageView mGift;
    private GameBean bean;
    DownLoadUtil downLoadUtil;

    public RecommendGridItem(Context context, GameBean bean) {
        super(context);
        this.bean = bean;
        LayoutInflater.from(context).inflate(R.layout.recomend_game_item, this);
        ButterKnife.inject(this);
        initGameBean();
        downLoadUtil = DownLoadUtil.getInstance(this);
        initDownloadStatus(mRoundProgress, bean);
        mRoundProgress.setTag(bean);
        mRoundProgress.setOnClickListener(downloadListener);
        setOnClickListener(showGameDetailListener);
    }

    private void initGameBean() {
        mGameName.setText(bean.getName());
        mGameSize.setText(bean.getSize());
        mGameType.setText(" | " + bean.getCategory());
        if(bean.getScore()!=null) {
            mScro.setRating(bean.getScore() / 2.0f);
        }else {
            mScro.setRating(0f);
        }
        ImageLoadUtil.displayImage(bean.getIconUrl(), mGameIcon);
        mDownloadNumber.setText(bean.getDownloads());
        if (bean.getHasSpree()) {
            mGift.setVisibility(VISIBLE);
        } else {
            mGift.setVisibility(GONE);
        }
    }

    private OnClickListener showGameDetailListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            GameDetailActivity.startGameDetailActivity(getContext(), bean.getId(), bean.getName(), bean.getSource());
        }
    };

    public void downLoadBean(GameBean bean) {
        LOGUtil.e("TAG",bean.getDownloadUrl());
        if (downLoadUtil.hasInstall(bean.getPackageName())) {
            AppInfoUtils.startAPP(getContext(), bean.getPackageName());
            return;
        }
        GameBean downLoadBean = downLoadUtil.getDownloadBeanByUrl(bean.getDownloadUrl());
        if (downLoadBean == null) {
            downLoadBean = bean;
            downLoadUtil.addHandler(downLoadBean);
        } else {
            if (downLoadBean.getDownloadStatus() == DownLoadUtil.DOWNLOADING) {
                downLoadUtil.pause(downLoadBean.getDownloadUrl());
            } else if (downLoadBean.getDownloadStatus() == DownLoadUtil.PAUSE) {
                downLoadUtil.continueDown(downLoadBean.getDownloadUrl());
                int pro = (int) (downLoadBean.getCurrentSize() * 100 / downLoadBean.getTotalSize());
            } else if (downLoadBean.getDownloadStatus() == DownLoadUtil.FAIL) {
                downLoadBean = bean;
                downLoadUtil.addHandler(downLoadBean);
            } else if (downLoadBean.getDownloadStatus() == DownLoadUtil.SUCCESS) {
                if (downLoadBean.getDownloadPath() != null) {
                    File apk = new File(downLoadBean.getDownloadPath());
                    if (apk.exists()) {
                        AppInfoUtils.install(getContext(), new File(downLoadBean.getDownloadPath()));
                    } else {
                        DaoManager.getDaoSession().getGameBeanDao().delete(downLoadBean);
                        downLoadBean = bean;
                        downLoadUtil.addHandler(downLoadBean);
                    }
                }
            }
        }
        ((HomeActivity)getContext()).initDownLoadNumber();
    }

    protected View.OnClickListener downloadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GameBean bean = (GameBean) v.getTag();
            downLoadBean(bean);
        }
    };

    protected void initDownloadStatus(RoundProgressView progressView, GameBean bean) {
        if (downLoadUtil.hasInstall(bean.getPackageName())) {
            progressView.setLoadOpenStautsUI();
            return;
        }
        GameBean downLoadBean = downLoadUtil.getDownloadBeanByUrl(bean.getDownloadUrl());
        if (downLoadBean != null) {
            if (downLoadBean.getDownloadStatus() == DownLoadUtil.DOWNLOADING) {
                int pro = (int) (downLoadBean.getCurrentSize() * 100 / downLoadBean.getTotalSize());
                progressView.setLoadingStautsUI(pro);
            } else if (downLoadBean.getDownloadStatus() == DownLoadUtil.FAIL) {
                progressView.setLoadRestartStautsUI();
            } else if (downLoadBean.getDownloadStatus() == DownLoadUtil.SUCCESS) {
                progressView.setLoadInstallStautsUI();
            } else {
                progressView.setLoadConntineStautsUI();
            }
        } else {
            progressView.setLoadStautsUI();
        }
    }

    public void unRegist() {
        downLoadUtil.unregist(this);
    }

    @Override
    public void onAdd(String url, Boolean isInterrupt,GameBean gameBean) {
        if (url.equals(bean.getDownloadUrl())) {
            Long currentSize = bean.getCurrentSize();
            Long totalSize = bean.getTotalSize();
            if(currentSize!=null&&totalSize!=null) {
                int pro = (int) (currentSize * 100 / totalSize);
                mRoundProgress.setLoadingStautsUI(pro);
            }else {
                mRoundProgress.setLoadingStautsUI(0);
            }
        }
    }

    @Override
    public void onLoading(String url, long totalSize, long currentSize, long speed) {
        if (url.equals(bean.getDownloadUrl())) {
            int pro = (int) (currentSize * 100 / totalSize);
            mRoundProgress.setLoadingStautsUI(pro);
        }
    }

    @Override
    public void onSuccess(String url, File file) {
        if (url.equals(bean.getDownloadUrl())) {
            mRoundProgress.setLoadInstallStautsUI();
        }
    }

    @Override
    public void onDelete(String url) {
        if (url.equals(bean.getDownloadUrl())) {
            mRoundProgress.setLoadStautsUI();
        }
    }

    @Override
    public void onFailure(String url, String strMsg) {
        if (url.equals(bean.getDownloadUrl())) {
            mRoundProgress.setLoadRestartStautsUI();
        }
    }

    @Override
    public void onContinue(String url) {
        if (url.equals(bean.getDownloadUrl())) {
            GameBean downLoadBean = downLoadUtil.getDownloadBeanByUrl(url);
            int pro = (int) (downLoadBean.getCurrentSize() * 100 / downLoadBean.getTotalSize());
            mRoundProgress.setLoadingStautsUI(pro);
        }
    }

    @Override
    public void onPause(String url) {
        if (url.equals(bean.getDownloadUrl())) {
            mRoundProgress.setLoadConntineStautsUI();
        }
    }
}
