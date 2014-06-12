package com.jiubang.ggheart.apps.appmanagement.component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.jiubang.ggheart.apps.gowidget.gostore.util.FileUtil;

/**
 * 命令管理类 (包括：卸载应用命令)
 * 
 * @author zhaojunjie
 * 
 */
public class CommandManager {
	private Process mProcess;
	private BufferedReader mProcessBri;
	private BufferedReader mProcessBre;
	private BufferedWriter mProcessBwo;

	private static boolean isRoot = false;

	/**
	 * 从线程创建是否成功判断手机是否被ROOT
	 * 
	 * @return
	 */
	public static boolean isCreatRootProcess() {
		return isRoot;
	}

	// 初始化单例
	private static CommandManager instance;

	public static synchronized CommandManager getInstance() {

		if (instance == null) {
			instance = new CommandManager();
		}
		return instance;
	}

	private CommandManager() {
		isRoot = initRootProcess();
	}

	/**
	 * 创建Root进程
	 * 
	 * @return
	 */
	private boolean initRootProcess() {
		boolean result = false;
		try {
			if (mProcess == null) {
				mProcess = new ProcessBuilder("su").start();
			}

			if (mProcess == null) {
				return result;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return result;
		} finally {
		}

		result = true;

		return result;
	}

	/**
	 * 取ROOT权限
	 * 
	 * @return
	 */
	public boolean getRoot() {
		boolean isUninstall = false;

		try {
			// mProcess = new ProcessBuilder("su").start();
			mProcessBri = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
			mProcessBre = new BufferedReader(new InputStreamReader(mProcess.getErrorStream()));
			mProcessBwo = new BufferedWriter(new OutputStreamWriter(mProcess.getOutputStream()));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
		}

		String cmd = "";
		cmd += "su";
		cmd += "\n";
		isUninstall = setCommand(cmd, "", "Permission denied", false); // 如果成功，不返回信息

		return isUninstall;
	}

	/**
	 * 卸载应用
	 * 
	 * @param context
	 * @param pkgname
	 *            应用包名，如：com.jb.gosms
	 * @return
	 */
	public boolean useRootUninstall(Context context, String pkgname) {
		boolean isUninstall = false;

		int sdkLevel = Build.VERSION.SDK_INT;
		String cmd = "";
		// 注：如果桌面有此应用widget，桌面会重启。
		if (sdkLevel >= 14) { // android 4.0 以上要指定目录
			cmd += "LD_LIBRARY_PATH=" + System.getenv("LD_LIBRARY_PATH") + " pm uninstall "
					+ pkgname;
		} else {
			cmd += "pm uninstall " + pkgname;
		}
		cmd += "\n";
		isUninstall = setCommand(cmd, "Success", "Failure", true);

		return isUninstall;
	}

	/**
	 * 输入命令
	 * 
	 * @param cmd
	 *            命令内容
	 * @param succeedresult
	 *            命令执行成功结果
	 * @param faildresult
	 *            命令执行失败结果
	 * @param iswaitoutput
	 *            是否需要等待输出
	 * @return
	 */
	private boolean setCommand(String cmd, String succeedresult, String faildresult,
			boolean iswaitoutput) {
		boolean cmdresult = false;
		boolean hasAuthority = false;
		try {
			// ProcessBuilder mProcessbuilder = new
			// ProcessBuilder("/system/bin/sh");
			// mProcessbuilder.redirectErrorStream(true);
			// Process mProcess = mProcessbuilder.start();

			int sdkLevel = Build.VERSION.SDK_INT;
			// Log.d("zjj msg", "cmd = " + cmd);
			mProcessBwo.write(cmd);
			mProcessBwo.flush();

			if (!iswaitoutput) {
				return true;
			}

			char[] buffer = new char[1024];
			int rc = 0;
			while (!hasAuthority) {
				if (mProcessBre.ready()) {
					rc = mProcessBre.read(buffer);
					String error = new String(buffer, 0, rc);
					// Log.i("zjj msg",
					// "RootProcess : getRootAuthority : bre : error = " +
					// error);
					if (error.contains("Permission denied")) {
						cmdresult = false;
						hasAuthority = true;
						break;
					}
				}
				if (mProcessBri.ready()) {
					rc = mProcessBri.read(buffer);
					if (rc > 0) {
						String result = new String(buffer, 0, rc);
						// Log.i("zjj msg",
						// "RootProcess : getRootAuthority : bri : result = " +
						// result);
						if (result.contains(succeedresult)) {
							cmdresult = true;
							hasAuthority = true;
						} else if (result.contains(faildresult)) {
							cmdresult = false;
							hasAuthority = true;
						}
						break;
					}
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			cmdresult = false;
		} finally {
		}
		return cmdresult;
	}

	/**
	 * 判断手机是否已ROOT
	 * 
	 * @return
	 */
	public static boolean findSu() {
		FileFilter suFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if (pathname.isDirectory()) {
					return false;
				}
				String fileName = pathname.getName();
				if (fileName.equalsIgnoreCase("su")) {
					return true;
				}
				return false;
			}
		};

		boolean ret = false;
		File xbinFile = new File("/system/xbin");
		File[] xbinSubFile = xbinFile.listFiles(suFilter);
		if (xbinSubFile != null && xbinSubFile.length > 0) {
			ret = true;
		}
		if (!ret) {
			File sbinFile = new File("/system/sbin");
			File[] sbinSubFile = sbinFile.listFiles(suFilter);
			if (sbinSubFile != null && sbinSubFile.length > 0) {
				ret = true;
			}

			File binFile = new File("/system/bin");
			File[] binSubFile = binFile.listFiles(suFilter);
			if (binSubFile != null && binSubFile.length > 0) {
				ret = true;
			}

			File suFile = new File("su");
			File[] suSubFile = suFile.listFiles(suFilter);
			if (suSubFile != null && suSubFile.length > 0) {
				ret = true;
			}

			File shFile = new File("sh");
			File[] shSubFile = shFile.listFiles(suFilter);
			if (shSubFile != null && shSubFile.length > 0) {
				ret = true;
			}
		}

		return ret;
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
	 * 取环境变量路径
	 * 
	 * @return
	 */
	private static String getEnvPath() {
		return System.getenv("PATH");
	}

	/**
	 * 判断手机是否已被ROOT
	 * 
	 * @return
	 */
	public static boolean IsRoot() {
		boolean result = false;
		String[] envpaths = getEnvPath().split(":");
		for (int i = 0; i < envpaths.length; i++) {
			if (FileUtil.isFileExist(envpaths[i] + "/su")) {
				result = true;
				break;
			}
		}
		return result;
	}
}
