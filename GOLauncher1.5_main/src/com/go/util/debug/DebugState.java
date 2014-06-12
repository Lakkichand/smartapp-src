package com.go.util.debug;

import java.io.File;

import android.os.Environment;

import com.jiubang.ggheart.launcher.LauncherEnv;

/**
 * 通过判断SDCard/GOLauncherEx/debug文件下是否有控制开关状态的相关文件来进行开关控制的工具类
 * 
 * @author huyong
 * 
 */

//CHECKSTYLE:OFF
public class DebugState {

	private final static String BASE_PATH = Environment.getExternalStorageDirectory()
			+ LauncherEnv.Path.LAUNCHER_DIR + "/debug/";

	private final static boolean DEBUG_DISABLE = false; // 不允许debug

	// 是否绘制内存占用
	private final static String MEMORRY = "memory";
	private static boolean sMemoryEnable = false;

	// 是否报告返回数据库文件
	private final static String DATABASE = "database";
	private static boolean sReportDBEnable = false;

	// 是否报告所有app
	private final static String APP = "apps";
	private static boolean sReportAppsEnable = false;

	// 是否打开搜索键截图
	private final static String SCREEN_CAPTURE = "capture";
	private static boolean sScreenCapture = false;
	
	private static final String SHELLPLUGIN = "3dplugin";
	private static boolean sIsUsedShellPlugin = false; 

	/**
	 * 是否绘制内存。
	 * 若在sd卡上GOLauncherEX/debug/目录下存在memory文件，则绘制内存，否则不绘制内存。
	 * 
	 * @author huyong
	 * @return
	 */
	public static boolean isDrawCost() {
		if (DEBUG_DISABLE) {
			return false;
		}

		sMemoryEnable = (sMemoryEnable ? sMemoryEnable : isExistFile(MEMORRY));
		return sMemoryEnable;
	}

	/**
	 * 是否开启搜索键截图
	 * 若在sd卡上GOLauncherEX/debug/目录下存在capture文件，则开启搜索键截图，否则不开启。
	 * 
	 * @author luopeihuan
	 * @return
	 */
	public static boolean isScreenCaptureEnable() {
		if (DEBUG_DISABLE) {
			return false;
		}

		sScreenCapture = (sScreenCapture ? sScreenCapture : isExistFile(SCREEN_CAPTURE));
		return sScreenCapture;
	}
	
	/**
	 * 是否开启3D模式
	 * 若在sd卡上GOLauncherEX/debug/目录下存在3dplugin文件，则使用3D插件模式。
	 * @return
	 */
	public static boolean isUsed3DPlugin() {
		if (DEBUG_DISABLE) {
			return false;
		}
		
		sIsUsedShellPlugin = (sIsUsedShellPlugin ? sIsUsedShellPlugin : isExistFile(SHELLPLUGIN));
		return sIsUsedShellPlugin;
		
	}

	private static boolean isExistFile(String fileName) {
		boolean result = false;
		File file = new File(BASE_PATH + fileName);
		if (file != null && file.exists()) {
			result = true;
		}
		return result;
	}

}
