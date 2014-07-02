package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import com.youle.gamebox.ui.DaoManager;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.DownLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.view.RoundProgressView;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Administrator on 14-6-5.
 */
public abstract class DownloadAdapter<T> extends YouleBaseAdapter implements DownLoadUtil.IDownLoadListener {
    public DownLoadUtil downLoadUtil;
    protected Set<RoundProgressView> holdersSet = new HashSet<RoundProgressView>();
    public DownloadAdapter(Context mContext, List<T> mList) {
        super(mContext, mList);
        downLoadUtil = DownLoadUtil.getInstance(this);
    }

    @Override
    public T getItem(int position) {
        return (T) super.getItem(position);
    }


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

    public void downLoadBean(GameBean bean, RoundProgressView progressView) {
        if(downLoadUtil.hasInstall(bean.getPackageName())){
            AppInfoUtils.startAPP(mContext,bean.getPackageName());
            return;
        }
        GameBean downLoadBean = downLoadUtil.getDownloadBeanByUrl(bean.getDownloadUrl());
        if (downLoadBean == null) {
            downLoadBean = bean ;
            downLoadUtil.addHandler(downLoadBean);
            progressView.setLoadingStautsUI(0);
        } else {
            if (downLoadBean.getDownloadStatus() == DownLoadUtil.DOWNLOADING) {
                downLoadUtil.pause(downLoadBean.getDownloadUrl());
                progressView.setLoadConntineStautsUI();
            } else if (downLoadBean.getDownloadStatus() == DownLoadUtil.PAUSE) {
                downLoadUtil.continueDown(downLoadBean.getDownloadUrl());
                int pro = (int) (downLoadBean.getCurrentSize() * 100 / downLoadBean.getTotalSize());
                progressView.setLoadingStautsUI(pro);
            } else if (downLoadBean.getDownloadStatus() == DownLoadUtil.FAIL) {
                downLoadBean = bean ;
                downLoadUtil.addHandler(downLoadBean);
                progressView.setLoadRestartStautsUI();
            } else if (downLoadBean.getDownloadStatus() == DownLoadUtil.SUCCESS) {
                if (downLoadBean.getDownloadPath() != null) {
                    File apk = new File(downLoadBean.getDownloadPath());
                    if (apk.exists()) {
                        AppInfoUtils.install(mContext, new File(downLoadBean.getDownloadPath()));
                    } else {
                        Toast.makeText(mContext, "apk文件不存在需要重新下载", Toast.LENGTH_SHORT).show();
                        DaoManager.getDaoSession().getGameBeanDao().delete(downLoadBean);
                        downLoadBean = bean;
                        downLoadUtil.addHandler(downLoadBean);
                    }
                }
            }
        }
    }

    @Override
    public void onAdd(String url, Boolean isInterrupt) {

    }

    @Override
    public void onLoading(String url, long totalSize, long currentSize, long speed) {
        if (isFliping()) return;
        RoundProgressView holder = getHolderByUrl(url);
        if (holder != null) {
            int pro = (int) (currentSize * 100 / totalSize);
            LOGUtil.d(TAG, "url=" + url + " pro=" + pro);
            holder.setLoadingStautsUI(pro);
        }
    }

    @Override
    public void onSuccess(String url, File file) {
        RoundProgressView holder = getHolderByUrl(url);
        if (holder != null) {
            holder.setLoadInstallStautsUI();
        }
    }

    @Override
    public void onFailure(String url, String strMsg) {
        Toast.makeText(mContext, "下载失败-" + url + " msg=" + strMsg, Toast.LENGTH_SHORT).show();
        RoundProgressView holder = getHolderByUrl(url);
        if (holder != null) {
            holder.setLoadRestartStautsUI();
        }
    }

    @Override
    public void onContinue(String url) {
        GameBean downLoadBean = downLoadUtil.getDownloadBeanByUrl(url);
        RoundProgressView holder = getHolderByUrl(url);
        if (downLoadBean != null && holder != null) {
            int pro = (int) (downLoadBean.getCurrentSize() * 100 / downLoadBean.getTotalSize());
            holder.setLoadingStautsUI(pro);
        }
    }

    @Override
    public void onPause(String url) {
        RoundProgressView holder = getHolderByUrl(url);
        if (holder != null) {
            holder.setLoadConntineStautsUI();
        }
    }

    private RoundProgressView getHolderByUrl(String url) {
        for (RoundProgressView h : holdersSet) {
            GameBean bean = (GameBean) h.getTag();
            if (bean.getDownloadUrl().equals(url)) {
                return h;
            }
        }
        return null;
    }
    protected View.OnClickListener downloadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GameBean bean = (GameBean) v.getTag();
            downLoadBean(bean, (RoundProgressView) v);
        }
    };

    public  void onDestroy(){
        downLoadUtil.unregist(this);
    }

}
