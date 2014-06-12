package com.jiubang.ggheart.plugin.shell;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;

import com.go.util.AppUtils;
import com.go.util.debug.DebugState;
import com.jiubang.ggheart.launcher.PackageName;
import com.jiubang.ggheart.plugin.BasePluginFactory;

/**
 * 
 * <br>类描述:
 * <br>功能详细描述:
 * 
 * @author  yangguanxiang
 * @date  [2012-11-16]
 */
public class ShellPluginFactory extends BasePluginFactory {
	private static final boolean DEV_FLAG = DebugState.isUsed3DPlugin();
	private static final String ADMIN_NAME = "com.jiubang.ggheart.plugin.shell.ShellAdmin";
	private static final String[] DEX_ZIP_FILE_NAMES = null/*{ "package_files_1" }*/;
	private static int sShellPluginExist = -1;

	private static IShellManager sShellManager;

	@SuppressWarnings("rawtypes")
	public static void buildShellPlugin(Activity activity) {
		Context ctx = getRemoteContext(activity, PackageName.SHELL_PLUGIN);
		ClassLoader loader = createDexClassLoader(activity, ctx, PackageName.SHELL_PLUGIN,
				DEX_ZIP_FILE_NAMES);
		Class clazz = getPluginAdminClass(activity, ADMIN_NAME, loader);
		try {
			Constructor constructor = clazz.getConstructor(Activity.class, Context.class,
					ClassLoader.class);
			Object pluginMain = constructor.newInstance(activity, ctx, loader);
			Method method = clazz.getMethod("getShellManager");
			sShellManager = (IShellManager) method.invoke(pluginMain);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static IShellManager getShellManager() {
		return sShellManager;
	}

	public static boolean isShellPluginExist(Context context) {
		if (sShellPluginExist == -1) {
			sShellPluginExist = AppUtils.isAppExist(context, PackageName.SHELL_PLUGIN) ? 1 : 0;
		}
		return sShellPluginExist == 1 ? true : false;
	}

	public static boolean isUseShellPlugin(Context context) {
		return DEV_FLAG && isShellPluginExist(context);
	}

}
