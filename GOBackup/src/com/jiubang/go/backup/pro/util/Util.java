package com.jiubang.go.backup.pro.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.jiubang.go.backup.pro.PreferenceManager;
import com.jiubang.go.backup.pro.data.UserDictionaryBackupEntry;
import com.jiubang.go.backup.pro.model.Constant;
import com.jiubang.go.backup.pro.model.IAsyncTaskListener;
import com.jiubang.go.backup.pro.model.RootShell;
import com.jiubang.go.backup.pro.statistics.GOLauncherStatisticsProviderConstants;

/**
 * 工具类
 *
 * @author GoBackup Dev Team
 */
public class Util {
	private static final String LOG_TAG = "Util";

	public static final boolean IS_DEBUG = false;

	private static Method sAcceleratedMethod = null;

	public static final int LAYER_TYPE_NONE = 0;
	public static final int LAYER_TYPE_SOFTWARE = 1;
	public static final int LAYER_TYPE_HARDWARE = 2;

	private static final int M1024 = 1024;
	private static final int M8192 = 8192;
	private static final int M4096 = 4096;
	private static final int M0 = 0;
	private static final int M3 = 3;
	private static final int M4 = 4;
	private static final int M5 = 5;
	private static final int M10 = 10;
	private static final int M100 = 100;
	private static final int M11 = 11;
	// 版本小于3.0
	public static boolean sLevelUnder3 = Build.VERSION.SDK_INT < M11;

	private static final String PACKAGE_PARSER_PATH = "android.content.pm.PackageParser";
	private static final String ASSET_MANAGER_PATH = "android.content.res.AssetManager";

	public static String formatFileSize(long size) {
		String result = "";
		if (size >= M1024 * M1024 * M1024) {
			// 保留一位小数
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(1);
			result = nf.format((double) size / (double) (M1024 * M1024 * M1024)) + "GB";
		} else if (size >= M1024 * M1024) {
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMaximumFractionDigits(1);
			result = nf.format((double) size / (double) (M1024 * M1024)) + "MB";
		} else if (size >= M1024) {
			size /= M1024;
			result = String.valueOf(size) + "KB";
		} else {
			result = String.valueOf(size) + "Bytes";
		}
		return result;
	}

