package com.youle.gamebox.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import com.youle.gamebox.ui.DaoManager;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.activity.HomeActivity;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.util.AppInfoUtils;
import com.youle.gamebox.ui.util.DownLoadUtil;
import com.youle.gamebox.ui.util.LOGUtil;
import com.youle.gamebox.ui.util.UIUtil;
import com.youle.gamebox.ui.view.RoundProgressView;
import org.w3c.dom.Text;

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
                File file = new File(downLoadBean.getDownloadPath());
                if(file.exists()) {
                    progressView.setLoadInstallStautsUI();
                }
            } else {
                progressView.setLoadConntineStautsUI();
            }
        } else {
            progressView.setLoadStautsUI();
        }
    }

    public void downLoadBean(GameBean bean) {
        if(TextUtils.isEmpty(bean.getDownloadUrl())){
            UIUtil.toast(mContext, R.string.cant_down);
            return;
        }
        if (downLoadUtil.hasInstall(bean.getPackageName())) {
            if(bean.getDownloadStatus()!=null&&bean.getDownloadStatus()!=DownLoadUtil.FAIL) {
                AppInfoUtils.startAPP(mContext, bean.getPackageName());
            }
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
        if(mContext instanceof HomeActivity){
            ((HomeActivity)mContext).initDownLoadNumber();
        }
    }

    @Override
    public void onAdd(String url, Boolean isInterrupt,GameBean bean) {
        RoundProgressView progressView = getHolderByUrl(url);
        if (progressView != null) {
            Long currentSize = bean.getCurrentSize();
            Long totalSize = bean.getTotalSize();
            if(currentSize!=null&&totalSize!=null) {
                int pro = (int) (currentSize * 100 / totalSize);
                progressView.setLoadingStautsUI(pro);
            }else {
                progressView.setLoadingStautsUI(0);
            }
        }

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
    public void onDelete(String url) {
        RoundProgressView holder = getHolderByUrl(url);
        if (holder != null) {
            holder.setLoadStautsUI();
        }
    }

    @Override
    public void onFailure(String url, String strMsg) {
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
            if(!TextUtils.isEmpty(bean.getDownloadUrl())) {
                if (bean.getDownloadUrl().equals(url)) {
                    return h;
                }
            }
        }
        return null;
    }

    protected View.OnClickListener downloadListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            GameBean bean = (GameBean) v.getTag();
            downLoadBean(bean);
        }
    };

    public void onDestroy() {
        downLoadUtil.unregist(this);
    }

}
