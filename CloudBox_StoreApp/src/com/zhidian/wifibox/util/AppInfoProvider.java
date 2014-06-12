package com.zhidian.wifibox.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import com.zhidian.wifibox.data.AppInfo;
import com.zhidian.wifibox.listener.AppsizeListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.os.RemoteException;
import android.util.Log;

/**
 * 获取手机中已安装App工具
 * 
 * @author zhaoyl
 * 
 */
public class AppInfoProvider {

	private PackageManager packmanager;
	private Context mContext;

	public AppInfoProvider(Context context) {
		packmanager = context.getPackageManager();
		mContext = context;
	}

	/**
	 * 获取手机中所有已安装的app
	 * 
	 * @return
	 */
	@SuppressLint("NewApi")
	public List<AppInfo> getAllApps() {
		List<AppInfo> appinfos = new ArrayList<AppInfo>();
		List<PackageInfo> packinfos = packmanager
				.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		 List<String> disableList = filterDisabled(); //获得已被停用的系统应用
		for (PackageInfo info : packinfos) {
			AppInfo myApp = new AppInfo();
			String packname = info.packageName;
			myApp.setPackname(packname);
			ApplicationInfo appinfo = info.applicationInfo;
//			Drawable icon = appinfo.loadIcon(packmanager);
//			myApp.setIcon(icon);
			String appname = appinfo.loadLabel(packmanager).toString();
			myApp.setAppname(appname);
			

			if (filterApp(appinfo)) {//非系统应用
				myApp.setSystemApp(false);
				appinfos.add(myApp);
			} else {
				if (null != disableList && disableList.size() > 0) {
					if(!disableList.contains(packname)){
						myApp.setSystemApp(true);
						appinfos.add(myApp);
					}
					
				}else {
					myApp.setSystemApp(true);
					appinfos.add(myApp);
				}
				
			}
			
		}
		return appinfos;
	}

	/**
	 * 判断是否为系统应用
	 * 
	 * @param appinfo
	 * @return
	 */
	public static boolean filterApp(ApplicationInfo appinfo) {
		if ((appinfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			return true;
		} else if ((appinfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
			return true;
		}
		return false;

	}

	/**
	 * 判断此应用是否能打开
	 * 
	 * @param packname
	 * @return
	 */
	public boolean filterOpen(String packname) {
		try {
			PackageInfo info = mContext.getPackageManager().getPackageInfo(
					packname,
					PackageManager.GET_UNINSTALLED_PACKAGES
							| PackageManager.GET_ACTIVITIES);
			ActivityInfo[] activityinfos = info.activities;
			if (activityinfos == null) {
				return false;
			}
			if (activityinfos.length > 0) {
				ActivityInfo startActivity = activityinfos[0];
				String s = startActivity.name;
				if (s == null || s.equals("")) {
					Log.d("没有", "无");
					return false;
				}
				return true;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return false;

	}

	/**
	 * 获取安装的应用的大小
	 * 
	 * @param pkgName
	 * @param backListener
	 * @throws Exception
	 */
	public void queryPacakgeSize(String pkgName, AppsizeListener backListener)
			throws Exception {
		if (pkgName != null) {
			// 使用放射机制得到PackageManager类的隐藏函数getPackageSizeInfo
			PackageManager pm = mContext.getPackageManager(); // 得到pm对象
			try {

				int version = CheckSDKVersion.check();
				Log.d("AppInfoProvider", "系统版本号:" + version);
				// 判断手机系统是否为4.2及以上。
				if (version < 17) {// 4.2以下
					// 通过反射机制获得该隐藏函数
					Method getPackageSizeInfo = pm.getClass()
							.getDeclaredMethod("getPackageSizeInfo",
									String.class, IPackageStatsObserver.class);

					// 调用该函数，并且给其分配参数 ，待调用流程完成后会回调PkgSizeObserver类的函数
					getPackageSizeInfo.invoke(pm, pkgName, new PkgSizeObserver(
							backListener, pkgName));
				} else {
					Method getPackageSizeInfo = pm.getClass()
							.getDeclaredMethod("getPackageSizeInfo",
									String.class, int.class,
									IPackageStatsObserver.class);
					getPackageSizeInfo.invoke(pm, pkgName,
							android.os.Process.myUid() / 100000,
							new PkgSizeObserver(backListener, pkgName));
				}

			} catch (Exception ex) {
				Log.e("AppInfoProvider", "NoSuchMethodException");
				ex.printStackTrace();
				throw ex; // 抛出异常
			}
		}
	}

	// aidl文件形成的Bindler机制服务类
	public class PkgSizeObserver extends IPackageStatsObserver.Stub {
		/***
		 * 回调函数，
		 * 
		 * @param pStatus
		 *            ,返回数据封装在PackageStats对象中
		 * @param succeeded
		 *            代表回调成功
		 */
		private long totalsize;
		private String packname;
		private AppsizeListener backListener;

		public PkgSizeObserver(AppsizeListener backListener, String packname) {
			// TODO Auto-generated constructor stub
			this.backListener = backListener;
			this.packname = packname;
		}

		@Override
		public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
				throws RemoteException {
			// TODO Auto-generated method stub
			long cachesize = pStats.cacheSize; // 缓存大小
			long datasize = pStats.dataSize; // 数据大小
			long codesize = pStats.codeSize; // 应用程序大小
			totalsize = cachesize + datasize + codesize;
			backListener.BackCall(totalsize, packname);
			Log.i("AppInfoProvider", "cachesize--->" + cachesize
					+ " datasize---->" + datasize + " codeSize---->" + codesize);
		}
	}

	/**
	 * 获取应用大小成功后的回调接口
	 */
	public static interface AsyncAppSizeLoadedCallBack {
		public void SizeLoaded(long size);
	}

	/**
	 * 获得被冻结的系统应用
	 */
	public static List<String> filterDisabled() {
		List<String> list = new ArrayList<String>();
		try {			
			Process su = Runtime.getRuntime().exec("pm list packages -d");
			BufferedReader br = new BufferedReader(new InputStreamReader(
					su.getInputStream()));
			String packageName = br.readLine();
			
			if (packageName != null) {
				packageName = packageName.substring(packageName.indexOf(':') + 1);
				list.add(packageName);
			}
			while (null != packageName) {
				packageName = br.readLine();
				
				if (packageName != null) {
					packageName = packageName.substring(packageName.indexOf(':') + 1);
					list.add(packageName);
				}
			}
			
			br.close();
			su.waitFor();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return list;

	}
	
	/**
	 * 获取所有已被冻结的系统应用的信息
	 * @return
	 */
	public List<AppInfo> getDisableInfo(){
		List<AppInfo> infolist = new ArrayList<AppInfo>();
		AppInfo appinfo;
		ApplicationInfo applicationInfo;
		List<String> list = filterDisabled();
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				appinfo = new AppInfo();
				String packname = list.get(i);
				applicationInfo = AppUtils.getAppInfo(mContext, packname);
				String appName = (String) applicationInfo.loadLabel(packmanager);
				String sourceDir = applicationInfo.sourceDir;
				String dataDir = applicationInfo.dataDir;
				appinfo.setAppname(appName);
				appinfo.setPackname(packname);
				appinfo.setDataDir(dataDir);
				
				//sourceDir = sourceDir.substring(sourceDir.lastIndexOf("/") + 1);
				appinfo.setSourceDir(sourceDir);
				infolist.add(appinfo);
			}
			
		}		 		
		
		return infolist;
		
	}
	

}
