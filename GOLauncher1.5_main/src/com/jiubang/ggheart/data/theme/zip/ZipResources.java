package com.jiubang.ggheart.data.theme.zip;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

import com.jiubang.ggheart.launcher.LauncherEnv;
/**
 * 
 * <br>类描述:zip主题文件工具
 * <br>功能详细描述:
 * 
 * @author  rongjinsong
 * @date  [2012-11-7]
 */
public class ZipResources {
	public static final String ZIP_THEME_PATH = LauncherEnv.Path.GOT_ZIP_HEMES_PATH;
	public static final String ZIP_POSTFIX = ".go";
	private static HashMap<String, String> sThemePathMap;
	private static String sCurThemeName;
	private static Resources sCurThemeRes;

	/**
	 * 查看sd卡主题是否存在
	 * 
	 * @param packageName
	 * @return
	 */
	public static boolean isZipThemeExist(String packageName) {
		boolean ret = false;
		if (getZipThemeName(packageName) != null) {
			ret = true;
		}
		return ret;
	}
	//	public static boolean isZipThemeExistFortest(String packageName) {
	//		boolean ret = false;
	//		String[] fileList = getSdThemeFileList();
	//		if (fileList == null) {
	//			ret = false;
	//		}
	//		for (int i = 0; i < fileList.length; i++) {
	//			if(fileList[i].contains(packageName)){
	//				ret = true;
	//			}
	//		}
	//		return ret;
	//	}

	/**
	 * <br>功能简述:返回package对应的go主题包文件名
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param packageName
	 * @return
	 */
	public static String getZipThemeName(String packageName) {
		String[] fileList = getSdThemeFileList();
		if (fileList == null) {
			return null;
		}
		for (int i = 0; i < fileList.length; i++) {
			String apkPath = ZIP_THEME_PATH + fileList[i];
			String pkg = getThemePkgFromReflect(apkPath);
			if (pkg != null && pkg.equals(packageName)) {
				return fileList[i];
			}
		}
		return null;
	}

	//	public static void readZipFile(String zipName, String resName) throws Exception {
	//		ZipFile zf = new ZipFile(zipName);
	//		InputStream in = new BufferedInputStream(new FileInputStream(zipName));
	//		ZipInputStream zin = new ZipInputStream(in);
	//		ZipEntry ze;
	//		while ((ze = zin.getNextEntry()) != null) {
	//			if (ze.isDirectory()) {
	//				Enumeration enu = zf.entries();
	//			} else {
	//				long size = ze.getSize();
	//				if (size > 0) {
	//					BufferedReader br = new BufferedReader(new InputStreamReader(
	//							zf.getInputStream(ze)));
	//					String line;
	//					while ((line = br.readLine()) != null) {
	//						System.out.println(line);
	//					}
	//					br.close();
	//				}
	//				System.out.println();
	//			}
	//		}
	//		zin.closeEntry();
	//	}

	/**
	 * 
	 * @param packageName
	 * @param fileName
	 * @return
	 */
	//	public static InputStream createInputStream(String packageName, String fileName) {
	//		try {
	//			String zipFileName = ZIP_THEME_PATH + packageName + ZIP_POSTFIX;
	//			File file = new File(zipFileName);
	//			if (!file.exists()) {
	//				return null;
	//			}
	//			ZipEntry entry = null;
	//			ZipFile zipFile = new ZipFile(zipFileName);
	//			Enumeration enu = zipFile.entries();
	//			while (enu.hasMoreElements()) {
	//				entry = ( ZipEntry ) enu.nextElement();
	//				if (entry.isDirectory()) {
	//					continue;
	//				}
	//				String name = entry.getName();
	//				if (name.indexOf(fileName) > -1) {
	//					return zipFile.getInputStream(entry);
	//				}
	//			}
	//		} catch (IOException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//		return null;
	//	}

