package com.zhidian.wifibox.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.DisplayMetrics;

import com.ta.common.TAStringUtils;

/**
 * 下载模块工具类
 * 
 * @author xiedezhi
 * 
 */
public class DownloadUtil {
	/**
	 * 极速模式下默认下载速度为2M/S
	 */
	public static final int sXDownloadSpeed = 2048;

	/**
	 * 普通模式下根据url获取对应的APK文件路径
	 */
	public static String getCApkFileFromUrl(String url) {
		if (FileUtil.isSDCardAvaiable()) {
			return PathConstant.C_APK_ROOTPATH
					+ TAStringUtils.getFileNameFromUrl(url);
		} else {
			return PathConstant.C_APK_ROOTPATH_CACHE
					+ TAStringUtils.getFileNameFromUrl(url);
		}
	}

	/**
	 * 普通模式下根据url获取对应的临时APK文件路径
	 */
	public static String getCTempApkFileFromUrl(String url) {
		if (FileUtil.isSDCardAvaiable()) {
			return PathConstant.C_APK_ROOTPATH
					+ TAStringUtils.getFileNameFromUrl(url)
					+ PathConstant.TEMP_SUFFIX;
		} else {
			return PathConstant.C_APK_ROOTPATH_CACHE
					+ TAStringUtils.getFileNameFromUrl(url)
					+ PathConstant.TEMP_SUFFIX;
		}
	}

	/**
	 * 极速模式下根据url获取对应的APK文件路径
	 */
	public static String getXApkFileFromUrl(String url) {
		if (FileUtil.isSDCardAvaiable()) {
			return PathConstant.X_APK_ROOTPATH
					+ TAStringUtils.getFileNameFromUrl(url);
		} else {
			return PathConstant.X_APK_ROOTPATH_CACHE
					+ TAStringUtils.getFileNameFromUrl(url);
		}
	}

	/**
	 * 极速模式下根据url获取对应的临时APK文件路径
	 */
	public static String getXTempApkFileFromUrl(String url) {
		if (FileUtil.isSDCardAvaiable()) {
			return PathConstant.X_APK_ROOTPATH
					+ TAStringUtils.getFileNameFromUrl(url)
					+ PathConstant.TEMP_SUFFIX;
		} else {
			return PathConstant.X_APK_ROOTPATH_CACHE
					+ TAStringUtils.getFileNameFromUrl(url)
					+ PathConstant.TEMP_SUFFIX;
		}
	}

	/**
	 * 获取未安装的apk包名
	 */
	public static String getApkFilePackName(Context ctx, String apkPath) {
		File apkFile = new File(apkPath);
		if (!apkFile.exists()) {
			return "";
		}
		String PATH_PackageParser = "android.content.pm.PackageParser";
		try {
			// 反射得到pkgParserCls对象并实例化,有参数
			Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
			Class<?>[] typeArgs = { String.class };
			Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = { apkPath };
			Object pkgParser = pkgParserCt.newInstance(valueArgs);

			// 从pkgParserCls类得到parsePackage方法
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();// 这个是与显示有关的, 这边使用默认
			typeArgs = new Class<?>[] { File.class, String.class,
					DisplayMetrics.class, int.class };
			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod(
					"parsePackage", typeArgs);

			valueArgs = new Object[] { new File(apkPath), apkPath, metrics, 0 };

			// 执行pkgParser_parsePackageMtd方法并返回
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,
					valueArgs);

			// 从返回的对象得到名为"applicationInfo"的字段对象
			if (pkgParserPkg == null) {
				return "";
			}
			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField(
					"applicationInfo");

			// 从对象"pkgParserPkg"得到字段"appInfoFld"的值
			if (appInfoFld.get(pkgParserPkg) == null) {
				return "";
			}
			ApplicationInfo info = (ApplicationInfo) appInfoFld
					.get(pkgParserPkg);

			if (info != null && info.packageName != null) {
				return info.packageName;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 向指定URL发送GET方法的请求
	 * 
	 * @param url
	 *            发送请求的URL
	 * @param param
	 *            请求参数，请求参数应该是name1=value1&name2=value2的形式。
	 * @return URL所代表远程资源的响应
	 */
	public static String sendGet(String url) {
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			conn.setConnectTimeout(1000);
			conn.setReadTimeout(2000);
			// 建立实际的连接
			conn.connect();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += "/n" + line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	public static void chmod(File file) {
		try {
			String command = "chmod 777 " + file.getAbsolutePath();
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(command);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
