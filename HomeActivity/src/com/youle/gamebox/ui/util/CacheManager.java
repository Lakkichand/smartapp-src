package com.youle.gamebox.ui.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.content.pm.*;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StatFs;


public class CacheManager {

    private PackageManager packageManager;
    private OnActionListener onActionListener = null;

    public interface OnActionListener {
        public void onScanStarted(int appsCount);
        public void onCleanCompleted(long cacheSize);
    }

    private class TaskScan extends
            AsyncTask<Void, Integer, Long> {
        private List<ApplicationInfo> packages;

        @Override
        protected void onPreExecute() {
            packages = packageManager
                    .getInstalledApplications(PackageManager.GET_META_DATA);

            if (onActionListener != null)
                onActionListener.onScanStarted(packages.size());
        }

        @Override
        protected Long doInBackground(Void... params) {
            final long[] size = {0};
            for (ApplicationInfo pkg : packages) {
                invokeMethod("getPackageSizeInfo", pkg.packageName,
                        new IPackageStatsObserver.Stub() {

                            @Override
                            public void onGetStatsCompleted(
                                    PackageStats pStats, boolean succeeded)
                                    throws RemoteException {
                                if (succeeded) {
                                    try {
                                        if (pStats.cacheSize > 0) {
                                            size[0] = size[0] + pStats.cacheSize;
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        });
            }

            return size[0];
        }

        @Override
        protected void onPostExecute(Long aLong) {
            new TaskClean().execute(aLong);
        }
    }

    private class TaskClean extends AsyncTask<Long, Void, Long> {

        private CountDownLatch countDownLatch = new CountDownLatch(1);

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Long doInBackground(Long... params) {
            StatFs stat = new StatFs(Environment.getDataDirectory()
                    .getAbsolutePath());

            invokeMethod(
                    "freeStorageAndNotify",
                    (2 * params[0])
                            + ((long) stat.getFreeBlocks() * (long) stat
                            .getBlockSize()),
                    new IPackageDataObserver.Stub() {
                        @Override
                        public void onRemoveCompleted(String packageName,
                                                      boolean succeeded) throws RemoteException {
                            countDownLatch.countDown();
                        }
                    });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
            }

            return params[0];
        }

        @Override
        protected void onPostExecute(Long result) {
            if (onActionListener != null)
                onActionListener.onCleanCompleted(result);
        }
    }

    public CacheManager(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    private Method getMethod(String methodName) {
        for (Method method : packageManager.getClass().getMethods()) {
            if (method.getName().equals(methodName))
                return method;
        }

        return null;
    }

    private void invokeMethod(String method, Object... args) {
        try {
            getMethod(method).invoke(packageManager, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void cleanAllCache(){
        new TaskScan().execute();
    }

    public void setOnActionListener(OnActionListener listener) {
        onActionListener = listener;
    }
}