	/**
	 * 复制单个文件
	 *
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf.txt
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf.txt
	 * @return boolean
	 */
	public static boolean copyFile(String oldPath, String newPath) {
		boolean ret = false;
		InputStream inStream = null;
		FileOutputStream fs = null;

		try {
			int byteread = 0;
			File oldfile = new File(oldPath);
			if (oldfile.exists()) { // 文件存在时
				// 判断目的目录是否存在
				File newFile = new File(newPath);
				if (!newFile.getParentFile().exists()) {
					newFile.getParentFile().mkdirs();
				}

				inStream = new FileInputStream(oldPath); // 读入原文件
				fs = new FileOutputStream(newPath);
				byte[] buffer = new byte[M8192];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
				}
				ret = true;
				// inStream.close();
				// fs.close();
				// inStream = null;
				// fs = null;
			}
		} catch (Exception e) {
			System.out.println("复制单个文件操作出错");
			e.printStackTrace();
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (fs != null) {
				try {
					fs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	/**
	 * 复制整个文件夹内容
	 *
	 * @param oldPath
	 *            String 原文件路径 如：c:/fqf
	 * @param newPath
	 *            String 复制后路径 如：f:/fqf/ff
	 * @return boolean
	 */
	public static boolean copyFolder(String oldPath, String newPath) {
		boolean ret = true;
		try {
			(new File(newPath)).mkdirs(); // 如果文件夹不存在 则建立新文件夹
			File a = new File(oldPath);
			String[] file = a.list();
			File temp = null;
			for (int i = 0; i < file.length; i++) {
				if (oldPath.endsWith(File.separator)) {
					temp = new File(oldPath + file[i]);
				} else {
					temp = new File(oldPath + File.separator + file[i]);
				}

				if (temp.isFile()) {
					FileInputStream input = new FileInputStream(temp);
					FileOutputStream output = new FileOutputStream(newPath + "/"
							+ (temp.getName()).toString());
					byte[] b = new byte[M1024 * M5];
					int len;
					while ((len = input.read(b)) != -1) {
						output.write(b, 0, len);
					}
					output.flush();
					output.close();
					input.close();
				}
				if (temp.isDirectory()) {
					// 如果是子文件夹
					ret = copyFolder(oldPath + "/" + file[i], newPath + "/" + file[i]);
				}
			}
		} catch (Exception e) {
			System.out.println("复制整个文件夹内容操作出错");
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	public static void deleteFile(String filePath) {
		if (filePath == null) {
			return;
		}
		File file = new File(filePath);
		if (!file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			File[] subFiles = file.listFiles();
			if (subFiles != null && subFiles.length > 0) {
				for (File subFile : subFiles) {
					deleteFile(subFile.getAbsolutePath());
				}
			}
		}
		file.delete();
	}

	public static byte[] bitmapToByteArray(Bitmap bmp) {
		if (bmp == null) {
			return null;
		}

		byte[] buffers = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			try {
				Bitmap.CompressFormat localCompressFormat = Bitmap.CompressFormat.PNG;
				bmp.compress(localCompressFormat, 100, bos);
				buffers = bos.toByteArray();
			} finally {
				bos.close();
			}
		} catch (Exception e) {
			//			Log.d(LOG_TAG, "bitmapToByteArray : ERROR : " + e.getMessage());
			return null;
		}
		return buffers;
	}

	public static Bitmap byteArrayToBitmap(byte[] bitmapData) {
		if (bitmapData == null) {
			return null;
		}
		Bitmap bm = null;
		try {
			bm = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bm;
	}

	public static long getFileSize(String fileName) {
		if (fileName == null) {
			return 0;
		}
		File file = new File(fileName);
		if (!file.exists()) {
			return 0;
		}
		long size = 0;
		if (file.isDirectory()) {
			long subSize = 0;
			File[] subFiles = file.listFiles();
			if (subFiles != null && subFiles.length != 0) {
				for (File subFile : subFiles) {
					subSize += getFileSize(subFile.getAbsolutePath());
				}
			}
			size += subSize;
		} else {
			size = file.length();
		}
		return size;
	}

	public static String[] getSystemPath() {
		String[] result = null;
		String path = System.getenv("PATH");
		if (path == null) {
			return null;
		}

		if (path.endsWith("\n")) {
			path = path.substring(0, path.length() - 2);
		}
		result = path.split(":");
		return result;
	}

	public static boolean findLinuxCmd(final String cmd) {
		boolean ret = false;
		// LinuxShell ls = new LinuxShell();
		// String[] paths = ls.querySystemPath(); //系统path
		String[] paths = getSystemPath();
		if (paths == null) {
			// Error
			Log.d("GOBackup", "Util : findLinuxCmd : paths == null");
			return false;
		}

		for (String path : paths) {
			if (!path.endsWith(File.separator)) {
				path += File.separator;
			}
			File desFile = new File(path + cmd);
			if (desFile.exists()) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	/**
	 * 不弹框 这是一个较耗时的接口，注意不要在循环中使用
	 *
	 * @param context
	 * @return
	 */
	public static boolean isRootRom(Context context) {
		return findLinuxCmd("su");
	}

	/**
	 * 会弹一次框
	 *
	 * @param context
	 * @return
	 */
	public static boolean isSuCmdValid(Context context) {
		if (context == null) {
			return false;
		}
		if (findLinuxCmd("su")) {
			LinuxShell ls = new LinuxShell();
			if (!ls.checkSupportSu(context.getFilesDir().getAbsolutePath())) {
				ls = null;
				return false;
			}
			ls = null;
		}
		return true;
	}

	/**
	 * 会弹一次框
	 *
	 * @param context
	 * @return
	 */
	public static boolean getRootAndEnsureSuValid(Context context) {
		if (context == null) {
			return false;
		}
		LinuxShell ls = new LinuxShell();
		if (!ls.getRootAuthorityAndEnsureValid(context)) {
			ls = null;
			return false;
		}
		ls = null;
		return true;
	}

	public static boolean hasSuperUserApp(Context ctx) {
		boolean ret = false;
		PackageManager pm = ctx.getPackageManager();
		List<PackageInfo> packInfos = pm.getInstalledPackages(PackageManager.PERMISSION_GRANTED);
		if (packInfos != null) {
			for (PackageInfo packinfo : packInfos) {
				String packageName = packinfo.packageName;
				if (packageName.equalsIgnoreCase("com.noshufou.android.su")
						|| packageName.equalsIgnoreCase("com.miui.uac")) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}

	/**
	 * 拷贝root权限备份恢复应用的二进制文件到/data/data/com.jiubang.go.backup/files/目录下
	 *
	 * @param ctx
	 * @return
	 */
	public static boolean copyBackupBinaryFileToSystemDirectory(Context ctx) {
		boolean ret = true;
		if (ctx == null) {
			return ret;
		}
		String desDirPath = ctx.getFilesDir().getAbsolutePath();
		if (!desDirPath.endsWith(File.separator)) {
			desDirPath += File.separator;
		}
		String backupFilePath = desDirPath + Constant.BACKUP_BINARY_FILE_BACKUP;
		String busyboxFilePath = desDirPath + Constant.BACKUP_BINARY_FILE_BUSYBOX;
		// String socketFilePath = desDirPath +
		// Constant.BACKUP_BINARY_FILE_SOCKET_CLIENT;
		File desDirFile = new File(desDirPath);
		if (!desDirFile.exists()) {
			ret = desDirFile.mkdirs();
		}
		if (!ret) {
			return ret;
		}

		AssetManager am = ctx.getAssets();
		int rc = 0;
		byte[] buffer = new byte[M4096];

		try {
			// 拷贝backup可执行文件
			InputStream is = am.open(Constant.BACKUP_BINARY_FILE_BACKUP);
			FileOutputStream fos = new FileOutputStream(new File(backupFilePath));
			while ((rc = is.read(buffer)) != -1) {
				fos.write(buffer, 0, rc);
			}
			is.close();
			fos.close();

			// 拷贝busybox文件
			is = am.open(Constant.BACKUP_BINARY_FILE_BUSYBOX);
			fos = new FileOutputStream(new File(busyboxFilePath));
			rc = 0;
			while ((rc = is.read(buffer)) != -1) {
				fos.write(buffer, 0, rc);
			}
			is.close();
			fos.close();

			// 拷贝socket_client文件
			// is = am.open(Constant.BACKUP_BINARY_FILE_SOCKET_CLIENT);
			// fos = new FileOutputStream(new File(socketFilePath));
			// rc = 0;
			// while((rc = is.read(buffer)) != -1){
			// fos.write(buffer, 0, rc);
			// }
			// is.close();
			// fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		}

		if (!ret) {
			return ret;
		}

		// 更改文件的执行权限
		LinuxShell ls = new LinuxShell();
		ret = ls.chmod("755", new String[] { backupFilePath, busyboxFilePath
		/*
		 * , socketFilePath
		 */}, false);
		return ret;
	}

	public static boolean checkInternalSdCardReady() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	// 该接口比较耗时，注意不能在循环中使用
	public static boolean checkSdCardReady(Context context) {
		return !TextUtils.isEmpty(getDefalutValidSdPath(context));
	}

	public static Drawable loadIconFromPackageName(Context context, String packageName) {
		if (context == null) {
			return null;
		}
		Drawable icon = null;
		final PackageManager pm = context.getPackageManager();
		if (pm != null) {
			try {
				icon = pm.getApplicationIcon(packageName);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				icon = null;
			}
		}
		return icon;
	}

	public static Drawable loadIconFromAPK(Context context, String apkFullPath) {
		if (context == null || apkFullPath == null || apkFullPath.equals("")) {
			return null;
		}
		File apkFile = new File(apkFullPath);
		if (!apkFile.exists()) {
			return null;
		}
		Drawable icon = null;

		try {
			Class pkgParserCls = Class.forName(PACKAGE_PARSER_PATH);
			Class[] typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = new Object[1];
			valueArgs[0] = apkFullPath;
			Object pkgParser = pkgParserCt.newInstance(valueArgs);

			typeArgs = new Class[M4];
			typeArgs[0] = File.class;
			typeArgs[1] = String.class;
			typeArgs[2] = DisplayMetrics.class;
			typeArgs[M3] = Integer.TYPE;
			Method mPkgParserParsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage",
					typeArgs);

			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();
			valueArgs = new Object[M4];
			valueArgs[0] = apkFile;
			valueArgs[1] = apkFullPath;
			valueArgs[2] = metrics;
			valueArgs[M3] = M0;
			Object pkgParserPkg = mPkgParserParsePackageMtd.invoke(pkgParser, valueArgs);

			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
			ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
			Class assetMagCls = Class.forName(ASSET_MANAGER_PATH);
			Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
			Object assetMag = assetMagCt.newInstance((Object[]) null);
			typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Method mMssetMagaddAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath",
					typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkFullPath;
			mMssetMagaddAssetPathMtd.invoke(assetMag, valueArgs);

			Resources res = context.getResources();
			typeArgs = new Class[M3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();
			Constructor resCt = Resources.class.getConstructor(typeArgs);
			valueArgs = new Object[M3];
			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();
			res = (Resources) resCt.newInstance(valueArgs);

			icon = res.getDrawable(info.icon);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return icon;
	}

	public static String getPackageNameFromApk(Context context, File apkFile) {
		if (context == null || apkFile == null || !apkFile.exists()) {
			return null;
		}
		PackageManager pm = context.getPackageManager();
		PackageInfo pi = pm.getPackageArchiveInfo(apkFile.getAbsolutePath(),
				PackageManager.GET_ACTIVITIES);
		if (pi != null) {
			return pi.packageName;
		}
		return null;
	}

	public static boolean copyFileToSdCard(Context context, File originFile, String destFilePath) {
		if (originFile == null || !originFile.exists() || originFile.isDirectory()
				|| !checkSdCardReady(context)) {
			return false;
		}
		File destFile = new File(getDefalutValidSdPath(context), destFilePath);
		try {
			if (!destFile.exists()) {
				File parent = destFile.getParentFile();
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}
			} else {
				destFile.delete();
			}
			destFile.createNewFile();
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(originFile));
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFile));
			byte[] buffer = new byte[M8192];
			int len;
			while ((len = bis.read(buffer)) != -1) {
				bos.write(buffer, 0, len);
			}
			bos.flush();
			bis.close();
			bos.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * 获取指定包名对应的data目录（有一定风险）
	 *
	 * @param ctx
	 * @param packagename
	 * @return
	 */
	public static String getApplicationDataDirPath(Context ctx, String packagename) {
		if (ctx == null || packagename == null) {
			return null;
		}
		String localApplicationDataDirPath = ctx.getApplicationInfo().dataDir;
		String dataDirPath = localApplicationDataDirPath.substring(0,
				localApplicationDataDirPath.indexOf(ctx.getPackageName()));
		return dataDirPath + packagename;
	}

	/**
	 * 确保文件an路径带文件分隔符
	 *
	 * @param filePath
	 * @return
	 */
	public static String ensureFileSeparator(String filePath) {
		if (filePath == null) {
			return null;
		}
		if (!filePath.endsWith(File.separator)) {
			filePath += File.separator;
		}
		return filePath;
	}

	public static boolean encryFile(File srcFile, File descFile, String password) {
		if (srcFile == null || descFile == null) {
			return false;
		}

		EncryptDecrypt ed = new EncryptDecrypt();
		if (!ed.encrypt(srcFile, descFile, password)) {
			descFile.deleteOnExit();
			return false;
		}
		return true;
	}

	public static boolean decryptFile(File srcFile, File descFile, String password) {
		if (srcFile == null || descFile == null) {
			return false;
		}

		EncryptDecrypt ed = new EncryptDecrypt();
		if (!ed.decrypt(srcFile, descFile, password)) {
			descFile.deleteOnExit();
			return false;
		}
		return true;
	}

	public static List<PackageInfo> syncLoadInstalledPackages(Context context) {
		PackageManager pm = context.getPackageManager();
		return pm.getInstalledPackages(PackageManager.PERMISSION_GRANTED);
	}

	public static void asyncLoadInstalledPackages(final Context context,
			final IAsyncTaskListener listener) {
		if (context == null && listener != null) {
			listener.onEnd(false, null, null);
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (listener != null) {
					listener.onEnd(true, syncLoadInstalledPackages(context), null);
				}
			}
		}).start();
	}

	public static int getAndroidSystemVersion() {
		return android.os.Build.VERSION.SDK_INT;
	}

	public static void createGoBackupRootDir(String dir) {
		if (dir == null) {
			return;
		}
		File dirFile = new File(dir);
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
	}

	// 获取Go桌面ID
	public static String getGoLauncherVirtualIMEI(Context context) {
		if (context == null) {
			return null;
		}
		String imei = null;
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(
				GOLauncherStatisticsProviderConstants.CONTENT_DATA_URI,
				GOLauncherStatisticsProviderConstants.IMEI_ID);
		Cursor cursor = null;
		try {
			cursor = contentResolver.query(uri, null, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				int imeiIndex = cursor.getColumnIndex(GOLauncherStatisticsProviderConstants.IMEI);
				imei = cursor.getString(imeiIndex);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
		return imei;
	}

	public static String pad(int c) {
		if (c >= M10) {
			return String.valueOf(c);
		} else {
			return "0" + String.valueOf(c);
		}
	}

	private static String[] getMount() {
		ArrayList<String> mountsResult = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		byte[] buffer = new byte[M1024];
		int rc = 0;

		try {
			Process process = new ProcessBuilder().command("mount").redirectErrorStream(true)
					.start();
			process.waitFor();

			InputStream is = process.getInputStream();
			while ((rc = is.read(buffer)) != -1) {
				sb.append(new String(buffer, 0, rc));
			}
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		String str = sb.substring(0);
		String[] lines = str.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String temp = lines[i];
			// 如果行内有挂载路径且为vfat类型，说明可能是内置或者外置sd的挂载点
			if ((-1 != temp.indexOf("/sdcard") || -1 != temp.indexOf("/mnt") || -1 != temp
					.indexOf("/storage"))
					&& -1 != temp.indexOf("vfat")
					&& -1 == temp.indexOf("/asec")) {
				// 再用空格分隔
				String[] blocks = temp.split("\\s");
				for (int j = 0; j < blocks.length; j++) {
					if (-1 != blocks[j].indexOf("/sdcard") || -1 != blocks[j].indexOf("/mnt")
							|| -1 != temp.indexOf("/storage")) {
						// Test if it is the external sd card.
						//						 Log.d("TEST", "testMount : blocks[" + j + "] = " +
						//						 blocks[j]);
						if (!mountsResult.contains(blocks[j])) {
							mountsResult.add(blocks[j]);
						}
					}
				}
			}
		}
		String[] result = new String[mountsResult.size()];
		result = mountsResult.toArray(result);
		mountsResult.clear();
		return result;
	}

	private static String[] getVoldfstab() {
		ArrayList<String> voldfstabResult = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		byte[] buffer = new byte[M1024];
		int rc = 0;

		try {
			Process process = new ProcessBuilder().command("sh").redirectErrorStream(true).start();
			process.getOutputStream().write(
					new String("cat /system/etc/vold.fstab; exit \n").getBytes());
			process.waitFor();

			InputStream is = process.getInputStream();
			while ((rc = is.read(buffer)) != -1) {
				sb.append(new String(buffer, 0, rc));
			}
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		String str = sb.substring(0);
		String[] lines = str.split("\n");
		for (int i = 0; i < lines.length; i++) {
			// 过滤注释行
			if (lines[i].startsWith("#")) {
				continue;
			}

			String temp = lines[i];
			// 如果行内有挂载路径且为vfat类型，说明可能是内置或者外置sd的挂载点
			if (-1 != temp.indexOf("/sdcard") || -1 != temp.indexOf("/mnt")
					|| -1 != temp.indexOf("/storage")) {
				// 再用空格分隔
				String[] blocks = temp.split("\\s");
				for (int j = 0; j < blocks.length; j++) {
					if (-1 != blocks[j].indexOf("/sdcard") || -1 != blocks[j].indexOf("/mnt")
							|| -1 != blocks[j].indexOf("/storage")) {
						// Test if it is the external sd card.
						//						 Log.d("TEST", "testVoldfstab : blocks[" + j + "] = " +
						//						 blocks[j]);
						if (!voldfstabResult.contains(blocks[j])) {
							voldfstabResult.add(blocks[j]);
						}
					}
				}
			}
		}

		String[] result = new String[voldfstabResult.size()];
		result = voldfstabResult.toArray(result);
		voldfstabResult.clear();
		return result;
	}

	/**
	 * 获取所有sd卡路径
	 *
	 * @return
	 */
	public static String[] getAllSdPath() {
		String[] mounts = getMount();
		String[] voldfstabs = getVoldfstab();

		if (voldfstabs == null || mounts == null) {
			// TEST
			// Log.d("GOBackup", "getAllSdPath : mounts = " + mounts +
			// ", voldfstabs = " + voldfstabs);
			return null;
		}

		ArrayList<String> pathList = new ArrayList<String>();
		for (String voldfstabItem : voldfstabs) {
			for (String mountItem : mounts) {
				if (mountItem.equals(voldfstabItem)) {
					voldfstabItem = Util.ensureFileSeparator(voldfstabItem);
					pathList.add(voldfstabItem);
				}
			}
		}

		// 有些机型，通过mount和分析vold.fstab文件，解析不出来存储路径，通过标准接口获取
		String internalSdPath = getInternalSdPath();
		if (internalSdPath != null && !pathList.contains(internalSdPath)) {
			pathList.add(internalSdPath);
		}

		String[] result = new String[pathList.size()];
		result = pathList.toArray(result);
		pathList.clear();
		return result;
	}

	/**
	 * 测试目录是否有效
	 * @param path
	 * @return
	 */
	public static boolean isPathValid(String path) {
		if (TextUtils.isEmpty(path)) {
			return false;
		}

		File pathFile = new File(path);
		if (!pathFile.isDirectory()) {
			return false;
		}

		File testFile = new File(path, "test.test");
		if (testFile.exists()) {
			testFile.delete();
		}
		boolean ret = false;
		try {
			ret = testFile.createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (testFile.exists()) {
				testFile.delete();
			}
		}
		return ret;
	}

	/**
	 * 获取内置或者默认sd路径
	 *
	 * @return
	 */
	public static String getInternalSdPath() {
		if (!checkInternalSdCardReady()) {
			return null;
		}
		File sdDirectory = Environment.getExternalStorageDirectory();
		if (sdDirectory != null) {
			return ensureFileSeparator(sdDirectory.getAbsolutePath());
		}
		return null;
	}

	/**
	 * 获取第二外置sd卡路径
	 *
	 * @return
	 */
	private static String getSecondExternalSdPath() {
		String result = null;
		String[] externalSdPaths = getAllSdPath();
		if (externalSdPaths != null && externalSdPaths.length > 0) {
			String defaultExternalSdPath = Util.ensureFileSeparator(getInternalSdPath());
			for (String str : externalSdPaths) {
				str = Util.ensureFileSeparator(str);
				if (!(str.equals(defaultExternalSdPath))) {
					result = str;
					break;
				}
			}
		}

		return isPathValid(result) ? result : null;
	}

	public static boolean isInternalSdPath(String path) {
		if (path == null) {
			return false;
		}
		path = ensureFileSeparator(path);
		String internalPath = ensureFileSeparator(getInternalSdPath());
		return TextUtils.equals(path, internalPath);
	}

	/**
	 * 获取默认有效的备份根路径
	 *
	 * @param context
	 * @return
	 */
	public static String getDefalutValidBackupRootPath(Context context) {
		String backupRootPath = getDefalutValidSdPath(context);
		if (backupRootPath == null) {
			return null;
		}

		return backupRootPath + Constant.GOBACKUP_ROOT_DIR + File.separator;
	}

	public static String getSdRootPathOnPreference(Context context) {
		if (context == null) {
			return null;
		}
		String sdPath = PreferenceManager.getInstance().getString(context,
				PreferenceManager.KEY_BACKUP_SD_PATH, null);
		return TextUtils.isEmpty(sdPath) ? null : sdPath;
	}

	/**
	 * 获取默认有效的SD根路径
	 * @param context
	 * @return
	 */
	public static String getDefalutValidSdPath(Context context) {
		String backupRootPath = null;
		// 检查一下原来是否有记录，原来的记录是否可用
		PreferenceManager pm = PreferenceManager.getInstance();
		backupRootPath = pm.getString(context, PreferenceManager.KEY_BACKUP_SD_PATH, null);

		boolean needReset = backupRootPath == null || !isPathValid(backupRootPath);

		if (needReset) {
			//如果需要重置
			//获取所有SD卡根路径
			String[] allSdPaths = getAllSdPath();
			if (allSdPaths == null || allSdPaths.length == 0) {
				return null;
			}

			String internalPath = getInternalSdPath();
			if (internalPath == null) {
				// 内置存储器为空
				backupRootPath = allSdPaths[0];
			} else {
				// 如果内置SD卡不为空
				String externalPath = null;
				final int lenght = allSdPaths.length;
				// 优先尝试使用外置的SD卡
				for (int i = 0; i < lenght; i++) {
					if (!TextUtils.equals(ensureFileSeparator(allSdPaths[i]),
							ensureFileSeparator(internalPath))) {
						externalPath = allSdPaths[i];
						break;
					}
				}
				backupRootPath = externalPath == null ? internalPath : externalPath;
			}
		}

		if (backupRootPath == null) {
			return null;
		}
		return ensureFileSeparator(backupRootPath);
	}

	public static void killRunningProcess(Context ctx, String packageName) {
		ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		// ArrayList<RunningAppProcessInfo> runningAppInfos =
		// (ArrayList<RunningAppProcessInfo>) am.getRunningAppProcesses();
		// if(runningAppInfos != null){
		// for(RunningAppProcessInfo info : runningAppInfos){
		// Log.d("GOBackup", "killRunningProcess : packagename = " + packageName
		// + ", info.processName = " + info.processName);
		// if(info.processName.equals(packageName)){
		// Log.d("GOBackup",
		// "~~~~~~~~~~~~~~~~~~~~~~killRunningProcess : packagename = " +
		// packageName);
		//
		// am.restartPackage(packageName);
		// break;
		// }
		// }
		// }
		try {
			am.restartPackage(packageName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void forceKillProcess(Context ctx, String packageName) {
		RootShell shell = null;
		try {
			 shell = RootShell.startShell();
		} catch (Exception e) {
			return;
		}
		
		ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningAppProcessInfo> runningAppInfos = (ArrayList<RunningAppProcessInfo>) am
				.getRunningAppProcesses();
		if (runningAppInfos != null) {
			for (RunningAppProcessInfo info : runningAppInfos) {
				// Log.d("GOBackup", "info.processName = " + info.processName);
				if (info.processName.equals(packageName)) {
					String cmd = "kill -9 " + info.pid + "; echo kill_result=$?";
					new RootShell.Command(cmd).execute(shell);
					break;
				}
			}
		}
	}

	public static boolean isUserDictionaryEmpty(Context context) {
		if (context == null) {
			return false;
		}
		Cursor cursor = UserDictionaryBackupEntry.getAllWords(context);
		if (cursor == null) {
			return true;
		}
		try {
			return cursor.getCount() <= 0;
		} finally {
			cursor.close();
		}
	}

	public static boolean copyFileWithRootProcess(Context context, RootShell rp,
			String srcFilePath, String desFilePath) {
		if (context == null || rp == null || srcFilePath == null || desFilePath == null) {
			return false;
		}
		boolean ret = false;
		String internalDataDir = Util.ensureFileSeparator(context.getFilesDir().getAbsolutePath());
		String command = "";
		command += internalDataDir + "busybox cp ";
		command += srcFilePath + " ";
		command += desFilePath + ";";
		command += "echo result=$?";
		String result = new RootShell.Command(command).execute(rp);
		if (result != null) {
			ret = result.contains("result=0");
		}
		return ret;
	}

	public static void reboot() {
		try {
			new RootShell.Command("reboot").execute(RootShell.startShell());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isCollectionEmpty(Collection<?> collection) {
		return collection == null || collection.size() <= 0;
	}

	public static String getDefaultDbPath(Context context, String dbName) {
		return Util.ensureFileSeparator(context.getFilesDir().getParent()) + "databases"
				+ File.separator + dbName;
	}

	public static String getInternalDatabasePath(Context context, String dbName) {
		if (context == null || dbName == null) {
			return null;
		}

		String defaultInternalDbPath = getDefaultDbPath(context, dbName);
		String internalDbPath = defaultInternalDbPath;
		File internalDbFile = context.getDatabasePath(dbName);
		internalDbPath = internalDbFile != null
				? internalDbFile.getAbsolutePath()
				: defaultInternalDbPath;
		return internalDbPath;
	}

	public static int calcCollectionsHashCode(Collection<?> collection) {
		if (isCollectionEmpty(collection)) {
			return 0;
		}
		int hashCode = 0;
		Iterator<?> it = collection.iterator();
		Object obj = null;
		while (it.hasNext()) {
			obj = it.next();
			hashCode += obj.hashCode();
		}
		return hashCode;
	}

	/**
	 * 设置硬件加速
	 *
	 * @param view
	 * @param accelerate
	 */
	public static void setHardwareAccelerated(View view, int mode) {
		if (sLevelUnder3) {
			return;
		}
		try {
			if (null == sAcceleratedMethod) {
				sAcceleratedMethod = View.class.getMethod("setLayerType", new Class[] {
						Integer.TYPE, Paint.class });
			}
			sAcceleratedMethod.invoke(view, new Object[] { Integer.valueOf(mode), null });
		} catch (Throwable e) {
			sLevelUnder3 = true;
		}
	}

	public static void clearCookies(Context context) {
		// Edge case: an illegal state exception is thrown if an instance of
		// CookieSyncManager has not be created. CookieSyncManager is normally
		// created by a WebKit view, but this might happen if you start the
		// app, restore saved state, and click logout before running a UI
		// dialog in a WebView -- in which case the app crashes
		@SuppressWarnings("unused")
		CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
	}

	public static File renameFile(File srcFile, String newName) {
		if (srcFile == null || TextUtils.isEmpty(newName)) {
			return null;
		}
		File renamedFile = new File(srcFile.getParentFile(), newName);
		return srcFile.renameTo(renamedFile) ? renamedFile : null;
	}

	public static boolean createDir(String dirPath) {
		File dir = new File(dirPath);
		if (dir.exists()) {
			return true;
		}
		return dir.mkdirs();
	}

	public static byte[] readDataFromFile(String path) {
		byte[] data = null;
		try {
			FileInputStream fis = new FileInputStream(path);
			try {
				data = new byte[fis.available()];
				int len = 0;
				int i = 0;
				while ((len = fis.read()) != -1) {
					data[i++] = (byte) len;
				}
			} finally {
				fis.close();
			}
		} catch (IOException e) {
			return null;
		}
		return data;
	}

	public static boolean isWifiEnable(Context context) {
		if (context == null) {
			return false;
		}
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return wifiManager.isWifiEnabled();
	}

	public static boolean isNetworkValid(Context context) {
		if (context == null) {
			return false;
		}
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}

		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo == null || !networkInfo.isAvailable()) {
			return false;
		}
		return true;
	}

	/**
	 * <br>功能简述:判断当前是否大陆用户
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param context
	 * @return
	 */
	public static boolean isInland(Context context) {
		// 测试情况下返回不是大陆版
		if (IS_DEBUG) {
			return false;
		}

		if (context == null) {
			return false;
		}

		// 从系统服务上获取了当前网络的MCC(移动国家号)，进而确定所处的国家和地区
		TelephonyManager manager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (manager == null) {
			return false;
		}

		//SIM卡状态
		String simOperator = manager.getSimOperator();
		if ((manager.getSimState() != TelephonyManager.SIM_STATE_READY)
				|| TextUtils.isEmpty(simOperator)) {
			// SIM卡不存在或有问题
			String country = Locale.getDefault().getCountry();
			if (TextUtils.isEmpty(country)) {
				return false;
			}

			if (country.contains("CN")) {
				return true;
			}
			return false;

		}

		// SIM卡正常
		// 中国大陆的前5位是(46000)
		// 中国移动：46000、46002
		// 中国联通：46001
		// 中国电信：46003
		return simOperator.startsWith("460");
	}

	public static String getCountryCode(Context context) {
		// SIM卡运营商的国家代码
		TelephonyManager manager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String simOperator = null;
		if (manager != null) {
			simOperator = manager.getSimCountryIso();
		}

		if (manager == null || (manager.getSimState() != TelephonyManager.SIM_STATE_READY)
				|| TextUtils.isEmpty(simOperator)) {
			// SIM卡不存在或有问题，从手机语言里面的国家代码
			String country = Locale.getDefault().getCountry();
			return country;

		}
		//SIM卡状态
		return simOperator;

	}

	public static String getVersionName(Context context) {
		final String unknownVersion = "unknown_version";
		if (context == null) {
			return unknownVersion;
		}
		PackageManager pm = context.getPackageManager();
		if (pm != null) {
			try {
				PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
				if (pi != null) {
					return pi.versionName != null ? pi.versionName : unknownVersion;
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return unknownVersion;
	}

	public static int getVersionCode(Context context) {
		PackageManager pm = context.getPackageManager();
		if (pm != null) {
			try {
				PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
				if (pi != null) {
					return pi.versionCode;
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	//	public static String getMd5sum(Context context, String filePath) {
	//		if (filePath == null || context == null) {
	//			return null;
	//		}
	//
	//		String md5sum = null;
	//		Process process = null;
	//		DataOutputStream dos = null;
	//		DataInputStream dis = null;
	//
	//		String backupPath = context.getFilesDir() + File.separator + "backup";
	//		String busyboxPath = context.getFilesDir() + File.separator + "busybox";
	//		String cmd = backupPath + " " + "md5sum" + " " + busyboxPath + " " + filePath;
	//		cmd += "\n";
	//
	//		try {
	//			process = new ProcessBuilder("sh").redirectErrorStream(true).start();
	//			dos = new DataOutputStream(process.getOutputStream());
	//			dis = new DataInputStream(process.getInputStream());
	//			dos.writeBytes(cmd);
	//			dos.writeBytes("exit \n");
	//			dos.flush();
	//			process.waitFor();
	//
	//			String result = null;
	//			final int max_size = 256;
	//			byte[] buffer = new byte[max_size];
	//			int rc = dis.available();
	//			if (rc > 0) {
	//				rc = dis.read(buffer, 0, max_size);
	//				if (rc > 0) {
	//					result = new String(buffer, 0, rc);
	//				}
	//			}
	//			buffer = null;
	//
	//			if (result != null) {
	//				if (result.contains("GO_FAILURE")) {
	//					// 失败
	//					md5sum = null;
	//				} else {
	//					String[] allResults = result.split("\n");
	//					if (allResults != null) {
	//						String md5Prefix = "md5sum_result=";
	//						for (String str : allResults) {
	//							if (str.startsWith(md5Prefix)) {
	//								md5sum = str.substring(md5Prefix.length());
	//							}
	//						}
	//					}
	//				}
	//			}
	//
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		} catch (InterruptedException e) {
	//			e.printStackTrace();
	//		} finally {
	//			try {
	//				if (dis != null) {
	//					dis.close();
	//					dis = null;
	//				}
	//				if (dos != null) {
	//					dos.close();
	//					dos = null;
	//				}
	//				process = null;
	//			} catch (IOException e) {
	//			}
	//		}
	//
	//		return md5sum;
	//	}

	public static long getAvaliableRamSize(Context context) {
		if (context == null) {
			return 0;
		}
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		return mi.availMem;
	}

	public static String trimAllSpace(String str) {
		return str == null ? str : str.replaceAll("^[\\s　]*|[\\s　]*$", "");
	}
}
