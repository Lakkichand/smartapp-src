package com.youle.gamebox.ui.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.text.format.*;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.ta.TAApplication;
import com.ta.common.TAStringUtils;
import com.ta.util.config.TAIConfig;
import com.ta.util.download.DownLoadCallback;
import com.ta.util.download.DownloadManager;
import com.youle.gamebox.ui.DaoManager;
import com.youle.gamebox.ui.R;
import com.youle.gamebox.ui.YouleAplication;
import com.youle.gamebox.ui.account.UserInfoCache;
import com.youle.gamebox.ui.activity.HomeActivity;
import com.youle.gamebox.ui.activity.SettingActivity;
import com.youle.gamebox.ui.api.DownloadRecordApi;
import com.youle.gamebox.ui.bean.AppInfoBean;
import com.youle.gamebox.ui.greendao.GameBean;
import com.youle.gamebox.ui.greendao.UserInfo;
import com.youle.gamebox.ui.http.JsonHttpListener;
import com.youle.gamebox.ui.http.ZhidianHttpClient;

import java.io.File;
import java.text.Format;
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
    public static int UPDATE = 5;
    private static Context contex;
    private static Set<IDownLoadListener> listenerSet = new HashSet<IDownLoadListener>();
    public static Set<String> installApp = new HashSet<String>();
    private DownloadManager mDownLoadManager;

    public interface IDownLoadListener {
        public void onAdd(String url, Boolean isInterrupt, GameBean gameBean);

        public void onLoading(String url, long totalSize, long currentSize, long speed);

        public void onSuccess(String url, File file);

        public void onFailure(String url, String strMsg);

        public void onContinue(String url);

        public void onPause(String url);

        public void onDelete(String url);

    }

    public static Map<String, GameBean> downLoadBeanMap = new HashMap<String, GameBean>();

    private DownLoadUtil() {
        mDownLoadManager = DownloadManager.getDownloadManager();
        mDownLoadManager.setDownLoadCallback(this);
    }

    public static void init(Context c) {
        if (contex == null) {
            contex = c;
        }
        installApp.clear();
        List<AppInfoBean> list = AppInfoUtils.getPhoneAppInfo(c);
        for (AppInfoBean appInfoBean : list) {
            if (!contents(appInfoBean.getPackageName())) {
                installApp.add(appInfoBean.getPackageName());
            }
        }
    }