	/**
		 * 通过反射获取内置主题的Resources
		 * @param context
		 * @param apkPath
		 * @return
		 * @author rongjinsong
		 */
	public static Resources getThemeResourcesFromReflect(Context context, String packageName) {
		if (null == packageName || packageName.length() < 0) {
			return context.getResources();
		}
		if (sCurThemeName != null && packageName.equals(sCurThemeName) && sCurThemeRes != null) {
			return sCurThemeRes;
		}
		String[] zipThemeNames = getSdThemeFileList();
		if (zipThemeNames != null) {
			String goFilePath = null;
			if (sThemePathMap == null) {
				sThemePathMap = new HashMap<String, String>();
			} else {
				goFilePath = sThemePathMap.get(packageName);
			}
			if (goFilePath == null) {
				for (int i = 0; i < zipThemeNames.length; i++) {
					goFilePath = ZIP_THEME_PATH + zipThemeNames[i];
					String pkg = getThemePkgFromReflect(goFilePath);
					if (pkg != null) {
						sThemePathMap.put(pkg, goFilePath);
					}
					if (pkg != null && pkg.equals(packageName)) {
						break;
					} else {
						goFilePath = null;
					}
				}
			}
			if (goFilePath != null) {
				String path_assetmanager = "android.content.res.AssetManager";
				try {
					// apk包的文件路径
					// 这是一个Package 申明器, 是隐蔽的
					// 构造函数的参数只有一个, apk文件的路径
					Class[] typeArgs = new Class[1];
					typeArgs[0] = String.class;
					Object[] valueArgs = new Object[1];

					Class assetMagCls = Class.forName(path_assetmanager);
					Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);
					Object assetMag = assetMagCt.newInstance((Object[]) null);
					typeArgs = new Class[1];
					typeArgs[0] = String.class;
					Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath",
							typeArgs);
					valueArgs = new Object[1];
					valueArgs[0] = goFilePath;
					assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);
					Resources res = context.getResources();
					typeArgs = new Class[3];
					typeArgs[0] = assetMag.getClass();
					typeArgs[1] = res.getDisplayMetrics().getClass();
					typeArgs[2] = res.getConfiguration().getClass();
					Constructor resCt = Resources.class.getConstructor(typeArgs);
					valueArgs = new Object[3];
					valueArgs[0] = assetMag;
					valueArgs[1] = res.getDisplayMetrics();
					valueArgs[2] = res.getConfiguration();
					sCurThemeRes = (Resources) resCt.newInstance(valueArgs);
					sCurThemeName = packageName;
					return sCurThemeRes;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return context.getResources();
	}

	/**
	 * 采用反射获取内置主题的包名
	 * @param fileName 
	 * @return
	 * @author rongjinsong
	 */
	public static String getThemePkgFromReflect(String apkPath) {
		if (null == apkPath || apkPath.length() < 0) {
			return LauncherEnv.PACKAGE_NAME;
		}
		if (sThemePathMap != null) {
			Iterator iter = sThemePathMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				Object val = entry.getValue();
				if (val != null && ((String) val).equals(apkPath)) {
					return (String) key;
				}
			}
		}
		String path_packageparser = "android.content.pm.PackageParser";
		try {
			// apk包的文件路径
			// 这是一个Package 申明器, 是隐蔽的
			// 构造函数的参数只有一个, apk文件的路径
			Class[] typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Object[] valueArgs = new Object[1];
			Class pkgParserCls = Class.forName(path_packageparser);
			Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			valueArgs[0] = apkPath;
			Object pkgParser = pkgParserCt.newInstance(valueArgs);
			// 这个是与显示有关的, 里面涉及到一些像素显示等等
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();

			typeArgs = new Class[4];
			typeArgs[0] = File.class;
			typeArgs[1] = String.class;
			typeArgs[2] = DisplayMetrics.class;
			typeArgs[3] = Integer.TYPE;
			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage",
					typeArgs);
			valueArgs = new Object[4];
			valueArgs[0] = new File(apkPath);
			valueArgs[1] = apkPath;
			valueArgs[2] = metrics;
			valueArgs[3] = 0;
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
			// 应用法度信息包, 这个公开的, 不过有些函数, 变量没公开
			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");
			ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);
			// uid 输出为"-1",原因是未安装,体系未分派其Uid。
			Log.d("ANDROID_LAB", "pkg:" + info.packageName + " uid=" + info.uid);
			if (sThemePathMap == null) {
				sThemePathMap = new HashMap<String, String>();
			}
			if (info.packageName != null) {
				sThemePathMap.put(info.packageName, apkPath);
			}
			return info.packageName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return LauncherEnv.PACKAGE_NAME;

	}
	/**
	 * <br>功能简述:扫描所有sd卡内go主题
	 * <br>功能详细描述:
	 * <br>注意:
	 * @return
	 */
	public static String[] getSdThemeFileList() {
		String[] zipThemeNames = null;
		File dir = new File(ZIP_THEME_PATH);
		if (dir.exists()) {
			FilenameFilter filter = new FilenameFilter() {
				private Pattern mPattern = Pattern.compile("go");

				@Override
				public boolean accept(File dir, String filename) {
					// TODO Auto-generated method stub
					String nameString = new File(filename).getName();
					String postfix = nameString.substring(nameString.lastIndexOf(".") + 1);
					return mPattern.matcher(postfix).matches();
				}
			};
			zipThemeNames = dir.list(filter);
		}
		return zipThemeNames;
	}

	/**
	 * <br>功能简述:删除主题
	 * <br>功能详细描述:
	 * <br>注意:
	 * @param packageName
	 */
	public static void deleteTheme(String packageName) {
		String fileName = getZipThemeName(packageName);
		File file = new File(ZipResources.ZIP_THEME_PATH + fileName);
		if (file.exists()) {
			file.delete();
		}
	}
}
