package com.youle.gamebox.ui.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;
import com.youle.gamebox.ui.bean.AppInfoBean;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Administrator on 2014/5/4.
 */
public class AppInfoUtils {

    /**
     * get phone appinfo
     */
    public static List<AppInfoBean> getPhoneAppInfo(Context context) {
        List<AppInfoBean> appInfos = new ArrayList<AppInfoBean>();
        PackageManager pManager = context.getPackageManager();
        List<PackageInfo> packList = pManager.getInstalledPackages(0);
        for (int i = 0; i < packList.size(); i++) {
            PackageInfo packageInfo = (PackageInfo) packList.get(i);
            // 判断是否为非系统预装的应用程序
            if ((packageInfo.applicationInfo.flags &
                    packageInfo.applicationInfo.FLAG_SYSTEM) <= 0) {
                AppInfoBean appInfo = new AppInfoBean();
                appInfo.setPackageName(packageInfo.packageName);
                appInfo.setName(packageInfo.applicationInfo.loadLabel(
                        pManager).toString());
                appInfo.setVersion(String.valueOf(packageInfo.versionName));
                appInfo.setVersionCode(String.valueOf(packageInfo.versionCode));
                String fileDir = packageInfo.applicationInfo.publicSourceDir;
                File file = new File(fileDir);
                long size = file.length() / 1024 / 1024;
                appInfo.setSize(size+"");
                appInfos.add(appInfo);
            }
        }
        return appInfos;
    }

    public static  boolean isInstall(Context context, String packageName){
       List<AppInfoBean>  installList = getPhoneAppInfo(context);
        for(AppInfoBean b:installList){
            if(b.getPackageName().equals(packageName)) return  true;
        }
        return  false;
    }

    public static String getInstalledPackage(Context context){
        List<AppInfoBean> apps= AppInfoUtils.getPhoneAppInfo(context) ;
        StringBuilder sb = new StringBuilder();
        for (AppInfoBean b:apps){
            sb.append(b.getPackageName()).append(",");
        }
        return  sb.toString();
    }

    public static  String getPkgAndVersion(Context context){
        StringBuilder sb = new StringBuilder();
        List<AppInfoBean> apps= AppInfoUtils.getPhoneAppInfo(context) ;
        for (AppInfoBean b:apps){
            sb.append(b.getPackageName()+"|"+b.getVersionCode()).append(",");
        }
        return  sb.toString();
    }
    public static String getPhoneAppInfoPack(List<AppInfoBean> appInfos) {
        StringBuffer sbPackStr = new StringBuffer();
        String packages = "";
        if (appInfos.size() > 0) {
            for (int i = 0; i < appInfos.size(); i++) {
                AppInfoBean infoBean = appInfos.get(i);
                sbPackStr.append(infoBean.getPackageName());
                sbPackStr.append(",");
            }
            packages = new String(sbPackStr);
            packages = packages.substring(0, packages.length() - 1);
        }
        return packages;
    }


    // install apk
    public static String install(Context context, File file) {
        if (file.exists()) {
            String apkPath = file.getAbsolutePath();
            if (file.isFile() && apkPath.endsWith(".apk")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file),
                        "application/vnd.android.package-archive");
                context.startActivity(intent);
                return "";
            } else {
                return "不是有效的apk安装文件,安装失败。";
            }
        } else {
            return "文件不存在,安装失败。";
        }

    }


    // uninstall apk
    public static void uninstall(Context context, String packsName) {
        Uri packageURI = Uri.parse("package:" + packsName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        context.startActivity(uninstallIntent);
    }

    /*
     * 启动一个app
     */
    public static void startAPP(Context context,String appPackageName){
        try{
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
            context.startActivity(intent);
        }catch(Exception e){
            Toast.makeText(context, "没有安装", Toast.LENGTH_LONG).show();
        }
    }


    // run phone app
    public static Intent runApp(Context context, File f) {

        if (f == null) {
            return null;
        }
        if (!f.exists()) {
            return null;
        }
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        /* 调用getMIMEType()来取得MimeType */
        String type = "";
        if (f.getName().endsWith("apk")) {
            type = "application/vnd.android.package-archive";
			/* 设置intent的file与MimeType */
            intent.setDataAndType(Uri.fromFile(f), type);
            //context.startActivity(intent);
            //LogUtil.d(TAG, "install apk...");
            return intent;
        } else {
            return null;
        }

    }


}