//    public static void removeDownloadByPkg(String pkc){
//       for (String key:downLoadBeanMap.keySet()){
//           GameBean b = downLoadBeanMap.get(key);
//           if(pkc.equals(b.getPackageName())){
//               downLoadBeanMap.remove(key);
//           }
//       }
//    }

    private static boolean contents(String packageName) {
        for (String key : downLoadBeanMap.keySet()) {
            GameBean bean = downLoadBeanMap.get(key);
            if (bean.getPackageName().equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public static int getDownLoadingNumber() {
        return downLoadBeanMap.size();
    }

    public static void init() {
        List<GameBean> downLoadBeans = DaoManager.getDaoSession().getGameBeanDao().queryBuilder().build().list();
        downLoadBeanMap.clear();
        for (GameBean b : downLoadBeans) {
            if (b.getDownloadStatus() == DownLoadUtil.DOWNLOADING) {
                if (!DownloadManager.getDownloadManager().hasHandler(b.getDownloadUrl())) {
                    b.setDownloadStatus(DownLoadUtil.FAIL);
                }
            }
            //处理手动删除apk包的情况
            if (TextUtils.isEmpty(b.getDownloadPath())) {
                DaoManager.getDaoSession().delete(b);
                continue;
            } else {
                File file = new File(b.getDownloadPath());
                if (file.exists()) {
                    downLoadBeanMap.put(b.getDownloadUrl(), b);
                } else {
                    File tempFile = new File(b.getDownloadPath() + ".apk");
                    if (tempFile.exists()) {
                        downLoadBeanMap.put(b.getDownloadUrl(), b);
                    } else {
                        if (b.getCurrentSize() == null || b.getCurrentSize() == 0) {//0%的情况 。。
                            downLoadBeanMap.put(b.getDownloadUrl(), b);
                        } else {
                            DaoManager.getDaoSession().getGameBeanDao().delete(b);
                        }
                    }
                }
            }
        }
    }

    private static DownLoadUtil instance;

    public static DownLoadUtil getInstance(IDownLoadListener listener) {
        if (instance == null) {
            instance = new DownLoadUtil();
        }
        if (listener != null) {
            listenerSet.add(listener);
        }
        return instance;
    }

    public List<GameBean> getDowanLoadList() {
        List<GameBean> list = new ArrayList<GameBean>();
        for (String key : downLoadBeanMap.keySet()) {
            list.add(downLoadBeanMap.get(key));
        }
        return list;
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
//        Toast.makeText(contex, "新增下载->" + bean.getName(), Toast.LENGTH_SHORT).show();
        if (bean == null || bean.getDownloadUrl() == null) return;
        bean.setDownloadPath(DownloadManager.FILE_ROOT + TAStringUtils.getFileNameFromUrl(bean.getDownloadUrl()));
        if (downLoadBeanMap.get(bean.getDownloadUrl()) != null) {
            bean = downLoadBeanMap.get(bean.getDownloadUrl());
            mDownLoadManager.deleteHandler(bean.getDownloadUrl());
            downLoadBeanMap.remove(bean.getDownloadUrl());
        } else {
            bean.setCurrentSize(0L);
            bean.setTotalSize(100L);
        }
        if (bean.getCurrentSize() == null) {
            bean.setCurrentSize(0L);
            bean.setTotalSize(100L);
        }
        downLoadBeanMap.put(bean.getDownloadUrl(), bean);
        bean.setDownloadStatus(DOWNLOADING);
        DaoManager.getDaoSession().getGameBeanDao().insertOrReplace(bean);
        mDownLoadManager.addHandler(bean.getDownloadUrl());
        doanLoadRecoder(DownloadRecordApi.START, bean);
        for (IDownLoadListener listener : listenerSet) {
            listener.onAdd(bean.getDownloadUrl(), false, bean);
        }
//        showCustomizeNotification(bean);
        updateNotification(bean);
    }

    private void doanLoadRecoder(int type, GameBean bean) {
        DownloadRecordApi downloadRecordApi = new DownloadRecordApi();
        UserInfo userInfo = new UserInfoCache().getUserInfo();
        PhoneInformation phoneInformation = new PhoneInformation(contex);
        if (userInfo != null) {
            downloadRecordApi.setSid(userInfo.getSid());
        }
        downloadRecordApi.setType(type);
        downloadRecordApi.setDeviceCode(phoneInformation.getDeviceCode());
        downloadRecordApi.setReleaseVersion(phoneInformation.getReleaseVersion());
        downloadRecordApi.setPhoneModel(phoneInformation.getPhoneModel());
        downloadRecordApi.setAppId(bean.getId() + "");
        downloadRecordApi.setNetworkType(phoneInformation.getNetworkType());
        downloadRecordApi.setResolution(phoneInformation.getResolution());
        downloadRecordApi.setVersion(phoneInformation.getSdkVersion());
        ZhidianHttpClient.request(downloadRecordApi, new JsonHttpListener(false));
    }

    public void downLoadVoice(String url) {
        mDownLoadManager.addHandler(url);
    }

    @Override
    public void onLoading(String url, long totalSize, long currentSize, long speed) {
        super.onLoading(url, totalSize, currentSize, speed);
        if (totalSize == 0) return;
        GameBean bean = downLoadBeanMap.get(url);
        if (bean == null) return;
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
        updateNotification(bean);
    }

    private GameBean currentBean;

    private void updateNotification(GameBean bean) {
        if (notification == null) {
            notification = new Notification();
            Intent intent = new Intent(contex, HomeActivity.class);
            intent.putExtra(HomeActivity.MANAGE, HomeActivity.MANAGE_VALUE);
            PendingIntent contentIntent = PendingIntent.getActivity
                    (contex, 0, intent, 0);
            notification.contentIntent = contentIntent;
        }
        if (mnotiManager == null) {
            mnotiManager = (NotificationManager) contex.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        // 设置通知的icon

        notification.icon = R.drawable.youle_logo;
        // 设置通知在状态栏上显示的滚动信息
        // notification.tickerText = "你有 " + mAllTask.size() + "个应用下载";
        // 设置通知的时间
        notification.when = System.currentTimeMillis();

        if (notification.contentView == null) {
            notification.contentView = new RemoteViews(
                    contex.getPackageName(), R.layout.download_notification);
        }
        int pro = (int) (bean.getCurrentSize() * 100 / bean.getTotalSize());
        notification.contentView.setProgressBar(R.id.progressBar, 100, pro, false);
        String cusize = android.text.format.Formatter.formatFileSize(YouleAplication.getApplication(), bean.getCurrentSize());
        String tSize = android.text.format.Formatter.formatFileSize(YouleAplication.getApplication(), bean.getTotalSize());
        notification.contentView.setTextViewText(R.id.size, cusize + "/" + tSize);
        notification.contentView.setTextViewText(R.id.progrees, pro + "%");
        notification.contentView.setTextViewText(R.id.gameName, bean.getName());
        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(bean.getIconUrl());
        if (currentBean == null || !currentBean.getIconUrl().equals(bean.getIconUrl())) {
            if (bitmap != null) {
                notification.contentView.setImageViewBitmap(R.id.gameIcon, bitmap);
            } else {
                notification.contentView.setImageViewResource(R.id.gameIcon, R.drawable.youle_logo);
            }
        }
        currentBean = bean;
        notification.contentView.setViewVisibility(R.id.progressBar, View.VISIBLE);
        notification.contentView.setViewVisibility(R.id.loading_desc, View.VISIBLE);
        notification.contentView.setViewVisibility(R.id.desc, View.GONE);
        if (downLoadBeanMap.size() > 1) {
            notification.contentView.setImageViewResource(R.id.gameIcon, R.drawable.youle_logo);
            notification.contentView.setTextViewText(R.id.gameName, downLoadBeanMap.size() + "个下载任务");
            notification.contentView.setViewVisibility(R.id.progressBar, View.GONE);
            notification.contentView.setViewVisibility(R.id.loading_desc, View.GONE);
            notification.contentView.setTextViewText(R.id.desc, buildDesc());
            notification.contentView.setViewVisibility(R.id.desc, View.VISIBLE);
            currentBean = null;
        }
//        if (downLoadBeanMap.size() == 1) {
//            notification.contentView.setTextViewText(R.id.gameName, bean.getName());
//            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(bean.getIconUrl());
//            if (bitmap != null) {
//                notification.contentView.setImageViewBitmap(R.id.gameIcon, bitmap);
//            } else {
//                notification.contentView.setImageViewResource(R.id.gameIcon, R.drawable.youle_logo);
//            }
//            notification.contentView.setViewVisibility(R.id.progressBar, View.VISIBLE);
//            notification.contentView.setViewVisibility(R.id.loading_desc, View.VISIBLE);
//            notification.contentView.setViewVisibility(R.id.desc, View.GONE);
//        } else {
//            notification.contentView.setImageViewResource(R.id.gameIcon, R.drawable.youle_logo);
//            notification.contentView.setTextViewText(R.id.gameName, downLoadBeanMap.size() + "个下载任务");
//            notification.contentView.setViewVisibility(R.id.progressBar, View.GONE);
//            notification.contentView.setViewVisibility(R.id.loading_desc, View.GONE);
//            notification.contentView.setTextViewText(R.id.desc, buildDesc());
//            notification.contentView.setViewVisibility(R.id.desc, View.VISIBLE);
//        }
        /*
         * notification .setLatestEventInfo(mContext, "WIFI盒子", "你有 " +
		 * mAllTask.size() + "个应用下载", pendingIntent);
		 */
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // 4.发送通知
        mnotiManager.notify(0, notification);

    }

//    private void updateNotification(GameBean bean) {
//        CharSequence title = "游乐游戏下载";
//        int icon = R.drawable.youle_logo;
//
//        if(notification==null) {
//            notification = new Notification();
//        }
//        notification.icon = icon ;
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;
//        // 1、创建一个自定义的消息布局 view.xml
//        // 2、在程序代码中使用RemoteViews的方法来定义image和text。然后把RemoteViews对象传到contentView字段
//        if(remoteView==null) {
//            remoteView = new RemoteViews(contex.getPackageName(), R.layout.download_notification);
//        }
//        notification.contentView = remoteView;
//        // 3、为Notification的contentIntent字段定义一个Intent(注意，使用自定义View不需要setLatestEventInfo()方法)
//        //这儿点击后简单启动Settings模块
//        Intent intent = new Intent(contex, HomeActivity.class);
//        intent.putExtra(HomeActivity.MANAGE, HomeActivity.MANAGE_VALUE);
//        PendingIntent contentIntent = PendingIntent.getActivity
//                (contex, 0, intent, 0);
//        notification.contentIntent = contentIntent;
//        if(mnotiManager==null) {
//            mnotiManager = (NotificationManager) contex.getSystemService(Context.NOTIFICATION_SERVICE);
//        }
//
//        if (downLoadBeanMap.size() == 1) {
//            notification.contentView.setTextViewText(R.id.gameName, bean.getName());
//            Bitmap bitmap = ImageLoader.getInstance().loadImageSync(bean.getIconUrl());
//            if (bitmap != null) {
//                notification.contentView.setImageViewBitmap(R.id.gameIcon, bitmap);
//            } else {
//                notification.contentView.setImageViewResource(R.id.gameIcon, R.drawable.youle_logo);
//            }
//            notification.contentView.setViewVisibility(R.id.progressBar, View.VISIBLE);
//            notification.contentView.setViewVisibility(R.id.loading_desc, View.VISIBLE);
//            notification.contentView.setProgressBar(R.id.progressBar, bean.getTotalSize().intValue(), bean.getCurrentSize().intValue(), false);
//            notification.contentView.setViewVisibility(R.id.desc, View.GONE);
//            String cusize = android.text.format.Formatter.formatFileSize(YouleAplication.getApplication(), bean.getCurrentSize());
//            String tSize = android.text.format.Formatter.formatFileSize(YouleAplication.getApplication(), bean.getTotalSize());
//            notification.contentView.setTextViewText(R.id.size, cusize + "/" + tSize);
//            int pro = (int) (bean.getCurrentSize() * 100 / bean.getTotalSize());
//            notification.contentView.setTextViewText(R.id.progrees, pro + "%");
//        } else {
//            notification.contentView.setImageViewResource(R.id.gameIcon, R.drawable.youle_logo);
//            notification.contentView.setTextViewText(R.id.gameName, downLoadBeanMap.size() + "个下载任务");
//            notification.contentView.setViewVisibility(R.id.progressBar, View.GONE);
//            notification.contentView.setViewVisibility(R.id.loading_desc, View.GONE);
//            notification.contentView.setTextViewText(R.id.desc, buildDesc());
//            notification.contentView.setViewVisibility(R.id.desc, View.VISIBLE);
//        }
//        mnotiManager.notify(0x12, notification);
//    }

    private String buildDesc() {
        StringBuilder sb = new StringBuilder();
        int downing = 0;
        int done = 0;
        int pause = 0;
        for (String key : downLoadBeanMap.keySet()) {
            GameBean gameBean = downLoadBeanMap.get(key);
            if (gameBean == null || gameBean.getDownloadStatus() == null) continue;
            if (gameBean.getDownloadStatus() == PAUSE) {
                pause += 1;
            }
            if (gameBean.getDownloadStatus() == DOWNLOADING) {
                downing += 1;
            }

            if (gameBean.getDownloadStatus() == SUCCESS) {
                done += 1;
            }
        }
        if (downing > 0) {
            sb.append(downing + "个正在下载中 ");
        }
        if (pause > 0) {
            sb.append(pause + "个已暂停 ");
        }

        if (done > 0) {
            sb.append(done + "个已完成");
        }
        return sb.toString();
    }

    @Override
    public void onSuccess(String url, File file) {
        GameBean bean = downLoadBeanMap.get(url);
        if (bean != null) {
            doanLoadRecoder(DownloadRecordApi.END, bean);
            bean.setDownloadStatus(SUCCESS);
            bean.setDownloadPath(file.getPath());
            DaoManager.getDaoSession().getGameBeanDao().insertOrReplace(bean);
            super.onSuccess(url, file);
            TAIConfig config = TAApplication.getApplication().getPreferenceConfig();
            if (config.getBoolean(SettingActivity.AUTO_INSTALL, true)) {
                installApk(file);
            }
            for (IDownLoadListener listener : listenerSet) {
                listener.onSuccess(url, file);
            }
            bean.setCurrentSize(bean.getTotalSize());
            updateNotification(bean);
        } else {
            for (IDownLoadListener listener : listenerSet) {
                listener.onSuccess(url, file);
            }
        }

    }


    private void installApk(File apk) {
        AppInfoUtils.install(contex, apk);
    }

    @Override
    public void onFailure(String url, String strMsg) {
        super.onFailure(url, strMsg);
        GameBean b = downLoadBeanMap.get(url);
        if (b != null) {
            b.setDownloadStatus(FAIL);
            DaoManager.getDaoSession().getGameBeanDao().insertOrReplace(b);
        }
        for (IDownLoadListener listener : listenerSet) {
            listener.onFailure(url, strMsg);
        }
    }

    @Override
    public void onAdd(String url, Boolean isInterrupt) {
        GameBean b = downLoadBeanMap.get(url);
        if (b != null) {
            for (IDownLoadListener listener : listenerSet) {
                listener.onAdd(url, isInterrupt, b);
            }
        }
    }

    RemoteViews remoteView;
    NotificationManager mnotiManager;
    Notification notification;

    //自定义显示的通知 ，创建RemoteView对象
//    private void showCustomizeNotification(GameBean bean) {
//
//        CharSequence title = "游乐游戏下载";
//        int icon = R.drawable.youle_logo;
//
//        long when = System.currentTimeMillis();
//        noti = new Notification(icon, title, when + 10000);
//        noti.flags |= Notification.FLAG_AUTO_CANCEL;
//        // 1、创建一个自定义的消息布局 view.xml
//        // 2、在程序代码中使用RemoteViews的方法来定义image和text。然后把RemoteViews对象传到contentView字段
//        if(remoteView==null) {
//            remoteView = new RemoteViews(contex.getPackageName(), R.layout.download_notification);
//        }
//        noti.contentView = remoteView;
//        // 3、为Notification的contentIntent字段定义一个Intent(注意，使用自定义View不需要setLatestEventInfo()方法)
//        //这儿点击后简单启动Settings模块
//        Intent intent = new Intent(contex, HomeActivity.class);
//        intent.putExtra(HomeActivity.MANAGE, HomeActivity.MANAGE_VALUE);
//        PendingIntent contentIntent = PendingIntent.getActivity
//                (contex, 0, intent, 0);
//        noti.contentIntent = contentIntent;
//        mnotiManager = (NotificationManager) contex.getSystemService(Context.NOTIFICATION_SERVICE);
//        updateNotification(bean);
//
//    }

    public void unregist(IDownLoadListener listener) {
        listenerSet.remove(listener);
    }

    public void pause(String url) {
        Toast.makeText(contex, "暂停下载", Toast.LENGTH_SHORT).show();
        GameBean b = downLoadBeanMap.get(url);
        b.setDownloadStatus(PAUSE);
        mDownLoadManager.pauseHandler(url);
        for (IDownLoadListener listener : listenerSet) {
            listener.onPause(url);
        }
        updateNotification(b);
    }

    public void continueDown(String url) {
        Toast.makeText(contex, "继续下载", Toast.LENGTH_SHORT).show();
        GameBean b = downLoadBeanMap.get(url);
        b.setDownloadStatus(DOWNLOADING);
        mDownLoadManager.continueHandler(url);
        for (IDownLoadListener listener : listenerSet) {
            listener.onContinue(url);
        }
        updateNotification(b);
    }

    public void deleteDown(String url) {
        mDownLoadManager.deleteHandler(url);
        GameBean b = downLoadBeanMap.get(url);
        if (b == null) return;
        DaoManager.getDaoSession().getGameBeanDao().delete(b);
        downLoadBeanMap.remove(url);
        for (IDownLoadListener listener : listenerSet) {
            listener.onDelete(url);
        }
    }

    public void installedApk(String pkg) {
        GameBean gameBean = getGameBeanByPacg(pkg);
        installApp.add(pkg);
        if (gameBean != null) {
        }

    }

    private GameBean getGameBeanByPacg(String pkg) {
        for (String key : downLoadBeanMap.keySet()) {
            GameBean g = downLoadBeanMap.get(pkg);
            if (g.getPackageName().equals(pkg)) {
                return g;
            }
        }
        return null;
    }
}
