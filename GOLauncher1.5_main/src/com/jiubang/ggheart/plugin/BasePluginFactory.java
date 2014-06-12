package com.jiubang.ggheart.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.util.Log;

import com.go.util.device.Machine;
import com.jiubang.ggheart.apps.desks.diy.GoLauncher;

import dalvik.system.DexClassLoader;

/**
 * 插件包工厂基类，用于对插件进行动态类加载
 * @author yangguanxiang
 *
 */
public abstract class BasePluginFactory {
	private static final String APP_CACHE = "cache";
	private static final String PLUGIN_DEX_FOLDER = "plugin_dex";
	private static final String DEX_ZIP = "dex.zip";
	private static String sDexOutputDir;

	protected static ClassLoader createDexClassLoader(Context context, Context remoteContext,
			String pluginPackageName, String[] zipFileNames) {
		ClassLoader loader = null;
		try {
			String dexPath = getDexPath(context, remoteContext, pluginPackageName, zipFileNames);
			String dexOutputDir = getDexOutputDir(context, pluginPackageName);
			Log.i("Test", "-----------dexPath: " + dexPath);
			loader = new DexClassLoader(dexPath, dexOutputDir, null, context.getClassLoader());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return loader;
	}

	private static String getDexPath(Context context, Context remoteContext,
			String pluginPackageName, String[] zipFileNames) throws NameNotFoundException {
		if (Machine.IS_JELLY_BEAN && zipFileNames != null && zipFileNames.length > 0) {
			int versionCode = context.getPackageManager().getPackageInfo(pluginPackageName, 0).versionCode;
			String destPath = getDexOutputDir(context, pluginPackageName) + "/" + versionCode + "/"
					+ DEX_ZIP;
			try {
				int[] zipResIds = getZipResIds(remoteContext, pluginPackageName, zipFileNames);
				combineZipFile(remoteContext, zipResIds, destPath);
				return destPath;
			} catch (Exception e) {
				e.printStackTrace();
				File file = new File(destPath);
				file.delete();
			}
		}
		return context.getPackageManager().getPackageInfo(pluginPackageName, 0).applicationInfo.sourceDir;
	}

	private static void combineZipFile(Context remoteContext, int[] zipResIds, String destPath)
			throws IOException {
		File file = new File(destPath);
		if (!file.exists()) {
			File parent = new File(file.getParent());
			parent.mkdirs();
			OutputStream os = null;
			try {
				os = new FileOutputStream(file);
				Resources res = remoteContext.getResources();
				for (int id : zipResIds) {
					InputStream is = null;
					try {
						is = res.openRawResource(id);
						byte[] cache = new byte[1024];
						int len = 0;
						while ((len = is.read(cache)) > 0) {
							os.write(cache, 0, len);
						}
					} finally {
						if (is != null) {
							is.close();
						}
					}
				}
			} finally {
				if (os != null) {
					os.flush();
					os.close();
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected static Class getPluginAdminClass(Context context, String className,
			ClassLoader dexLoader) {
		Class clazz = null;
		try {
			if (dexLoader != null) {
				clazz = dexLoader.loadClass(className);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return clazz;
	}

	protected static String getDexOutputDir(Context context, String packageName) {
		if (sDexOutputDir == null) {
			String parentDir = context.getDir(APP_CACHE, Context.MODE_PRIVATE).getAbsolutePath();
			File file = new File(parentDir + "/" + PLUGIN_DEX_FOLDER + "/" + packageName);
			if (!file.exists()) {
				file.mkdirs();
			}
			sDexOutputDir = file.getAbsolutePath();
		}
		return sDexOutputDir;
	}

	/**
	 * 更新卸载时删除.dex文件
	 */
	public static void deleteDexFile(String pluginPackageName) {
		try {
			File file = new File(getDexOutputDir(GoLauncher.getContext(), pluginPackageName));
			if (!file.exists()) {
				return;
			}
			if (file.isDirectory()) {
				File[] childFiles = file.listFiles();
				if (childFiles == null || childFiles.length == 0) {
					return;
				}
				// 删除该文件夹下的所有文件，不删除文件夹
				for (int i = 0; i < childFiles.length; i++) {
					childFiles[i].delete();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	protected static Context getRemoteContext(Context context, String remotePackgeName) {
		Context remoteContext = null;
		try {
			remoteContext = context.createPackageContext(remotePackgeName,
					Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return remoteContext;
	}

	protected static int[] getZipResIds(Context remoteContext, String pluginPackageName,
			String[] zipResName) {
		Resources res = remoteContext.getResources();
		int[] dexZipIds = new int[zipResName.length];
		for (int i = 0; i < zipResName.length; i++) {
			dexZipIds[i] = res.getIdentifier(zipResName[i], "raw", pluginPackageName);
		}
		return dexZipIds;
	}
}
