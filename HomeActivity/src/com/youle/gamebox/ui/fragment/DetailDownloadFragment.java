package com.youle.gamebox.ui.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.InjectView;
import com.youle.gamebox.ui.DaoManager;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.DownLoadUtil;
import com.youle.gamebox.ui.util.TOASTUtil;
import com.youle.gamebox.ui.util.UIUtil;

import java.io.File;

/**
 * Created by Administrator on 14-7-7.
 */
public abstract class DetailDownloadFragment extends BaseFragment implements DownLoadUtil.IDownLoadListener {

    ProgressBar mProgressBar;
    ImageView mDownloadIcon;
    TextView mDownloadText;
    RelativeLayout mDownloadLayout;
    DownLoadUtil mDownLoadUtil ;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mDownLoadUtil == null){
            mDownLoadUtil = DownLoadUtil.getInstance(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mDownLoadUtil != null){
            mDownLoadUtil.unregist(this);
        }
    }

    @Override
    public void onAdd(String url, Boolean isInterrupt,GameBean gameBean) {
        mDownloadIcon.setVisibility(View.GONE);
        if(gameBean!=null){
            Long currentSize = gameBean.getCurrentSize();
            Long totalSize = gameBean.getTotalSize();
            if(currentSize!=null&&totalSize!=null) {
                int pro = (int) (currentSize * 100 / totalSize);
                mDownloadText.setText(pro+"%");
            }else {
                mDownloadText.setText("0%");
            }
        }
    }

    @Override
    public void onLoading(String url, long totalSize, long currentSize, long speed) {
            mProgressBar.setMax((int)totalSize);
        String tagUrl = (String) mProgressBar.getTag();
        if(tagUrl!=null && tagUrl.equals(url)) {
            mProgressBar.setProgress((int) currentSize);
            int pro = (int) (currentSize * 100 / totalSize);
            mDownloadText.setText(pro + "%");
        }
    }


    protected void initDownloadStatus(GameBean bean) {
        if (mDownLoadUtil.hasInstall(bean.getPackageName())) {
            mDownloadIcon.setImageDrawable(getResources().getDrawable(R.drawable.detail_install));
            mDownloadText.setText(R.string.install);
            return;
        }
        GameBean downLoadBean = mDownLoadUtil.getDownloadBeanByUrl(bean.getDownloadUrl());
        if (downLoadBean != null) {
            if (downLoadBean.getDownloadStatus() == DownLoadUtil.DOWNLOADING) {
                int pro = (int) (downLoadBean.getCurrentSize() * 100 / downLoadBean.getTotalSize());
                mDownloadText.setText(pro+"%");
                mProgressBar.setMax(Integer.parseInt(downLoadBean.getTotalSize()+""));
                mProgressBar.setProgress(Integer.parseInt(downLoadBean.getCurrentSize()+""));
                mDownloadIcon.setVisibility(View.GONE);
            } else if (downLoadBean.getDownloadStatus() == DownLoadUtil.FAIL) {
                mDownloadIcon.setVisibility(View.VISIBLE);
                mDownloadIcon.setImageResource(R.drawable.game_detail_fail);
                mDownloadText.setText(R.string.re_down);
            } else if (downLoadBean.getDownloadStatus() == DownLoadUtil.SUCCESS) {
                mDownloadIcon.setVisibility(View.VISIBLE);
                mDownloadIcon.setImageDrawable(getResources().getDrawable(R.drawable.detail_install));
                mDownloadText.setText(R.string.install);
            } else if(downLoadBean.getDownloadStatus() == DownLoadUtil.PAUSE){
                mDownloadIcon.setImageDrawable(getResources().getDrawable(R.drawable.detail_pause));
                mDownloadIcon.setVisibility(View.VISIBLE);
                int pro = (int) (downLoadBean.getCurrentSize() * 100 / downLoadBean.getTotalSize());
                mDownloadText.setText(pro+"%");
            }else{
                mDownloadIcon.setImageDrawable(getResources().getDrawable(R.drawable.detail_down_icon));
                mDownloadIcon.setVisibility(View.VISIBLE);
                mDownloadText.setText(R.string.download);
            }
        } else {
            mDownloadIcon.setImageDrawable(getResources().getDrawable(R.drawable.detail_down_icon));
            mDownloadIcon.setVisibility(View.VISIBLE);
            mDownloadText.setText(R.string.download);
        }
    }

    @Override
    public void onSuccess(String url, File file) {
        String tagString = (String) mProgressBar.getTag();
        if(tagString!=null&&tagString.equals(url)) {
            mDownloadText.setText(R.string.install);
            mDownloadIcon.setVisibility(View.VISIBLE);
            mDownloadIcon.setImageDrawable(getResources().getDrawable(R.drawable.detail_install));
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onFailure(String url, String strMsg) {
        mDownloadIcon.setImageDrawable(getResources().getDrawable(R.drawable.detail_down_icon));
        mDownloadText.setText(R.string.download);
    }

    @Override
    public void onContinue(String url) {
        mDownloadIcon.setVisibility(View.GONE);
    }

    @Override
    public void onPause(String url) {
        mDownloadIcon.setVisibility(View.VISIBLE);
        mDownloadIcon.setImageDrawable(getResources().getDrawable(R.drawable.detail_pause));
    }

    @Override
    public void onDelete(String url) {
        mDownloadIcon.setVisibility(View.VISIBLE);
        mDownloadIcon.setImageDrawable(getResources().getDrawable(R.drawable.detail_down_icon));
        mDownloadText.setText(R.string.download);
    }
    public void downLoadBean(GameBean bean) {
        if (mDownLoadUtil.hasInstall(bean.getPackageName())) {
            AppInfoUtils.startAPP(getActivity(), bean.getPackageName());
            return;
        }
        GameBean downLoadBean = mDownLoadUtil.getDownloadBeanByUrl(bean.getDownloadUrl());
        if (downLoadBean == null) {
            downLoadBean = bean;
            mDownLoadUtil.addHandler(downLoadBean);
        } else {
            if (downLoadBean.getDownloadStatus() == DownLoadUtil.DOWNLOADING) {
                mDownLoadUtil.pause(downLoadBean.getDownloadUrl());
            } else if (downLoadBean.getDownloadStatus() == DownLoadUtil.PAUSE) {
                mDownLoadUtil.continueDown(downLoadBean.getDownloadUrl());
                int pro = (int) (downLoadBean.getCurrentSize() * 100 / downLoadBean.getTotalSize());
            } else if (downLoadBean.getDownloadStatus() == DownLoadUtil.FAIL) {
                downLoadBean = bean;
                mDownLoadUtil.addHandler(downLoadBean);
            } else if (downLoadBean.getDownloadStatus() == DownLoadUtil.SUCCESS) {
                if (downLoadBean.getDownloadPath() != null) {
                    File apk = new File(downLoadBean.getDownloadPath());
                    if (apk.exists()) {
                        AppInfoUtils.install(getActivity(), new File(downLoadBean.getDownloadPath()));
                    } else {
                        UIUtil.toast(getActivity(), "apk文件不存在需要重新下载");
                        DaoManager.getDaoSession().getGameBeanDao().delete(downLoadBean);
                        downLoadBean = bean;
                        mDownLoadUtil.addHandler(downLoadBean);
                    }
                }
            }
        }
    }
}
