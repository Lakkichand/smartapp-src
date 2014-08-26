package com.zhidian.wifibox.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import com.zhidian.wifibox.data.APKInfo;
import com.zhidian.wifibox.listener.ApkScaningCallBackListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.format.Formatter;
import android.util.Log;

/**
 * 获取手机中Apk安装包工具
 * 
 * @author zhaoyl
 * 
 */
public class ApkInfoProvider {

	/**
	 * 获取手机上apk文件信息类，主要是判断是否安装再手机上了，安装的版本比较现有apk版本信息 <a
	 * href="http://my.oschina.net/arthor" target="_blank"
	 * rel="nofollow">@author</a> Dylan
	 */
	private static int INSTALLED = 0; // 表示已经安装，且跟现在这个apk文件是一个版本
	private static int UNINSTALLED = 1; // 表示未安装
	private static int INSTALLED_UPDATE = 2; // 表示已经安装，版本比现在这个版本要低，可以点击按钮更新

	private Context context;
	// private List<APKInfo> APKInfos = new ArrayList<APKInfo>();
	private long totalSize = 0;
	private int total = 0; // 共个数

	public ApkInfoProvider(Context context) {
		super();
		this.context = context;
	}

	// public List<APKInfo> getAPKInfos() {
	// return APKInfos;
	// }
	//
	// public void setAPKInfos(List<APKInfo> APKInfos) {
	// this.APKInfos = APKInfos;
	// }

	public long getTotalSize() {
		return totalSize;

	}

	public int getTotal() {
		return total;

	}

	/**
	 * @param args
	 *            运用递归的思想，递归去找每个目录下面的apk文件
	 */
	@SuppressLint("DefaultLocale")
	public void FindAllAPKFile(File file, ApkScaningCallBackListener listener) {

		// 手机上的文件,目前只判断SD卡上的APK文件
		// file = Environment.getDataDirectory();
		// SD卡上的文件目录
		if (file.isFile()) {
			String name_s = file.getName();
			APKInfo info = new APKInfo();
			String apk_path = null;
			// MimeTypeMap.getSingleton()
			if (name_s.toLowerCase().endsWith(".apk")) {
				apk_path = file.getAbsolutePath();// apk文件的绝对路径
				PackageManager pm = context.getPackageManager();
				PackageInfo packageInfo = pm.getPackageArchiveInfo(apk_path,
						PackageManager.GET_ACTIVITIES);
				if (packageInfo == null) {
					info.setDamage(true);
					info.setPath(apk_path);
					info.setPackname(name_s);
					// APKInfos.add(info);
					total = total + 1;
					listener.callback(info);
					return;
				}

				ApplicationInfo appInfo = packageInfo.applicationInfo;

				/** 获取apk的图标 */
				appInfo.sourceDir = apk_path;
				appInfo.publicSourceDir = apk_path;
				Drawable apk_icon = appInfo.loadIcon(pm);
				info.setIcon(apk_icon);
				/** 得到应用名称 */
				String name = pm.getApplicationLabel(appInfo).toString();
				info.setAppname(name);

				/** 得到包名 */
				String packageName = packageInfo.packageName;
				info.setPackname(packageName);

				/** apk的绝对路径 */
				info.setPath(file.getAbsolutePath());
				/** apk的版本名称 String */
				 String versionName = packageInfo.versionName;
				 info.setVersionName(versionName);
				/** apk的版本号码 int */
				int versionCode = packageInfo.versionCode;
				// info.setVersionCode(versionCode);
				/** 安装处理类型 */
				int type = doType(pm, packageName, versionCode);
				info.setIsInstall(type);

				/** 获取安装包的大小 */
				// File f = new File(apk_path);
				try {
					long size = getFileSizes(file);
					totalSize = totalSize + size;
					// String apkSize = formatSize(size);
					info.setSize(size);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Log.i("ok", "处理类型:" + String.valueOf(type) + "\n"
						+ "------------------我是纯洁的分割线-------------------");
				// APKInfos.add(info);
				total = total + 1;
				listener.callback(info);
			}
			// String apk_app = name_s.substring(name_s.lastIndexOf("."));
		} else {
			File[] files = file.listFiles();
			if (files != null && files.length > 0) {
				for (File file_str : files) {
					listener.nowScaning(file_str.getAbsolutePath());
					FindAllAPKFile(file_str, listener);
				}

			}
		}
	}

	/*
	 * 判断该应用是否在手机上已经安装过，有以下集中情况出现 1.未安装，这个时候按钮应该是“安装”点击按钮进行安装 2.已安装，按钮显示“已安装”
	 * 可以卸载该应用 3.已安装，但是版本有更新，按钮显示“更新” 点击按钮就安装应用
	 */

	/**
	 * 判断该应用在手机中的安装情况
	 * 
	 * @param pm
	 *            PackageManager
	 * @param packageName
	 *            要判断应用的包名
	 * @param versionCode
	 *            要判断应用的版本号
	 */
	public static int doType(PackageManager pm, String packageName, int versionCode) {
		List<PackageInfo> pakageinfos = pm
				.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
		for (PackageInfo pi : pakageinfos) {
			String pi_packageName = pi.packageName;
			int pi_versionCode = pi.versionCode;
			// 如果这个包名在系统已经安装过的应用中存在
			if (packageName.equals(pi_packageName)) {
				// Log.i("test","此应用安装过了");
				if (versionCode == pi_versionCode) {
					Log.i("test", "已经安装，不用更新，可以卸载该应用");
					return INSTALLED;
				} else if (versionCode > pi_versionCode) {
					Log.i("test", "已经安装，有更新");
					return INSTALLED_UPDATE;
				}
			}
		}
		Log.i("test", "未安装该应用，可以安装");
		return UNINSTALLED;
	}

	/*** 获取文件大小 ***/
	@SuppressWarnings("resource")
	public static long getFileSizes(File f) throws Exception {
		long s = 0;
		if (f.exists()) {
			FileInputStream fis = null;
			fis = new FileInputStream(f);
			s = fis.available();
		} else {
			f.createNewFile();
			System.out.println("文件不存在");
		}
		return s;
	}

}
