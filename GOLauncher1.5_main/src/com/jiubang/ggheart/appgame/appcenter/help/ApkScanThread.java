/**
 * 
 */
package com.jiubang.ggheart.appgame.appcenter.help;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.jiubang.ggheart.appgame.appcenter.bean.AppPackageInfoBean;
import com.jiubang.ggheart.apps.gowidget.gostore.util.GoStoreAppInforUtil;

/**
 * Apk安装包扫描线程
 * @author liguoliang
 *
 */
public class ApkScanThread extends Thread {

	/**
	 * 扫描监听
	 * @author liguoliang
	 *
	 */
	public static interface IApkScanListener {
		void onStart();

		/**
		 * @param bean
		 * @param file	当前扫描文件
		 */
		void onProgress(AppPackageInfoBean bean, File file);

		void onFinish();
	}

	public final static String SDCARD = Environment.getExternalStorageDirectory().toString();

	// 下载文件保存目录
	public final static String DOWNLOAD_DIRECTORY_PATH = SDCARD + "/GoStore/download/";

	/**
	 * 是否终止扫描
	 */
	private boolean mIsTerminate = false;

	private static final String APK_SUFFIX = ".apk";

	private Context mContext;

	private PackageManager mPm;

	private IApkScanListener mListener;

	public ApkScanThread(Context context) {
		super();
		mContext = context;
		mPm = mContext.getPackageManager();
	}

	public void setListener(IApkScanListener listener) {
		mListener = listener;
	}

	@Override
	public void run() {
		if (mListener != null) {
			mListener.onStart();
		}
		File downloadFile = new File(DOWNLOAD_DIRECTORY_PATH);
		// 优先扫描下载目录
		scanApkFromSDCard(downloadFile, null);

		// 然后扫描SD卡，忽略downloadFile
		File sdFile = new File(SDCARD);
		scanApkFromSDCard(sdFile, downloadFile);

		if (mListener != null) {
			mListener.onFinish();
		}
	}
	
	public void stopScan() {
		mIsTerminate = true;
	}

	/**
	 * 扫描SD卡中的APK安装包
	 * @param file
	 * @param ignoreFile	忽略扫描的文件
	 */
	private void scanApkFromSDCard(File file, File ignoreFile) {
		if (ignoreFile != null && file.getAbsolutePath().equals(ignoreFile.getAbsolutePath())) {
			// 判断是否忽略不扫描
			return;
		}
		if (file.isFile()) {
			// 如果是文件
			String fileName = file.getAbsolutePath();
			if (!fileName.toLowerCase().endsWith(APK_SUFFIX)) {
				return;
			}
			PackageInfo packageInfo = mPm.getPackageArchiveInfo(fileName,
					PackageManager.GET_ACTIVITIES);
			if (packageInfo == null) {
				return;
			}
			ApplicationInfo appInfo = packageInfo.applicationInfo;
			//			AppPackageInfoBean infoBean = new AppPackageInfoBean();
			//			// 应用程序ICON
			//			infoBean.mIcon = appInfo.loadIcon(mPm);
			//			// 应用程序名
			//			infoBean.mName = appInfo.loadLabel(mPm).toString();
			//			
			//			// 应用程序版本名
			//			infoBean.mVersionName = packageInfo.versionName;
			//			// 应用程序版本号
			//			int versionCode = packageInfo.versionCode;
			//			infoBean.mVersionCode = versionCode;
			//			// 应用程序包名
			//			String packageName = packageInfo.packageName;
			//			infoBean.mPackageName = packageName; 

			// TODO:LIGUOLIANG 此处代码重复了，需要修改
			AppPackageInfoBean infoBean = getApkFileInfo(mContext, file.getAbsolutePath());
			
			if (infoBean != null) {
				int state = getPackageState(mContext, infoBean.mPackageName, infoBean.mVersionCode);
				infoBean.mState = state;
				infoBean.mFilePath = fileName;
				if (mListener != null) {
					mListener.onProgress(infoBean, file);
				}
			}			

			
		} else {
			// 如果是目录,则遍历目录文件
			File[] files = file.listFiles();
			if (files != null && files.length > 0) {
				for (File tempFile : files) {
					if (mIsTerminate) {
						break;
					}
					scanApkFromSDCard(tempFile, ignoreFile);
				}
			}
		}
	}

