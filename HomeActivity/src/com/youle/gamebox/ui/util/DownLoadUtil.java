package com.youle.gamebox.ui.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.ta.util.download.DownLoadCallback;
import com.ta.util.download.DownloadManager;
import com.youle.gamebox.ui.DaoManager;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.bean.AppInfoBean;
import com.youle.gamebox.ui.greendao.GameBean;

import java.io.File;
import java.util.*;

/**
 * Created by Administrator on 14-5-5.
 */
public class DownLoadUtil extends DownLoadCallback {
    private String TAG = "DownloadUtil";
    public static int DOWNLOADING = 1;
    public static int PAUSE = 2;
    public static int FAIL = 3;
    public static int SUCCESS = 4;
    private static Context contex;
    private static Set<IDownLoadListener> listenerSet = new HashSet<IDownLoadListener>();
    private static Set<String> installApp = new HashSet<String>();

    public interface IDownLoadListener {
        public void onAdd(String url, Boolean isInterrupt);

        public void onLoading(String url, long totalSize, long currentSize, long speed);

        public void onSuccess(String url, File file);

        public void onFailure(String url, String strMsg);

        public void onContinue(String url);

        public void onPause(String url);
    }

    private static Map<String, GameBean> downLoadBeanMap = new HashMap<String, GameBean>();

    private DownLoadUtil() {
    }

    public static void init(Context c) {
        contex = c;
        installApp.clear();
        List<AppInfoBean> list = AppInfoUtils.getPhoneAppInfo(c);
        for (AppInfoBean appInfoBean : list) {
            installApp.add(appInfoBean.getPackageName());
        }
    }

    public static void init() {
        List<GameBean> downLoadBeans = DaoManager.getDaoSession().getGameBeanDao().queryBuilder().build().list();
        for (GameBean b : downLoadBeans) {
            if (b.getDownloadStatus() == DownLoadUtil.DOWNLOADING) {
                if (!DownloadManager.getDownloadManager().hasHandler(b.getDownloadUrl())) {
                    b.setDownloadStatus(DownLoadUtil.FAIL);
                }
            }
            downLoadBeanMap.put(b.getDownloadUrl(), b);
        }
    }

    private static DownLoadUtil instance;

    public static DownLoadUtil getInstance(IDownLoadListener listener) {
        if (instance == null) {
            instance = new DownLoadUtil();
        }
        if(listener!=null) {
            listenerSet.add(listener);
        }
        return instance;
    }

    public List<GameBean> getDowanLoadList(){
        List<GameBean> list = new ArrayList<GameBean>() ;
        for(String key:downLoadBeanMap.keySet()){
            list.add(downLoadBeanMap.get(key));
        }
        return  list ;
    }


    public GameBean getDownloadBeanByUrl(String url) {
        return downLoadBeanMap.get(url);
    }

    public boolean hasInstall(String packageName) {
        if (packageName == null) return false;
        for (String pa : installApp) {
            if (packageName.equals(pa)) {
                return true;
            }
        }
        return false;
    }

    public void addHandler(GameBean bean) {
        Toast.makeText(contex, "新增下载->" + bean.getName(), Toast.LENGTH_SHORT).show();
        DownloadManager manager = DownloadManager.getDownloadManager();
        if (bean == null || bean.getDownloadUrl() == null) return;
        if (downLoadBeanMap.get(bean.getDownloadUrl()) != null) {
            manager.deleteHandler(bean.getDownloadUrl());
            downLoadBeanMap.remove(bean.getDownloadUrl());
        }
        downLoadBeanMap.put(bean.getDownloadUrl(), bean);
        bean.setDownloadStatus(DOWNLOADING);
        bean.setCurrentSize(0L);
        bean.setTotalSize(100L);
        showCustomizeNotification(bean);
        manager.setDownLoadCallback(this);
        manager.addHandler(bean.getDownloadUrl());
    }

