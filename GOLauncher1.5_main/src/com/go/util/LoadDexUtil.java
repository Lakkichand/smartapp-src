package com.go.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import com.jiubang.ggheart.launcher.GOLauncherApp;
import dalvik.system.DexClassLoader;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

/**
 * Dex文件操作工具
 * 付费版的超级主题需要用到
 * @author jiangxuwen
 * @date  [2013-1-16]
 */
public class LoadDexUtil {
	public static final String CLASSES_ZIP_FILE_NAME = "/classes.zip";
	public static final String DEX_PATH_NAME = "/dex";
	private static final String VERSIONCODE = "versioncode";
	private String mDirPathHead;
	private Context mAppContext = null;
	private static LoadDexUtil sLoadDexUtil;
	private DexClassLoader mStaticDexCl = null;
	private Context mThemeContext = null;
	private String mLastPkgName = "";
	
	public LoadDexUtil(Context appContext) {
		mAppContext = appContext;
		mDirPathHead = mAppContext.getFilesDir().getAbsolutePath() + "/";
	}
	
	public static synchronized LoadDexUtil getInstance(Context appContext) {
		if (sLoadDexUtil == null) {
			sLoadDexUtil = new LoadDexUtil(appContext);
		}
		return sLoadDexUtil;
	}
	
	public String getDirPathHead() {
		return mDirPathHead;
	}
	
	private boolean copyDexFromRes(String pkgName, int newVersionCode, int[] dexIdList) {
		SharedPreferences sharePreference = PreferenceManager.getDefaultSharedPreferences(mAppContext);
		int versioncode = sharePreference.getInt(pkgName + VERSIONCODE, 0);
		
		String dirPath = mDirPathHead + pkgName + DEX_PATH_NAME;
//		String dirPath = "/sdcard/api_demo/dex";
		if (versioncode == newVersionCode)
		{
			return true;
		}
		File dir = new File(dirPath);
		if (dir.exists())
		{
			deleteFileDir(dir);
		}
		boolean kdir = dir.mkdirs();
		if (!kdir)
		{
			return false;
		}
		InputStream is = null;
		FileOutputStream os = null;
		Context themeContext = null;
		boolean mergeOK = true;
		try
		{
			themeContext = mAppContext.createPackageContext(pkgName,
					Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
			String path = dirPath + CLASSES_ZIP_FILE_NAME;
			File file = new File(path);
			file.createNewFile();
			os = new FileOutputStream(file);
			final int length = dexIdList.length;
			for (int i = 0; i < length; i++)
			{
				is = themeContext.getResources().openRawResource(dexIdList[i]);
				byte[] datacache = new byte[8 * 1024];
				while (true)
				{
					int len = is.read(datacache);
					if (len <= 0)
					{
						break;
					}
					os.write(datacache, 0, len);
				}
				closeStream(is);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			mergeOK = false;
		}
		closeStream(os);

		if (mergeOK)
		{
			// 保存主题versioncode
			sharePreference.edit().putInt(pkgName + VERSIONCODE, newVersionCode).commit();
		}
		
		return mergeOK;
	}
	
	private void closeStream(Closeable cls) {
		if (cls == null) {
			return;
		}
		try
		{
			cls.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean deleteFileDir(File dir) {
		if (!dir.exists())
		{
			return true;
		}
		if (!dir.isDirectory())
		{
			return false;
		}
		String fileNameList[] = dir.list();
		if (null != fileNameList && 0 != fileNameList.length)
		{
			for (int i = 0; i < fileNameList.length; i++)
			{
				File file = new File(dir.getAbsolutePath() + "/" + fileNameList[i]);
				if (!file.exists())
				{
					continue;
				}
				if (file.isDirectory())
				{
					deleteFileDir(file);
				}
				else
				{
					file.delete();
				}
			}
		}
		return dir.delete();
		
	}
	
	public View createDexAppView(String pkgName, int[] classDexIds, int versionCode, String viewPath) {
		View view = null;
		Context goContext = GOLauncherApp.getContext();
//		LoadDexUtil loadDexUtil = LoadDexUtil.getInstance(goContext);
		if (classDexIds.length == 0) {
			return view;
		}
		if (!copyDexFromRes(pkgName, versionCode, classDexIds)) {
			return view;
		}
		try {
			boolean notSameTheme = true;
			if (notSameTheme = !mLastPkgName.equals(pkgName)) {
				mLastPkgName = pkgName;
			}
			if (mStaticDexCl == null || notSameTheme) {
				ClassLoader loader = ClassLoader.getSystemClassLoader();
//			ClassLoader loader = mActivity.getApplicationContext().getClassLoader();
				final String dirPath = getDirPathHead() + pkgName;
				final String dexPath = getDirPathHead() + pkgName
						+ LoadDexUtil.DEX_PATH_NAME + LoadDexUtil.CLASSES_ZIP_FILE_NAME;
				mStaticDexCl = new DexClassLoader(dexPath, dirPath, null, loader);
			}
			if (mThemeContext == null || notSameTheme) {
				mThemeContext = goContext.createPackageContext(pkgName,
						Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
			}
			Class<?> cls = mStaticDexCl.loadClass(viewPath);
			Constructor<?> constructor = null;
			constructor = cls.getConstructor(Context.class);
			view = (View) constructor.newInstance(mThemeContext);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return view;
	}
	
	public synchronized void cleanUp() {
		if (sLoadDexUtil != null) {
			sLoadDexUtil.mStaticDexCl = null;
			sLoadDexUtil.mThemeContext = null;
			sLoadDexUtil = null;
		}
	} // end cleanUp
}