	/**
	 * 检测是否有更新的方法
	 * 
	 * @param context
	 *            应用上下文
	 * @param packageName
	 *            待检测应用的包名
	 * @param versionCode
	 *            最新的版本,要大于等于0
	 * @return 如果给的版本号高于本机已经安装的版本，就返回TRUE；否则返回FALSE；
	 */
	public static int getPackageState(Context context, String packageName, int versionCode) {
		if (context == null || TextUtils.isEmpty(packageName) || versionCode < 0) {
			return -1;
		}
		int ret = AppPackageInfoBean.STATE_INSTALLED;
		if (GoStoreAppInforUtil.isApplicationExsit(context, packageName)) {
			// 如果应用程序已安装
			PackageManager packageManager = context.getPackageManager();
			if (packageManager != null) {
				try {
					PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
					int localVersionCode = 0;
					if (packageInfo != null) {
						localVersionCode = packageInfo.versionCode;
						if (versionCode > localVersionCode) {
							ret = AppPackageInfoBean.STATE_UPDATE;
						} else if (versionCode < localVersionCode) {
							ret = AppPackageInfoBean.STATE_VERSION_LOWER;
						} else {
							ret = AppPackageInfoBean.STATE_INSTALLED;
						}
					}
				} catch (NameNotFoundException e) {

				}
			}
		} else {
			ret = AppPackageInfoBean.STATE_INSTALL;
		}
		return ret;
	}

	/**  
	 * 获取未安装的apk信息  
	 *   
	 * @param ctx Context 
	 * @param apkPath apk路径，可以放在SD卡 
	 * @return  
	 */
	public static AppPackageInfoBean getApkFileInfo(Context ctx, String apkPath) {
		File apkFile = new File(apkPath);
		if (!apkFile.exists() || !apkPath.toLowerCase().endsWith(".apk")) {
			System.out.println("file path is not correct");
			return null;
		}

		AppPackageInfoBean infoBean = new AppPackageInfoBean();
		String packageParser = "android.content.pm.PackageParser";
		String assetManager = "android.content.res.AssetManager";
		try {
			//反射得到pkgParserCls对象并实例化,有参数    
			Class<?> pkgParserCls = Class.forName(packageParser);
			Class<?>[] typeArgs = { String.class };
			Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = { apkPath };
			Object pkgParser = pkgParserCt.newInstance(valueArgs);

			//从pkgParserCls类得到parsePackage方法    
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults(); //这个是与显示有关的, 这边使用默认    
			typeArgs = new Class<?>[] { File.class, String.class, DisplayMetrics.class, int.class };
			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage",
					typeArgs);

			valueArgs = new Object[] { new File(apkPath), apkPath, metrics, 0 };

			//执行pkgParser_parsePackageMtd方法并返回    
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);

			//从返回的对象得到名为"applicationInfo"的字段对象     
			if (pkgParserPkg == null) {
				return null;
			}
			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");

			//从对象"pkgParserPkg"得到字段"appInfoFld"的值    
			if (appInfoFld.get(pkgParserPkg) == null) {
				return null;
			}
			ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);

			//反射得到assetMagCls对象并实例化,无参    
			Class<?> assetMagCls = Class.forName(assetManager);
			Object assetMag = assetMagCls.newInstance();
			//从assetMagCls类得到addAssetPath方法    
			typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath",
					typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			//执行assetMag_addAssetPathMtd方法    
			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);

			//得到Resources对象并实例化,有参数    
			Resources res = ctx.getResources();
			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();
			Constructor<Resources> resCt = Resources.class.getConstructor(typeArgs);
			valueArgs = new Object[3];
			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();
			//这个是重点  
			//得到Resource对象后可以有很多用处  
			res = (Resources) resCt.newInstance(valueArgs);

			// 读取apk文件的信息    

			if (info != null) {
				if (info.icon != 0) {
					// 图片存在，则读取相关信息    
					Drawable icon = res.getDrawable(info.icon); // 图标    
					infoBean.mIcon = icon;
				}
				if (info.labelRes != 0) {
					String name = (String) res.getText(info.labelRes); // 名字    
					infoBean.mName = name;
				} else {
					String apkName = apkFile.getName();
					infoBean.mName = apkName;
				}
				String pkgName = info.packageName; // 包名       
				infoBean.mPackageName = pkgName;
			} else {
				return null;
			}
			PackageManager pm = ctx.getPackageManager();
			PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath,
					PackageManager.GET_ACTIVITIES);
			if (packageInfo != null) {
				infoBean.mVersionName = packageInfo.versionName;
				infoBean.mVersionCode = packageInfo.versionCode;
			}
			infoBean.mSize = sizeToStr(apkFile.length());
			return infoBean;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static final String KB = "KB";
	private static final String MB = "MB";
	public static String sizeToStr(long size) {
		String strSize = "";
		// 保留两位小数
		DecimalFormat format = new DecimalFormat("#####0.00");
		if (size < 1024 * 1024) {
			// 
			double ret = (double) size / 1024;
			strSize = format.format(ret) + KB;
		} else {
			double ret = (double) size / 1024 / 1024;
			strSize = format.format(ret) + MB;
		}
		return strSize;
	}
}