    @Override
    public void onLoading(String url, long totalSize, long currentSize, long speed) {
        super.onLoading(url, totalSize, currentSize, speed);
        GameBean bean = downLoadBeanMap.get(url);
        if (bean.getDownloadStatus() == PAUSE) return;
        if (bean.getDownloadStatus() == SUCCESS) return;
        if (bean.getDownloadStatus() == FAIL) return;
        int oldpro = (int) (bean.getCurrentSize() * 100 / bean.getTotalSize());
        int pro = (int) (currentSize * 100 / totalSize);
        if (pro - oldpro <= 0) return;
        bean.setTotalSize(totalSize);
        bean.setCurrentSize(currentSize);
        bean.setDownloadStatus(DOWNLOADING);
        DaoManager.getDaoSession().getGameBeanDao().insertOrReplace(bean);
        for (IDownLoadListener listener : listenerSet) {
            listener.onLoading(url, totalSize, currentSize, speed);
        }
    }

    @Override
    public void onSuccess(String url, File file) {
        installApk(file);
        GameBean bean = downLoadBeanMap.get(url);
        bean.setDownloadStatus(SUCCESS);
        bean.setDownloadPath(file.getPath());
        DaoManager.getDaoSession().getGameBeanDao().insertOrReplace(bean);
        deleteDownload(url);
        super.onSuccess(url, file);
        for (IDownLoadListener listener : listenerSet) {
            listener.onSuccess(url, file);
        }
    }

    private void deleteDownload(String url) {
        downLoadBeanMap.remove(url);
        DownloadManager.getDownloadManager().deleteHandler(url);
    }

    private void installApk(File apk) {
        AppInfoUtils.install(contex, apk);
    }

    @Override
    public void onFailure(String url, String strMsg) {
        super.onFailure(url, strMsg);
        GameBean b = downLoadBeanMap.get(url);
        b.setDownloadStatus(FAIL);
        for (IDownLoadListener listener : listenerSet) {
            listener.onFailure(url, strMsg);
        }
    }

    @Override
    public void onAdd(String url, Boolean isInterrupt) {
        for (IDownLoadListener listener : listenerSet) {
            listener.onAdd(url, isInterrupt);
        }
    }


    //自定义显示的通知 ，创建RemoteView对象
    private void showCustomizeNotification(GameBean bean) {

        CharSequence title = "游乐游戏下载";
        int icon = R.drawable.ic_launcher;

        long when = System.currentTimeMillis();
        Notification noti = new Notification(icon, title, when + 10000);
        noti.flags = Notification.FLAG_INSISTENT;

        // 1、创建一个自定义的消息布局 view.xml
        // 2、在程序代码中使用RemoteViews的方法来定义image和text。然后把RemoteViews对象传到contentView字段
        RemoteViews remoteView = new RemoteViews(contex.getPackageName(), R.layout.download_notification);
        remoteView.setTextViewText(R.id.gameName, bean.getName());
        noti.contentView = remoteView;
        // 3、为Notification的contentIntent字段定义一个Intent(注意，使用自定义View不需要setLatestEventInfo()方法)
        //这儿点击后简单启动Settings模块
        PendingIntent contentIntent = PendingIntent.getActivity
                (contex, 0, new Intent("com.youle.gamebox.ui.activity.DownLoadManagerActivity"), 0);
        noti.contentIntent = contentIntent;

        NotificationManager mnotiManager = (NotificationManager) contex.getSystemService(Context.NOTIFICATION_SERVICE);
        mnotiManager.notify(0, noti);

    }

    public void unregist(IDownLoadListener listener) {
        listenerSet.remove(listener);
    }

    public void pause(String url) {
        Toast.makeText(contex, "暂停下载", Toast.LENGTH_SHORT).show();
        GameBean b = downLoadBeanMap.get(url);
        b.setDownloadStatus(PAUSE);
        DaoManager.getDaoSession().getGameBeanDao().insertOrReplace(b);
        DownloadManager.getDownloadManager().pauseHandler(url);
        for (IDownLoadListener listener : listenerSet) {
            listener.onPause(url);
        }
    }

    public void continueDown(String url) {
        Toast.makeText(contex, "继续下载", Toast.LENGTH_SHORT).show();
        GameBean b = downLoadBeanMap.get(url);
        b.setDownloadStatus(DOWNLOADING);
        DaoManager.getDaoSession().getGameBeanDao().insertOrReplace(b);
        DownloadManager.getDownloadManager().continueHandler(url);
        for (IDownLoadListener listener : listenerSet) {
            listener.onContinue(url);
        }
    }

    public void deleteDown(String url) {
        DownloadManager.getDownloadManager().deleteHandler(url);
    }

}
